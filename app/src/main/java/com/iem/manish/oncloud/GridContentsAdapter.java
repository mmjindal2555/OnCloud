package com.iem.manish.oncloud;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manish on 3/12/2016.
 */
public class GridContentsAdapter extends BaseAdapter implements ContentsAdapter{

    List<File> mObjects;
    private Context mContext;
    SparseBooleanArray mSelectedItemsIds;
    ArrayList<ArrayList<File>> allInnerFiles;
    GridContentsAdapter(List<File> objects,Context context,ArrayList<ArrayList<File>> innerFiles){
        //super(context,R.layout.contents_layout,objects);
        this.mObjects = objects;
        mContext = context;
        mSelectedItemsIds = new SparseBooleanArray();
        allInnerFiles = innerFiles;
    }
    @Override
    public int getCount() {
        return this.mObjects.size();
    }
    @Override
    public File getItem(int position) {
        return this.mObjects.get(position);
    }
    @Override
    public long getItemId(int position) {
        return 0;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if(convertView==null)
        {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contents_grid_layout, null);
            holder = new ViewHolder();
            holder.fileIcon = (ImageView)convertView.findViewById(R.id.content_image);
            holder.gridName = (TextView)convertView.findViewById(R.id.grid_title);
            holder.tick = (ImageView)convertView.findViewById(R.id.tick_grid);
            holder.layer = (ImageView)convertView.findViewById(R.id.translucent_grid_checked);
            holder.foldersGrid = (GridView)convertView.findViewById(R.id.folder_grid_contents);
            convertView.setTag(holder);
            //convertView.setLongClickable(true);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }
        File object = mObjects.get(position);
        boolean folder = false;
        if(object.getAbsolutePath().indexOf('.')>0) {
            String FatherDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/OnCloud/";
            String path = object.getAbsolutePath().substring(FatherDirectory.length());
            String fileExtension = path.substring(path.indexOf('.') + 1);
            if(KEYS.getFileType(fileExtension).equals("")){
                //holder.fileIcon.setImageResource(R.mipmap.folder);
                return null;
            }
            else if(KEYS.getFileType(fileExtension).equals(KEYS.IMAGE_TYPE)){
                holder.fileIcon.setImageResource(R.drawable.ic_picture_50dp);
                if(fileExtension.equals("jpg")){
                    holder.fileIcon.setImageResource(R.drawable.ic_picture_50dp);

                }
                Picasso.with(mContext).load("https://s3.amazonaws.com/test.excellencetech.com/" + Uri.encode(path))
                        .centerCrop()
                        .placeholder(R.drawable.ic_picture_50dp)
                        .fit()
                        .into(holder.fileIcon);
            }
            else if(KEYS.getFileType(fileExtension).equals(KEYS.VIDEO_TYPE)){
                holder.fileIcon.setImageResource(R.drawable.ic_video_file_50dp);
            }
            else if(KEYS.getFileType(fileExtension).equals(KEYS.AUDIO_TYPE)){
                holder.fileIcon.setImageResource(R.drawable.ic_audio_file_50dp);

            }
            else if(KEYS.getFileType(fileExtension).equals(KEYS.DOC_TYPE)){
                holder.fileIcon.setImageResource(R.drawable.ic_document_50dp);
                if(fileExtension.equals("pdf")){
                    holder.fileIcon.setImageResource(R.drawable.ic_pdf_50dp);
                }
            }
            holder.fileIcon.setVisibility(View.VISIBLE);
            String file = path.substring(path.lastIndexOf('/') + 1, path.indexOf('.'));
            holder.gridName.setText(file);
            holder.foldersGrid.setVisibility(View.INVISIBLE);
        }
        else{
            holder.fileIcon.setImageResource(R.drawable.ic_folder_50dp);
            //holder.lastModified.setVisibility(View.GONE);
            //holder.fileName.setPadding(0,0,0,20);
            InnerContentsAdapter innerContentsAdapter = null;
            ArrayList<File> dummy = new ArrayList<>();
            for(ArrayList<File> arrayList:allInnerFiles){
                if(arrayList.size()>=0)
                {
                        if (object.getAbsolutePath().equals(arrayList.get(0).getAbsolutePath())) {
                            dummy = new ArrayList<>(arrayList);
                            dummy.remove(0);
                        }
                }
            }
            innerContentsAdapter = new InnerContentsAdapter(dummy, mContext);
            if(dummy.size()==0){
                holder.foldersGrid.setVisibility(View.INVISIBLE);
                holder.fileIcon.setImageResource(R.drawable.ic_folder_50dp);
            }
            else {
                holder.foldersGrid.setAdapter(innerContentsAdapter);
                holder.foldersGrid.setVisibility(View.VISIBLE);
                holder.foldersGrid.setFocusable(false);
                holder.foldersGrid.setClickable(false);
                holder.fileIcon.setImageBitmap(null);
            }
            holder.gridName.setText(object.getAbsolutePath().substring(object.getAbsolutePath().lastIndexOf('/') + 1));
        }
        holder.foldersGrid.setEnabled(false);
        if (mSelectedItemsIds.get(position,false)) {
            holder.tick.setVisibility(View.VISIBLE);
            holder.layer.setVisibility(View.VISIBLE);
        } else {
            holder.tick.setVisibility(View.INVISIBLE);
            holder.layer.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();

        notifyDataSetChanged();
    }
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        //notifyDataSetChanged();
    }
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
    public static class ViewHolder{
        ImageView fileIcon;
        TextView gridName;
        ImageView tick;
        ImageView layer;
        GridView foldersGrid;
    }
}
