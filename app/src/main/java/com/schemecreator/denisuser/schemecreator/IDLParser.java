package com.schemecreator.denisuser.schemecreator;

import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Denisuser on 12.11.2014.
 */
public class IDLParser {
    public String content;
    public int currentInd;
    public int currentBoxInd;
    public int currentArrowInd;

    //Для чтения
    IDLParser(String content){
        this.content=content;
    }

    //Для записи
    IDLParser(){

    }

    //Index function for Diagram text
    public void setCurrentInd(int pos){
        currentInd=pos;
    }

    public int getCurrentInd(){
        return currentInd;
    }

    public void setCurrentIndToStart(){
        setCurrentInd(0);
    }

    public boolean isCurrentIndNatural(){
        if(currentInd==-1)
            return false;
        return true;
    }

    //Index function for Box text
    public void setCurrentBoxInd(int pos){
        currentBoxInd=pos;
    }

    public int getCurrentBoxInd(){
        return currentBoxInd;
    }

    public void setCurrentBoxIndToStart(){
        setCurrentBoxInd(0);
    }

    public boolean isCurrentBoxIndNatural(){
        if(currentBoxInd==-1)
            return false;
        return true;
    }

    //Index function for Arrow text
    public void setCurrentArrowInd(int pos){
        currentArrowInd=pos;
    }

    public int getCurrentArrowInd(){
        return currentArrowInd;
    }

    public void setCurrentArrowIndToStart(){
        setCurrentArrowInd(0);
    }

    public boolean isCurrentArrowIndNatural(){
        if(currentArrowInd==-1)
            return false;
        return true;
    }

    IDEFEntity readModel(){
        IDEFEntity entity=new IDEFEntity();
        //Инициализация заголовка модели
        readHeader(entity);

        //Чтение диаграмм IDL-файла
        while(isCurrentIndNatural()) {
            String diagramString=diagramReader();
            if(diagramString!=null) {
                int diagramId=Integer.valueOf(diagramId(diagramString));
                IDEFEntity.Diagram diagram=new IDEFEntity.Diagram(diagramId,new IDEFEntity.BoxLimit(2,8));
                diagram.setCreationDate(diagramCreationDate(diagramString));
                diagram.setRevisionDate(diagramRevisionDate(diagramString));
                entity.setCurrentDiagram(diagram);
                entity.setStartDiagramIfNotSet();
                entity.addDiagram(diagram);
                while (isCurrentBoxIndNatural()) {
                    String boxString=boxReader(diagramString);
                    if(boxString!=null) {
                        int number=boxNumber(boxString);
                        String name=boxName(boxString);
                        diagramId=Integer.valueOf(boxReference(boxString));
                        String coordStr = boxCoordinates(boxString);
                        IDEFEntity.Box.BoxCoord boxCoord = new IDEFEntity.Box.BoxCoord(IDLParser.boxCoordStringToFloat(coordStr));
                        diagram.addDiagramNumber(diagramId);
                        diagram.addBox(new IDEFEntity.Box(number,name,boxCoord,diagramId));
                    }
                }
                setCurrentBoxIndToStart();
                while (isCurrentArrowIndNatural()) {
                    String arrowString=arrowReader(diagramString);
                    if(arrowString!=null) {
                        int number = arrowNumber(arrowString);
                        String source = arrowSource(arrowString);
                        String sourceObj = arrowConnectObj(source);

                        //Пример заполнения source/sink, класса arrow
                        /*
                        String sourceObjNumber=null;
                        String sourceRelationCount=null;
                        String sourceRelationType=null;

                        if ((!sourceObj.equals("BORDER")) && (!sourceObj.equals("none")) && sourceObj!=null){
                            String sourceRelation = idlParser.arrowConnectRelation(source);
                            sourceObjNumber=idlParser.arrowConnectObjName(sourceRelation);
                            sourceRelationCount=idlParser.arrowConnectRelationCount(sourceRelation);
                            sourceRelationType=idlParser.arrowConnectRelationType(sourceRelation);

                        }
                        */

                        String sink = arrowSink(arrowString);
                        String sinkObj = arrowConnectObj(sink);

                        //Пример заполнения source/sink, класса arrow
                        /*
                        String sinkObjNumber=null;
                        String sinkRelationCount=null;
                        String sinkRelationType=null;

                        if ((!sinkObj.equals("BORDER")) && (!sinkObj.equals("none")) && sinkObj!=null) {
                            String sinkRelation = idlParser.arrowConnectRelation(sink);
                            sinkObjNumber=idlParser.arrowConnectObjName(sinkRelation);
                            sinkRelationCount=idlParser.arrowConnectRelationCount(sinkRelation);
                            sinkRelationType=idlParser.arrowConnectRelationType(sinkRelation);
                        }
                        */

                        String coordStr = arrowPath(arrowString);
                        float[] arrowCoord=IDLParser.arrowCoordStringToFloat(coordStr);
                        //new IDEFEntity.Arrow.ArrowCoord(IDLParser.arrowCoordStringToFloat(coordStr));
                        String labelName=IDLParser.arrowLabelName(arrowString);
                        if(labelName!=null) {
                            String labelCoordString=IDLParser.arrowLabelCoordinates(arrowString);
                            if(labelCoordString!=null) {
                                String[] labelCoord = labelCoordString.split(" ");
                                if (labelCoord.length == 2) {
                                    IDEFEntity.Arrow.ArrowLabel arrowLabel = new IDEFEntity.Arrow.ArrowLabel(labelName, Float.valueOf(labelCoord[0]), Float.valueOf(labelCoord[1]));
                                    String squigleString = IDLParser.arrowSquiggle(arrowString);
                                    if (squigleString != null) {
                                        String[] squiggleCoord = squigleString.split(" ");
                                        if (squiggleCoord.length == 4) {
                                            IDEFEntity.Arrow.ArrowSquiggle squiggle = new IDEFEntity.Arrow.ArrowSquiggle(Float.valueOf(squiggleCoord[0]), Float.valueOf(squiggleCoord[1]), Float.valueOf(squiggleCoord[2]), Float.valueOf(squiggleCoord[3]));
                                            IDEFEntity.Arrow arrow=new IDEFEntity.Arrow(number, source, sink, arrowCoord, arrowLabel, squiggle);
                                            diagram.addArrow(arrow);
                                            arrowLabel.setArrowParent(arrow);
                                            squiggle.setArrowParent(arrow);
                                        }
                                    } else {
                                        IDEFEntity.Arrow arrow=new IDEFEntity.Arrow(number, source, sink, arrowCoord, arrowLabel);
                                        diagram.addArrow(arrow);
                                        arrowLabel.setArrowParent(arrow);
                                    }
                                }
                            }
                        }else {
                            diagram.addArrow(new IDEFEntity.Arrow(number, source, sink, arrowCoord));
                        }
                    }
                }
                setCurrentArrowIndToStart();
            }
        }
        setCurrentIndToStart();
        content=null;
        return entity;
    }

