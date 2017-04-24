package com.iem.manish.oncloud.Database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;



import com.iem.manish.oncloud.Application;
import com.iem.manish.oncloud.KEYS;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Manish on 2/19/2016.
 */
public class DatabaseTable {

    private static final String TAG = "OnCloudDatabase";

    //The columns we'll include in the dictionary table
    public static final String COL_FILE_NAME = "FILE_NAME";
    public static final String COL_FILE_LOCATION = "FILE_LOCATION";

    private static final String DATABASE_NAME = "ON_CLOUD";
    private static final String FTS_VIRTUAL_TABLE = "FTS";
    private static int DATABASE_VERSION = 2;

    public void setVersion(){
        DATABASE_VERSION++;
    }

    private final DatabaseOpenHelper mDatabaseOpenHelper;

    public DatabaseTable(Context context) {
        int version=1;
        SharedPreferences sharedPref = context.getSharedPreferences(KEYS.APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
        version = sharedPref.getInt(KEYS.DATABASE_VERSION_KEY,1);
        mDatabaseOpenHelper = new DatabaseOpenHelper(context, version);
    }

    private static class DatabaseOpenHelper extends SQLiteOpenHelper {

        private final Context mHelperContext;
        private SQLiteDatabase mDatabase;

        private static final String FTS_TABLE_CREATE =
                "CREATE VIRTUAL TABLE " + FTS_VIRTUAL_TABLE +
                        " USING fts3 (" +
                        COL_FILE_NAME + ", " +
                        COL_FILE_LOCATION + ")";

        DatabaseOpenHelper(Context context, int version) {
            super(context, DATABASE_NAME, null, version);
            mHelperContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            mDatabase = db;
            mDatabase.execSQL(FTS_TABLE_CREATE);
            loadFiles();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
            onCreate(db);
        }
        private void loadFiles(){
            Application app = new Application();
            SharedPreferences sharedPref = mHelperContext.getSharedPreferences(KEYS.APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
            String path = sharedPref.getString(KEYS.APP_STORAGE_PATH, "");
            String username = sharedPref.getString(KEYS.USERNAME,"");
            app.checkDevice(new File(path+username));
            ArrayList<File> filesInDevice= app.getFilesInDevice();
            ArrayList<String> files = new ArrayList<>();
            ArrayList<String> objects = new ArrayList<>();
            for(File file:filesInDevice){
                files.add(file.getAbsolutePath().substring(path.length()));

            }
            String line;
            int i=0;
            for(String file:files) {

                if(file.lastIndexOf('.')>0)
                    file = file.substring(0,file.lastIndexOf('.'));
                if(file.lastIndexOf('/')==file.length()-1)
                    file = file.substring(0,file.lastIndexOf('/'));
                file = file.substring(file.lastIndexOf('/'));
                long id = addFile(file, filesInDevice.get(i).getAbsolutePath());
                if (id < 0) {
                    //Log.e(TAG, "unable to add word: " + file);
                }
                i++;
            }
        }

        public long addFile(String word, String definition) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(COL_FILE_NAME, word);
            initialValues.put(COL_FILE_LOCATION, definition);

            return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
        }
    }
    public Cursor getFileMatches(String query,String parent, String[] columns) {
        String selection = COL_FILE_NAME + " MATCH ?";
        String[] selectionArgs = new String[] {query+"*"};

        return query(selection, selectionArgs, columns);
    }

    private Cursor query(String selection, String[] selectionArgs, String[] columns) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(FTS_VIRTUAL_TABLE);

        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(),
                columns, selection, selectionArgs, null, null, null);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

}
