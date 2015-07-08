package com.schemecreator.denisuser.schemecreator;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

/**
 * Created by Denisuser on 03.11.2014.
 */
class FileInfo{
    Drawable image;
    String fileName;
    int fileType;

    Context context;

    FileInfo(Context context,int fileType,String fileName){
        this.context=context;
        this.fileName=fileName;
        Resources img=context.getResources();
        this.fileType=fileType;
        switch(fileType){
            //directory
            case 0:
                image=img.getDrawable(R.drawable.ic_filelist_directory);
                break;
            //file
            case 1:
                image=img.getDrawable(R.drawable.ic_filelist_file);
                break;
            //app file
            case 2:
                image=img.getDrawable(R.drawable.ic_filelist_file);
                break;
        }
    }

};
