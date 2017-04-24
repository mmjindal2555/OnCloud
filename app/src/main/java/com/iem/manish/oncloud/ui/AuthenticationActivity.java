package com.iem.manish.oncloud.ui;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.iem.manish.oncloud.R;

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

public class AuthenticationActivity extends AppCompatActivity {
    static class Gist {
        Map<String, GistFile> files;
    }
    static class GistFile {
        String content;
    }
    String authtoken;
    String AUTHORIZATION_ENDPOINT = "http://sky.excellencetech.co.uk/lime_server/api/authorize.php";
    String TOKEN_ENDPOINT = "http://sky.excellencetech.co.uk/lime_server/api/token.php";
    String RESOURCE_URL = "http://sky.excellencetech.co.uk/lime_server/api/resource.php";
    String CIENT_ID = "RMh53N";
    String SECRET_ACCESS_KEY = "KZrSn7";
    Response mResponse;
    OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    WebView mWebview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        mWebview = (WebView) findViewById(R.id.webView);
        mWebview.loadUrl("http://sky.excellencetech.co.uk/lime_server/api/authorize.php?response_type=code&client_id=RMh53N&redirect_uri=myapp://oncloudresponse/&state=abc");
        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("http:") || url.startsWith("https:")) {
                    return false;
                }
                // Otherwise allow the OS to handle things like tel, mailto, etc.
                else {
                    String code = Uri.parse(url).getQueryParameter("code");
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + "&response_type=code"));
                    startActivity(intent);
                    return true;
                }
            }
        });
    }
    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        Log.e("check","New Intent called "+intent.toString());
        Uri uri = intent.getData();
        if(uri != null && uri.toString().startsWith("myapp://oncloudresponse"))
        {
            String code = uri.getQueryParameter("code");
            Log.e("Response1 : ",code);
            //...
            try {
                run(code);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    String run(String auth_code) throws IOException {
        //String code = this.CIENT_ID + ":" + this.SECRET_ACCESS_KEY;
        String url = TOKEN_ENDPOINT;
        RequestBody formBody = new FormBody.Builder()
                .add("code", auth_code)
                .add("client_id", CIENT_ID)
                .add("client_secret", SECRET_ACCESS_KEY)
                .add("redirect_uri", "myapp://oncloudresponse/")
                .add("grant_type", "authorization_code")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        client.newCall(request).enqueue(callback);

        return null;
    }

    Callback callback = new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String json_response = response.body().string();
            Object obj = new JsonParser().parse(json_response);
            Log.e("Response", json_response);
            JSONObject res = null;
            try {
                res = new JSONObject(json_response);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                String access_token = res.getString("access_token");
                Log.e("Response", access_token);
                callResource(access_token);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };
    public void callResource(String access_token){
        String url = RESOURCE_URL+"?access_token="+access_token;
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                Log.e("Response : ",response.body().string());
            }
        });
    }
}
