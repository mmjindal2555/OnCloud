package com.iem.manish.oncloud;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
public class Explorer extends Activity {
    ArrayList<File> folders;
    ListView foldersListView;
    Button cancelButton;
    Button okButton;
    Button backButton;
    ArrayList<File> filesDevice;
    Application application;
    String username = "mj";
    String parentFolder=username+File.separatorChar;
    String mainDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/OnCloud/";
    ListContentsAdapter folderAdapter;
    TextView pathTextView;
    TextView titleTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);
        foldersListView = (ListView)findViewById(R.id.mov_list_view);
        cancelButton = (Button)findViewById(R.id.cancel_transfer);
        okButton = (Button)findViewById(R.id.transfer_button);
        backButton = (Button)findViewById(R.id.transfer_back_button);
        pathTextView = (TextView)findViewById(R.id.transfer_path_text_view);
        titleTextView = (TextView)findViewById(R.id.transfer_title);
        Intent intent = getIntent();
        String action = intent.getStringExtra(KEYS.TRANSFER_ACTION_KEY);
        if(action.equals(KEYS.ACTION_COPY)){
            titleTextView.setText("COPY TO");
        }
        else{
            titleTextView.setText("MOVE TO");
        }
        application = new Application();
        filesDevice = application.getFilesInDevice();
        setFolderList(parentFolder);
        backButton.setEnabled(false);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(parentFolder.charAt(parentFolder.length()-1) == (File.separatorChar)){
                    parentFolder = parentFolder.substring(0,parentFolder.length()-1);
                }
                parentFolder = parentFolder.substring(0,parentFolder.lastIndexOf(File.separatorChar)+1);
                setFolderList(parentFolder);
                pathTextView.setText(parentFolder);
                if(parentFolder.equals(username) || parentFolder.equals(username+"/")){
                    backButton.setEnabled(false);
                }else{
                    backButton.setEnabled(true);
                }

            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Explorer.this.setResult(Activity.RESULT_CANCELED);
                Explorer.this.finish();
            }
        });
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(KEYS.TRANSFER_PATH_KEY, parentFolder);
                Explorer.this.setResult(Activity.RESULT_OK, intent);
                Explorer.this.finish();
            }
        });
    }
    ArrayList<File> filesInList;
    public void setFolderList(String parentFolder){
        pathTextView.setText(Explorer.this.parentFolder);
        this.application.setFilesInDevice(new ArrayList<File>());
        this.application.checkDevice(new File(mainDirectory+username+File.separatorChar));
        filesDevice = this.application.filesInDevice;
        filesInList = new ArrayList<>();
        String leaf;
        for(File file : filesDevice){
            leaf = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('/')+1);
            if(file.getAbsolutePath().equals(mainDirectory+parentFolder+leaf) && file.getAbsolutePath().lastIndexOf('.') < 0)  {
                    filesInList.add( file);
            }
        }
        folders = filesInList;
        folderAdapter = new ListContentsAdapter(folders,Explorer.this);
        foldersListView.setAdapter(folderAdapter);
        foldersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = filesInList.get(position).getAbsolutePath();
                key = key.substring(key.lastIndexOf('/') + 1);
                Explorer.this.parentFolder += key;
                if (Explorer.this.parentFolder.charAt(Explorer.this.parentFolder.length() - 1) != File.separatorChar) {
                    Explorer.this.parentFolder += File.separatorChar;
                }
                Explorer.this.setFolderList(Explorer.this.parentFolder);
                backButton.setEnabled(true);
            }
        });
    }
}