package com.schemecreator.denisuser.schemecreator;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Created by Denisuser on 02.11.2014.
 */
public class FileManager implements Parcelable{
    static ArrayList<String> checkedList=new ArrayList<String>();

    public File root;
    public File current;
    public File parent;
    public File file;
    public Rule rule;

    //Обычно файл Saved.idl
    public File fileForSave;

    FileManager(String root,String current,Rule rule){
        this.root=new File(root);
        this.current=new File(current);
        if(this.current==this.root)
            this.parent=null;
        else
            this.parent=this.current.getParentFile();

        this.rule=new Rule(rule);
    }

    FileManager(Parcel parcel){
        this.root=new File(parcel.readString());
        this.current=new File(parcel.readString());
        if(this.current==this.root)
            this.parent=null;
        else
            this.parent=this.current.getParentFile();

        this.rule=new Rule((Rule)parcel.readValue(Rule.class.getClassLoader()));

        String path=parcel.readString();
        if(path!=null) {
            this.setFileForSave(new File(path));
        }
    }

    static class Rule implements Parcelable{
        boolean read;
        boolean write;
        boolean select;
        boolean save;
        boolean delete;

        Rule(Parcel parcel){
            read=(boolean)parcel.readValue(null);
            write=(boolean)parcel.readValue(null);
            select=(boolean)parcel.readValue(null);
            save=(boolean)parcel.readValue(null);
            delete=(boolean)parcel.readValue(null);
        }

        Rule(boolean read,boolean write){
            this.read=read;
            this.write=write;
        }

        Rule(boolean read,boolean write,boolean select,boolean save,boolean delete){
            this.read=read;
            this.write=write;
            this.select=select;
            this.save=save;
            this.delete=delete;
        }

        Rule(Rule rule){
            this.read=rule.read;
            this.write=rule.write;
            this.select=rule.select;
            this.save=rule.save;
            this.delete=rule.delete;
        }

        public void setRead(boolean value){
            read=value;
        }

        public boolean getRead(){
            return read;
        }

        public void setWrite(boolean value){
            write=value;
        }

        public boolean getWrite(){
            return write;
        }

        public void setSelect(boolean value){
            select=value;
        }

        public boolean getSelect(){
            return select;
        }

        public void setSave(boolean value){
            save=value;
        }

        public boolean getSave(){
            return save;
        }

        public void setDelete(boolean value){
            delete=value;
        }

        public boolean getDelete(){
            return delete;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeValue(getRead());
            parcel.writeValue(getWrite());
            parcel.writeValue(getSelect());
            parcel.writeValue(getSave());
            parcel.writeValue(getDelete());
        }

        public static final Creator<Rule> CREATOR=new Creator<Rule>() {
            @Override
            public Rule createFromParcel(Parcel parcel) {
                return new Rule(parcel);
            }

            @Override
            public Rule[] newArray(int i) {
                return new Rule[0];
            }
        };
    }

    public void setFileForSave(File file){
        fileForSave=file;
    }

    public File getFileForSave(){
        return fileForSave;
    }

    public void removeFileForSave(){
        setFileForSave(null);
    }

    public String[] listFiles(){
       File[] fileList=current.listFiles();
       String[] nameList = new String[fileList.length];
       for (int i = 0; i < fileList.length; ++i){
           nameList[i] = fileList[i].getName();
       }
       return nameList;
    }

    public boolean saveFile(File file){
        try {
            if(fileForSave==null || file==null){
                return false;
            }

            //проверяем, что если файл не существует то создаем его
            if(!file.exists()){
                file.createNewFile();

                if(!writeFile(file,readFile(fileForSave))){
                  return false;
                }
                cancelSave();
                return true;
            }

        } catch(IOException e) {
            Log.w("Error","Save error!!!");
        }
        return false;
    }

    public void cancelSave(){
        removeFileForSave();
        rule.setSave(false);
    }

    public boolean deleteFiles(){
        boolean deleteAll=true;
        for(String str : checkedList){
            String path=current.getAbsolutePath()+"/"+str;
            if(!path.equals(ApplicationSettings.getBaseDir().getAbsolutePath()+"/Saved.idl")) {
                deleteAll=deleteAll && deleteFile(new File(path));
            }
        }
        return deleteAll;
    }

