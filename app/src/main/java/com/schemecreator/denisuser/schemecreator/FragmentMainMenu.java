package com.schemecreator.denisuser.schemecreator;

import android.annotation.TargetApi;
import android.app.Activity;
import android.support.v4.app.Fragment;
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
public class FragmentMainMenu extends Fragment {

    public View onCreateView(LayoutInflater inflater,ViewGroup container,
                             Bundle savedInstanceState){
        View fragmentView = inflater.inflate(R.layout.fragment_main_menu,null);

        ImageButton addButton = (ImageButton)fragmentView.findViewById(R.id.menu_add);
        addButton.setOnClickListener(menuClickListener);

        ImageButton saveButton = (ImageButton)fragmentView.findViewById(R.id.menu_save);
        saveButton.setOnClickListener(menuClickListener);

        ImageButton openButton = (ImageButton)fragmentView.findViewById(R.id.menu_open);
        openButton.setOnClickListener(menuClickListener);

        ImageButton portButton = (ImageButton)fragmentView.findViewById(R.id.menu_port);
        portButton.setOnClickListener(menuClickListener);

        ImageButton settingsButton = (ImageButton)fragmentView.findViewById(R.id.menu_settings);
        settingsButton.setOnClickListener(menuClickListener);

        ImageButton levelDownButton = (ImageButton)fragmentView.findViewById(R.id.level_down);
        levelDownButton.setOnClickListener(menuClickListener);

        ImageButton levelUpButton = (ImageButton)fragmentView.findViewById(R.id.level_up);
        levelUpButton.setOnClickListener(menuClickListener);

        return fragmentView;

    }

    FragmentMenuListener mainMenuListener;

    public interface FragmentMenuListener {
        public void onMenuNew();
        public void onMenuSave();
        public void onMenuOpen();
        public void onMenuPort();
        public void onMenuLevelDown();
        public void onMenuLevelUp();
        public void onMenuSettings();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mainMenuListener = (FragmentMenuListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FragmentMenuListener");
        }

    }

    private View.OnClickListener menuClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            if(v.getId()==R.id.menu_add){
                mainMenuListener.onMenuNew();
            }else if(v.getId()==R.id.menu_save){
                mainMenuListener.onMenuSave();
            }else if(v.getId()==R.id.menu_open){
                mainMenuListener.onMenuOpen();
            }else if(v.getId()==R.id.menu_port) {
                mainMenuListener.onMenuPort();
            }else if(v.getId()==R.id.menu_settings){
                mainMenuListener.onMenuSettings();
            }else if(v.getId()==R.id.level_down){
                mainMenuListener.onMenuLevelDown();
            }else if(v.getId()==R.id.level_up){
                mainMenuListener.onMenuLevelUp();
            }
        }
    };


}
