package com.schemecreator.denisuser.schemecreator;

import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Denisuser on 17.12.2014.
 */
public class ToolsActionCalc {

    //Радиус окружности касания
    static final float RADIUS=0.02f;

    public static boolean isPointInBox(PointF touchPoint,IDEFEntity.Box box){
        if((box.coord.p1.x) <= touchPoint.x && touchPoint.x <= (box.coord.p2.x)){
            if((box.coord.p2.y) <= touchPoint.y && touchPoint.y <= (box.coord.p1.y))
                return true;
        }
        return false;
    }

    public static boolean isPointInLabel(PointF touchPoint,IDEFEntity.Arrow.ArrowLabel label){
        PointF p1=new PointF();
        PointF p2=new PointF();
        if(label.container==null) {
            p1 = new PointF(label.getPoint1().x, label.getPoint1().y);
            p2 = new PointF(label.getPoint2().x, label.getPoint2().y);
        }else{
            RectF container=label.getContainer();
            p1 = new PointF(container.left, container.bottom);
            p2 = new PointF(container.right, container.top);
        }
        if (p1.x < touchPoint.x && touchPoint.x < p2.x) {
            if (p2.y < touchPoint.y && touchPoint.y < p1.y) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPointBesideConnector(PointF touchPoint,IDEFEntity.Arrow.ArrowSource arrowSource){
        PointF center=touchPoint;
        float r=2*RADIUS;
        PointF con=arrowSource.getConnectorPoint();

        if(((con.x - center.x) * (con.x - center.x) + (con.y - center.y) * (con.y - center.y)) <= r * r){
            return true;
        }
        return false;
    }


    public static boolean isPointBesideConnector(PointF touchPoint,IDEFEntity.Arrow.ArrowSink arrowSink){
        PointF center=touchPoint;
        float r=2*RADIUS;

        PointF con=arrowSink.getConnectorPoint();

        if(((con.x - center.x) * (con.x - center.x) + (con.y - center.y) * (con.y - center.y)) <= r * r){
            return true;
        }
        return false;
    }

    public static boolean isPointBesideArrow(PointF touchPoint,IDEFEntity.Arrow arrow){
        PointF center=touchPoint;
        float r=2*RADIUS;

        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);
        while(pointList.size()!=0){
            PointF p1=pointList.remove(0);
            for(int i=0;i<pointList.size();i++) {
                PointF p2 = pointList.get(i);

                if (p1.x == p2.x || p1.y == p2.y){

                    double x1 = p1.x;
                    double y1 = p1.y;
                    double x2 = p2.x;
                    double y2 = p2.y;
                    double xC = center.x;
                    double yC = center.y;
                    double rr = r * r;

                    if ((x1 - xC) * (x1 - xC) + (y1 - yC) * (y1 - yC) <= rr) return true;
                    if ((x2 - xC) * (x2 - xC) + (y2 - yC) * (y2 - yC) <= rr) return true;

                    if (x1 == x2) {
                        if ((y1 < yC && y2 > yC || y1 > yC && y2 < yC) && Math.abs(x1 - xC) <= r)
                            return true;
                    }

                    if (y1 == y2) {
                        if ((x1 < xC && x2 > xC || x1 > xC && x2 < xC) && Math.abs(y1 - yC) <= r)
                            return true;
                    }

                    double a = (y1 - y2) / (x1 - x2);
                    double b = y1 - a * x1;
                    double xp = (yC - b + xC / a) / (a + 1 / a);
                    double yp = a * xp + b;

                    if (x1 < xp && x2 > xp || x2 < xp && x1 > xp)
                        if ((xp - xC) * (xp - xC) + (yp - yC) * (yp - yC) <= rr)
                            return true;
                }
            }
        }
        return false;
    }

    //Возвращает ближайшую точку на векторе (curPoint1,curPoint2) от точки touch
    public static PointF getClosestPoint(PointF touch,PointF curPoint1,PointF curPoint2){
        PointF A=new PointF(curPoint2.x-curPoint1.x,curPoint2.y-curPoint1.y);
        PointF B=new PointF(touch.x-curPoint1.x,touch.y-curPoint1.y);
        float scal=(A.x*B.x+A.y*B.y)/(A.x*A.x+A.y*A.y);
        PointF proj=new PointF(A.x*scal,A.y*scal);
        PointF closest=new PointF(curPoint1.x+proj.x,curPoint1.y+proj.y);
        closest=pointToVectorLimit(closest,curPoint1,curPoint2);
        return closest;
    }

    public static double getDistance(PointF p1,PointF p2){
        double distance=Math.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y));
        return distance;
    }

    public static double getDistance(Point p1,Point p2){
        double distance=Math.sqrt((p2.x-p1.x)*(p2.x-p1.x)+(p2.y-p1.y)*(p2.y-p1.y));
        return distance;
    }

    public static double vectorLength(PointF vec){
        double distance=Math.sqrt(vec.x*vec.x+vec.y*vec.y);
        return distance;
    }

    //Точка point переносится на вектор (curPoint1,curPoint2),
    // при условии что точка лежит на прямой вектора и не лежит на отрезке вектора
    public static PointF pointToVectorLimit(PointF point,PointF curPoint1,PointF curPoint2){
        PointF A=new PointF(curPoint2.x-curPoint1.x,curPoint2.y-curPoint1.y);
        PointF B=new PointF(curPoint2.x-point.x,curPoint2.y-point.y);
        if(vectorLength(B)>vectorLength(A)){
            double d1=getDistance(curPoint1,point);
            double d2=getDistance(curPoint2,point);
            if(d1<d2){
                return curPoint1;
            }else{
                return curPoint2;
            }
        }else{
            B=new PointF(curPoint1.x-point.x,curPoint1.y-point.y);
            if(vectorLength(B)>vectorLength(A)){
                double d1=getDistance(curPoint1,point);
                double d2=getDistance(curPoint2,point);
                if(d1<d2){
                    return curPoint1;
                }else{
                    return curPoint2;
                }
            }
        }
        return point;
    }

    //Проецирует точку на на ближайший отрезок
    public static PointF getPointBesideArrow(PointF touchPoint,IDEFEntity.Arrow arrow){
        float r=2*RADIUS;

        PointF res=new PointF();
        float minDis=-1;
        float minR=RADIUS;

        PointF closest;
        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);
        int j=0;
        for(int i=0;i<(pointList.size()-1);i++){
            j=i+1;
            PointF curPoint1=pointList.get(i);
            PointF curPoint2=pointList.get(j);

            //Ближайшая точка Arrow вошедшая в круг выбора
            if(((curPoint1.x - touchPoint.x) * (curPoint1.x - touchPoint.x) + (curPoint1.y - touchPoint.y) * (curPoint1.y - touchPoint.y)) <= minR * minR) {
                return new PointF(curPoint1.x,curPoint1.y);
            }

            //Ближайшая точка Arrow вошедшая в круг выбора
            if(((curPoint2.x - touchPoint.x) * (curPoint2.x - touchPoint.x) + (curPoint2.y - touchPoint.y) * (curPoint2.y - touchPoint.y)) <= minR * minR) {
                return new PointF(curPoint2.x,curPoint2.y);
            }

            closest=getClosestPoint(touchPoint,curPoint1,curPoint2);

            float dis=(float)Math.sqrt((closest.x-touchPoint.x)*(closest.x-touchPoint.x)+(closest.y-touchPoint.y)*(closest.y-touchPoint.y));
            if(minDis>dis || minDis==(-1)){
                minDis=dis;
                res=closest;
            }
        }
        return res;
    }

    public static double boxTouchDistance(PointF touchPoint,IDEFEntity.Box box){
        float xc=(box.coord.p1.x+box.coord.p2.x)/2;
        float yc=(box.coord.p1.y+box.coord.p2.y)/2;

        double distance=Math.sqrt(Math.pow(xc-touchPoint.x,2)+Math.pow(yc-touchPoint.y,2));

        return distance;
    }

    public static double labelTouchDistance(PointF touchPoint,IDEFEntity.Arrow.ArrowLabel label){
        PointF center=label.getCenterPoint();
        double distance=Math.sqrt(Math.pow(center.x-touchPoint.x,2)+Math.pow(center.y-touchPoint.y,2));
        return distance;
    }

    public static double connectorTouchDistance(PointF touchPoint,IDEFEntity.Arrow.ArrowSource arrowSource){
        PointF con=arrowSource.getConnectorPoint();
        double distance=Math.sqrt(Math.pow(con.x-touchPoint.x,2)+Math.pow(con.y-touchPoint.y,2));
        return distance;
    }

    public static double connectorTouchDistance(PointF touchPoint,IDEFEntity.Arrow.ArrowSink arrowSink){
        PointF con=arrowSink.getConnectorPoint();
        double distance=Math.sqrt(Math.pow(con.x-touchPoint.x,2)+Math.pow(con.y-touchPoint.y,2));
        return distance;
    }

    public static PointF vectorsIntersectionPoint(PointF p1,PointF p2,PointF np1,PointF np2){
        PointF dir1 =new PointF(p2.x - p1.x,p2.y - p1.y);
        PointF dir2 =new PointF(np2.x - np1.x,np2.y - np1.y);

        float a1 = -dir1.y;
        float b1 = +dir1.x;
        float d1 = -(a1*p1.x + b1*p1.y);

        float a2 = -dir2.y;
        float b2 = +dir2.x;
        float d2 = -(a2*np1.x + b2*np1.y);

        float seg1_line2_start = a2*p1.x + b2*p1.y + d2;
        float seg1_line2_end = a2*p2.x + b2*p2.y + d2;

        float seg2_line1_start = a1*np1.x + b1*np1.y + d1;
        float seg2_line1_end = a1*np2.x + b1*np2.y + d1;

        if (seg1_line2_start * seg1_line2_end >= 0 || seg2_line1_start * seg2_line1_end >= 0)
            return null;

        float u = seg1_line2_start / (seg1_line2_start - seg1_line2_end);
        PointF intersection = new PointF(p1.x + u*dir1.x,p1.y + u*dir1.y);
        return intersection;
    }

    public static double arrowTouchDistance(PointF touchPoint,IDEFEntity.Arrow arrow){
        double distance=-1;
        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);
        while(pointList.size()!=0){
            PointF p1=pointList.remove(0);
            for(int i=0;i<pointList.size();i++){
                PointF p2=pointList.get(i);
                double R=-1;

                if(p1.x==p2.x || p1.y==p2.y){

                    PointF A=p1;
                    PointF B=p2;
                    PointF C=touchPoint;

                    double a=(C.x-A.x)*(C.x-A.x)+(C.y-A.y)*(C.y-A.y);
                    double b=(C.x-B.x)*(C.x-B.x)+(C.y-B.y)*(C.y-B.y);
                    double c=(A.x-B.x)*(A.x-B.x)+(A.y-B.y)*(A.y-B.y);

                    if(a>=(b+c)) {
                        R = Math.sqrt(b);
                    }else if(b>=(a+c)) {
                        R = Math.sqrt(a);
                    }else {
                        double a1 = A.x - C.x;
                        double a2 = A.y - C.y;
                        double b1 = B.x - C.x;
                        double b2 = B.y - C.y;

                        R = Math.sqrt(((a1 * b2) * (a1 * b2) + (-a2 * b1) * (-a2 * b1)) / c);
                    }
                }

                if(R!=-1){
                    if(distance!=-1){
                        if(distance>R){
                            distance=R;
                        }
                    }else{
                        distance=R;
                    }
                }

            }
        }

        return distance;
    }

    public static PointF arrowDirectionVec(PointF touchPoint,IDEFEntity.Arrow arrow){
        float x=RADIUS;
        float y=RADIUS;
        PointF py1=new PointF(touchPoint.x,touchPoint.y+y);
        PointF py2=new PointF(touchPoint.x,touchPoint.y-y);
        PointF px1=new PointF(touchPoint.x+x,touchPoint.y);
        PointF px2=new PointF(touchPoint.x-x,touchPoint.y);


        ArrayList<PointF> pointList=new ArrayList<PointF>();
        pointList.addAll(arrow.coord.path);
        int j=0;
        PointF nearestPoint=null;
        PointF resultVec=null;
        for(int i=0;i<pointList.size()-1;i++){
            j=i+1;
            PointF p1=pointList.get(i);
            PointF p2=pointList.get(j);

            nearestPoint=vectorsIntersectionPoint(p1,p2,px1,px2);
            if(nearestPoint!=null){
                resultVec=new PointF(touchPoint.x-nearestPoint.x,touchPoint.y-nearestPoint.y);
                break;
            }else{
                nearestPoint=vectorsIntersectionPoint(p1, p2, py1, py2);
                if(nearestPoint!=null){
                    resultVec=new PointF(touchPoint.x-nearestPoint.x,touchPoint.y-nearestPoint.y);
                    break;
                }
            }
        }

        return resultVec;
    }


    public static String getBoxConnectorType(PointF touchPoint,IDEFEntity.Box box){
        PointF p1=box.coord.p1;
        PointF p2=box.coord.p2;
        PointF np1=new PointF(p1.x,p2.y);
        PointF np2=new PointF(p2.x,p1.y);
        PointF centr=new PointF(p1.x+(p2.x-p1.x)/2,p1.y+(p2.y-p1.y)/2);

        PointF a=p1;
        PointF b=np1;
        PointF c=centr;
        if(((touchPoint.x - a.x) * (a.y - b.y) - (touchPoint.y - a.y) * (a.x - b.x) >= 0) &&
           ((touchPoint.x-b.x)*(b.y-c.y)-(touchPoint.y-b.y)*(b.x-c.x)>=0) &&
           ((touchPoint.x-c.x)*(c.y-a.y)-(touchPoint.y-c.y)*(c.x-a.x)>=0)){
            Log.w("Type","I");
            return "I";
        }

        a=np1;
        b=p2;
        c=centr;
        if(((touchPoint.x - a.x) * (a.y - b.y) - (touchPoint.y - a.y) * (a.x - b.x) >= 0) &&
           ((touchPoint.x-b.x)*(b.y-c.y)-(touchPoint.y-b.y)*(b.x-c.x)>=0) &&
           ((touchPoint.x-c.x)*(c.y-a.y)-(touchPoint.y-c.y)*(c.x-a.x)>=0)){
            Log.w("Type","C");
            return "C";
        }
        a=p2;
        b=np2;
        c=centr;
        if(((touchPoint.x - a.x) * (a.y - b.y) - (touchPoint.y - a.y) * (a.x - b.x) >= 0) &&
            ((touchPoint.x-b.x)*(b.y-c.y)-(touchPoint.y-b.y)*(b.x-c.x)>=0) &&
            ((touchPoint.x-c.x)*(c.y-a.y)-(touchPoint.y-c.y)*(c.x-a.x)>=0)){
            Log.w("Type","O");
            return "O";
        }
        a=np2;
        b=p1;
        c=centr;
        if(((touchPoint.x - a.x) * (a.y - b.y) - (touchPoint.y - a.y) * (a.x - b.x) >= 0) &&
            ((touchPoint.x-b.x)*(b.y-c.y)-(touchPoint.y-b.y)*(b.x-c.x)>=0) &&
            ((touchPoint.x-c.x)*(c.y-a.y)-(touchPoint.y-c.y)*(c.x-a.x)>=0)){
            Log.w("Type","M");
            return "M";
        }
        return null;
    }


    public static String getBorderTypeByPoint(PointF touchPoint){
        PointF point=touchPoint;
        float k=0.03f;

        RectF left=new RectF(0,0,k,1);
        RectF top=new RectF(k,0,1-k,k);
        RectF right=new RectF(1-k,0,1,1);
        RectF bottom=new RectF(k,1-k,1-k,1);

        if(left.left<=point.x && point.x<=left.right){
            if(left.top<=point.y && point.y<=left.bottom){
                return "I";
            }
        }

        if(top.left<=point.x && point.x<=top.right){
            if(top.top<=point.y && point.y<=top.bottom){
                return "C";
            }
        }

        if(right.left<=point.x && point.x<=right.right){
            if(right.top<=point.y && point.y<=right.bottom){
                return "O";
            }
        }

        if(bottom.left<=point.x && point.x<=bottom.right){
            if(bottom.top<=point.y && point.y<=bottom.bottom){
                return "M";
            }
        }

        return null;
    }

    public static PointF getBorderPoint(PointF touchPoint,IDEFEntity.Border border){
        float k=0.019f;

        if(border.type.equals("I")){
            return new PointF(k,touchPoint.y);
        }
        if(border.type.equals("C")){
            return new PointF(touchPoint.x,k);
        }
        if(border.type.equals("O")){
            return new PointF(1-k,touchPoint.y);
        }
        if(border.type.equals("M")){
            return new PointF(touchPoint.x,1-k);
        }
        return null;
    }


    public static PointF getBoxConnectorPoint(PointF touchPoint,IDEFEntity.Box box){
        PointF p1=box.coord.p1;
        PointF p2=box.coord.p2;
        //PointF np1=new PointF(p1.x,p2.y);
        //PointF np2=new PointF(p2.x,p1.y);
        float[][] side=new float[4][4];
        //left
        side[0][0]=Math.abs(touchPoint.x-p1.x);
        side[0][1]=p1.x;
        side[0][2]=touchPoint.y;
        //top
        side[1][0]=Math.abs(touchPoint.y-p2.y);
        side[1][1]=touchPoint.x;
        side[1][2]=p2.y;
        //right
        side[2][0]=Math.abs(touchPoint.x-p2.x);
        side[2][1]=p2.x;
        side[2][2]=touchPoint.y;
        //bottom
        side[3][0]=Math.abs(touchPoint.y-p1.y);
        side[3][1]=touchPoint.x;
        side[3][2]=p1.y;

        float min=side[0][0];
        int ind=0;
        for(int i=0;i<side.length;i++){
            if(min>side[i][0]){
                min=side[i][0];
                ind=i;
            }
        }
        return new PointF(side[ind][1],side[ind][2]);
    }

}
