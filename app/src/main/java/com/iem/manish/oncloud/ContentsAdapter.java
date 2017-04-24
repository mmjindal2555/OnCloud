package com.iem.manish.oncloud;

import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Manish on 3/22/2016.
 */
public interface ContentsAdapter {
    public void notifyDataSetChanged();
    public void toggleSelection(int a);
    public void selectView(int position, boolean value);
    public int getSelectedCount();
    public SparseBooleanArray getSelectedIds();
}
