package com.schemecreator.denisuser.schemecreator;

import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;


/**
 * Created by Denisuser on 14.12.2014.
 */
public class IDEFEntity {
    public ArrayList<Diagram> diagramList = new ArrayList<Diagram>();
    public Diagram currentDiagram;
    public Diagram startDiagram;
    public Box clickedBox;
    public Header header;
    public static final int N=60;

    public static RectF arrowWorkSpace = new RectF(0.019f, 0.031f, 0.977f, 0.968f);
    public static RectF boxWorkSpace = new RectF(0.025f, 0.042f, 0.971f, 0.96f);

    IDEFEntity(){
        header=new Header();
    }

    static class Header implements Parcelable{
        String title;
        String author;
        String creationDate;
        String projectName;
        String modelName;

        Header(){

        }

        Header(Parcel parcel){
            this.title=parcel.readString();
            this.author=parcel.readString();
            this.creationDate=parcel.readString();
            this.projectName=parcel.readString();
            this.modelName=parcel.readString();
        }

        void setTitle(String title){
            this.title = title;
        }

        String getTitle(){
            return title;
        }

        void setAuthor(String author){
            this.author=author;
        }

        String getAuthor(){
            return author;
        }

        void setCreationDate(String creationDate){
            this.creationDate=creationDate;
        }

        String getCreationDate(){
            return creationDate;
        }

        void setProjectName(String projectName){
            this.projectName=projectName;
        }

        String getProjectName(){
            return projectName;
        }

        void setModelName(String modelName){
            this.modelName=modelName;
        }

        String getModelName(){
            return modelName;
        }

        void newCreationDate(){
            Calendar today=Calendar.getInstance();
            SimpleDateFormat df=new SimpleDateFormat("dd/M/y");
            setCreationDate(df.format(today.getTimeInMillis()).toString());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int flags) {
            parcel.writeString(getTitle());
            parcel.writeString(getAuthor());
            parcel.writeString(getCreationDate());
            parcel.writeString(getProjectName());
            parcel.writeString(getModelName());
        }

        public static final Creator<Header> CREATOR=new Creator<Header>() {
            @Override
            public Header createFromParcel(Parcel parcel) {
                return new Header(parcel);
            }

            @Override
            public Header[] newArray(int i) {
                return new Header[0];
            }
        };
    }

    //Возвращает Box связанный с текущей диаграммой
    public Box getLinkedBox(Diagram diagram){
        Box linkedBox=null;
        if(diagram.getParent()!=null){
            Diagram parent=diagram.getParent();
            for(Box box : diagram.parent.boxSet){
                if(box.reference==diagram){
                    linkedBox=box;
                }
            }
        }
        return linkedBox;
    }

    //Инициализирует дату создания для заголовка модели
    //и начальной диаграммы
    public void initCreationDate(){
        header.newCreationDate();
        getStartDiagram().newCreationDate();
    }

    public String getDiagramTitle(Diagram diagram){
        Box linkedBox=getLinkedBox(diagram);
        if(linkedBox!=null){
            return linkedBox.getBoxText();
        }else{
            return header.getTitle();
        }
    }

    public static ArrayList<PointF> toArrowWorkSpace(PointF sourcePoint, PointF sinkPoint) {
        ArrayList<PointF> pointList = new ArrayList<PointF>();
        PointF p1 = sourcePoint;
        PointF p2 = sinkPoint;
        p1=toArrowWorkSpace(p1);
        p2=toArrowWorkSpace(p2);

        pointList.add(p1);
        pointList.add(p2);
        return pointList;
    }

    public static PointF toArrowWorkSpace(PointF point) {
        PointF p1 = point;
        if (p1.x < arrowWorkSpace.left) {
            p1 = new PointF(arrowWorkSpace.left, p1.y);
        }
        if (p1.y < arrowWorkSpace.top) {
            p1 = new PointF(p1.x, arrowWorkSpace.top);
        }
        if (p1.x > arrowWorkSpace.right) {
            p1 = new PointF(arrowWorkSpace.right, p1.y);
        }
        if (p1.y > arrowWorkSpace.bottom) {
            p1 = new PointF(p1.x, arrowWorkSpace.bottom);
        }

        return p1;
    }

    public static ArrayList<PointF> toBoxWorkspacePoint(PointF p1, PointF p2) {
        ArrayList<PointF> pointList = new ArrayList<PointF>();
        PointF vecToP1 = new PointF(p1.x - p2.x, p1.y - p2.y);
        PointF vecToP2 = new PointF(p2.x - p1.x, p2.y - p1.y);
        if (p1.x < boxWorkSpace.left) {
            p1 = new PointF(boxWorkSpace.left, p1.y);
            p2 = new PointF(p1.x + vecToP2.x, p1.y + vecToP2.y);
        }
        if (p1.y > boxWorkSpace.bottom) {
            p1 = new PointF(p1.x, boxWorkSpace.bottom);
            p2 = new PointF(p1.x + vecToP2.x, p1.y + vecToP2.y);
        }
        if (p2.y < boxWorkSpace.top) {
            p2 = new PointF(p2.x, boxWorkSpace.top);
            p1 = new PointF(p2.x + vecToP1.x, p2.y + vecToP1.y);
        }
        if (p2.x > boxWorkSpace.right) {
            p2 = new PointF(boxWorkSpace.right, p2.y);
            p1 = new PointF(p2.x + vecToP1.x, p2.y + vecToP1.y);
        }
        pointList.add(p1);
        pointList.add(p2);
        return pointList;
    }

    public static ArrayList<PointF> toBoxWorkspacePoint(float x1, float y1, float x2, float y2) {
        PointF p1 = new PointF(x1, y1);
        PointF p2 = new PointF(x2, y2);
        ArrayList<PointF> pointList = new ArrayList<PointF>();
        PointF vecToP1 = new PointF(p1.x - p2.x, p1.y - p2.y);
        PointF vecToP2 = new PointF(p2.x - p1.x, p2.y - p1.y);
        if (p1.x < boxWorkSpace.left) {
            p1 = new PointF(boxWorkSpace.left, p1.y);
            p2 = new PointF(p1.x + vecToP2.x, p1.y + vecToP2.y);
        }
        if (p1.y > boxWorkSpace.bottom) {
            p1 = new PointF(p1.x, boxWorkSpace.bottom);
            p2 = new PointF(p1.x + vecToP2.x, p1.y + vecToP2.y);
        }
        if (p2.y < boxWorkSpace.top) {
            p2 = new PointF(p2.x, boxWorkSpace.top);
            p1 = new PointF(p2.x + vecToP1.x, p2.y + vecToP1.y);
        }
        if (p2.x > boxWorkSpace.right) {
            p2 = new PointF(boxWorkSpace.right, p2.y);
            p1 = new PointF(p2.x + vecToP1.x, p2.y + vecToP1.y);
        }
        pointList.add(p1);
        pointList.add(p2);
        return pointList;
    }

    public boolean setStartDiagramIfNotSet(){
        if(startDiagram==null){
            startDiagram=currentDiagram;
            return true;
        }
        return false;
    }

    public Diagram goToStartDiagram(){
        currentDiagram=startDiagram;
        currentDiagram.newRevisionDate();
        return currentDiagram;
    }

