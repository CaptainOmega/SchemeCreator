package com.schemecreator.denisuser.schemecreator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class FileActivity extends Activity implements FileListInterface {
    FileManager fm;
    AlertDialog.Builder dialogBuilder;
    //Context context = getApplicationContext();
    static final int EDIT_FILE_NAME=0;
    static final int ERROR_SAVE_FILE=1;
    static final int ERROR_DELETE_FILE=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);

        fm=getIntent().getParcelableExtra(FileManager.class.getCanonicalName());
        if(fm==null){
            return;
        }
        /*
        if(fm==null){
            fm = new FileManager(getIntent().getStringExtra("root"),getIntent().getStringExtra("current"),true,true);
        }
        */

        Log.w("TextView",fm.current.getAbsolutePath());
        final TextView dirPath=(TextView)findViewById(R.id.dir_path);

        String path=fm.current.getAbsolutePath();
        if(path!=null) {
            dirPath.setText(path);
        }

        ListView lv = (ListView) findViewById(R.id.file_list);
        FileAdapter adapter=new FileAdapter(this,getFileListItems(fm.current.listFiles()),fm.rule);
        lv.setAdapter(adapter);

        showMenuButton();

        if(fm.getFileForSave()!=null){
            showDialog(EDIT_FILE_NAME);
        }
    }

    public void showMenuButton(){
        if(fm.rule.getSave()){
            showSaveButton();
        }
        if(fm.rule.getDelete()){
            showDeleteButton();
        }
    }

    public void showSaveButton(){
        ImageButton btn=(ImageButton)findViewById(R.id.file_save);
        if(btn!=null) {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if(fm.getFileForSave()!=null){
                        showDialog(EDIT_FILE_NAME);
                    }
                }
            });
        }
    }

    public void showDeleteButton(){
        ImageButton btn=(ImageButton)findViewById(R.id.file_delete);
        if(btn!=null) {
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if(!fm.deleteFiles()){
                        showDialog(ERROR_DELETE_FILE);
                    }
                    //Проверяет существование временных файлов
                    checkRecentFiles();
                    reloadActivity();
                }
            });
        }
    }

    //Удаляет пути из файла recent_files.xml
    /*
    public void deleteRecentFiles(){
        for(String path : fm.checkedList) {
            RecentFiles recentFiles = new RecentFiles(getContext(), MainActivity.BASE_FILE);
            recentFiles.deleteFile(path);
        }
    }
    */

    //Проверяет существование временных файлов
    public void checkRecentFiles(){
        RecentFiles recentFiles = new RecentFiles(this, ApplicationSettings.getBaseDir());
        recentFiles.checkFiles();
    }

    //Для получения контекста активити
    Activity getContext(){
        return this;
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        final EditText inputText;
        switch(id) {
            //aaa
            case EDIT_FILE_NAME:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_file_name));
                inputText=new EditText(this);
                inputText.setId(0);
                dialogBuilder.setView(inputText);
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String ext=FileManager.getExtension(fm.fileForSave);
                        String text=inputText.getText().toString().trim();
                        if(text.length()>0){
                            File file=new File(ApplicationSettings.getBaseDir()+"/"+inputText.getText().toString()+ext);
                            if(!fm.saveFile(file)){
                                showDialog(ERROR_SAVE_FILE);
                            }else{
                                //При успешном сохранении файла, добавляем в recent files
                                RecentFiles recentFiles=new RecentFiles(getContext(),ApplicationSettings.getBaseDir());
                                recentFiles.addFile(file);
                                recentFiles.getFiles();
                                DocumentActivity.activeFile=file;
                            }
                            reloadActivity();
                        }
                        return;
                    }
                });

                dialogBuilder.setNegativeButton(getString(R.string.btn_negative), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                return dialogBuilder.create();
            case ERROR_SAVE_FILE:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_error));
                dialogBuilder.setMessage(getString(R.string.msg_error_save_file));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });
                return dialogBuilder.create();
            case ERROR_DELETE_FILE:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_error));
                dialogBuilder.setMessage(getString(R.string.msg_error_delete_file));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.open_doc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar file_item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if(fm.goBack()) {
            finish();
            Log.w("Activity","finish");
            sendToThisActivity();
        }
        //fm.goBack();
    }

    public void backToParent(View view){
        finish();
    }

    public ArrayList<FileInfo> getFileListItems(File[] file){
        if(file == null)
            return null;
        //String[] nameList=new String[file.length];
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

            //Log.d("Files", "FileName:" + file[i].getName());
           // nameList[i]=file[i].getName();
        }
        return fileList;
    }

    public void getMessage(String message){
        //Log.w("App", message);
        switch (fm.goNext(message)){
            case 0:
                Log.w("0","Not Document");
            break;
            case 1:
                Log.w("1","Document");
                finish();
                sendToDocumentActivity();
            break;
            case 2:
                Log.w("2","Directory");
                finish();
                sendToThisActivity();
            break;
            case 3:
                Log.w("3","IDL");
                finish();
                sendToDocumentActivity();
            break;
        }
    }

    public void reloadActivity(){
        finish();
        sendToThisActivity();
    }

    public void sendToThisActivity(){
        Intent intent=new Intent(this,FileActivity.class);
        intent.putExtra(FileManager.class.getCanonicalName(),fm);
        startActivity(intent);
    }

    public void sendToDocumentActivity(){
        Intent intent=new Intent(this,DocumentActivity.class);
        if(FileManager.getExtension(fm.file).equals(".xml"))
            intent.putExtra("XML",fm.file.getAbsolutePath());
        else if(FileManager.getExtension(fm.file).equals(".idl"))
            intent.putExtra("IDL",fm.file.getAbsolutePath());
        startActivity(intent);
    }

}
