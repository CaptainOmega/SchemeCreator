package com.schemecreator.denisuser.schemecreator;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Denisuser on 28.04.2015.
 */
public interface FileListInterface {

    //Метод должен быть вызван, после нажатия на файл из списка
    public void getMessage(String id);

    //Метод должен быть использован для приведения массива файлов к
    //списку FileInfo объектов
    public ArrayList<FileInfo> getFileListItems(File[] file);
}
