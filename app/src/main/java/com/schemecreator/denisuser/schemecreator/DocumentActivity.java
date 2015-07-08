package com.schemecreator.denisuser.schemecreator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.HashMap;

//ActionBarActivity
public class DocumentActivity extends FragmentActivity implements FragmentMainMenu.FragmentMenuListener {
    IDLParser idlParser;
    IDEFEntity entity;
    DocumentDraw draw;
    AlertDialog.Builder dialogBuilder;
    FileManager fm;
    //Timer actionTimer;
    String newText;
    public static File activeFile;
    HashMap<String,String> documentOptions;
    View currentView;

    //Константы для диалога
    private final int EDIT_TEXT_BOX=1;
    private final int EDIT_TEXT_LABEL=2;
    private final int CREATE_NEW_DIAGRAM=3;
    private final int ERROR_READ_FILE=4;
    private final int ERROR_WRITE_FILE=5;
    private final int EDIT_EXIT_SAVE=6;
    private final int EDIT_NEW=7;
    private final int EDIT_OPEN=8;


    CanvasTouchListener listener=new CanvasTouchListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        fm=new FileManager(ApplicationSettings.getBaseDir().getAbsolutePath(),ApplicationSettings.getBaseDir().getAbsolutePath(),new FileManager.Rule(true,true));


        documentOptions=new HashMap<String, String>(){{
            put("readyForSave","false");
            put("saveBeforeClose","");
        }};

        final View canvas = findViewById(R.id.canvas);
        ViewTreeObserver vto = canvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Log.w("Action","OnCreate");
                draw=(DocumentDraw)canvas;
                //DocumentDraw.setCanvasSize(canvas.getWidth(),canvas.getHeight());
                //draw.createCanvas(width,height);
                if(!draw.createView(listener)) {
                    finish();
                }
                createDocument();

                if (Build.VERSION.SDK_INT < 16) {
                    canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.w("onStart", "true");
    }

    protected void onResume(){
        super.onResume();
        Log.w("onResume","true");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("onPause", "true");
    }

