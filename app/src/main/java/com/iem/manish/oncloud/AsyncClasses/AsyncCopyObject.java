package com.iem.manish.oncloud.AsyncClasses;

import android.os.AsyncTask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.iem.manish.oncloud.Application;

/**
 * Created by Manish on 1/27/2016.
 */
public class AsyncCopyObject  extends AsyncTask<Void,Void,Void> {


    private AmazonS3Client s3client;
    private Application appInstance;
    public void setDestinationKey(String destinationKey) {
        this.destinationKey = destinationKey;
    }
    private String destinationKey;
    public void setAppInstance(Application appInstance) {
        this.appInstance = appInstance;
    }
    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }
    private String sourceKey;
    private String mBucketName;
    public void setS3client(AmazonS3Client s3client) {
        this.s3client = s3client;
    }
    public void setmBucketName(String mBucketName) {
        this.mBucketName = mBucketName;
    }
    @Override
    protected Void doInBackground(Void... params) {
        appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });
        s3client.copyObject(new CopyObjectRequest(mBucketName, sourceKey,mBucketName,destinationKey));
        return null;
    }
    @Override
    protected void onPostExecute(Void v) {
        appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });
    }
}

