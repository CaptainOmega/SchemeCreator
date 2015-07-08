package com.schemecreator.denisuser.schemecreator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Denisuser on 09.11.2014.
 */
class DocumentDraw extends View {
    //Текущий холст полученный в методе onDraw
    Canvas curCanvas;

    //Текущая диаграмма
    IDEFEntity.Diagram curDiagram;
    //Размеры документа
    static int docWidth;
    static int docHeight;
    Camera camera;

    //Константа для отрисовки кривых в Arrow
    //Значение относительно модели
    final static float K1=0.015f;
    //Константа для отрисовки треугольников в Arrow
    //Значение относительно модели
    final static float K2=0.01f;
    //Константа для отрисовки текста
    //Значение относительно холста
    final static int K3=10;
    //label width
    final static int K4=200;
    //Константа для счетчика
    final static int N=200;

    CanvasTouch touch;
    Paint paint;
    Paint selectPaint;
    Paint filledPaint;
    Paint filledSelectPaint;
    Paint eraserPaint;
    TextPaint textPaint;
    TextPaint selectTextPaint;

    public DocumentDraw(Context context,AttributeSet attrs){
        super(context,attrs);
        docWidth=800;
        docHeight=600;
    }

    //единиц ширины на единицу высоты
    public double getDocAspect(){
        return (double)docWidth/(double)docHeight;
    }

    public double getCanAspect(){
        //return (double)curCanvas.getWidth()/(double)curCanvas.getHeight();
        return (double)getWidth()/(double)getHeight();
    }

    //Возвращает точку нажатия на холсте
    public PointF getCanvasPoint(){
        return new PointF(touch.touchPoint);
    }

