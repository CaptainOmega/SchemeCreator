package com.schemecreator.denisuser.schemecreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


public class SettingsActivity extends ActionBarActivity {
    AlertDialog.Builder dialogBuilder;
    public static final int ABOUT_APP=0;
    public static final int RESET_SETTINGS=1;
    public static final int SDCARD_NOT_FOUND=2;
    public static final int SDCARD_NOT_AVAILABLE=3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        setContentView(R.layout.activity_settings);
        showSettingsField();

        showDirLocation();
    }

    public void showDirLocation(){
        TextView tv=(TextView)findViewById(R.id.location_info);
        tv.setText(tv.getText()+" "+ApplicationSettings.getBaseDir().getAbsolutePath());
    }

    public void showSettingsField(){

        EditText author=(EditText)findViewById(R.id.app_settings_author);
        author.setText(ApplicationSettings.author);

        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,R.layout.location_item,R.id.location_item,ApplicationSettings.locationArr);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner=(Spinner)findViewById(R.id.app_settings_location);
        spinner.setAdapter(adapter);
        spinner.setPrompt("Location");
        if(ApplicationSettings.location==ApplicationSettings.PHONE) {
            spinner.setSelection(0);
        }else if(ApplicationSettings.location==ApplicationSettings.CARD){
            spinner.setSelection(1);
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getBaseContext(),ApplicationSettings.locationArr[position],Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
            case ABOUT_APP:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_info));
                dialogBuilder.setMessage(getString(R.string.msg_info_about_app));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });
                return dialogBuilder.create();
            case RESET_SETTINGS:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_reset_settings));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ApplicationSettings.writeDefaultSettings();
                        showSettingsField();
                        return;
                    }
                });
                dialogBuilder.setNegativeButton(getString(R.string.btn_negative),new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                return dialogBuilder.create();
            case SDCARD_NOT_FOUND:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_warning));
                dialogBuilder.setMessage(getString(R.string.msg_warning_no_sdcard));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });
                return dialogBuilder.create();
            case SDCARD_NOT_AVAILABLE:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_warning));
                dialogBuilder.setMessage(getString(R.string.msg_warning_sdcard_not_available));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive),new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        return;
                    }
                });
                return dialogBuilder.create();
            default:
                return null;
        }
    }

    public void saveSettings(View view){
        EditText author=(EditText)findViewById(R.id.app_settings_author);
        ApplicationSettings.writeAuthor(author.getText().toString());

        Spinner locSpinner=(Spinner)findViewById(R.id.app_settings_location);
        String location=(String)locSpinner.getSelectedItem();
        ApplicationSettings.writeLocation(location);

        if(ApplicationSettings.location) {
            if (!ApplicationSettings.isOnSDCard(ApplicationSettings.getBaseDir())) {
                ApplicationSettings.writeLocation(ApplicationSettings.locationArr[0]);
                showDialog(SDCARD_NOT_FOUND);
                showSettingsField();
                return;
            }
            if (!ApplicationSettings.isSDCardAvailable()) {
                ApplicationSettings.writeLocation(ApplicationSettings.locationArr[0]);
                showDialog(SDCARD_NOT_AVAILABLE);
                showSettingsField();
                return;
            }
        }

        finish();
    }

    public void aboutApp(View view){
        showDialog(ABOUT_APP);
    }

    public void resetSettings(View view){
        showDialog(RESET_SETTINGS);
    }
}
