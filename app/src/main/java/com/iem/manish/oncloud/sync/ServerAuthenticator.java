package com.iem.manish.oncloud.sync;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.amazonaws.http.HttpResponse;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.iem.manish.oncloud.KEYS;
import com.iem.manish.oncloud.MainActivity;
import com.iem.manish.oncloud.ui.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by Manish on 3/29/2016.
 */

public class ServerAuthenticator {
    OkHttpClient client = new OkHttpClient();
    String tokenResponse="";
    Context mContext;
    String username;
    String password;
    AccountManager mAccountManager;
    LoginActivity mLoginActivity;
    public ServerAuthenticator(Context context){
        mContext = context;
        mAccountManager = AccountManager.get(context);
    }
    public void getAuthCode(String username,String password){
        RequestBody formBody = new FormBody.Builder()
                .add("username",username)
                .add("password",password)
                .build();
        username = Uri.encode(username);
        password = Uri.encode(password);
        String url = KEYS.AUTHORIZATION_ENDPOINT
                + "?response_type=code"
                + "&client_id="+KEYS.CIENT_ID
                + "&redirect_uri="+KEYS.REDIRECT_URL
                + "&state=abc"
                + "&username="+username
                + "&password="+password;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException throwable) {
                throwable.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // check for response headers
                String location = response.header("location");
                if(location == null){
                    mLoginActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoginActivity.toggleProgress();
                            mLoginActivity.showError("Enter Username and Password");
                        }
                    });
                }
                else{
                    if(location.contains("error")){
                        String errorDescription = Uri.parse(location).getQueryParameter("error_description");
                        errorDescription = Uri.decode(errorDescription);
                        final String finalErrorDescription = errorDescription;
                        mLoginActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLoginActivity.toggleProgress();
                                mLoginActivity.showError(finalErrorDescription);
                            }
                        });
                    }
                    else{
                        String authCode = Uri.parse(location).getQueryParameter("code");
                        getToken(authCode);
                    }
                }
                // if we do not have location header: show 'No Username Password Entered"
                // else if location contains error: show error description
                // else call token
            }
        });
    }
    public void getToken(String auth_code) throws IOException {
        String url = KEYS.TOKEN_ENDPOINT;
        RequestBody formBody = new FormBody.Builder()
                .add("code", auth_code)
                .add("client_id", KEYS.CIENT_ID)
                .add("client_secret",KEYS.SECRET_ACCESS_KEY)
                .add("redirect_uri", KEYS.REDIRECT_URL)
                .add("grant_type", "authorization_code")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(callback);
    }
    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            tokenResponse = response.body().string();
            JSONObject res = null;
            try {
                res = new JSONObject(tokenResponse);
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            String accessToken="";
            String refreshToken="";
            try {
                accessToken = res.getString("access_token");
                refreshToken = res.getString("refresh_token");
                //callResource(access_token);
                createAccount(accessToken);
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    };
    public void userSignUp(String username, String password, String authType, LoginActivity loginActivity) throws Exception {
        this.username = username;
        this.password = password;
        mLoginActivity = loginActivity;
        mLoginActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLoginActivity.toggleProgress();
            }
        });
        getAuthCode(username, password);

    }
    public void createAccount(final String authToken){
                final Intent res = new Intent();
                res.putExtra(AccountManager.KEY_ACCOUNT_NAME, this.username);
                res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, KEYS.ACCOUNT_TYPE);
                res.putExtra(AccountManager.KEY_AUTHTOKEN, authToken);
                res.putExtra("password", this.password);
                finishLogin(res);
    }
    private void finishLogin(Intent intent) {
        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra("password");
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));
        String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
        String authtokenType = "Bearer";
        // Creating the account on the device and setting the auth token we got
        // (Not setting the auth token will cause another call to the server to authenticate the user)
        mAccountManager.addAccountExplicitly(account, accountPassword, null);
        mAccountManager.setAuthToken(account, authtokenType, authtoken);
        Intent i = new Intent(mContext, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(i);
    }
}
