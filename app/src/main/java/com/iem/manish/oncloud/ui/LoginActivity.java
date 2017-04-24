package com.iem.manish.oncloud.ui;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iem.manish.oncloud.KEYS;
import com.iem.manish.oncloud.R;
import com.iem.manish.oncloud.sync.ServerAuthenticator;
public class LoginActivity extends Activity {
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";
    EditText mUsername;
    String username;
    String userpass;
    EditText mPassword;
    String mAuthTokenType = "";
    Button mLogin;
    boolean isPressed=false;
    ServerAuthenticator sServerAuthenticate;
    TextView errorMessgae;
    ProgressBar pb;
    public TextView getErrorMessgae() {
        return errorMessgae;
    }

    public void setErrorMessgae(TextView errorMessgae) {
        this.errorMessgae = errorMessgae;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sServerAuthenticate = new ServerAuthenticator(LoginActivity.this);
        getWindow().setStatusBarColor(getResources().getColor(R.color.blue_login));
        mUsername =  (EditText)findViewById(R.id.username);
        mPassword = (EditText)findViewById(R.id.password);
        mLogin = (Button)findViewById(R.id.button);
        errorMessgae = (TextView)findViewById(R.id.error_message);
        pb = (ProgressBar)findViewById(R.id.progressBar);
        pb.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        mLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //Button Pressed
                    mLogin.setTextColor(getResources().getColor(R.color.blue_login));
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    //finger was lifted
                    mLogin.setTextColor(Color.WHITE);
                }
                return false;
            }
        });
        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = mUsername.getText() + "";
                userpass = mPassword.getText() + "";
                username = username.trim();
                userpass = userpass.trim();
                //TODO use the code to request authentication token
                try {
                    sServerAuthenticate.userSignUp(username, userpass, "Public", LoginActivity.this);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void toggleProgress(){
        if(pb.getVisibility()==View.INVISIBLE)
        {
            getErrorMessgae().setVisibility(View.INVISIBLE);
            pb.setVisibility(View.VISIBLE);
        }
        else
        {
            pb.setVisibility(View.INVISIBLE);
        }
    }
    public void progressStatus(){

    }
    public void showError(String error){
        getErrorMessgae().setText(error);
        getErrorMessgae().setVisibility(View.VISIBLE);
    }
}
