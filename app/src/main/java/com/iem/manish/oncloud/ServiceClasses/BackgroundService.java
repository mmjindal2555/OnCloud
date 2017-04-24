package com.iem.manish.oncloud.ServiceClasses;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.iem.manish.oncloud.AsyncClasses.AsyncFetch;
import com.iem.manish.oncloud.AsyncClasses.AsyncGetObject;
import com.iem.manish.oncloud.AsyncClasses.AsyncPutObject;
import com.iem.manish.oncloud.ProfileCredentialsProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Manish on 1/28/2016.
 */
public class BackgroundService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Toast.makeText(this,"Service Bound",Toast.LENGTH_LONG).show();
        Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show();
        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=manager.getActiveNetworkInfo();
        boolean isAvailavle = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailavle=true;
        }

        if(isAvailavle){
            AmazonS3Client s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
            AsyncFetch a = new AsyncFetch(s3Client, "test.excellencetech.com");
            a.execute();

            ObjectListing ob = null;
            try {
                ob = a.get();
                ArrayList<String> objectKeys = new ArrayList<>();

                List<S3ObjectSummary> s3ObjectSummaries= ob.getObjectSummaries();
                for(S3ObjectSummary s:s3ObjectSummaries){
//                    if(s.getKey().indexOf('.')>0) {

                        objectKeys.add(s.getKey());
                        AsyncGetObject asyncGetObject = new AsyncGetObject(s3Client,"test.excellencetech.com",s.getKey(),this);
                        asyncGetObject.execute();


                }
                AsyncPutObject putObject = new AsyncPutObject(this,s3Client, "test.excellencetech.com",s3ObjectSummaries);
                putObject.execute();


            } catch (InterruptedException | ExecutionException |NullPointerException e) {
                e.printStackTrace();
            }
            stopSelf();
        }
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Let it continue running until it is stopped.
        Handler mUiHandler = new Handler();
        mUiHandler.post(new Runnable(){
            @Override
            public void run() {
                //Toast.makeText(BackgroundService.this, "Service Started", Toast.LENGTH_LONG).show();
                ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo=manager.getActiveNetworkInfo();
                boolean isAvailavle = false;
                if(networkInfo != null && networkInfo.isConnected()){
                    isAvailavle=true;
                }

                if(isAvailavle){
                    AmazonS3Client s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
                    AsyncFetch a = new AsyncFetch(s3Client, "test.excellencetech.com");
                    a.execute();

                    ObjectListing ob = null;
                    try {
                        ob = a.get();
                        ArrayList<String> objectKeys = new ArrayList<>();

                        List<S3ObjectSummary> s3ObjectSummaries= ob.getObjectSummaries();
                        for(S3ObjectSummary s:s3ObjectSummaries){
                            if(s.getKey().indexOf('.')>0) {
                                objectKeys.add(s.getKey());
                                AsyncGetObject asyncGetObject = new AsyncGetObject(s3Client,"test.excellencetech.com",s.getKey(),BackgroundService.this);
                                asyncGetObject.execute();
                            }

                        }
                        AsyncPutObject putObject = new AsyncPutObject(BackgroundService.this,s3Client, "test.excellencetech.com",s3ObjectSummaries);
                        putObject.execute();


                    } catch (InterruptedException | ExecutionException |NullPointerException e) {
                        e.printStackTrace();
                    }
                    stopSelf();
                }


            }
        });
        return START_STICKY;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();
    }
}