    public void readHeader(IDEFEntity entity){
        int startInd=0;
        int endInd=0;
        startInd = 0;
        endInd=content.indexOf("   ;",startInd);
        if(endInd==-1){
            return;
        }
        String header = content.substring(startInd, endInd);
        if(header.length()!=0) {
            entity.header.setTitle(readTitle(header));
            entity.header.setAuthor(readAuthor(header));
            entity.header.setCreationDate(readCreationDate(header));
            entity.header.setProjectName(readProjectName(header));
            entity.header.setModelName(readModelName(header));
        }
    }

    public String readTitle(String header){
        int startInd=0;
        int endInd=0;
        startInd = header.indexOf("TITLE '",startInd);
        if(startInd==-1){
            return null;
        }
        endInd=header.indexOf("' ;",startInd);
        if(endInd==-1){
            return null;
        }
        startInd = startInd + "TITLE '".length();
        String result = header.substring(startInd, endInd);
        Log.w("TITLE",result);
        return result;
    }

    public String readAuthor(String header){
        int startInd=0;
        int endInd=0;
        startInd = header.indexOf("AUTHOR '",startInd);
        if(startInd==-1){
            return null;
        }
        endInd=header.indexOf("' ;",startInd);
        if(endInd==-1){
            return null;
        }
        startInd = startInd + "AUTHOR '".length();
        String result = header.substring(startInd, endInd);
        Log.w("AUTHOR",result);
        return result;
    }

    public String readCreationDate(String header){
        int startInd=0;
        int endInd=0;
        startInd = header.indexOf("CREATION DATE ",startInd);
        if(startInd==-1){
            return null;
        }
        endInd=header.indexOf(" ;",startInd);
        if(endInd==-1){
            return null;
        }
        startInd = startInd + "CREATION DATE ".length();
        String result = header.substring(startInd, endInd);
        Log.w("CREATION DATE",result);
        return result;
    }

    public String readProjectName(String header){
        int startInd=0;
        int endInd=0;
        startInd = header.indexOf("PROJECT NAME '",startInd);
        if(startInd==-1){
            return null;
        }
        endInd=header.indexOf("' ;",startInd);
        if(endInd==-1){
            return null;
        }
        startInd = startInd + "PROJECT NAME '".length();
        String result = header.substring(startInd, endInd);
        Log.w("PROJECT NAME",result);
        return result;
    }

