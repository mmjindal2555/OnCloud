package com.iem.manish.oncloud;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

/**
 * Created by Manish on 1/27/2016.
 */
public class FileOperations {
    Context mContext;
    MainActivity activity;

    public void setActivity(MainActivity act) {
        activity = act;
    }

    FileOperations(Context context) {
        this.mContext = context;
    }

    public void open(String key) {
        String mime = key.substring(key.lastIndexOf('.') + 1);
        String type = KEYS.getFileType(mime) + "/" + mime;
        File openFile = new File(key);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(openFile), type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        mContext.startActivity(intent);


    }

    public void delete(File file) {
        //file.delete();

        //S3ObjectSummary object = this.activity.objectsInList.get(position);
        //this.activity.app.deleteObject(file);

    }

    public void copy() {

    }

    public void move() {

    }

    public void share() {

    }

    public void rename(int position) {

    }
}