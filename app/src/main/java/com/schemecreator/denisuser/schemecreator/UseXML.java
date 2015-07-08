package com.schemecreator.denisuser.schemecreator;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Created by Denisuser on 30.04.2015.
 */
public class UseXML {
    File xmlFile;
    DocumentBuilderFactory dbf;
    DocumentBuilder db;

    //Если файл xmlFile не существует, то методы класса должны сразу завершаться
    //Если существует, то должна быть проверка файла на соответствие DOM описанию
    public UseXML(File xmlFile) {
        dbf=DocumentBuilderFactory.newInstance();
        try {
            db=dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        if (xmlFile.exists()) {
            this.xmlFile=xmlFile;
            if (!isXMLFile()) {
                createXMLFile();
            }
        }else{
           Log.w("Error:","XML file not exist");
        }

    }

    public boolean isXMLFile(){
        if(!isFileSet()){
            return false;
        }

        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document doc=db.parse(getFile());
        } catch (SAXException e) {
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void createXMLFile(){
        if(!isFileSet()){
            return;
        }
        try {
            db = dbf.newDocumentBuilder();
            Document doc=db.newDocument();
            Element root=doc.createElement("root");
            doc.appendChild(root);

            TransformerFactory tf=TransformerFactory.newInstance();
            Transformer t=tf.newTransformer();
            DOMSource source=new DOMSource(doc);
            StreamResult result=new StreamResult(getFile());
            t.transform(source, result);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    public boolean isFileSet(){
        if(xmlFile==null){
            return false;
        }else{
            return true;
        }
    }

    public File getFile(){
        return xmlFile;
    }

    public String readXML(){
        if(!isFileSet()){
            return null;
        }

        String result="";
        try {
            BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(getFile())));
            String str=null;
            while((str=br.readLine())!=null){
                result+=str;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //--------------------------------------recent_files.xml----------------------------------------

    public void writeXML(){
        if(!isFileSet()){
            return;
        }

        try {
            Document doc=db.newDocument();
            Element root=doc.createElement("root");
            doc.appendChild(root);
            for(int i=0;i<5;i++){
                Element f=doc.createElement("file");
                Attr id=doc.createAttribute("id");
                id.setValue(String.valueOf(i));
                f.setAttributeNode(id);
                Node path=doc.createTextNode("path");
                f.appendChild(path);
                root.appendChild(f);
            }
            TransformerFactory tf=TransformerFactory.newInstance();
            Transformer t=tf.newTransformer();
            DOMSource source=new DOMSource(doc);
            StreamResult result=new StreamResult(getFile());
            t.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<File> readRecentFiles(){
        if(!isFileSet()){
            return null;
        }

        ArrayList<File> pathList=new ArrayList<File>();
        try {
            db=dbf.newDocumentBuilder();
            Document doc=db.parse(getFile());

            Node root=doc.getFirstChild();
            NodeList files=root.getChildNodes();
            for(int i=0;i<files.getLength();i++){
                Node item=files.item(i);
                pathList.add(new File(item.getTextContent()));
                Log.w("Recent file "+String.valueOf(i),item.getTextContent());
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return pathList;
    }

    public void numerateFiles(NodeList files){
        for(int i=0;i<files.getLength();i++){
            NamedNodeMap attr=files.item(i).getAttributes();
            Node id=attr.getNamedItem("id");
            id.setNodeValue(String.valueOf(i));
        }
    }

    //Удаляет первого потомка объекта root
    public void deleteFirstElement(Node root){
        Node firstChild=root.getFirstChild();
        root.removeChild(firstChild);
    }

    //Удаляет <file> у которого содержимое равно path
    public void deleteFile(String path){
        if(!isFileSet()){
            return;
        }

        try {
            db=dbf.newDocumentBuilder();
            Document doc=db.parse(getFile());

            Node root=doc.getFirstChild();
            NodeList files=root.getChildNodes();
            for(int i=0;i<files.getLength();i++){
                Node file=files.item(i);
                if(file.getTextContent().toString().equals(path)){
                    root.removeChild(file);
                }
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            StreamResult result = new StreamResult(getFile());
            DOMSource dom = new DOMSource(doc);
            t.transform(dom, result);
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

    //Проверяет теги <file> на существование файла
    public void checkFiles() {
        if (!isFileSet()) {
            return;
        }

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(getFile());

            Node root = doc.getFirstChild();
            NodeList files = root.getChildNodes();
            for (int i = 0; i < files.getLength(); i++) {
                Node file = files.item(i);
                String path=file.getTextContent();
                if(path!=null){
                    File f=new File(path);
                    if(!f.exists()){
                        root.removeChild(file);
                    }
                }
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            StreamResult result = new StreamResult(getFile());
            DOMSource dom = new DOMSource(doc);
            t.transform(dom, result);
        } catch (SAXException | IOException | ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
        }
    }

        //Если текущий файл в спске последний.
    boolean isFilePathLast(String path){
        try {
            db = dbf.newDocumentBuilder();
            Document doc=db.parse(getFile());
            Node root=doc.getFirstChild();
            Node lastNode=root.getLastChild();
            if(lastNode!=null) {
                String content = lastNode.getTextContent();
                if (content.equals(path)) {
                    return true;
                }
            }
        } catch (SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }
        return false;
    }

    //Удаляет все элементы с путями до файлов
    public void deletePaths(){
        if(!isFileSet()){
            return;
        }

        createXMLFile();
    }

    public void addFilePath(String path){
        if(!isFileSet()){
            return;
        }

        try {
            Document doc=db.parse(getFile());
            Node root=doc.getFirstChild();
            //Если текущий файл в спске последний,
            //то его не нужно записывать в конец списка
            if(!isFilePathLast(path)) {
                Element file = doc.createElement("file");
                Attr id = doc.createAttribute("id");
                id.setValue("0");
                file.setAttributeNode(id);
                file.setTextContent(path);
                root.appendChild(file);
                if (root.getChildNodes().getLength() > 5) {
                    deleteFirstElement(root);
                }
                numerateFiles(root.getChildNodes());
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer t = tf.newTransformer();
                StreamResult result = new StreamResult(getFile());
                DOMSource dom = new DOMSource(doc);
                t.transform(dom, result);
            }
        } catch (SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }


    //--------------------------------------app_settings.xml----------------------------------------


    //Заносит в XML-файл теги без содержания
    //состояние нового XML-файла
    public void initSettings(){
        if(!isFileSet()){
            return;
        }

        try {
            Document doc=db.parse(getFile());
            Node root=doc.getFirstChild();

            Node author=doc.createElement("author");
            root.appendChild(author);

            Node location=doc.createElement("location");
            location.setTextContent(ApplicationSettings.locationArr[0]);
            root.appendChild(location);

            DOMSource source=new DOMSource(doc);
            StreamResult result=new StreamResult(getFile());
            TransformerFactory tf=TransformerFactory.newInstance();

            Transformer t=tf.newTransformer();
            t.transform(source,result);
        } catch (TransformerException | IOException | SAXException e) {
            e.printStackTrace();
        }

    }

    public boolean setAuthor(String name){
        try {
            if(name!=null) {
                Document doc = db.parse(getFile());
                NodeList author = doc.getElementsByTagName("author");
                if (author.getLength() > 0) {
                    author.item(0).setTextContent(name);
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(getFile());
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer t=tf.newTransformer();
                    t.transform(source,result);
                    return true;
                }
            }
        } catch (SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getAuthor(){
        String name="";
        try {
            Document doc = db.parse(getFile());
            NodeList author = doc.getElementsByTagName("author");
            if (author.getLength() > 0) {
                name=author.item(0).getTextContent();
            }

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return name;
    }

    //тег location может иметь два состояния: phone и card
    public boolean setLocation(String value){
        try {
            if(value!=null) {
                Document doc = db.parse(getFile());
                NodeList location = doc.getElementsByTagName("location");
                if (location.getLength() > 0) {
                    location.item(0).setTextContent(value);
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(getFile());
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer t=tf.newTransformer();
                    t.transform(source,result);
                    return true;
                }
            }
        } catch (SAXException | IOException | TransformerException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getLocation(){
        String name="";
        try {
            Document doc = db.parse(getFile());
            NodeList location = doc.getElementsByTagName("location");
            if (location.getLength() > 0) {
                name=location.item(0).getTextContent();
            }

        } catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        return name;
    }

}
