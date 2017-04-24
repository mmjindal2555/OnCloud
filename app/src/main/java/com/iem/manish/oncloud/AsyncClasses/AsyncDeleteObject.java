package com.iem.manish.oncloud.AsyncClasses;

import android.os.AsyncTask;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.iem.manish.oncloud.Application;

/**
 * Created by Manish on 1/27/2016.
 */
public class AsyncDeleteObject extends AsyncTask<Void,Void,Void> {


    private AmazonS3Client s3client;

    public void setAppInstance(Application appInstance) {
        this.appInstance = appInstance;
    }

    private Application appInstance;
    public void setKey(String key) {
        this.key = key;
    }

    private String key;
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

        s3client.deleteObject(new DeleteObjectRequest(mBucketName, key));
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
