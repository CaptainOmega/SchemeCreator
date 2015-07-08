package com.schemecreator.denisuser.schemecreator;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Denisuser on 28.04.2015.
 */
public class RecentFiles{
    Activity curActivity;
    //XML файл с информацией о просмотренных файлах
    File xmlFile;
    //Директория, где будем искать просмотренные файлы
    File baseFile;

    //Если файл recent_files не существует, то конструктор должен его создать
    RecentFiles(Activity curActivity,File baseFile){
        this.curActivity=curActivity;
        //Имя файла должно быть написано без пробела, так как при работе с XML-файлом в дальнейшем
        //на месте пробелов, добавляется символ "%20"
        xmlFile=new File(curActivity.getApplicationContext().getFilesDir(),"recent_files.xml");

        if(!xmlFile.exists()){
            try {
                xmlFile.createNewFile();
            }catch(IOException e){
                e.getStackTrace();
                Log.w("File error", "Recent file can't be created.");
                return;
            }
        }

        readDir();
        this.baseFile=baseFile;
    }

    //Удаляет все файлы в директории приложения
    public void delete(){
        File file=new File(curActivity.getApplicationContext().getFilesDir().getAbsolutePath());
        for(File f : file.listFiles()){
            f.delete();
        }
    }

    //Удаляет все пути в тегах <file>
    public void deletePaths(){
        UseXML useXML=new UseXML(getXMLFile());
        useXML.deletePaths();
    }

    //Удаляет теги <file>, в которых содержимое равно path
    public void deleteFile(String path){
        UseXML useXML=new UseXML(getXMLFile());
        useXML.deleteFile(path);
    }

    //Проверяет теги <file> на существование файла
    public void checkFiles(){
        UseXML useXML=new UseXML(getXMLFile());
        useXML.checkFiles();
    }

    public void readDir(){
        File file=new File(curActivity.getApplicationContext().getFilesDir().getAbsolutePath());
        for(File f : file.listFiles()){
            Log.w("File",f.getName());
        }
    }

    public File getXMLFile(){
        return xmlFile;
    }

    public File getBaseFile(){
        return baseFile;
    }

    public boolean isBaseFileExist(){
        boolean result=true;
        if(baseFile==null){
            result = false;
        }else {
            if (!baseFile.exists()) {
                result = false;
            }
        }
        return result;
    }

    public boolean isXMLFileExist(){
        boolean result=true;
        if(xmlFile==null){
            result = false;
        }else {
            if (!xmlFile.exists()) {
                result = false;
            }
        }
        return result;
    }

    public boolean filesExist(){
        boolean result=true;
        result=result & isXMLFileExist();
        result=result & isBaseFileExist();
        return result;
    }


    public void addFile(File file){
        UseXML useXML=new UseXML(getXMLFile());
        useXML.addFilePath(file.getAbsolutePath());
    }

    public ArrayList<File> getFiles(){
        if(!filesExist()){
            return null;
        }

        UseXML useXML=new UseXML(getXMLFile());
        return useXML.readRecentFiles();
    }

}
