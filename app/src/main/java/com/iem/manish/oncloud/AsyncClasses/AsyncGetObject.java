package com.iem.manish.oncloud.AsyncClasses;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import com.amazonaws.services.s3.AmazonS3;
import java.io.File;
/**
 * Created by Manish on 1/19/2016.
 */
public class AsyncGetObject extends AsyncTask<Void,Void,Void> {
    Context mContext;
    String bucketName;
    AmazonS3 s3client;
    String key;
    boolean downloadFile;
    //Application appInstance;
    public AsyncGetObject(AmazonS3 s3Client, String bucktname, String k,Context c){
        bucketName=bucktname;
        mContext =c;
        s3client = s3Client;
        key = k;
        //appInstance =app;
    }
    long downloadReference;
    DownloadManager downloadManager;
    @Override
    protected Void doInBackground(Void... params) {
        downloadFile = false;
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        String pathToDownload = "OnCloud";
        pathToDownload += File.separator;
        pathToDownload += key;
        //Log.e("Key ", key);
        if(key.lastIndexOf('.')>0) {
            String filename = key.substring(key.lastIndexOf("/") + 1);
            File destinationFile = new File(root + "/" + pathToDownload);


            if (!destinationFile.exists()) {


                downloadManager = (DownloadManager) mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
                Uri Download_Uri = Uri.parse("https://s3.amazonaws.com/test.excellencetech.com/" + Uri.encode(key));
                DownloadManager.Request request = new DownloadManager.Request(Download_Uri);
                request.setTitle("On Cloud Download");
                //Set a description of this download, to be displayed in notifications (if enabled)
                request.setDescription(filename + "is downloading");
                //Set the local destination for the downloaded file to a path within the application's external files directory
                request.setDestinationInExternalPublicDir("OnCloud", key);
                downloadReference = downloadManager.enqueue(request);

            }
        }
        else{

            boolean b = false;
            if(pathToDownload.charAt(pathToDownload.length()-1)==File.separatorChar){
                pathToDownload = pathToDownload.substring(0,pathToDownload.length()-1);
                File folder = new File(pathToDownload);

                if(!folder.exists()){
                    b = folder.mkdir();
                }
            }
        }
        return null;
    }
    @Override
    protected void onPostExecute(Void r){
    }
}