package com.iem.manish.oncloud;

import android.os.Environment;

/**
 * Created by Manish on 1/19/2016.
 */
public class KEYS {

    private String bucketName;

    public String getBucketName() {return bucketName;}
    public void setBucketName(String bucketName) {this.bucketName = bucketName;}

    public final static String USERNAME = "username";
    public final static String AUTHORIZATION_ENDPOINT = "http://sky.excellencetech.co.uk/lime_server/api/authorize.php";
    public final static String TOKEN_ENDPOINT = "http://sky.excellencetech.co.uk/lime_server/api/token.php";
    public final static String RESOURCE_URL = "http://sky.excellencetech.co.uk/lime_server/api/resource.php";
    public final static String CIENT_ID ="RMh53N";
    public final static String SECRET_ACCESS_KEY = "KZrSn7";
    public final static String REDIRECT_URL = "myapp://oncloudresponse/";
    public final static String ACCOUNT_TYPE = "limelms.oncloud.com";

    public final static String APP_PREFERENCE_NAME = "OnCloudPreferences";
    public final static String APP_STORAGE_PATH = "StoragePathKey";
    public final static String APP_VIEW_STYLE = "viewStyle";
    public final static String TRANSFER_ACTION_KEY = "transferAction";
    public final static String ACTION_MOVE = "move";
    public final static String TRANSFER_PATH_KEY="transferpathkey";
    public final static String ACTION_COPY = "copy";
    public final static int PICK_FILE_REQUEST_CODE = 1;
    public final static int TAKE_PICTURE_REQUEST_CODE = 2;
    public final static int RENAME_FILE_REQUEST_CODE = 3;
    public final static int CREATE_FOLDER_REQUEST_CODE = 4;
    public final static int MOVE_REQUEST_CODE = 6;
    public final static int COPY_REQUEST_CODE = 7;
    public final static int DELETE_REQUEST_CODE = 8;
    public final static int SEARCH_REQUEST_CODE = 5;
    public final static String DATABASE_VERSION_KEY = "version";


    public final static String IMAGE_TYPE = "image";
    public final static String VIDEO_TYPE = "video";
    public final static String AUDIO_TYPE = "audio";
    public final static String DOC_TYPE = "application";
    public final static String LIST_ITEM_SELECTED = "listItemSeected";
    public final static String imageType[] = {"jpg","jpeg","jpe","gif", "png", "bmp", "tif","tiff", "ico"};
    public final static String videoType[] ={"asf","asx","wmv","wmx","wm","avi","divx","flv","mov","qt","mpeg","mpg","mpe","mp4","m4v","ogv","webm","mkv"};
    public final static String audioType[]={"mp3","m4a","m4b","ra","ram","wav","ogg","oga","mid","midi","wma","wax","mka","aac"};
    public final static String docType[] = {"pdf","cpp","doc","pot","pps","ppt","wri","xla","xls","xlt","xlw","mdb","mpp","docx","docm","dotx","dotm","xlsx","xlsm","xlsb","xltx","xltm","xlam","pptx","pptm","ppsx","ppsm","potx","potm","ppam","sldx","sldm","onetoc","onetoc2","onetmp","onepkg"};
    public static String getFileType(String fileExtension) {
        for(String ext : KEYS.imageType){
            if(ext.equalsIgnoreCase(fileExtension))
                return KEYS.IMAGE_TYPE;
        }
        for (String ext:KEYS.videoType){
            if(ext.equalsIgnoreCase(fileExtension))
                return KEYS.VIDEO_TYPE;
        }
        for(String ext : KEYS.audioType)
        {
            if(ext.equalsIgnoreCase(fileExtension))
                return KEYS.AUDIO_TYPE;
        }
        for (String ext: KEYS.docType){
            if(ext.equalsIgnoreCase(fileExtension))
                return KEYS.DOC_TYPE;
        }
        return "";

    }
}
