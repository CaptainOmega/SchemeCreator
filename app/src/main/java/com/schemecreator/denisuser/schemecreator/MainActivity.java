package com.schemecreator.denisuser.schemecreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements FileListInterface {

    AlertDialog.Builder dialogBuilder;
    public final static int DIRECTORY_NOT_EXIST=0;
    FileManager fm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String root=ApplicationSettings.getBaseDir().getAbsolutePath();
        String current=ApplicationSettings.getBaseDir().getAbsolutePath();
        fm=new FileManager(root,current,new FileManager.Rule(true,false));

        if(ApplicationSettings.getBaseDir().mkdirs()){
            createExampleFile();
        }else{
            if(!ApplicationSettings.getBaseDir().exists()){
                showDialog(DIRECTORY_NOT_EXIST);
            }
        }

        /*
        if (!ApplicationSettings.getBaseDir().mkdirs()) {
            if(!ApplicationSettings.getBaseDir().exists()){
                showDialog(DIRECTORY_NOT_EXIST);
            }else{
                if(!exampleFileExist()) {
                    createExampleFile();
                }
            }
        }
        */

        checkRecentFiles();
        showRecentFiles();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.w("MainActivity","onRestart");
        setContentView(R.layout.activity_main);

        String root=ApplicationSettings.getBaseDir().getAbsolutePath();
        String current=ApplicationSettings.getBaseDir().getAbsolutePath();
        fm=new FileManager(root,current,new FileManager.Rule(true,false));

        if(ApplicationSettings.getBaseDir().mkdirs()){
            createExampleFile();
        }else{
            if(!ApplicationSettings.getBaseDir().exists()){
                showDialog(DIRECTORY_NOT_EXIST);
            }
        }
        /*
        if (!ApplicationSettings.getBaseDir().mkdirs()) {
            if(!ApplicationSettings.getBaseDir().exists()){
                showDialog(DIRECTORY_NOT_EXIST);
            }else {
                if(!exampleFileExist()) {
                    createExampleFile();
                }
            }
        }
        */
        checkRecentFiles();
        showRecentFiles();
    }

    //Метод будет вызван, после нажатия на файл из списка
    public void getMessage(String message){
        //Log.w("App", message);
        switch (fm.goNext(message)){
            case 0:
                Log.w("0","Not Document");
                break;
            case 1:
                Log.w("1","Document");
                /*
                curActivity.finish();
                sendToDocumentActivity();
                */
                break;
            case 2:
                Log.w("2","Directory");
                break;
            case 3:
                Log.w("3","IDL");
                sendToDocumentActivity();
                break;
        }
    }

    public void sendToDocumentActivity(){
        Intent intent=new Intent(this,DocumentActivity.class);
        if(FileManager.getExtension(fm.file).equals(".xml"))
            intent.putExtra("XML",fm.file.getAbsolutePath());
        else if(FileManager.getExtension(fm.file).equals(".idl"))
            intent.putExtra("IDL",fm.file.getAbsolutePath());
        this.startActivity(intent);
    }

    public ArrayList<FileInfo> getFileListItems(File[] file){
        if(file == null)
            return null;

        ArrayList<FileInfo>fileList=new ArrayList<FileInfo>();
        Log.d("Files", "Size: " + file.length);
        for (int i=0; i < file.length; i++)
        {
            String fileName=file[i].getName();
            if(file[i].isFile()){
                int dotPos = fileName.lastIndexOf(".");
                try {
                    String ext = fileName.substring(dotPos);
                    if(ext==".xml"){
                        fileList.add(new FileInfo(this,2,fileName));
                    }else{
                        fileList.add(new FileInfo(this,1,fileName));
                    }
                }catch(Exception e){
                    Log.w("Exception",fileName);
                }
            }else{
                fileList.add(new FileInfo(this,0,fileName));
            }

        }
        return fileList;
    }

    public FileAdapter getFileAdapter(){
        RecentFiles recentFiles=new RecentFiles(this,ApplicationSettings.getBaseDir());
        ArrayList<File> fileList=recentFiles.getFiles();
        File[] fileArr={};
        if(fileList!=null) {
            fileArr = new File[fileList.size()];
            for (int i = 0; i < fileArr.length; i++) {
                fileArr[i] = fileList.remove(0);
            }
        }
        FileAdapter adapter=new FileAdapter(this,getFileListItems(fileArr),fm.rule,false,true);
        return adapter;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        //final EditText inputText;
        switch(id) {
            case DIRECTORY_NOT_EXIST:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle("Error!!!");
                dialogBuilder.setMessage("IME directory, not exist!");
                dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //System.exit(0);
                        finish();
                    }
                });
                return dialogBuilder.create();
            default:
                break;
        }
        return super.onCreateDialog(id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    //Проверяет существование временных файлов
    public void checkRecentFiles(){
        RecentFiles recentFiles = new RecentFiles(this, ApplicationSettings.getBaseDir());
        recentFiles.checkFiles();
    }

    public void showRecentFiles(){
        ListView lv=(ListView)findViewById(R.id.recent_file);
        lv.setAdapter(getFileAdapter());
    }

    public void newDocument(View view){
        Intent intent=new Intent(this,DocumentActivity.class);
        intent.putExtra("New","true");
        startActivity(intent);
    }

    public void openDocument(View view){
        Intent intent=new Intent(this,FileActivity.class);
        //String root=getApplication().getApplicationContext().getFilesDir().getAbsolutePath();
        //String current=getApplication().getApplicationContext().getFilesDir().getAbsolutePath();

        //File file = new File(Environment.getExternalStorageDirectory(), "IML");

        String root=ApplicationSettings.getBaseDir().getAbsolutePath();
        String current=ApplicationSettings.getBaseDir().getAbsolutePath();

        FileManager fm=new FileManager(root,current,new FileManager.Rule(true,true,true,false,true));
        intent.putExtra(FileManager.class.getCanonicalName(),fm);
        startActivity(intent);
    }

    public void openSettings(View view){
        Intent intent=new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }


    public void createExampleFile(){
        InputStream is = null;
        OutputStream os = null;
        try {
            try {
                is = getResources().getAssets().open("Example.idl");
                os = new FileOutputStream(ApplicationSettings.getBaseDir() + "/Example.idl");
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
            } finally {
                is.close();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
