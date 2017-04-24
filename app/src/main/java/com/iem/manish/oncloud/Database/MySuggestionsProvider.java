package com.iem.manish.oncloud.Database;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by Manish on 2/24/2016.
 */
public class MySuggestionsProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "com.oncloud.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionsProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

}