    //Для отрисовки выделенного коннектора Arrow
    //Значение относительно холста
    public int getRadius(){
        //Часть которую будем выводить на холст
        Rect srcRect = calculateSrcRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                getWidth(), getHeight(), ScalingLogic.CROP);
        int radius=(int)(srcRect.width()*0.05f);
        if(radius<10){
            radius=10;
        }else if(radius>15){
            radius=15;
        }
        return radius;
    }

    //Перевести координаты точек с холста на модель
    //может вернуть null
    //используется для перевода касания
    public PointF pointToModel(PointF point){
        PointF modelPoint=null;

        if(getWidth()<100 || getHeight()<100){
            Log.w("Canvas","bad canvas");
        }

        if(camera.scale>=1){
            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);
            float relativeX = point.x / dstRect.width();
            float relativeY = point.y / dstRect.height();
            point=new PointF(relativeX*srcRect.width(),relativeY*srcRect.height());
            point = new PointF((point.x + srcRect.left) / docWidth, (point.y + srcRect.top) / docHeight);
            point = new PointF(point.x + camera.getCameraPoint().x, point.y + camera.getCameraPoint().y);
            if(0<point.x && point.x<1){
                if(0<point.y && point.y<1){
                    modelPoint=point;
                }
            }
        }else{
            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);
            float relativeX = point.x / dstRect.width();
            float relativeY = point.y / dstRect.height();
            point = new PointF(relativeX*srcRect.width(),relativeY*srcRect.height());
            point = new PointF(point.x+srcRect.left,point.y+srcRect.top);
            point=new PointF(point.x/docWidth*(float)camera.getWidth(),point.y/docHeight*(float)camera.getHeight());
            PointF newCameraPoint=new PointF((float)(camera.getWidth()-1)/2,(float)(camera.getHeight()-1)/2);
            point=new PointF(point.x-newCameraPoint.x,point.y-newCameraPoint.y);
            if(0<point.x && point.x<1){
                if(0<point.y && point.y<1){
                    modelPoint=point;
                }
            }
        }
        return modelPoint;
    }

    //Перевести координаты точек с холста на модель
    //может вернуть null
    public PointF pointToModel(Point p){
        PointF modelPoint=null;
        PointF point=new PointF((float)p.x,(float)p.y);
        if(camera.scale>=1){
            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);
            float relativeX = point.x / dstRect.width();
            float relativeY = point.y / dstRect.height();
            point=new PointF(relativeX*srcRect.width(),relativeY*srcRect.height());
            point = new PointF((point.x + srcRect.left) / docWidth, (point.y + srcRect.top) / docHeight);
            modelPoint = new PointF(point.x + camera.getCameraPoint().x, point.y + camera.getCameraPoint().y);
        }else{
            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);
            float relativeX = point.x / dstRect.width();
            float relativeY = point.y / dstRect.height();
            point = new PointF(relativeX*srcRect.width(),relativeY*srcRect.height());
            point = new PointF(point.x+srcRect.left,point.y+srcRect.top);
            point=new PointF(point.x/docWidth*(float)camera.getWidth(),point.y/docHeight*(float)camera.getHeight());
            PointF newCameraPoint=new PointF((float)(camera.getWidth()-1)/2,(float)(camera.getHeight()-1)/2);
            point=new PointF(point.x-newCameraPoint.x,point.y-newCameraPoint.y);
            if(0<point.x && point.x<1){
                if(0<point.y && point.y<1){
                    modelPoint=point;
                }
            }
        }
        return modelPoint;
    }


    //Перевести координаты точек с модели на холст
    public PointF pointToCanvas(PointF point){

        if(getWidth()<100 || getHeight()<100){
            Log.w("Canvas","bad canvas");
        }

        if(camera.scale>=1) {
            //Координаты точек относительно камеры
            point = new PointF(point.x - camera.getCameraPoint().x, point.y - camera.getCameraPoint().y);

            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect((int) (camera.getWidth() * docWidth), (int) (camera.getHeight() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect((int) (camera.getWidth() * docWidth), (int) (camera.getHeight() * docHeight),
                    getWidth(), getHeight(), ScalingLogic.CROP);

            //Точка относительно начала области выводимой на холст
            point = new PointF(point.x * docWidth - srcRect.left, point.y * docHeight - srcRect.top);
            //Координаты точки относительно области выводимой на холст
            float relativeX = point.x / srcRect.width();
            float relativeY = point.y / srcRect.height();
            point = new PointF(dstRect.width() * relativeX, dstRect.height() * relativeY);
        }else{
            PointF newCameraPoint=new PointF((float)(camera.getWidth()-1)/2,(float)(camera.getHeight()-1)/2);
            point=new PointF(newCameraPoint.x+point.x,newCameraPoint.y+point.y);
            point=new PointF(point.x/(float)camera.getWidth()*docWidth,point.y/(float)camera.getHeight()*docHeight);
            //Часть которую будем выводить на холст
            Rect srcRect = calculateSrcRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);
            //Часть которую определили для холста
            Rect dstRect = calculateDstRect(docWidth,docHeight,
                    getWidth(), getHeight(), ScalingLogic.CROP);

            //Точка относительно начала области выводимой на холст
            point = new PointF(point.x-srcRect.left,point.y-srcRect.top);
            //Координаты точки относительно области выводимой на холст
            float relativeX = point.x / srcRect.width();
            float relativeY = point.y / srcRect.height();
            point = new PointF(dstRect.width() * relativeX, dstRect.height() * relativeY);
        }
        return point;
    }

    //Перевод ширины относительно модели в значение относительно холста
    public float toCanvasWidth(float width){
        //Часть которую определили для холста
        Rect dstRect = calculateDstRect((int) (camera.getWidth() * docWidth),(int) (camera.getWidth() * docHeight),
                getWidth(), getHeight(), ScalingLogic.CROP);

        float canWidth=0;
        canWidth=Math.abs(width*dstRect.width());
        return canWidth;
    }

    //Перевод высоты относительно модели в значение относительно холста
    public float toCanvasHeight(float height){
        float canHeight=0;
        PointF p=new PointF(0,height);
        p=pointToCanvas(p);
        if(p!=null){
            canHeight=Math.abs(p.y);
        }
        return canHeight;
    }

    //Инициализирует объекты, необходимые для работы с холстом
    public boolean createView(DocumentActivity.CanvasTouchListener listener){
        camera=new Camera();
        touch=new CanvasTouch();
        touch.addCanvasTouchListener(listener);

        setDefaultPaint();
        setSelectPaint();
        setTextPaint();
        setSelectTextPaint();
        setFilledPaint();
        setFilledSelectPaint();
        setEraserPaint();
        return true;
    }


    public void onDraw(Canvas canvas){
        super.onDraw(canvas);

        if(canvas.getWidth()<100 || canvas.getHeight()<100){
            Log.w("Canvas","bad canvas");
        }

        Log.w("Action","onDraw");
        curCanvas=canvas;
        drawDiagram();
    }

    public void clearCanvas(){
        if(curCanvas!=null) {
            curCanvas.drawColor(Color.WHITE);
        }
    }

    //Переводит из dip в pixel
    public int dipToPixel(int dip){
        final float scale = getContext().getResources().getDisplayMetrics().density;
        int pixel = (int) (dip * scale + 0.5f);
        return pixel;
    }

    public void setDefaultPaint(){
        paint=new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
    }

    public void setSelectPaint(){
        selectPaint=new Paint();
        selectPaint.setStyle(Paint.Style.STROKE);
        selectPaint.setAntiAlias(true);
        selectPaint.setColor(Color.BLUE);
    }

    public void setTextPaint(){
        textPaint=new TextPaint();
        textPaint.setTextSize(dipToPixel(K3));
    }

    public void setSelectTextPaint(){
        selectTextPaint=new TextPaint();
        selectTextPaint.setColor(Color.BLUE);
        selectTextPaint.setTextSize(dipToPixel(K3));
    }

    public void setFilledPaint(){
        filledPaint=new Paint();
        filledPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        filledPaint.setAntiAlias(true);
        filledPaint.setColor(Color.BLACK);
    }

    public void setFilledSelectPaint(){
        filledSelectPaint=new Paint();
        filledSelectPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        filledSelectPaint.setAntiAlias(true);
        filledSelectPaint.setColor(Color.BLUE);
    }

    public void setEraserPaint(){

        eraserPaint=new Paint();
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeWidth(2);
        //eraserPaint.setAntiAlias(true);
        eraserPaint.setColor(Color.WHITE);

    }

    public void drawBoxText(IDEFEntity.Box box){
        if(box.name==null)
            return;
        if(box.name.length()==0)
            return;
        PointF canPoint1=pointToCanvas(box.coord.p1);
        PointF canPoint2=pointToCanvas(box.coord.p2);
        int leftSide=(int)(canPoint1.x);
        int topSide=(int)(canPoint2.y);
        int rightSide=(int)(canPoint2.x);
        int bottomSide=(int)(canPoint1.y);

        Rect bounds = new Rect();
        String newString=box.name.trim().replace("<CR>", "\n");

        /*
        String resultString="";
        String currentString="";
        int iSave=0;
        int jSave=0;
        int i=0;
        int j=0;

        while(i<newString.length()){
            char ch=newString.charAt(i);
            if(ch==' '){
                iSave=i;
                jSave=j;
            }
            currentString+=ch;
            textPaint.getTextBounds(currentString, 0, currentString.length(), bounds);
            if(bounds.width()>(rightSide-leftSide)){
                if(iSave!=0){
                    currentString=currentString.substring(0,jSave);
                    jSave=0;
                    j=-1;
                    i=iSave;
                    iSave=0;
                }else{
                    currentString=currentString.substring(0,currentString.length()-2);
                    i--;
                    i--;
                }
                currentString+='\n';
                resultString+=currentString;
                currentString="";
            }
            i++;
            j++;
        }

        resultString+=currentString;
        */


        String resultString="";
        String currentString="";
        String[] arrStr=newString.split("\n");

        for(int i=0;i<arrStr.length;i++) {
            currentString = arrStr[i];
            int startPos = 0;
            Integer savePos = null;
            for (int j = startPos; j < currentString.length(); j++) {
                textPaint.getTextBounds(currentString, startPos, j, bounds);
                if (bounds.width() > (rightSide - leftSide)) {
                    if (savePos == null) {
                        savePos = j - 1;
                    }
                    resultString += currentString.substring(startPos, savePos).trim() + "\n";
                    startPos = savePos.intValue();
                    savePos = null;
                    j = startPos;
                }else{
                    if (currentString.charAt(j) == ' ') {
                        savePos = j;
                    }
                }
            }
            resultString += currentString.substring(startPos, currentString.length()).trim() + "\n";
        }
        resultString.trim();

        Point boxCenter=new Point(leftSide+((rightSide-leftSide)/2),topSide+((bottomSide-topSide)/2));

        int textHeight=0;
        ArrayList<String> stringList=new ArrayList<String>();
        for(String str: resultString.split("\n")){
            textPaint.getTextBounds(str, 0, str.length(), bounds);
            textHeight+=bounds.height();
            stringList.add(str);
        }

        int yPosition=boxCenter.y-textHeight/2;

        for(String str : stringList){
            textPaint.getTextBounds(str, 0, str.length(), bounds);
            Point textCenter = new Point(bounds.centerX(),Math.abs(bounds.centerY()));
            Point textPosition= new Point(boxCenter.x-textCenter.x,yPosition);
            yPosition+=bounds.height();

            curCanvas.drawText(str, textPosition.x, textPosition.y, textPaint);
        }

        //defaultScaleBoxText();
    }

    public void drawSelectBoxText(IDEFEntity.Box box){
        if(box.name==null)
            return;
        if(box.name.length()==0)
            return;
        PointF canPoint1=pointToCanvas(box.coord.p1);
        PointF canPoint2=pointToCanvas(box.coord.p2);
        int leftSide=(int)(canPoint1.x);
        int topSide=(int)(canPoint2.y);
        int rightSide=(int)(canPoint2.x);
        int bottomSide=(int)(canPoint1.y);

        Rect bounds = new Rect();
        String newString=box.name.trim().replace("<CR>", "\n");

        /*
        String resultString="";
        String currentString="";
        int iSave=0;
        int jSave=0;
        int i=0;
        int j=0;

        while(i<newString.length()){
            char ch=newString.charAt(i);
            if(ch==' '){
                iSave=i;
                jSave=j;
            }
            currentString+=ch;
            textPaint.getTextBounds(currentString, 0, currentString.length(), bounds);
            if(bounds.width()>(rightSide-leftSide)){
                if(iSave!=0){
                    currentString=currentString.substring(0,jSave);
                    jSave=0;
                    j=-1;
                    i=iSave;
                    iSave=0;
                }else{
                    currentString=currentString.substring(0,currentString.length()-2);
                    i--;
                    i--;
                }
                currentString+='\n';
                resultString+=currentString;
                currentString="";
            }
            i++;
            j++;
        }

        resultString+=currentString;
        */


        String resultString="";
        String currentString="";
        String[] arrStr=newString.split("\n");

        for(int i=0;i<arrStr.length;i++) {
            currentString = arrStr[i];
            int startPos = 0;
            Integer savePos = null;
            for (int j = startPos; j < currentString.length(); j++) {
                selectTextPaint.getTextBounds(currentString, startPos, j, bounds);
                if (bounds.width() > (rightSide - leftSide)) {
                    if (savePos == null) {
                        savePos = j - 1;
                    }
                    resultString += currentString.substring(startPos, savePos).trim() + "\n";
                    startPos = savePos.intValue();
                    savePos = null;
                    j = startPos;
                }else{
                    if (currentString.charAt(j) == ' ') {
                        savePos = j;
                    }
                }
            }
            resultString += currentString.substring(startPos, currentString.length()).trim() + "\n";
        }
        resultString.trim();

        Point boxCenter=new Point(leftSide+((rightSide-leftSide)/2),topSide+((bottomSide-topSide)/2));

        int textHeight=0;
        ArrayList<String> stringList=new ArrayList<String>();
        for(String str: resultString.split("\n")){
            selectTextPaint.getTextBounds(str, 0, str.length(), bounds);
            textHeight+=bounds.height();
            stringList.add(str);
        }

        int yPosition=boxCenter.y-textHeight/2;

        for(String str : stringList){
            selectTextPaint.getTextBounds(str, 0, str.length(), bounds);
            Point textCenter = new Point(bounds.centerX(),Math.abs(bounds.centerY()));
            Point textPosition= new Point(boxCenter.x-textCenter.x,yPosition);
            yPosition+=bounds.height();

            curCanvas.drawText(str, textPosition.x, textPosition.y, selectTextPaint);
        }

        //defaultScaleBoxText();
    }

    public void drawBoxReference(IDEFEntity.Box box){
        float k=10;
        String text="A"+String.valueOf(box.diagramId);
        TextPaint smallTextPaint=new TextPaint(textPaint);
        smallTextPaint.setTextSize((int)(smallTextPaint.getTextSize()/1.5f));
        Rect bounds=new Rect();
        smallTextPaint.getTextBounds(text,0,text.length(),bounds);
        PointF canPoint1=pointToCanvas(box.coord.p1);
        PointF canPoint2=pointToCanvas(box.coord.p2);
        float x=canPoint2.x-bounds.width()-k;
        float y=canPoint1.y-bounds.height()-k;
        curCanvas.drawText(text,x,y,smallTextPaint);
    }

    /*
    public void scaleBoxText(float size){
       textPaint.setTextSize(size);
    }

    public void defaultScaleBoxText(){
        textPaint.setTextSize(24);
    }
    */


    //Изменяет размар текста, в зависимости от масштаба
    public void scaleTextSize(){
        float size=(float)(dipToPixel(K3)*camera.scale);
        if(size>(dipToPixel(K3)*1.5f)){
            size=dipToPixel(K3)*1.5f;
        }else if(size<K3/1.5f){
            size=dipToPixel(K3)/1.5f;
        }
        textPaint.setTextSize(size);
        selectTextPaint.setTextSize(size);
    }

    public void drawBox(IDEFEntity.Box box){
        PointF canPoint1=pointToCanvas(box.coord.p1);
        PointF canPoint2=pointToCanvas(box.coord.p2);

        float leftSide=canPoint1.x;
        float topSide=canPoint2.y;
        float rightSide=canPoint2.x;
        float bottomSide=canPoint1.y;

        /*
        float leftSide=box.coord.p1.x*docWidth;
        float topSide=box.coord.p2.y*docHeight;
        float rightSide=box.coord.p2.x*docWidth;
        float bottomSide=box.coord.p1.y*docHeight;
        */

        curCanvas.drawRect(new RectF(leftSide,topSide,rightSide,bottomSide),paint);
        drawBoxText(box);
        drawBoxReference(box);
    }

    public void drawSelectedBox(IDEFEntity.Box box){

        PointF canPoint1=pointToCanvas(box.coord.p1);
        PointF canPoint2=pointToCanvas(box.coord.p2);
        float leftSide=canPoint1.x;
        float topSide=canPoint2.y;
        float rightSide=canPoint2.x;
        float bottomSide=canPoint1.y;

        curCanvas.drawRect(new RectF(leftSide,topSide,rightSide,bottomSide),selectPaint);
        drawSelectBoxText(box);
        drawBoxReference(box);
    }


    public void drawArrow(IDEFEntity.Arrow arrow){
        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);

        Path path=new Path();
        PointF[] snake=new PointF[3];
        PointF point;
        ArrayList<PointF> tempPoint=new ArrayList<PointF>();

        tempPoint.add(pointToCanvas(pointList.get(0)));

        int i=0;
        ArrayList<PointF> nodeList=new ArrayList<PointF>();
        PointF lastPoint=new PointF();
        while(pointList.size()!=0){
            snake[i]=pointList.remove(0);
            lastPoint=pointToCanvas(snake[i]);
            i++;
            if(i==3){
                if(snake[0].y==snake[1].y){
                    if(snake[1].x>snake[0].x) {
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x - K1, snake[1].y)));
                    }else {
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x + K1, snake[1].y)));
                    }
                }else if(snake[0].x==snake[1].x){
                    if(snake[1].y>snake[0].y)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y-K1)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y+K1)));
                }

                nodeList.add(pointToCanvas(snake[1]));

                if(snake[1].y==snake[2].y){
                    if(snake[2].x>snake[1].x)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x+K1,snake[1].y)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x-K1,snake[1].y)));
                }else if(snake[1].x==snake[2].x){
                    if(snake[2].y>snake[1].y)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y+K1)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y-K1)));
                }

                snake[0]=snake[1];
                snake[1]=snake[2];
                i=2;
            }
        }
        tempPoint.add(lastPoint);

        PointF startPoint=tempPoint.remove(0);
        path.moveTo(startPoint.x,startPoint.y);
        PointF nextPoint;
        PointF node;
        boolean curve=false;
        while(tempPoint.size()!=0){
            nextPoint=tempPoint.remove(0);
            if(!curve){
                path.lineTo(nextPoint.x,nextPoint.y);
                path.moveTo(nextPoint.x,nextPoint.y);
            }else{
                node=nodeList.remove(0);
                path.quadTo(node.x,node.y,nextPoint.x,nextPoint.y);
                path.moveTo(nextPoint.x,nextPoint.y);
            }
            curve=!curve;
        }

        curCanvas.drawPath(path,paint);

        if((arrow.sink.type!=null) && !(arrow.sink.obj instanceof IDEFEntity.Branch)){
            Path trianglePath=new Path();
            PointF canPoint=pointToCanvas(arrow.sink.point);
            trianglePath.moveTo(canPoint.x,canPoint.y);
            if(arrow.sink.type.equals("I")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y-K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y+K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("C")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2/2,arrow.sink.point.y-K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x+K2/2,arrow.sink.point.y-K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("O")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y-K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y+K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("M")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2/2,arrow.sink.point.y+K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x+K2/2,arrow.sink.point.y+K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }
            curCanvas.drawPath(trianglePath,filledPaint);
        }
    }

    public void drawSelectedArrow(IDEFEntity.Arrow arrow){
        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);

        Path path=new Path();
        PointF[] snake=new PointF[3];
        ArrayList<PointF> tempPoint=new ArrayList<PointF>();
        PointF canPoint;

        tempPoint.add(pointToCanvas(pointList.get(0)));

        int i=0;
        ArrayList<PointF> nodeList=new ArrayList<PointF>();
        PointF lastPoint=new PointF();
        while(pointList.size()!=0){
            snake[i]=pointList.remove(0);
            lastPoint=pointToCanvas(snake[i]);
            i++;
            if(i==3){
                if(snake[0].y==snake[1].y){
                    if(snake[1].x>snake[0].x) {
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x - K1, snake[1].y)));
                    }else {
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x + K1, snake[1].y)));
                    }
                }else if(snake[0].x==snake[1].x){
                    if(snake[1].y>snake[0].y)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y-K1)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y+K1)));
                }

                nodeList.add(pointToCanvas(snake[1]));

                if(snake[1].y==snake[2].y){
                    if(snake[2].x>snake[1].x)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x+K1,snake[1].y)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x-K1,snake[1].y)));
                }else if(snake[1].x==snake[2].x){
                    if(snake[2].y>snake[1].y)
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y+K1)));
                    else
                        tempPoint.add(pointToCanvas(new PointF(snake[1].x,snake[1].y-K1)));
                }

                snake[0]=snake[1];
                snake[1]=snake[2];
                i=2;
            }
        }
        tempPoint.add(lastPoint);

        PointF startPoint=tempPoint.remove(0);
        path.moveTo(startPoint.x,startPoint.y);
        PointF nextPoint;
        PointF node;
        boolean curve=false;
        while(tempPoint.size()!=0){
            nextPoint=tempPoint.remove(0);
            if(!curve){
                path.lineTo(nextPoint.x,nextPoint.y);
                path.moveTo(nextPoint.x,nextPoint.y);
            }else{
                node=nodeList.remove(0);
                path.quadTo(node.x,node.y,nextPoint.x,nextPoint.y);
                path.moveTo(nextPoint.x,nextPoint.y);
            }
            curve=!curve;
        }

        curCanvas.drawPath(path,selectPaint);

        if(arrow.sink.type!=null && !(arrow.sink.obj instanceof IDEFEntity.Branch)){
            Path trianglePath=new Path();
            canPoint=pointToCanvas(arrow.sink.point);
            trianglePath.moveTo(canPoint.x,canPoint.y);
            if(arrow.sink.type.equals("I")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y-K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y+K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("C")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2/2,arrow.sink.point.y-K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x+K2/2,arrow.sink.point.y-K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("O")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y-K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2,arrow.sink.point.y+K2/2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }else if(arrow.sink.type.equals("M")){
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x-K2/2,arrow.sink.point.y+K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
                canPoint=pointToCanvas(new PointF(arrow.sink.point.x+K2/2,arrow.sink.point.y+K2));
                trianglePath.lineTo(canPoint.x,canPoint.y);
            }
            curCanvas.drawPath(trianglePath,filledSelectPaint);
        }
    }

    public void drawBorder(IDEFEntity.Border border){
        float k=0.1f;
        if(border.type.equals("I")){
            curCanvas.drawRect(new RectF(0,0,k*getWidth(),getHeight()),filledSelectPaint);
        }else if(border.type.equals("C")){
            curCanvas.drawRect(new RectF(k*getWidth(),0,(1-k)*getWidth(),k*getHeight()),filledSelectPaint);
        }else if(border.type.equals("O")){
            curCanvas.drawRect(new RectF((1-k)*getWidth(),0,getWidth(),getHeight()),filledSelectPaint);
        }else if(border.type.equals("M")){
            curCanvas.drawRect(new RectF(k*getWidth(),(1-k)*getHeight(),(1-k)*getWidth(),getHeight()),filledSelectPaint);
        }
    }


    public void drawBranch(IDEFEntity.Branch branch){
        PointF prevSinkParentPoint=branch.parent.coord.path.get(branch.parent.coord.path.size()-2);
        for(IDEFEntity.Arrow arrow : branch.childList){
            PointF sourceChildPoint=arrow.coord.path.get(0);
            PointF nextSourceChildPoint=arrow.coord.path.get(1);
            if(prevSinkParentPoint.x!=nextSourceChildPoint.x && prevSinkParentPoint.y!=nextSourceChildPoint.y) {
                PointF tempPoint1 = new PointF();
                PointF clearPoint1 = new PointF();
                PointF clearPoint2 = new PointF();
                if (sourceChildPoint.x == prevSinkParentPoint.x) {
                    if (sourceChildPoint.y > prevSinkParentPoint.y) {
                        tempPoint1 = pointToCanvas(new PointF(sourceChildPoint.x, sourceChildPoint.y-K1));
                    } else if (sourceChildPoint.y < prevSinkParentPoint.y) {
                        tempPoint1 = pointToCanvas(new PointF(sourceChildPoint.x, sourceChildPoint.y+K1));
                    }
                } else if (sourceChildPoint.y == prevSinkParentPoint.y) {
                    if (sourceChildPoint.x > prevSinkParentPoint.x) {
                        tempPoint1 = pointToCanvas(new PointF(sourceChildPoint.x-K1, sourceChildPoint.y));
                    } else if (sourceChildPoint.x < prevSinkParentPoint.x) {
                        tempPoint1 = pointToCanvas(new PointF(sourceChildPoint.x+K1, sourceChildPoint.y));
                    }
                }

                PointF tempPoint2 = new PointF();
                if (sourceChildPoint.y == nextSourceChildPoint.y) {
                    if (nextSourceChildPoint.x < prevSinkParentPoint.x) {
                        tempPoint2 = pointToCanvas(new PointF(sourceChildPoint.x-K1, sourceChildPoint.y));
                        clearPoint1=pointToCanvas(sourceChildPoint);
                        clearPoint1=new PointF(clearPoint1.x-1,clearPoint1.y);
                        clearPoint2=new PointF(tempPoint2.x+1,tempPoint2.y);
                    } else if (nextSourceChildPoint.x > prevSinkParentPoint.x) {
                        tempPoint2 = pointToCanvas(new PointF(sourceChildPoint.x+K1, sourceChildPoint.y));
                        clearPoint1=pointToCanvas(sourceChildPoint);
                        clearPoint1=new PointF(clearPoint1.x+1,clearPoint1.y);
                        clearPoint2=new PointF(tempPoint2.x-1,tempPoint2.y);
                    }
                } else if (sourceChildPoint.x == nextSourceChildPoint.x) {
                    if (nextSourceChildPoint.y < prevSinkParentPoint.y) {
                        tempPoint2 = pointToCanvas(new PointF(sourceChildPoint.x, sourceChildPoint.y-K1));
                        clearPoint1=pointToCanvas(sourceChildPoint);
                        clearPoint1=new PointF(clearPoint1.x,clearPoint1.y-1);
                        clearPoint2=new PointF(tempPoint2.x,tempPoint2.y+1);
                    } else if (nextSourceChildPoint.y > prevSinkParentPoint.y) {
                        tempPoint2 = pointToCanvas(new PointF(sourceChildPoint.x, sourceChildPoint.y+K1));
                        clearPoint1=pointToCanvas(sourceChildPoint);
                        clearPoint1=new PointF(clearPoint1.x,clearPoint1.y+1);
                        clearPoint2=new PointF(tempPoint2.x,tempPoint2.y-1);
                    }
                }

                Path path = new Path();
                path.moveTo(clearPoint1.x, clearPoint1.y);
                path.lineTo(clearPoint2.x, clearPoint2.y);
                curCanvas.drawPath(path, eraserPaint);

                path = new Path();
                path.moveTo(tempPoint1.x, tempPoint1.y);
                PointF nodePoint=pointToCanvas(sourceChildPoint);
                path.quadTo(nodePoint.x, nodePoint.y, tempPoint2.x, tempPoint2.y);
                curCanvas.drawPath(path, paint);
            }
        }
    }

    public void drawJoin(IDEFEntity.Join join){
        PointF nextSourceChildPoint=join.child.coord.path.get(1);
        for(IDEFEntity.Arrow arrow : join.parentList){
            PointF sinkParentPoint=arrow.coord.path.get(arrow.coord.path.size()-1);
            PointF prevSinkParentPoint=arrow.coord.path.get(arrow.coord.path.size()-2);
            if(nextSourceChildPoint.x!=prevSinkParentPoint.x && nextSourceChildPoint.y!=prevSinkParentPoint.y) {
                PointF tempPoint1 = new PointF();
                PointF clearPoint1 = new PointF();
                PointF clearPoint2 = new PointF();
                if (sinkParentPoint.x == nextSourceChildPoint.x) {
                    if (sinkParentPoint.y > nextSourceChildPoint.y) {
                        tempPoint1 = pointToCanvas(new PointF(sinkParentPoint.x, sinkParentPoint.y-K1));
                    } else if (sinkParentPoint.y < nextSourceChildPoint.y) {
                        tempPoint1 = pointToCanvas(new PointF(sinkParentPoint.x, sinkParentPoint.y+K1));
                    }
                } else if (sinkParentPoint.y == nextSourceChildPoint.y) {
                    if (sinkParentPoint.x > nextSourceChildPoint.x) {
                        tempPoint1 = pointToCanvas(new PointF(sinkParentPoint.x-K1, sinkParentPoint.y));
                    } else if (sinkParentPoint.x < nextSourceChildPoint.x) {
                        tempPoint1 = pointToCanvas(new PointF(sinkParentPoint.x+K1, sinkParentPoint.y));
                    }
                }

                PointF tempPoint2 = new PointF();
                if (sinkParentPoint.y == prevSinkParentPoint.y) {
                    if (prevSinkParentPoint.x < nextSourceChildPoint.x) {
                        tempPoint2 = pointToCanvas(new PointF(sinkParentPoint.x-K1,sinkParentPoint.y));
                        clearPoint1=pointToCanvas(sinkParentPoint);
                        clearPoint1=new PointF(clearPoint1.x-1,clearPoint1.y);
                        clearPoint2=new PointF(tempPoint2.x+1,tempPoint2.y);
                    } else if (prevSinkParentPoint.x > nextSourceChildPoint.x) {
                        tempPoint2 = pointToCanvas(new PointF(sinkParentPoint.x+K1,sinkParentPoint.y));
                        clearPoint1=pointToCanvas(sinkParentPoint);
                        clearPoint1=new PointF(clearPoint1.x+1,clearPoint1.y);
                        clearPoint2=new PointF(tempPoint2.x-1,tempPoint2.y);
                    }
                } else if (sinkParentPoint.x == prevSinkParentPoint.x) {
                    if (prevSinkParentPoint.y < nextSourceChildPoint.y) {
                        tempPoint2 = pointToCanvas(new PointF(sinkParentPoint.x,sinkParentPoint.y-K1));
                        clearPoint1=pointToCanvas(sinkParentPoint);
                        clearPoint1=new PointF(clearPoint1.x,clearPoint1.y-1);
                        clearPoint2=new PointF(tempPoint2.x,tempPoint2.y+1);
                    } else if (prevSinkParentPoint.y > nextSourceChildPoint.y) {
                        tempPoint2 = pointToCanvas(new PointF(sinkParentPoint.x,sinkParentPoint.y+K1));
                        clearPoint1=pointToCanvas(sinkParentPoint);
                        clearPoint1=new PointF(clearPoint1.x,clearPoint1.y+1);
                        clearPoint2=new PointF(tempPoint2.x,tempPoint2.y-1);
                    }
                }

                Path path = new Path();
                path.moveTo(clearPoint1.x, clearPoint1.y);
                path.lineTo(clearPoint2.x, clearPoint2.y);
                curCanvas.drawPath(path, eraserPaint);

                path = new Path();
                path.moveTo(tempPoint1.x, tempPoint1.y);
                PointF nodePoint=pointToCanvas(sinkParentPoint);
                path.quadTo(nodePoint.x, nodePoint.y, tempPoint2.x, tempPoint2.y);
                curCanvas.drawPath(path, paint);
            }
        }
    }

    public void drawLabel(IDEFEntity.Arrow arrow){
        if(arrow.label==null)
            return;

        PointF p1=pointToCanvas(arrow.label.getPoint1());
        PointF p2=pointToCanvas(arrow.label.getPoint2());
        PointF lastPoint=arrow.label.getPoint2();
        //int labelWidth=(int)(arrow.label.getWidth()*curCanvas.getWidth());
        /*
        float scale=1;
        int labelWidth=(int)((toCanvasWidth(arrow.label.getWidth()))*scale);
        */
        float scale=1;
        if(camera.scale<1){
            scale=(float)camera.scale;
        }
        int labelWidth=(int)(K4*scale);
        Rect bounds=new Rect();
        String text=arrow.label.text.trim().replace("<CR>"," ");
        int i=0;
        int startInd=0;
        int lastInd=-1;
        int counter=0;
        String currentString="";
        PointF currentPoint=new PointF(p1.x,p1.y);
        while(i<text.length()){
            char ch=text.charAt(i);
            if(ch==' '){
                lastInd=i;
            }

            currentString+=ch;
            textPaint.getTextBounds(currentString,0,currentString.length(),bounds);
            if(bounds.width()>labelWidth){
                if(lastInd!=(-1)){
                    currentString=text.substring(startInd,lastInd);
                    i=lastInd;
                    startInd=i+1;
                    curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,textPaint);
                    currentPoint.y+=bounds.height()+2;
                    currentString="";
                    lastInd=-1;
                }else{
                    currentString=text.substring(startInd,i);
                    startInd=i;
                    i--;
                    curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,textPaint);
                    currentPoint.y+=bounds.height()+2;
                    currentString="";
                }

                counter++;
                if(counter>N){
                    break;
                }
            }

            if(pointToModel(currentPoint)!=null){
                lastPoint=pointToModel(currentPoint);
            }

            i++;
            if(i==text.length()){
                curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,textPaint);
                arrow.label.setContainer(lastPoint,pointToModel(p2));
            }
        }
    }

    public void drawSelectedLabel(IDEFEntity.Arrow arrow){
        if(arrow.label==null)
            return;

        //int labelWidth=(int)(arrow.label.getWidth()*curCanvas.getWidth());
        //int labelWidth=(int)(toCanvasWidth(arrow.label.getWidth()));
        float scale=1;
        if(camera.scale<1){
            scale=(float)camera.scale;
        }
        int labelWidth=(int)(K4*scale);
        PointF p1=pointToCanvas(arrow.label.getPoint1());
        PointF p2=pointToCanvas(arrow.label.getPoint2());
        PointF lastPoint=arrow.label.getPoint2();

        Rect bounds=new Rect();
        String text=arrow.label.text.trim().replace("<CR>"," ");
        int i=0;
        int startInd=0;
        int lastInd=-1;
        int counter=0;
        String currentString="";
        PointF currentPoint=new PointF(p1.x,p1.y);
        while(i<text.length()){
            char ch=text.charAt(i);
            if(ch==' '){
                lastInd=i;
            }

            currentString+=ch;
            selectTextPaint.getTextBounds(currentString,0,currentString.length(),bounds);
            if(bounds.width()>labelWidth){
                if(lastInd!=(-1)){
                    currentString=text.substring(startInd,lastInd);
                    i=lastInd;
                    startInd=i+1;
                    curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,selectTextPaint);
                    currentPoint.y+=bounds.height()+2;
                    currentString="";
                    lastInd=-1;
                }else{
                    currentString=text.substring(startInd,i);
                    startInd=i;
                    i--;
                    curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,selectTextPaint);
                    currentPoint.y+=bounds.height()+2;
                    currentString="";
                }

                counter++;
                if(counter>N){
                    break;
                }
            }

            if(pointToModel(currentPoint)!=null){
                lastPoint=pointToModel(currentPoint);
            }

            i++;
            if(i==text.length()){
                //curCanvas.drawRect(new RectF(currentPoint.x,p2.y,p2.x,currentPoint.y),selectPaint);
                arrow.label.setContainer(lastPoint,pointToModel(p2));
                curCanvas.drawText(currentString,currentPoint.x,currentPoint.y,selectTextPaint);
            }
        }
    }

    public void drawSquiggle(IDEFEntity.Arrow arrow){
        if(arrow==null){
            return;
        }

        PointF labelPoint=pointToCanvas(arrow.squiggle.getLabelPoint());
        PointF arrowPoint=pointToCanvas(arrow.squiggle.getArrowPoint());

        PointF centrPoint=new PointF((arrowPoint.x-labelPoint.x)/2+labelPoint.x,(arrowPoint.y-labelPoint.y)/2+labelPoint.y);
        int length=20;
        PointF A=new PointF(centrPoint.x-arrowPoint.x,centrPoint.y-arrowPoint.y);
        /*
        float norm=0;
        PointF B=new PointF();
        if (A.y!=0) {
            norm = -A.x / A.y;
            B=new PointF(length,norm*length);
        }else{
            norm = -A.y / A.x;
            B=new PointF(norm*length,length);
        }
        */

        PointF norm=new PointF(-A.y,A.x);
        //PointF ort2=new PointF(A.y,-A.x);

        if(norm.x!=0){
            norm.x=norm.x/Math.abs(norm.x);
        }
        if(norm.y!=0){
            norm.y=norm.y/Math.abs(norm.y);
        }

        PointF B=new PointF(norm.x*length,norm.y*length);

        PointF p1=new PointF(centrPoint.x+B.x,centrPoint.y+B.y);
        PointF p2=new PointF(centrPoint.x-B.x,centrPoint.y-B.y);
        Path path=new Path();
        path.moveTo(arrowPoint.x,arrowPoint.y);
        path.lineTo(p1.x,p1.y);
        path.moveTo(p1.x,p1.y);
        path.lineTo(p2.x,p2.y);
        path.moveTo(p2.x,p2.y);
        path.lineTo(labelPoint.x,labelPoint.y);
        curCanvas.drawPath(path,paint);
    }


    public void drawConnector(IDEFEntity.Box box,IDEFEntity.Box.BoxConnector boxConnector){
        PointF p1=pointToCanvas(box.coord.p1);
        PointF p2=pointToCanvas(box.coord.p2);
        PointF np1=pointToCanvas(new PointF(box.coord.p1.x,box.coord.p2.y));
        PointF np2=pointToCanvas(new PointF(box.coord.p2.x,box.coord.p1.y));
        PointF centr=new PointF(p1.x+(p2.x-p1.x)/2,p1.y+(p2.y-p1.y)/2);

        Path trianglePath=new Path();
        trianglePath.moveTo(centr.x,centr.y);
        if(boxConnector.type.equals("I")){
            trianglePath.lineTo(p1.x,p1.y);
            trianglePath.lineTo(np1.x,np1.y);
        }else if(boxConnector.type.equals("C")){
            trianglePath.lineTo(np1.x,np1.y);
            trianglePath.lineTo(p2.x,p2.y);
        }else if(boxConnector.type.equals("O")){
            trianglePath.lineTo(p2.x,p2.y);
            trianglePath.lineTo(np2.x,np2.y);
        }else if(boxConnector.type.equals("M")){
            trianglePath.lineTo(p1.x,p1.y);
            trianglePath.lineTo(np2.x,np2.y);
        }
        curCanvas.drawPath(trianglePath,filledSelectPaint);
    }

    public void drawConnector(IDEFEntity.Arrow.ArrowSource arrowSource){
        PointF con=pointToCanvas(arrowSource.getConnectorPoint());
        curCanvas.drawCircle(con.x, con.y, getRadius(), filledSelectPaint);
    }

    public void drawNewArrow(IDEFEntity.NewArrow newArrow){

        if(newArrow.getSource()!=null) {
            if(newArrow.getSource().getConnectorPoint()!=null) {
                PointF point=pointToCanvas(newArrow.getSource().getConnectorPoint());
                curCanvas.drawCircle(point.x, point.y, getRadius(), filledSelectPaint);
            }
        }
        if(newArrow.getSink()!=null) {
            if(newArrow.getSink().getConnectorPoint()!=null) {
                PointF point=pointToCanvas(newArrow.getSink().getConnectorPoint());
                curCanvas.drawCircle(point.x, point.y, getRadius(), filledSelectPaint);
            }
        }
    }


    public void drawConnector(IDEFEntity.Arrow.ArrowSink arrowSink){
        PointF con=pointToCanvas(arrowSink.getConnectorPoint());
        curCanvas.drawCircle(con.x,con.y,getRadius(),filledSelectPaint);
    }

    public void drawDiagramName(IDEFEntity.Diagram diagram){
        TextPaint bigTextPaint = new TextPaint(textPaint);
        bigTextPaint.setTextSize(dipToPixel(K3) * 2);
        if(diagram.getParent()==null) {
            curCanvas.drawText("A-" + String.valueOf(diagram.id), 0.1f * getWidth(), 0.9f * getHeight(), bigTextPaint);
        }else{
            curCanvas.drawText("A" + String.valueOf(diagram.id), 0.1f * getWidth(), 0.9f * getHeight(), bigTextPaint);
        }
    }

    public void drawBackground(){
        curCanvas.drawColor(getResources().getColor(R.color.silver));
    }

    public void drawWorkspace(){
        Paint whiteFilled=new Paint();
        whiteFilled.setStyle(Paint.Style.FILL);
        whiteFilled.setAntiAlias(true);
        whiteFilled.setColor(Color.WHITE);

        /*
        PointF p1=pointToCanvas(new PointF(0.03f,0.03f));
        PointF p2=pointToCanvas(new PointF(0.97f,0.97f));
        */
        PointF p1=pointToCanvas(new PointF(0,0));
        PointF p2=pointToCanvas(new PointF(1,1));
        curCanvas.drawRect(new RectF(p1.x,p1.y,p2.x,p2.y),whiteFilled);
    }

    //Перерисовывание диаграммы
    public void drawDiagram(IDEFEntity.Diagram diagram){
        curDiagram=diagram;
        invalidate();
    }

    //Перед вызовом обязательно должен быть проинициализирован
    //холст curCanvas
    public void drawDiagram(){
        if(curCanvas==null){
            return;
        }

        clearCanvas();

        scaleTextSize();
        drawBackground();
        drawWorkspace();
        drawDiagramName(curDiagram);

        if(curDiagram.getSelectedObject() instanceof IDEFEntity.Border){
            if(curDiagram.getSelectedObject()==curDiagram.leftBorder){
                drawBorder(curDiagram.leftBorder);
            }else if(curDiagram.getSelectedObject()==curDiagram.topBorder){
                drawBorder(curDiagram.topBorder);
            }else if(curDiagram.getSelectedObject()==curDiagram.rightBorder){
                drawBorder(curDiagram.rightBorder);
            }else if(curDiagram.getSelectedObject()==curDiagram.bottomBorder){
                drawBorder(curDiagram.bottomBorder);
            }
        }

        for(IDEFEntity.Box box : curDiagram.boxSet){
            if(curDiagram.getSelectedObject()==box){
                drawSelectedBox(box);
            }else{
                drawBox(box);
            }

            if(curDiagram.getSelectedObject()==box.left){
                drawConnector(box,box.left);
            }else if(curDiagram.getSelectedObject()==box.top){
                drawConnector(box,box.top);
            }else if(curDiagram.getSelectedObject()==box.right){
                drawConnector(box,box.right);
            }else if(curDiagram.getSelectedObject()==box.bottom){
                drawConnector(box,box.bottom);
            }

        }

        for(IDEFEntity.Arrow arrow : curDiagram.arrowSet){

            if(curDiagram.getSelectedObject()==arrow){
                drawSelectedArrow(arrow);
            }else{
                drawArrow(arrow);
            }

            if(arrow.label!=null){
                if(curDiagram.getSelectedObject()==arrow.label){
                    drawSelectedLabel(arrow);
                }else{
                    drawLabel(arrow);
                }
            }

            if(arrow.squiggle!=null){
                drawSquiggle(arrow);
            }

            if(curDiagram.getSelectedObject()==arrow.source){
                drawConnector(arrow.source);
            }else if(curDiagram.getSelectedObject()==arrow.sink){
                drawConnector(arrow.sink);
            }
        }

        for(IDEFEntity.Branch branch : curDiagram.branchSet){
            drawBranch(branch);
        }

        for(IDEFEntity.Join join : curDiagram.joinSet) {
            drawJoin(join);
        }

        drawNewArrow(curDiagram.newArrow);
        //invalidate();
    }


    public void canvasBorder(){
        curCanvas.drawRect(1, 1, docWidth-1, docHeight-1, paint);
    }

    public boolean onTouchEvent(MotionEvent event){
        Point point1 = new Point((int)event.getX(0), (int)event.getY(0));

        Log.w("Mode",FragmentToolsMenu.getCurrentMode());
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                touch.fireDownTouchEvent(point1);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Point point2 = new Point((int)event.getX(1), (int)event.getY(1));
                touch.fireDownMultiTouchEvent(point1,point2);
                break;
            case MotionEvent.ACTION_MOVE:
                if(event.getPointerCount()>=2){
                    point2 = new Point((int)event.getX(1), (int)event.getY(1));
                    touch.fireMoveMultiTouchEvent(point1,point2);
                }else {
                    touch.fireMoveTouchEvent(point1);
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                touch.fireCancelMultiTouchEvent();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touch.fireCancelTouchEvent();
                break;
        }

        return true;
    }


    class CanvasTouch{
        //Текущее нажатие
        Point touchPoint;
        //Прошлое нажатие
        Point lastTouchPoint;
        Point multiTouchPoint;
        Point lastMultiTouchPoint;
        Set<DocumentActivity.CanvasTouchListener> listeners;

        CanvasTouch(){
            listeners=new HashSet<DocumentActivity.CanvasTouchListener>();
        }

        public void setLastTouchPoint(){
            lastTouchPoint=touchPoint;
        }

        public void setLastTouchPoint(Point point){
            lastTouchPoint=point;
        }

        public void setTouchPoint(Point point){
            touchPoint=point;
        }

        public void setLastMultiTouchPoint(Point point){
            lastMultiTouchPoint=point;
        }

        public void setLastMultiTouchPoint(){
            lastMultiTouchPoint=multiTouchPoint;
        }

        public void setMultiTouchPoint(Point point){
            multiTouchPoint=point;
        }

        public Point getTouchPoint(){
            return touchPoint;
        }

        public Point getLastTouchPoint(){
            return lastTouchPoint;
        }

        public Point getMultiTouchPoint(){
            return multiTouchPoint;
        }

        public Point getLastMultiTouchPoint(){
            return lastMultiTouchPoint;
        }

        public void fireDownTouchEvent(Point point){
            touch.setTouchPoint(point);

            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionDown(event);
        }

        public void fireDownMultiTouchEvent(Point point1,Point point2){
            touch.setTouchPoint(point1);
            touch.setMultiTouchPoint(point2);

            /*
            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionDown(event);
            */
        }

        public void fireMoveTouchEvent(Point point){
            touch.setLastTouchPoint();
            touch.setTouchPoint(point);

            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionMove(event);
        }

        public void fireMoveMultiTouchEvent(Point point1,Point point2){
            touch.setLastTouchPoint();
            touch.setLastMultiTouchPoint();
            touch.setTouchPoint(point1);
            touch.setMultiTouchPoint(point2);

            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionZoom(event);
        }

        public void fireCancelTouchEvent(){
            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionCancel(event);

            touch.setLastTouchPoint(null);
            touch.setTouchPoint(null);
        }

        public void fireCancelMultiTouchEvent(){
            /*
            CanvasTouchEvent event=new CanvasTouchEvent(this);
            for(DocumentActivity.CanvasTouchListener listener : listeners)
                listener.actionCancel(event);
            */

            touch.setLastMultiTouchPoint(null);
            touch.setMultiTouchPoint(null);
        }

        public void addCanvasTouchListener(DocumentActivity.CanvasTouchListener listener){
            listeners.add(listener);
        }

        public void removeCanvasTouchListener(DocumentActivity.CanvasTouchListener listener){
            listeners.remove(listener);
        }
    }

    class CanvasTouchEvent{
        CanvasTouch source;

        CanvasTouchEvent(CanvasTouch source){
            this.source=source;
        }
    }

    public class Camera {
        //В пределах от 0 до 1
        PointF cameraPoint;
        double scale;
        static final double SCALE_MIN=0.5;
        static final double SCALE_MAX=2;

        Camera(){
            cameraPoint=new PointF(0,0);
            scale=1;
        }

        public double getScale(){
            return scale;
        }

        public void setScale(double scale){
            if(scale>SCALE_MAX){
                this.scale=SCALE_MAX;
            }else if(scale<SCALE_MIN){
                this.scale=SCALE_MIN;
            }else{
                this.scale=scale;
            }
        }

        public PointF getCameraPoint(){
            return cameraPoint;
        }

        //Получаем противоположную точку камеры
        public PointF getCameraEndPoint(){
            return new PointF((float)(getCameraPoint().x+getWidth()),(float)(getCameraPoint().y+getHeight()));
        }

        //Устанавливает точку камеры
        public void setCameraPoint(PointF cameraPoint){
            this.cameraPoint=cameraPoint;
            cameraLimit();
        }

        public void cameraLimit(){
            cameraLeftLimit();
            cameraTopLimit();
            cameraRightLimit();
            cameraBottomLimit();
        }

        public void cameraLeftLimit(){
            if(0>(cameraPoint.x)){
                cameraPoint.x=0;
            }
        }

        public void cameraTopLimit(){
            if(0>(cameraPoint.y)){
                cameraPoint.y=0;
            }
        }

        public void cameraRightLimit(){
            if(getCameraEndPoint().x>1){
                cameraPoint.x-=getCameraEndPoint().x-1;
            }
        }

        public void cameraBottomLimit(){
            if(getCameraEndPoint().y>1){
                cameraPoint.y-=getCameraEndPoint().y-1;
            }
        }

        //Ширина камеры
        public double getWidth(){
            return 1/scale;
        }

        //Высота камеры
        public double getHeight(){
            return 1/scale;
        }

        //Устанавливаем центр камеры,
        //смещаем cameraPoint
        public void setCameraCenter(PointF point){
            float width=((float)getWidth()/2);
            float height=((float)getHeight()/2);
            setCameraPoint(new PointF(point.x-width,point.y-height));
        }

        //Получаем центр камеры
        public PointF getCameraCenter(){
            return new PointF((float)(cameraPoint.x+getWidth()/2),(float)(cameraPoint.y+getHeight()/2));
        }

        public boolean isPointInCamera(PointF p){
            if(getCameraPoint().x<p.x && p.x<getCameraEndPoint().x){
                if(getCameraPoint().y<p.y && p.y<getCameraEndPoint().y){
                    return true;
                }
            }
            return false;
        }


        //Масштабирование предполагаемое для multi touch события
        void zoom(double k){
            PointF oldCenter=getCameraCenter();
            setScale(this.scale*k);
            if (Math.abs(1 - getScale()) < 0.1) {
                setScale(1);
            }

            if(getScale()<=1){
                setCameraPoint(new PointF(0,0));
            }else{
                setCameraCenter(oldCenter);
            }

        }


        public void moveCamera(PointF from,PointF to){
            if(scale>1) {
                if(from!=null && to!=null) {
                    PointF vec = new PointF(to.x - from.x, to.y - from.y);
                    setCameraPoint(new PointF(getCameraPoint().x + vec.x, getCameraPoint().y + vec.y));
                }
            }
        }


    }


    public static enum ScalingLogic {
        CROP, FIT
    }

    public static Rect calculateSrcRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.CROP) {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect) {
                final int srcRectWidth = (int)(srcHeight * dstAspect);
                final int srcRectLeft = (srcWidth - srcRectWidth) / 2;
                return new Rect(srcRectLeft, 0, srcRectLeft + srcRectWidth, srcHeight);
            } else {
                final int srcRectHeight = (int)(srcWidth / dstAspect);
                final int scrRectTop = (int)(srcHeight - srcRectHeight) / 2;
                return new Rect(0, scrRectTop, srcWidth, scrRectTop + srcRectHeight);
            }
        } else {
            return new Rect(0, 0, srcWidth, srcHeight);
        }
    }

    public static Rect calculateDstRect(int srcWidth, int srcHeight, int dstWidth, int dstHeight,
                                        ScalingLogic scalingLogic) {
        if (scalingLogic == ScalingLogic.FIT) {
            final float srcAspect = (float)srcWidth / (float)srcHeight;
            final float dstAspect = (float)dstWidth / (float)dstHeight;

            if (srcAspect > dstAspect) {
                return new Rect(0, 0, dstWidth, (int)(dstWidth / srcAspect));
            } else {
                return new Rect(0, 0, (int)(dstHeight * srcAspect), dstHeight);
            }
        } else {
            return new Rect(0, 0, dstWidth, dstHeight);
        }
    }

}