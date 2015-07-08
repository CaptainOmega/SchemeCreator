package com.schemecreator.denisuser.schemecreator;

import android.annotation.TargetApi;
import android.app.Activity;
import android.support.v4.app.Fragment;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * Created by Denisuser on 09.11.2014.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class FragmentToolsMenu extends Fragment {
    public static String mode="Pointer";
    public static View selectedButton;
    public static Activity listenerActivity;

    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        View fragmentView = inflater.inflate(R.layout.fragment_tools_menu,null);

        ImageButton pointerButton = (ImageButton)fragmentView.findViewById(R.id.tool_pointer);
        pointerButton.setOnClickListener(toolsClickListener);

        ImageButton boxButton = (ImageButton)fragmentView.findViewById(R.id.tool_box);
        boxButton.setOnClickListener(toolsClickListener);

        ImageButton arrowButton = (ImageButton)fragmentView.findViewById(R.id.tool_arrow);
        arrowButton.setOnClickListener(toolsClickListener);

        ImageButton textButton = (ImageButton)fragmentView.findViewById(R.id.tool_text);
        textButton.setOnClickListener(toolsClickListener);

        ImageButton removeButton = (ImageButton)fragmentView.findViewById(R.id.tool_remove);
        removeButton.setOnClickListener(toolsClickListener);

        selectedButton=pointerButton;
        setModePointer();
        setSelectedButton(pointerButton);

        return fragmentView;
    }

    public static void setListenerActivity(Activity listenerActivity){
        FragmentToolsMenu.listenerActivity=listenerActivity;
    }

    public static Activity getListenerActivity(){
        return listenerActivity;
    }

    //Обнуляет ссылку selectedObject на объект
    public void removeSelectedObject(){
        DocumentActivity documentActivity=(DocumentActivity)getListenerActivity();
        documentActivity.entity.getCurrentDiagram().removeSelectedObject();
        documentActivity.draw.drawDiagram(documentActivity.entity.getCurrentDiagram());
    }

    //Обнуляет ссылки newArrow текущей диаграммы на source и sink объекты
    public void removeNewArrowSelect(){
        DocumentActivity documentActivity=(DocumentActivity)getListenerActivity();
        documentActivity.entity.getCurrentDiagram().newArrow.setStage("start");
        documentActivity.entity.getCurrentDiagram().newArrow.clearNewArrow();
        documentActivity.draw.drawDiagram(documentActivity.entity.getCurrentDiagram());
    }

    public void setSelectedButton(View v){
        if(selectedButton!=null) {
            selectedButton.setBackgroundColor(0);
        }
        v.setBackgroundColor(Color.parseColor("#75BCFF"));
        selectedButton=v;
    }



    private View.OnClickListener toolsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            setSelectedButton(v);
            if(v.getId()==R.id.tool_pointer){
                setModePointer();
            }else if(v.getId()==R.id.tool_box){
                setModeBox();
            }else if(v.getId()==R.id.tool_arrow){
                setModeArrow();
            }else if(v.getId()==R.id.tool_text){
                setModeText();
            }else if(v.getId()==R.id.tool_remove){
                setModeRemove();
            }
        }
    };

    public static String getCurrentMode(){
        return mode;
    }

    public void setModePointer(){
        mode="Pointer";
    }

    public void setModeBox(){
        mode="Box";
    }

    public void setModeArrow(){
        mode="Arrow";
        removeSelectedObject();
    }

    public void setModeText(){
        mode="Text";
    }

    public void setModeRemove(){
        mode="Remove";
        removeNewArrowSelect();
    }
}
