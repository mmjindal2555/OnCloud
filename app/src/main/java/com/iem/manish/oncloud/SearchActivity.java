package com.iem.manish.oncloud;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.iem.manish.oncloud.Database.DatabaseTable;

import java.io.File;
import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    DatabaseTable db;
    ListView mListView;
    ArrayList<File> searchResults;
    Toolbar toolbar;
    SearchView searchView;
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        db = new DatabaseTable(this);
        mListView = (ListView)findViewById(R.id.searchListView);

        toolbar = (Toolbar)findViewById(R.id.search_toolbar);
        toolbar.setOverflowIcon(getDrawable(R.drawable.ic_more_vert_white_18dp));
        toolbar.setTitleTextColor(Color.BLACK);

        setSupportActionBar(toolbar);

        searchResults = new ArrayList<>();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String key = searchResults.get(position).getAbsolutePath();
                Intent intent = new Intent(SearchActivity.this,MainActivity.class);
                if (key.lastIndexOf('.') > 0) {
                    FileOperations fo = new FileOperations(SearchActivity.this);
                    fo.open(key);
                } else {
                    intent.putExtra("parentFolder", key);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }

            }
        });

        handleIntent(getIntent());
    }
    @Override
    protected void onNewIntent(Intent intent) {
        searchResults = new ArrayList<>();
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String parent = intent.getStringExtra("ParentFolder");

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {

            String query = intent.getStringExtra(SearchManager.QUERY);
            //SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,MySuggestionsProvider.AUTHORITY, MySuggestionsProvider.MODE);
            //suggestions.saveRecentQuery(query, null);

            this.toolbar.setTitle(query);
            this.toolbar.setSubtitle(query);
            this.toolbar.setTitleTextColor(Color.WHITE);
            this.toolbar.setSubtitleTextColor(Color.WHITE);
            //use the query to search your data somehow
            Cursor c = db.getFileMatches(query, parent ,null);
            if(c!=null){

                c.moveToFirst();
                int locationIndex = c.getColumnIndex("FILE_LOCATION");
                for(int i=0;i<c.getCount();i++){
                    String s = c.getString(locationIndex)+"";
                    if(s.contains(parent))
                    searchResults.add(new File(s));
                    c.moveToNext();
                }

                if(searchResults.size()>0){
                    ListContentsAdapter ca = new ListContentsAdapter(searchResults,this);
                    mListView.setAdapter(ca);
                }
                else{
                    Toast.makeText(this,"No such file or folder present", Toast.LENGTH_LONG).show();
                    this.finish();
                }

            }
            else{
                Toast.makeText(this,"No such file or folder present", Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        /*getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setBackgroundColor(Color.WHITE);
        //searchView.setIconified(false);
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                SearchActivity.this.finish();
                return true;

            }
        });
        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(this,SearchActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
        */
        return true;

    }

}