    public boolean deleteFile(File file){
        try {
            if(file==null){
                return false;
            }

            if(file.exists()){
                file.delete();
                return true;
            }

        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean writeFile(File file,String content){
        if(!rule.getWrite()){
            return false;
        }

        if(file==null || content==null){
            return false;
        }

        if ((-1)!=file.getAbsolutePath().indexOf(Environment.MEDIA_MOUNTED.toString())){
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return false;
            }
        }

        try {
            FileOutputStream fs=new FileOutputStream(file);
            byte[] b=content.getBytes("Cp1251");
            fs.write(b);
            fs.close();

            Log.w("Write file","true");
            return true;
        }catch(FileNotFoundException e){
            e.getStackTrace();
            Log.w("File error","File not found");
            return false;
        }catch(IOException e){
            e.getStackTrace();
            if(!file.isFile()) {
                Log.w("File error", "This is not file");
                return false;
            }
            Log.w("File error","Can't write file");
            return false;
        }
    }


    public String readFile(File file){
        if(file==null){
            return null;
        }

        if(!rule.getRead()){
            return null;
        }

        if ((-1)!=file.getAbsolutePath().indexOf(Environment.MEDIA_MOUNTED.toString())){
            if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return null;
            }
        }

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "windows-1251");
            BufferedReader br = new BufferedReader(isr);
            String str,result="";
            while((str=br.readLine())!=null){
                result+=str;
            }
            br.close();
            isr.close();

            Charset cset = Charset.forName("UTF-8");
            ByteBuffer buf = cset.encode(result);
            byte[] b = buf.array();
            result = new String(b);

            Log.w("Read file","true");
            return result;
        }catch(FileNotFoundException e){
            e.getStackTrace();
            Log.w("File error","File not found");
            return null;
        }catch(IOException e){
            e.getStackTrace();
            if(!file.isFile()) {
                Log.w("File error", "This is not file");
                return null;
            }
            Log.w("File error","Can't read file");
            return null;
        }
    }


   public int goNext(String childName){
       File child=new File(current.getAbsolutePath()+"/"+childName);
       Log.w("App", child.toString());
       if(child.isFile()){
           Log.w("App", "File");
           //Document in XML format
           if(openDocument(child))
            return 1;
           else if(openIDL(child))
            return 3;
           else
            return 0;
       }else if(child.isDirectory()){
           Log.w("App", "Directory");
           parent=current;
           current=child;
           Log.w("Current", current.getAbsolutePath());
           Log.w("Parent", parent.getAbsolutePath());
           return 2;
       }else{
           return 0;
       }
   }


    public boolean goBack(){
        String parentString;
        Log.w("Current", current.getAbsolutePath());
        Log.w("Root", root.getAbsolutePath());
        String currentPath=current.getAbsolutePath();
        String rootPath=root.getAbsolutePath();
        if(!currentPath.equals(rootPath)) {
            current = parent;
            if(parent.getParentFile()==null) {
                parent = null;
                parentString = "NULL";
            }else {
                parent = parent.getParentFile();
                parentString = parent.getAbsolutePath();
            }
            Log.w("Current", current.getAbsolutePath());
            Log.w("Parent", parentString);
            return true;
        }else{
            return false;
        }
    }


    public void openFile(){

    }

    public static String getExtension(File file){
        //extension with dot
        int ind=file.getName().lastIndexOf(".");
        return file.getName().substring(ind);
    }

    public static String getFileName(File file){
        String name=file.getName();
        String[] strArr=name.split("\\.");
        String newName="";
        for(int i=0;i<strArr.length-1;i++){
            String str=strArr[i];
            newName+=str+".";
        }
        newName=newName.substring(0, newName.length()-1);
        return newName;
    }

    public boolean openDocument(File file){
        String ext=getExtension(file);
        Log.w("File ext",ext);
        if(ext.equals(".xml")){
           this.file=file;
           Log.w("XML","true");
           return true;
        }else{
            Log.w("XML","false");
           return false;
        }
    }

    public boolean openIDL(File file){
        String ext=getExtension(file);
        Log.w("File ext",ext);
        if(ext.equals(".idl")){
            this.file=file;
            Log.w("IDL","true");
            return true;
        }else{
            Log.w("IDL","false");
            return false;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(root.toString());
        parcel.writeString(current.toString());
        parcel.writeValue(rule);
        if(fileForSave!=null) {
            parcel.writeString(fileForSave.toString());
        }
    }

    public static final Creator<FileManager> CREATOR=new Creator<FileManager>() {
        @Override
        public FileManager createFromParcel(Parcel parcel) {
            return new FileManager(parcel);
        }

        @Override
        public FileManager[] newArray(int i) {
            return new FileManager[0];
        }
    };
}