    public boolean goToLevelDownDiagram(){
        if(getCurrentDiagram().selectedObject !=null) {
            if (getCurrentDiagram().selectedObject instanceof Box) {
                Box box = (Box) getCurrentDiagram().selectedObject;
                if (box.reference != null) {
                    Diagram child = box.reference;
                    if (child.getParent() == null)
                        child.setParent(getCurrentDiagram());
                    setCurrentDiagram(child);
                    child.newRevisionDate();
                    clickedBox=box;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean goToLevelUpDiagram(){
        Diagram parent=getCurrentDiagram().getParent();
        if(parent!=null){
            setCurrentDiagram(parent);
            parent.newRevisionDate();
            return true;
        }
        return false;
    }

    public void setCurrentDiagram(Diagram diagram){
        currentDiagram=diagram;
    }

    public Diagram getCurrentDiagram(){
        return currentDiagram;
    }

    public Diagram getStartDiagram(){
        return startDiagram;
    }

    public void addDiagram(Diagram diagram){
        diagramList.add(diagram);
    }

    public Diagram getDiagramById(int id){
        for (Diagram diagram : diagramList) {
            if(diagram!=getStartDiagram()) {
                if (diagram.id == id) {
                    return diagram;
                }
            }
        }
        return null;
    }

    //Устанавливает текст ArrowLabel, текущей Arrow
    public void setArrowLabelText(Arrow arrow,String text){
        arrow.label.setText(text);
        renameArrowStreamLabel(arrow,text);
    }

    //Переименновывание Label, относящейся к текущей Arrow
    public void renameArrowStreamLabel(Arrow arrow,String newText){
        Arrow curArrow=arrow;
        Diagram saveDiagram=getCurrentDiagram();
        int i=0;
        while(i<100){
            curArrow=goBackByArrow(curArrow);
            if(curArrow==null){
                break;
            }
            if(curArrow.label!=null){
                curArrow.label=new Arrow.ArrowLabel(curArrow.label,newText);
            }/*else{
                curArrow.addDefaultLabel(newText);
            }*/
            i++;
        }
        curArrow=arrow;
        currentDiagram=saveDiagram;
        i=0;
        while(i<100){
            curArrow=goForwardByArrow(curArrow);
            if(curArrow==null){
                break;
            }
            if(curArrow.label!=null){
                curArrow.label=new Arrow.ArrowLabel(curArrow.label,newText);
            }/*else{
                curArrow.addDefaultLabel(newText);
            }*/
            i++;
        }
        currentDiagram=saveDiagram;
    }

    //Возвращает Arrow связанную с текущей
    public Arrow goBackByArrow(Arrow arrow){
        if(arrow.source!=null){
            if(arrow.source.getConnectorSource() instanceof Border){
                return getExternalRelatedArrow(arrow.source);
            }else if(arrow.source.getConnectorSource() instanceof Box){
                return getInternalRelatedArrow(arrow.source);
            }
        }
        return null;
    }

    //Возвращает Arrow связанную с текущей
    public Arrow goForwardByArrow(Arrow arrow){
        if(arrow.sink!=null){
            if(arrow.sink.getConnectorSink() instanceof Border){
                return getExternalRelatedArrow(arrow.sink);
            }else if(arrow.sink.getConnectorSink() instanceof Box){
                return getInternalRelatedArrow(arrow.sink);
            }
        }
        return null;
    }

    //При смещении ArrowSource, связанная с ним внешняя Arrow также смещается
    //в точке ArrowSink
    public Arrow getInternalRelatedArrow(Arrow.ArrowSource source){
        if(source.getConnectorSource() instanceof Box) {
            Box box=(Box)source.getConnectorSource();
            if(box.reference!=null) {
                Diagram childDiagram=box.reference;
                Border border = getInternalRelatedObj(source);
                if(border!=null){
                    for(Arrow arrow : childDiagram.arrowSet){
                        if(arrow.sink.getConnectorSink()==border){
                            if(arrow.sink.getConnectorType().equals(source.getConnectorType())) {
                                if (arrow.sink.getRelationNum() == source.getRelationNum()) {
                                    currentDiagram=childDiagram;
                                    return arrow;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //При смещении ArrowSink, связанная с ним внешняя Arrow также смещается
    //в точке ArrowSource
    public Arrow getInternalRelatedArrow(Arrow.ArrowSink sink){
        if(sink.getConnectorSink() instanceof Box) {
            Box box=(Box)sink.getConnectorSink();
            if(box.reference!=null) {
                Diagram childDiagram=box.reference;
                Border border = getInternalRelatedObj(sink);
                if(border!=null){
                    for(Arrow arrow : childDiagram.arrowSet){
                        if(arrow.source.getConnectorSource()==border){
                            if(arrow.source.getConnectorType().equals(sink.getConnectorType())) {
                                if (arrow.source.getRelationNum() == sink.getRelationNum()) {
                                    currentDiagram=childDiagram;
                                    return arrow;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //При смещении ArrowSink, связанная с ним внешняя Arrow также смещается
    //в точке ArrowSource
    public Arrow getExternalRelatedArrow(Arrow.ArrowSink sink){
        if(getCurrentDiagram().getParent()!=null) {
            if (sink.getConnectorSink() instanceof Border) {
                Diagram parentDiagram = getCurrentDiagram().getParent();
                Box box = getExternalRelatedObj(sink);
                if (box != null) {
                    for (Arrow arrow : parentDiagram.arrowSet) {
                        if (arrow.source.getConnectorSource() == box) {
                            if (arrow.source.getConnectorType().equals(sink.getConnectorType())) {
                                if (arrow.source.getRelationNum() == sink.getRelationNum()) {
                                    currentDiagram=parentDiagram;
                                    return arrow;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //При смещении ArrowSource, связанная с ним внешняя Arrow также смещается
    //в точке ArrowSink
    public Arrow getExternalRelatedArrow(Arrow.ArrowSource source){
        if(getCurrentDiagram().getParent()!=null) {
            if (source.getConnectorSource() instanceof Border) {
                Diagram parentDiagram = getCurrentDiagram().getParent();
                Box box = getExternalRelatedObj(source);
                if (box != null) {
                    for (Arrow arrow : parentDiagram.arrowSet) {
                        if (arrow.sink.getConnectorSink() == box) {
                            if (arrow.sink.getConnectorType().equals(source.getConnectorType())) {
                                if (arrow.sink.getRelationNum() == source.getRelationNum()) {
                                    currentDiagram=parentDiagram;
                                    return arrow;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
    public Box getExternalRelatedObj(Object obj){
        if(obj instanceof Arrow.ArrowSource){
            return getExternalRelatedObj((Arrow.ArrowSource)obj);
        }else if(obj instanceof Arrow.ArrowSink){
            return getExternalRelatedObj((Arrow.ArrowSink)obj);
        }
        return null;
    }
    */

    public Box getExternalRelatedObj(Arrow.ArrowSource source){
        if(source.getConnectorSource()!=null){
            if(getCurrentDiagram().getParent()!=null) {
                Diagram parentDiagram = getCurrentDiagram().getParent();
                if (source.getConnectorSource() instanceof Border) {
                    for (Box box : parentDiagram.boxSet) {
                        if (box.reference != null) {
                            if (box.reference == getCurrentDiagram()) {
                                return box;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public Box getExternalRelatedObj(Arrow.ArrowSink sink){
        if(sink.getConnectorSink()!=null){
            if(getCurrentDiagram().getParent()!=null) {
                Diagram parentDiagram = getCurrentDiagram().getParent();
                if (sink.getConnectorSink() instanceof Box) {
                    for (Box box : parentDiagram.boxSet) {
                        if (box.reference != null) {
                            if (box.reference == getCurrentDiagram()) {
                                return box;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /*
    public Border getInternalRelatedObj(Object obj){
        if(obj instanceof Arrow.ArrowSource){
            return getInternalRelatedObj((Arrow.ArrowSource)obj);
        }else if(obj instanceof Arrow.ArrowSink){
            return getInternalRelatedObj((Arrow.ArrowSink)obj);
        }
        return null;
    }
    */

    public Border getInternalRelatedObj(Arrow.ArrowSource source){
        if(source.getConnectorSource()!=null){
            if(source.getConnectorSource() instanceof Box){
                Diagram childDiagram=((Box) source.getConnectorSource()).reference;
                return childDiagram.getBorderByType("O");
            }
        }
        return null;
    }

    public Border getInternalRelatedObj(Arrow.ArrowSink sink){
        if(sink.getConnectorSink()!=null){
            if(sink.getConnectorSink() instanceof Box){
                Diagram childDiagram=((Box) sink.getConnectorSink()).reference;
                if(sink.getConnectorType().equals("I")) {
                    return childDiagram.getBorderByType("I");
                }else if(sink.getConnectorType().equals("C")){
                    return childDiagram.getBorderByType("C");
                }else if(sink.getConnectorType().equals("M")){
                    return childDiagram.getBorderByType("M");
                }
            }
        }
        return null;
    }

    //Инициализирует объекты при создании документа
    public void initAllReference(){
        for(Diagram diagram : diagramList){
            for(Box box : diagram.boxSet){
                Diagram curDiagram=getDiagramById(box.diagramId);
                if(curDiagram!=null)
                    box.reference=curDiagram;
            }

            for (Arrow arrow : diagram.arrowSet) {

                //Init arrow connector point
                if(arrow.source.getConnectorPoint()==null){
                    arrow.source.setConnectorPoint();
                }

                if(arrow.sink.getConnectorPoint()==null){
                    arrow.sink.setConnectorPoint();
                }

                if(arrow.sourceString==null)
                    continue;
                String connectObj=IDLParser.arrowConnectObj(arrow.sourceString);
                String connectContent=IDLParser.arrowConnectContent(arrow.sourceString);

                if(connectObj.equals("BOX")) {
                    //if (parse.length == 2) {
                    if (connectContent!=null) {
                        for (Box box : diagram.boxSet) {
                            if (box.number == Integer.parseInt(IDLParser.arrowConnectObjName(connectContent))) {
                                arrow.source.setConnectorSource(box);
                                String type=IDLParser.arrowConnectRelationType(connectContent);
                                arrow.source.setConnectorType(type);
                                String relationNum=IDLParser.arrowConnectRelationNumber(connectContent);
                                if(relationNum!=null) {
                                    arrow.source.setRelationNum(Integer.valueOf(relationNum));
                                }

                                /*
                                if(diagram==startDiagram){
                                    arrow.sink.setConnectorSink(diagram.rightBorder);
                                }
                                */
                                break;
                            }
                        }

                    }
                }else if(connectObj.equals("BORDER")){
                    if(diagram!=startDiagram) {
                        String type = IDLParser.arrowConnectRelationType(connectContent);
                        arrow.source.setConnectorType(type);

                        if (type.equals("I")) {
                            arrow.source.setConnectorSource(diagram.leftBorder);
                        } else if (type.equals("C")) {
                            arrow.source.setConnectorSource(diagram.topBorder);
                        } else if (type.equals("M")) {
                            arrow.source.setConnectorSource(diagram.bottomBorder);
                        }
                        String relationNum=IDLParser.arrowConnectRelationNumber(connectContent);
                        if(relationNum!=null) {
                            arrow.source.setRelationNum(Integer.valueOf(relationNum));
                        }
                    }else{
                        //init border by source point
                        PointF point=arrow.source.getConnectorPoint();
                        String type=MathCalc.getBorderTypeByPoint(point);
                        arrow.source.setConnectorType(type);
                        arrow.source.setConnectorSource(getCurrentDiagram().getBorderByType(type));
                    }
                }else if(connectObj.equals("BRANCH")){
                    int arrowNumber=IDLParser.arrowSourceBranch(arrow.sourceString);
                    Branch branch=getCurrentDiagram().getBranchByArrowNumber(arrowNumber);
                    if(branch==null){
                        branch=new Branch(arrowNumber);
                        getCurrentDiagram().addBranch(branch);
                    }
                    arrow.source.setConnectorSource(branch);
                }else if(connectObj.equals("JOIN")){
                    Join join=getCurrentDiagram().getJoinByArrowNumber(arrow.number);
                    if(join==null){
                        join=new Join(arrow.number);
                        getCurrentDiagram().addJoin(join);
                    }
                    if(join.child==null){
                        join.setChild(arrow);
                    }
                    if(join.parentList.size()==0){
                        ArrayList<Integer> numberList=IDLParser.arrowSourceJoin(connectContent);
                        while(numberList.size()!=0){
                            Arrow parentArrow=getCurrentDiagram().getArrowByNumber(numberList.remove(0));
                            if(parentArrow!=null){
                                join.addParent(parentArrow);
                            }
                        }
                    }
                    arrow.source.setConnectorSource(join);
                }


                if(arrow.sinkString==null)
                    continue;
                connectObj=IDLParser.arrowConnectObj(arrow.sinkString);
                connectContent=IDLParser.arrowConnectContent(arrow.sinkString);

                if(connectObj.equals("BOX")) {
                    //if (parse.length == 2) {
                    if(connectContent!=null){
                        for (Box box : diagram.boxSet) {

                            if (box.number == Integer.parseInt(IDLParser.arrowConnectObjName(connectContent))) {
                                arrow.sink.setConnectorSink(box);
                                String type=IDLParser.arrowConnectRelationType(connectContent);
                                arrow.sink.setConnectorType(type);
                                String relationNum=IDLParser.arrowConnectRelationNumber(connectContent);
                                if(relationNum!=null) {
                                    arrow.sink.setRelationNum(Integer.valueOf(relationNum));
                                }

                                break;
                            }
                        }

                    }

                }else if(connectObj.equals("BORDER")){
                    arrow.sink.setConnectorType("O");
                    arrow.sink.setConnectorSink(diagram.rightBorder);
                    String relationNum=IDLParser.arrowConnectRelationNumber(connectContent);
                    if(relationNum!=null) {
                        arrow.sink.setRelationNum(Integer.valueOf(relationNum));
                    }

                }else if(connectObj.equals("BRANCH")){
                    Branch branch=getCurrentDiagram().getBranchByArrowNumber(arrow.number);
                    if(branch==null){
                        branch=new Branch(arrow.number);
                        getCurrentDiagram().addBranch(branch);
                    }
                    if(branch.parent==null){
                        branch.setParent(arrow);
                    }
                    if(branch.childList.size()==0){
                        ArrayList<Integer> numberList=IDLParser.arrowSinkBranch(connectContent);
                        while(numberList.size()!=0){
                            Arrow childArrow=getCurrentDiagram().getArrowByNumber(numberList.remove(0));
                            if(childArrow!=null){
                                branch.addChild(childArrow);
                            }
                        }
                    }
                    arrow.sink.setConnectorSink(branch);
                }else if(connectObj.equals("JOIN")){
                    int arrowNumber=IDLParser.arrowSinkJoin(arrow.sinkString);
                    Join join=getCurrentDiagram().getJoinByArrowNumber(arrowNumber);
                    if(join==null){
                        join=new Join(arrowNumber);
                        getCurrentDiagram().addJoin(join);
                    }
                    arrow.sink.setConnectorSink(join);
                }

            }
        }
    }


    //Рекурсивное переименование диаграмм
    //в параметре будет clickedBox
    public void renameDiagrams(Box curBox){
        if(curBox.reference!=null) {
            Diagram diagram = curBox.reference;
            final PointF zero = new PointF(0, 0);
            ArrayList<Box> boxList = new ArrayList<Box>();
            Comparator<Box> boxComparator = new Comparator<Box>() {
                @Override
                public int compare(Box box1, Box box2) {
                    PointF boxPoint1 = new PointF(box1.coord.p1.x, box1.coord.p2.y);
                    PointF boxPoint2 = new PointF(box2.coord.p1.x, box2.coord.p2.y);
                    double dis1 = MathCalc.getDistance(zero, boxPoint1);
                    double dis2 = MathCalc.getDistance(zero, boxPoint2);
                    if (dis1 < dis2) {
                        return -1;
                    } else if (dis1 > dis2) {
                        return 1;
                    }
                    return 0;
                }
            };

            for (Box box : diagram.boxSet) {
                boxList.add(box);
            }

            Collections.sort(boxList, boxComparator);

            int i = 0;
            for (Box box : boxList) {
                int id = diagram.id * 10 + i;
                //xxx
                i++;
                renameDiagram(box, id);
                renameDiagrams(box);
            }
        }
    }

    public void renameDiagram(Box curBox,int id){
        curBox.diagramId=id;
        if(curBox.reference!=null) {
            Diagram diagram=curBox.reference;
            diagram.id=id;
        }
    }


    public void removeDiagram(Diagram diagram){
        diagramList.remove(diagram);
    }



    public static class MathCalc {

        //Возвращает расстояние между двумя точками
        public static double getDistance(PointF p1,PointF p2){
            return Math.sqrt(((p2.x-p1.x)*(p2.x-p1.x))+((p2.y-p1.y)*(p2.y-p1.y)));
        }

        public static char getOrientationByDirection(String direction){
            if(direction.equals("left") || direction.equals("right")){
                return 'H';
            }else if(direction.equals("up") || direction.equals("down")){
                return 'V';
            }
            return ' ';
        }

        //Проверяет правильно ли построен путь
        public static boolean isCorrectPath(ArrayList<PointF> path){
            PointF p1=null;
            PointF p2=null;
            String oldDirection=null;
            String curDirection=null;
            if(path!=null) {
                if(path.size()>1) {
                    for (int i = 0; i < (path.size() - 1); i++) {
                        p1 = path.get(i);
                        p2 = path.get(i + 1);
                        curDirection = getDirectionByPoints(p1, p2);

                        if (curDirection == null) {
                            return false;
                        } else if (oldDirection != null) {
                            if (getOrientationByDirection(curDirection) == getOrientationByDirection(oldDirection)) {
                                return false;
                            }
                        }

                        oldDirection = new String(curDirection);
                    }
                }
            }
            return true;
        }

        //Метод создает дополнительную точку в указаном направлении
        //Будет применён при построении пути от branch к box и т.д.
        public static PointF getExtraPoint(PointF start,String direction){
            float k=0.05f;
            if(direction.equals("left")){
                return new PointF(start.x-k,start.y);
            }else if(direction.equals("up")){
                return new PointF(start.x,start.y-k);
            }else if(direction.equals("right")){
                return new PointF(start.x+k,start.y);
            }else if(direction.equals("down")){
                return new PointF(start.x,start.y+k);
            }
            return null;
        }

        //Возвращает ближайшее направление от точки from к точке to
        //в зависимости от выбранной ориентации
        public static String getClosestDirection(PointF from,PointF to,char orientation){
            if(orientation=='V'){
                return getClosestLeftRightDirection(from,to);
            }else if(orientation=='H'){
                return getClosestUpDownDirection(from,to);
            }
            return null;
        }

        //Возвращает ближайшее направление от точки from к точке to
        //return "up" "down"
        public static String getClosestUpDownDirection(PointF from,PointF to){
            if(from.y<to.y){
                return "down";
            }else{
                return "up";
            }
        }


        //Возвращает ближайшее направление от точки from к точке to
        //return "left" "right"
        public static String getClosestLeftRightDirection(PointF from,PointF to){
            if(from.x<to.x){
                return "right";
            }else{
                return "left";
            }
        }


        //Метод возвращает точки пути, в обратном порядке
        public static ArrayList<PointF> invertPath(ArrayList<PointF> path){
            ArrayList<PointF> invertPath=new ArrayList<PointF>();
            for(int i=path.size();i>0;i--){
                invertPath.add(path.get(i-1));
            }
            return invertPath;
        }

        //Возвращает список из двух точек,
        //которые расположены ближе остальных к точке касания
        public static HashMap<Integer,ArrayList<PointF>> getCurTouchPointMap(Arrow arrow,PointF touchPoint){
            ArrayList<PointF> path = arrow.coord.path;

            int j = 0;
            double distance = -1;
            int ind = 0;
            PointF curPoint1 = new PointF();
            PointF curPoint2 = new PointF();
            for (int i = 0; i < path.size() - 1; i++) {
                j = i + 1;

                PointF p1 = path.get(i);
                PointF p2 = path.get(j);
                double d = MathCalc.minDistance(touchPoint, p1, p2);
                if (distance == (-1) || distance > d) {
                    distance = d;
                    curPoint1 = p1;
                    curPoint2 = p2;
                    ind = i;
                }
            }

            //Индекс точки curPoint1
            final int index=ind;
            final ArrayList<PointF> pointList=new ArrayList<PointF>();
            pointList.add(curPoint1);
            pointList.add(curPoint2);

            return new HashMap<Integer,ArrayList<PointF>>(){{
                    put(index,pointList);
            }};
        }

        public static boolean pointInBox(PointF p, Box box) {
            float left = box.coord.p1.x;
            float top = box.coord.p2.y;
            float right = box.coord.p2.x;
            float bottom = box.coord.p1.y;
            if (left < p.x && p.x < right) {
                if (top < p.y && p.y < bottom) {
                    return true;
                }
            }
            return false;
        }

        public static boolean pointInBoxRangeY(PointF point, Box box) {
            float top = box.coord.p2.y;
            float bottom = box.coord.p1.y;

            if (top < point.y && point.y < bottom) {
                return true;
            }
            return false;
        }

        public static boolean pointInBoxRangeX(PointF point, Box box) {
            float right = box.coord.p2.x;
            float left = box.coord.p1.x;

            if (left < point.x && point.x < right) {
                return true;
            }
            return false;
        }


        public static ArrayList<String> nextDirections(String direction) {
            if (direction == "left" || direction == "right") {
                ArrayList<String> directionList = new ArrayList<String>();
                directionList.add("up");
                directionList.add("down");
                return directionList;
            } else {
                ArrayList<String> directionList = new ArrayList<String>();
                directionList.add("left");
                directionList.add("right");
                return directionList;
            }
        }

        public static String getDirectionByBoxConnectorType(String type){
            if(type.equals("I")){
                return "left";
            }
            if(type.equals("C")){
                return "up";
            }
            if(type.equals("O")){
                return "right";
            }
            if(type.equals("M")){
                return "down";
            }
            return null;
        }

        public static String getDirectionByPoints(PointF startPoint,PointF endPoint){
            if(startPoint.x==endPoint.x){
                if(startPoint.y>endPoint.y){
                    return "up";
                }else if(startPoint.y<endPoint.y){
                    return "down";
                }
            }else if(startPoint.y==endPoint.y){
                if(startPoint.x>endPoint.x){
                    return "left";
                }else if(startPoint.x<endPoint.x){
                    return "right";
                }
            }
            return null;
        }

        public static String getDirectionByBorder(Border border){
            if(border.type.equals("I")){
                return "right";
            }else if(border.type.equals("C")){
                return "down";
            }else if(border.type.equals("O")){
                return "left";
            }else if(border.type.equals("M")){
                return "up";
            }
            return null;
        }


        //Направления при построении пути к Branch
        public static ArrayList<String> getDirectionsByBranch(Branch branch){

            if(branch.parent!=null){
                Arrow arrow=(Arrow)branch.parent;
                PointF sinkPoint=arrow.coord.path.get(arrow.coord.path.size()-1);
                PointF prevSinkPoint=arrow.coord.path.get(arrow.coord.path.size()-2);
                char orientation=getOrientationByPoints(sinkPoint,prevSinkPoint);

                if(orientation!=' '){
                    ArrayList<String> result = new ArrayList<String>();

                    if (orientation == 'V') {
                        result.add("left");
                        result.add("right");
                    } else if (orientation == 'H') {
                        result.add("up");
                        result.add("down");
                    }

                    return result;
                }
            }
            return null;
        }

        //Направления при построении пути к Join
        public static ArrayList<String> getDirectionsByJoin(Join join){

            if(join.child!=null){
                Arrow arrow=(Arrow)join.child;
                PointF sourcePoint=arrow.coord.path.get(0);
                PointF nextSourcePoint=arrow.coord.path.get(1);
                char orientation=getOrientationByPoints(sourcePoint,nextSourcePoint);

                if(orientation!=' '){
                    ArrayList<String> result = new ArrayList<String>();

                    if (orientation == 'V') {
                        result.add("left");
                        result.add("right");
                    } else if (orientation == 'H') {
                        result.add("up");
                        result.add("down");
                    }

                    return result;
                }
            }
            return null;
        }

        //Возвращает угол между двумя векторами в градусах
        public static double getAngleBetweenVectors(PointF v1,PointF v2){
            //float rad=(v1.x*v2.x+v1.y*v2.y)/((float)Math.sqrt((v1.x-v2.x)*(v1.x-v2.x)+(v1.y-v2.y)*(v1.y-v2.y)));
            double rad=Math.acos((v1.x * v2.x + v1.y * v2.y) / (Math.sqrt(v1.x * v1.x + v1.y * v1.y) * Math.sqrt(v2.x * v2.x + v2.y * v2.y)));
            //return Math.cos((double)rad);
            double angle=rad*(180/3.14);

            return angle;
        }


        public static String getBorderTypeByPoint(PointF point){
            if(point.x<=arrowWorkSpace.left){
                return "I";
            }else if(point.x>=arrowWorkSpace.right){
                return "O";
            }

            if(point.y<=arrowWorkSpace.top){
                return "C";
            }else if(point.y>=arrowWorkSpace.bottom){
                return "M";
            }

            return null;
        }

        //Создает точку в указаном направлении, недалеко от текущей точки
        public static PointF getClosestPointByDirection(PointF currentPoint, String direction) {
            float k=0.05f;
            if (direction.equals("left")) {
                return new PointF(currentPoint.x-k,currentPoint.y);
            } else if (direction.equals("right")) {
                return new PointF(currentPoint.x+k,currentPoint.y);
            } else if (direction.equals("up")) {
                return new PointF(currentPoint.x,currentPoint.y-k);
            } else if (direction.equals("down")) {
                return new PointF(currentPoint.x,currentPoint.y+k);
            }
            return null;
        }

        public static PointF getPointByDirection(RectF plotArea, PointF currentPoint, String direction) {
            if (direction.equals("left")) {
                return new PointF(plotArea.left, currentPoint.y);
            } else if (direction.equals("right")) {
                return new PointF(plotArea.right, currentPoint.y);
            } else if (direction.equals("up")) {
                return new PointF(currentPoint.x, plotArea.top);
            } else if (direction.equals("down")) {
                return new PointF(currentPoint.x, plotArea.bottom);
            }
            return null;
        }

        public static PointF getNormVecByDirection(String direction) {
            if (direction.equals("left")) {
                return new PointF(-1, 0);
            } else if (direction.equals("up")) {
                return new PointF(0, -1);
            } else if (direction.equals("right")) {
                return new PointF(1, 0);
            } else if (direction.equals("down")) {
                return new PointF(0, 1);
            }
            return null;
        }

        public static String getInverseDirection(String direction){
            if(direction.equals("left")){
                return "right";
            }

            if(direction.equals("up")){
                return "down";
            }

            if(direction.equals("right")){
                return "left";
            }

            if(direction.equals("down")){
                return "up";
            }

            return null;
        }

        public static char getOrientationByPoints(PointF point1,PointF point2){
            if(point1.x != point2.x || point1.y != point2.y) {
                if (point1.x == point2.x) {
                    return 'V';
                } else if (point1.y == point2.y) {
                    return 'H';
                }
            }
            return ' ';
        }

        public static char getApproxOrientationByDirections(String dir1,String dir2){
            if((dir1.equals("left") && dir2.equals("right")) || (dir1.equals("right") && dir2.equals("left"))){
                return 'H';
            }else if((dir1.equals("up") && dir2.equals("down")) || (dir1.equals("down") && dir2.equals("up"))){
                return 'V';
            }
            return ' ';
        }

        public static boolean isOneDirection(PointF v1, PointF v2) {
            double acos = (v1.x * v2.x + v1.y * v2.y) / (Math.sqrt(v1.x * v1.x + v1.y * v1.y) * Math.sqrt(v2.x * v2.x + v2.y * v2.y));
            //double res=(double)Math.round(acos);
            //return res==1;
            return 0.9999 < acos && acos < 1.0001;
        }

        public static boolean vectorsIntersection(PointF p1, PointF p2, PointF p3, PointF p4) {
            float ax1 = p1.x;
            float ay1 = p1.y;
            float ax2 = p2.x;
            float ay2 = p2.y;
            float bx1 = p3.x;
            float by1 = p3.y;
            float bx2 = p4.x;
            float by2 = p4.y;

            float v1 = (bx2 - bx1) * (ay1 - by1) - (by2 - by1) * (ax1 - bx1);
            float v2 = (bx2 - bx1) * (ay2 - by1) - (by2 - by1) * (ax2 - bx1);
            float v3 = (ax2 - ax1) * (by1 - ay1) - (ay2 - ay1) * (bx1 - ax1);
            float v4 = (ax2 - ax1) * (by2 - ay1) - (ay2 - ay1) * (bx2 - ax1);
            return (v1 * v2 <= 0) && (v3 * v4 <= 0);
        }

        public static PointF vectorsIntersectionPoint(PointF p1, PointF p2, PointF np1, PointF np2) {
            PointF dir1 = new PointF(p2.x - p1.x, p2.y - p1.y);
            PointF dir2 = new PointF(np2.x - np1.x, np2.y - np1.y);

            float a1 = -dir1.y;
            float b1 = +dir1.x;
            float d1 = -(a1 * p1.x + b1 * p1.y);

            float a2 = -dir2.y;
            float b2 = +dir2.x;
            float d2 = -(a2 * np1.x + b2 * np1.y);

            float seg1_line2_start = a2 * p1.x + b2 * p1.y + d2;
            float seg1_line2_end = a2 * p2.x + b2 * p2.y + d2;

            float seg2_line1_start = a1 * np1.x + b1 * np1.y + d1;
            float seg2_line1_end = a1 * np2.x + b1 * np2.y + d1;

            if (seg1_line2_start * seg1_line2_end >= 0 || seg2_line1_start * seg2_line1_end >= 0)
                return null;

            float u = seg1_line2_start / (seg1_line2_start - seg1_line2_end);
            PointF intersection = new PointF(p1.x + u * dir1.x, p1.y + u * dir1.y);
            return intersection;
        }

        public static ArrayList<PointF> boxIntersection(PointF point1, PointF point2, Box box) {

            PointF p1 = box.coord.p1;
            PointF p2 = box.coord.p2;
            float y1 = p1.y;
            float y2 = p2.y;
            PointF np1 = new PointF(p1.x, y2);
            PointF np2 = new PointF(p2.x, y1);

            if (MathCalc.vectorsIntersection(point1, point2, p1, p2) || MathCalc.vectorsIntersection(point1, point2, np1, np2)) {
                ArrayList<PointF> pointList = new ArrayList<PointF>();
                pointList.add(p1);
                pointList.add(p2);
                pointList.add(np1);
                pointList.add(np2);
                return pointList;
            } else {
                return null;
            }
        }

        public static ArrayList<PointF> boxIntersectionPoints(PointF point1,PointF point2,Box box){

            ArrayList<PointF> pointList=new ArrayList<PointF>();
            PointF p1 = box.coord.p1;
            PointF p2 = box.coord.p2;
            float y1 = p1.y;
            float y2 = p2.y;
            PointF np1 = new PointF(p1.x, y2);
            PointF np2 = new PointF(p2.x, y1);

            PointF intersecPoint=vectorsIntersectionPoint(p1,np1,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(np1,p2,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(p2,np2,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(np2,p1,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            return pointList;
        }

        public static ArrayList<PointF> labelIntersectionPoints(PointF point1,PointF point2,Arrow.ArrowLabel label){

            ArrayList<PointF> pointList=new ArrayList<PointF>();
            PointF p1=new PointF();
            PointF p2=new PointF();
            if(label.getContainer()==null) {
                p1 = label.getPoint1();
                p2 = label.getPoint2();
            }else{
                RectF container=label.getContainer();
                p1 = new PointF(container.left, container.bottom);
                p2 = new PointF(container.right, container.top);
            }
            float y1 = p1.y;
            float y2 = p2.y;
            PointF np1 = new PointF(p1.x, y2);
            PointF np2 = new PointF(p2.x, y1);

            PointF intersecPoint=vectorsIntersectionPoint(p1,np1,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(np1,p2,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(p2,np2,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            intersecPoint=null;
            intersecPoint=vectorsIntersectionPoint(np2,p1,point1,point2);
            if(intersecPoint!=null){
                pointList.add(intersecPoint);
            }
            return pointList;
        }

        public static boolean labelIntersection(PointF point1,PointF point2,Arrow.ArrowLabel label){

            PointF p1=new PointF();
            PointF p2=new PointF();
            PointF np1=new PointF();
            PointF np2=new PointF();
            if(label.getContainer()==null) {
                p1 = label.getPoint1();
                p2 = label.getPoint2();
                float y1 = p1.y;
                float y2 = p2.y;
                np1 = new PointF(p1.x, y2);
                np2 = new PointF(p2.x, y1);
            }else{
                p1 = new PointF(label.getContainer().left, label.getContainer().bottom);
                p2 = new PointF(label.getContainer().right, label.getContainer().top);
                float y1 = p1.y;
                float y2 = p2.y;
                np1 = new PointF(p1.x, y2);
                np2 = new PointF(p2.x, y1);
            }

            if (MathCalc.vectorsIntersection(point1, point2, p1, p2) || MathCalc.vectorsIntersection(point1, point2, np1, np2)) {
                return true;
            }
            return false;
        }

        public static RectF buildPlotArea(PointF p1, PointF p2) {
            RectF plotArea = new RectF();
            if (p1.x < p2.x) {
                plotArea.left = p1.x;
                plotArea.right = p2.x;
            } else {
                plotArea.left = p2.x;
                plotArea.right = p1.x;
            }
            if (p1.y < p2.y) {
                plotArea.top = p1.y;
                plotArea.bottom = p2.y;
            } else {
                plotArea.top = p2.y;
                plotArea.bottom = p1.y;
            }

            return plotArea;
        }

        public static RectF buildPlotAreaMore(PointF p1, PointF p2) {
            RectF plotArea = new RectF();
            float k = 0.05f;
            if (p1.x < p2.x) {
                plotArea.left = p1.x - k;
                plotArea.right = p2.x + k;
            } else {
                plotArea.left = p2.x - k;
                plotArea.right = p1.x + k;
            }
            if (p1.y < p2.y) {
                plotArea.top = p1.y - k;
                plotArea.bottom = p2.y + k;
            } else {
                plotArea.top = p2.y - k;
                plotArea.bottom = p1.y + k;
            }

            return plotArea;
        }

        //Проекция точки на Box
        //метод может быть исползьован при построении пути
        public static PointF projPointOnBox(PointF point,Box box){
            ArrayList<PointF> pointList=box.coord.getPointList();
            PointF projPoint=null;
            PointF p1=pointList.remove(0);
            PointF p2=pointList.remove(0);
            if(!pointInBox(point,box)) {
                if (p1.x < point.x && point.x < p2.x) {
                    projPoint=new PointF(point.x,point.y);
                    if(point.y>p1.y){
                        projPoint.y=p1.y;
                    }else if(point.y<p2.y){
                        point.y=p2.y;
                    }
                }else if(p2.y < point.y && point.y < p1.y){
                    projPoint=new PointF(point.x,point.y);
                    if(point.x<p1.x){
                        projPoint.x=p1.x;
                    }else if(point.x>p2.x){
                        projPoint.x=p2.x;
                    }
                }
            }
            return projPoint;
        }


        //Проекция точки на plotArea
        //метод может быть исползьован при построении пути
        public static PointF projPointOnPlotArea(PointF point,RectF plotArea){
            PointF projPoint=null;

            //Если точка вне области plotArea
            if(!pointInPlotArea(point,plotArea)) {
                if (plotArea.left < point.x && point.x < plotArea.right) {
                    projPoint = new PointF(point.x, point.y);
                    if (point.y > plotArea.bottom) {
                        projPoint.y = plotArea.bottom;
                    } else if (point.y < plotArea.top) {
                        projPoint.y = plotArea.top;
                    }
                } else if (plotArea.top < point.y && point.y < plotArea.bottom) {
                    projPoint = new PointF(point.x, point.y);
                    if (point.x > plotArea.right) {
                        projPoint.x = plotArea.right;
                    } else if (point.x < plotArea.left) {
                        projPoint.x = plotArea.left;
                    }
                }
            }else{
                projPoint = new PointF(point.x, point.y);
                PointF center=getPlotAreaCenter(plotArea);

                if(plotArea.left<point.x && point.x<center.x){
                    projPoint.x=plotArea.left;
                }
                if(plotArea.right>point.x && point.x>center.x){
                    projPoint.x=plotArea.right;
                }
                if(plotArea.top<point.y && point.y<center.y){
                    projPoint.y=plotArea.top;
                }
                if(plotArea.bottom>point.y && point.y>center.y){
                    projPoint.y=plotArea.bottom;
                }

            }
            return projPoint;
        }

        public static PointF getPlotAreaCenter(RectF plotArea){
            return new PointF(plotArea.centerX(),plotArea.centerY());
        }

        //Метод будет использован для создание точки, перед Border
        //Аналог buildPlotAreaMore для Box
        public static PointF createBorderPrevPoint(PointF point,Border border) {
            PointF prevPoint=new PointF();
            float k = 0.05f;
            if(border.type.equals("I")){
                prevPoint=new PointF(point.x+k,point.y);
            }else if(border.type.equals("C")){
                prevPoint=new PointF(point.x,point.y+k);
            }else if(border.type.equals("O")){
                prevPoint=new PointF(point.x-k,point.y);
            }else if(border.type.equals("M")){
                prevPoint=new PointF(point.x,point.y-k);
            }
            return prevPoint;
        }

        public static RectF buildPlotAreaVertical(PointF p1,PointF p2){

            RectF plotArea=new RectF();
            plotArea.top=0;
            plotArea.bottom=1;
            if(p1.x<p2.x){
                plotArea.left=p1.x;
                plotArea.right=p2.x;
            }else{
                plotArea.left=p2.x;
                plotArea.right=p1.x;
            }

            return plotArea;
        }

        public static RectF buildPlotAreaHorizontal(PointF p1,PointF p2){

            RectF plotArea=new RectF();
            plotArea.left=0;
            plotArea.right=1;
            if(p1.y<p2.y){
                plotArea.top=p1.y;
                plotArea.bottom=p2.y;
            }else{
                plotArea.top=p2.y;
                plotArea.bottom=p1.y;
            }

            return plotArea;
        }


        public static boolean pointInPlotArea(PointF point,RectF plotArea){
            if(plotArea.left<=point.x && point.x<=plotArea.right){
                if(plotArea.top<=point.y && point.y<=plotArea.bottom){
                    return true;
                }
            }
            return false;
        }

        //Возвращает список точек, лежащих на границе области
        public static ArrayList<PointF> pointsToPlotAreaLimit(RectF plotArea,ArrayList<PointF> pointList){
            ArrayList<PointF> newPointList=new ArrayList<PointF>();
            for(PointF point : pointList){
                if(point.x<plotArea.left){
                    point.x=plotArea.left;
                }else if(point.x>plotArea.right){
                    point.x=plotArea.right;
                }

                if(point.y<plotArea.top){
                    point.y=plotArea.top;
                }else if(point.y>plotArea.bottom){
                    point.y=plotArea.bottom;
                }

                newPointList.add(point);
            }
            return newPointList;
        }

        //Возвращает точку, лежащую на границе области
        public static PointF pointsToPlotAreaLimit(RectF plotArea,PointF point){

            if(point.x<plotArea.left){
                point.x=plotArea.left;
            }else if(point.x>plotArea.right){
                point.x=plotArea.right;
            }

            if(point.y<plotArea.top){
                point.y=plotArea.top;
            }else if(point.y>plotArea.bottom){
                point.y=plotArea.bottom;
            }

            return point;
        }

        //Возвращает точку, лежащую внутри области перед границей
        public static PointF pointsToPlotAreaLimitLess(RectF plotArea,PointF point){
            float k=0.05f;

            if(point.x<plotArea.left){
                point.x=plotArea.left+k;
            }else if(point.x>plotArea.right){
                point.x=plotArea.right-k;
            }

            if(point.y<plotArea.top){
                point.y=plotArea.top+k;
            }else if(point.y>plotArea.bottom){
                point.y=plotArea.bottom-k;
            }

            return point;
        }

        //Возвращает новую точку, лежащую на границе области
        public static PointF newPointsToPlotAreaLimit(RectF plotArea,PointF point){
            float k=0.05f;
            PointF newPoint=null;

            if(point.x<plotArea.left){
                newPoint=new PointF(plotArea.left,point.y);
            }else if(point.x>plotArea.right){
                newPoint=new PointF(plotArea.right,point.y);
            }

            if(point.y<plotArea.top){
                newPoint=new PointF(point.x,plotArea.top);
            }else if(point.y>plotArea.bottom){
                newPoint=new PointF(point.x,plotArea.bottom);
            }

            if(newPoint!=null){
                point=newPoint;
            }

            return point;
        }

        //Метод может только расширить область
        public static RectF resizePlotArea(RectF plotArea, PointF p) {
            if (p.x < plotArea.left) {
                plotArea.left = p.x;
            }
            if (p.x > plotArea.right) {
                plotArea.right = p.x;
            }
            if (p.y < plotArea.top) {
                plotArea.top = p.y;
            }
            if (p.y > plotArea.bottom) {
                plotArea.bottom = p.y;
            }
            return plotArea;
        }

        //Обрезает часть области по точке cutPoint,
        //точка savePoint обозначает ту часть области, которую нужно оставить
        public static RectF cutPlotAreaX(RectF plotArea,PointF savePoint,PointF cutPoint){
            if(pointInPlotArea(cutPoint,plotArea) && pointInPlotArea(savePoint,plotArea)){
                if(cutPoint.x>savePoint.x){
                    plotArea.right=cutPoint.x;
                }else{
                    plotArea.left=cutPoint.x;
                }
            }
            return plotArea;
        }

        //Обрезает часть области по точке cutPoint,
        //точка savePoint обозначает ту часть области, которую нужно оставить
        //Метод возвращает меньше области, чем метод cutPlotAreaX
        public static RectF cutPlotAreaXByK(RectF plotArea,PointF savePoint,PointF cutPoint){
            float k=0.005f;
            if(pointInPlotArea(cutPoint,plotArea) && pointInPlotArea(savePoint,plotArea)){
                if(cutPoint.x>savePoint.x){
                    plotArea.right=cutPoint.x-k;
                }else{
                    plotArea.left=cutPoint.x+k;
                }
            }
            return plotArea;
        }

        //Обрезает часть области по точке cutPoint,
        //точка savePoint обозначает ту часть области, которую нужно оставить
        public static RectF cutPlotAreaY(RectF plotArea,PointF savePoint,PointF cutPoint){
            if(pointInPlotArea(cutPoint,plotArea) && pointInPlotArea(savePoint,plotArea)){
                if(cutPoint.y>savePoint.y){
                    plotArea.bottom=cutPoint.y;
                }else{
                    plotArea.top=cutPoint.y;
                }
            }
            return plotArea;
        }

        //Обрезает часть области по точке cutPoint,
        //точка savePoint обозначает ту часть области, которую нужно оставить
        //Метод возвращает меньше области, чем метод cutPlotAreaY
        public static RectF cutPlotAreaYByK(RectF plotArea,PointF savePoint,PointF cutPoint){
            float k=0.005f;
            if(pointInPlotArea(cutPoint,plotArea) && pointInPlotArea(savePoint,plotArea)){
                if(cutPoint.y>savePoint.y){
                    plotArea.bottom=cutPoint.y-k;
                }else{
                    plotArea.top=cutPoint.y+k;
                }
            }
            return plotArea;
        }

        //Обрезает часть области по вертикали,
        //метод обратный методу resizePlotArea
        public static RectF cutPlotAreaVertical(RectF plotArea,float k){
            plotArea.top=plotArea.top+k;
            plotArea.bottom=plotArea.bottom-k;
            return plotArea;
        }

        //Обрезает часть области по вертикали, значением по умолчанию
        //метод обратный методу resizePlotArea
        public static RectF cutPlotAreaVertical(RectF plotArea){
            float k=0.005f;
            return cutPlotAreaVertical(plotArea,k);
        }

        //Обрезает часть области по горизонтали,
        //метод обратный методу resizePlotArea
        public static RectF cutPlotAreaHorizontal(RectF plotArea,float k){
            plotArea.left=plotArea.left+k;
            plotArea.right=plotArea.right-k;
            return plotArea;
        }

        //Обрезает часть области по горизонтали, значением по умолчанию
        //метод обратный методу resizePlotArea
        public static RectF cutPlotAreaHorizontal(RectF plotArea){
            float k=0.005f;
            return cutPlotAreaHorizontal(plotArea,k);
        }

        //Возвращает пересечение двух областей
        public static RectF intersectionPlotArea(RectF plotArea1,RectF plotArea2){
            RectF plotArea=new RectF();
            if(plotArea1.left<plotArea2.left){
                plotArea.left=plotArea2.left;
            }else{
                plotArea.left=plotArea1.left;
            }

            if(plotArea1.top<plotArea2.top){
                plotArea.top=plotArea2.top;
            }else{
                plotArea.top=plotArea1.top;
            }

            if(plotArea1.right<plotArea2.right){
                plotArea.right=plotArea1.right;
            }else{
                plotArea.right=plotArea2.right;
            }

            if(plotArea1.bottom<plotArea2.bottom){
                plotArea.bottom=plotArea1.bottom;
            }else{
                plotArea.bottom=plotArea2.bottom;
            }

            return plotArea;
        }

        //Метод может только расширить область
        public static RectF resizePlotAreaMore(RectF plotArea, PointF p) {
            float k=0.05f;
            if (p.x <= plotArea.left) {
                plotArea.left = p.x - k;
            }
            if (p.x >= plotArea.right) {
                plotArea.right = p.x + k;
            }
            if (p.y <= plotArea.top) {
                plotArea.top = p.y - k;
            }
            if (p.y >= plotArea.bottom) {
                plotArea.bottom = p.y + k;
            }
            return plotArea;
        }

        //Минимальное расстояние между отрезком и точкой
        public static double minDistance(PointF point, PointF linePoint1, PointF linePoint2) {
            double distance;

            PointF A = new PointF(linePoint1.x, linePoint1.y);
            PointF B = new PointF(linePoint2.x, linePoint2.y);
            PointF C = new PointF(point.x, point.y);

            double a = (C.x - A.x) * (C.x - A.x) + (C.y - A.y) * (C.y - A.y);
            double b = (C.x - B.x) * (C.x - B.x) + (C.y - B.y) * (C.y - B.y);
            double c = (A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y);

            if (a >= (b + c)) {
                distance = Math.sqrt(b);
            } else if (b >= (a + c)) {
                distance = Math.sqrt(a);
            } else {
                double a1 = A.x - C.x;
                double a2 = A.y - C.y;
                double b1 = B.x - C.x;
                double b2 = B.y - C.y;

                distance = Math.sqrt(((a1 * b2) * (a1 * b2) + (-a2 * b1) * (-a2 * b1)) / c);
            }

            return distance;
        }
    }

    static class Diagram{

        //public String id;
        public int id;
        public Diagram parent;
        public int diagramNumberRegister;

        public String creationDate;
        public String revisionDate;

        public Border leftBorder;
        public Border rightBorder;
        public Border topBorder;
        public Border bottomBorder;

        public int boxNumberRegister;
        public int arrowNumberRegister;

        public BoxLimit boxLimit;
        HashSet<Box> boxSet=new HashSet<Box>();
        HashSet<Arrow> arrowSet=new HashSet<Arrow>();
        HashSet<Branch> branchSet=new HashSet<Branch>();
        HashSet<Join> joinSet=new HashSet<Join>();
        ArrayList<Suspicion> suspicionList=new ArrayList<Suspicion>();
        ArrayList<Suspicion> guiltyList=new ArrayList<Suspicion>();
        NewArrow newArrow=new NewArrow();

        Object selectedObject;

        public void setRevisionDate(String revisionDate){
           this.revisionDate=revisionDate;
        }

        public String getRevisionDate(){
            return revisionDate;
         }

        public void newRevisionDate(){
            Calendar today=Calendar.getInstance();
            SimpleDateFormat df=new SimpleDateFormat("dd/M/y");
            setRevisionDate(df.format(today.getTimeInMillis()).toString());
        }

        public void setCreationDate(String creationDate){
            this.creationDate=creationDate;
        }

        public String getCreationDate(){
            return creationDate;
        }

        public void newCreationDate(){
            Calendar today=Calendar.getInstance();
            SimpleDateFormat df=new SimpleDateFormat("dd/M/y");
            setCreationDate(df.format(today.getTimeInMillis()).toString());
        }

        Comparator<Box> boxSortByNumber=new Comparator<Box>() {
            @Override
            public int compare(Box box1, Box box2) {
                if(box1.number<box2.number){
                    return -1;
                }else if(box1.number>box2.number){
                    return 1;
                }
                return 0;
            }
        };

        Comparator<Arrow> arrowSortByNumber=new Comparator<Arrow>() {
            @Override
            public int compare(Arrow arrow1, Arrow arrow2) {
                if(arrow1.number<arrow2.number){
                    return -1;
                }else if(arrow1.number>arrow2.number){
                    return 1;
                }
                return 0;
            }
        };

        Diagram(int id){
            this.id=id;
            leftBorder=new Border("I");
            topBorder=new Border("C");
            rightBorder=new Border("O");
            bottomBorder=new Border("M");
            diagramNumberRegister=id*10;
        }

        Diagram(int id,BoxLimit boxLimit){
            this.id=id;
            this.boxLimit=boxLimit;
            leftBorder=new Border("I");
            topBorder=new Border("C");
            rightBorder=new Border("O");
            bottomBorder=new Border("M");
            diagramNumberRegister=id*10;
        }

        Diagram(int id,Diagram diagram){
             this.id=id;
             this.boxLimit=new BoxLimit(diagram.boxLimit);
             leftBorder=new Border(diagram.leftBorder);
             topBorder=new Border(diagram.topBorder);
             rightBorder=new Border(diagram.rightBorder);
             bottomBorder=new Border(diagram.bottomBorder);
             this.boxSet= new HashSet<Box>(diagram.boxSet);
             this.arrowSet= new HashSet<Arrow>(diagram.arrowSet);
             this.branchSet=new HashSet<Branch>(diagram.branchSet);
             this.joinSet=new HashSet<Join>(diagram.joinSet);
             diagramNumberRegister=id*10;
        }

        public Border getBorderByType(String type){
            if(type.equals("I")){
                return leftBorder;
            }
            if(type.equals("C")){
                return topBorder;
            }
            if(type.equals("O")){
                return rightBorder;
            }
            if(type.equals("M")){
                return bottomBorder;
            }
            return null;
        }

        public void setDiagramNumberRegister(int diagramNumberRegister){
             this.diagramNumberRegister=diagramNumberRegister;
        }

        public int getDiagramNumberRegister(){
             return diagramNumberRegister;
         }

        public int newDiagramNumber(){
             diagramNumberRegister++;
             return getDiagramNumberRegister();
        }

         public void addDiagramNumber(int number){
             if(number>getDiagramNumberRegister()){
                 setDiagramNumberRegister(number);
             }
         }

         public void removeDiagramNumber(){
             setDiagramNumberRegister(diagramNumberRegister-1);
         }


         //Рекурсивное переименование диаграмм
        /*
         public void renameDiagrams(Box removedBox){
             for(Box box : boxSet){
                 if(box.diagramId>removedBox.diagramId){
                     box.diagramId--;
                     if(box.reference!=null){
                         renameDiagram(box);
                     }
                 }
             }
         }

         public void renameDiagram(Box lastBox){
             if(lastBox.reference!=null) {
                 int lastId=lastBox.reference.id;
                 lastBox.reference=new Diagram(lastBox.diagramId,lastBox.reference);
                 for (Box box : lastBox.reference.boxSet) {
                     box.diagramId=box.diagramId-(lastId*10)+lastBox.reference.id*10;
                     if (box.reference!=null) {
                        renameDiagram(box);
                     }
                 }
             }
         }
        */


        public int newRelationNumber(Box box,String type){
            int i=0;
            for(Arrow arrow : arrowSet){
                if (type.equals("O")) {
                    if (arrow.source.getConnectorSource() == box) {
                        if (i < arrow.source.getRelationNum()) {
                            i = arrow.source.getRelationNum();
                        }
                    }
                }else{
                    if (arrow.sink.getConnectorSink() == box) {
                        if (arrow.sink.getConnectorType().equals(type)) {
                            if (i < arrow.sink.getRelationNum()) {
                                i = arrow.sink.getRelationNum();
                            }
                        }
                    }
                }
            }

            i++;
            return i;
        }

        public int newRelationNumber(Border border,String type){
            int i=0;
            if(getParent()!=null) {
                for (Arrow arrow : arrowSet) {
                    if (type.equals("O")) {
                        if (arrow.sink.getConnectorSink() == border) {
                            if (i < arrow.source.getRelationNum()) {
                                i = arrow.source.getRelationNum();
                            }
                        }
                    } else {
                        if (arrow.source.getConnectorSource() == border) {
                            if (arrow.source.getConnectorType().equals(type)) {
                                if (i < arrow.source.getRelationNum()) {
                                    i = arrow.source.getRelationNum();
                                }
                            }
                        }
                    }
                }
                i++;
                return i;
            }
            return 0;
        }

         public void setBoxNumberRegister(int boxNumberRegister){
             this.boxNumberRegister=boxNumberRegister;
         }

        public int getBoxNumberRegister(){
            return boxNumberRegister;
        }

        public int newBoxNumber(){
            int curBoxNumber=getBoxNumberRegister();
            boxNumberRegister++;
            return curBoxNumber;
        }

        public void addBoxNumber(int number){
            if(number>getBoxNumberRegister()){
                setBoxNumberRegister(number);
            }
        }

        public void removeBoxNumber(){
            boxNumberRegister--;
        }



        public void addDiagramStartArrow(Arrow arrow,Box box){
            float k=0.15f;
            float lk=0.03f;
            if(box.reference!=null){
                Diagram diagram=box.reference;
                if (newArrow.source.getConnectorSource() == box) {
                    float ky=(newArrow.source.getConnectorPoint().y-box.coord.p2.y)/(box.coord.p1.y-box.coord.p2.y);
                    Arrow startArrow=new Arrow(diagram.newArrowNumber(),arrow.label);
                    startArrow.source.setConnectorPoint(new PointF(1-k,ky));
                    startArrow.sink.setConnectorPoint(new PointF(1,ky));
                    startArrow.sink.setConnectorType("O");
                    startArrow.sink.setConnectorSink(diagram.getBorderByType("O"));
                    startArrow.sink.setRelationNum(arrow.source.getRelationNum());
                    startArrow.insertLabel(arrow.label, new PointF(1 - k, ky + lk));
                    diagram.addArrow(startArrow);
                }
                if (newArrow.sink.getConnectorSink() == box) {
                    float kx=1;
                    float ky=1;
                    if (arrow.sink.getConnectorType().equals("I")) {
                        ky = (arrow.sink.getConnectorPoint().y - box.coord.p2.y) / (box.coord.p1.y - box.coord.p2.y);
                    }else{
                        kx = (arrow.sink.getConnectorPoint().x - box.coord.p1.x) / (box.coord.p2.x - box.coord.p1.x);
                    }
                    Arrow startArrow=new Arrow(diagram.newArrowNumber(),arrow.label);
                    //при инициализации точек ConnectorPoint, нужно сначало добавит в source, а затем в sink
                    if(arrow.sink.getConnectorType().equals("I")){
                        startArrow.source.setConnectorPoint(new PointF(0,ky));
                        startArrow.sink.setConnectorPoint(new PointF(k,ky));
                        startArrow.sink.setConnectorType("I");
                        startArrow.insertLabel(arrow.label, new PointF(k, ky + lk));
                    }else if(arrow.sink.getConnectorType().equals("C")){
                        startArrow.source.setConnectorPoint(new PointF(kx,0));
                        startArrow.sink.setConnectorPoint(new PointF(kx,k));
                        startArrow.sink.setConnectorType("C");
                        startArrow.insertLabel(arrow.label, new PointF(kx - lk, k));
                    }else if(arrow.sink.getConnectorType().equals("M")){
                        startArrow.source.setConnectorPoint(new PointF(kx,1));
                        startArrow.sink.setConnectorPoint(new PointF(kx,1-k));
                        startArrow.sink.setConnectorType("M");
                        startArrow.insertLabel(arrow.label, new PointF(kx - lk, 1 - k));
                    }

                    startArrow.source.setConnectorType(arrow.sink.getConnectorType());
                    startArrow.source.setConnectorSource(diagram.getBorderByType(arrow.sink.getConnectorType()));
                    startArrow.source.setRelationNum(arrow.sink.getRelationNum());
                    diagram.addArrow(startArrow);
                }
            }
        }


        public void initDiagramStartArrows(Box box){
            float k=0.15f;
            float lk=0.03f;
            if(box.reference!=null){
                Diagram diagram=box.reference;
                for(Arrow arrow : arrowSet){
                    if(arrow.source.getConnectorSource()==box){
                        float ky=(arrow.source.getConnectorPoint().y-box.coord.p2.y)/(box.coord.p1.y-box.coord.p2.y);
                        Arrow startArrow=new Arrow(diagram.newArrowNumber());
                        startArrow.source.setConnectorPoint(new PointF(1-k,ky));
                        startArrow.sink.setConnectorPoint(new PointF(1,ky));
                        startArrow.sink.setConnectorType("O");
                        startArrow.sink.setConnectorSink(diagram.getBorderByType("O"));
                        startArrow.sink.setRelationNum(arrow.source.getRelationNum());
                        startArrow.insertLabel(arrow.label, new PointF(1 - k, ky + lk));
                        diagram.addArrow(startArrow);
                    }
                    if(arrow.sink.getConnectorSink()==box){
                        float kx=1;
                        float ky=1;
                        if (arrow.sink.getConnectorType().equals("I")) {
                            ky = (arrow.sink.getConnectorPoint().y - box.coord.p2.y) / (box.coord.p1.y - box.coord.p2.y);
                        }else{
                            kx = (arrow.sink.getConnectorPoint().x - box.coord.p1.x) / (box.coord.p2.x - box.coord.p1.x);
                        }
                        Arrow startArrow=new Arrow(diagram.newArrowNumber(),arrow.label);
                        //при инициализации точек ConnectorPoint, нужно сначало добавит в source, а затем в sink
                        if(arrow.sink.getConnectorType().equals("I")){
                            startArrow.source.setConnectorPoint(new PointF(0,ky));
                            startArrow.sink.setConnectorPoint(new PointF(k,ky));
                            startArrow.sink.setConnectorType("I");
                            startArrow.insertLabel(arrow.label, new PointF(k, ky + lk));
                        }else if(arrow.sink.getConnectorType().equals("C")){
                            startArrow.source.setConnectorPoint(new PointF(kx,0));
                            startArrow.sink.setConnectorPoint(new PointF(kx,k));
                            startArrow.sink.setConnectorType("C");
                            startArrow.insertLabel(arrow.label, new PointF(kx - lk, k));
                        }else if(arrow.sink.getConnectorType().equals("M")){
                            startArrow.source.setConnectorPoint(new PointF(kx,1));
                            startArrow.sink.setConnectorPoint(new PointF(kx,1-k));
                            startArrow.sink.setConnectorType("M");
                            startArrow.insertLabel(arrow.label, new PointF(kx - lk, 1 - k));
                        }

                        startArrow.source.setConnectorType(arrow.sink.getConnectorType());
                        startArrow.source.setConnectorSource(diagram.getBorderByType(arrow.sink.getConnectorType()));
                        startArrow.source.setRelationNum(arrow.sink.getRelationNum());
                        diagram.addArrow(startArrow);
                    }
                }
            }
        }


        public void setArrowNumberRegister(int arrowNumberRegister){
            this.arrowNumberRegister=arrowNumberRegister;
        }

        public int getArrowNumberRegister(){
            return arrowNumberRegister;
        }

        public int newArrowNumber(){
            arrowNumberRegister++;
            return getArrowNumberRegister();
        }

        public void addArrowNumber(int number){
             if(getArrowNumberRegister()<number){
                 setArrowNumberRegister(number);
             }
         }

        public void removeArrowNumber(){
             arrowNumberRegister--;
        }

         public void renameArrows(Arrow removedArrow){
            if(arrowSet.size()==0) {
                return;
            }

            for(Arrow arrow : arrowSet){
                if(arrow.number>removedArrow.number){
                    arrow.number--;
                }
            }
        }

        public void setBoxLimit(BoxLimit boxLimit){
            this.boxLimit=boxLimit;
        }

        public BoxLimit getBoxLimit(){
            return boxLimit;
        }

        public void setParent(Diagram parent){
            this.parent=parent;
        }

        public Diagram getParent(){
            return parent;
        }

        public void setSelectedObject(Box box){
            selectedObject=box;
        }

        public void setSelectedObject(Arrow arrow){
            selectedObject=arrow;
        }

        public void setSelectedObject(Arrow.ArrowSource arrowSource){
            selectedObject=arrowSource;
        }

        public void setSelectedObject(Arrow.ArrowSink arrowSink){
            selectedObject=arrowSink;
        }

        public void setSelectedObject(Box.BoxConnector boxConnector){
            selectedObject=boxConnector;
        }

        public void setSelectedObject(Arrow.ArrowLabel label){
            selectedObject=label;
        }

        public void setSelectedObject(Border border){
            selectedObject=border;
        }

        public Object getSelectedObject(){
            return selectedObject;
        }

        public void removeSelectedObject(){
            selectedObject=null;
        }

        public void addBox(Box box){
            if(boxLimit.getMaxLimit()>boxSet.size()) {
                boxSet.add(box);
            }else{
                Log.w("Предел Box'ов достигнут",String.valueOf(boxSet.size()));
            }
        }

         public void addBox(PointF touchPoint){
             if(boxLimit.getMaxLimit()>boxSet.size()) {
                 int number=newBoxNumber();
                 String name="(dafault)";
                 int diagramId=0;
                 boxSet.add(new Box(touchPoint,number,name,diagramId));

             }else{
                 Log.w("Предел Box'ов достигнут",String.valueOf(boxSet.size()));
             }
         }

        public void removeBox(Box box){
            boxSet.remove(box);
            removeBoxNumber();
        }

        public void moveBox(Box box,PointF touchPoint){
            PointF p1=new PointF(box.coord.p1.x,box.coord.p1.y);
            PointF p2=new PointF(box.coord.p2.x,box.coord.p2.y);

            float xShift=touchPoint.x-p1.x;
            float yShift=touchPoint.y-p1.y;

            //Shift points
            PointF sp1=new PointF(p1.x+xShift,p1.y+yShift);
            PointF sp2=new PointF(p2.x+xShift,p2.y+yShift);
            box.setBoxCoord(sp1,sp2);

            //New points, real shift
            PointF np1=new PointF(box.coord.p1.x,box.coord.p1.y);
            xShift=np1.x-p1.x;
            yShift=np1.y-p1.y;

            for(Arrow arrow : this.arrowSet) {
                if (arrow.source.obj == box) {
                    PointF con = arrow.source.getConnectorPoint();
                    moveArrowSource(arrow,xShift,yShift);
                }
                else if (arrow.sink.obj == box) {
                    PointF con = arrow.sink.getConnectorPoint();
                    moveArrowSink(arrow,xShift,yShift);
                    //arrow.setSinkPoint(new PointF(((con.x * docWidth) + xShift) / docWidth, ((con.y * docHeight) + yShift) / docHeight));
                }
            }

        }

         public void moveBox(Box box,float xShift,float yShift){
            PointF p1=box.coord.p1;
            PointF p2=box.coord.p2;

            //Shift points
            PointF sp1=new PointF(p1.x+xShift,p1.y+yShift);
            PointF sp2=new PointF(p2.x+xShift,p2.y+yShift);
            box.setBoxCoord(sp1,sp2);

            //New points, real shift
            PointF np1=box.coord.p1;
            xShift=np1.x-p1.x;
            yShift=np1.y-p1.y;

            for(Arrow arrow : this.arrowSet) {
                if (arrow.source.obj == box) {
                    PointF con = arrow.source.getConnectorPoint();
                    moveArrowSource(arrow,xShift,yShift);
                }else if(arrow.sink.obj == box){
                    PointF con = arrow.sink.getConnectorPoint();
                    moveArrowSink(arrow, xShift, yShift);
                }
            }

        }

        //Строит путь по newArrow
        public void buildPath() {
            Object obj1 = newArrow.getSource().getConnectorSource();
            Object obj2 = newArrow.getSink().getConnectorSink();
            if (obj1 instanceof Box && obj2 instanceof Box) {
                buildPath((Box) obj1, (Box) obj2);
            } else if (obj1 instanceof Box && obj2 instanceof Border) {
                buildPath((Box) obj1, (Border) obj2);
            } else if (obj1 instanceof Border && obj2 instanceof Box) {
                buildPath((Border) obj1, (Box) obj2);
            } else if (obj1 instanceof Arrow.ArrowLabel && obj2 instanceof Arrow) {
                buildSquigglePath();
            } else if (obj1 instanceof Arrow && obj2 instanceof Arrow.ArrowLabel) {
                buildSquigglePath();
            } else if(obj1 instanceof Arrow.ArrowSink && obj2 instanceof Box){
                buildPath((Arrow.ArrowSink)obj1,(Box)obj2);
            } else if(obj1 instanceof Arrow.ArrowSink && obj2 instanceof Border){
                buildPath((Arrow.ArrowSink)obj1,(Border)obj2);
            } else if(obj1 instanceof Box && obj2 instanceof Arrow.ArrowSource){
                buildPath((Box)obj1, (Arrow.ArrowSource)obj2);
            } else if(obj1 instanceof Border && obj2 instanceof Arrow.ArrowSource){
                buildPath((Border)obj1, (Arrow.ArrowSource)obj2);
            } else if(obj1 instanceof Arrow && obj2 instanceof Box){
                //Новая стрелка будет начинаться на Branch
                buildPath((Arrow)obj1,(Box)obj2);
            } else if(obj1 instanceof Arrow && obj2 instanceof Border){
                //Новая стрелка будет начинаться на Branch
                buildPath((Arrow)obj1,(Border)obj2);
            } else if(obj1 instanceof Box && obj2 instanceof Arrow){
                //Новая стрелка будет указывать на Join
                buildPath((Box)obj1,(Arrow)obj2);
            } else if(obj1 instanceof Border && obj2 instanceof Arrow){
                //Новая стрелка будет указывать на Join
                buildPath((Border)obj1,(Arrow)obj2);
            }else if(obj1 instanceof Arrow && obj2 instanceof Arrow){
                buildPath((Arrow)obj1,(Arrow)obj2);
            }
        }

         public Arrow buildPath(boolean addChildArrow) {
             Object obj1 = newArrow.getSource().getConnectorSource();
             Object obj2 = newArrow.getSink().getConnectorSink();
             Arrow arrow=new Arrow();
             if (obj1 instanceof Box && obj2 instanceof Box) {
                 arrow=buildPath((Box) obj1, (Box) obj2,addChildArrow);
             } else if (obj1 instanceof Box && obj2 instanceof Border) {
                 arrow=buildPath((Box) obj1, (Border) obj2,addChildArrow);
             } else if (obj1 instanceof Border && obj2 instanceof Box) {
                 arrow=buildPath((Border) obj1, (Box) obj2,addChildArrow);
             }
             return arrow;
         }

         //Строит путь по newArrow
         //Перестраивает Arrow
         public void buildPath(Box box,Arrow.ArrowSource source){
             Arrow curArrow=new Arrow();
             for(Arrow arrow : arrowSet){
                 if(arrow.source == source){
                     curArrow=arrow;
                     break;
                 }
             }
             if(curArrow.sink.getConnectorSink() instanceof Box){
                 Box newBox=(Box)curArrow.sink.getConnectorSink();
                 newArrow.setSink(curArrow.sink.getConnectorPoint(),curArrow.sink.getConnectorType(),newBox,curArrow.sink.getRelationNum());
                 buildPath(box,newBox);
                 removeArrow(curArrow);
             }else if(curArrow.sink.getConnectorSink() instanceof Border){
                 Border newBorder=(Border)curArrow.sink.getConnectorSink();
                 newArrow.setSink(curArrow.sink.getConnectorPoint(),curArrow.sink.getConnectorType(),newBorder,curArrow.sink.getRelationNum());
                 buildPath(box,newBorder);
                 removeArrow(curArrow);
             }
         }

         //Строит путь по newArrow
         //Перестраивает Arrow
         public void buildPath(Border border,Arrow.ArrowSource source){
             Arrow curArrow=new Arrow();
             for(Arrow arrow : arrowSet){
                 if(arrow.source == source){
                     curArrow=arrow;
                     break;
                 }
             }
             if(curArrow.sink.getConnectorSink() instanceof Box){
                 Box newBox=(Box)curArrow.sink.getConnectorSink();
                 newArrow.setSink(curArrow.sink.getConnectorPoint(),curArrow.sink.getConnectorType(),newBox,curArrow.sink.getRelationNum());
                 buildPath(border,newBox);
                 removeArrow(curArrow);
             }
         }

         //Строит путь по newArrow
         //Перестраивает Arrow
         public void buildPath(Arrow.ArrowSink sink,Box box){
             Arrow curArrow=new Arrow();
             for(Arrow arrow : arrowSet){
                 if(arrow.sink == sink){
                     curArrow=arrow;
                     break;
                 }
             }
             if(curArrow.source.getConnectorSource() instanceof Box){
                 Box newBox=(Box)curArrow.source.getConnectorSource();
                 newArrow.setSource(curArrow.source.getConnectorPoint(),curArrow.source.getConnectorType(),newBox,curArrow.source.getRelationNum());
                 buildPath(newBox,box);
                 removeArrow(curArrow);
             }else if(curArrow.source.getConnectorSource() instanceof Border){
                 Border newBorder=(Border)curArrow.source.getConnectorSource();
                 newArrow.setSource(curArrow.source.getConnectorPoint(),curArrow.source.getConnectorType(),newBorder,curArrow.source.getRelationNum());
                 buildPath(newBorder,box);
                 removeArrow(curArrow);
             }
         }

         public void buildPath(Arrow.ArrowSink sink,Border border){
             Arrow curArrow=new Arrow();
             for(Arrow arrow : arrowSet){
                 if(arrow.sink == sink){
                     curArrow=arrow;
                     break;
                 }
             }
             if(curArrow.source.getConnectorSource() instanceof Box){
                 Box newBox=(Box)curArrow.source.getConnectorSource();
                 newArrow.setSource(curArrow.source.getConnectorPoint(),curArrow.source.getConnectorType(),newBox,curArrow.source.getRelationNum());
                 buildPath(newBox,border);
                 removeArrow(curArrow);
             }
         }


         //Строит путь по newArrow
         //Построение пути с использованием Branch
         public void buildPath(Arrow arrow,Box box){
             PointF divPoint=newArrow.getSource().getConnectorPoint();
             boolean defaultBuild=true;
             //Проверяем если выбрана узловая точка arrow
             if(arrow.isNodalPoint(divPoint)){
                 defaultBuild=false;
             }

             Branch branch=new Branch();
             ArrayList<Arrow> arrowList = divArrowByBranch(arrow,branch,divPoint);
             if (arrowList != null) {

                 newArrow.source.setConnectorSource(branch);
                 Arrow newChildArrow=null;
                 if(defaultBuild!=true){
                     newChildArrow = buildPath(branch, box, true);
                 }else {
                     newChildArrow = buildPath(branch, box);
                 }
                 branch.addChild(newChildArrow);
                 addBranch(branch);
             }
         }


         //Строит путь по newArrow, по направлению branch.parent
         public Arrow buildPath(Branch branch,Box box,boolean parentDirection){

             ArrayList<PointF> path=new ArrayList<PointF>();
             Arrow.ArrowSource source=newArrow.getSource();
             Arrow.ArrowSink sink=newArrow.getSink();

             PointF sourcePoint=source.getConnectorPoint();
             PointF sinkPoint=sink.getConnectorPoint();
             PointF firstPoint=sourcePoint;
             PointF lastPoint=sinkPoint;
             PointF startPoint=firstPoint;
             PointF endPoint=lastPoint;


             Arrow parentArrow=branch.parent;
             PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
             PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
             String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
             //startDirections.add(MathCalc.getInverseDirection(direction));
             String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());


             startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
             ArrayList<String> startDirections=new ArrayList<String>();
             ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
             startDirections.add(direction);
             startDirections.add(nextDirections.remove(0));
             startDirections.add(nextDirections.remove(0));


             ArrayList<PointF> pointList=box.coord.getPointList();
             if(pointList!=null){
                 RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                 endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
             }

             RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

             pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
             if(pointList!=null){
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
             }

             path.add(firstPoint);
             if(startPoint!=firstPoint){
                 path.add(startPoint);
             }

             ArrayList<PointF> newPath=new ArrayList<PointF>();
             //Построение прямолинейного пути
             newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

             path.addAll(newPath);
             path.add(endPoint);
             if(endPoint!=lastPoint){
                 path.add(lastPoint);
             }
             path=removeExcessPoints(path);
             //path=MathCalc.screenPointsToDoc(path);

             newArrow.number=newArrowNumber();
             Arrow arrow=new Arrow(newArrow);
             arrow.setArrowCoord(path);
             addArrow(arrow);
             return arrow;
         }


         public Arrow buildPath(Branch branch,Box box){
             ArrayList<PointF> path=new ArrayList<PointF>();
             Arrow.ArrowSource source=newArrow.getSource();
             Arrow.ArrowSink sink=newArrow.getSink();

             PointF sourcePoint=source.getConnectorPoint();
             PointF sinkPoint=sink.getConnectorPoint();
             PointF firstPoint=sourcePoint;
             PointF lastPoint=sinkPoint;
             PointF startPoint=firstPoint;
             PointF endPoint=lastPoint;

             //ArrayList<String> startDirections=MathCalc.getDirectionsByBranch(branch);

             Arrow parentArrow=branch.parent;
             PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
             PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);

             String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());

             ArrayList<PointF> pointList=box.coord.getPointList();
             if(pointList!=null){
                 RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                 endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
             }

             //Ближайшее направление до endPoint с учетом текущей ориентации
             String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint));
             startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
             ArrayList<String> startDirections=new ArrayList<String>();
             ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
             startDirections.add(closestDirection);
             startDirections.add(nextDirections.remove(0));
             startDirections.add(nextDirections.remove(0));


             RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

             pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
             if(pointList!=null){
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
             }

             path.add(firstPoint);
             if(startPoint!=firstPoint){
                 path.add(startPoint);
             }

             ArrayList<PointF> newPath=new ArrayList<PointF>();
             //Построение прямолинейного пути
             newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

             path.addAll(newPath);
             path.add(endPoint);
             if(endPoint!=lastPoint){
                 path.add(lastPoint);
             }
             path=removeExcessPoints(path);
             //path=MathCalc.screenPointsToDoc(path);

             newArrow.number=newArrowNumber();
             Arrow arrow=new Arrow(newArrow);
             arrow.setArrowCoord(path);
             addArrow(arrow);
             return arrow;
         }



        //Строит путь по ArrowSource и ArrowSink, по направлению branch.parent
        //Может быть использован в методе moveArrowSink
        public ArrayList<PointF> buildPath(Branch branch,Box box,boolean parentDirection,Arrow.ArrowSource source,Arrow.ArrowSink sink){

            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;


            Arrow parentArrow=branch.parent;
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
            String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
            //startDirections.add(MathCalc.getInverseDirection(direction));
            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());


            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
            startDirections.add(direction);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));


            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            return path;
        }

        //Строит путь по ArrowSource и ArrowSink
        //Может быть использован в методе moveArrowSink
        public ArrayList<PointF> buildPath(Branch branch,Box box,Arrow.ArrowSource source,Arrow.ArrowSink sink){
            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            //ArrayList<String> startDirections=MathCalc.getDirectionsByBranch(branch);

            Arrow parentArrow=branch.parent;
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);

            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }

            //Ближайшее направление до endPoint с учетом текущей ориентации
            String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint));
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
            startDirections.add(closestDirection);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            return path;
        }


        //Строит путь по newArrow
        //Построение пути с использованием Branch
        public void buildPath(Arrow arrow,Border border){
            PointF divPoint=newArrow.getSource().getConnectorPoint();
            boolean defaultBuild=true;
            //Проверяем если выбрана узловая точка arrow
            if(arrow.isNodalPoint(divPoint)){
                defaultBuild=false;
            }

            Branch branch=new Branch();
            ArrayList<Arrow> arrowList = divArrowByBranch(arrow,branch,divPoint);
            if (arrowList != null) {

                newArrow.source.setConnectorSource(branch);
                Arrow newChildArrow=null;
                if(defaultBuild!=true){
                    newChildArrow = buildPath(branch, border, true);
                }else {
                    newChildArrow = buildPath(branch, border);
                }
                branch.addChild(newChildArrow);
                addBranch(branch);
            }
        }

        //Строит путь по newArrow, если была выбрана узловая точка
        public Arrow buildPath(Branch branch,Border border,boolean parentDirection){

                ArrayList<PointF> path=new ArrayList<PointF>();
                Arrow.ArrowSource source=newArrow.getSource();
                Arrow.ArrowSink sink=newArrow.getSink();

                PointF sourcePoint=source.getConnectorPoint();
                PointF sinkPoint=sink.getConnectorPoint();
                PointF firstPoint=sourcePoint;
                PointF lastPoint=sinkPoint;
                PointF startPoint=firstPoint;
                PointF endPoint=lastPoint;

                Arrow parentArrow=branch.parent;
                PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
                PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
                String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
                //startDirections.add(MathCalc.getInverseDirection(direction));

                startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
                ArrayList<String> startDirections=new ArrayList<String>();
                ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
                startDirections.add(direction);
                startDirections.add(nextDirections.remove(0));
                startDirections.add(nextDirections.remove(0));

                //Создание дополнительной точки перед Border
                endPoint=toArrowWorkSpace(MathCalc.createBorderPrevPoint(endPoint,border));

                RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

                path.add(firstPoint);
                if(startPoint!=firstPoint){
                    path.add(startPoint);
                }

                ArrayList<PointF> newPath=new ArrayList<PointF>();
                //Построение прямолинейного пути
                newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections);

                path.addAll(newPath);
                path.add(endPoint);
                if(endPoint!=lastPoint){
                    path.add(lastPoint);
                }
                path=removeExcessPoints(path);
                //path=MathCalc.screenPointsToDoc(path);

                newArrow.number=newArrowNumber();
                Arrow arrow=new Arrow(newArrow);
                arrow.setArrowCoord(path);
                addArrow(arrow);
                return arrow;
        }

        public Arrow buildPath(Branch branch,Border border){
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            //Ближайшее направление до endPoint с учетом текущей ориентации
            ArrayList<String> startDirections=MathCalc.getDirectionsByBranch(branch);

            //Создание дополнительной точки перед Border
            endPoint=toArrowWorkSpace(MathCalc.createBorderPrevPoint(endPoint,border));

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);


            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }


        //Строит путь по ArrowSource и ArrowSink, от Box к Branch
        //Может быть использован в методе moveArrowSource
        //Будем строить путь от Branch к Box, затем нужно будет использовать метод invertPath
        public ArrayList<PointF> buildPath(Box box,Branch branch,Arrow.ArrowSource source,Arrow.ArrowSink sink,String firstDirection){
            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            ArrayList<String> startDirections=new ArrayList<String>();
            String lastDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint,firstDirection));
            startDirections.add(firstDirection);
            startDirections.add(MathCalc.nextDirections(firstDirection).get(0));
            startDirections.add(MathCalc.nextDirections(firstDirection).get(1));

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if (startPoint != firstPoint) {
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections, box);
            path.addAll(newPath);

            if (endPoint != lastPoint) {
                path.add(endPoint);
            }
            path.add(lastPoint);

            path=removeExcessPoints(path);
            path=MathCalc.invertPath(path);

            PointF newSink=path.get(path.size()-1);
            PointF newPrevSink=path.get(path.size()-2);
            if(newSink.x!=newPrevSink.x && newSink.y!=newPrevSink.y){
                Log.w("path error","true");
            }

            return path;
        }

        //Строит путь по ArrowSource и ArrowSink, от Join к Box
        //Может быть использован в методе moveArrowSink
        //Будем строить путь от Join к Box.
        public ArrayList<PointF> buildPath(Join join,Box box,Arrow.ArrowSource source,Arrow.ArrowSink sink,String firstDirection){
            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            ArrayList<String> startDirections=new ArrayList<String>();
            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint,firstDirection));
            startDirections.add(firstDirection);
            startDirections.add(MathCalc.nextDirections(firstDirection).get(0));
            startDirections.add(MathCalc.nextDirections(firstDirection).get(1));

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if (startPoint != firstPoint) {
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections, box);
            path.addAll(newPath);

            if (endPoint != lastPoint) {
                path.add(endPoint);
            }
            path.add(lastPoint);
            path=removeExcessPoints(path);

            return path;
        }

        //Строит путь по newArrow
        //Построение пути с использованием Join
        public void buildPath(Box box,Arrow arrow){
            PointF divPoint=newArrow.getSink().getConnectorPoint();
            boolean defaultBuild=true;
            //Проверяем если выбрана узловая точка arrow
            if(arrow.isNodalPoint(divPoint)){
                defaultBuild=false;
            }

            Join join = new Join();
            ArrayList<Arrow> arrowList = divArrowByJoin(arrow, join, divPoint);
            if (arrowList != null) {

                newArrow.sink.setConnectorSink(join);
                Arrow newParentArrow=null;
                if(defaultBuild!=true){
                    newParentArrow = buildPath(box, join, true);
                }else {
                    newParentArrow = buildPath(box,join);
                }
                join.addParent(newParentArrow);
                addJoin(join);
            }
        }


        //Строит путь по newArrow, если была выбрана узловая точка
        //Путь в отличии от Branch -> Box, будет строиться от Join к Box
        public Arrow buildPath(Box box,Join join,boolean nodeSelect){
            //Путь строится от join к box
            //В конце построенный путь должен быть инвертирован
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
            String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
            //startDirections.add(MathCalc.getInverseDirection(direction));
            String lastDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());

            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
            startDirections.add(direction);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }

        //Строит путь по newArrow
        public Arrow buildPath(Box box,Join join){
            //Путь строится от join к box
            //В конце построенный путь должен быть инвертирован

            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);

            //Ближайшее направление до endPoint с учетом текущей ориентации
            String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint));
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
            startDirections.add(closestDirection);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            String lastDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }

        //Строит путь по ArrowSource и ArrowSink
        //Может быть использован в методе moveArrowSource
        //Если была выбрана узловая точка
        //Путь в отличии от Branch -> Box, будет строиться от Join к Box
        public ArrayList<PointF> buildPath(Box box,Join join,boolean nodeSelect,Arrow.ArrowSource source,Arrow.ArrowSink sink){
            //Путь строится от join к box
            //В конце построенный путь должен быть инвертирован
            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
            String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
            //startDirections.add(MathCalc.getInverseDirection(direction));
            String lastDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());

            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
            startDirections.add(direction);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            return path;
        }


        //Строит путь по ArrowSource и ArrowSink
        //Может быть использован в методе moveArrowSource
        public ArrayList<PointF> buildPath(Box box,Join join,Arrow.ArrowSource source,Arrow.ArrowSink sink){
            //Путь строится от join к box
            //В конце построенный путь должен быть инвертирован

            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);

            //Ближайшее направление до endPoint с учетом текущей ориентации
            String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint));
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
            startDirections.add(closestDirection);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            String lastDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());

            ArrayList<PointF> pointList=box.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea1, endPoint, lastDir);
            }


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections,box);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            return path;
        }


        //Строит путь по newArrow
        //Построение пути с использованием Join
        public void buildPath(Border border,Arrow arrow){
            PointF divPoint=newArrow.getSink().getConnectorPoint();
            boolean defaultBuild=true;
            //Проверяем если выбрана узловая точка arrow
            if(arrow.isNodalPoint(divPoint)){
                defaultBuild=false;
            }

            Join join = new Join();
            ArrayList<Arrow> arrowList = divArrowByJoin(arrow, join, divPoint);
            if (arrowList != null) {

                newArrow.sink.setConnectorSink(join);
                Arrow newParentArrow=null;
                if(defaultBuild!=true){
                    newParentArrow = buildPath(border, join, true);
                }else {
                    newParentArrow = buildPath(border,join);
                }
                join.addParent(newParentArrow);
                addJoin(join);
            }
        }

        //Строит путь по newArrow, если была выбрана узловая точка
        //Путь в отличии от Branch -> Border, будет строиться от Join к Border
        public Arrow buildPath(Border border,Join join,boolean nodeSelect){
            //Путь строится от join к border
            //В конце построенный путь должен быть инвертирован
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
            String direction=MathCalc.getDirectionByPoints(parentPrevSinkPoint,parentSinkPoint);
            //startDirections.add(MathCalc.getInverseDirection(direction));

            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, direction));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(direction);
            startDirections.add(direction);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            //Создание дополнительной точки перед Border
            endPoint=toArrowWorkSpace(MathCalc.createBorderPrevPoint(endPoint,border));

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }


        //Строит путь по newArrow, если была выбрана узловая точка
        //Путь в отличии от Branch -> Border, будет строиться от Join к Border
        public Arrow buildPath(Border border,Join join){
            //Путь строится от join к border
            //В конце построенный путь должен быть инвертирован

            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sinkPoint;
            PointF lastPoint=sourcePoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            Arrow parentArrow=join.parentList.get(0);
            PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
            PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);

            //Ближайшее направление до endPoint с учетом текущей ориентации
            String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint));
            startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
            ArrayList<String> startDirections=new ArrayList<String>();
            ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
            startDirections.add(closestDirection);
            startDirections.add(nextDirections.remove(0));
            startDirections.add(nextDirections.remove(0));

            //Создание дополнительной точки перед Border
            endPoint=toArrowWorkSpace(MathCalc.createBorderPrevPoint(endPoint,border));

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //Возвращает точки пути в обратном порядке
            path=MathCalc.invertPath(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }


        //Строит путь по newArrow
        //Путь будет построен от branch к join
        public void buildPath(Arrow arrow1,Arrow arrow2){
            PointF branchDivPoint=newArrow.getSource().getConnectorPoint();
            PointF joinDivPoint=newArrow.getSink().getConnectorPoint();
            boolean branchParentDirection=false;
            boolean joinParentDirection=false;
            //Проверяем если выбрана узловая точка arrow1
            if(arrow1.isNodalPoint(branchDivPoint)){
                branchParentDirection=true;
            }

            //Проверяем если выбрана узловая точка arrow2
            if(arrow2.isNodalPoint(joinDivPoint)){
                joinParentDirection=true;
            }

            Branch branch = new Branch();
            Join join = new Join();
            ArrayList<Arrow> arrowList1 = divArrowByBranch(arrow1, branch, branchDivPoint);
            ArrayList<Arrow> arrowList2 = divArrowByJoin(arrow2, join, joinDivPoint);
            if (arrowList1 != null) {

                newArrow.sink.setConnectorSink(join);
                Arrow newParentArrow = buildPath(branch, join, branchParentDirection,joinParentDirection);

                branch.addChild(newParentArrow);
                addBranch(branch);
                join.addParent(newParentArrow);
                addJoin(join);
            }
        }

        //Строит путь по newArrow, от branch к join
        //При установки true логическим параметрам, выбирает точки в направлении parent
        public Arrow buildPath(Branch branch,Join join,boolean branchParentDirection,boolean joinParentDirection){

            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;


            Arrow branchParentArrow=branch.parent;
            PointF branchParentPrevSinkPoint=branchParentArrow.coord.path.get(branchParentArrow.coord.path.size()-2);
            PointF branchParentSinkPoint=branchParentArrow.coord.path.get(branchParentArrow.coord.path.size()-1);
            String branchDirection=MathCalc.getDirectionByPoints(branchParentPrevSinkPoint,branchParentSinkPoint);

            Arrow joinParentArrow=join.parentList.get(0);
            PointF joinParentPrevSinkPoint=joinParentArrow.coord.path.get(joinParentArrow.coord.path.size()-2);
            PointF joinParentSinkPoint=joinParentArrow.coord.path.get(joinParentArrow.coord.path.size()-1);
            String joinDirection=MathCalc.getDirectionByPoints(joinParentPrevSinkPoint,joinParentSinkPoint);

            ArrayList<String> startDirections = new ArrayList<String>();

            if(branchParentDirection){
                startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, branchDirection));
                ArrayList<String> nextDirections=MathCalc.nextDirections(branchDirection);
                //Продолжаем строить путь в начальном направлении,
                //если будет выполнено условие
                if(branchDirection.equals(joinDirection) || branchDirection.equals(MathCalc.getInverseDirection(joinDirection))) {
                    startDirections.add(branchDirection);
                }
                startDirections.add(nextDirections.remove(0));
                startDirections.add(nextDirections.remove(0));
            }else{
                String closestDirection=MathCalc.getClosestDirection(startPoint,endPoint,MathCalc.getOrientationByPoints(branchParentPrevSinkPoint,branchParentSinkPoint));
                startPoint=toArrowWorkSpace(MathCalc.getExtraPoint(startPoint, closestDirection));
                ArrayList<String> nextDirections=MathCalc.nextDirections(closestDirection);
                if(closestDirection.equals(joinDirection) || closestDirection.equals(MathCalc.getInverseDirection(joinDirection))) {
                    startDirections.add(closestDirection);
                }
                startDirections.add(nextDirections.remove(0));
                startDirections.add(nextDirections.remove(0));
            }

            if(joinParentDirection){
                endPoint=toArrowWorkSpace(MathCalc.getExtraPoint(endPoint, joinDirection));
            }else{
                //Ближайшее направление до startPoint с учетом текущей ориентации
                String closestDirection=MathCalc.getClosestDirection(endPoint,startPoint,MathCalc.getOrientationByPoints(joinParentPrevSinkPoint,joinParentSinkPoint));
                endPoint=toArrowWorkSpace(MathCalc.getExtraPoint(endPoint, closestDirection));
            }

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);


            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint, endPoint, plotArea, startDirections);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
            return arrow;
        }


        public void buildSquigglePath(){
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            Arrow.ArrowLabel label=new Arrow.ArrowLabel();
            Arrow arrow=new Arrow();
            PointF arrowPoint=new PointF();
            if(source.getConnectorSource() instanceof Arrow.ArrowLabel){
                label=(Arrow.ArrowLabel)source.getConnectorSource();
            }else{
                arrow=(Arrow)source.getConnectorSource();
                arrowPoint=source.getConnectorPoint();
            }

            if(sink.getConnectorSink() instanceof Arrow.ArrowLabel){
                label=(Arrow.ArrowLabel)sink.getConnectorSink();
            }else{
                arrow=(Arrow)sink.getConnectorSink();
                arrowPoint=sink.getConnectorPoint();
            }

            if(arrow.label!=label){
                return;
            }

            PointF labelPoint=new PointF();
            PointF labelCenter=label.getCenterPoint();
            ArrayList<PointF> pointList=MathCalc.labelIntersectionPoints(arrowPoint,labelCenter,label);
            if(pointList.size()!=0){
                labelPoint=pointList.get(0);
            }

            path.add(labelPoint);
            path.add(arrowPoint);
            //path=MathCalc.screenPointsToDoc(path);

            Arrow.ArrowSquiggle squiggle=new Arrow.ArrowSquiggle();
            squiggle.setLabelPoint(path.remove(0));
            squiggle.setArrowPoint(path.remove(0));
            arrow.addSquiggle(squiggle);
        }

        //Строит путь по NewArrow
        public void buildPath(Box box1,Box box2){
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            /*
            String firstDir=MathCalc.getDirectionByBoxSide(startPoint,box1);
            String lastDir=MathCalc.getDirectionByBoxSide(endPoint,box2);
            */
            String firstDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());
            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());

            ArrayList<String> startDirections=MathCalc.nextDirections(firstDir);
            startDirections.add(0,firstDir);

            ArrayList<PointF> pointList=box1.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                startPoint=MathCalc.getPointByDirection(plotArea1,startPoint,firstDir);
            }

            pointList=box2.coord.getPointList();
            if(pointList!=null){
                RectF plotArea2=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                endPoint=MathCalc.getPointByDirection(plotArea2,endPoint,lastDir);
            }


            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(firstPoint,lastPoint,box1);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            pointList=MathCalc.boxIntersection(startPoint,endPoint,box2);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }


            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //Построение прямолинейного пути
            newPath=buildRectPath(startPoint,endPoint,plotArea,startDirections,box2);

            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);
        }

        //Функция для построения прямоугольного пути.
        //Часто вызывается в методах buildPath
        public ArrayList<PointF> buildRectPath(PointF startPoint,PointF endPoint,RectF plotArea,ArrayList<String> startDirections,Box box){
            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //В случае построения прямоугольного пути, к объекту Box,
            //нужно избегать пересечение Box'а, для остальных объектов этого делать не нужно

            ArrayList<PointF> pointList=new ArrayList<PointF>();
            PointF currentPoint=startPoint;
            int counter1=0;
            int counter2=0;
            while(startDirections.size()!=0) {
                ArrayList<String> currentDirections=startDirections;
                String direction;
                while ((currentPoint.x != endPoint.x) && (currentPoint.y != endPoint.y)) {
                    Log.w("Circle", "true");

                    if(currentDirections.size()==0){
                        break;
                    }else {
                        direction = currentDirections.get(0);
                    }
                    PointF newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                    pointList=MathCalc.boxIntersection(newPoint,currentPoint,box);
                    if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) && (pointList==null)) {
                        newPath.add(newPoint);
                        currentPoint = newPoint;
                        currentDirections = MathCalc.nextDirections(direction);
                    } else {
                        currentDirections.remove(0);
                        if (currentDirections.size() == 0) {
                            currentPoint=startPoint;
                            newPath.clear();
                            if(startDirections.size()!=0) {
                                startDirections.remove(0);
                                currentDirections=startDirections;
                                continue;
                            }
                            break;
                        }
                    }

                    if(counter1>N){
                        break;
                    }
                    counter1++;
                }

                pointList=MathCalc.boxIntersection(currentPoint,endPoint,box);
                if(pointList!=null) {
                    if (startDirections.size() != 0) {
                        startDirections.remove(0);
                        currentPoint = startPoint;
                        newPath.clear();
                    }
                }else if((currentPoint.x == endPoint.x) || (currentPoint.y == endPoint.y)){
                    break;
                }

                if(counter2>N){
                    break;
                }
                counter2++;
            }

            return newPath;
        }


        //Функция для построения прямоугольного пути.
        //Часто вызывается в методах buildPath
        //Если прямолинейный путь в обход box не может быть построен,
        //то строит другой путь
        public ArrayList<PointF> buildUniversalPath(PointF startPoint,PointF endPoint,RectF plotArea,ArrayList<String> startDirections,Box box){
            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //В случае построения прямоугольного пути, к объекту Box,
            //нужно избегать пересечение Box'а, для остальных объектов этого делать не нужно
            ArrayList<String> cloneStartDirections=(ArrayList<String>)startDirections.clone();

            if(startPoint.x!=endPoint.x && startPoint.y!=endPoint.y) {
                ArrayList<PointF> pointList=new ArrayList<PointF>();
                PointF currentPoint=startPoint;
                int counter1=0;
                int counter2=0;

                while (startDirections.size() != 0) {
                    ArrayList<String> currentDirections = startDirections;
                    String direction;
                    while ((currentPoint.x != endPoint.x) && (currentPoint.y != endPoint.y)) {
                        Log.w("Circle", "true");

                        if (currentDirections.size() == 0) {
                            break;
                        } else {
                            direction = currentDirections.get(0);
                        }
                        PointF newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                        pointList = MathCalc.boxIntersection(newPoint, currentPoint, box);
                        if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) && (pointList == null)) {
                            newPath.add(newPoint);
                            currentPoint = newPoint;
                            currentDirections = MathCalc.nextDirections(direction);
                        } else {
                            currentDirections.remove(0);
                            if (currentDirections.size() == 0) {
                                currentPoint = startPoint;
                                newPath.clear();
                                if (startDirections.size() != 0) {
                                    startDirections.remove(0);
                                    currentDirections = startDirections;
                                    continue;
                                }
                                break;
                            }
                        }

                        if (counter1 > N) {
                            break;
                        }
                        counter1++;
                    }

                    pointList = MathCalc.boxIntersection(currentPoint, endPoint, box);
                    if (pointList != null) {
                        if (startDirections.size() != 0) {
                            startDirections.remove(0);
                            currentPoint = startPoint;
                            newPath.clear();
                        }
                    } else if ((currentPoint.x == endPoint.x) || (currentPoint.y == endPoint.y)) {
                        break;
                    }

                    if (counter2 > N) {
                        break;
                    }
                    counter2++;
                }
            }
            newPath=removeExcessPoints(newPath);

            if(newPath.size()==0 || (!MathCalc.isCorrectPath(newPath))){
                newPath.clear();
                newPath=buildRectPath(startPoint,endPoint,plotArea,cloneStartDirections);
                newPath=removeExcessPoints(newPath);
            }

            return newPath;
        }


        //Строит обходной путь вокруг Box
        //от точки boxPoint к точке outPoint
        //outPoint не входит в создаваемый путь
        public ArrayList<PointF> buildBypassPath(PointF boxPoint,PointF outPoint,Box box,String firstDir){
            ArrayList<PointF> path=null;
            if(MathCalc.projPointOnBox(outPoint, box)!=null){
                PointF firstPoint=boxPoint;
                PointF startPoint=firstPoint;

                PointF projPoint=null;
                RectF plotArea=null;

                ArrayList<PointF> pointList=box.coord.getPointList();
                if(pointList!=null) {
                    plotArea=MathCalc.buildPlotAreaMore(pointList.remove(0), pointList.remove(0));
                    startPoint=MathCalc.getPointByDirection(plotArea, startPoint, firstDir);

                    projPoint=MathCalc.projPointOnPlotArea(outPoint,plotArea);
                }

                PointF endPoint=projPoint;
                path=new ArrayList<PointF>();

                ArrayList<String> startDirections=MathCalc.nextDirections(firstDir);
                ArrayList<PointF> newPath=buildRectPath(startPoint,endPoint,plotArea,startDirections,box);

                path.add(firstPoint);
                path.add(startPoint);
                path.addAll(newPath);
                path.add(projPoint);

                path=removeExcessPoints(path);
            }
            return path;
        }


        //Строит прямолинейный путь
        public ArrayList<PointF> buildRectPath(PointF startPoint,PointF endPoint,RectF plotArea,ArrayList<String> startDirections){
            ArrayList<PointF> newPath=new ArrayList<PointF>();
            //В случае построения прямоугольного пути, к объекту Box,
            //нужно избегать пересечение Box'а, для остальных объектов этого делать не нужно
            PointF currentPoint=startPoint;
            int counter1=0;
            int counter2=0;
            while(startDirections.size()!=0) {
                ArrayList<String> currentDirections=startDirections;
                String direction;
                while ((currentPoint.x != endPoint.x) && (currentPoint.y != endPoint.y)) {
                    Log.w("Circle", "true");

                    if(currentDirections.size()==0){
                        break;
                    }else {
                        direction = currentDirections.get(0);
                    }
                    PointF newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                    if ((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) {
                        newPath.add(newPoint);
                        currentPoint = newPoint;
                        currentDirections = MathCalc.nextDirections(direction);
                    } else {
                        currentDirections.remove(0);
                        if (currentDirections.size() == 0) {
                            currentPoint=startPoint;
                            newPath.clear();
                            if(startDirections.size()!=0) {
                                startDirections.remove(0);
                                currentDirections=startDirections;
                                continue;
                            }
                            break;
                        }
                    }

                    if(counter1>N){
                        break;
                    }
                    counter1++;
                }

                if((currentPoint.x == endPoint.x) || (currentPoint.y == endPoint.y)){
                    break;
                }

                if(counter2>N){
                    break;
                }
                counter2++;
            }
            return newPath;
        }

         //Строит путь по NewArrow + addArrowChild
         public Arrow buildPath(Box box1,Box box2,boolean addArrowChild){
             ArrayList<PointF> path=new ArrayList<PointF>();
             Arrow.ArrowSource source=newArrow.getSource();
             Arrow.ArrowSink sink=newArrow.getSink();

             PointF sourcePoint=source.getConnectorPoint();
             PointF sinkPoint=sink.getConnectorPoint();
             PointF firstPoint=sourcePoint;
             PointF lastPoint=sinkPoint;
             PointF startPoint=firstPoint;
             PointF endPoint=lastPoint;

            /*
            String firstDir=MathCalc.getDirectionByBoxSide(startPoint,box1);
            String lastDir=MathCalc.getDirectionByBoxSide(endPoint,box2);
            */
             String firstDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());
             String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());

             ArrayList<String> startDirections=MathCalc.nextDirections(firstDir);
             startDirections.add(0,firstDir);

             ArrayList<PointF> pointList=box1.coord.getPointList();
             if(pointList!=null){
                 RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                 startPoint=MathCalc.getPointByDirection(plotArea1,startPoint,firstDir);
             }

             pointList=box2.coord.getPointList();
             if(pointList!=null){
                 RectF plotArea2=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                 endPoint=MathCalc.getPointByDirection(plotArea2,endPoint,lastDir);
             }


             RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

             pointList=MathCalc.boxIntersection(firstPoint,lastPoint,box1);
             if(pointList!=null){
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
             }

             pointList=MathCalc.boxIntersection(startPoint,endPoint,box2);
             if(pointList!=null){
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                 plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
             }


             path.add(firstPoint);
             if(startPoint!=firstPoint){
                 path.add(startPoint);
             }

             ArrayList<PointF> newPath=new ArrayList<PointF>();
             newPath=buildRectPath(startPoint,endPoint,plotArea,startDirections,box2);
             path.addAll(newPath);
             path.add(endPoint);
             if(endPoint!=lastPoint){
                 path.add(lastPoint);
             }
             path=removeExcessPoints(path);
             //path=MathCalc.screenPointsToDoc(path);

             newArrow.number=newArrowNumber();
             Arrow arrow=new Arrow(newArrow);
             arrow.setArrowCoord(path);
             //addArrow(arrow,addArrowChild);
             return arrow;
         }


        //Строит путь по NewArrow + addChildArrow
        public Arrow buildPath(Box box,Border border,boolean addChildArrow){
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=new PointF(lastPoint.x,startPoint.y);

            path.add(startPoint);
            path.add(endPoint);

            //path=MathCalc.screenPointsToDoc(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            //addArrow(arrow,addChildArrow);
            return arrow;
        }

         //Строит путь по NewArrow
         public void buildPath(Box box,Border border){
             ArrayList<PointF> path=new ArrayList<PointF>();
             Arrow.ArrowSource source=newArrow.getSource();
             Arrow.ArrowSink sink=newArrow.getSink();

             PointF sourcePoint=source.getConnectorPoint();
             PointF sinkPoint=sink.getConnectorPoint();
             PointF firstPoint=sourcePoint;
             PointF lastPoint=sinkPoint;
             PointF startPoint=firstPoint;
             PointF endPoint=new PointF(lastPoint.x,startPoint.y);

             path.add(startPoint);
             path.add(endPoint);

             //path=MathCalc.screenPointsToDoc(path);

             newArrow.number=newArrowNumber();
             Arrow arrow=new Arrow(newArrow);
             arrow.setArrowCoord(path);
             addArrow(arrow);
         }

        //Строит путь по NewArrow
        public void buildPath(Border border,Box box){
            ArrayList<PointF> path=new ArrayList<PointF>();
            ArrayList<PointF> newPath=new ArrayList<PointF>();
            Arrow.ArrowSource source=newArrow.getSource();
            Arrow.ArrowSink sink=newArrow.getSink();

            PointF sourcePoint=source.getConnectorPoint();
            PointF sinkPoint=sink.getConnectorPoint();
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            String firstDir=MathCalc.getDirectionByBorder(border);
            //String lastDir=MathCalc.getDirectionByBoxSide(endPoint,box);
            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());
            ArrayList<String> startDirections=new ArrayList<String>();
            startDirections.add(firstDir);


            if(border.type.equals("I") && sink.getConnectorType().equals("I")){
                startPoint=new PointF(startPoint.x,endPoint.y);
                path.add(startPoint);
                path.add(endPoint);
                //path=MathCalc.screenPointsToDoc(path);

                Arrow arrow=new Arrow(newArrow);
                arrow.setArrowCoord(path);
                addArrow(arrow);
                return;
            }else{
                RectF plotArea=new RectF();
                RectF plotArea1=new RectF();
                ArrayList<PointF> pointList=box.coord.getPointList();
                if(pointList!=null){
                    plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                    endPoint=MathCalc.getPointByDirection(plotArea1,endPoint,lastDir);
                }

                if(MathCalc.boxIntersection(startPoint,lastPoint,box)!=null){
                    if((MathCalc.pointInBoxRangeY(startPoint,box) || MathCalc.pointInBoxRangeX(startPoint,box))){
                       startPoint=MathCalc.newPointsToPlotAreaLimit(plotArea1,startPoint);
                       plotArea=plotArea1;
                       startDirections=MathCalc.nextDirections(firstDir);
                    }else{
                       plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
                       pointList=box.coord.getAllPointList();
                       if(pointList!=null){
                           plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                           plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                           plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                           plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                       }
                    }
                }else{
                    plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
                }

                //Построение
                path.add(firstPoint);
                if(startPoint!=firstPoint){
                    path.add(startPoint);
                }

                newPath=buildRectPath(startPoint,endPoint,plotArea,startDirections,box);
            }

            path.addAll(newPath);
            path.add(endPoint);
            if(lastPoint!=endPoint){
                path.add(lastPoint);
            }
            path=removeExcessPoints(path);
            //path=MathCalc.screenPointsToDoc(path);

            newArrow.number=newArrowNumber();
            Arrow arrow=new Arrow(newArrow);
            arrow.setArrowCoord(path);
            addArrow(arrow);

        }



         //Строит путь по NewArrow + addChildArrow
         public Arrow buildPath(Border border,Box box,boolean addChildArrow){
             ArrayList<PointF> path=new ArrayList<PointF>();
             ArrayList<PointF> newPath=new ArrayList<PointF>();
             Arrow.ArrowSource source=newArrow.getSource();
             Arrow.ArrowSink sink=newArrow.getSink();

             PointF sourcePoint=source.getConnectorPoint();
             PointF sinkPoint=sink.getConnectorPoint();
             PointF firstPoint=sourcePoint;
             PointF lastPoint=sinkPoint;
             PointF startPoint=firstPoint;
             PointF endPoint=lastPoint;

             String firstDir=MathCalc.getDirectionByBorder(border);
             //String lastDir=MathCalc.getDirectionByBoxSide(endPoint,box);
             String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());
             ArrayList<String> startDirections=new ArrayList<String>();
             startDirections.add(firstDir);


             if(border.type.equals("I") && sink.getConnectorType().equals("I")){
                 startPoint=new PointF(startPoint.x,endPoint.y);
                 path.add(startPoint);
                 path.add(endPoint);
                 //path=MathCalc.screenPointsToDoc(path);

                 Arrow arrow=new Arrow(newArrow);
                 arrow.setArrowCoord(path);
                 //addArrow(arrow);
                 return arrow;
             }else{
                 RectF plotArea=new RectF();
                 RectF plotArea1=new RectF();
                 ArrayList<PointF> pointList=box.coord.getPointList();
                 if(pointList!=null){
                     plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                     endPoint=MathCalc.getPointByDirection(plotArea1,endPoint,lastDir);
                 }

                 if(MathCalc.boxIntersection(startPoint,lastPoint,box)!=null){
                     if((MathCalc.pointInBoxRangeY(startPoint,box) || MathCalc.pointInBoxRangeX(startPoint,box))){
                         startPoint=MathCalc.newPointsToPlotAreaLimit(plotArea1,startPoint);
                         plotArea=plotArea1;
                         startDirections=MathCalc.nextDirections(firstDir);
                     }else{
                         plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
                         pointList=box.coord.getAllPointList();
                         if(pointList!=null){
                             plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                             plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                             plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                             plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                         }
                     }
                 }else{
                     plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
                 }

                 //Построение
                 path.add(firstPoint);
                 if(startPoint!=firstPoint){
                     path.add(startPoint);
                 }

                 newPath=buildRectPath(startPoint,endPoint,plotArea,startDirections,box);
             }

             path.addAll(newPath);
             path.add(endPoint);
             if(lastPoint!=endPoint){
                 path.add(lastPoint);
             }
             path=removeExcessPoints(path);
             //path=MathCalc.screenPointsToDoc(path);

             newArrow.number=newArrowNumber();
             Arrow arrow=new Arrow(newArrow);
             arrow.setArrowCoord(path);
             return arrow;
             //addArrow(arrow,addChildArrow);
         }


        public void moveArrowSource(Arrow arrow,float x,float y){
            Object obj1=arrow.source.getConnectorSource();
            Object obj2=arrow.sink.getConnectorSink();
            if(obj1 instanceof Box && obj2 instanceof  Box) {
                if(obj1==obj2){
                    moveArrowSource(arrow, (Box) obj1, x, y);
                }else {
                    moveArrowSource(arrow, (Box) obj1, (Box) obj2, x, y);
                }
            }else if(obj1 instanceof Box && obj2 instanceof  Border){
                moveArrowSource(arrow, (Box)obj1, (Border)obj2, x, y);
            }else if(obj1 instanceof Box && obj2 instanceof  Branch){
                moveArrowSource(arrow, (Box)obj1, (Branch)obj2, x, y);
            }else if(obj1 instanceof Box && obj2 instanceof  Join){
                moveArrowSource(arrow, (Box)obj1, (Join)obj2, x, y);
            }
        }

        public void moveArrowSink(Arrow arrow,float x,float y){
            Object obj1=arrow.source.getConnectorSource();
            Object obj2=arrow.sink.getConnectorSink();
            if(obj1 instanceof Box && obj2 instanceof  Box) {
                moveArrowSink(arrow, (Box)obj1, (Box)obj2, x, y);
            }else if(obj1 instanceof Border && obj2 instanceof  Box){
                moveArrowSink(arrow, (Border)obj1, (Box)obj2, x, y);
            }else if(obj1 instanceof Branch && obj2 instanceof  Box){
                moveArrowSink(arrow, (Branch)obj1, (Box)obj2, x, y);
            }else if(obj1 instanceof Join && obj2 instanceof  Box){
                moveArrowSink(arrow, (Join)obj1, (Box)obj2, x, y);
            }
        }


        public void moveArrowSource(Arrow arrow,Box box,Border border,float x,float y) {
            ArrayList<PointF> path = new ArrayList<PointF>();
            PointF sourcePoint=arrow.source.getConnectorPoint();
            PointF sinkPoint=arrow.sink.getConnectorPoint();

            PointF newSourcePoint=new PointF(sourcePoint.x+x,sourcePoint.y+y);
            PointF startPoint=newSourcePoint;
            PointF endPoint=sinkPoint;

            String firstDir="right";
            ArrayList<String> startDirections=MathCalc.nextDirections(firstDir);

            if(arrow.coord.path.size()==2){
                PointF newSinkPoint=new PointF(sinkPoint.x,sinkPoint.y+y);
                path.add(newSourcePoint);
                path.add(newSinkPoint);
                arrow.setArrowCoord(path);

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    //arrow.squiggle.moveArrowPoint(x,y,'V');
                    arrow.squiggle.newArrowPoint();
                }

                return;
            }else {
                if (Math.abs(newSourcePoint.y - sinkPoint.y) < 5) {
                    PointF newSinkPoint=new PointF(sinkPoint.x,newSourcePoint.y);
                    path.add(newSourcePoint);
                    path.add(newSinkPoint);
                    //arrow.coord.path=screenPointsToDoc(path);
                    arrow.setArrowCoord(path);

                    //Изменение точки соединения Arrow и Squiggle
                    if(arrow.squiggle!=null){
                        arrow.squiggle.newArrowPoint();
                    }

                    return;
                }
            }

            PointF nextSourcePoint=arrow.coord.path.get(1);
            PointF newNextSourcePoint=new PointF(nextSourcePoint.x,nextSourcePoint.y+y);
            PointF nextNextPoint=arrow.coord.path.get(2);

            //Добавляем startPoint
            path.add(startPoint);
            ArrayList<PointF> pointList=box.coord.getPointList();
            RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
            startPoint=MathCalc.getPointByDirection(plotArea1,startPoint,firstDir);

            PointF normVec=MathCalc.getNormVecByDirection("right");
            PointF firstVec=new PointF(newSourcePoint.x-newNextSourcePoint.x,newSourcePoint.y-newNextSourcePoint.y);
            if(MathCalc.isOneDirection(firstVec,normVec)){
                startPoint=newNextSourcePoint;
            }
            //Добавляем новый(сдвинутый) startPoint
            path.add(startPoint);

            if(startPoint.x!=nextNextPoint.x && startPoint.y!=nextNextPoint.y){
                RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
                PointF currentPoint=startPoint;
                ArrayList<String> currentDirections=startDirections;
                ArrayList<PointF> newPath=new ArrayList<PointF>();
                while(currentPoint.x!=endPoint.x && currentPoint.x!=endPoint.y){
                    String direction=currentDirections.get(0);
                    PointF newPoint=MathCalc.getPointByDirection(plotArea,currentPoint,direction);

                    if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y))) {
                        newPath.add(newPoint);
                        currentPoint = newPoint;
                        currentDirections = MathCalc.nextDirections(direction);
                    } else {
                        currentDirections.remove(0);
                        if (currentDirections.size() == 0) {
                            break;
                        }
                    }
                }

                path.addAll(newPath);
                //arrow.coord.path=screenPointsToDoc(path);
                path=removeLoop(path);
                path=removeExcessPoints(path);
                arrow.setArrowCoord(path);

                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }

                return;

            }else {

                for(int i=2;i<arrow.coord.path.size();i++) {
                    path.add(arrow.coord.path.get(i));
                }
                arrow.setArrowCoord(path);

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }


                return;
            }

        }

        public void moveArrowSource(Arrow arrow,Box box1,float x,float y){
            ArrayList<PointF> path=new ArrayList<PointF>();
            Arrow.ArrowSource source=arrow.source;
            Arrow.ArrowSink sink=arrow.sink;

            PointF sourcePoint=new PointF(source.getConnectorPoint().x+x,source.getConnectorPoint().y+y);
            PointF sinkPoint=new PointF(sink.getConnectorPoint().x+x,sink.getConnectorPoint().y+y);
            PointF firstPoint=sourcePoint;
            PointF lastPoint=sinkPoint;
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            /*
            String firstDir=MathCalc.getDirectionByBoxSide(startPoint,box1);
            String lastDir=MathCalc.getDirectionByBoxSide(endPoint,box1);
            */
            String firstDir=MathCalc.getDirectionByBoxConnectorType(source.getConnectorType());
            String lastDir=MathCalc.getDirectionByBoxConnectorType(sink.getConnectorType());
            ArrayList<String> startDirections=MathCalc.nextDirections(firstDir);
            startDirections.add(0,firstDir);

            ArrayList<PointF> pointList=box1.coord.getPointList();
            if(pointList!=null){
                RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));
                startPoint=MathCalc.getPointByDirection(plotArea1,startPoint,firstDir);
                endPoint=MathCalc.getPointByDirection(plotArea1,endPoint,lastDir);
            }

            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);

