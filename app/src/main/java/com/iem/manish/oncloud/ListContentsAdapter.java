package com.iem.manish.oncloud;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.SparseBooleanArray;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;
import java.util.List;
public class ListContentsAdapter extends BaseAdapter implements ContentsAdapter{
    List<File> mObjects;
    private Context mContext;
    private SparseBooleanArray mSelectedItemsIds;
    ListContentsAdapter(List<File> objects, Context context){
        //super(context,R.layout.contents_layout,objects);
        this.mObjects = objects;
        mContext = context;
        mSelectedItemsIds = new SparseBooleanArray();
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
                convertView = LayoutInflater.from(mContext).inflate(R.layout.contents_layout, null);
                holder = new ViewHolder();
                holder.fileIcon = (ImageView)convertView.findViewById(R.id.fileIcon);
                holder.fileName=(TextView)convertView.findViewById(R.id.filename);
                holder.lastModified =(TextView)convertView.findViewById(R.id.lastmodified);
                holder.folderName = (TextView)convertView.findViewById(R.id.folderText);
                holder.tick = (ImageView)convertView.findViewById(R.id.item_checked_tick);
                holder.layer = (ImageView)convertView.findViewById(R.id.translucentChecked);
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
            String fileExtension = path.substring(path.indexOf('.')+1);
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
            String file = path.substring(path.lastIndexOf('/') + 1, path.indexOf('.'));
            holder.fileName.setText(file);
            holder.folderName.setText("");
            long date=object.lastModified();
            Date now = new Date();
            long nowTime = now.getTime();
            String timeSpan = DateUtils.getRelativeTimeSpanString(date, nowTime, DateUtils.SECOND_IN_MILLIS).toString();
            holder.lastModified.setText(timeSpan);
        }
        else{
            holder.fileIcon.setImageResource(R.drawable.ic_folder_50dp);
            holder.folderName.setText(object.getAbsolutePath().substring(object.getAbsolutePath().lastIndexOf('/') + 1));
            holder.fileName.setText("");
            holder.lastModified.setText("");
            //holder.lastModified.setVisibility(View.GONE);
            //holder.fileName.setPadding(0,0,0,20)
        }
        convertView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch(event.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        Toast.makeText(mContext,"drag started",Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DRAG_ENTERED:
                        Toast.makeText(mContext,"drag entered",Toast.LENGTH_LONG).show();
                        break;
                    case DragEvent.ACTION_DRAG_EXITED:
                        Toast.makeText(mContext,"drag exited",Toast.LENGTH_LONG).show();
                        break;
                    default:break;
                }
                return true;
            }
        });

            if (mSelectedItemsIds.get(position,false)) {
                holder.tick.setVisibility(View.VISIBLE);
                holder.layer.setVisibility(View.VISIBLE);
            } else {
                holder.tick.setVisibility(View.INVISIBLE);
                holder.layer.setVisibility(View.INVISIBLE);
            }

        return convertView;
    }
    @Override
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }
    @Override
    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);

        //notifyDataSetChanged();
    }
    @Override
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }
    @Override
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }
    public static class ViewHolder{
        ImageView fileIcon;
        TextView lastModified;
        TextView fileName;
        TextView folderName;
        ImageView tick;
        ImageView layer;
    }
}
