package com.iem.manish.oncloud.ServiceClasses;

import android.os.Environment;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;

import java.io.File;


/**
 * Created by Manish on 1/29/2016.
 */
public class AsyncGetRunnable implements Runnable{
    String key;
    String Bucket;
    AmazonS3 amazonS3Client;

    public AsyncGetRunnable(String key, String bucket, AmazonS3 s3Client){
        this.key = key;
        this.Bucket = bucket;
        this.amazonS3Client = s3Client;

    }

    @Override
    public void run() {
        String pathToDownload = Environment.getExternalStorageDirectory() + File.separator + "OnCloud";
        pathToDownload += File.separator;
        String fileName = key.toString();
        File destinationFile = new File(pathToDownload + File.separator + key);
        if (!destinationFile.exists()) {
            amazonS3Client.getObject(new GetObjectRequest(Bucket, key), destinationFile);

        }
    }

}