    @Override
    public void onBackPressed() {
        if(currentView!=findViewById(R.id.doc_view)){
            switchView();
        }else{
            //Сохранить модель перед выходом?
            if(getOptSaveBeforeClose()==null) {
                showDialog(EDIT_EXIT_SAVE);
            }else{
                if(getOptSaveBeforeClose()){
                    finish();
                    saveFile();
                }else {
                    super.onBackPressed();
                }
            }
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.w("onStop","true");
    }

    //documentOptions
    public void setOptReadyForSave(boolean readyForSave) {
        if(readyForSave){
            documentOptions.put("readyForSave","true");
        }else{
            documentOptions.put("readyForSave","false");
        }
    }

    public boolean getOptReadyForSave() {
        String str=documentOptions.get("readyForSave");
        if(str!=null){
            if(str.equals("true")){
                return true;
            }
        }
        return false;
    }

    //true = yes
    //false = no
    public void setOptSaveBeforeClose(Boolean saveBeforeClose) {
        if(saveBeforeClose==null){
            documentOptions.put("saveBeforeClose","");
        }else {
            if (saveBeforeClose) {
                documentOptions.put("saveBeforeClose", "true");
            } else {
                documentOptions.put("saveBeforeClose", "false");
            }
        }
    }

    //true = yes
    //false = no
    public Boolean getOptSaveBeforeClose() {
        String str=documentOptions.get("saveBeforeClose");
        if(str!=null){
            if(str.equals("")){
                return null;
            }else {
                if (str.equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setCurrentView(View currentView){
        this.currentView=currentView;
        currentView.setVisibility(View.VISIBLE);
    }

    public View getCurrentView(){
        return currentView;
    }

    public void switchView(){
        View documentView=findViewById(R.id.doc_view);
        View modelSettings=findViewById(R.id.model_settings);
        currentView.setVisibility(View.INVISIBLE);
        if(getCurrentView()==documentView){
            setCurrentView(modelSettings);
            showModelSettingsField();
        }else{
            setCurrentView(documentView);
        }
    }

    public void showModelSettingsField(){

        IDEFEntity.Header settings=entity.header;

        if(settings!=null){
            if(settings.getCreationDate()!=null){
                TextView tv=(TextView)findViewById(R.id.model_creation_date);
                tv.setText(settings.getCreationDate());
            }
            EditText et=null;
            if(settings.getTitle()!=null){
                et=(EditText)findViewById(R.id.model_settings_title);
                et.setText(settings.getTitle());
            }
            if(settings.getAuthor()!=null){
                et=(EditText)findViewById(R.id.model_settings_author);
                et.setText(settings.getAuthor());
            }else{
                //Автозаполнение автора
                et=(EditText)findViewById(R.id.model_settings_author);
                et.setText(ApplicationSettings.author);
            }
            if(settings.getProjectName()!=null){
                et=(EditText)findViewById(R.id.model_settings_project);
                et.setText(settings.getProjectName());
            }
            if(settings.getModelName()!=null){
                et=(EditText)findViewById(R.id.model_settings_name);
                et.setText(settings.getModelName());
            }
        }
    }


    public void saveModelSettings(View view){
        IDEFEntity.Header settings=new IDEFEntity.Header();
        settings.setCreationDate(entity.header.getCreationDate());
        EditText et=(EditText)findViewById(R.id.model_settings_title);
        settings.setTitle(et.getText().toString());
        et=(EditText)findViewById(R.id.model_settings_author);
        settings.setAuthor(et.getText().toString());
        et=(EditText)findViewById(R.id.model_settings_project);
        settings.setProjectName(et.getText().toString());
        et=(EditText)findViewById(R.id.model_settings_name);
        settings.setModelName(et.getText().toString());
        entity.header=settings;
        switchView();
    }

    public void clearModelSettings(View view){
        EditText et=(EditText)findViewById(R.id.model_settings_title);
        et.setText("");
        et=(EditText)findViewById(R.id.model_settings_author);
        et.setText("");
        et=(EditText)findViewById(R.id.model_settings_project);
        et.setText("");
        et=(EditText)findViewById(R.id.model_settings_name);
        et.setText("");
    }

    /*
    public void saveDiagramSettings(View view){
        IDEFEntity.Box box=entity.clickedBox;
        EditText et=(EditText)findViewById(R.id.diagram_settings_title);
        box.setParseBoxText(et.getText().toString());
        switchView();
    }
    */

    /*
    public void clearDiagramSettings(View view){
        EditText et=(EditText)findViewById(R.id.diagram_settings_title);
        et.setText("");
    }
    */

    public void createDocument(){
        if(getIntent().getStringExtra("XML")!=null){
            activeFile=new File(getIntent().getStringExtra("XML"));
            openDocument(activeFile);
            setOptReadyForSave(true);
        }else if(getIntent().getStringExtra("IDL")!=null){
            activeFile=new File(getIntent().getStringExtra("IDL"));
            openIDL(activeFile);
            setOptReadyForSave(true);
        }else if(getIntent().getStringExtra("New")!=null){
            newIDL();
        }
    }

    //Метод должен открывать XML-файл
    public void openDocument(File file){
        Log.w("Open", "XML");
    }

    //Открывает IDL-файл
    //file=activeFile
    public void openIDL(File file){
        Log.w("Open","IDL");
        String content;

        content=fm.readFile(file);
        if(content==null){
            //Создадим пустой документ
            //creatDocument()
            showDialog(ERROR_READ_FILE);
            return;
        }

        //draw.setDefaultPaint();

        //IDLRead
        try {
            idlParser = new IDLParser(content);
            entity = idlParser.readModel();
            entity.initAllReference();
            entity.getStartDiagram().setBoxLimit(new IDEFEntity.BoxLimit(1, 1));
        }catch(Exception e){
            showDialog(ERROR_READ_FILE);
            return;
        }
        draw.drawDiagram(entity.goToStartDiagram());
        setCurrentView(findViewById(R.id.doc_view));

        //FragmentToolsMenu добавляет себе единственного слушателя (DocumentActivity),
        //для того, чтобы изменение инструмента, могло влиять на текущую диаграмму
        FragmentToolsMenu.setListenerActivity(this);
        //При успешном открытии файла, добавляем в recent files
        RecentFiles recentFiles=new RecentFiles(this,ApplicationSettings.getBaseDir());
        recentFiles.addFile(file);
        recentFiles.getFiles();
    }

    //Save into Saved.idl
    public void saveIDL(File file){
        IDLParser idlParserWriter=new IDLParser();
        //if(!FileManager.writeFile(file,idlParserWriter.createIDL(entity))){
        if(!fm.writeFile(file,idlParserWriter.createIDL(entity))){
            showDialog(ERROR_WRITE_FILE);
        }
    }


    public void saveFile(){
        if(activeFile!=null){
            saveIDL(activeFile);
        }else{
            exportFile();
        }
    }

    //Вызывает файловый менеджер для открытия нового файла
    public void openFile(){
        Intent intent=new Intent(this,FileActivity.class);
        String root=ApplicationSettings.getBaseDir().getAbsolutePath();
        String current=ApplicationSettings.getBaseDir().getAbsolutePath();
        FileManager fm=new FileManager(root,current,new FileManager.Rule(true,true,true,false,true));
        intent.putExtra(FileManager.class.getCanonicalName(),fm);
        startActivity(intent);
    }

    public void newFile(){
        Intent intent=new Intent(this,DocumentActivity.class);
        intent.putExtra("New", "true");
        startActivity(intent);
    }

    //Создает новый IDL-файл
    public void newIDL(){
        //openIDL
        activeFile=null;
        entity=new IDEFEntity();
        IDEFEntity.Diagram diagram=new IDEFEntity.Diagram(0,new IDEFEntity.BoxLimit(1,1));
        entity.setCurrentDiagram(diagram);
        entity.setStartDiagramIfNotSet();
        entity.addDiagram(diagram);
        entity.initCreationDate();
        draw.drawDiagram(entity.goToStartDiagram());

        setCurrentView(findViewById(R.id.model_settings));

        //FragmentToolsMenu добавляет себе единственного слушателя (DocumentActivity),
        //для того, чтобы изменение инструмента, могло влиять на текущую диаграмму
        FragmentToolsMenu.setListenerActivity(this);
        showModelSettingsField();
    }

    //Экспорт файла, может быть вызван в случае сохранения
    //нового файла
    public void exportFile(){
        Intent intent=new Intent(this,FileActivity.class);
        String root=ApplicationSettings.getBaseDir().getAbsolutePath();
        String current=ApplicationSettings.getBaseDir().getAbsolutePath();
        FileManager fm=new FileManager(root,current,new FileManager.Rule(true,true,true,true,true));

        File fileForSave=new File(ApplicationSettings.getBaseDir().getAbsolutePath()+"/Saved.idl");
        saveIDL(fileForSave);

        fm.setFileForSave(fileForSave);

        //finish();
        intent.putExtra(FileManager.class.getCanonicalName(), fm);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document, menu);
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        final EditText inputText;
        switch(id){
            case EDIT_TEXT_BOX:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_box_text));
                inputText=new EditText(this);
                inputText.setId(0);
                inputText.setText(((IDEFEntity.Box)entity.getCurrentDiagram().getSelectedObject()).getBoxText());
                dialogBuilder.setView(inputText);
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ((IDEFEntity.Box)entity.getCurrentDiagram().getSelectedObject()).setBoxText(inputText.getText().toString());
                        draw.drawDiagram(entity.getCurrentDiagram());
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
            case EDIT_TEXT_LABEL:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_label_text));
                inputText=new EditText(this);
                inputText.setId(0);
                inputText.setText(((IDEFEntity.Arrow.ArrowLabel)entity.getCurrentDiagram().getSelectedObject()).getText());
                dialogBuilder.setView(inputText);
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //((IDEFEntity.Arrow.ArrowLabel)entity.getCurrentDiagram().getSelectedObject()).setText(inputText.getText().toString());
                        IDEFEntity.Arrow.ArrowLabel label=(IDEFEntity.Arrow.ArrowLabel) entity.getCurrentDiagram().getSelectedObject();
                        entity.setArrowLabelText(label.parent,inputText.getText().toString());
                        draw.drawDiagram(entity.getCurrentDiagram());
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
            case EDIT_EXIT_SAVE:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_save_before_close));
                dialogBuilder.setPositiveButton(getString(R.string.btn_yes), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setOptSaveBeforeClose(true);
                        onBackPressed();
                        return;
                    }
                });

                dialogBuilder.setNeutralButton(getString(R.string.btn_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        setOptSaveBeforeClose(false);
                        onBackPressed();
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
            case EDIT_NEW:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_new));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                        newFile();
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
            case EDIT_OPEN:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_open));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                        openFile();
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
            case CREATE_NEW_DIAGRAM:
                dialogBuilder=new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_edit));
                dialogBuilder.setMessage(getString(R.string.msg_edit_create_diagram));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //((IDEFEntity.Arrow.ArrowLabel)entity.getCurrentDiagram().getSelectedObject()).setText(inputText.getText().toString());
                        IDEFEntity.Box box=(IDEFEntity.Box)entity.getCurrentDiagram().getSelectedObject();
                        //String diagramId=box.diagramId;
                        IDEFEntity.Diagram diagram=new IDEFEntity.Diagram(box.diagramId,new IDEFEntity.BoxLimit(2,8));
                        //IDEFEntity.Diagram diagram=entity.initDiagram(diagramId,new IDEFEntity.BoxLimit(2,8));
                        entity.addDiagram(diagram);
                        diagram.newCreationDate();
                        box.setBoxReference(diagram);
                        entity.getCurrentDiagram().initDiagramStartArrows(box);
                        entity.goToLevelDownDiagram();
                        draw.drawDiagram(entity.getCurrentDiagram());
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
            case ERROR_READ_FILE:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_error));
                dialogBuilder.setMessage(getString(R.string.msg_error_read_file));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });
                return dialogBuilder.create();
            case ERROR_WRITE_FILE:
                dialogBuilder = new AlertDialog.Builder(this);
                dialogBuilder.setTitle(getString(R.string.dialog_error));
                dialogBuilder.setMessage(getString(R.string.msg_error_write_file));
                dialogBuilder.setPositiveButton(getString(R.string.btn_positive), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                });
                return dialogBuilder.create();
            default:
                return null;
        }
        //return super.onCreateDialog(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.w("Action", "OnChanged");

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.w("Orientation","landscape");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Log.w("Orientation","portrait");
        }

        /*
        final View canvas=findViewById(R.id.canvas);
        ViewTreeObserver vto = canvas.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {


                int width  = canvas.getMeasuredWidth();
                int height = canvas.getMeasuredHeight();
                draw.createView(width, height);

                if (Build.VERSION.SDK_INT < 16) {
                    canvas.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    canvas.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

            }
        });
        */
    }

    @Override
    public void onMenuNew() {
        Log.w("Menu", "New");
        showDialog(EDIT_NEW);
    }

    @Override
    public void onMenuSave() {
        Log.w("Menu", "Save");
        saveFile();
    }

    @Override
    public void onMenuOpen() {

        Log.w("Menu", "Open new file");
        showDialog(EDIT_OPEN);
        //openFile();
    }

    @Override
    public void onMenuPort() {
        Log.w("Menu","Port");
        exportFile();
    }

    @Override
    public void onMenuLevelDown() {
        Log.w("Menu","Level down");
        if(entity.goToLevelDownDiagram()){
            draw.drawDiagram(entity.getCurrentDiagram());
        }else if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Box){
            showDialog(CREATE_NEW_DIAGRAM);
            entity.clickedBox=(IDEFEntity.Box)entity.getCurrentDiagram().selectedObject;

        }
    }

    @Override
    public void onMenuSettings() {
        Log.w("Menu","Settings");
        switchView();
    }


    @Override
    public void onMenuLevelUp() {
        Log.w("Menu","Level up");
        if(entity.goToLevelUpDiagram()){
            IDEFEntity.Box box=entity.getLinkedBox(entity.getCurrentDiagram());
            if(box!=null){
                entity.clickedBox=box;
            }
            draw.drawDiagram(entity.getCurrentDiagram());
        }
    }


    class CanvasTouchListener{

        //Попали в уже выделенный объект
        boolean isAlreadySelected;
        //Был ли объект передвинут в предыдущем действии?
        boolean objectMoved;

        public void actionDown(DocumentDraw.CanvasTouchEvent event){
            Log.w("Action","Down");
            boolean hit=false;
            PointF touchPoint=draw.pointToModel(draw.getCanvasPoint());
            if(touchPoint==null) {
                return;
            }
            objectMoved=false;


            //Добавление Box и ArrowLabel
            if(FragmentToolsMenu.getCurrentMode().equals("Text")) {
                if (entity.getCurrentDiagram().getSelectedObject() instanceof IDEFEntity.Arrow) {
                    IDEFEntity.Arrow arrow = (IDEFEntity.Arrow) entity.getCurrentDiagram().getSelectedObject();
                    arrow.addLabel(new IDEFEntity.Arrow.ArrowLabel(touchPoint));
                    return;
                }
            }else if(FragmentToolsMenu.getCurrentMode().equals("Box")){
                //Создает и добавляет новый Box по точке на экране
                entity.getCurrentDiagram().addBox(touchPoint);
                if(entity.clickedBox!=null){
                    entity.renameDiagrams(entity.clickedBox);
                }
                return;
            }

            for(IDEFEntity.Box box : entity.getCurrentDiagram().boxSet) {
                if (ToolsActionCalc.isPointInBox(touchPoint, box)) {
                    hit=true;

                    if(entity.getCurrentDiagram().selectedObject==box){
                        isAlreadySelected=true;
                    }

                    double distance=ToolsActionCalc.boxTouchDistance(touchPoint, box);
                    if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                        entity.getCurrentDiagram().trySelectTextContainer(distance, box);

                    } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                        String type=ToolsActionCalc.getBoxConnectorType(touchPoint, box);
                        if(type!=null) {
                            boolean isArrowSource=entity.getCurrentDiagram().newArrow.getSink().getConnectorSink() instanceof IDEFEntity.Arrow.ArrowSource;
                            if((entity.getCurrentDiagram().newArrow.getStage().equals("start") && type.equals("O")) || isArrowSource) {
                                IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                if(relationNum!=0) {
                                    entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                }else{
                                    entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                }
                            }else if(entity.getCurrentDiagram().newArrow.getStage().equals("end") && (!type.equals("O"))){
                                IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                if(relationNum!=0) {
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                }else{
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                }
                            }
                        }
                    } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        entity.getCurrentDiagram().setSelectedObject(box);
                        showDialog(EDIT_TEXT_BOX);
                    } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                        entity.getCurrentDiagram().removeBox(box);
                        if(entity.clickedBox!=null) {
                            entity.renameDiagrams(entity.clickedBox);
                        }
                        return;
                    }

                }
            }

            for(IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet){
                if(arrow.label!=null){
                    if (ToolsActionCalc.isPointInLabel(touchPoint, arrow.label)) {
                        hit = true;

                        if(entity.getCurrentDiagram().selectedObject==arrow.label){
                            isAlreadySelected=true;
                        }

                        double distance=ToolsActionCalc.labelTouchDistance(touchPoint, arrow.label);
                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectTextContainer(distance, arrow.label);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                            if (entity.getCurrentDiagram().newArrow.getStage().equals("start")) {
                                entity.getCurrentDiagram().setSelectedObject(arrow.label);
                                entity.getCurrentDiagram().newArrow.setSource(touchPoint, null, arrow.label);
                            }else{
                                entity.getCurrentDiagram().setSelectedObject(arrow.label);
                                entity.getCurrentDiagram().newArrow.setSink(touchPoint, null, arrow.label);
                            }
                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {
                            entity.getCurrentDiagram().setSelectedObject(arrow.label);
                            showDialog(EDIT_TEXT_LABEL);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            arrow.removeLabel();
                        }
                    }
                }
            }

            if(!hit) {

                for (IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet) {
                    if (ToolsActionCalc.isPointBesideConnector(touchPoint, arrow.source)) {
                        hit=true;

                        if(entity.getCurrentDiagram().selectedObject==arrow.source){
                            isAlreadySelected=true;
                        }

                        double distance=ToolsActionCalc.connectorTouchDistance(touchPoint, arrow.source);
                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectConnector(distance, arrow.source);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode()



                                == "Arrow") {
                            if (entity.getCurrentDiagram().newArrow.getStage().equals("start")) {
                                entity.getCurrentDiagram().setSelectedObject(arrow.source);
                                //setSink, ArrowSink должно быть Sink объекта newArrow
                                entity.getCurrentDiagram().newArrow.setSink(arrow.source.getConnectorPoint(), null, arrow.source);
                            }
                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            //entity.getCurrentDiagram().removeArrow(arrow);
                        }

                        //entity.getCurrentDiagram().forceSelectArrow();
                    }else if(ToolsActionCalc.isPointBesideConnector(touchPoint, arrow.sink)){
                        hit=true;

                        if(entity.getCurrentDiagram().selectedObject==arrow.sink){
                            isAlreadySelected=true;
                        }

                        double distance=ToolsActionCalc.connectorTouchDistance(touchPoint, arrow.sink);
                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectConnector(distance, arrow.sink);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                            if (entity.getCurrentDiagram().newArrow.getStage().equals("start")) {
                                entity.getCurrentDiagram().setSelectedObject(arrow.sink);
                                entity.getCurrentDiagram().newArrow.setSource(arrow.sink.getConnectorPoint(), null, arrow.sink);
                            }
                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            //entity.getCurrentDiagram().removeArrow(arrow);
                        }
                    }

                }
            }

            if(!hit) {

                //Border
                String type = ToolsActionCalc.getBorderTypeByPoint(touchPoint);
                if (type != null && FragmentToolsMenu.getCurrentMode() == "Arrow") {
                    if (entity.getCurrentDiagram().newArrow.getStage().equals("start")  && (!type.equals("O"))) {
                        IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                        entity.getCurrentDiagram().setSelectedObject(border);
                        //entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(draw.touch.touchPoint, border), type, border);

                        int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                        if(relationNum!=0) {
                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                        }else{
                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                        }

                        return;
                    } else if (entity.getCurrentDiagram().newArrow.getStage().equals("end") && (type.equals("O"))) {
                        if(!(entity.getCurrentDiagram().newArrow.getSource().getConnectorSource() instanceof IDEFEntity.Border)) {
                            IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                            entity.getCurrentDiagram().setSelectedObject(border);
                            //entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(draw.touch.touchPoint, border), type, border);
                            int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                            if(relationNum!=0) {
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                            }else{
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                            }
                            return;
                        }
                    }
                }


                for (IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet) {
                    if (ToolsActionCalc.isPointBesideArrow(touchPoint, arrow)) {
                        hit=true;

                        if(entity.getCurrentDiagram().selectedObject==arrow){
                            isAlreadySelected=true;
                        }

                        double distance=ToolsActionCalc.arrowTouchDistance(touchPoint, arrow);
                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectArrow(distance,arrow);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                            if (entity.getCurrentDiagram().newArrow.getStage().equals("start")) {
                                entity.getCurrentDiagram().setSelectedObject(arrow);
                                entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getPointBesideArrow(touchPoint, arrow), null, arrow);
                            }else if(entity.getCurrentDiagram().newArrow.getStage().equals("end")){
                                entity.getCurrentDiagram().setSelectedObject(arrow);
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getPointBesideArrow(touchPoint, arrow), null, arrow);
                            }
                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            entity.getCurrentDiagram().removeArrow(arrow);
                            return;
                        }

                        //entity.getCurrentDiagram().forceSelectArrow();
                    }

                }
            }

        }


        //Идея переделать работу событий. События где будет изменяться состояние объектов канваса в конце завершаются drawDiagram
        //остальные сразу завершаются moveCamera() и return;
        public void actionMove(DocumentDraw.CanvasTouchEvent event){
            Log.w("Action","Move");
            PointF touchPoint=draw.pointToModel(draw.getCanvasPoint());
            if(touchPoint==null) {
                return;
            }
            boolean hit=false;

            //Действия с выделенными объектами
            if(isAlreadySelected) {
                hit=true;

                if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Border) {
                    String type = ToolsActionCalc.getBorderTypeByPoint(touchPoint);
                    if (type != null && FragmentToolsMenu.getCurrentMode() == "Arrow") {
                        if (entity.getCurrentDiagram().newArrow.getStage().equals("start") && (!type.equals("O"))) {
                            IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                            entity.getCurrentDiagram().setSelectedObject(border);
                            //entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(draw.touch.touchPoint, border), type, border);

                            int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                            if(relationNum!=0) {
                                entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                            }else{
                                entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                            }

                        } else if (entity.getCurrentDiagram().newArrow.getStage().equals("end") && (type.equals("O"))) {
                            if (!(entity.getCurrentDiagram().newArrow.getSource().getConnectorSource() instanceof IDEFEntity.Border)) {
                                IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                                entity.getCurrentDiagram().setSelectedObject(border);
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                                int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                                if(relationNum!=0) {
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                                }else{
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                                }

                            }
                        }

                    }
                }else if (entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Box) {
                    IDEFEntity.Box box = (IDEFEntity.Box) entity.getCurrentDiagram().selectedObject;
                    if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                        entity.getCurrentDiagram().moveBox(box, touchPoint);
                        if(entity.clickedBox!=null) {
                            entity.renameDiagrams(entity.clickedBox);
                        }
                        objectMoved = true;
                    } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                        String type = ToolsActionCalc.getBoxConnectorType(touchPoint, box);
                        if (type != null) {
                            if (entity.getCurrentDiagram().newArrow.getStage().equals("start") && type.equals("O")) {
                                IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                //entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(draw.touch.touchPoint, box), type, box);
                                int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                if(relationNum!=0) {
                                    entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                }else{
                                    entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                }
                            } else if (entity.getCurrentDiagram().newArrow.getStage().equals("end") && (!type.equals("O"))) {
                                IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                //entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(draw.touch.touchPoint, box), type, box);
                                int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                if(relationNum!=0) {
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                }else{
                                    entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                }
                            }
                        }
                    } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                        //entity.getCurrentDiagram().removeArrow(arrow);
                    }
                }else if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow.ArrowLabel){
                    IDEFEntity.Arrow.ArrowLabel label = (IDEFEntity.Arrow.ArrowLabel) entity.getCurrentDiagram().selectedObject;
                    if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                        label.moveLabel(touchPoint);
                        objectMoved = true;
                    } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {

                    }
                } else if (entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow) {
                    IDEFEntity.Arrow arrow = (IDEFEntity.Arrow) entity.getCurrentDiagram().selectedObject;
                    if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                        entity.getCurrentDiagram().moveArrow(arrow,touchPoint);
                        objectMoved = true;
                        //entity.getCurrentDiagram().moveBox(box, draw.touch.touchPoint);
                        //objectMoved = true;
                    } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                    }else if (FragmentToolsMenu.getCurrentMode() == "Arrow"){

                    }else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                        //entity.getCurrentDiagram().removeArrow(arrow);
                    }
                }
            }else {
                //Действия с не выделенными объектами

                String type = ToolsActionCalc.getBorderTypeByPoint(touchPoint);
                if(type!=null && FragmentToolsMenu.getCurrentMode() == "Arrow") {
                    hit=true;

                    if (entity.getCurrentDiagram().newArrow.getStage().equals("start")  && (!type.equals("O"))) {
                        IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                        entity.getCurrentDiagram().setSelectedObject(border);
                        //entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(draw.touch.touchPoint, border), type, border);

                        int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                        if(relationNum!=0) {
                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                        }else{
                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                        }

                    } else if (entity.getCurrentDiagram().newArrow.getStage().equals("end") && (type.equals("O"))) {
                        if(!(entity.getCurrentDiagram().newArrow.getSource().getConnectorSource() instanceof IDEFEntity.Border)) {
                            IDEFEntity.Border border = entity.getCurrentDiagram().getBorderByType(type);
                            entity.getCurrentDiagram().setSelectedObject(border);
                            //entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(draw.touch.touchPoint, border), type, border);
                            int relationNum=entity.getCurrentDiagram().newRelationNumber(border,type);
                            if(relationNum!=0) {
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border, relationNum);
                            }else{
                                entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBorderPoint(touchPoint, border), type, border);
                            }
                        }
                    }

                }


                if(!hit) {

                    for (IDEFEntity.Box box : entity.getCurrentDiagram().boxSet) {
                        if (ToolsActionCalc.isPointInBox(touchPoint, box)) {
                            hit = true;

                            if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                                type = ToolsActionCalc.getBoxConnectorType(touchPoint, box);
                                if (type != null) {
                                    boolean isArrowSource=entity.getCurrentDiagram().newArrow.getSink().getConnectorSink() instanceof IDEFEntity.Arrow.ArrowSource;
                                    if ((entity.getCurrentDiagram().newArrow.getStage().equals("start") && type.equals("O")) || isArrowSource) {
                                        IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                        entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                        //entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(draw.touch.touchPoint, box), type, box);
                                        int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                        if(relationNum!=0) {
                                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                        }else{
                                            entity.getCurrentDiagram().newArrow.setSource(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                        }
                                    } else if (entity.getCurrentDiagram().newArrow.getStage().equals("end") && (!type.equals("O"))) {
                                        IDEFEntity.Box.BoxConnector boxConnector = box.getBoxConnectorByType(type);
                                        entity.getCurrentDiagram().setSelectedObject(boxConnector);
                                        //entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(draw.touch.touchPoint, box), type, box);
                                        int relationNum=entity.getCurrentDiagram().newRelationNumber(box,type);
                                        if(relationNum!=0) {
                                            entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box, relationNum);
                                        }else{
                                            entity.getCurrentDiagram().newArrow.setSink(ToolsActionCalc.getBoxConnectorPoint(touchPoint, box), type, box);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(!hit){
                //Задаем вектор для перемещения камеры
                PointF from=draw.pointToModel(event.source.getTouchPoint());
                PointF to=draw.pointToModel(event.source.getLastTouchPoint());
                draw.camera.moveCamera(from,to);
            }

            draw.drawDiagram(entity.getCurrentDiagram());
        }

        //Не нужна проверка на выход touchPoint за пределы холста
        public void actionZoom(DocumentDraw.CanvasTouchEvent event){
            Log.w("Action","Zoom");
            //Log.w("Move",event.source.touchPoint.x+" "+event.source.touchPoint.y);
            Point touchPoint=event.source.getTouchPoint();
            Point multiTouchPoint=event.source.getMultiTouchPoint();
            Point lastTouchPoint=event.source.getLastTouchPoint();
            Point lastMultiTouchPoint=event.source.getLastMultiTouchPoint();
            double distance=ToolsActionCalc.getDistance(touchPoint,multiTouchPoint);
            double startDistance=ToolsActionCalc.getDistance(lastTouchPoint,lastMultiTouchPoint);
            draw.camera.zoom(distance/startDistance);
            draw.drawDiagram(entity.getCurrentDiagram());
        }


        public void actionCancel(DocumentDraw.CanvasTouchEvent event){
            Log.w("Action","Cancel");
            PointF touchPoint=draw.pointToModel(draw.getCanvasPoint());
            if(touchPoint==null) {
                return;
            }
            boolean hit=false;
            isAlreadySelected=false;


            //События, которые срабатывают не зависимо от того попал ли указатель на объект или нет.
            if (FragmentToolsMenu.getCurrentMode() == "Arrow") {
                //zzz ошибка с режимами Arrow
                if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow.ArrowSource) {
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if (entity.getCurrentDiagram().newArrow.size() == 2) {
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                }else if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow.ArrowSink){
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if (entity.getCurrentDiagram().newArrow.size() == 2) {
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                }else if (entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Border) {
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if (entity.getCurrentDiagram().newArrow.size() == 2) {
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                } else if (entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Box.BoxConnector) {
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if (entity.getCurrentDiagram().newArrow.size() == 2) {
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                } else if (entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow.ArrowLabel) {
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if (entity.getCurrentDiagram().newArrow.size() == 2) {
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                }else if(entity.getCurrentDiagram().selectedObject instanceof IDEFEntity.Arrow){
                    entity.getCurrentDiagram().removeSelectedObject();
                    entity.getCurrentDiagram().newArrow.nextStage();
                    if(entity.getCurrentDiagram().newArrow.size()==2){
                        entity.getCurrentDiagram().buildPath();
                        entity.getCurrentDiagram().newArrow.clearNewArrow();
                    }
                }
            }


            for(IDEFEntity.Box box : entity.getCurrentDiagram().boxSet) {
                if (ToolsActionCalc.isPointInBox(touchPoint, box)) {
                    hit=true;

                    if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                        entity.getCurrentDiagram().trySelectTextContainer(box);
                    } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                    } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                        entity.getCurrentDiagram().removeBox(box);
                        if(entity.clickedBox!=null) {
                            entity.renameDiagrams(entity.clickedBox);
                        }
                    }

                }
            }

            for(IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet) {
                if(arrow.label!=null) {
                    if (ToolsActionCalc.isPointInLabel(touchPoint, arrow.label)) {
                        hit = true;

                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectTextContainer(arrow.label);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            arrow.removeLabel();
                        }

                    }
                }
            }

            if(hit){
                entity.getCurrentDiagram().forceSelectTextContainer();
            }



            if(!hit){
                for (IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet) {
                    if (ToolsActionCalc.isPointBesideConnector(touchPoint, arrow.source)) {
                        hit=true;

                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectConnector(arrow.source);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            //entity.getCurrentDiagram().removeArrow(arrow);
                        }

                    }else if(ToolsActionCalc.isPointBesideConnector(touchPoint, arrow.sink)){
                        hit=true;

                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectConnector(arrow.sink);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            //entity.getCurrentDiagram().removeArrow(arrow);
                        }
                    }

                }
                if(hit){
                    entity.getCurrentDiagram().forceSelectConnector();
                }
            }

            if(!hit) {

                for (IDEFEntity.Arrow arrow : entity.getCurrentDiagram().arrowSet) {
                    if (ToolsActionCalc.isPointBesideArrow(touchPoint, arrow)) {
                        hit=true;

                        if (FragmentToolsMenu.getCurrentMode() == "Pointer") {
                            entity.getCurrentDiagram().trySelectArrow(arrow);
                        } else if (FragmentToolsMenu.getCurrentMode() == "Box") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Arrow") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Text") {

                        } else if (FragmentToolsMenu.getCurrentMode() == "Remove") {
                            //entity.getCurrentDiagram().removeBox(arrow);
                        }
                    }
                }
                if(hit) {
                    entity.getCurrentDiagram().forceSelectArrow();
                }
            }

            draw.drawDiagram(entity.getCurrentDiagram());
        }

    }

}
