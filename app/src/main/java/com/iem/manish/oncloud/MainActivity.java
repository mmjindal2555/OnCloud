package com.iem.manish.oncloud;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.iem.manish.oncloud.ServiceClasses.BackgroundService;
import com.iem.manish.oncloud.ui.EnterFolderNameDialog;
import com.iem.manish.oncloud.ui.SettingsActivity;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.io.File;
import java.net.UnknownHostException;
import java.util.ArrayList;

import okhttp3.Request;

public class MainActivity extends AppCompatActivity {

    ListView mListView;
    String username = "mj";
    String parentFolder=username+File.separatorChar;
    String mainDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/OnCloud/";
    Application app;
    ArrayList<File> filesInDevice;
    ArrayList<File> filesInList;
    FloatingActionMenu fam;
    ProgressBar pb;
    Toolbar toolbar;
    private AlarmManager alarmMgr;
    private PendingIntent alarmIntent;
    SwipeRefreshLayout swipe;
    TextView pathTextView;
    Drawer result = null;
    SharedPreferences sharedpreferences;
    ContentsAdapter contentsAdapter;
    SearchView searchView;
    GridView mGridView;
    ArrayList<String> filesToTransfer = new ArrayList<>();
    boolean multipleSelection;
    private ActionMode mActionMode;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume(){
        super.onResume();
        Intent intent = getIntent();
        try {
            String s = intent.getStringExtra("parentFolder");
            //MainActivity.this.parentFolder = s;
            if(!s.equals("")) {
                s = s.substring(mainDirectory.length());
                parentFolder = s+"/";
                setList(parentFolder);
            }
            intent.putExtra("parentFolder","");
        }
        catch(NullPointerException e){
        }
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pathTextView = (TextView)findViewById(R.id.path_text_view);
        mListView = (ListView)findViewById(R.id.list);
        mGridView = (GridView)findViewById(R.id.gridView);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pathTextView.setText(parentFolder);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_more_vert_white_24dp));
        //toolbar.setLogo(R.mipmap.ic_launcher);
        setNavDrawer();
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);

        Request req= new Request.Builder().url("").build();

        sharedpreferences = getSharedPreferences(KEYS.APP_PREFERENCE_NAME, Context.MODE_PRIVATE);
        getWindow().setStatusBarColor(getResources().getColor(R.color.secondary));
        app = new Application(this,MainActivity.this);
        app.setFilesInDevice(new ArrayList<File>());
        app.checkDevice(new File(mainDirectory + parentFolder));

        pb = (ProgressBar)findViewById(R.id.syncProgress);
        swipe = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);

        swipe.setColorSchemeColors(Color.BLUE,Color.RED,Color.YELLOW,Color.GREEN);
        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetwrokAvailable())
                    try {
                        app.syncObjects();
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                swipe.setRefreshing(false);
            }
        });

        setList(parentFolder);
        int version = sharedpreferences.getInt(KEYS.DATABASE_VERSION_KEY,1);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(KEYS.APP_STORAGE_PATH, this.mainDirectory);
        editor.putString(KEYS.USERNAME, this.username);
        editor.putInt(KEYS.DATABASE_VERSION_KEY, version);
        editor.apply();
        setUploadMenu();

        Handler mUiHandler = new Handler();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent serviceIntent = new Intent(MainActivity.this, BackgroundService.class);
                alarmIntent = PendingIntent.getService(MainActivity.this, 0, serviceIntent, 0);
                alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 120 * 1000, alarmIntent);
            }
        });

    }
    public void setNavDrawer(){
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withName("Starred")
                .withIcon(R.drawable.ic_star_rate_black_18dp);
        SecondaryDrawerItem item2 = new SecondaryDrawerItem().withName("On Cloud Secondary");
        
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.nav_header_bg)
                .addProfiles(
                        new ProfileDrawerItem().withName("Mojo Jojo").withEmail("mmjindal2555@gmail.com").withIcon(getResources().getDrawable(R.drawable.user_male))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withTranslucentStatusBarShadow(true)
                .addDrawerItems(
                        item1,
                        new DividerDrawerItem(),
                        item2,
                        new SecondaryDrawerItem().withName("On Cloud Secondary Settings")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        Toast.makeText(MainActivity.this,"hello world",Toast.LENGTH_LONG).show();
                        return true;
                    }
                })
                .build();

        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Settings").withIcon(R.drawable.ic_settings_black_18dp));

    }
    public void toggleSync(){
        if(pb.getVisibility() == View.INVISIBLE){
            pb.setVisibility(View.VISIBLE);

        }
        else
            pb.setVisibility(View.INVISIBLE);
    }
    public void setUploadMenu(){
        fam = (FloatingActionMenu)findViewById(R.id.upload_menu);
        FloatingActionButton cameraUpload = (FloatingActionButton)findViewById(R.id.camera_upload);
        FloatingActionButton uploadFile = (FloatingActionButton)findViewById(R.id.upload_file);
        FloatingActionButton createFolder = (FloatingActionButton)findViewById(R.id.create_folder);
        int delay = 400;
        Handler mUiHandler = new Handler();
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fam.showMenuButton(true);
            }
        }, delay);
        fam.setClosedOnTouchOutside(true);
        cameraUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetwrokAvailable()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, KEYS.TAKE_PICTURE_REQUEST_CODE);
                }
                fam.close(true);
            }
        });
        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetwrokAvailable()) {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, "OnCloud Pick file"), KEYS.PICK_FILE_REQUEST_CODE);
                }
                fam.close(true);
            }
        });
        createFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fam.close(true);
                Intent intent = new Intent(MainActivity.this, EnterFolderNameDialog.class);
                startActivityForResult(intent, KEYS.CREATE_FOLDER_REQUEST_CODE);

            }
        });
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == KEYS.TAKE_PICTURE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                copyUploadFile(data);
            }
        }
        else if(requestCode == KEYS.PICK_FILE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                copyUploadFile(data);
            }
        }
        else if(requestCode == KEYS.RENAME_FILE_REQUEST_CODE){
            if(resultCode == RESULT_OK) {

                String fileString = data.getStringExtra("fileSelected");
                String newName = data.getStringExtra("newName");
                File from = new File(fileString);
                this.app.renameObject(from, newName);
                this.setList(parentFolder);
            }
        }
        else if(requestCode == KEYS.CREATE_FOLDER_REQUEST_CODE){
            if(resultCode == RESULT_OK) {

                String newName = data.getStringExtra("newName");
                File folder = new File(mainDirectory+parentFolder+newName);
                if(!folder.exists())
                    folder.mkdir();
                this.setList(parentFolder);
            }
        }
        else if(requestCode == KEYS.COPY_REQUEST_CODE){
            if(resultCode == RESULT_OK){
                String toTransfer = data.getStringExtra(KEYS.TRANSFER_PATH_KEY);
                //Toast.makeText(MainActivity.this,toTransfer,Toast.LENGTH_LONG).show();
                String destination = mainDirectory+toTransfer;
                String sourceObject,destObject;
                for(String source:filesToTransfer) {

                    destination += source.substring(source.lastIndexOf('/'));
                    app.copyFiles(source,destination);
                    sourceObject = source.substring(mainDirectory.length());
                    destObject = destination.substring(mainDirectory.length());
                    //Log.d("copying",sourceObject+" to "+destObject);
                    app.copyObject(sourceObject,destObject);
                }
            }
        }
        else if(requestCode == KEYS.MOVE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String toTransfer = data.getStringExtra(KEYS.TRANSFER_PATH_KEY);
                //Toast.makeText(MainActivity.this,toTransfer,Toast.LENGTH_LONG).show();
                String destination = mainDirectory + toTransfer;
                String sourceObject, destObject;
                for (String source : filesToTransfer) {
                    //Log.d("copying",source+" to "+destination);
                    destination += source.substring(source.lastIndexOf('/'));
                    app.copyFiles(source, destination);
                    new File(source).delete();
                    sourceObject = source.substring(mainDirectory.length());
                    destObject = destination.substring(mainDirectory.length());
                    app.copyObject(sourceObject, destObject);
                    app.deleteObject(sourceObject);
                }
            }
        }
    }
    private void copyUploadFile(Intent data) {
        String uri  = data.toURI();
        String type = data.getType();
        Uri uril = Uri.parse(uri);
        Cursor cursor = null;
        String filePath;
        try{
            String[] proj = {MediaStore.Video.Media.DATA};
            cursor = this.getContentResolver().query(uril,proj,null,null,null);
            int column_indeex =cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            filePath = cursor.getString(column_indeex);
        }
        finally{
            if(cursor!=null){
                cursor.close();
            }
        }
        String filename = filePath.substring(filePath.lastIndexOf('/')+1);
        app.copyFiles(filePath, mainDirectory + parentFolder + filename);
    }
    public void setList(final String parentFolder){
        pathTextView.setText(MainActivity.this.parentFolder);
        this.app.setFilesInDevice(new ArrayList<File>());
        this.app.checkDevice(new File(mainDirectory+username+File.separatorChar));
        filesInDevice = this.app.filesInDevice;
        int imageLength = 0,folderLength = 0, videosLength = 0, audiosLength = 0;
        filesInList = new ArrayList<>();
        String leaf;
        ArrayList<ArrayList<File>> allInnerFiles = new ArrayList<>();
        for(File file : filesInDevice){
            leaf = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('/')+1);
            if(file.getAbsolutePath().equals(mainDirectory+parentFolder+leaf)) {
                if (file.getAbsolutePath().lastIndexOf('.') > 0) {
                    String extension = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('.') + 1);

                    if (KEYS.getFileType(extension).equals(KEYS.IMAGE_TYPE)) {
                        filesInList.add(folderLength + imageLength++, file);
                    } else if (KEYS.getFileType(extension).equals(KEYS.AUDIO_TYPE)) {
                        filesInList.add(folderLength + imageLength + audiosLength++, file);
                    } else if (KEYS.getFileType(extension).equals(KEYS.VIDEO_TYPE)) {
                        filesInList.add(folderLength + imageLength + audiosLength + videosLength++, file);
                    } else {
                        filesInList.add(file);
                    }
                } else {

                    filesInList.add(folderLength++, file);
                    ArrayList<File> innerFiles = getInnerFiles(file.getAbsolutePath());
                    allInnerFiles.add(innerFiles);
                }
            }

        }
        RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.list_empty);
        if(filesInList.size()!=0) {
            relativeLayout.setVisibility(View.INVISIBLE);
        }
        else
        if(filesInList.size()==0){
            ImageView emptySymbol = (ImageView)findViewById(R.id.empty_icon);
            TextView emptyText = (TextView)findViewById(R.id.empty_text);
            if(isNetwrokAvailable()){
                emptySymbol.setImageResource(R.drawable.ic_error_outline_black_48dp);
                emptyText.setText("No Files To Show ");
            }
            else{
                emptySymbol.setImageResource(R.drawable.ic_cloud_off_black_48dp);
                emptyText.setText("Can't Get To The Servers");
            }
            relativeLayout.setVisibility(View.VISIBLE);
        }
        String viewStyle = sharedpreferences.getString(KEYS.APP_VIEW_STYLE,"");
        if(viewStyle.equals("") || viewStyle.equals("List")){
            contentsAdapter = new ListContentsAdapter(filesInList, this);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String key = filesInList.get(position).getAbsolutePath();


                    if(mActionMode!=null){
                        ImageView itemCheckedLayer = (ImageView) view.findViewById(R.id.translucentChecked);
                        ImageView checkedTick = (ImageView) view.findViewById(R.id.item_checked_tick);

                        if(checkedTick.getVisibility()==View.INVISIBLE) {
                            itemCheckedLayer.setVisibility(View.VISIBLE);
                            checkedTick.setVisibility(View.VISIBLE);
                        }
                        else{
                            itemCheckedLayer.setVisibility(View.INVISIBLE);
                            checkedTick.setVisibility(View.INVISIBLE);
                        }

                        if(!(mListView.getCheckedItemCount()>0)){
                            mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                            //multipleSelection = false;
                        }
                        onListItemSelect(position);
                    }
                    else
                    {
                        if (key.lastIndexOf('.') > 0) {
                            FileOperations fo = new FileOperations(MainActivity.this);
                            fo.open(key);
                        } else {
                            key = key.substring(key.lastIndexOf('/') + 1);
                            MainActivity.this.parentFolder += key;
                            if (MainActivity.this.parentFolder.charAt(MainActivity.this.parentFolder.length() - 1) != File.separatorChar) {
                                MainActivity.this.parentFolder += File.separatorChar;
                            }
                            MainActivity.this.setList(MainActivity.this.parentFolder);
                        }
                    }
                }
            });
            mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    String s = filesInList.get(position).getAbsolutePath();
                    //Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
                    View.DragShadowBuilder shadow = new View.DragShadowBuilder(view);
                    ClipData.Item item = new ClipData.Item(s);
                    String mimeTypes[] = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                    ClipData data = new ClipData(s, mimeTypes, item);
                    //view.startDrag(data,shadow,null,0);
                    mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                    mListView.setItemChecked(position, true);
                    ImageView itemCheckedLayer = (ImageView) view.findViewById(R.id.translucentChecked);
                    ImageView checkedTick = (ImageView) view.findViewById(R.id.item_checked_tick);
                    itemCheckedLayer.setVisibility(View.VISIBLE);
                    checkedTick.setVisibility(View.VISIBLE);
                    onListItemSelect(position);
                    return true;
                }
            });
            mListView.setAdapter((ListContentsAdapter)contentsAdapter);
            //registerForContextMenu(mListView);
        }
        else
        if(viewStyle.equals("Grid")){
            swipe.setVisibility(View.INVISIBLE);
            contentsAdapter = new GridContentsAdapter(filesInList,this,allInnerFiles);
            mGridView.setAdapter((GridContentsAdapter)contentsAdapter);
            mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String key = filesInList.get(position).getAbsolutePath();
                    if(mActionMode!=null){
                        ImageView itemCheckedLayer = (ImageView) view.findViewById(R.id.translucent_grid_checked);
                        ImageView checkedTick = (ImageView) view.findViewById(R.id.tick_grid);

                        if(checkedTick.getVisibility()==View.INVISIBLE) {
                            itemCheckedLayer.setVisibility(View.VISIBLE);
                            checkedTick.setVisibility(View.VISIBLE);
                        }
                        else{
                            itemCheckedLayer.setVisibility(View.INVISIBLE);
                            checkedTick.setVisibility(View.INVISIBLE);
                        }

                        if(!(mListView.getCheckedItemCount()>0)){
                            mListView.setChoiceMode(ListView.CHOICE_MODE_NONE);
                            //multipleSelection = false;
                        }
                        onListItemSelect(position);
                    }
                    else {
                        if (key.lastIndexOf('.') > 0) {
                            FileOperations fo = new FileOperations(MainActivity.this);
                            fo.open(key);
                        } else {
                            key = key.substring(key.lastIndexOf('/') + 1);
                            MainActivity.this.parentFolder += key;
                            if (MainActivity.this.parentFolder.charAt(MainActivity.this.parentFolder.length() - 1) != File.separatorChar) {
                                MainActivity.this.parentFolder += File.separatorChar;
                            }
                            MainActivity.this.setList(MainActivity.this.parentFolder);
                        }
                    }

                }
            });

            mGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(MainActivity.this,"hey ",Toast.LENGTH_LONG).show();
                    ImageView itemCheckedLayer = (ImageView) view.findViewById(R.id.translucent_grid_checked);
                    ImageView checkedTick = (ImageView) view.findViewById(R.id.tick_grid);
                    itemCheckedLayer.setVisibility(View.VISIBLE);
                    checkedTick.setVisibility(View.VISIBLE);
                    onListItemSelect(position);
                    return true;
                }
            });
            //registerForContextMenu(mGridView);
        }
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {

            @Override
            public void run() {
                ArrayList<File> checking = filesInDevice;
                app.setFilesInDevice(new ArrayList<File>());
                app.checkDevice(new File(mainDirectory + username + File.separatorChar));
                if (!checking.equals(app.getFilesInDevice())) {
                    setList(parentFolder);
                    int version = sharedpreferences.getInt(KEYS.DATABASE_VERSION_KEY, 1);
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putInt(KEYS.DATABASE_VERSION_KEY, ++version);
                    editor.apply();
                }
                handler.postDelayed(this, 3 * 1000);
            }
        }, 3 * 1000);
    }

    private ArrayList<File> getInnerFiles(String absolutePath) {
        String leaf="";
        ArrayList<File> innerFiles= new ArrayList<>();
        innerFiles.add(new File(absolutePath));
        for(File file:filesInDevice){
            leaf = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf('/')+1);
            if(file.getAbsolutePath().equals(absolutePath+"/"+leaf)) {
                innerFiles.add(file);
            }
        }
        return innerFiles;
    }

    private void onListItemSelect(int position) {
        (contentsAdapter).toggleSelection(position);
        boolean hasCheckedItems = (contentsAdapter).getSelectedCount() > 0;
        if (hasCheckedItems && mActionMode == null)
            // there are some selected items, start the actionMode
            mActionMode = startActionMode(new ActionModeCallback());
        else if (!hasCheckedItems && mActionMode != null)
            // there no selected items, finish the actionMode
            mActionMode.finish();

        if (mActionMode != null)
            mActionMode.setTitle(String.valueOf((contentsAdapter)
                    .getSelectedCount()) + " selected");
    }


    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // inflate contextual menu
            mode.getMenuInflater().inflate(R.menu.contetual_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            SparseBooleanArray selected = (contentsAdapter).getSelectedIds();
            filesToTransfer = new ArrayList<>();
            for (int i = (selected.size() - 1); i >= 0; i--) {
                if (selected.valueAt(i)) {
                    //Log.d("selected ",filesInList.get(selected.keyAt(i)).getPath());
                    filesToTransfer.add(filesInList.get(selected.keyAt(i)).getPath());
                }
            }
            switch (item.getItemId()) {
                case R.id.action_move_file:
                    // retrieve selected items and delete them out
                    Intent intent = new Intent(MainActivity.this, Explorer.class);
                    intent.putExtra(KEYS.TRANSFER_ACTION_KEY,KEYS.ACTION_MOVE);
                    startActivityForResult(intent, KEYS.MOVE_REQUEST_CODE);
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.action_copy_file:
                    //Intent intent
                    intent= new Intent(MainActivity.this, Explorer.class);
                    intent.putExtra(KEYS.TRANSFER_ACTION_KEY,KEYS.ACTION_COPY);
                    startActivityForResult(intent, KEYS.COPY_REQUEST_CODE);
                    mode.finish();
                    return true;
                case R.id.action_delete_file:
                    for(String toDelete:filesToTransfer){
                        new File(toDelete).delete();
                        //Log.d("deleting",toDelete.substring(mainDirectory.length()));
                        app.deleteObject(toDelete.substring(mainDirectory.length()));
                        mode.finish();
                    }
                    return true;
                default:
                    return false;
            }

        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            // remove selection
            //((ListContentsAdapter) contentsAdapter).removeSelection();
            SparseBooleanArray selected = (contentsAdapter).getSelectedIds();
            for (int i = (selected.size() - 1); i >= 0; i--) {
                if (selected.valueAt(i)) {
                    (contentsAdapter).toggleSelection(selected.keyAt(i));
                }
            }
            contentsAdapter.notifyDataSetChanged();
            //Toast.makeText(MainActivity.this,"Destroy, co : "+mListView.getCheckedItemCount()+" , se : "+( contentsAdapter).getSelectedCount(),Toast.LENGTH_LONG).show();
            mActionMode = null;
        }
    }
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setBackgroundColor(Color.WHITE);
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(MainActivity.this,SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setTextColor(Color.WHITE);
        ((EditText)searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text)).setHintTextColor(getResources().getColor(R.color.material_drawer_dark_hint_text));

        searchView.setBackgroundColor(getResources().getColor(R.color.secondary));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }
    @Override
    public void startActivity(Intent intent) {
        // check if search intent
        try {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                intent.putExtra("ParentFolder", MainActivity.this.parentFolder);
            }
            super.startActivity(intent);
        }
        catch(ActivityNotFoundException e){
            Toast.makeText(MainActivity.this,"No App found to open this file",Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onBackPressed() {
        if(fam.isOpened()){
            fam.close(true);
        }
        else if(!parentFolder.equals(username+File.separatorChar)){
            if(parentFolder.charAt(parentFolder.length()-1) == (File.separatorChar)){
                parentFolder = parentFolder.substring(0,parentFolder.length()-1);
            }
            parentFolder = parentFolder.substring(0,parentFolder.lastIndexOf(File.separatorChar)+1);
            setList(parentFolder);
            pathTextView.setText(parentFolder);

        }
        else
            this.finish();
    }
    private boolean isNetwrokAvailable() {
        ConnectivityManager manager=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=manager.getActiveNetworkInfo();
        boolean isAvailavle = false;
        if(networkInfo != null && networkInfo.isConnected()){
            isAvailavle=true;
        }
        return isAvailavle;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if(id == R.id.search){
            onSearchRequested();
        }
        if(id==R.id.action_change_view_style){
            SharedPreferences.Editor editor = sharedpreferences.edit();
            String viewStyle = sharedpreferences.getString(KEYS.APP_VIEW_STYLE,"");
            if(viewStyle.equals("") || viewStyle.equals("List")) {
                item.setIcon(R.drawable.ic_view_list_white_36dp);
                mListView.setVisibility(View.INVISIBLE);
                swipe.setVisibility(View.INVISIBLE);
                mGridView.setVisibility(View.VISIBLE);
                editor.putString(KEYS.APP_VIEW_STYLE, "Grid");
                editor.apply();
            }
            else
            {
                item.setIcon(R.drawable.ic_view_module_white_36dp);
                swipe.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.INVISIBLE);
                editor.putString(KEYS.APP_VIEW_STYLE, "List");
                editor.apply();
            }
            setList(parentFolder);
        }
        return super.onOptionsItemSelected(item);
    }
}