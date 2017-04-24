package com.iem.manish.oncloud.AsyncClasses;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.amazonaws.mobileconnectors.s3.transfermanager.TransferManager;
import com.amazonaws.mobileconnectors.s3.transfermanager.Upload;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;
import com.iem.manish.oncloud.Application;
import com.iem.manish.oncloud.KEYS;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 1/21/2016.
 */
public class AsyncPutObject extends AsyncTask<Void,Void,Void>{
    private AmazonS3 s3client;
    private String mBucketName;
    private String mUri;
    private Context mContext;
    private String mContentType;
    private Application appInstance;
    private String parentFolder;
    private boolean uploadWithUri;
    private boolean uploaMissingFiles;
    String key;
    File mFile;
    public AsyncPutObject(Context context,AmazonS3 client, String bucketName, String uri, String contentType,Application application,String parent){
        mContext = context;
        this.s3client = client;
        this.mBucketName = bucketName;
        this.mUri = uri;
        this.mContentType =contentType;
        this.appInstance = application;
        this.parentFolder = parent;
        uploadWithUri = true;
        uploaMissingFiles = false;
    }
    public AsyncPutObject(Context context,AmazonS3 client, String bucketName,String key, File file, String parent){
        uploadWithUri = false;
        mContext = context;
        this.s3client = client;
        this.mBucketName = bucketName;
        this.key = key;
        this.mFile =file;
        this.parentFolder = parent;
        uploaMissingFiles = false;
    }
    List<S3ObjectSummary> s3ObjectSummaries;
    public AsyncPutObject(Context context, AmazonS3 client, String bucketName, List<S3ObjectSummary> s3ObjectSummaries){
        mContext = context;
        this.s3client = client;
        this.mBucketName = bucketName;
        this.s3ObjectSummaries = s3ObjectSummaries;
        uploaMissingFiles =true;

    }
    @Override
    protected Void doInBackground(Void... params) {
        /*appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });*/
        PutObjectRequest putObjectRequest;
        if(uploadWithUri) {
            String fileName = null;
            Uri uri = Uri.parse(this.mUri);
            ContentResolver cr = mContext.getContentResolver();
            String[] projection = {MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor metaCursor = cr.query(uri, projection, null, null, null);
            if (metaCursor != null) {
                try {
                    if (metaCursor.moveToFirst()) {
                        fileName = metaCursor.getString(0);
                    }
                } finally {
                    metaCursor.close();
                }
            }
            String mKey = mUri.substring(mUri.lastIndexOf('/') + 1);
            //this.s3client.putObject(new PutObjectRequest(mBucketName,mKey,mFile));

            InputStream mContent = null;
            try {
                mContent = mContext.getContentResolver().openInputStream(uri);

                byte[] contentBytes = IOUtils.toByteArray(mContent);
                Long contentLength = Long.valueOf(contentBytes.length);
                ObjectMetadata om = new ObjectMetadata();
                om.setContentLength(contentLength);
                om.setContentType(mContentType);
                //String type1 = uri.get
                //String ext = mContentType.substring(mContentType.indexOf('/') + 1);
                // create a PutObjectRequest passing the folder name suffixed by /
                InputStream i = new ByteArrayInputStream(contentBytes);
               // Log.e("putting object",fileName);
                if (fileName.lastIndexOf('.') > 0 && fileName != null) {
                    putObjectRequest = new PutObjectRequest(mBucketName, parentFolder + fileName, i, om);
                } else {
                    putObjectRequest = new PutObjectRequest(mBucketName, parentFolder + fileName, i, om);
                }
                // send request to S3 to create folder
                s3client.putObject(putObjectRequest);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!uploadWithUri && !uploaMissingFiles){

            s3client.putObject(mBucketName, key, mFile);
        }
        if(uploaMissingFiles){
            Application app = new Application();
            SharedPreferences sharedPref = mContext.getSharedPreferences(KEYS.APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
            String path = sharedPref.getString(KEYS.APP_STORAGE_PATH, "");
            String username = sharedPref.getString(KEYS.USERNAME,"");
            app.checkDevice(new File(path+username));
            ArrayList<File> filesInDevice= app.getFilesInDevice();
            ArrayList<String> files = new ArrayList<>();
            ArrayList<String> objects = new ArrayList<>();
            for(File file:filesInDevice){

                files.add(file.getAbsolutePath().substring(path.length()));

            }
            for(S3ObjectSummary s: this.s3ObjectSummaries){
                objects.add(s.getKey());
            }
            ArrayList<String> missingFiles = new ArrayList<>();
            boolean flagMissing = false;
            for(String file:files){
                flagMissing = false;
                for(String object:objects){
                 //   Log.e("missing",file+" and "+object);
                    if(file.equals(object)){
                       flagMissing = true;
                        break;
                    }
                }
                if(!flagMissing)
                {
                    //Log.e("**********",file);
                    TransferManager transferManager = new TransferManager(s3client);
                    Upload upload = transferManager.upload(mBucketName,file,new File(path+ file));

                    //s3client.putObject(mBucketName, file,new file(path+file));
                }
            }


        }
        return null;
    }
    @Override
    protected void onPostExecute(Void r){
        /*appInstance.activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                appInstance.activity.toggleSync();
            }
        });
        */
        //Uri uri = Uri.parse(this.mUri);

    }
}
