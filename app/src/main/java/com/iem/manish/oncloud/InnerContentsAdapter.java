package com.iem.manish.oncloud;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.List;

/**
 * Created by Manish on 3/28/2016.
 */
public class InnerContentsAdapter extends BaseAdapter{

    List<File> mObjects;
    private Context mContext;
    SparseBooleanArray mSelectedItemsIds;
    InnerContentsAdapter(List<File> objects,Context context){
        //super(context,R.layout.contents_layout,objects);
        this.mObjects = objects;
        mContext = context;
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.inner_content_layout, null);
            holder = new ViewHolder();
            holder.fileIcon = (ImageView)convertView.findViewById(R.id.inner_content);
            convertView.setTag(holder);
            //convertView.setLongClickable(true);
        }
        else{
            holder=(ViewHolder)convertView.getTag();
        }
        File object = mObjects.get(position);

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


        }
        else{
            holder.fileIcon.setImageResource(R.drawable.ic_folder_50dp);
            //holder.lastModified.setVisibility(View.GONE);
            //holder.fileName.setPadding(0,0,0,20);


            //holder.foldersGrid.se

        }
        convertView.setEnabled(false);
        convertView.setClickable(false);
        return convertView;
    }
    public static class ViewHolder{
        ImageView fileIcon;
    }
}

