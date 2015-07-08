package com.schemecreator.denisuser.schemecreator;

import android.app.Activity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Denisuser on 04.05.2015.
 */

public class ApplicationSettings {
    static Activity curActivity;
    static String author;
    static boolean location;
    static final boolean PHONE=false;
    static final boolean CARD=true;
    static final String[] locationArr={"phone","card"};

    static File xmlFile;

    //Конструктор вызывается только при запуске приложения
    ApplicationSettings(Activity curActivity){
        this.curActivity=curActivity;
        //Имя файла должно быть написано без пробела, так как при работе с XML-файлом в дальнейшем
        //на месте пробелов, добавляется символ "%20"
        xmlFile=new File(curActivity.getApplicationContext().getFilesDir(),"app_settings.xml");

        //Если файл настроек он осутствует,
        //то нужно создать его
        if(!xmlFile.exists()){
            try {
                xmlFile.createNewFile();
                UseXML useXML=new UseXML(xmlFile);
                //Создает необходимые теги, в только что созданном файле
                //с значениями по умолчанию
                useXML.initSettings();
                Log.w("Content:",useXML.readXML());
            }catch(IOException e){
                e.getStackTrace();
                Log.w("File error", "Settings file can't be created.");
                return;
            }
        }

        readAuthor();
        readLocation();

        //Если пользовательские файлы приложения должны быть расположены на SD-карте,
        //но карта не доступна
        if(location){
            if(!isSDCardAvailable()){
                writeLocation(false);
            }
        }
    }


    //Возвращает директорию, в котором будут храниться IDL-файлы
    public static File getBaseDir(){
        File baseFile=null;
        Map<String,String> env=System.getenv();
        if(location){
            //Путь к памяти карты
            //external sd card
            if(System.getenv("SECONDARY_STORAGE")!=null) {
                String externalStorage = System.getenv("SECONDARY_STORAGE");
                baseFile = new File(externalStorage,"SchemeCreator");
            }
        }

        if(baseFile==null){
            //Путь к памяти телефона
            //internal sd card
            String internalStorage = System.getenv("EXTERNAL_STORAGE");
            baseFile = new File(internalStorage,"SchemeCreator");
        }
        return baseFile;
    }

    //Проверяет находится ли файл на SD карте
    public static boolean isOnSDCard(File file){
        boolean result=false;
        if(System.getenv("SECONDARY_STORAGE")!=null) {
            result = file.getAbsolutePath().contains(System.getenv("SECONDARY_STORAGE"));
        }
        return result;
    }

    //Проверяет доступна ли SD карта
    public static boolean isSDCardAvailable(){
        boolean result=false;
        if(System.getenv("SECONDARY_STORAGE")!=null) {
            File cardDir = new File(System.getenv("SECONDARY_STORAGE"));
            result=cardDir.canRead() && cardDir.canWrite();
        }
        return result;
    }

    //Проверяет полностью ли заполнена SD карта
    //API >= 9
    /*
    public static boolean isSDCardFree(){
        boolean result=false;
        if(System.getenv("SECONDARY_STORAGE")!=null) {
            File cardDir = new File(System.getenv("SECONDARY_STORAGE"));
            if(cardDir.getFreeSpace()>0){
                result=true;
            }
        }
        return result;
    }
    */

    static boolean locationToBoolean(String location){
        boolean value = false;
        if(location.equals(locationArr[0])){
            value=PHONE;
        }else if(location.equals(locationArr[1])){
            value=CARD;
        }
        return value;
    }

    static String locationToString(boolean location){
        String value = null;
        if(location==PHONE){
            value=locationArr[0];
        }else if(location==CARD){
            value=locationArr[1];
        }
        return value;
    }

    public static void writeDefaultSettings(){
        writeAuthor("");
        writeLocation(false);
    }

    public static void writeAuthor(String author){
        UseXML useXML=new UseXML(xmlFile);
        if(useXML.setAuthor(author)) {
            setAuthor(author);
        }
    }

    public static String readAuthor(){
        UseXML useXML=new UseXML(xmlFile);
        String author=useXML.getAuthor();
        setAuthor(author);
        return author;
    }

    public static void writeLocation(boolean location) {
        UseXML useXML = new UseXML(xmlFile);
        String value = locationToString(location);
        if(value!=null) {
            if (useXML.setLocation(value)) {
                setLocation(location);
            }
        }
    }

    public static void writeLocation(String location) {
        UseXML useXML = new UseXML(xmlFile);
        String value = location;
        if(value!=null) {
            if (useXML.setLocation(value)) {
                setLocation(locationToBoolean(location));
            }
        }
    }

    public static String readLocation(){
        UseXML useXML=new UseXML(xmlFile);
        String loc=useXML.getLocation();
        setLocation(locationToBoolean(loc));
        return loc;
    }

    static void setLocation(boolean location){
        ApplicationSettings.location=location;
    }

    static boolean getLocation(){
        return location;
    }

    static void setAuthor(String author){
        ApplicationSettings.author=author;
    }

    static String getAuthor(){
        return author;
    }


}
