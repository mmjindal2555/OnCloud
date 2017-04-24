package com.iem.manish.oncloud.AsyncClasses;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.iem.manish.oncloud.Application;
import com.iem.manish.oncloud.ServiceClasses.AsyncGetRunnable;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Created by Manish on 1/19/2016.
 */
public class AsyncFetch extends AsyncTask<Void,Void,ObjectListing>{
    AmazonS3 s3client;
    ObjectListing objectListing;

    //Application appInstance;
    String mBucketName;
    //Context mContext;
    public AsyncFetch(AmazonS3 s3Client, String bucketName)
    {
        //mContext = c;
        s3client = s3Client;
        //appInstance = app;
        mBucketName = bucketName;
    }
    @Override
    protected ObjectListing doInBackground(Void... params) {
        /*appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });
*/
        //Toast.makeText(this.mContext,"in Background",Toast.LENGTH_LONG).show();
        try {
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
                    .withBucketName(mBucketName)
                    .withPrefix("mj");
            //Log.d("Async fetch", "Fetching.... "+ Looper.myLooper().getThread().getName());
            //ObjectListing objectListing;

            do {

                objectListing = s3client.listObjects(listObjectsRequest);
                listObjectsRequest.setMarker(objectListing.getNextMarker());
            } while (objectListing.isTruncated());
        }
        catch (Exception e){}

        return objectListing;
    }
    ArrayList<String> objectKeys;
    @Override
    protected void onPostExecute(ObjectListing objectListing1){
        /*appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });
        appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                appInstance.activity.swipe.setRefreshing(false);
            }
        });
*/
        //appInstance.setGetObjectListings(objectListing);
    }
}