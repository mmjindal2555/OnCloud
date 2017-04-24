package com.iem.manish.oncloud;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.iem.manish.oncloud.AsyncClasses.AsyncCopyObject;
import com.iem.manish.oncloud.AsyncClasses.AsyncDeleteObject;
import com.iem.manish.oncloud.AsyncClasses.AsyncFetch;
import com.iem.manish.oncloud.AsyncClasses.AsyncGetObject;
import com.iem.manish.oncloud.AsyncClasses.AsyncPutObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
/**
 * Created by Manish on 1/19/2016.
 */
public class Application{
    ObjectListing objectListings;
    Context mContext;
    List<S3ObjectSummary> objects;
    ArrayList<File> filesInDevice;
    AmazonS3Client s3Client;
    String mBucketName;
    public MainActivity activity;
    public Application() {
        filesInDevice = new ArrayList<>();
    }
    public ArrayList<File> getFilesInDevice() {
        return filesInDevice;
    }
    public void setFilesInDevice(ArrayList<File> filesInDevice) {
        this.filesInDevice = filesInDevice;
    }
    public List<S3ObjectSummary> getObjects() {
        return objects;

    }
    public void setObjects(List<S3ObjectSummary> objects) {
        this.objects = objects;
    }
    public Application(Context c, MainActivity activity1){
        mContext = c;
        this.s3Client = new AmazonS3Client(new ProfileCredentialsProvider());
        this.mBucketName = "test.excellencetech.com";
        activity = activity1;
        filesInDevice = new ArrayList<>();
    }
    public void renameObject(File file,String newName){
        String parent = Environment.getExternalStorageDirectory()+"/OnCloud/";
        String key = file.getAbsolutePath().substring(parent.length());
        String destinationKey ="";
        if(key.indexOf('.')>0)
        {
            destinationKey+= key.substring(0,key.lastIndexOf('/')+1)+newName;
            destinationKey+=key.substring(key.lastIndexOf('.'));
        }
        else{
            String withoutlastSlash="";
            if(key.charAt(key.length()-1)=='/')
            withoutlastSlash = key.substring(0,key.length()-1);
            destinationKey+= withoutlastSlash.substring(0,withoutlastSlash.lastIndexOf('/')+1)+newName;
        }
        File from = file;
        String t = parent;
        if(t.lastIndexOf('/')!=t.length()-1){
            t+='/';
        }
        t+=destinationKey;
        File to = new File(t);
        from.renameTo(to);
        copyObject(key,destinationKey);
        this.deleteObject(key);
    }
    public void copyFiles(String source, String destination){
        File to = new File(destination);
        File from = new File(source);
        //Toast.makeText(MainActivity.this, filePath, Toast.LENGTH_LONG).show();

        try {
            InputStream in = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void copyObject(String source,String destination){
        AsyncCopyObject copyObject = new AsyncCopyObject();
        copyObject.setS3client(s3Client);
        copyObject.setmBucketName(mBucketName);
        copyObject.setAppInstance(this);
        copyObject.setSourceKey(source);
        copyObject.setDestinationKey(destination);
        copyObject.execute();
    }
    public void syncObjects() throws UnknownHostException {
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
                        AsyncGetObject asyncGetObject = new AsyncGetObject(s3Client,"test.excellencetech.com",s.getKey(),this.mContext);
                        asyncGetObject.execute();
                    }

                }
                AsyncPutObject putObject = new AsyncPutObject(this.mContext,s3Client, "test.excellencetech.com",s3ObjectSummaries);
                putObject.execute();


            } catch (InterruptedException | ExecutionException |NullPointerException e) {
                e.printStackTrace();
            }
    }
    public void uploadFile(File file,String parent,String key){
        AsyncPutObject putObject = new AsyncPutObject(mContext,s3Client,mBucketName,key,file,parent);
        putObject.execute();
    }
    public void uploadObjects(String uri, String contentType, String parent){

        AsyncPutObject putObject = new AsyncPutObject(this.mContext,this.s3Client,mBucketName,uri,contentType,this, parent);
        putObject.execute();
    }
    public void deleteObject(String from){
        AsyncDeleteObject deleteObject = new AsyncDeleteObject();
        deleteObject.setS3client(s3Client);
        deleteObject.setmBucketName(mBucketName);
        deleteObject.setAppInstance(this);
        deleteObject.setKey(from);
        deleteObject.execute();

    }
    public void checkDevice(File folder1){
        File[] listOfFiles = folder1.listFiles();
        //Log.e(" check ",folder1.getAbsolutePath());
        try{
        for (int i = 0; i < listOfFiles.length; i++) {

                if (listOfFiles[i].isFile()) {
               //     Log.e("file is :", listOfFiles[i].toString());
                    this.filesInDevice.add(listOfFiles[i]);
                } else if (listOfFiles[i].isDirectory()) {
             //       Log.e("Is folder :", listOfFiles[i].toString());
                    this.filesInDevice.add(listOfFiles[i]);
                    this.checkDevice(new File(listOfFiles[i].getAbsolutePath()));
                }
            }

        }
        catch(NullPointerException e) {

        }
    }
}