            pointList=MathCalc.boxIntersection(firstPoint,lastPoint,box1);
            if(pointList!=null){
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
            }

            path.add(firstPoint);
            if(startPoint!=firstPoint){
                path.add(startPoint);
            }

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            PointF currentPoint=startPoint;
            int counter1=0;
            int counter2=0;
            while(startDirections.size()!=0) {
                ArrayList<String> currentDirections=startDirections;
                String direction;
                while ((currentPoint.x != endPoint.x) && (currentPoint.y != endPoint.y)) {
                    Log.w("Circle", "true");

                    if(currentDirections.size()==0){
                        break;
                    }else {
                        direction = currentDirections.get(0);
                    }

                    PointF newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                    pointList=MathCalc.boxIntersection(newPoint,currentPoint,box1);
                    if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) && (pointList==null)) {
                        newPath.add(newPoint);
                        currentPoint = newPoint;
                        currentDirections = MathCalc.nextDirections(direction);
                    } else {
                        currentDirections.remove(0);
                        if (currentDirections.size() == 0) {
                            currentPoint=startPoint;
                            newPath.clear();
                            if(startDirections.size()!=0) {
                                startDirections.remove(0);
                                currentDirections=startDirections;
                                continue;
                            }
                            break;
                        }
                    }

                    if(counter1>N){
                        break;
                    }
                    counter1++;
                }

                pointList=MathCalc.boxIntersection(currentPoint,endPoint,box1);
                if(pointList!=null) {
                    if (startDirections.size() != 0) {
                        startDirections.remove(0);
                        currentPoint = startPoint;
                        newPath.clear();
                    }
                }else if((currentPoint.x == endPoint.x) || (currentPoint.y == endPoint.y)){
                    break;
                }

                if(counter2>N){
                    break;
                }
                counter2++;
            }
            path.addAll(newPath);
            path.add(endPoint);
            if(endPoint!=lastPoint){
                path.add(lastPoint);
            }

            //path=MathCalc.screenPointsToDoc(path);
            arrow.setArrowCoord(path);
            //Arrow arrow=new Arrow(newArrow);
            //arrow.setArrowCoord(path);
            //addArrow(arrow);

            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }

        public void moveArrowSource(Arrow arrow,Box box,Branch branch,float x,float y) {
            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();
            Arrow childArrow=branch.childList.get(0);
            PointF childSourcePoint=childArrow.coord.path.get(0);
            PointF childNextSourcePoint=childArrow.coord.path.get(1);

            String direction=MathCalc.getDirectionByPoints(childNextSourcePoint,childSourcePoint);
            //Новая sourcePoint расположенная на Box
            PointF newSourcePoint = new PointF(sourcePoint.x + x, sourcePoint.y + y);
            //Метод buildPath, будет строить путь по arrow.source и arrow.sink
            arrow.source.setConnectorPoint(newSourcePoint);

            if(direction!=null) {
                arrow.setArrowCoord(buildPath(box, branch,arrow.source,arrow.sink,direction));
            }else{
                arrow.setArrowCoord(buildPath(box, branch,arrow.source,arrow.sink,"left"));
            }

        }

        public void moveArrowSource(Arrow arrow,Box box,Join join,float x,float y) {
            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            //Определим начальные направления,
            //которые зависят от того, находятся ли изначально последние две точки arrow, и последние две точки другого родителя jon
            //в одной ориентации
            boolean joinParentDirection=false;
            PointF prevSinkPoint=arrow.coord.path.get(arrow.coord.path.size() - 2);
            for(Arrow parent : join.parentList){
                if(parent!=arrow){
                    PointF parentSinkPoint=parent.coord.path.get(parent.coord.path.size()-1);
                    PointF parentPrevSinkPoint=parent.coord.path.get(parent.coord.path.size()-2);
                    char orientation=MathCalc.getOrientationByPoints(sinkPoint,prevSinkPoint);
                    char parentOrientation=MathCalc.getOrientationByPoints(parentSinkPoint,parentPrevSinkPoint);
                    if(orientation==parentOrientation){
                        joinParentDirection=true;
                    }
                    break;
                }
            }

            //Новая sourcePoint расположенная на Box
            PointF newSourcePoint = new PointF(sourcePoint.x + x, sourcePoint.y + y);
            //Метод buildPath, будет строить путь по arrow.source и arrow.sink
            arrow.source.setConnectorPoint(newSourcePoint);

            if(joinParentDirection) {
                arrow.setArrowCoord(buildPath(box, join,true,arrow.source,arrow.sink));
            }else{
                arrow.setArrowCoord(buildPath(box, join,arrow.source,arrow.sink));
            }

        }

        public void moveArrowSource(Arrow arrow,Box box1,Box box2,float x,float y) {
            ArrayList<PointF> path = new ArrayList<PointF>();
            PointF sourcePoint = arrow.source.getConnectorPoint();
            Log.w("Read sourcePoint", sourcePoint.toString());
            PointF newSourcePoint = new PointF(sourcePoint.x + x, sourcePoint.y + y);
            PointF firstPoint = newSourcePoint;

            PointF nextSourcePoint = arrow.coord.path.get(1);
            PointF newNextSourcePoint = new PointF(nextSourcePoint.x, firstPoint.y);

            PointF sinkPoint = arrow.sink.getConnectorPoint();
            PointF lastPoint = sinkPoint;
            //PointF lastPoint=new PointF();

            if (arrow.coord.path.size() == 2) {
                lastPoint = newNextSourcePoint;
            }

            //RectF plotArea=buildPlotArea(firstPoint,lastPoint);
            //String firstDir=MathCalc.getDirectionByBoxSide(firstPoint,box1);
            String firstDir = MathCalc.getDirectionByBoxConnectorType(arrow.source.getConnectorType());
            if (firstDir == null) {
                Log.w("Direction", "null");
            }
            //String lastDir=MathCalc.getDirectionByBoxSide(lastPoint,box2);
            String lastDir = MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());

            PointF startPoint = firstPoint;
            PointF endPoint = lastPoint;

            ArrayList<PointF> pointList = box1.coord.getPointList();
            RectF plotArea1 = MathCalc.buildPlotAreaMore(pointList.remove(0), pointList.remove(0));

            //xxx
            PointF newPoint = MathCalc.getPointByDirection(plotArea1, startPoint, firstDir);
            if (!MathCalc.pointInBox(newPoint, box2)) {
                startPoint = newPoint;
            }


            pointList = box2.coord.getPointList();
            RectF plotArea2 = MathCalc.buildPlotAreaMore(pointList.remove(0), pointList.remove(0));

            //xxx
            newPoint = MathCalc.getPointByDirection(plotArea2, endPoint, lastDir);
            if (!MathCalc.pointInBox(newPoint, box1)) {
                endPoint = newPoint;
            }

            PointF normFirstVec = MathCalc.getNormVecByDirection(firstDir);
            PointF firstVec = new PointF(newNextSourcePoint.x - firstPoint.x, newNextSourcePoint.y - firstPoint.y);

            ArrayList<String> startDirections = new ArrayList<String>();

            if (MathCalc.isOneDirection(firstVec, normFirstVec)) {
                pointList = MathCalc.boxIntersection(startPoint, newNextSourcePoint, box2);
                if (pointList == null) {
                    startPoint = newNextSourcePoint;
                } else {
                    Log.w("Old startPoint", "true");
                }
            }
            startDirections = MathCalc.nextDirections(firstDir);

            RectF plotArea = MathCalc.buildPlotArea(startPoint, endPoint);
            boolean intersection = false;
            pointList = MathCalc.boxIntersection(startPoint, endPoint, box1);
            if (pointList != null) {
                Log.w("Intersection1", "true");
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                intersection = true;
            }

            pointList = MathCalc.boxIntersection(startPoint, endPoint, box2);
            if (pointList != null) {
                Log.w("Intersection2", "true");
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                intersection = true;
            }


            if (arrow.sink.type.equals("I") && (!intersection)) {
                if (plotArea.height() <= 5 || arrow.coord.path.size() == 2) {
                    newPoint = new PointF(lastPoint.x, firstPoint.y);
                    if (MathCalc.pointInBoxRangeY(newPoint, box2)) {
                        path.add(firstPoint);
                        path.add(newPoint);
                        arrow.setArrowCoord(path);

                        //Изменение точки соединения Arrow и Squiggle
                        if (arrow.squiggle != null) {
                            arrow.squiggle.newArrowPoint();
                        }

                        return;
                    } else if (lastPoint == newNextSourcePoint) {
                        lastPoint = sinkPoint;
                    }
                }

            }


            PointF nextPoint = new PointF(0, 0);
            if (arrow.coord.path.size() > 2) {
                nextPoint = arrow.coord.path.get(2);
            }


            if (firstPoint != startPoint) {
                path.add(firstPoint);
            }

            path.add(startPoint);

            ArrayList<PointF> newPath = new ArrayList<PointF>();
            ArrayList<PointF> lastPath = new ArrayList<PointF>();

            //Условие для перестроения пути
            if (startPoint.x != nextPoint.x && startPoint.y != nextPoint.y) {

                if (arrow.sink.type.equals("I") && (!intersection)) {
                    PointF vec = new PointF((lastPoint.x - firstPoint.x) / 2, 0);
                    startPoint = new PointF(firstPoint.x + vec.x, firstPoint.y);
                    endPoint = new PointF(firstPoint.x + vec.x, lastPoint.y);
                    path.clear();
                    path.add(firstPoint);
                    path.add(startPoint);
                    path.add(endPoint);
                    path.add(lastPoint);
                    arrow.setArrowCoord(path);

                    //Изменение точки соединения Arrow и Squiggle
                    if (arrow.squiggle != null) {
                        arrow.squiggle.newArrowPoint();
                    }

                    return;
                }

                PointF currentPoint = startPoint;
                int attempt = 0;
                int counter1 = 0;
                int counter2 = 0;
                boolean exit = false;

                startDirections.add(0, firstDir);
                while (startDirections.size() != 0) {
                    ArrayList<String> currentDirections = startDirections;
                    String direction;
                    while ((currentPoint.x != endPoint.x) && (currentPoint.y != endPoint.y)) {
                        Log.w("Circle", "true");

                        if (currentDirections.size() == 0) {
                            break;
                        } else {
                            direction = currentDirections.get(0);
                        }

                        newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                        pointList = MathCalc.boxIntersection(newPoint, currentPoint, box2);
                        if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) && (pointList == null)) {
                            newPath.add(newPoint);
                            currentPoint = newPoint;
                            currentDirections = MathCalc.nextDirections(direction);
                        } else {
                            currentDirections.remove(0);
                            if (currentDirections.size() == 0) {
                                currentPoint = startPoint;
                                newPath.clear();
                                if (startDirections.size() != 0) {
                                    startDirections.remove(0);
                                    currentDirections = startDirections;
                                    continue;
                                }
                                break;
                            }
                        }


                        if (counter1 > N) {
                            break;
                        }
                        counter1++;


                    }

                    pointList = MathCalc.boxIntersection(currentPoint, endPoint, box2);
                    if (pointList != null) {
                        if (startDirections.size() != 0) {
                            startDirections.remove(0);
                            currentPoint = startPoint;
                            newPath.clear();
                        }
                    } else if ((currentPoint.x == endPoint.x) || (currentPoint.y == endPoint.y)) {
                        break;
                    }

                    attempt++;

                    if (counter2 > N) {
                        break;
                    }
                    counter2++;
                }
                path.addAll(newPath);
                path.add(endPoint);
                if (lastPoint != endPoint) {
                    path.add(lastPoint);
                }

                path = removeLoop(path);
                path = removeExcessPoints(path);
                arrow.setArrowCoord(path);


                //Изменение точки соединения Arrow и Squiggle
                if (arrow.squiggle != null) {
                    arrow.squiggle.newArrowPoint();
                }


            } else {
                for (int i = 2; i < arrow.coord.path.size(); i++) {
                    lastPath.add(arrow.coord.path.get(i));
                }
                path.addAll(lastPath);
                path = removeLoop(path);
                path = removeExcessPoints(path);
                arrow.setArrowCoord(path);

                //Изменение точки соединения Arrow и Squiggle
                if (arrow.squiggle != null) {
                    arrow.squiggle.newArrowPoint();
                }
            }
            Log.w("Write sourcePoint", firstPoint.toString());
        }

        //Новый метод equalizeArrow, функционал отличен от старого метода
        //Служит для изменения Arrow из 4-ёх точек в Arrow из двух точек
         public void equalizeArrow(Arrow arrow){
             if(arrow.source!=null && arrow.sink!=null){
                 if(arrow.source.getConnectorSource()!=null && arrow.sink.getConnectorSink()!=null){
                     if(arrow.coord.path.size()==4) {
                         PointF sourcePoint = arrow.source.getConnectorPoint();
                         PointF nextSourcePoint = arrow.coord.path.get(1);
                         PointF sinkPoint = arrow.sink.getConnectorPoint();
                         PointF prevSinkPoint = arrow.coord.path.get(2);

                         String dir1="";
                         String dir2="";
                         if(arrow.source.getConnectorSource() instanceof Box) {
                             dir1 = MathCalc.getDirectionByBoxConnectorType(arrow.source.getConnectorType());
                         }else if(arrow.source.getConnectorSource() instanceof Border){
                             dir1=MathCalc.getDirectionByBorder((Border)arrow.source.getConnectorSource());
                         }

                         if(arrow.sink.getConnectorSink() instanceof Box) {
                             dir2 = MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());
                         }else if(arrow.sink.getConnectorSink() instanceof Border){
                             dir2=MathCalc.getDirectionByBorder((Border)arrow.sink.getConnectorSink());
                         }

                         char orientation=MathCalc.getApproxOrientationByDirections(dir1,dir2);
                         if(orientation=='H'){
                             if(Math.abs(nextSourcePoint.y-prevSinkPoint.y)<3){
                                 if(arrow.sink.getConnectorSink() instanceof Box){
                                     Box box=(Box)arrow.sink.getConnectorSink();
                                     if(MathCalc.pointInBoxRangeY(sourcePoint,box)){
                                         PointF newSinkPoint=new PointF(sinkPoint.x,sourcePoint.y);
                                         arrow.setArrowCoord(sourcePoint,newSinkPoint);
                                         return;
                                     }
                                 }else if(arrow.sink.getConnectorSink() instanceof Border){
                                     arrow.setArrowCoord(sourcePoint,sinkPoint);
                                     return;
                                 }else if(arrow.sink.getConnectorSink() instanceof Branch){

                                 }

                                 if(arrow.source.getConnectorSource() instanceof Box){
                                     Box box=(Box)arrow.source.getConnectorSource();
                                     if(MathCalc.pointInBoxRangeY(sinkPoint,box)){
                                         PointF newSourcePoint=new PointF(sourcePoint.x,sinkPoint.y);
                                         arrow.setArrowCoord(newSourcePoint,sinkPoint);
                                         return;
                                     }
                                 }else if(arrow.source.getConnectorSource() instanceof Border){
                                     arrow.setArrowCoord(sourcePoint,sinkPoint);
                                     return;
                                 }else if(arrow.sink.getConnectorSink() instanceof Branch){
                                    //zzz
                                 }
                             }
                         }else if(orientation=='V'){
                             if(Math.abs(nextSourcePoint.x-prevSinkPoint.x)<3){
                                 if(arrow.sink.getConnectorSink() instanceof Box){
                                     Box box=(Box)arrow.sink.getConnectorSink();
                                     if(MathCalc.pointInBoxRangeX(sourcePoint, box)){
                                         PointF newSinkPoint=new PointF(sourcePoint.x,sinkPoint.y);
                                         arrow.setArrowCoord(sourcePoint,newSinkPoint);
                                         return;
                                     }
                                 }else if(arrow.sink.getConnectorSink() instanceof Border){
                                     arrow.setArrowCoord(sourcePoint,new PointF(sourcePoint.x,sinkPoint.y));
                                     return;
                                 }else if(arrow.sink.getConnectorSink() instanceof Branch){

                                 }

                                 if(arrow.source.getConnectorSource() instanceof Box){
                                     Box box=(Box)arrow.source.getConnectorSource();
                                     if(MathCalc.pointInBoxRangeX(sinkPoint, box)){
                                         PointF newSourcePoint=new PointF(sinkPoint.x,sourcePoint.y);
                                         arrow.setArrowCoord(newSourcePoint,sinkPoint);
                                         return;
                                     }
                                 }else if(arrow.source.getConnectorSource() instanceof Border){
                                     arrow.setArrowCoord(new PointF(sinkPoint.x,sourcePoint.y),sinkPoint);
                                     return;
                                 }else if(arrow.sink.getConnectorSink() instanceof Branch){
                                       //zzz
                                 }
                             }
                         }
                     }
                 }
             }
         }


         public void moveArrowSink(Arrow arrow,Border border,Box box,float x,float y){
            ArrayList<PointF> path=new ArrayList<PointF>();

            PointF sinkPoint=arrow.sink.getConnectorPoint();
            PointF sourcePoint=arrow.source.getConnectorPoint();

            PointF newSinkPoint=new PointF(sinkPoint.x+x,sinkPoint.y+y);
            PointF prevSinkPoint=arrow.coord.path.get(arrow.coord.path.size()-2);
            PointF newPrevSinkPoint=new PointF();
            PointF lastPoint=newSinkPoint;
            PointF firstPoint=sourcePoint;

            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            if(arrow.sink.getConnectorType().equals("I")){
                newPrevSinkPoint=new PointF(prevSinkPoint.x,prevSinkPoint.y+y);
            }else{
                newPrevSinkPoint=new PointF(prevSinkPoint.x+x,prevSinkPoint.y);
            }

            if(arrow.coord.path.size()==2) {

                ArrayList<PointF> newPath=new ArrayList<PointF>();
                for(int i=0;i<arrow.coord.path.size()-2;i++){
                    newPath.add(arrow.coord.path.get(i));
                }

                path.add(newPrevSinkPoint);
                path.add(newSinkPoint);
                newPath.addAll(path);

                arrow.setArrowCoord(newPath);

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }

                return;
            }

            String firstDir;
            if(arrow.source.getConnectorType().equals("I")){
                firstDir="right";
            }else if(arrow.source.getConnectorType().equals("C")){
                firstDir="down";
            }else{
                firstDir="up";
            }

            //String lastDir=MathCalc.getDirectionByBoxSide(newSinkPoint,box);
            String lastDir=MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());
            PointF lastVec=new PointF(newPrevSinkPoint.x-newSinkPoint.x,newPrevSinkPoint.y-newSinkPoint.y);
            PointF normVec=MathCalc.getNormVecByDirection(lastDir);

            ArrayList<PointF> pointList=box.coord.getPointList();
            RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));

            //Начальное смещение в сторону нужного направления для стартовой точки
            PointF newPoint=MathCalc.getPointByDirection(plotArea1,endPoint,lastDir);
            endPoint=newPoint;


            ArrayList<String> startDirections=new ArrayList<String>();
            startDirections.add(firstDir);
            //startDirections.add(0,lastDir);

            if(MathCalc.isOneDirection(lastVec,normVec)){
                endPoint=newPrevSinkPoint;
            }


            PointF prevPoint=arrow.coord.path.get(arrow.coord.path.size()-3);
            if(endPoint.x!=prevPoint.x && endPoint.y!=prevPoint.y) {

                path.add(startPoint);
                if (MathCalc.pointInBoxRangeY(startPoint, box)) {
                    newPoint = new PointF(plotArea1.left, startPoint.y);
                    startPoint = newPoint;
                    path.add(startPoint);
                    startDirections=MathCalc.nextDirections(firstDir);
                } else if (MathCalc.pointInBoxRangeX(startPoint, box)) {
                    if (arrow.source.getConnectorType().equals("C")) {
                        newPoint = new PointF(startPoint.x, plotArea1.top);
                        startPoint = newPoint;
                    } else {
                        newPoint = new PointF(startPoint.x, plotArea1.bottom);
                        startPoint = newPoint;
                    }
                    path.add(startPoint);
                    startDirections=MathCalc.nextDirections(firstDir);
                }

                RectF plotArea = new RectF();
                if (startPoint != firstPoint) {
                    plotArea = plotArea1;
                } else {
                    plotArea = MathCalc.buildPlotArea(firstPoint, endPoint);
                    pointList = MathCalc.boxIntersection(startPoint, lastPoint, box);
                    if (pointList != null) {
                        plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                        plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                        plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                        plotArea = MathCalc.resizePlotAreaMore(plotArea, pointList.remove(0));
                    }
                }

                ArrayList<PointF> newPath=new ArrayList<PointF>();
                ArrayList<String> currentDirections=startDirections;
                String direction;
                PointF currentPoint=startPoint;
                int counter=0;
                while(currentPoint.x!=endPoint.x && currentPoint.y!=endPoint.y){
                    direction=currentDirections.get(0);
                    newPoint=MathCalc.getPointByDirection(plotArea,currentPoint,direction);

                    //pointList=boxIntersection(currentPoint,newPoint,box);
                    if(newPoint.x!=currentPoint.x || newPoint.y!=currentPoint.y){
                        newPath.add(newPoint);
                        currentPoint=newPoint;
                        currentDirections=MathCalc.nextDirections(direction);
                    }else{
                        currentDirections.remove(0);
                        if (currentDirections.size() == 0) {
                            currentPoint=startPoint;
                            newPath.clear();
                            if(startDirections.size()!=0) {
                                startDirections.remove(0);
                                currentDirections=startDirections;
                                continue;
                            }
                            break;
                        }
                    }

                    if(counter>N){
                        break;
                    }
                    counter++;
                }

                newPath.add(endPoint);
                if(endPoint!=lastPoint){
                    newPath.add(lastPoint);
                }
                path.addAll(newPath);
                path=removeLoop(path);
                path=removeExcessPoints(path);
                arrow.setArrowCoord(path);

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }

                //Изменение точки соединения Arrow и Squiggle
                /*
                path=MathCalc.docPointsToScreen(arrow.coord.path);
                newSinkPoint=path.get(path.size()-1);
                arrow.moveSquiggleArrow(sinkPoint,prevSinkPoint,newSinkPoint.x-sinkPoint.x,newSinkPoint.y-sinkPoint.y);
                */
            }else {
                //lastPath
                path.add(newPrevSinkPoint);
                path.add(newSinkPoint);
                int size = arrow.coord.path.size();
                ArrayList<PointF> newPath=new ArrayList<PointF>();
                for(int i=0;i<arrow.coord.path.size()-2;i++){
                    newPath.add(arrow.coord.path.get(i));
                }

                newPath.addAll(path);
                arrow.setArrowCoord(removeLoop(newPath));

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }
            }
         }

        public void moveArrowSink(Arrow arrow, Branch branch,Box box,float x,float y){
            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            //Определим начальные направления,
            //которые зависят от того, находятся ли изначально первые две точки arrow, и последние две точки родителя branch
            //в одной ориентации
            boolean branchParentDirection=false;
            PointF nextSourcePoint=arrow.coord.path.get(1);

            PointF parentSinkPoint=branch.parent.coord.path.get(branch.parent.coord.path.size()-1);
            PointF parentPrevSinkPoint=branch.parent.coord.path.get(branch.parent.coord.path.size()-2);
            char orientation=MathCalc.getOrientationByPoints(sourcePoint,nextSourcePoint);
            char parentOrientation=MathCalc.getOrientationByPoints(parentSinkPoint,parentPrevSinkPoint);
            if(orientation==parentOrientation){
                branchParentDirection=true;
            }

            //Новая sinkPoint расположенная на Box
            PointF newSinkPoint = new PointF(sinkPoint.x + x, sinkPoint.y + y);
            //Метод buildPath, будет строить путь по arrow.source и arrow.sink
            arrow.sink.setConnectorPoint(newSinkPoint);

            if(branchParentDirection) {
                arrow.setArrowCoord(buildPath(branch, box,true,arrow.source,arrow.sink));
            }else{
                arrow.setArrowCoord(buildPath(branch, box,arrow.source,arrow.sink));
            }
        }

        //Если два родителя находятся в одной ориентации, то выбираем ближайшее направлении до newSinkPoin в противоположной ориентации
        //иначе выбираем направление одного из родителя
        public void moveArrowSink(Arrow arrow,Join join,Box box,float x,float y){
            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            Arrow parent1=join.parentList.get(0);
            Arrow parent2=join.parentList.get(1);

            PointF parent1SinkPoint=parent1.coord.path.get(parent1.coord.path.size()-1);
            PointF parent1PrevSinkPoint=parent1.coord.path.get(parent1.coord.path.size()-2);

            PointF parent2SinkPoint=parent2.coord.path.get(parent2.coord.path.size()-1);
            PointF parent2PrevSinkPoint=parent2.coord.path.get(parent2.coord.path.size()-2);

            //Новая sinkPoint расположенная на Box
            PointF newSinkPoint = new PointF(sinkPoint.x + x, sinkPoint.y + y);
            //Метод buildPath, будет строить путь по arrow.source и arrow.sink
            arrow.sink.setConnectorPoint(newSinkPoint);

            String direction=null;
            char parent1Orientation=MathCalc.getOrientationByPoints(parent1SinkPoint, parent1PrevSinkPoint);
            char parent2Orientation=MathCalc.getOrientationByPoints(parent2SinkPoint,parent2PrevSinkPoint);
            if(parent1Orientation==parent2Orientation){
                direction=MathCalc.getClosestDirection(sourcePoint,newSinkPoint,parent1Orientation);
            }else{
                direction=MathCalc.getDirectionByPoints(parent1PrevSinkPoint,parent1SinkPoint);
            }

            if(direction!=null){
                arrow.setArrowCoord(buildPath(join,box,arrow.source,arrow.sink,direction));
            }else{
                arrow.setArrowCoord(buildPath(join,box,arrow.source,arrow.sink,"right"));
            }

        }


         public void moveArrowSink(Arrow arrow,Box box1,Box box2,float x,float y){
            ArrayList<PointF> path=new ArrayList<PointF>();
            PointF sourcePoint=arrow.source.getConnectorPoint();
            Log.w("Read sourcePoint",sourcePoint.toString());
            //PointF newSourcePoint=new PointF(sourcePoint.x+x,sourcePoint.y+y);
            PointF firstPoint=sourcePoint;

            PointF sinkPoint=arrow.sink.getConnectorPoint();
            PointF newSinkPoint=new PointF(sinkPoint.x+x,sinkPoint.y+y);
            PointF lastPoint=newSinkPoint;
            PointF prevSinkPoint=new PointF(arrow.coord.path.get(arrow.coord.path.size()-2).x,arrow.coord.path.get(arrow.coord.path.size()-2).y);

            PointF newPrevSinkPoint=new PointF();
            if(arrow.sink.getConnectorType()=="C" || arrow.sink.getConnectorType()=="M"){
                newPrevSinkPoint=new PointF(prevSinkPoint.x+x,prevSinkPoint.y);
            }else{
                newPrevSinkPoint=new PointF(prevSinkPoint.x,prevSinkPoint.y+y);
            }


            String firstDir=MathCalc.getDirectionByBoxConnectorType(arrow.source.getConnectorType());
            String lastDir=MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());

            //Начальная инициализация стартовой и конечной точек, которые участвую в построении нового пути
            PointF startPoint=firstPoint;
            PointF endPoint=lastPoint;

            ArrayList<PointF> pointList=box1.coord.getPointList();
            RectF plotArea1=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));

            //Начальное смещение в сторону нужного направления для стартовой точки
            PointF newPoint=MathCalc.getPointByDirection(plotArea1,startPoint,firstDir);
            if(!MathCalc.pointInBox(newPoint,box2)){
                startPoint=newPoint;
            }


            pointList=box2.coord.getPointList();
            RectF plotArea2=MathCalc.buildPlotAreaMore(pointList.remove(0),pointList.remove(0));


            //Начальное смещение в сторону нужного направления для конечной точки
            newPoint=MathCalc.getPointByDirection(plotArea2,endPoint,lastDir);
            if(!MathCalc.pointInBox(newPoint,box1)){
                endPoint=newPoint;
            }

            PointF normFirstVec=MathCalc.getNormVecByDirection(firstDir);
            //PointF firstVec=new PointF(newNextSourcePoint.x-firstPoint.x,newNextSourcePoint.y-firstPoint.y);
            PointF normLastVec=MathCalc.getNormVecByDirection(lastDir);
            PointF lastVec=new PointF(newPrevSinkPoint.x-lastPoint.x,newPrevSinkPoint.y-lastPoint.y);

            ArrayList<String> endDirections=new ArrayList<String>();

            //Если направление изменилось, то строим новый путь от начального смещения
            //Иначе точка конца будет равна новой версии точки, предыдущей точки
            if(MathCalc.isOneDirection(lastVec,normLastVec)){
                pointList=MathCalc.boxIntersection(endPoint,newPrevSinkPoint,box1);
                if(pointList==null) {
                    endPoint = newPrevSinkPoint;
                }else{
                    Log.w("Old startPoint","true");
                }
            }
            endDirections = MathCalc.nextDirections(lastDir);

            //Определяем область построения пути с учетом box1
            RectF plotArea=MathCalc.buildPlotArea(startPoint,endPoint);
            boolean intersection=false;
            pointList=MathCalc.boxIntersection(startPoint,endPoint,box1);
            if(pointList!=null){
                Log.w("Intersection1","true");
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                intersection=true;
            }

            //Определяем область построения пути с учетом box2
            pointList=MathCalc.boxIntersection(startPoint,endPoint,box2);
            if(pointList!=null){
                Log.w("Intersection2","true");
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                plotArea=MathCalc.resizePlotAreaMore(plotArea,pointList.remove(0));
                intersection=true;
            }


            //Условие для объединения пути в одну линию, когда sink имеет тип Input
            if(arrow.sink.type.equals("I") && (!intersection)){
                if(plotArea.height()<=5 || arrow.coord.path.size()==2){
                    newPoint=new PointF(firstPoint.x,lastPoint.y);
                    if(MathCalc.pointInBoxRangeY(newPoint,box1)) {
                        path.add(newPoint);
                        path.add(lastPoint);
                        arrow.setArrowCoord(path);
                        /*
                        arrow.source.setConnectorPoint(arrow.coord.path.get(0));
                        arrow.sink.setConnectorPoint(arrow.coord.path.get(1));
                        arrow.syncConnector();
                        */

                        //Изменение точки соединения Arrow и Squiggle
                        if(arrow.squiggle!=null){
                            arrow.squiggle.newArrowPoint();
                        }

                        return;
                    }/*
                    else if(firstPoint==newPrevSinkPoint){
                        firstPoint=sourcePoint;
                    }
                    */
                }

            }

            //Условие для разделения пути, когда sink имеет тип Input
            if(arrow.sink.type.equals("I") && (!intersection)){
                if(arrow.coord.path.size()==2) {
                    PointF vec = new PointF((lastPoint.x - firstPoint.x) / 2, 0);
                    startPoint = new PointF(firstPoint.x + vec.x, firstPoint.y);
                    endPoint = new PointF(firstPoint.x + vec.x, lastPoint.y);
                    path.clear();
                    path.add(firstPoint);
                    path.add(startPoint);
                    path.add(endPoint);
                    path.add(lastPoint);
                    //path = MathCalc.screenPointsToDoc(path);
                    arrow.setArrowCoord(path);
                    /*
                    arrow.source.setConnectorPoint(arrow.coord.path.get(0));
                    arrow.sink.setConnectorPoint(arrow.coord.path.get(arrow.coord.path.size() - 1));
                    arrow.syncConnector();
                    */

                    //Изменение точки соединения Arrow и Squiggle
                    if(arrow.squiggle!=null){
                        arrow.squiggle.newArrowPoint();
                    }

                    return;
                }
            }

            //Пред предыдущая точка, от соответсвия с которой будет зависить необходимость построения нового пути
            PointF prevPoint=new PointF(0,0);
            if(arrow.coord.path.size()>2){
                prevPoint=arrow.coord.path.get(arrow.coord.path.size()-3);
            }

            //Записываем новые точки пути
            if(lastPoint!=endPoint){
                path.add(lastPoint);
            }

            path.add(0,endPoint);

            ArrayList<PointF> newPath=new ArrayList<PointF>();
            ArrayList<PointF> lastPath=new ArrayList<PointF>();


            //Условие для перестроения пути
            if(endPoint.x!=prevPoint.x && endPoint.y!=prevPoint.y){

                PointF currentPoint=endPoint;
                int counter1=0;
                int counter2=0;
                //boolean complete=false;

                //Добавляем дополнительное направление, только для endPoint
                endDirections.add(0,lastDir);
                while(endDirections.size()!=0) {
                    ArrayList<String> currentDirections=endDirections;
                    String direction;
                    //Выполняем, пока текущая точка пути не будет равна одной из координат точки startPoint
                    while ((currentPoint.x != startPoint.x) && (currentPoint.y != startPoint.y)) {
                        Log.w("Circle", "true");

                        if(currentDirections.size()==0){
                            break;
                        }else {
                            direction = currentDirections.get(0);
                        }

                        newPoint = MathCalc.getPointByDirection(plotArea, currentPoint, direction);

                        pointList=MathCalc.boxIntersection(newPoint,currentPoint,box1);
                        //Если точка имеет новое значение, и она вместе с текущей не пересекает box1,
                        //то заносим её в новый путь
                        if (((newPoint.x != currentPoint.x) || (newPoint.y != currentPoint.y)) && (pointList==null)) {
                            newPath.add(0,newPoint);
                            currentPoint = newPoint;
                            currentDirections = MathCalc.nextDirections(direction);
                        } else {
                            currentDirections.remove(0);
                            if (currentDirections.size() == 0) {
                                currentPoint=startPoint;
                                newPath.clear();
                                if(endDirections.size()!=0) {
                                    endDirections.remove(0);
                                    currentDirections=endDirections;
                                    continue;
                                }
                                break;
                            }
                        }


                        if(counter1>30){
                            break;
                        }
                        counter1++;


                    }

                    //Проверка пересечения последней точки с box1, возможно нужно изменить условие и добавить проверку на то,
                    // что построение за вершилось успешно, без break
                    pointList=MathCalc.boxIntersection(currentPoint,endPoint,box1);
                    if(pointList!=null) {
                        if (endDirections.size() != 0) {
                            endDirections.remove(0);
                            currentPoint = endPoint;
                            newPath.clear();
                        }
                    }


                    if(counter2>30){
                        break;
                    }
                    counter2++;
                }
                path.addAll(0,newPath);
                path.add(0,startPoint);
                if(firstPoint!=startPoint){
                    path.add(0,firstPoint);
                }
                //path=MathCalc.screenPointsToDoc(path);
                //arrow.coord.path=removeExcessPoints(arrow.coord.path);
                //arrow.coord.path=equalizeArrow(arrow);
                path=removeLoop(path);
                path=removeExcessPoints(path);
                arrow.setArrowCoord(path);
                /*
                arrow.source.setConnectorPoint(arrow.coord.path.get(0));
                arrow.sink.setConnectorPoint(arrow.coord.path.get(arrow.coord.path.size()-1));
                arrow.syncConnector();
                */

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }

                //Изменение точки соединения Arrow и Squiggle
                /*
                path=MathCalc.docPointsToScreen(arrow.coord.path);
                newSinkPoint=path.get(path.size()-1);
                arrow.moveSquiggleArrow(sinkPoint,prevSinkPoint,newSinkPoint.x-sinkPoint.x,newSinkPoint.y-sinkPoint.y);
                */
            }else{
                //path=MathCalc.screenPointsToDoc(path);
                //Записываем не изменёные точки в lastPath
                for(int i=0;i<arrow.coord.path.size()-2;i++){
                    newPath.add(arrow.coord.path.get(i));
                }
                newPath.addAll(path);

                //arrow.coord.path=removeExcessPoints(arrow.coord.path);
                //arrow.coord.path=equalizeArrow(arrow);
                newPath=removeLoop(newPath);
                newPath=removeExcessPoints(newPath);
                arrow.setArrowCoord(newPath);
                /*
                arrow.source.setConnectorPoint(arrow.coord.path.get(0));
                arrow.sink.setConnectorPoint(arrow.coord.path.get(arrow.coord.path.size()-1));
                arrow.syncConnector();
                */

                //Изменение точки соединения Arrow и Squiggle
                if(arrow.squiggle!=null){
                    arrow.squiggle.newArrowPoint();
                }
            }
            Log.w("Write sourcePoint",firstPoint.toString());
         }



         public ArrayList<PointF> jointPointsByY(Box sourceBox,ArrayList<PointF> pointList){
            if(pointList.size()>=4) {
                PointF point1=pointList.get(1);
                PointF point2=pointList.get(2);
                PointF nextPoint=pointList.get(3);
                if (Math.abs(point1.x - point2.x) < 5) {
                    if (Math.abs(point1.y - point2.y) < 5) {
                        PointF newPoint = new PointF(pointList.get(0).x,nextPoint.y);
                        if(MathCalc.pointInBoxRangeY(newPoint,sourceBox)){
                            pointList.remove(1);
                            pointList.remove(1);
                            pointList.set(0,newPoint);
                        }
                    }
                }
            }
            return pointList;
         }



        /*
        public boolean pointInBox(PointF p,Box box){
            int docWidth=DocumentDraw.docWidth;
            int docHeight=DocumentDraw.docHeight;
            float left=box.coord.p1.x*docWidth;
            float top=box.coord.p2.y*docHeight;
            float right=box.coord.p2.x*docWidth;
            float bottom=box.coord.p1.y*docHeight;
            if(left<p.x && p.x<right){
                if(top<p.y && p.y<bottom){
                    return true;
                }
            }
            return false;
        }
        */

         public boolean vectorInBoxRange(PointF p1,PointF p2,Box box){
            float left=box.coord.p1.x;
            float top=box.coord.p2.y;
            float right=box.coord.p2.x;
            float bottom=box.coord.p1.y;

            if(left<p1.x && p1.x<right){
                if(left<p2.x && p2.x<right){
                    return true;
                }
            }

            if(top<p1.y && p1.y<bottom){
                if(top<p2.y && p2.y<bottom){
                    return true;
                }
            }
            return false;
         }


         //Метод удаляет лишние точки
         public ArrayList<PointF> removeExcessPoints(ArrayList<PointF> pointList){
            if(pointList.size()>2){
                PointF[] snake=new PointF[3];
                int i=0;
                int j=2;

                ArrayList<PointF> newPointList=new ArrayList<PointF>();
                newPointList.add(pointList.get(0));
                while(j<pointList.size()) {
                    snake[0] = pointList.get(i);
                    snake[1] = pointList.get(i + 1);
                    snake[2] = pointList.get(j);

                    PointF vec1=new PointF(snake[2].x-snake[0].x,snake[2].y-snake[0].y);
                    PointF vec2=new PointF(snake[2].x-snake[1].x,snake[2].y-snake[1].y);

                    if(!MathCalc.isOneDirection(vec1,vec2)){
                        newPointList.add(snake[1]);
                    }else{
                        Log.w("Event","Excess points");
                    }

                    i++;
                    j++;
                }

                newPointList.add(pointList.get(pointList.size()-1));
                return newPointList;
            }else{
                return pointList;
            }
         }


        /*
        public ArrayList<PointF> removeExcessPoints(ArrayList<PointF> path){
            PointF p1=null;
            PointF p2=null;
            String oldDirection=null;
            String curDirection=null;
            ArrayList<PointF> newPath=null;
            if(path!=null) {
                if(path.size()>1) {
                    newPath=new ArrayList<PointF>();
                    newPath.add(path.get(0));
                    for (int i = 0; i < (path.size() - 1); i++) {
                        p1 = path.get(i);
                        p2 = path.get(i + 1);
                        curDirection = MathCalc.getDirectionByPoints(p1, p2);

                        if (curDirection != null && oldDirection != null) {
                            if (MathCalc.getOrientationByDirection(curDirection) != MathCalc.getOrientationByDirection(oldDirection)) {
                                newPath.add(p1);
                            }
                        }

                        if(curDirection==null){
                            Log.w("Direction","Null");
                        }

                        oldDirection = new String(curDirection);
                    }
                    newPath.add(path.get(path.size()-1));
                }else{
                    return path;
                }
            }

            return newPath;
        }
        */

         //Метод удаляет петли
         public ArrayList<PointF> removeLoop(ArrayList<PointF> pointList){
            if(pointList.size()>=5){

                ArrayList<PointF> newPointList=new ArrayList<PointF>();
                PointF[] currentVec=new PointF[2];
                PointF[] nextVec=new PointF[2];

                int i=1;
                while(i<pointList.size()){
                    currentVec[0]=pointList.get(i-1);
                    currentVec[1]=pointList.get(i);
                    int j=2;
                    while(j<pointList.size()){
                        nextVec[0]=pointList.get(j-1);
                        nextVec[1]=pointList.get(j);

                        PointF intersection=MathCalc.vectorsIntersectionPoint(currentVec[0],currentVec[1],nextVec[0],nextVec[1]);
                        if(intersection!=null){
                            for(int n=0;n<i;n++){
                                newPointList.add(pointList.get(n));
                            }
                            newPointList.add(intersection);
                            for(int n=j;n<pointList.size();n++){
                                newPointList.add(pointList.get(n));
                            }
                            pointList=newPointList;
                            i=0;
                            break;
                        }

                        j++;
                    }

                    if(pointList.size()<5){
                        break;
                    }
                    i++;
                }
            }
            return pointList;

         }


         public void trySelectTextContainer(double distance,Box box){
            Suspicion ns = new Suspicion(box, distance);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectTextContainer(Box box){
            Suspicion ns = new Suspicion(box);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectTextContainer(double distance,Arrow.ArrowLabel label){
            Suspicion ns = new Suspicion(label, distance);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectTextContainer(Arrow.ArrowLabel label){
            Suspicion ns = new Suspicion(label);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void selectBoxConnector(Box box,String type){

         }

         public void forceSelectTextContainer(){
            if(guiltyList.size()!=0){
                Collections.sort(guiltyList,new SuspicionComparator());
                if(guiltyList.get(0).object instanceof Box) {
                    setSelectedObject((Box)guiltyList.get(0).object);
                }else if(guiltyList.get(0).object instanceof Arrow.ArrowLabel){
                    setSelectedObject((Arrow.ArrowLabel)guiltyList.get(0).object);
                }
            }
            guiltyList.clear();
            suspicionList.clear();
         }

         public void trySelectArrow(double distance,Arrow arrow){
            Suspicion ns = new Suspicion(arrow, distance);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectArrow(Arrow arrow){
            Suspicion ns = new Suspicion(arrow);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }

         public void forceSelectArrow(){
            if(guiltyList.size()!=0){
                Collections.sort(guiltyList,new SuspicionComparator());
                if(guiltyList.get(0).object instanceof Arrow) {
                    setSelectedObject((Arrow)guiltyList.get(0).object);
                }
            }
            guiltyList.clear();
            suspicionList.clear();
         }

         public void trySelectConnector(double distance,Arrow.ArrowSource arrowSource){
            Suspicion ns = new Suspicion(arrowSource, distance);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectConnector(Arrow.ArrowSource arrowSource){
            Suspicion ns = new Suspicion(arrowSource);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }

         public void trySelectConnector(double distance,Arrow.ArrowSink arrowSink){
            Suspicion ns = new Suspicion(arrowSink, distance);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }


         public void trySelectConnector(Arrow.ArrowSink arrowSink){
            Suspicion ns = new Suspicion(arrowSink);
            if(suspicionList.size()==0){
                suspicionList.add(ns);
            }else {
                boolean eq = false;
                for (Suspicion s : suspicionList) {
                    if (ns.equals(s)) {
                        eq=true;
                        guiltyList.add(s);
                        break;
                    }
                }
                if(!eq)
                    suspicionList.add(ns);
            }
         }

         public void forceSelectConnector(){
            if(guiltyList.size()!=0){
                Collections.sort(guiltyList,new SuspicionComparator());
                if(guiltyList.get(0).object instanceof Arrow.ArrowSource) {
                    setSelectedObject((Arrow.ArrowSource) guiltyList.get(0).object);
                }else if(guiltyList.get(0).object instanceof Arrow.ArrowSink){
                    setSelectedObject((Arrow.ArrowSink) guiltyList.get(0).object);
                }
            }
            guiltyList.clear();
            suspicionList.clear();
         }


        /*
        public void removeSelectSuspiction(){

            suspicion.selectSuspicion=null;
        }
        */

        /*
        public Box findBoxByDiagramId(String id){
            for(Box box : boxSet){
                if(box.diagramId.equals(id)){
                    return box;
                }
            }
            return null;
        }
        */

         public void addArrow(Arrow arrow){
            arrowSet.add(arrow);
            if(arrow.source.getConnectorSource() instanceof Box) {
                addDiagramStartArrow(arrow, (Box)arrow.source.getConnectorSource());
            }else if(arrow.sink.getConnectorSink() instanceof Box){
                addDiagramStartArrow(arrow, (Box)arrow.sink.getConnectorSink());
            }
         }

         public void addArrow(Arrow arrow,boolean addChildArrow){
             arrowSet.add(arrow);
             if(addChildArrow) {
                 if (arrow.source.getConnectorSource() instanceof Box) {
                     addDiagramStartArrow(arrow, (Box) arrow.source.getConnectorSource());
                 } else if (arrow.sink.getConnectorSink() instanceof Box) {
                     addDiagramStartArrow(arrow, (Box) arrow.sink.getConnectorSink());
                 }
             }
         }

         //Удаляет arrow из arrowSet текущей диаграммы
         public void removeArrow(Arrow arrow){
            removeSinkArrows(arrow);
            removeSourceArrows(arrow);
            removeBadBranch();
            removeBadJoin();
            renameArrows(arrow);
            removeArrowNumber();
            arrow=null;
         }

        //Удаляет не правильные Branch
        public void removeBadBranch(){
            ArrayList<Branch> badBranchList=new ArrayList<Branch>();
            for(Branch branch : branchSet){
                if(isBadBranch(branch)){
                    badBranchList.add(branch);
                }
            }

            for(Branch branch : badBranchList){
                branchSet.remove(branch);
                branch=null;
            }
        }

        //Проверяет branch на не правильность
        public boolean isBadBranch(Branch branch){
            if(branch==null){
                return true;
            }
            if(branch.parent==null){
                return true;
            }
            int i=0;
            for(Arrow child : branch.childList){
                i++;
                if(child==null){
                    return true;
                }
            }
            if(i!=2){
                return true;
            }
            return false;
        }


        //Удаляет не правильные Join
        public void removeBadJoin(){
            ArrayList<Join> badJoinList=new ArrayList<Join>();
            for(Join join : joinSet){
                if(isBadJoin(join)){
                    badJoinList.add(join);
                }
            }

            for(Join join : badJoinList){
                joinSet.remove(join);
                join=null;
            }
        }

        //Проверяет join на не правильность
        public boolean isBadJoin(Join join){
            if(join==null){
                return true;
            }
            if(join.child==null){
                return true;
            }
            int i=0;
            for(Arrow parent : join.parentList){
                i++;
                if(join==null){
                    return true;
                }
            }
            if(i!=2){
                return true;
            }
            return false;
        }

        //Удаляет Arrow, Join, Branch по направлению sink
        public void removeSinkArrows(Arrow arrow){
            if(arrow.sink!=null) {
                if (arrow.sink.getConnectorSink() instanceof Join) {
                    Join join = (Join) arrow.sink.getConnectorSink();
                    arrow.sink = null;
                    join.removeParent(arrow);

                    Arrow child = join.child;
                    Arrow parent = null;
                    for (Arrow curArrow : join.parentList) {
                        if (curArrow != arrow) {
                            parent = curArrow;
                            break;
                        }
                    }
                    if (parent != null) {
                        mergeArrowByJoin(parent, child, join);
                        removeSourceArrows(arrow);
                        arrowSet.remove(arrow);
                    }
                } else if (arrow.sink.getConnectorSink() instanceof Branch) {
                    Branch branch = (Branch) arrow.sink.getConnectorSink();
                    branch.parent = null;
                    arrow.sink = null;

                    Arrow parent = arrow;
                    ArrayList<Arrow> removedChild = new ArrayList<Arrow>();
                    for (Arrow child : branch.childList) {
                        child.source = null;
                        removeSinkArrows(child);
                        //child=null;
                        removedChild.add(child);
                        arrowSet.remove(child);
                    }
                    for (Arrow child : removedChild) {
                        branch.removeChild(child);
                    }
                    //arrow=null;
                    removeBranch(branch);
                    arrowSet.remove(arrow);
                } else {
                    arrowSet.remove(arrow);
                }
            }
        }


        //Удаляет Arrow, Join, Branch по направлению source
        public void removeSourceArrows(Arrow arrow){
            if(arrow.source!=null) {
                if (arrow.source.getConnectorSource() instanceof Join) {
                    Join join = (Join) arrow.source.getConnectorSource();
                    arrow.source = null;
                    join.child=null;

                    Arrow child = arrow;
                    ArrayList<Arrow> removedParent = new ArrayList<Arrow>();
                    for (Arrow parent : join.parentList) {
                        parent.sink = null;
                        removeSourceArrows(parent);
                        removedParent.add(parent);
                        arrowSet.remove(parent);
                    }
                    for (Arrow parent : removedParent) {
                        join.removeParent(parent);
                    }

                    removeJoin(join);
                    removeSinkArrows(child);
                    arrowSet.remove(arrow);
                } else if (arrow.source.getConnectorSource() instanceof Branch) {
                    Branch branch = (Branch) arrow.source.getConnectorSource();
                    arrow.source = null;
                    branch.removeChild(arrow);

                    Arrow child = null;
                    Arrow parent = branch.parent;
                    for (Arrow curArrow : branch.childList) {
                        if (curArrow != arrow) {
                            child = curArrow;
                            break;
                        }
                    }

                    if (child != null) {
                        mergeArrowByBranch(parent, child, branch);
                        removeSinkArrows(arrow);
                        arrowSet.remove(arrow);
                    }
                } else {
                    arrowSet.remove(arrow);
                }
            }
        }



         public void moveArrow(Arrow arrow,PointF touchPoint){
            Object obj1=arrow.source.getConnectorSource();
            Object obj2=arrow.sink.getConnectorSink();
            if(obj1 instanceof Box && obj2 instanceof Box){
                moveArrow(arrow,touchPoint,(Box)obj1,(Box)obj2);
            }else if(obj1 instanceof Box && obj2 instanceof Border){
                moveArrow(arrow,touchPoint,(Box)obj1,(Border)obj2);
            }else if(obj1 instanceof Border && obj2 instanceof Box){
                moveArrow(arrow,touchPoint,(Box)obj2,(Border)obj1);
            }else if((obj1 instanceof Branch && obj2 instanceof Box)){
                moveArrow(arrow,touchPoint,(Branch)obj1,(Box)obj2);
            //Должна быть связь Branch-Join и т.д.
            }else if((obj1 instanceof Branch || obj2 instanceof Branch)){
                if(obj1 instanceof Branch) {
                    moveArrow(arrow, touchPoint, (Branch) obj1);
                }else{
                    moveArrow(arrow, touchPoint, (Branch) obj2);
                }
            }else if((obj1 instanceof Box && obj2 instanceof Join)){
                moveArrow(arrow,touchPoint,(Box)obj1,(Join)obj2);
            }else if((obj1 instanceof Join || obj2 instanceof Join)){
                if(obj1 instanceof Join) {
                    moveArrow(arrow, touchPoint, (Join) obj1);
                }else{
                    moveArrow(arrow, touchPoint, (Join) obj2);
                }
            }
            //arrow.setArrowCoord(equalizeArrow(arrow));
         }

         //Сдвиг Arrow по индексу точки
         public void moveArrow(int ind,Arrow arrow,RectF plotArea,float xShift,float yShift){
             //ArrayList<PointF> path = MathCalc.docPointsToScreen(arrow.coord.path);
             ArrayList<PointF> path = arrow.coord.path;
             PointF point1=new PointF(path.get(ind).x,path.get(ind).y);
             PointF point2=new PointF(path.get(ind+1).x,path.get(ind+1).y);

             RectF plotArea1=new RectF(plotArea);
             plotArea1=MathCalc.resizePlotArea(plotArea1,point1);
             plotArea1=MathCalc.resizePlotArea(plotArea1,point2);

             PointF newPoint1=new PointF(point1.x+xShift,point1.y+yShift);
             PointF newPoint2=new PointF(point2.x+xShift,point2.y+yShift);

             //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
             newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea1,newPoint1);
             newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea1,newPoint2);

             path.set(ind, newPoint1);
             path.set(ind + 1, newPoint2);

             //zzz--------------------------------------

             path=removeLoop(path);
             arrow.setArrowCoord(path);
             /*
             if(arrow.coord.path.size()>2) {
                 if(arrow.coord.path.size()!=4) {
                     //Вызывает ошибку
                     jointArrowPoints(arrow);
                 }else{
                     equalizeArrow(arrow);
                 }
             }
             */
             //--------------------------------------

             //Изменение точки соединения Arrow и Squiggle
             if(arrow.squiggle!=null){
                 arrow.squiggle.newArrowPoint();
             }
         }

        public void moveArrow(Arrow arrow,PointF touchPoint,Branch branch) {
            ArrayList<PointF> path = arrow.coord.path;

            //Выбранные две точки (текущие)
            HashMap<Integer,ArrayList<PointF>> pointMap=MathCalc.getCurTouchPointMap(arrow,touchPoint);
            int ind=0;
            for(int key : pointMap.keySet()){
                ind=key;
            }

            PointF curPoint1=pointMap.get(ind).get(0);
            PointF curPoint2=pointMap.get(ind).get(1);

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            RectF plotArea = new RectF();
            RectF plotAreaSource = null;
            RectF plotAreaSink = null;
            PointF newPoint1 = new PointF();
            PointF newPoint2 = new PointF();

            Arrow parentArrow = null;
            Arrow childArrow = null;

            //Построение plotArea по текущим точкам
            char orientation = MathCalc.getOrientationByPoints(curPoint1, curPoint2);
            if(orientation=='H'){
                plotArea=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
            }else if(orientation=='V'){
                plotArea=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
            }

            //Если выбранные точки являются начальными в arrow
            if(curPoint1.x==sourcePoint.x && curPoint1.y==sourcePoint.y){
                plotAreaSource=getSourcePlotArea(arrow,orientation);
                if(plotAreaSource!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSource);
                }
            }

            //Если выбранные точки являются конечными в arrow
            if(curPoint2.x==sinkPoint.x && curPoint2.y==sinkPoint.y){
                plotAreaSink=getSinkPlotArea(arrow,orientation);
                if(plotAreaSink!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSink);
                }
            }

            //Если текущие точки не начальные и не конечные
            if(plotAreaSource==null && plotAreaSink==null){

            }

            //Создание новых точек
            float xShift=0;
            float yShift=0;
            if(orientation=='V'){
                xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);
            }else if(orientation=='H'){
                yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);
            }

            //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
            //zzz
            newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
            newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

            //Рекурсивный сдвиг Arrow связанных с arrow
            recursionShift(ind,arrow, orientation, plotArea, xShift, yShift);

            //Перемещение объекта Branch(изменение координат parentArrow и childArrow), если были выбраны sourcePoint и nextSourcePoint Arrow
            //Для этого объекты parentArrow и childArrow должны были быть инициализированы
            if(parentArrow!=null && childArrow!=null){
                //branchPointShift(parentArrow,childArrow,xShift,yShift);
            }


            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            arrow.setArrowCoord(path);
            /*
            if(arrow.coord.path.size()>2) {
                if(arrow.coord.path.size()!=4) {
                    jointArrowPoints(arrow);
                }else{
                    equalizeArrow(arrow);
                }
            }
            */
            //--------------------------------------


            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }


        public void moveArrow(Arrow arrow,PointF touchPoint,Branch branch,Box box) {
            ArrayList<PointF> path = arrow.coord.path;

            //Выбранные две точки (текущие)
            HashMap<Integer,ArrayList<PointF>> pointMap=MathCalc.getCurTouchPointMap(arrow,touchPoint);
            int ind=0;
            for(int key : pointMap.keySet()){
                ind=key;
            }

            PointF curPoint1=pointMap.get(ind).get(0);
            PointF curPoint2=pointMap.get(ind).get(1);

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            RectF plotArea = new RectF();
            RectF plotAreaSource = null;
            RectF plotAreaSink = null;
            PointF newPoint1 = new PointF();
            PointF newPoint2 = new PointF();

            Arrow parentArrow = null;
            Arrow childArrow = null;

            //Построение plotArea по текущим точкам
            char orientation = MathCalc.getOrientationByPoints(curPoint1, curPoint2);
            if(orientation=='H'){
                plotArea=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
            }else if(orientation=='V'){
                plotArea=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
            }

            //Если выбранные точки являются начальными в arrow
            if(curPoint1.x==sourcePoint.x && curPoint1.y==sourcePoint.y){
                plotAreaSource=getSourcePlotArea(arrow,orientation);
                if(plotAreaSource!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSource);
                }
            }

            //Если выбранные точки являются конечными в arrow
            if(curPoint2.x==sinkPoint.x && curPoint2.y==sinkPoint.y){
                plotAreaSink=getSinkPlotArea(arrow,orientation);
                if(plotAreaSink!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSink);
                }
            }

            //Если текущие точки не начальные и не конечные
            PointF boxPoint=sinkPoint;
            if(plotAreaSource==null && plotAreaSink==null){
                if(orientation=='H'){
                    plotArea=MathCalc.cutPlotAreaYByK(plotArea,curPoint1,boxPoint);
                }else if(orientation=='V'){
                    plotArea=MathCalc.cutPlotAreaXByK(plotArea,curPoint1,boxPoint);
                }
            }

            //Создание новых точек
            float xShift=0;
            float yShift=0;
            if(orientation=='V'){
                xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);
            }else if(orientation=='H'){
                yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);
            }

            //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
            //zzz
            newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
            newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

            //Рекурсивный сдвиг Arrow связанных с arrow
            recursionShift(ind,arrow, orientation, plotArea, xShift, yShift);

            //Перемещение объекта Branch(изменение координат parentArrow и childArrow), если были выбраны sourcePoint и nextSourcePoint Arrow
            //Для этого объекты parentArrow и childArrow должны были быть инициализированы
            if(parentArrow!=null && childArrow!=null){
                //branchPointShift(parentArrow,childArrow,xShift,yShift);
            }


            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            arrow.setArrowCoord(path);
            /*
            if(arrow.coord.path.size()>2) {
                if(arrow.coord.path.size()!=4) {
                    jointArrowPoints(arrow);
                }else{
                    equalizeArrow(arrow);
                }
            }
            */
            //--------------------------------------


            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }


        public void moveArrow(Arrow arrow,PointF touchPoint,Join join) {
            ArrayList<PointF> path = arrow.coord.path;

            //Выбранные две точки (текущие)
            HashMap<Integer,ArrayList<PointF>> pointMap=MathCalc.getCurTouchPointMap(arrow,touchPoint);
            int ind=0;
            for(int key : pointMap.keySet()){
                ind=key;
            }

            PointF curPoint1=pointMap.get(ind).get(0);
            PointF curPoint2=pointMap.get(ind).get(1);

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            RectF plotArea = new RectF();
            RectF plotAreaSource = null;
            RectF plotAreaSink = null;
            PointF newPoint1 = new PointF();
            PointF newPoint2 = new PointF();

            Arrow parentArrow = null;
            Arrow childArrow = null;

            //Построение plotArea по текущим точкам
            char orientation = MathCalc.getOrientationByPoints(curPoint1, curPoint2);
            if(orientation=='H'){
                plotArea=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
            }else if(orientation=='V'){
                plotArea=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
            }

            //Если выбранные точки являются начальными в arrow
            if(curPoint1.x==sourcePoint.x && curPoint1.y==sourcePoint.y){
                plotAreaSource=getSourcePlotArea(arrow,orientation);
                if(plotAreaSource!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSource);
                }
            }

            //Если выбранные точки являются конечными в arrow
            if(curPoint2.x==sinkPoint.x && curPoint2.y==sinkPoint.y){
                plotAreaSink=getSinkPlotArea(arrow,orientation);
                if(plotAreaSink!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSink);
                }
            }

            //Если текущие точки не начальные и не конечные
            if(plotAreaSource==null && plotAreaSink==null){

            }

            //Создание новых точек
            float xShift=0;
            float yShift=0;
            if(orientation=='V'){
                xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);
            }else if(orientation=='H'){
                yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);
            }

            //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
            //zzz
            newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
            newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

            //Рекурсивный сдвиг Arrow связанных с arrow
            recursionShift(ind,arrow, orientation, plotArea, xShift, yShift);

            //Перемещение объекта Branch(изменение координат parentArrow и childArrow), если были выбраны sourcePoint и nextSourcePoint Arrow
            //Для этого объекты parentArrow и childArrow должны были быть инициализированы
            if(parentArrow!=null && childArrow!=null){
                //branchPointShift(parentArrow,childArrow,xShift,yShift);
            }


            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            arrow.setArrowCoord(path);
            /*
            if(arrow.coord.path.size()>2) {
                if(arrow.coord.path.size()!=4) {
                    jointArrowPoints(arrow);
                }else{
                    equalizeArrow(arrow);
                }
            }
            */
            //--------------------------------------


            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }


        public void moveArrow(Arrow arrow,PointF touchPoint,Box box,Join join) {
            ArrayList<PointF> path = arrow.coord.path;

            //Выбранные две точки (текущие)
            HashMap<Integer,ArrayList<PointF>> pointMap=MathCalc.getCurTouchPointMap(arrow,touchPoint);
            int ind=0;
            for(int key : pointMap.keySet()){
                ind=key;
            }

            PointF curPoint1=pointMap.get(ind).get(0);
            PointF curPoint2=pointMap.get(ind).get(1);

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();

            RectF plotArea = new RectF();
            RectF plotAreaSource = null;
            RectF plotAreaSink = null;
            PointF newPoint1 = new PointF();
            PointF newPoint2 = new PointF();

            Arrow parentArrow = null;
            Arrow childArrow = null;

            //Построение plotArea по текущим точкам
            char orientation = MathCalc.getOrientationByPoints(curPoint1, curPoint2);
            if(orientation=='H'){
                plotArea=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
            }else if(orientation=='V'){
                plotArea=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
            }

            //Если выбранные точки являются начальными в arrow
            if(curPoint1.x==sourcePoint.x && curPoint1.y==sourcePoint.y){
                plotAreaSource=getSourcePlotArea(arrow,orientation);
                if(plotAreaSource!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSource);
                }
            }

            //Если выбранные точки являются конечными в arrow
            if(curPoint2.x==sinkPoint.x && curPoint2.y==sinkPoint.y){
                plotAreaSink=getSinkPlotArea(arrow,orientation);
                if(plotAreaSink!=null){
                    plotArea=MathCalc.intersectionPlotArea(plotArea,plotAreaSink);
                }
            }

            //Если текущие точки не начальные и не конечные
            PointF boxPoint=sourcePoint;
            if(plotAreaSource==null && plotAreaSink==null){
                if(orientation=='H'){
                    plotArea=MathCalc.cutPlotAreaYByK(plotArea,curPoint1,boxPoint);
                }else if(orientation=='V'){
                    plotArea=MathCalc.cutPlotAreaXByK(plotArea,curPoint1,boxPoint);
                }
            }


            //Создание новых точек
            float xShift=0;
            float yShift=0;
            if(orientation=='V'){
                xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);
            }else if(orientation=='H'){
                yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);
            }

            //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
            //zzz
            newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
            newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

            //Рекурсивный сдвиг Arrow связанных с arrow
            recursionShift(ind,arrow, orientation, plotArea, xShift, yShift);

            //Перемещение объекта Branch(изменение координат parentArrow и childArrow), если были выбраны sourcePoint и nextSourcePoint Arrow
            //Для этого объекты parentArrow и childArrow должны были быть инициализированы
            if(parentArrow!=null && childArrow!=null){
                //branchPointShift(parentArrow,childArrow,xShift,yShift);
            }


            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            arrow.setArrowCoord(path);
            /*
            if(arrow.coord.path.size()>2) {
                if(arrow.coord.path.size()!=4) {
                    jointArrowPoints(arrow);
                }else{
                    equalizeArrow(arrow);
                }
            }
            */
            //--------------------------------------


            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }

        //Получение исходной области построения для arrow
        public RectF getSourcePlotArea(Arrow arrow,char orientation){
           RectF plotArea=null;
           Arrow curArrow = arrow;


            if(curArrow.source.getConnectorSource() instanceof Box){
                Box box=(Box)curArrow.source.getConnectorSource();
                ArrayList<PointF> pointList=box.coord.getPointList();
                plotArea=MathCalc.buildPlotAreaHorizontal(pointList.remove(0),pointList.remove(0));
            }

           //Выполняем перемещение в сторону source, пока есть объект Branch
           while(curArrow.source.getConnectorSource() instanceof Branch || curArrow.source.getConnectorSource() instanceof Join) {
               if(curArrow.source.getConnectorSource() instanceof Branch) {
                   Branch curBranch = (Branch) curArrow.source.getConnectorSource();
                   curArrow = (Arrow) curBranch.parent;
                   PointF sinkPoint = curArrow.coord.path.get(curArrow.coord.path.size() - 1);
                   PointF prevSinkPoint = curArrow.coord.path.get(curArrow.coord.path.size() - 2);
                   char curOrientation = MathCalc.getOrientationByPoints(sinkPoint, prevSinkPoint);
                   //Если ориентация конечных точек Arrow предка, не соответствует ориентации
                   //выбранных двух точек arrow
                   if (curOrientation != orientation) {

                       for (Arrow child : curBranch.childList) {
                           PointF childSourcePoint = child.coord.path.get(0);
                           PointF childNextSourcePoint = child.coord.path.get(1);
                           char childOrientation = MathCalc.getOrientationByPoints(childSourcePoint, childNextSourcePoint);
                           //Если ориентация потомка текущего предка, схожа с ориентацией предка,
                           //то расширяем область построения
                           if (curOrientation == childOrientation || curOrientation == ' ') {

                               if (curOrientation == 'H') {
                                   plotArea = MathCalc.buildPlotAreaVertical(prevSinkPoint, sinkPoint);
                               } else if (curOrientation == 'V') {
                                   plotArea = MathCalc.buildPlotAreaHorizontal(prevSinkPoint, sinkPoint);
                                   //В случае, если точки текущего предка равны
                               } else if (curOrientation == ' ') {
                                   if (orientation == 'V') {
                                       plotArea = MathCalc.buildPlotAreaVertical(prevSinkPoint, sinkPoint);
                                   } else if (orientation == 'H') {
                                       plotArea = MathCalc.buildPlotAreaHorizontal(prevSinkPoint, sinkPoint);
                                   }
                               }

                               plotArea = MathCalc.resizePlotArea(plotArea, childNextSourcePoint);

                           }
                       }


                       //Ограничение для plotArea
                       //plotArea была построена не по объекту Box
                       if(plotArea!=null){
                           if(curOrientation=='H'){
                               //была построена вертикальная область
                               plotArea=MathCalc.cutPlotAreaHorizontal(plotArea);
                           }else if(curOrientation=='V'){
                               //была построена горизонтальная область
                               plotArea=MathCalc.cutPlotAreaVertical(plotArea);
                           }
                       }

                       //Область построения найдена, выход из цикла.
                       break;
                   } else if (curArrow.coord.path.size()==2) {
                       //Если Arrow предка состоит из двух точек,
                       //то проверяем, связана, ли она с Box
                       if(curArrow.source.getConnectorSource() instanceof Box) {
                           //Если все выбранные потомки были в одной ориентации, и
                           //последний из них заканчивается Box'ом
                           Box box = (Box) curArrow.source.getConnectorSource();
                           ArrayList<PointF> pointList = box.coord.getPointList();
                           plotArea = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));
                           //Область построения найдена, выход из цикла.
                           break;
                       }
                   }else{
                       //Не нашлось подходящего предка, завершаем цикл
                       break;
                   }
               }else if(curArrow.source.getConnectorSource() instanceof Join){
                   Join curJoin = (Join) curArrow.source.getConnectorSource();
                   curArrow=null;

                   for (Arrow parent : curJoin.parentList) {
                       if (parent.coord.path.size() == 2) {
                           PointF sinkPoint = parent.coord.path.get(parent.coord.path.size()-1);
                           PointF prevSinkPoint = parent.coord.path.get(parent.coord.path.size()-2);
                           char curOrientation = MathCalc.getOrientationByPoints(prevSinkPoint, sinkPoint);
                           if (orientation == curOrientation) {
                               curArrow = parent;
                               break;
                           }
                       }
                   }

                   if(curArrow==null){
                       //Не нашлось подходящего предка, завершаем цикл
                       break;
                   }else if(curArrow.source.getConnectorSource() instanceof Box){
                       Box box = (Box) curArrow.source.getConnectorSource();
                       ArrayList<PointF> pointList = box.coord.getPointList();
                       plotArea = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));
                       break;
                   }

               }
           }

           return plotArea;
        }

        //Получение конечной области построения для arrow
        //Объектом для построения такой области, будет Box (для Branch)
        //Пока что Box
        public RectF getSinkPlotArea(Arrow arrow,char orientation){
            RectF plotArea=null;
            Arrow curArrow = arrow;

            if(curArrow.sink.getConnectorSink() instanceof Box){
                Box box=(Box)curArrow.sink.getConnectorSink();
                ArrayList<PointF> pointList=box.coord.getPointList();
                if(curArrow.sink.getConnectorType().equals("I")){
                    plotArea=MathCalc.buildPlotAreaHorizontal(pointList.remove(0),pointList.remove(0));
                }else{
                    plotArea=MathCalc.buildPlotAreaVertical(pointList.remove(0),pointList.remove(0));
                }
            }

            while(curArrow.sink.getConnectorSink() instanceof Branch || curArrow.sink.getConnectorSink() instanceof Join) {
                if(curArrow.sink.getConnectorSink() instanceof Branch) {
                    Branch curBranch = (Branch) curArrow.sink.getConnectorSink();
                    //Обнуляем указатель на следуюшую Arrow, пока не найдём подходящего потомка
                    curArrow = null;
                    for (Arrow child : curBranch.childList) {
                        if (child.coord.path.size() == 2) {
                            PointF sourcePoint = child.coord.path.get(0);
                            PointF nextSourcePoint = child.coord.path.get(1);
                            char curOrientation = MathCalc.getOrientationByPoints(sourcePoint, nextSourcePoint);
                            if (orientation == curOrientation) {
                                curArrow = child;
                                break;
                            }
                        }
                    }

                    //Если не нашлось подходящего потомка, то завершаем обход
                    if (curArrow == null) {
                        break;
                        //Иначе, если curArrow указывает на Box, то строим plotArea и выходим из цикла
                    } else if (curArrow.sink.getConnectorSink() instanceof Box) {
                        Box box = (Box) curArrow.sink.getConnectorSink();
                        ArrayList<PointF> pointList = box.coord.getPointList();
                        if (curArrow.sink.getConnectorType().equals("I")) {
                            plotArea = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));
                        } else {
                            plotArea = MathCalc.buildPlotAreaVertical(pointList.remove(0), pointList.remove(0));
                        }
                        break;
                    }
                }else if(curArrow.sink.getConnectorSink() instanceof Join){
                    Join curJoin = (Join) curArrow.sink.getConnectorSink();
                    curArrow = (Arrow) curJoin.child;
                    PointF sourcePoint = curArrow.coord.path.get(0);
                    PointF nextSourcePoint = curArrow.coord.path.get(1);
                    char curOrientation = MathCalc.getOrientationByPoints(sourcePoint, nextSourcePoint);
                    if (curOrientation != orientation) {
                        /*
                        if (curOrientation == 'H') {
                            plotArea = MathCalc.buildPlotAreaVertical(sourcePoint, nextSourcePoint);
                        } else if (curOrientation == 'V') {
                            plotArea = MathCalc.buildPlotAreaHorizontal(sourcePoint, nextSourcePoint);
                        } else if (curOrientation == ' ') {
                            if (orientation == 'V') {
                                plotArea = MathCalc.buildPlotAreaVertical(sourcePoint, nextSourcePoint);
                            } else if (orientation == 'H') {
                                plotArea = MathCalc.buildPlotAreaHorizontal(sourcePoint, nextSourcePoint);
                            }
                        }
                        */

                        for (Arrow parent : curJoin.parentList) {
                            PointF parentSinkPoint = parent.coord.path.get(parent.coord.path.size()-1);
                            PointF parentPrevSinkPoint = parent.coord.path.get(parent.coord.path.size()-2);
                            char parentOrientation = MathCalc.getOrientationByPoints(parentPrevSinkPoint, parentSinkPoint);
                            if (curOrientation == parentOrientation || curOrientation == ' ') {

                                if (curOrientation == 'H') {
                                    plotArea = MathCalc.buildPlotAreaVertical(sourcePoint, nextSourcePoint);
                                } else if (curOrientation == 'V') {
                                    plotArea = MathCalc.buildPlotAreaHorizontal(sourcePoint, nextSourcePoint);
                                } else if (curOrientation == ' ') {
                                    if (orientation == 'V') {
                                        plotArea = MathCalc.buildPlotAreaVertical(sourcePoint, nextSourcePoint);
                                    } else if (orientation == 'H') {
                                        plotArea = MathCalc.buildPlotAreaHorizontal(sourcePoint, nextSourcePoint);
                                    }
                                }

                                plotArea = MathCalc.resizePlotArea(plotArea, parentPrevSinkPoint);
                            }
                        }

                        //Ограничение для plotArea
                        //plotArea была построена не по объекту Box
                        if(plotArea!=null){
                            if(curOrientation=='H'){
                                //была построена вертикальная область
                                plotArea=MathCalc.cutPlotAreaHorizontal(plotArea);
                            }else if(curOrientation=='V'){
                                //была построена горизонтальная область
                                plotArea=MathCalc.cutPlotAreaVertical(plotArea);
                            }
                        }

                        break;
                    } else if (curArrow.sink.getConnectorSink() instanceof Box) {
                        Box box = (Box) curArrow.sink.getConnectorSink();
                        ArrayList<PointF> pointList = box.coord.getPointList();
                        if(curArrow.sink.getConnectorType().equals("I")) {
                            plotArea = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));
                        }else{
                            plotArea = MathCalc.buildPlotAreaVertical(pointList.remove(0), pointList.remove(0));
                        }
                        break;
                    }
                }
            }
            return plotArea;
        }

        //Старый метод
        /*
         public void moveArrow(Arrow arrow,PointF touchPoint,Branch branch,Box box) {
             //ArrayList<PointF> path = MathCalc.docPointsToScreen(arrow.coord.path);
             ArrayList<PointF> path = arrow.coord.path;

             int j = 0;
             double distance = -1;
             int ind = 0;
             PointF curPoint1 = new PointF();
             PointF curPoint2 = new PointF();
             for (int i = 0; i < path.size() - 1; i++) {
                 j = i + 1;

                 PointF p1 = path.get(i);
                 PointF p2 = path.get(j);
                 double d = MathCalc.minDistance(touchPoint, p1, p2);
                 if (distance == (-1) || distance > d) {
                     distance = d;
                     curPoint1 = p1;
                     curPoint2 = p2;
                     ind = i;
                 }
             }

             PointF sourcePoint = arrow.source.getConnectorPoint();
             PointF sinkPoint = arrow.sink.getConnectorPoint();

             RectF plotArea = new RectF();
             RectF plotArea1 = new RectF();
             RectF plotArea2 = new RectF();
             PointF newPoint1 = new PointF();
             PointF newPoint2 = new PointF();

             Arrow parentArrow=null;
             Arrow childArrow=null;

             //Построение plotArea
             char orientation=MathCalc.getOrientationByPoints(curPoint1,curPoint2);
             //Если выбранные точки являются начальными в arrow
             if(curPoint1.x==sourcePoint.x && curPoint1.y==sourcePoint.y){
                if(MathCalc.getOrientationByPoints(curPoint1,curPoint2)=='H'){
                    plotArea1=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
                }else if(MathCalc.getOrientationByPoints(curPoint1,curPoint2)=='V'){
                    plotArea1=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
                }
                plotArea=plotArea1;

                Branch curBranch;
                parentArrow=arrow;
                //Определение plotArea, через plotArea1, plotArea2 и иногда plotArea3
                //plotArea - результат
                //plotArea1 - область построенная по выбранным двум точкам
                //plotArea2 - область построенная по Arrow предка и/или потомка
                //plotArea3 - область построенная по Box
                do{
                    curBranch=(Branch)parentArrow.source.getConnectorSource();
                    parentArrow=(Arrow)curBranch.parent;
                    PointF parentPrevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
                    PointF parentSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
                    char curOrientation=MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint);
                    if(curOrientation!=' '){
                        //Построение области перемещения
                        if(curOrientation!=orientation){
                            if(curOrientation=='H'){
                                plotArea2=MathCalc.buildPlotAreaVertical(parentPrevSinkPoint,parentSinkPoint);
                            }else if(curOrientation=='V'){
                                plotArea2=MathCalc.buildPlotAreaHorizontal(parentPrevSinkPoint,parentSinkPoint);
                            }

                            //Поиск Arrow потомка текущего Branch, с ориентацией как у предка, для расширения plotArea
                            for(Arrow child : curBranch.childList){
                                PointF childSourcePoint=child.coord.path.get(0);
                                PointF childNextSourcePoint=child.coord.path.get(1);
                                char childOrientation=MathCalc.getOrientationByPoints(childSourcePoint,childNextSourcePoint);
                                if(childOrientation==curOrientation){
                                    childArrow=child;
                                    MathCalc.resizePlotArea(plotArea2,childNextSourcePoint);
                                    break;
                                }
                            }

                            //Умеьшить область plotArea2 по горизонтали, либо по вертикали
                            if(curOrientation=='H'){
                                plotArea2=MathCalc.cutPlotAreaHorizontal(plotArea2);
                            }else if(curOrientation=='V'){
                                plotArea2=MathCalc.cutPlotAreaVertical(plotArea2);
                            }

                            plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                            break;
                        }
                    }
                    parentArrow=(Arrow)curBranch.parent;
                }while(parentArrow.source.getConnectorSource() instanceof Branch);

                 //Если arrow состоит из двух точек, то нужно не дать ей выйти за пределы box
                 if(arrow.coord.path.size()==2){
                     RectF plotArea3=new RectF();
                     ArrayList<PointF> boxCoord=box.coord.getPointList();
                     if(orientation=='H'){
                         plotArea3=MathCalc.buildPlotAreaHorizontal(boxCoord.remove(0),boxCoord.remove(0));
                     }else if(orientation=='V'){
                         plotArea3=MathCalc.buildPlotAreaVertical(boxCoord.remove(0), boxCoord.remove(0));
                     }
                     plotArea=MathCalc.intersectionPlotArea(plotArea,plotArea3);
                 }

             }else{
                if(orientation=='V'){
                    plotArea1=MathCalc.buildPlotAreaHorizontal(curPoint1,curPoint2);
                    plotArea=plotArea1;
                    //Если выбранные точки являются конечными в arrow
                    if(ind==path.size() - 2){
                        ArrayList<PointF> boxCoord=box.coord.getPointList();
                        plotArea2=MathCalc.buildPlotAreaVertical(boxCoord.remove(0),boxCoord.remove(0));
                        plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);
                    }else{
                        //Если точки являются не начальными, и не конечными,
                        //то ограничеть область по sinkPoint
                        plotArea=MathCalc.cutPlotAreaXByK(plotArea1,curPoint1,arrow.sink.getConnectorPoint());
                    }
                }else if(orientation=='H'){
                    plotArea1=MathCalc.buildPlotAreaVertical(curPoint1,curPoint2);
                    plotArea=plotArea1;
                    //Если выбранные точки являются конечными в arrow
                    if(ind==path.size() - 2){
                        ArrayList<PointF> boxCoord=box.coord.getPointList();
                        plotArea2=MathCalc.buildPlotAreaHorizontal(boxCoord.remove(0),boxCoord.remove(0));
                        plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);
                    }else{
                        //Если точки являются не начальными, и не конечными,
                        //то ограничеть область по sinkPoint
                        plotArea=MathCalc.cutPlotAreaYByK(plotArea1,curPoint1,arrow.sink.getConnectorPoint());
                    }
                }
             }

             //Создание новых точек
             float xShift=0;
             float yShift=0;
             if(orientation=='V'){
                 xShift = touchPoint.x - curPoint1.x;
                 newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                 newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);
             }else if(orientation=='H'){
                 yShift = touchPoint.y - curPoint1.y;
                 newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                 newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);
             }

             //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
             //zzz
             newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
             newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);
             //newPoint1=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint1);
             //newPoint2=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint2);

             //Рекурсивный сдвиг Arrow связанных с arrow
             recursionShift(ind,arrow, orientation, plotArea, xShift, yShift);

             //Перемещение объекта Branch(изменение координат parentArrow и childArrow), если были выбраны sourcePoint и nextSourcePoint Arrow
             //Для этого объекты parentArrow и childArrow должны были быть инициализированы
             if(parentArrow!=null && childArrow!=null){
                 //branchPointShift(parentArrow,childArrow,xShift,yShift);
             }


             path.set(ind, newPoint1);
             path.set(ind + 1, newPoint2);

             //zzz--------------------------------------
             path=removeLoop(path);
             arrow.setArrowCoord(path);
             if(arrow.coord.path.size()>2) {
                 if(arrow.coord.path.size()!=4) {
                     jointArrowPoints(arrow);
                 }else{
                     equalizeArrow(arrow);
                 }
             }
             //--------------------------------------


             //Изменение точки соединения Arrow и Squiggle
             if(arrow.squiggle!=null){
                 arrow.squiggle.newArrowPoint();
             }
         }
         */

         //Перемещение связующей точки между parentArrow и childArrow
         public void branchPointShift(Arrow parentArrow,Arrow childArrow,float xShift,float yShift){
             //ArrayList<PointF> parentPath=MathCalc.docPointsToScreen(parentArrow.coord.path);
             //ArrayList<PointF> childPath=MathCalc.docPointsToScreen(childArrow.coord.path);
             ArrayList<PointF> parentPath=parentArrow.coord.path;
             ArrayList<PointF> childPath=childArrow.coord.path;

             PointF sinkPoint=parentPath.get(parentPath.size()-1);
             PointF sourcePoint=childPath.get(0);

             PointF newSinkPoint=new PointF(sinkPoint.x+xShift,sinkPoint.y+yShift);
             PointF newSourcePoint=new PointF(sourcePoint.x+xShift,sourcePoint.y+yShift);

             Log.w("Sink point",String.valueOf(newSinkPoint.x)+" "+String.valueOf(newSinkPoint.y));
             Log.w("Source point",String.valueOf(newSourcePoint.x)+" "+String.valueOf(newSourcePoint.y));

             parentPath.set(parentPath.size() - 1, newSinkPoint);
             childPath.set(0, newSourcePoint);

             parentArrow.setArrowCoord(parentPath);
             childArrow.setArrowCoord(childPath);
         }

         public void recursionShift(int ind,Arrow arrow,char orientation,RectF plotArea,float xShift,float yShift){
             Log.w("Start plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));
             //Сдвиг Arrow
             if(ind==arrow.coord.path.size()-2) {
                 recursionSinkArrowShift(arrow, orientation, plotArea, xShift, yShift);
             }
             Log.w("Child shift plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));
             if(ind==0){
                 recursionSourceArrowShift(arrow, orientation, plotArea, xShift, yShift);
             }

             Log.w("Parent shift plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));
             //Сдвиг связанных точек
             if(ind==arrow.coord.path.size()-2) {
                 recursionSinkPointShift(arrow, orientation, plotArea, xShift, yShift);
             }
             if(ind==0) {
                 recursionSourcePointShift(arrow, orientation, plotArea, xShift, yShift);
             }
             Log.w("Parent point plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));

         }

         public void recursionSinkPointShift(Arrow arrow,char orientation,RectF plotArea,float xShift,float yShift){
             if(arrow.sink.getConnectorSink() instanceof Branch){
                 Branch branch=(Branch)arrow.sink.getConnectorSink();
                 //Arrow по которому будет идти обход
                 Arrow curArrow=null;

                 //Если child имеет другую ориентацию, то нужно выполнить сдвиг sourcePoint
                 for(Arrow child : branch.childList){
                    ArrayList<PointF> childPath=child.coord.path;
                    PointF childSourcePoint=childPath.get(0);
                    PointF childNextSourcePoint=childPath.get(1);
                    char childOrientation = MathCalc.getOrientationByPoints(childSourcePoint,childNextSourcePoint);
                    //Если направление Arrow потомка совпадает с направлением orientation,
                    // и потомок состоит из двух точек, то он будет использован в дальнейшем
                    if(childOrientation==orientation){
                        if(child.coord.path.size()==2) {
                            curArrow = child;
                        }
                    }else{
                        //Если направление Arrow потомка отличается
                        //, то выполняем сдвиг его sourcePoint
                        PointF newChildSourcePoint=null;
                        if(childOrientation=='H'){
                            newChildSourcePoint=new PointF(childSourcePoint.x+xShift,childSourcePoint.y);
                        }else if(childOrientation=='V'){
                            newChildSourcePoint=new PointF(childSourcePoint.x,childSourcePoint.y+yShift);
                        //Если две точки текущего потомка равны,
                        //то выполняем сдвиг в сторону противоположную orientation
                        }else if(childOrientation==' '){
                            if(orientation=='H'){
                                newChildSourcePoint=new PointF(childSourcePoint.x,childSourcePoint.y+yShift);
                            }else if(orientation=='V'){
                                newChildSourcePoint=new PointF(childSourcePoint.x+yShift,childSourcePoint.y);
                            }
                        }
                        newChildSourcePoint=MathCalc.pointsToPlotAreaLimit(plotArea,newChildSourcePoint);
                        //newChildSourcePoint=MathCalc.pointsToPlotAreaLimitLess(plotArea,newChildSourcePoint);
                        childPath.set(0,newChildSourcePoint);
                        child.setArrowCoord(childPath);
                    }
                 }
                 if(curArrow!=null){
                     recursionSinkPointShift(curArrow,orientation,plotArea,xShift,yShift);
                 }
             }else if(arrow.sink.getConnectorSink() instanceof Join){
                 Join join=(Join)arrow.sink.getConnectorSink();
                 Arrow childArrow=(Arrow)join.child;
                 char curOrientation=' ';

                 for(Arrow parent : join.parentList){
                     if(parent!=arrow){
                         ArrayList<PointF> parentPath=parent.coord.path;
                         PointF parentSinkPoint=parentPath.get(parentPath.size()-1);
                         PointF parentPrevSinkPoint=parentPath.get(parentPath.size()-2);

                         curOrientation=MathCalc.getOrientationByPoints(parentSinkPoint,parentPrevSinkPoint);
                         PointF newParentSinkPoint=null;
                         if(curOrientation!=' ') {
                             if (curOrientation != orientation) {
                                 if (curOrientation == 'H') {
                                     newParentSinkPoint = new PointF(parentSinkPoint.x + xShift, parentSinkPoint.y);
                                 } else if (curOrientation == 'V') {
                                     newParentSinkPoint = new PointF(parentSinkPoint.x, parentSinkPoint.y + yShift);
                                 }
                             }
                         }else{
                             if(orientation=='H'){
                                 newParentSinkPoint=new PointF(parentSinkPoint.x,parentSinkPoint.y+yShift);
                             }else if(orientation=='V'){
                                 newParentSinkPoint=new PointF(parentSinkPoint.x+xShift,parentSinkPoint.y);
                             }
                         }

                         if(newParentSinkPoint!=null){
                             newParentSinkPoint=MathCalc.pointsToPlotAreaLimit(plotArea,newParentSinkPoint);
                             //newChildSourcePoint=MathCalc.pointsToPlotAreaLimitLess(plotArea,newChildSourcePoint);
                             parentPath.set(parentPath.size()-1,newParentSinkPoint);
                             parent.setArrowCoord(parentPath);
                         }
                     }
                 }


                 ArrayList<PointF> childPath=childArrow.coord.path;
                 PointF childSourcePoint=childPath.get(0);
                 PointF childNextSourcePoint=childPath.get(1);

                 curOrientation=MathCalc.getOrientationByPoints(childSourcePoint,childNextSourcePoint);
                 PointF newChildSourcePoint=null;
                 if(curOrientation!=' ') {
                     if (curOrientation != orientation) {
                         if (curOrientation == 'H') {
                             newChildSourcePoint = new PointF(childSourcePoint.x + xShift, childSourcePoint.y);
                         } else if (curOrientation == 'V') {
                             newChildSourcePoint = new PointF(childSourcePoint.x, childSourcePoint.y + yShift);
                         }
                     }
                 }else{
                     //Если две точки равны
                     if(orientation=='H'){
                         newChildSourcePoint=new PointF(childSourcePoint.x,childSourcePoint.y+yShift);
                     }else if(orientation=='V'){
                         newChildSourcePoint=new PointF(childSourcePoint.x+xShift,childSourcePoint.y);
                     }
                 }

                 if(newChildSourcePoint!=null) {
                     newChildSourcePoint=MathCalc.pointsToPlotAreaLimit(plotArea,newChildSourcePoint);
                     childPath.set(0,newChildSourcePoint);
                     childArrow.setArrowCoord(childPath);
                 }else{
                     recursionSinkPointShift(childArrow,orientation,plotArea,xShift,yShift);
                 }
             }
         }

         //Метод сдвигает sinkPoint предка, и sourcePoint его потомка,
         //у которых отличается ориентация от текущей arrow
         public void recursionSourcePointShift(Arrow arrow,char orientation,RectF plotArea,float xShift,float yShift){
             if(arrow.source.getConnectorSource() instanceof Branch){
                 Branch branch=(Branch)arrow.source.getConnectorSource();
                 Arrow parentArrow=(Arrow)branch.parent;
                 char curOrientation=' ';

                 //Если child имеет другую ориентацию, то нужно выполнить сдвиг sourcePoint
                 for(Arrow child : branch.childList){
                     if(child!=arrow){
                        ArrayList<PointF> childPath=child.coord.path;
                        PointF childSourcePoint=childPath.get(0);
                        PointF childNextSourcePoint=childPath.get(1);
                        //plotArea=MathCalc.resizePlotArea(plotArea,childNextSourcePoint);
                        //Log.w("plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));

                        //Неправильный результат, если две точки во входных параметрах, будут совпадать
                        curOrientation=MathCalc.getOrientationByPoints(childSourcePoint,childNextSourcePoint);
                        PointF newChildSourcePoint=null;
                        //Если две точки не равны
                        if(curOrientation!=' ') {
                            //Если child имеет другую ориентацию, то нужно выполнить сдвиг его sourcePoint,
                            if (curOrientation != orientation) {
                                if (curOrientation == 'H') {
                                    newChildSourcePoint = new PointF(childSourcePoint.x + xShift, childSourcePoint.y);
                                } else if (curOrientation == 'V') {
                                    newChildSourcePoint = new PointF(childSourcePoint.x, childSourcePoint.y + yShift);
                                }

                                //Если начальные точки потомка, текущего родителя равны, то выполнить сдвиг sourcePoint
                                //в сторону ориентации обратной orientation
                            }
                        }else{
                            //Если две точки равны
                            if(orientation=='H'){
                                newChildSourcePoint=new PointF(childSourcePoint.x,childSourcePoint.y+yShift);
                            }else if(orientation=='V'){
                                newChildSourcePoint=new PointF(childSourcePoint.x+xShift,childSourcePoint.y);
                            }
                        }

                        if(newChildSourcePoint!=null){
                            newChildSourcePoint=MathCalc.pointsToPlotAreaLimit(plotArea,newChildSourcePoint);
                            //newChildSourcePoint=MathCalc.pointsToPlotAreaLimitLess(plotArea,newChildSourcePoint);
                            childPath.set(0,newChildSourcePoint);
                            child.setArrowCoord(childPath);
                        }
                     }
                 }


                 ArrayList<PointF> parentPath=parentArrow.coord.path;
                 int size=parentPath.size();
                 PointF parentPrevSinkPoint=parentPath.get(size-2);
                 PointF parentSinkPoint=parentPath.get(size-1);
                 //plotArea=MathCalc.resizePlotArea(plotArea,parentPrevSinkPoint);
                 //Log.w("plotArea",String.valueOf(plotArea.left)+String.valueOf(plotArea.top)+String.valueOf(plotArea.right)+String.valueOf(plotArea.bottom));

                 curOrientation=MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint);
                 PointF newParentSinkPoint=null;
                 //Если две точки не равны
                 if(curOrientation!=' ') {
                     //Если parentArrow имеет другую ориентацию, то нужно выполнить сдвиг его sinkPoint,
                     if (curOrientation != orientation) {
                         if (curOrientation == 'H') {
                             newParentSinkPoint = new PointF(parentSinkPoint.x + xShift, parentSinkPoint.y);
                         } else if (curOrientation == 'V') {
                             newParentSinkPoint = new PointF(parentSinkPoint.x, parentSinkPoint.y + yShift);
                         }
                         //Если конечные точки текущего родителя равны, то выполнить сдвиг sinkPoint
                         //в сторону ориентации обратной orientation
                     }
                 }else{
                     //Если две точки равны
                     if(orientation=='H'){
                         newParentSinkPoint=new PointF(parentSinkPoint.x,parentSinkPoint.y+yShift);
                     }else if(orientation=='V'){
                         newParentSinkPoint=new PointF(parentSinkPoint.x+xShift,parentSinkPoint.y);
                     }
                 }

                 if(newParentSinkPoint!=null) {
                     newParentSinkPoint=MathCalc.pointsToPlotAreaLimit(plotArea,newParentSinkPoint);
                     //newParentSinkPoint=MathCalc.pointsToPlotAreaLimitLess(plotArea,newParentSinkPoint);
                     parentPath.set(size-1,newParentSinkPoint);
                     parentArrow.setArrowCoord(parentPath);
                 }else{
                     recursionSourcePointShift(parentArrow,orientation,plotArea,xShift,yShift);
                 }
             }else if(arrow.source.getConnectorSource() instanceof Join){
                 Join join=(Join)arrow.source.getConnectorSource();
                 Arrow curArrow=null;

                 for(Arrow parent : join.parentList){
                     ArrayList<PointF> parentPath=parent.coord.path;
                     PointF parentSinkPoint=parentPath.get(parentPath.size()-1);
                     PointF parentPrevSinkPoint=parentPath.get(parentPath.size()-2);
                     char parentOrientation = MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint);
                     if(parentOrientation==orientation){
                         if(parent.coord.path.size()==2) {
                             curArrow = parent;
                         }
                     }else{
                         PointF newParentSinkPoint=null;
                         if(parentOrientation=='H'){
                             newParentSinkPoint=new PointF(parentSinkPoint.x+xShift,parentSinkPoint.y);
                         }else if(parentOrientation=='V'){
                             newParentSinkPoint=new PointF(parentSinkPoint.x,parentSinkPoint.y+yShift);
                         }else if(parentOrientation==' '){
                             if(orientation=='H'){
                                 newParentSinkPoint=new PointF(parentSinkPoint.x,parentSinkPoint.y+yShift);
                             }else if(orientation=='V'){
                                 newParentSinkPoint=new PointF(parentSinkPoint.x+yShift,parentSinkPoint.y);
                             }
                         }
                         newParentSinkPoint=MathCalc.pointsToPlotAreaLimit(plotArea,newParentSinkPoint);
                         parentPath.set(parentPath.size()-1,newParentSinkPoint);
                         parent.setArrowCoord(parentPath);
                     }
                 }
                 if(curArrow!=null){
                     recursionSourcePointShift(curArrow,orientation,plotArea,xShift,yShift);
                 }
             }
         }

         public void recursionSinkArrowShift(Arrow arrow,char orientation,RectF plotArea,float xShift,float yShift){
            if(arrow.sink.getConnectorSink() instanceof Branch){
                Branch branch=(Branch)arrow.sink.getConnectorSink();
                for(Arrow child : branch.childList){
                    PointF sourcePoint=child.coord.path.get(0);
                    PointF nextSourcePoint=child.coord.path.get(1);
                    char curOrientation=MathCalc.getOrientationByPoints(sourcePoint,nextSourcePoint);
                    if(curOrientation!=' ') {
                        if (curOrientation == orientation) {
                            int ind = 0;
                            RectF plotArea1 = MathCalc.resizePlotArea(plotArea, nextSourcePoint);
                            moveArrow(ind, child, plotArea1, xShift, yShift);
                            if (child.coord.path.size() == 2) {
                                recursionSinkArrowShift(child, orientation, plotArea, xShift, yShift);
                            }
                        }
                    }
                }

            }else if(arrow.sink.getConnectorSink() instanceof Join){
                Join join=(Join)arrow.sink.getConnectorSink();
                Arrow childArrow=(Arrow)join.child;
                PointF sourcePoint=childArrow.coord.path.get(0);
                PointF nextSourcePoint=childArrow.coord.path.get(1);
                char curOrientation=MathCalc.getOrientationByPoints(sourcePoint,nextSourcePoint);

                if(curOrientation!=' ') {
                    if (curOrientation == orientation) {
                        int ind = 0;
                        RectF plotArea1 = MathCalc.resizePlotArea(plotArea, nextSourcePoint);
                        moveArrow(ind, childArrow, plotArea1, xShift, yShift);
                        if (childArrow.coord.path.size() == 2) {
                            recursionSinkArrowShift(childArrow, orientation, plotArea, xShift, yShift);
                        }
                    }
                }

                //Для сдвтга Arrow T-связи
                Arrow parentArrow=null;
                for(Arrow parent : join.parentList) {
                    if(parent!=arrow){
                        parentArrow=parent;
                    }
                }

                PointF prevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
                PointF sinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
                curOrientation=MathCalc.getOrientationByPoints(prevSinkPoint,sinkPoint);

                if(curOrientation!=' '){
                    if (curOrientation == orientation) {
                        int ind = parentArrow.coord.path.size()-2;
                        RectF plotArea1 = MathCalc.resizePlotArea(plotArea, prevSinkPoint);
                        moveArrow(ind, parentArrow, plotArea1, xShift, yShift);
                        recursionSourcePointShift(parentArrow, orientation, plotArea, xShift, yShift);
                        if (parentArrow.coord.path.size() == 2) {
                            recursionSourceArrowShift(parentArrow, orientation, plotArea, xShift, yShift);
                        }
                    }
                }
            }

         }

         //Сдвигает parentArrow, если parentArrow ориентирован так же, как и arrow
         public void recursionSourceArrowShift(Arrow arrow,char orientation,RectF plotArea,float xShift,float yShift){
             if(arrow.source.getConnectorSource() instanceof Branch){
                 Branch branch=(Branch)arrow.source.getConnectorSource();
                 Arrow parentArrow=(Arrow)branch.parent;
                 PointF sinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-1);
                 PointF prevSinkPoint=parentArrow.coord.path.get(parentArrow.coord.path.size()-2);
                 char curOrientation=MathCalc.getOrientationByPoints(prevSinkPoint,sinkPoint);

                 //Защита для правильного определения ориентации
                 //Если координаты двух точек равны, то не нужно выполнять сдвиг данной Arrow
                 if(curOrientation!=' ') {
                     if (curOrientation == orientation) {
                         int ind = parentArrow.coord.path.size() - 2;
                         //Расширяет область, для сдвига Arrow предка
                         RectF plotArea1 = MathCalc.resizePlotArea(plotArea, prevSinkPoint);
                         moveArrow(ind, parentArrow, plotArea1, xShift, yShift);
                         //Можно продолжить рекурсивный сдвиг после предков,
                         //у которых имеется только две точки
                         if (parentArrow.coord.path.size() == 2) {
                             recursionSourceArrowShift(branch.parent, orientation, plotArea, xShift, yShift);
                         }
                     }
                 }

                 //Для сдвтга Arrow T-связи
                 Arrow childArrow=null;
                 for(Arrow child : branch.childList){
                     if(child!=arrow){
                         childArrow=child;
                     }
                 }

                 PointF sourcePoint=childArrow.coord.path.get(0);
                 PointF sourceNextPoint=childArrow.coord.path.get(1);
                 curOrientation=MathCalc.getOrientationByPoints(sourcePoint,sourceNextPoint);
                 if(curOrientation!=' '){
                     if(curOrientation==orientation){
                         int ind = 0;
                         RectF plotArea1 = MathCalc.resizePlotArea(plotArea, sourceNextPoint);
                         moveArrow(ind, parentArrow, plotArea1, xShift, yShift);
                         recursionSinkPointShift(childArrow, orientation, plotArea, xShift, yShift);
                         if (childArrow.coord.path.size() == 2) {
                             recursionSinkArrowShift(childArrow, orientation, plotArea, xShift, yShift);
                         }
                     }
                 }

             }else if(arrow.source.getConnectorSource() instanceof Join){
                 Join join=(Join)arrow.source.getConnectorSource();
                 for(Arrow parent : join.parentList){
                     int ind = parent.coord.path.size() - 2;
                     PointF sinkPoint=parent.coord.path.get(parent.coord.path.size()-1);
                     PointF prevSinkPoint=parent.coord.path.get(parent.coord.path.size()-2);
                     char curOrientation=MathCalc.getOrientationByPoints(prevSinkPoint,sinkPoint);
                     if(curOrientation!=' ') {
                         if (curOrientation == orientation) {
                             RectF plotArea1 = MathCalc.resizePlotArea(plotArea, prevSinkPoint);
                             moveArrow(ind, parent, plotArea1, xShift, yShift);
                             if (parent.coord.path.size() == 2) {
                                 recursionSourceArrowShift(parent, orientation, plotArea, xShift, yShift);
                             }
                         }
                     }
                 }
             }
         }

         public void moveArrow(Arrow arrow,PointF touchPoint,Box box1,Box box2) {
            ArrayList<PointF> path = arrow.coord.path;

            int j = 0;
            double distance = -1;
            int ind = 0;
            PointF curPoint1 = new PointF();
            PointF curPoint2 = new PointF();
            for (int i = 0; i < path.size() - 1; i++) {
                j = i + 1;

                PointF p1 = path.get(i);
                PointF p2 = path.get(j);
                double d = MathCalc.minDistance(touchPoint, p1, p2);
                if (distance == (-1) || distance > d) {
                    distance = d;
                    curPoint1 = p1;
                    curPoint2 = p2;
                    ind = i;
                }
            }

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();
            String firstDir = MathCalc.getDirectionByBoxConnectorType(arrow.source.getConnectorType());
            String lastDir = MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());
            PointF curVec = new PointF(curPoint2.x - curPoint1.x, curPoint2.y - curPoint1.y);
            PointF firstVec = MathCalc.getNormVecByDirection(firstDir);
            PointF lastVec = MathCalc.getNormVecByDirection(lastDir);

            RectF plotArea1 = new RectF();
            PointF newPoint1 = new PointF();
            PointF newPoint2 = new PointF();
            if (curPoint1.x == curPoint2.x) {
                plotArea1 = MathCalc.buildPlotAreaHorizontal(curPoint1, curPoint2);

                float xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);

            } else {
                plotArea1 = MathCalc.buildPlotAreaVertical(curPoint1, curPoint2);

                float yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);

            }


            RectF plotArea2 = new RectF();
            RectF plotArea = new RectF();
            //Проверка на двухточечную Arrow
            if(arrow.coord.path.size()==2){
                ind=0;
                ArrayList<PointF> pointList1=box1.coord.getPointList();
                ArrayList<PointF> pointList2=box2.coord.getPointList();
                plotArea2=MathCalc.intersectionPlotArea(MathCalc.buildPlotAreaHorizontal(pointList1.remove(0),pointList1.remove(0)),MathCalc.buildPlotAreaHorizontal(pointList2.remove(0),pointList2.remove(0)));
                plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                //Не дать точкам выйти за границы области plotArea
                newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);


            //Проверка на SourcePoint
            }else if(ind == 0) {
                if(newPoint1.x == newPoint2.x){
                    //Попадание сюда невозможно
                    ArrayList<PointF> pointList=box1.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaVertical(pointList.remove(0), pointList.remove(0));
                }else{
                    ArrayList<PointF> pointList=box1.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));

                }
                plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                //Не дать точкам выйти за границы области plotArea
                newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

                //Проверка на SinkPoint
            }else if(ind==(path.size()-2)){
                if(newPoint1.x == newPoint2.x){
                    ArrayList<PointF> pointList=box2.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaVertical(pointList.remove(0),pointList.remove(0));
                }else{
                    ArrayList<PointF> pointList=box2.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaHorizontal(pointList.remove(0),pointList.remove(0));
                }
                plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                //Не дать точкам выйти за границы области plotArea
                newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);
            }else{
                plotArea=plotArea1;

                //Ограничение по точке sourcePoint, если текущий вектор и начальный вектор перпендикулярны
                if(!MathCalc.isOneDirection(firstVec,curVec)){
                    PointF invFirstVec=MathCalc.getNormVecByDirection(MathCalc.getInverseDirection(firstDir));
                    if(!MathCalc.isOneDirection(invFirstVec,curVec)){
                        if(newPoint1.x==newPoint2.x) {
                            plotArea = MathCalc.cutPlotAreaX(plotArea1,curPoint1,sourcePoint);
                        }else{
                            plotArea = MathCalc.cutPlotAreaY(plotArea1,curPoint1,sourcePoint);
                        }
                    }
                }

                //Ограничение по точке sinkPoint, если текущий вектор и конечный вектор перпендикулярны
                if(!MathCalc.isOneDirection(lastVec,curVec)){
                    PointF invLastVec=MathCalc.getNormVecByDirection(MathCalc.getInverseDirection(lastDir));
                    if(!MathCalc.isOneDirection(invLastVec,curVec)){
                        if(newPoint1.x==newPoint2.x) {
                            plotArea = MathCalc.cutPlotAreaX(plotArea1,curPoint1,sinkPoint);
                        }else{
                            plotArea = MathCalc.cutPlotAreaY(plotArea1,curPoint1,sinkPoint);
                        }
                    }
                }

                //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
                newPoint1=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint2);
            }

            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            arrow.setArrowCoord(path);
            if(arrow.coord.path.size()>2) {
                if(arrow.coord.path.size()!=4) {
                    jointArrowPoints(arrow);
                }else{
                    equalizeArrow(arrow);
                }
            }
            //--------------------------------------


            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                arrow.squiggle.newArrowPoint();
            }
        }

        public void moveArrow(Arrow arrow,PointF touchPoint,Box box,Border border) {
            ArrayList<PointF> path = arrow.coord.path;
            float xShift=0;
            float yShift=0;
            char orientation=' ';

            int j = 0;
            double distance = -1;
            int ind = 0;
            PointF curPoint1 = new PointF();
            PointF curPoint2 = new PointF();
            for (int i = 0; i < path.size() - 1; i++) {
                j = i + 1;

                PointF p1 = path.get(i);
                PointF p2 = path.get(j);
                double d = MathCalc.minDistance(touchPoint, p1, p2);
                if (distance == (-1) || distance > d) {
                    distance = d;
                    curPoint1 = p1;
                    curPoint2 = p2;
                    ind = i;
                }
            }

            PointF sourcePoint = arrow.source.getConnectorPoint();
            PointF sinkPoint = arrow.sink.getConnectorPoint();
            PointF limitPoint=new PointF();
            PointF curVec = new PointF(curPoint2.x - curPoint1.x, curPoint2.y - curPoint1.y);
            String firstDir=new String();
            if(arrow.source.getConnectorSource()==box) {
                firstDir = MathCalc.getDirectionByBoxConnectorType(arrow.source.getConnectorType());
                limitPoint=sourcePoint;
            }else{
                firstDir = MathCalc.getDirectionByBoxConnectorType(arrow.sink.getConnectorType());
                limitPoint=sinkPoint;
            }
            PointF firstVec = MathCalc.getNormVecByDirection(firstDir);

            RectF plotArea1 = new RectF();
            PointF newPoint1=new PointF();
            PointF newPoint2=new PointF();
            if (curPoint1.x == curPoint2.x) {
                plotArea1 = MathCalc.buildPlotAreaHorizontal(curPoint1, curPoint2);

                orientation='H';
                xShift = touchPoint.x - curPoint1.x;
                newPoint1 = new PointF(curPoint1.x + xShift, curPoint1.y);
                newPoint2 = new PointF(curPoint2.x + xShift, curPoint2.y);

            } else {
                plotArea1 = MathCalc.buildPlotAreaVertical(curPoint1, curPoint2);

                orientation='V';
                yShift = touchPoint.y - curPoint1.y;
                newPoint1 = new PointF(curPoint1.x, curPoint1.y + yShift);
                newPoint2 = new PointF(curPoint2.x, curPoint2.y + yShift);

            }


            RectF plotArea2=new RectF();
            RectF plotArea=new RectF();
            //Проверка на SourcePoint
            if(ind == 0 && arrow.source.getConnectorSource()==box) {
                if(newPoint1.x == newPoint2.x){
                    ArrayList<PointF> pointList=box.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaVertical(pointList.remove(0), pointList.remove(0));
                }else{
                    ArrayList<PointF> pointList=box.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaHorizontal(pointList.remove(0), pointList.remove(0));

                }
                plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                //Не дать точкам выйти за границы области plotArea
                newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);

                //Проверка на SinkPoint
            }else if(ind==(path.size()-2) && arrow.sink.getConnectorSink()==box){
                if(newPoint1.x == newPoint2.x){
                    ArrayList<PointF> pointList=box.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaVertical(pointList.remove(0),pointList.remove(0));
                }else{
                    ArrayList<PointF> pointList=box.coord.getPointList();
                    plotArea2 = MathCalc.buildPlotAreaHorizontal(pointList.remove(0),pointList.remove(0));
                }
                plotArea=MathCalc.intersectionPlotArea(plotArea1,plotArea2);

                //Не дать точкам выйти за границы области plotArea
                newPoint1=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimit(plotArea,newPoint2);
            }else{

                plotArea=plotArea1;
                if(!MathCalc.isOneDirection(firstVec,curVec)){
                    PointF invFirstVec=MathCalc.getNormVecByDirection(MathCalc.getInverseDirection(firstDir));
                    if(!MathCalc.isOneDirection(invFirstVec,curVec)){
                        if(newPoint1.x==newPoint2.x) {
                            plotArea = MathCalc.cutPlotAreaX(plotArea1,curPoint1,limitPoint);
                        }else{
                            plotArea = MathCalc.cutPlotAreaY(plotArea1,curPoint1,limitPoint);
                        }
                    }
                }

                //Не дать точкам выйти за границы области plotArea, c уменьшением области предела
                newPoint1=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint1);
                newPoint2=MathCalc.pointsToPlotAreaLimitLess(plotArea,newPoint2);
            }

            path.set(ind, newPoint1);
            path.set(ind + 1, newPoint2);

            //zzz--------------------------------------
            path=removeLoop(path);
            //path=removeExcessPoints(path);
            //path=jointArrowPoints(path);
            arrow.setArrowCoord(path);
            //jointArrowPoints(arrow);
            if(arrow.coord.path.size()>2) {
                if (arrow.coord.path.size() != 4) {
                    jointArrowPoints(arrow);
                } else {
                    equalizeArrow(arrow);
                }
            }
            //--------------------------------------

            //Изменение точки соединения Arrow и Squiggle
            if(arrow.squiggle!=null){
                //arrow.squiggle.moveArrowPoint(xShift,yShift,orientation);
                arrow.squiggle.newArrowPoint();
            }

        }

        //Метод вызывает ошибку
         public void jointArrowPoints(Arrow arrow){
             newArrow.clearNewArrow();
             newArrow.setSource(arrow.source.getConnectorPoint(),arrow.source.getConnectorType(),arrow.source.getConnectorSource());
             newArrow.setSink(arrow.sink.getConnectorPoint(),arrow.sink.getConnectorType(),arrow.sink.getConnectorSink());

             ArrayList<PointF> path=arrow.coord.path;
             //ArrayList<PointF> newPath=new ArrayList<PointF>();
             PointF current=new PointF();
             PointF next=new PointF();
             for(int i=1;i<path.size();i++){
                 int j=i-1;
                 current=path.get(j);
                 next=path.get(i);

                 if((i!=(path.size()-1)) && (j!=0)) {
                     if (Math.abs(current.x - next.x) < 0.001) {
                         if (Math.abs(current.y - next.y) < 3) {
                             Arrow buildArrow=buildPath(false);
                             arrow.setArrowCoord(buildArrow.coord.path);
                             break;
                         }
                     } else if (Math.abs(current.y - next.y) < 0.001) {
                         if (Math.abs(current.x - next.x) < 3) {
                             Arrow buildArrow=buildPath(false);
                             arrow.setArrowCoord(buildArrow.coord.path);
                             break;
                         }
                     }
                 }
             }
             newArrow.clearNewArrow();
             newArrow.stage="start";
         }


         public Arrow getArrowByNumber(int number){
            for(Arrow arrow : this.arrowSet){
                if(arrow.number==number){
                    return arrow;
                }
            }
            return null;
         }

         //Объединяет Arrow, возвращает путь
         public ArrayList<PointF> mergeArrow(Arrow parent,Arrow child){
             ArrayList<PointF> path=new ArrayList<PointF>();
             path.addAll(parent.coord.path);
             for(int i=1;i<child.coord.path.size();i++) {
                 PointF point=child.coord.path.get(i);
                 path.add(point);
             }
             path=removeExcessPoints(path);
             return path;
         }

         //Объединяет child сливается с parent по branch
         public Arrow mergeArrowByBranch(Arrow parent,Arrow child,Branch branch){
             branch.parent=null;
             branch.removeChild(child);
             removeBranch(branch);
             if(parent!=null && child!=null){
                ArrayList<PointF> path=mergeArrow(parent,child);
                parent.setArrowCoord(path);

                parent.sink.setConnectorSink(child.sink.getConnectorSink());
                parent.sink.setConnectorType(child.sink.getConnectorType());
                //Точку устанавливать предку не нужно
                //parent.sink.setConnectorPoint(child.sink.getConnectorPoint());
                parent.sink.setRelationNum(child.sink.getRelationNum());

                arrowSet.remove(child);
                if(parent.sink.getConnectorSink() instanceof Branch){
                   Branch sinkBranch=(Branch)parent.sink.getConnectorSink();
                   sinkBranch.parent=parent;
                }else if(parent.sink.getConnectorSink() instanceof Join){
                    Join sinkJoin=(Join)parent.sink.getConnectorSink();
                    sinkJoin.removeParent(child);
                    sinkJoin.addParent(parent);
                }
             }
             return parent;
         }

        //Объединяет child сливается с parent по branch
        public Arrow mergeArrowByJoin(Arrow parent,Arrow child,Join join){
            join.removeParent(parent);
            join.child=null;
            removeJoin(join);
            if(parent!=null && child!=null){
                ArrayList<PointF> path=mergeArrow(parent,child);
                parent.setArrowCoord(path);

                parent.sink.setConnectorSink(child.sink.getConnectorSink());
                parent.sink.setConnectorType(child.sink.getConnectorType());
                //Точку устанавливать предку не нужно
                //parent.sink.setConnectorPoint(child.sink.getConnectorPoint());
                parent.sink.setRelationNum(child.sink.getRelationNum());

                arrowSet.remove(child);
                if(parent.sink.getConnectorSink() instanceof Branch){
                    Branch sinkBranch=(Branch)parent.sink.getConnectorSink();
                    sinkBranch.parent=parent;
                }else if(parent.sink.getConnectorSink() instanceof Join){
                    Join sinkJoin=(Join)parent.sink.getConnectorSink();
                    sinkJoin.removeParent(child);
                    sinkJoin.addParent(parent);
                }
            }
            return parent;
        }

         //Разделение Arrow на два Arrow по точке
         public ArrayList<ArrayList<PointF>> divArrowByPoint(Arrow arrow,PointF divPoint){
            ArrayList<ArrayList<PointF>> pathList=null;
            ArrayList<PointF> pointList=arrow.coord.path;
            ArrayList<PointF> parentList=new ArrayList<PointF>();
            ArrayList<PointF> childList=new ArrayList<PointF>();
            int ind=0;
            boolean createNew=false;
            for(int i=0;i<pointList.size()-1;i++){
                int j=i+1;
                ind=i;
                PointF current=pointList.get(i);
                PointF next=pointList.get(j);

                parentList.add(current);
                if(current.x==next.x && divPoint.x==current.x){
                    createNew=true;
                    if(current.y!=divPoint.y) {
                        parentList.add(new PointF(divPoint.x,divPoint.y));
                    }
                    break;
                }else if(current.y==next.y && divPoint.y==current.y){
                    createNew=true;
                    if(current.x!=divPoint.x) {
                        parentList.add(new PointF(divPoint.x,divPoint.y));
                    }
                    break;
                }
            }

            if(createNew) {
                pathList = new ArrayList<ArrayList<PointF>>();

                if(pointList.get(ind+1).x!=divPoint.x || pointList.get(ind+1).y!=divPoint.y) {
                    childList.add(new PointF(divPoint.x,divPoint.y));
                }

                for(int i=(ind+1);i<pointList.size();i++){
                    childList.add(pointList.get(i));
                }

                pathList.add(parentList);
                pathList.add(childList);

            }
            return pathList;
         }


        //Разделение Arrow на два Arrow по точке,
        //Меняет ссылки Arrow на Branch
        public ArrayList<Arrow> divArrowByBranch(Arrow arrow,Branch branch,PointF divPoint){
            ArrayList<Arrow> arrowList=null;

            //Получаем список путей для двух Arrow
            ArrayList<ArrayList<PointF>> pathList = divArrowByPoint(arrow, divPoint);
            if(pathList!=null) {
                ArrayList<PointF> parentPath = pathList.remove(0);
                ArrayList<PointF> childPath = pathList.remove(0);

                Arrow parentArrow = arrow;
                Arrow childArrow = parentArrow.copy();
                addArrow(childArrow);

                parentArrow.setArrowCoord(parentPath);
                childArrow.setArrowCoord(childPath);

                //Устанавливаем Source для childArrow
                childArrow.setArrowNumber(newArrowNumber());
                childArrow.source.setConnectorSource(branch);
                childArrow.source.setConnectorType(null);
                branch.addChild(childArrow);

                //parentArrow больше не родитель Branch
                //теперь родитель childArrow
                if (parentArrow.sink.getConnectorSink() instanceof Branch) {
                    Branch sinkBranch = (Branch) parentArrow.sink.getConnectorSink();
                    sinkBranch.setParent(childArrow);
                }

                //parentArrow больше не потомок Join
                //теперь потомок childArrow
                if (parentArrow.sink.getConnectorSink() instanceof Join) {
                    Join sinkJoin = (Join) parentArrow.sink.getConnectorSink();
                    sinkJoin.removeParent(parentArrow);
                    sinkJoin.addParent(childArrow);
                }

                //Устанавливаем Sink для parentArrow
                parentArrow.sink.setConnectorSink(branch);
                parentArrow.sink.setConnectorType(null);
                branch.setParent(parentArrow);

                arrowList=new ArrayList<Arrow>();
                arrowList.add(parentArrow);
                arrowList.add(childArrow);
            }
            return arrowList;
        }


        //Разделение Arrow на два Arrow по точке,
        //Меняет ссылки Arrow на Join
        public ArrayList<Arrow> divArrowByJoin(Arrow arrow,Join join,PointF divPoint){
            ArrayList<Arrow> arrowList=null;

            //Получаем список путей для двух Arrow
            ArrayList<ArrayList<PointF>> pathList = divArrowByPoint(arrow, divPoint);
            if(pathList!=null) {
                ArrayList<PointF> parentPath = pathList.remove(0);
                ArrayList<PointF> childPath = pathList.remove(0);

                Arrow parentArrow = arrow;
                Arrow childArrow = parentArrow.copy();
                addArrow(childArrow);

                parentArrow.setArrowCoord(parentPath);
                childArrow.setArrowCoord(childPath);

                //Устанавливаем Source для childArrow
                childArrow.setArrowNumber(newArrowNumber());
                childArrow.source.setConnectorSource(join);
                childArrow.source.setConnectorType(null);
                join.setChild(childArrow);

                //parentArrow больше не родитель Branch
                //теперь родитель childArrow
                if (parentArrow.sink.getConnectorSink() instanceof Branch) {
                    Branch sinkBranch = (Branch) parentArrow.sink.getConnectorSink();
                    sinkBranch.setParent(childArrow);
                }

                //parentArrow больше не потомок Join
                //теперь потомок childArrow
                if (parentArrow.sink.getConnectorSink() instanceof Join) {
                    Join sinkJoin = (Join) parentArrow.sink.getConnectorSink();
                    sinkJoin.removeParent(parentArrow);
                    sinkJoin.addParent(childArrow);
                }

                //Устанавливаем Sink для parentArrow
                parentArrow.sink.setConnectorSink(join);
                parentArrow.sink.setConnectorType(null);
                join.addParent(parentArrow);

                arrowList=new ArrayList<Arrow>();
                arrowList.add(parentArrow);
                arrowList.add(childArrow);
            }
            return arrowList;
        }

         public void addBranch(Branch branch){
            branchSet.add(branch);
         }


         public boolean existBranch(Branch branch){
            for(Branch br : branchSet){
                if(br==branch){
                    return true;
                }
            }
            return false;
         }

         public Branch getBranchByArrowNumber(int arrowNumber){
            for(Branch branch : branchSet){
                if(branch.parentArrowNumber==arrowNumber){
                    return branch;
                }
            }
            return null;
         }

         public void removeBranch(Branch branch){
            branchSet.remove(branch);
         }

         public void addJoin(Join join){
             joinSet.add(join);
         }

         public boolean existJoin(Join join){
             for(Join jo : joinSet){
                 if(jo==join){
                     return true;
                 }
             }
             return false;
         }

         public Join getJoinByArrowNumber(int arrowNumber){
             for(Join join : joinSet){
                 if(join.childArrowNumber==arrowNumber){
                     return join;
                 }
             }
             return null;
         }

         public void removeJoin(Join join){
             joinSet.remove(join);
         }

    }

    static class BoxLimit{
        int minLimit;
        int maxLimit;

        BoxLimit(int minLimit,int maxLimit){
            this.minLimit=minLimit;
            this.maxLimit=maxLimit;
        }

        BoxLimit(BoxLimit limit){
            this.minLimit=limit.minLimit;
            this.maxLimit=limit.maxLimit;
        }

        public int getMinLimit(){
            return minLimit;
        }

        public void setMinLimit(int minLimit){
            this.minLimit=minLimit;
        }

        public int getMaxLimit(){
            return maxLimit;
        }

        public void setMaxLimit(int maxLimit){
            this.maxLimit=maxLimit;
        }
    }

    static class Suspicion{
        Object object;
        double distance;

        Suspicion(Object object,double distance){
            this.object=object;
            this.distance=distance;
        }

        Suspicion(Object object){
            this.object=object;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Suspicion) {
                //if(this.object.getClass().getSimpleName()=="Box") {
                if (this.object instanceof Box && ((Suspicion) o).object instanceof Box) {
                    Box box1 = (Box) this.object;
                    Box box2 = (Box) ((Suspicion) o).object;

                    if (box1.number == box2.number)
                        return true;
                    else
                        return false;
                    //}else if(this.object.getClass().getSimpleName()=="Arrow"){
                }else if(this.object instanceof Arrow.ArrowLabel && ((Suspicion) o).object instanceof Arrow.ArrowLabel){
                    Arrow.ArrowLabel label1 = (Arrow.ArrowLabel) this.object;
                    Arrow.ArrowLabel label2 = (Arrow.ArrowLabel) ((Suspicion) o).object;

                    if (label1 == label2)
                        return true;
                    else
                        return false;
                }else if (this.object instanceof Arrow && ((Suspicion) o).object instanceof Arrow) {
                    Arrow arrow1 = (Arrow) this.object;
                    Arrow arrow2 = (Arrow) ((Suspicion) o).object;

                    if (arrow1.number == arrow2.number)
                        return true;
                    else
                        return false;
                }else if(this.object instanceof Arrow.ArrowSource && ((Suspicion) o).object instanceof Arrow.ArrowSource) {
                    Arrow.ArrowSource source1 = (Arrow.ArrowSource) this.object;
                    Arrow.ArrowSource source2 = (Arrow.ArrowSource) ((Suspicion) o).object;

                    if (source1 == source2)
                        return true;
                    else
                        return false;
                }else if(this.object instanceof Arrow.ArrowSink && ((Suspicion) o).object instanceof Arrow.ArrowSink){
                    Arrow.ArrowSink sink1 = (Arrow.ArrowSink) this.object;
                    Arrow.ArrowSink sink2 = (Arrow.ArrowSink) ((Suspicion) o).object;

                    if (sink1 == sink2)
                        return true;
                    else
                        return false;
                }else{
                    return false;
                }

            }
            //return super.equals(o);
            return false;
        }
    }

    static class SuspicionComparator implements Comparator<Suspicion>{
        @Override
        public int compare(Suspicion suspicion, Suspicion suspicion2) {
            if(suspicion.distance<suspicion2.distance)
                return -1;
            else
                return 1;
        }
    }

    static class Box{
        int number;
        String name;
        BoxCoord coord;
        int diagramId;
        Diagram reference;
        BoxConnector left;
        BoxConnector top;
        BoxConnector right;
        BoxConnector bottom;
        //BoxText boxText;

        Box(){

        }

        Box(int number,String name,BoxCoord coord){
            this.number=number;
            this.name=name;
            this.coord=coord;

            left=new BoxConnector("I");
            top=new BoxConnector("C");
            right=new BoxConnector("O");
            bottom=new BoxConnector("M");
        }

        //Конструктор для Box создаваемого инструментами в документе
        Box(PointF touchPoint,int number,String name,int diagramId) {
            float kx=0.088f;
            float ky=0.118f;

            PointF p1=new PointF(touchPoint.x,touchPoint.y);
            PointF p2=new PointF(p1.x+kx,p1.y-ky);

            this.number=number;
            this.name=name;
            this.coord=new BoxCoord(p1,p2);
            this.diagramId=diagramId;

            left=new BoxConnector("I");
            top=new BoxConnector("C");
            right=new BoxConnector("O");
            bottom=new BoxConnector("M");
        }

        Box(Box box){
            this.number=box.number;
            this.name=new String(box.name);
            this.coord=new BoxCoord(box.coord);

            left=new BoxConnector("I");
            top=new BoxConnector("C");
            right=new BoxConnector("O");
            bottom=new BoxConnector("M");
        }

        Box(int number,String name,BoxCoord coord,int diagramId){
            this.number=number;
            this.name=name;
            this.coord=coord;
            this.diagramId=diagramId;

            left=new BoxConnector("I");
            top=new BoxConnector("C");
            right=new BoxConnector("O");
            bottom=new BoxConnector("M");
        }

        public void setBoxReference(Diagram diagram){
            reference=diagram;
        }

        public BoxConnector getBoxConnectorByType(String type){
            if(type=="I"){
                return left;
            }else if(type=="C"){
                return top;
            }else if(type=="O"){
                return right;
            }else if(type=="M"){
                return bottom;
            }
            return null;
        }


        public void setBoxCoord(PointF p1,PointF p2){
            this.coord=new BoxCoord(p1.x,p1.y,p2.x,p2.y);
        }

        public void setBoxText(String text){
            name=text;
        }

        public String getBoxText(){
            return name;
        }

        //Возвращает текст без <CR>
        public String getParseBoxText(){
            String text=name;
            text=text.replace("<CR>"," ");
            return text;
        }

        //Меняет Enter на <CR>
        public void setParseBoxText(String text){
            name=text.replace("\r","<CR>");
        }

        static class BoxCoord{
            PointF p1;
            PointF p2;

            BoxCoord(float x1,float y1,float x2,float y2){
                PointF p1=new PointF(x1,y1);
                PointF p2=new PointF(x2,y2);
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,p2);
                p1=pointList.remove(0);
                p2=pointList.remove(0);

                this.p1=p1;
                this.p2=p2;
            }

            BoxCoord(PointF p1,PointF p2){
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,p2);
                p1=pointList.remove(0);
                p2=pointList.remove(0);

                this.p1=p1;
                this.p2=p2;
            }

            BoxCoord(float[] coord){
                PointF p1=new PointF(coord[0],coord[1]);
                PointF p2=new PointF(coord[2],coord[3]);
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,p2);
                p1=pointList.remove(0);
                p2=pointList.remove(0);

                this.p1=p1;
                this.p2=p2;
            }

            BoxCoord(BoxCoord coord){
                PointF p1=new PointF(coord.p1.x,coord.p1.y);
                PointF p2=new PointF(coord.p2.x,coord.p2.y);
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,p2);
                p1=pointList.remove(0);
                p2=pointList.remove(0);

                this.p1=p1;
                this.p2=p2;
            }

            public ArrayList<PointF> getPointList(){
                ArrayList<PointF> pointList=new ArrayList<PointF>();
                pointList.add(p1);
                pointList.add(p2);
                return pointList;
            }

            public ArrayList<PointF> getAllPointList(){
                ArrayList<PointF> pointList=new ArrayList<PointF>();
                pointList.add(p1);
                pointList.add(p2);
                PointF np1=new PointF(p1.x,p2.y);
                PointF np2=new PointF(p2.x,p1.y);
                pointList.add(np1);
                pointList.add(np2);
                return pointList;
            }

        }

        static class BoxText{
            String text;

            BoxText(String text){
                this.text=text;
            }
        }

        static class BoxConnector{
            String type;

            BoxConnector(String type) {
                this.type = type;
            }

            public void setBoxConnectorType(String type){
                this.type=type;
            }

            public String getBoxConnectorType(){
                return type;
            }

        }
    }

    static class NewArrow extends Arrow{
        //stage может быть только "start", либо "end"
        String stage;

        NewArrow(){
            super();
            setStage("start");
        }

        public void setStage(String stage){
            this.stage=stage;
        }

        public String getStage(){
            return stage;
        }

        public void nextStage(){
            if(stage.equals("start")){
                setStage("end");
            }else{
                setStage("start");;
            }
        }

        public int size(){
            int i=0;
            if(source!=null){
                if(source.getConnectorPoint()!=null) {
                    i++;
                }
            }
            if(sink!=null){
                if(sink.getConnectorPoint()!=null) {
                    i++;
                }
            }
            return i;
        }


        public void clearNewArrow(){
            number=0;
            source.setConnectorPoint(null);
            source.setConnectorType(null);
            source.setConnectorSource(null);
            source.setRelationNum(0);
            sink.setConnectorPoint(null);
            sink.setConnectorType(null);
            sink.setConnectorSink(null);
            sink.setRelationNum(0);
        }

        public ArrowSource getSource(){
            if(source!=null){
                return source;
            }else{
                return null;
            }
        }

        public void setSource(PointF point,String type,Object obj){
            point=toArrowWorkSpace(point);
            source.setConnectorPoint(point);
            source.setConnectorType(type);
            source.setConnectorSource(obj);
        }

        public void setSource(PointF point,String type,Object obj,int relationNum){
            point=toArrowWorkSpace(point);
            source.setConnectorPoint(point);
            source.setConnectorType(type);
            source.setConnectorSource(obj);
            source.relationNum=relationNum;
        }


        public ArrowSink getSink(){
            if(sink!=null){
                return sink;
            }else{
                return null;
            }
        }

        public void setSink(PointF point,String type,Object obj){
            point=toArrowWorkSpace(point);
            sink.setConnectorPoint(point);
            sink.setConnectorType(type);
            sink.setConnectorSink(obj);
        }

        public void setSink(PointF point,String type,Object obj,int relationNum){
            point=toArrowWorkSpace(point);
            sink.setConnectorPoint(point);
            sink.setConnectorType(type);
            sink.setConnectorSink(obj);
            sink.relationNum=relationNum;
        }
    }


    static class Arrow{
        int number;
        ArrowSource source;
        ArrowSink sink;
        String sourceString;
        String sinkString;
        ArrowCoord coord;
        ArrowLabel label;
        ArrowSquiggle squiggle;

        Arrow(){
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord();
        }

        Arrow(int number){
            this.number=number;
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord();
        }

        Arrow(int number,ArrowLabel label){
            this.number=number;
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord();
            this.label=label;
        }

        Arrow(int number,ArrowSource source,ArrowSink sink){
            this.number=number;
            this.source=source;
            this.sink=sink;
            coord=new ArrowCoord();
        }

        //Конструктор копирования для метода copy
        Arrow(int number,ArrowSource source,ArrowSink sink,ArrowCoord coord){
            this.number=number;
            this.source=new ArrowSource(source);
            this.sink=new ArrowSink(sink);
            this.coord=new ArrowCoord(coord);
        }

        Arrow(Arrow arrow){
            this.number=arrow.number;
            this.source=new ArrowSource(arrow.source);
            this.sink=new ArrowSink(arrow.sink);
            this.coord=new ArrowCoord(arrow.coord);
            if(arrow.label!=null) {
                this.label = new ArrowLabel(arrow.label);
            }
            if(arrow.squiggle!=null) {
                this.squiggle = new ArrowSquiggle(arrow.squiggle);
            }
        }

        //начальный конструктор для Arrow
        Arrow(int number,String sourceString,String sinkString,float[] coord){
            this.number=number;
            this.sourceString=sourceString;
            this.sinkString=sinkString;
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord(coord);
            //setConnector();
        }

        //начальный конструктор для Arrow
        Arrow(int number,String sourceString,String sinkString,float[] coord,ArrowLabel label){
            this.number=number;
            this.sourceString=sourceString;
            this.sinkString=sinkString;
            this.label=label;
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord(coord);
            //setConnector();
        }

        //начальный конструктор для Arrow
        Arrow(int number,String sourceString,String sinkString,float[] coord,ArrowLabel label,ArrowSquiggle squiggle){
            this.number=number;
            this.sourceString=sourceString;
            this.sinkString=sinkString;
            this.label=label;
            this.squiggle=squiggle;
            source=new ArrowSource();
            sink=new ArrowSink();
            this.coord=new ArrowCoord(coord);
        }

        public void setArrowNumber(int number){
            if(number>=0) {
                this.number = number;
            }
        }

        public void setArrowCoord(float[] coord){
            this.coord=new ArrowCoord(coord);
            this.coord.setSourceConnector();
            this.coord.setSinkConnector();
        }

        public void setArrowCoord(ArrayList<PointF> pointList){
            this.coord=new ArrowCoord(pointList);
            this.coord.setSourceConnector();
            this.coord.setSinkConnector();
        }

        //Возвращает копию без полей Label и Squiggle
        public Arrow copy(){
            return new Arrow(this.number,this.source,this.sink,this.coord);
        }

        public boolean isNodalPoint(PointF point){
            for(PointF node : coord.path){
                if(node.x==point.x && node.y==point.y){
                    return true;
                }
            }
            return false;
        }

        //Если нужно задать Arrow всего две точки
        public void setArrowCoord(PointF p1,PointF p2){
            this.coord=new ArrowCoord(p1,p2);
            this.coord.setSourceConnector();
            this.coord.setSinkConnector();
        }

        public void setSourcePoint(PointF p){
            this.source.setConnectorPoint(p);
        }

        public void setSinkPoint(PointF p){
            this.sink.setConnectorPoint(p);
        }

        //Два метода для инициализации source/sink NewArrow
        public void setSourceByPoint(PointF point){

        }

        public void setSinkByPoint(PointF point){

        }


        public void addLabel(ArrowLabel label){
            this.label=label;
            label.parent=this;
            if(squiggle!=null){
                squiggle.newLabelPoint();
            }
        }

        public void addDefaultLabel(){
            PointF newPoint=new PointF(this.sink.getConnectorPoint().x-0.03f,this.sink.getConnectorPoint().y+0.03f);
            this.label=new ArrowLabel("default",newPoint);
            label.parent=this;
            if(squiggle!=null){
                squiggle.newLabelPoint();
            }
        }

        public void addDefaultLabel(String newText){
            PointF newPoint=new PointF(this.sink.getConnectorPoint().x-0.03f,this.sink.getConnectorPoint().y+0.03f);
            this.label=new ArrowLabel(newText,newPoint);
            label.parent=this;
            if(squiggle!=null){
                squiggle.newLabelPoint();
            }
        }

        public void insertLabel(ArrowLabel label,PointF point){
            if(label!=null){
                this.label=new ArrowLabel(label,point);
                this.label.parent=this;
            }
        }

        public void removeLabel(){
            label=null;
            if(squiggle!=null){
                removeSquiggle();
            }
        }

        public void addSquiggle(ArrowSquiggle squiggle){
            this.squiggle=squiggle;
            squiggle.parent=this;
        }


        public void removeSquiggle(){
            this.squiggle=null;
        }


        class ArrowCoord{
            //Set<PointF> path;
            ArrayList<PointF> path;
            PointF sourcePoint;
            PointF sinkPoint;

            ArrowCoord() {
                path=new ArrayList<PointF>();
            }

            ArrowCoord(ArrowCoord coord){
                this.path=new ArrayList<PointF>();
                for(PointF p : coord.path){
                    PointF newPoint=new PointF(p.x,p.y);
                    this.path.add(newPoint);
                }
                this.sourcePoint=this.path.get(0);
                this.sinkPoint=this.path.get(path.size()-1);
            }

            ArrowCoord(float[] coord){
                path=new ArrayList<PointF>();
                int i=0;
                int j=1;
                if(coord.length>=2) {
                    while (j < coord.length) {
                        path.add(new PointF(coord[i],coord[j]));

                        i = i + 2;
                        j = j + 2;
                    }
                }


                //Определение source/sink точек, класса ArrowCoord
                setSourcePoint(path.get(0));
                setSinkPoint(path.get(path.size()-1));
                //Определение source/sink точек, класса ArrowSource/ArrowSink
                /*
                setSourceConnector();
                setSinkConnector();
                */
            }

            ArrowCoord(ArrayList<PointF> newPath){
                path=newPath;

                //Определение source/sink точек, класса ArrowCoord
                setSourcePoint(path.get(0));
                setSinkPoint(path.get(path.size()-1));
                //Определение source/sink точек, класса ArrowSource/ArrowSink
                /*
                setSourceConnector();
                setSinkConnector();
                */
            }

            //Если Arrow нужно задать две точки
            ArrowCoord(PointF p1,PointF p2){
                path=new ArrayList<PointF>();
                path.add(p1);
                path.add(p2);

                setSourcePoint(p1);
                setSinkPoint(p2);
            }

            public void setSourcePoint(PointF p){
                if(path.size()!=0) {
                    path.set(0, p);
                }else{
                    path.add(p);
                }
                sourcePoint=p;
            }

            public PointF getSourcePoint(){
                return sourcePoint;
            }

            public void setSinkPoint(PointF p){
                if(path.size()>1) {
                    path.set(path.size() - 1, p);
                }else{
                    path.add(p);
                }
                sinkPoint=p;
            }

            public PointF getSinkPoint(){
                return sinkPoint;
            }

            public void setSourceConnector(){
                Arrow.this.source.setConnectorPoint();
            }

            public void setSinkConnector(){
                Arrow.this.sink.setConnectorPoint();
            }


        }

        class ArrowSource{
            Object obj;
            PointF point;
            String type;
            int relationNum;

            ArrowSource(){

            }

            ArrowSource(ArrowSource source){
                this.point=new PointF(source.getConnectorPoint().x,source.getConnectorPoint().y);
                if(source.getConnectorType()!=null) {
                    this.type = new String(source.getConnectorType());
                }

                this.obj=source.getConnectorSource();

                if(source.relationNum!=0){
                    this.relationNum=source.relationNum;
                }
            }

            ArrowSource(PointF point){
                this.point=point;
            }

            ArrowSource(PointF point,String type,Object source){
                this.point=point;
                this.type=type;
                this.obj=source;
            }

            ArrowSource(PointF point,String type,Object source,int relationNum){
                this.point=point;
                this.type=type;
                this.obj=source;
                this.relationNum=relationNum;
            }

            public void setConnectorPoint(PointF point){
                this.point=point;
                Arrow.this.coord.setSourcePoint(point);
            }

            public void setConnectorPoint(){
                PointF sourcePoint=Arrow.this.coord.getSourcePoint();
                this.point=sourcePoint;
            }

            public PointF getConnectorPoint(){
                return point;
            }

            public void setConnectorType(String type){
                this.type=type;
            }

            public String getConnectorType(){
                return type;
            }

            public void setConnectorSource(Object source){
                this.obj=source;
            }

            public Object getConnectorSource(){
                return obj;
            }

            public void setRelationNum(int relationNum){
                this.relationNum=relationNum;
            }

            public int getRelationNum(){
                return relationNum;
            }

        }

        class ArrowSink{
            Object obj;
            PointF point;
            String type;
            int relationNum;

            ArrowSink(){

            }

            ArrowSink(ArrowSink sink){
                this.point=new PointF(sink.getConnectorPoint().x,sink.getConnectorPoint().y);
                if(sink.getConnectorType()!=null) {
                    this.type = new String(sink.getConnectorType());
                }

                this.obj=sink.getConnectorSink();

                if(sink.relationNum!=0){
                    this.relationNum=sink.relationNum;
                }
            }

            ArrowSink(PointF point){
                this.point=point;
            }

            ArrowSink(PointF point,String type,Object sink){
                this.point=point;
                this.type=type;
                this.obj=sink;
            }

            ArrowSink(PointF point,String type,Object sink,int relationNum){
                this.point=point;
                this.type=type;
                this.obj=sink;
                this.relationNum=relationNum;
            }

            public void setConnectorPoint(PointF point){
                this.point=point;
                Arrow.this.coord.setSinkPoint(point);
            }

            public void setConnectorPoint(){
                PointF sinkPoint=Arrow.this.coord.getSinkPoint();
                this.point=sinkPoint;
            }

            public PointF getConnectorPoint(){
                return point;
            }

            public void setConnectorType(String type){
                this.type=type;
            }


            public String getConnectorType(){
                return type;
            }

            public void setConnectorSink(Object sink){
                this.obj=sink;
            }

            public Object getConnectorSink(){
                return obj;
            }

            public void setRelationNum(int relationNum){
                this.relationNum=relationNum;
            }

            public int getRelationNum(){
                return relationNum;
            }
        }

        public static class ArrowLabel{
            static float width=0.07f;
            static float height=0.02f;

            String text;
            PointF p1;
            PointF p2;
            Arrow parent;
            RectF container;

            ArrowLabel() {

            }

            //Конструктор для ArrowLabel создаваемого инструментами в документе
            ArrowLabel(PointF touchPoint) {
                text="(default)";
                setLabelCoord(touchPoint);
            }

            ArrowLabel(String text,float x,float y){
                this.text=text;
                setLabelCoord(x,y);
            }

            ArrowLabel(ArrowLabel label){
                this.text=new String(label.getText());
                this.p1=new PointF(label.p1.x,label.p1.y);
                this.p2=new PointF(label.p2.x,label.p2.y);
                this.parent=label.parent;
            }

            ArrowLabel(String text,PointF point){
                this.text=text;
                setLabelCoord(point);
            }

            ArrowLabel(ArrowLabel label,PointF point){
                this.text = new String(label.getText());
                setLabelCoord(point);
            }

            ArrowLabel(ArrowLabel label,String text){
                this.p1=new PointF(label.getPoint1().x,label.getPoint1().y);
                this.parent=label.parent;
                this.text = text;
                setLabelCoord(this.p1);
            }

            public PointF getCenterPoint(){
                float x=p1.x+(p2.x-p1.x)/2;
                float y=p1.y+(p2.y-p1.y)/2;
                return new PointF(x,y);
            }

            public void setContainer(RectF container){
                this.container=container;
            }

            public void setContainer(PointF p1,PointF p2){
                if(p1==null || p2==null){
                    Log.w("Point","Null");
                }
                this.container=new RectF(p1.x,p2.y,p2.x,p1.y);
            }

            public RectF getContainer(){
                return container;
            }


            public void setArrowParent(Arrow arrow){
                parent=arrow;
            }

            public Arrow getArrowParent(){
                return parent;
            }

            public void setPoint1(PointF p1){
                this.p1=p1;
            }

            public PointF getPoint1(){
                return p1;
            }

            public void setPoint2(PointF p2){
                this.p2=p2;
            }

            public PointF getPoint2(){
                return p2;
            }

            public void setLabelCoord(PointF p1){
                //Пределы для позиции label
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,new PointF(p1.x+getWidth(),p1.y-getHeight()));

                setPoint1(pointList.remove(0));
                setPoint2(pointList.remove(0));
            }

            public void setLabelCoord(PointF p1,PointF p2 ){
                //Пределы для позиции label
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,p2);

                setPoint1(pointList.remove(0));
                setPoint2(pointList.remove(0));
            }

            public void setLabelCoord(float x,float y){
                //Пределы для позиции label
                PointF p1=new PointF(x,y);
                ArrayList<PointF> pointList=toBoxWorkspacePoint(p1,new PointF(p1.x+getWidth(),p1.y-getHeight()));

                setPoint1(pointList.remove(0));
                setPoint2(pointList.remove(0));
            }

            public ArrayList<PointF> getAllConnectorPoint(){
                ArrayList<PointF> pointList=new ArrayList<PointF>();
                pointList.add(getLeftConnectorPoint());
                pointList.add(getTopConnectorPoint());
                pointList.add(getRightConnectorPoint());
                pointList.add(getBottomConnectorPoint());
                return pointList;
            }

            public PointF getLeftConnectorPoint(){
               PointF leftPoint=new PointF();
               if(container==null) {
                   leftPoint=new PointF(getPoint1().x, getCenterPoint().y);
               }else{
                   leftPoint=new PointF(container.left,container.top+(container.bottom-container.top)/2);
               }
               return leftPoint;
            }

            public PointF getTopConnectorPoint(){
                PointF topPoint=new PointF();
                if(container==null) {
                    topPoint=new PointF(getCenterPoint().x, getPoint2().y);
                }else{
                    topPoint=new PointF(container.left+(container.right-container.left)/2,container.top);
                }
                return topPoint;
            }

            public PointF getRightConnectorPoint(){
                PointF rightPoint=new PointF();
                if(container==null) {
                    rightPoint=new PointF(getPoint2().x,getCenterPoint().y);
                }else{
                    rightPoint=new PointF(container.right,container.top+(container.bottom-container.top)/2);
                }
                return rightPoint;
            }

            public PointF getBottomConnectorPoint(){
                PointF bottomPoint=new PointF();
                if(container==null) {
                    bottomPoint=new PointF(getCenterPoint().x,getPoint1().y);
                }else{
                    bottomPoint=new PointF(container.left+(container.right-container.left)/2,container.bottom);
                }
                return bottomPoint;
            }

            public void moveLabel(PointF touchPoint){
                PointF oldPoint=getPoint1();
                float xShift=touchPoint.x-oldPoint.x;
                float yShift=touchPoint.y-oldPoint.y;

                PointF point=new PointF(oldPoint.x+xShift,oldPoint.y+yShift);
                setLabelCoord(point);

                if(this.parent.squiggle!=null) {
                    this.parent.squiggle.newLabelPoint();
                }
            }


            public float getWidth(){
                return width;
            }

            public float getHeight(){
                return height;
            }

            public void setText(String text){
                this.text=text;
            }

            public String getText(){
                return text;
            }
        }

        public static class ArrowSquiggle{
            PointF arrowPoint;
            PointF labelPoint;
            Arrow parent;

            ArrowSquiggle(){

            }

            ArrowSquiggle(float x1,float y1,float x2,float y2){
                labelPoint=new PointF(x1,y1);
                arrowPoint=new PointF(x2,y2);
            }

            ArrowSquiggle(PointF labelPoint,PointF arrowPoint){
                this.labelPoint=labelPoint;
                this.arrowPoint=arrowPoint;
            }

            ArrowSquiggle(ArrowSquiggle squiggle){
                this.labelPoint=new PointF(squiggle.labelPoint.x,squiggle.labelPoint.y);
                this.arrowPoint=new PointF(squiggle.arrowPoint.x,squiggle.arrowPoint.y);
                this.parent=squiggle.parent;
            }

            public void setLabelPoint(PointF p){
                labelPoint=p;
            }

            public PointF getLabelPoint(){
                return labelPoint;
            }

            public void setArrowPoint(PointF p){
                arrowPoint=p;
            }

            public PointF getArrowPoint(){
                return arrowPoint;
            }

            public void setArrowParent(Arrow arrow){
                parent=arrow;
            }

            public Arrow getArrowParent(){
                return parent;
            }

            public void moveArrowPoint(float xShift,float yShift,char orientation){
                PointF arrowPoint=getArrowPoint();

                ArrayList<PointF> newPath=parent.coord.path;

                int j=0;
                boolean newPlace=true;

                for(int i=0;i<(newPath.size()-1);i++){
                    j=i+1;

                    PointF curPoint=newPath.get(i);
                    PointF nextPoint=newPath.get(j);
                    if(Math.abs(curPoint.x-arrowPoint.x)<0.001){
                        if(Math.abs(nextPoint.x-arrowPoint.x)<0.001){
                            newPlace=false;
                            break;
                        }
                    }

                    if(Math.abs(curPoint.y-arrowPoint.y)<0.001){
                        if(Math.abs(nextPoint.y-arrowPoint.y)<0.001){
                            newPlace=false;
                            break;
                        }
                    }
                }

                if(newPlace){
                    if(orientation=='V'){
                        setArrowPoint(new PointF(arrowPoint.x,arrowPoint.y+yShift));
                    }else if(orientation=='H'){
                        setArrowPoint(new PointF(arrowPoint.x+xShift,arrowPoint.y));
                    }
                }

            }

            public void newArrowPoint(){
                ArrayList<PointF> newPath=parent.coord.path;
                int centr=Math.round(newPath.size()/2);

                PointF p1=new PointF(newPath.get(centr-1).x,newPath.get(centr-1).y);
                PointF p2=new PointF(newPath.get(centr).x,newPath.get(centr).y);
                PointF newPoint=new PointF();
                if(p1.x==p2.x){
                    newPoint=new PointF(p1.x,p1.y+(p2.y-p1.y)/2);
                    setArrowPoint(newPoint);
                }else{
                    newPoint=new PointF(p1.x+(p2.x-p1.x)/2,p1.y);
                    setArrowPoint(newPoint);
                }
            }

            public void newLabelPoint(){
                PointF centerPoint=parent.label.getCenterPoint();
                PointF arrowPoint=getArrowPoint();

                ArrayList<PointF> pointList=MathCalc.labelIntersectionPoints(centerPoint,arrowPoint,parent.label);
                if(pointList.size()!=0){
                    PointF intersecPoint=pointList.get(0);
                    setLabelPoint(intersecPoint);
                }
            }

        }
    }

    static class Branch{
        int parentArrowNumber;

        Arrow parent;
        ArrayList<Arrow> childList=new ArrayList<Arrow>();

        Branch(){

        }

        Branch(int parentArrowNumber){
            this.parentArrowNumber=parentArrowNumber;
        }

        Branch(Arrow parent,ArrayList<Arrow> childList){
            this.parent=parent;
            this.childList=childList;
        }

        public void setParent(Arrow parent){
            this.parent=parent;
        }

        public void addChild(Arrow child){
            this.childList.add(child);
        }

        public void removeChild(Arrow child){
            this.childList.remove(child);
        }

        public boolean isChild(Arrow arrow){
            for(Arrow child : childList){
                if(arrow==child){
                    return true;
                }
            }
            return false;
        }

        //Проверяет childArrow на сонаправленость с parentArrow
        public boolean isOneOrientation(Arrow childArrow){
            PointF parentPrevSinkPoint=parent.coord.path.get(parent.coord.path.size()-2);
            PointF parentSinkPoint=parent.coord.path.get(parent.coord.path.size()-1);

            PointF childSourcePoint=childArrow.coord.path.get(0);
            PointF childNextSourcePoint=childArrow.coord.path.get(1);

            char parentOrientation=MathCalc.getOrientationByPoints(parentPrevSinkPoint,parentSinkPoint);
            char childOrientation=MathCalc.getOrientationByPoints(childSourcePoint,childNextSourcePoint);
            if(parentOrientation!=' ' && childOrientation!=' ') {
                if (parentOrientation == childOrientation) {
                    return true;
                }
            }
            return false;
        }

        public boolean isParent(Arrow arrow){
            if(parent==arrow){
                return true;
            }
            return false;
        }

    }

    static class Join{
        int childArrowNumber;

        Arrow child;
        ArrayList<Arrow> parentList=new ArrayList<Arrow>();

        Join(){

        }

        Join(int childArrowNumber){
            this.childArrowNumber=childArrowNumber;
        }

        Join(Arrow child,ArrayList<Arrow> parentList){
            this.child=child;
            this.parentList=parentList;
        }

        public void setChild(Arrow child){
            this.child=child;
        }

        public void addParent(Arrow parent){
            this.parentList.add(parent);
        }

        public void removeParent(Arrow parent){
            this.parentList.remove(parent);
        }

    }

    static class Border{
        String type;

        Border(){

        }

        Border(String type){
            this.type=type;
        }

        Border(Border border){
            this.type=new String(border.type);
        }
    }

}