    public String readModelName(String header){
        int startInd=0;
        int endInd=0;
        startInd = header.indexOf("MODEL '",startInd);
        if(startInd==-1){
            return null;
        }
        startInd = startInd + "MODEL '".length();
        endInd=header.indexOf("'",startInd);
        if(endInd==-1){
            return null;
        }

        String result = header.substring(startInd, endInd);
        Log.w("MODEL NAME",result);
        return result;
    }

    public String diagramReader(){
        int startInd=0;
        int endInd=0;
        startInd=getCurrentInd();
        startInd = content.indexOf("DIAGRAM GRAPHIC",startInd);
        if(startInd==-1){
            setCurrentInd(-1);
            return null;
        }
        endInd=content.indexOf("ENDDIAGRAM ;",startInd);
        if(endInd==-1){
            setCurrentInd(-1);
            return null;
        }
        endInd = endInd + "ENDDIAGRAM ;".length();
        String result = content.substring(startInd, endInd);
        setCurrentInd(endInd);
        Log.w("Diagram",result);
        return result;
    }

    public String diagramId(String diagramString){
        if(diagramString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "DIAGRAM GRAPHIC ".length()+diagramString.indexOf("DIAGRAM GRAPHIC ",startInd);
        endInd=diagramString.indexOf(" ;",startInd);
        String result = diagramString.substring(startInd, endInd);
        Log.w("Diagram name",result);
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(result);
        while(matcher.find()){
            result=matcher.group();
        }
        return result;
    }

    public String diagramCreationDate(String diagramString){
        if(diagramString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "CREATION DATE ".length()+diagramString.indexOf("CREATION DATE ",startInd);
        endInd=diagramString.indexOf(" ;",startInd);
        String result = diagramString.substring(startInd, endInd);
        Log.w("CREATION DATE",result);
        return result;
    }

    public String diagramRevisionDate(String diagramString){
        if(diagramString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "REVISION DATE ".length()+diagramString.indexOf("REVISION DATE ",startInd);
        endInd=diagramString.indexOf(" ;",startInd);
        String result = diagramString.substring(startInd, endInd);
        Log.w("REVISION DATE",result);
        return result;
    }


    public String boxReader(String diagramString){
        if(diagramString==null){
            setCurrentBoxInd(-1);
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd=getCurrentBoxInd();
        startInd = diagramString.indexOf("BOX",startInd);
        if(startInd==-1){
            setCurrentBoxInd(-1);
            return null;
        }
        endInd=diagramString.indexOf("ENDBOX ;",startInd);
        if(endInd==-1){
            setCurrentBoxInd(-1);
            return null;
        }
        endInd = endInd + "ENDBOX ;".length();
        String result = diagramString.substring(startInd, endInd);
        setCurrentBoxInd(endInd);
        Log.w("Box",result);
        return result;
    }

    public Integer boxNumber(String boxString){
        if(boxString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "BOX ".length()+boxString.indexOf("BOX ",startInd);
        endInd=boxString.indexOf(" ;",startInd);
        String result = boxString.substring(startInd, endInd);
        Log.w("Box number",result);
        return Integer.parseInt(result);
    }

    public String boxName(String boxString){
        if(boxString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "NAME '".length()+boxString.indexOf("NAME '",startInd);
        endInd=boxString.indexOf(" ;",startInd);
        String sub = boxString.substring(startInd, endInd);

        startInd=0;
        endInd=0;
        startInd = "}".length()+sub.indexOf("}",startInd);
        endInd=sub.indexOf("'",startInd);
        String result="(Без названия)";
        if(startInd!=endInd) {
            result = sub.substring(startInd, endInd);
        }
        return result;
    }

    public String boxCoordinates(String boxString){
        if(boxString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "BOX COORDINATES ".length()+boxString.indexOf("BOX COORDINATES ",startInd);
        endInd=boxString.indexOf(" ;",startInd);
        String sub = boxString.substring(startInd, endInd);
        Log.w("Sub coord",sub);
        Pattern pattern=Pattern.compile("([0-9]|,)+");
        Matcher matcher=pattern.matcher(sub);
        String result="";
        while(matcher.find()){
            result+=matcher.group()+" ";
        }
        result=result.substring(0,result.length()-1);
        result=result.replace(',','.');
        Log.w("Box coord",result);
        return result;
    }

    public String boxReference(String boxString){
        if(boxString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "DETAIL REFERENCE N ".length()+boxString.indexOf("DETAIL REFERENCE N ",startInd);
        endInd=boxString.indexOf(" ;",startInd);
        String result = boxString.substring(startInd, endInd);
        Log.w("Box reference",result);
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(result);
        if(matcher.find()){
            result=matcher.group();
        }
        return result;
    }

    public String arrowReader(String diagramString){
        if(diagramString==null){
            setCurrentArrowInd(-1);
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd=getCurrentArrowInd();
        startInd = diagramString.indexOf("ARROWSEG",startInd);
        if(startInd==-1){
            setCurrentArrowInd(-1);
            return null;
        }
        endInd=diagramString.indexOf("ENDSEG ;",startInd);
        if(endInd==-1){
            setCurrentArrowInd(-1);
            return null;
        }
        endInd = endInd + "ENDSEG ;".length();
        String result = diagramString.substring(startInd, endInd);
        setCurrentArrowInd(endInd);
        Log.w("Arrow",result);
        return result;
    }

    public Integer arrowNumber(String arrowString){
        if(arrowString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "ARROWSEG ".length()+arrowString.indexOf("ARROWSEG ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String result = arrowString.substring(startInd, endInd);
        Log.w("Arrow number",result);
        return Integer.parseInt(result);
    }

    public String arrowSource(String arrowString){
        if(arrowString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "SOURCE ".length()+arrowString.indexOf("SOURCE ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);
        if(arrowConnectIsTunnel(sub)){
            return "none";
        }
        /*else if(arrowConnectIsBorder(sub)){
            return sub;
        }else if(arrowConnectIsBox(sub)){
            return sub;
        }
        */
        Log.w("Arrow source",sub);
        return sub;
    }

    public String arrowSink(String arrowString){
        if(arrowString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "SINK ".length()+arrowString.indexOf("SINK ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);
        if(arrowConnectIsTunnel(sub)){
            return "none";
        }
        /*else if(arrowConnectIsBorder(sub)){
            return sub;
        }else if(arrowConnectIsBox(sub)){
            return sub;
        }
        */
        Log.w("Arrow sink",sub);
        return sub;
    }

    public static ArrayList<Integer> arrowSourceJoin(String connectorContent){
        ArrayList<Integer> list=new ArrayList<Integer>();
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(connectorContent);
        while(matcher.find()){
            list.add(Integer.parseInt(matcher.group()));
        }
        return list;
    }

    public static Integer arrowSinkJoin(String connectorContent){
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(connectorContent);
        if(matcher.find()){
            return Integer.parseInt(matcher.group());
        }
        return null;
    }

    public static Integer arrowSourceBranch(String connectorContent){
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(connectorContent);
        if(matcher.find()){
            return Integer.parseInt(matcher.group());
        }
        return null;
    }


    public static ArrayList<Integer> arrowSinkBranch(String connectorContent){
        ArrayList<Integer> list=new ArrayList<Integer>();
        Pattern pattern=Pattern.compile("[0-9]+");
        Matcher matcher=pattern.matcher(connectorContent);
        while(matcher.find()){
            list.add(Integer.parseInt(matcher.group()));
        }
        return list;
    }

    public static String arrowConnectObj(String connectorString){
        if(connectorString==null){
            return null;
        }
        if(connectorString=="none"){
            return "none";
        }
        String[] arr=connectorString.split(" ");
        if(arr.length!=0) {
            return arr[0];
        }
        return null;
    }

    public static String arrowConnectContent(String connectorString){
        if(connectorString==null){
            return null;
        }
        if(connectorString=="none"){
            return "none";
        }
        String[] arr=connectorString.split(" ");
        if(arr.length>=2) {
            return connectorString.substring(arr[0].length()+1);
        }
        return null;
    }

    public String arrowConnectRelation(String connectorString){
        if(connectorString==null){
            return null;
        }
        if(connectorString=="none"){
            return "none";
        }
        String[] arr=connectorString.split(" ");
        if(arr.length>=2) {
            return arr[1];
        }
        return null;
    }

    public static String arrowConnectObjName(String connectorString){
        if(connectorString==null){
            return null;
        }
        Pattern pattern=Pattern.compile("^[0-9]+");
        Matcher matcher=pattern.matcher(connectorString);
        String result=null;
        while(matcher.find()){
            result=matcher.group();
        }
        return result;
    }

    public static String arrowConnectRelationNumber(String connectorString){
        if(connectorString==null){
            return null;
        }
        String result=null;
        if(connectorString.split(" ").length>1){
            String[] splitStr=connectorString.split(" ");
            connectorString=new String(splitStr[0]);
        }
        Pattern pattern=Pattern.compile("[0-9]+$");
        Matcher matcher=pattern.matcher(connectorString);

        while(matcher.find()){
            result=matcher.group();
        }


        return result;
    }

    public static String arrowConnectRelationType(String connectorString){
        if(connectorString==null){
            return null;
        }
        Pattern pattern=Pattern.compile("[A-Za-z]+");
        Matcher matcher=pattern.matcher(connectorString);
        String result=null;
        while(matcher.find()){
            result=matcher.group();
        }
        return result;
    }

    public String arrowPath(String arrowString){
        if(arrowString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "PATH ".length()+arrowString.indexOf("PATH ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);
        Pattern pattern=Pattern.compile("([0-9]|,)+");
        Matcher matcher=pattern.matcher(sub);
        String result="";
        while(matcher.find()){
            result+=matcher.group()+" ";
        }
        result=result.substring(0,result.length()-1);
        result=result.replace(',','.');
        Log.w("Arrow path",result);
        return result;
    }

    public static String arrowLabelName(String arrowString){
        if(arrowString==null){
            return null;
        }
        if(arrowString.indexOf("LABEL '")==(-1))
            return null;
        int startInd=0;
        int endInd=0;
        startInd = "LABEL '".length()+arrowString.indexOf("LABEL '",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);

        startInd=0;
        endInd=0;
        startInd = "}".length()+sub.indexOf("}",startInd);
        endInd=sub.indexOf("'",startInd);
        String result=null;
        if(startInd!=endInd) {
            result = sub.substring(startInd, endInd);
        }
        return result;
    }

    public static String arrowLabelCoordinates(String arrowString){
        if(arrowString==null){
            return null;
        }
        if(arrowString.indexOf("LABEL COORDINATES ")==(-1)){
            return null;
        }
        int startInd=0;
        int endInd=0;
        startInd = "LABEL COORDINATES ".length()+arrowString.indexOf("LABEL COORDINATES ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);
        Pattern pattern=Pattern.compile("([0-9]|,)+");
        Matcher matcher=pattern.matcher(sub);
        String result="";
        while(matcher.find()){
            result+=matcher.group()+" ";
        }
        result=result.substring(0,result.length()-1);
        result=result.replace(',','.');
        return result;
    }

    public static String arrowSquiggle(String arrowString){
        if(arrowString==null){
            return null;
        }
        int startInd=0;
        int endInd=0;
        if(arrowString.indexOf("SQUIGGLE COORDINATES")==(-1))
            return null;
        startInd = "SQUIGGLE COORDINATES ".length()+arrowString.indexOf("SQUIGGLE COORDINATES ",startInd);
        endInd=arrowString.indexOf(" ;",startInd);
        String sub = arrowString.substring(startInd, endInd);
        Pattern pattern=Pattern.compile("([0-9]|,)+");
        Matcher matcher=pattern.matcher(sub);
        String result="";
        while(matcher.find()){
            result+=matcher.group()+" ";
        }
        result=result.substring(0,result.length()-1);
        result=result.replace(',','.');
        return result;
    }

    public boolean arrowConnectIsTunnel(String str){
        if(str.equals("TUNNEL {LWI Q} BORDER") || str.equals("TUNNEL {LWI Q}BORDER")){
            return true;
        }
        return false;
    }


    public static float[] boxCoordStringToFloat(String coordStr){
        String[] splitCoord=coordStr.split(" ");
        float[] floatCoord=new float[splitCoord.length];
        for(int i=0;i<splitCoord.length;i++)
            floatCoord[i] = Float.parseFloat(splitCoord[i]);
        //floatCoord[0],floatCoord[3],floatCoord[2],floatCoord[1]
        return floatCoord;
    }

    public static float[] arrowCoordStringToFloat(String coordStr){
        String[] splitCoord=coordStr.split(" ");
        float[] floatCoord=new float[splitCoord.length];
        for(int i=0;i<splitCoord.length;i++)
            floatCoord[i] = Float.parseFloat(splitCoord[i]);
        //floatCoord[0],floatCoord[3],floatCoord[2],floatCoord[1]
        return floatCoord;
    }


    public String createIDL(IDEFEntity entity){
        String result=writeHeader(entity.header);
        result+=writeBody(entity);
        result+=writeFooter();

        return result;
    }

    public String writeBody(IDEFEntity entity){
        IDEFEntity.Diagram diagram=entity.getStartDiagram();
        String result=writeStartDiagram(diagram,entity);

        for(IDEFEntity.Box box : diagram.boxSet){
            if(box.reference!=null) {
                for (IDEFEntity.Diagram inner : entity.diagramList) {
                    if (box.reference == inner) {
                        result += writeBody(entity, box);
                    }
                }
            }
        }

        return result;
    }

    public String writeBody(IDEFEntity entity,IDEFEntity.Box parentBox) {
        IDEFEntity.Diagram diagram = parentBox.reference;
        String result = writeDiagram(diagram,parentBox);

        for (IDEFEntity.Box box : diagram.boxSet) {
            if (box.reference != null) {
                for (IDEFEntity.Diagram inner : entity.diagramList) {
                    if (box.reference == inner) {
                        result += writeBody(entity, box);
                    }
                }
            }
        }

        return result;
    }

    public static String writeDiagram(IDEFEntity.Diagram diagram,IDEFEntity.Box parentBox){
        Log.w("Diagram","---------------------------A"+String.valueOf(diagram.id)+"----------------------------");
        String result="    DIAGRAM GRAPHIC A"+String.valueOf(diagram.id)+" ;\n";
        result+=writeDiagramCreationDate(diagram);
        result+=writeDiagramRevisionDate(diagram);
        result+=writeDiagramTitle(parentBox);
        result+=writeDiagramStatus();
        result+="\n";
        for(IDEFEntity.Box box : diagram.boxSet){
            result+=writeBox(box);
        }
        for(IDEFEntity.Arrow arrow : diagram.arrowSet){
            result+=writeArrow(arrow);
        }

        result+="    ENDDIAGRAM ;\n";
        return result;
    }

    public static String writeStartDiagram(IDEFEntity.Diagram diagram,IDEFEntity entity){
        String result="    DIAGRAM GRAPHIC A-"+String.valueOf(diagram.id)+" ;\n";
        result+=writeDiagramCreationDate(diagram);
        result+=writeDiagramRevisionDate(diagram);
        result+=writeStartDiagramTitle(entity);
        result+=writeDiagramStatus();
        result+="\n";
        for(IDEFEntity.Box box : diagram.boxSet){
            result+=writeBox(box);
        }
        for(IDEFEntity.Arrow arrow : diagram.arrowSet){
            result+=writeStartArrow(arrow);
        }

        result+="    ENDDIAGRAM ;\n";
        return result;
    }

    public static String writeDiagramCreationDate(IDEFEntity.Diagram diagram){
        String text=diagram.getCreationDate();
        if(text==null){
            text="";
        }else if(text.equals("null")){
            text="";
        }
        return "      CREATION DATE "+text+" ;\n";
    }

    public static String writeDiagramRevisionDate(IDEFEntity.Diagram diagram) {
        String text=diagram.getRevisionDate();
        if(text==null){
            text="";
        }else if(text.equals("null")){
            text="";
        }
        return "      REVISION DATE "+text+" ;\n";
    }

    public static String writeStartDiagramTitle(IDEFEntity entity){
        return "      TITLE '"+entity.header.getTitle()+"' ;\n";
    }

    public static String writeDiagramTitle(IDEFEntity.Box box){
        return "      TITLE '"+box.name+"' ;\n";
    }

    public static String writeDiagramStatus(){
        return "      STATUS WORKING ;\n";
    }

    public static String writeBox(IDEFEntity.Box box){
        String result="      BOX "+String.valueOf(box.number)+" ;\n";
        result+="        NAME '{LWI I 4 255 255 255}"+box.name+"' ;\n";
        result+="        BOX COORDINATES "+writeCoord(box)+" ;\n";
        result+="        DETAIL REFERENCE N A"+String.valueOf(box.diagramId)+" ;\n";
        result+="      ENDBOX ;\n";
        return result;
    }

    public static String writeArrow(IDEFEntity.Arrow arrow){
        String result="      ARROWSEG "+String.valueOf(arrow.number)+" ;\n";
        result+=writeArrowSource(arrow);
        result+="        PATH "+writeCoord(arrow)+" ;\n";
        result+=writeArrowLabel(arrow);
        result+=writeArrowSquiggle(arrow);
        result+=writeArrowSink(arrow);
        result+="      ENDSEG ;\n";

        return result;
    }

    public static String writeStartArrow(IDEFEntity.Arrow arrow){
        String result="      ARROWSEG "+String.valueOf(arrow.number)+" ;\n";
        result+=writeArrowStartSource(arrow);
        result+="        PATH "+writeCoord(arrow)+" ;\n";
        result+=writeArrowLabel(arrow);
        result+=writeArrowSquiggle(arrow);
        result+=writeArrowStartSink(arrow);
        result+="      ENDSEG ;\n";

        return result;
    }

    public static String writeArrowSquiggle(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.squiggle!=null){
            result+="        SQUIGGLE COORDINATES "+writeCoord(arrow.squiggle)+" ;\n";
        }
        return result;
    }

    public static String writeArrowLabel(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.label!=null){
            result+="        LABEL '{LWI I 0 255 255 }"+arrow.label.getText()+"' ;\n";
            result+="        LABEL COORDINATES "+writeCoord(arrow.label)+" ;\n";
        }
        return result;
    }

    public static String writeArrowSource(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.source==null || arrow.source.getConnectorSource()==null){
            Log.w(String.valueOf(arrow.number),"Tunnel");
            result+="        SOURCE TUNNEL  {LWI Q}BORDER ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Border){
            Log.w(String.valueOf(arrow.number),"Border");
            String type=arrow.source.getConnectorType();
            String relationNum=String.valueOf(arrow.source.getRelationNum());
            String coordStr="("+String.valueOf(arrow.source.getConnectorPoint().x)+";"+String.valueOf(arrow.source.getConnectorPoint().y)+")";
            coordStr=coordStr.replace(".",",");
            result+="        SOURCE BORDER "+type+relationNum+" "+coordStr+" ;\n";
            //result+="        SOURCE BORDER "+type+"1 "+coordStr+" ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Box){
            Log.w(String.valueOf(arrow.number),"Box");
            String number=String.valueOf(((IDEFEntity.Box) arrow.source.getConnectorSource()).number);
            String type=arrow.source.getConnectorType();
            String relationNum=String.valueOf(arrow.source.getRelationNum());
            result+="        SOURCE BOX "+number+type+relationNum+" ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Branch){
            Log.w(String.valueOf(arrow.number),"Branch");
            IDEFEntity.Arrow parent=((IDEFEntity.Branch) arrow.source.getConnectorSource()).parent;
            result+="        SOURCE BRANCH "+String.valueOf(parent.number)+" ;\n";
        }
        return result;
    }

    public static String writeArrowStartSource(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.source==null || arrow.source.getConnectorSource()==null){
            result+="        SOURCE TUNNEL  {LWI Q}BORDER ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Border){
            String coordStr="("+String.valueOf(arrow.source.getConnectorPoint().x)+";"+String.valueOf(arrow.source.getConnectorPoint().y)+")";
            result+="        SOURCE BORDER ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Box){
            String number=String.valueOf(((IDEFEntity.Box) arrow.source.getConnectorSource()).number);
            String type=arrow.source.getConnectorType();
            String relationNum=String.valueOf(arrow.source.getRelationNum());
            result+="        SOURCE BOX "+number+type+relationNum+" ;\n";
        }else if(arrow.source.getConnectorSource() instanceof IDEFEntity.Branch){
            result+="        SOURCE BRANCH ;\n";
        }
        return result;
    }

    public static String writeArrowSink(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.sink==null || arrow.sink.getConnectorSink()==null){
            Log.w(String.valueOf(arrow.number),"Tunnel");
            result+="        SINK TUNNEL {LWI Q} BORDER ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Border){
            Log.w(String.valueOf(arrow.number),"Border");
            String type=arrow.sink.getConnectorType();
            String relationNum=String.valueOf(arrow.sink.getRelationNum());
            String coordStr="("+String.valueOf(arrow.sink.getConnectorPoint().x)+";"+String.valueOf(arrow.sink.getConnectorPoint().y)+")";
            coordStr=coordStr.replace(".",",");
            result+="        SINK BORDER "+type+relationNum+" "+coordStr+" ;\n";
            //result+="        SINK BORDER "+type+"1 "+coordStr+" ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Box){
            Log.w(String.valueOf(arrow.number),"Box");
            String number=String.valueOf(((IDEFEntity.Box) arrow.sink.getConnectorSink()).number);
            String type=arrow.sink.getConnectorType();
            String relationNum=String.valueOf(arrow.sink.getRelationNum());
            result+="        SINK BOX "+number+type+relationNum+" ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Branch){
            Log.w(String.valueOf(arrow.number),"Branch");
            //IDEFEntity.Arrow parent=((IDEFEntity.Branch) arrow.sink.getConnectorSink()).parent;
            ArrayList<IDEFEntity.Arrow> childList=((IDEFEntity.Branch) arrow.sink.getConnectorSink()).childList;
            String childStr="";
            for(IDEFEntity.Arrow child : childList){
                childStr+=String.valueOf(child.number)+" ";
            }
            childStr=childStr.trim();
            result+="        SINK BRANCH "+childStr+" ;\n";
        }
        return result;
    }

    public static String writeArrowStartSink(IDEFEntity.Arrow arrow){
        String result="";
        if(arrow.sink==null || arrow.sink.getConnectorSink()==null){
            result+="        SINK TUNNEL {LWI Q} BORDER ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Border){
            result+="        SINK BORDER ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Box){
            String number=String.valueOf(((IDEFEntity.Box) arrow.sink.getConnectorSink()).number);
            String type=arrow.sink.getConnectorType();
            String relationNum=String.valueOf(arrow.sink.getRelationNum());
            result+="        SINK BOX "+number+type+relationNum+" ;\n";
        }else if(arrow.sink.getConnectorSink() instanceof IDEFEntity.Branch){
            ArrayList<IDEFEntity.Arrow> childList=((IDEFEntity.Branch) arrow.sink.getConnectorSink()).childList;
            String childStr="";
            for(IDEFEntity.Arrow child : childList){
                childStr+=String.valueOf(child.number)+" ";
            }
            childStr=childStr.trim();
            result+="        SINK BRANCH "+childStr+" ;\n";
        }
        return result;
    }

    public static String writeCoord(IDEFEntity.Box box){
        String result="("+String.valueOf(box.coord.p1.x)+";"+String.valueOf(box.coord.p1.y)+") ("+String.valueOf(box.coord.p2.x)+";"+String.valueOf(box.coord.p2.y)+")";
        result=result.replace(".",",");
        return result;
    }

    public static String writeCoord(IDEFEntity.Arrow arrow){
        String result="";
        for(int i=0;i<arrow.coord.path.size();i++) {
            result+="("+String.valueOf(arrow.coord.path.get(i).x)+";"+String.valueOf(arrow.coord.path.get(i).y)+")";
        }
        result=result.replace(".",",");
        return result;
    }

    public static String writeCoord(IDEFEntity.Arrow.ArrowLabel label){
        String result="";
        result="("+String.valueOf(label.p1.x)+";"+String.valueOf(label.p1.y)+")";
        result=result.replace(".",",");
        return result;
    }

    public static String writeCoord(IDEFEntity.Arrow.ArrowSquiggle squiggle){
        String result="";
        result="("+String.valueOf(squiggle.labelPoint.x)+";"+String.valueOf(squiggle.labelPoint.y)+") ("+String.valueOf(squiggle.arrowPoint.x)+";"+String.valueOf(squiggle.arrowPoint.y)+")";
        result=result.replace(".",",");
        return result;
    }

    public static String writeHeaderTitle(IDEFEntity.Header header){
        String text=header.getTitle();
        if(text==null){
            text="";
        }
        return "  TITLE '"+text+"' ;\n";
    }

    public static String writeHeaderAuthor(IDEFEntity.Header header){
        String text=header.getAuthor();
        if(text==null){
            text="";
        }
        return "  AUTHOR '"+text+"' ;\n";
    }

    public static String writeHeaderCreationDate(IDEFEntity.Header header){
        String text=header.getCreationDate();
        if(text==null){
            text="";
        }else if(text.equals("null")){
            text="";
        }
        return "  CREATION DATE "+text+" ;\n";
    }

    public static String writeHeaderProjectName(IDEFEntity.Header header){
        String text=header.getProjectName();
        if(text==null){
            text="";
        }
        return "  PROJECT NAME '"+text+"' ;\n";
    }

    public static String writeHeaderModelName(IDEFEntity.Header header){
        String text=header.getModelName();
        if(text==null){
            text="";
        }
        return "  MODEL '"+text+"'\n";
    }

    public static String writeHeader(IDEFEntity.Header header){
        String result="KIT ;\n" +
                "  IDL VERSION 1.0.0 ;\n" +
                writeHeaderTitle(header)+
                writeHeaderAuthor(header)+
                writeHeaderCreationDate(header)+
                writeHeaderProjectName(header)+
                "\n" +
                writeHeaderModelName(header)+
                "    {LWI\n" +
                "    F 0 -13 0 0 0 400\n" +
                "      0 0 0 204 3 2 1 34\n" +
                "      100 'Arial'\n" +
                "    F 1 -24 0 0 0 400\n" +
                "      0 0 0 204 3 2 1 34\n" +
                "      190 'Arial'\n" +
                "    F 2 -11 0 0 0 400\n" +
                "      0 0 0 204 3 2 1 34\n" +
                "      80 'Courier New'\n" +
                "    F 3 -8 0 0 0 400\n" +
                "      0 0 0 204 3 2 1 34\n" +
                "      60 'Dialog'\n" +
                "    F 4 -10 0 0 0 400\n" +
                "      0 0 0 204 3 2 1 34\n" +
                "      80 'Dialog'\n" +
                "    D 0 0 0 0 0 1 0 0 2\n" +
                "    G 0 1\n" +
                "    T 'WORKING'\n" +
                "    R 77\n" +
                "    M 0 }\n" +
                "   ;\n" +
                writeHeaderAuthor(header)+
                writeHeaderProjectName(header);
        return result;
    }

    public static String writeFooter(){
        return "  ENDMODEL ;\n" +
                "ENDKIT ;";
    }
}
