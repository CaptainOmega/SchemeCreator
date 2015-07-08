package com.schemecreator.denisuser.schemecreator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Denisuser on 03.11.2014.
 */
public class FileAdapter extends BaseAdapter {
    LayoutInflater inflater;
    ArrayList<FileInfo> files;
    Context context;
    //Правила необходимы только для отображения CheckBox'ов
    FileManager.Rule rule;


    //Конструктор с обязательной сортировкой файлов
    FileAdapter(Context context, ArrayList<FileInfo> files, FileManager.Rule rule){
        this.context=context;

        FileManager.checkedList=new ArrayList<String>();
        this.rule=rule;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(files!=null) {
            this.files = files;
            Collections.sort(files, new FileComparator());
        }
    }

    //Конструктор с необязательной сортировкой файлов
    //параметр inverse задаёт порядок списка
    FileAdapter(Context context, ArrayList<FileInfo> files, FileManager.Rule rule,boolean sort,boolean inverse){
        this.context=context;

        FileManager.checkedList=new ArrayList<String>();
        this.rule=rule;

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(files!=null) {
            if(sort) {
                Collections.sort(files, new FileComparator());
            }
            if(inverse){
                ArrayList<FileInfo> list=new ArrayList<FileInfo>();
                for(int i=files.size();i>0;i--){
                    list.add(files.get(i-1));
                }
                files=list;
            }
            this.files = files;
        }
    }

    class FileComparator implements Comparator<FileInfo>{
        public int compare(FileInfo f1, FileInfo f2) {
            if(f1.fileType==0 && f2.fileType!=0)
                return -1;
            if(f1.fileType!=0 && f2.fileType==0)
                return 1;
            return f1.fileName.compareTo(f2.fileName);
        }
    }

    @Override
    public int getCount() {
        if(files==null){
            return 0;
        }
        return files.size();
    }

    @Override
    public Object getItem(int i) {
        return files.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.file_item, viewGroup, false);
        }
        FileInfo file = getFileByPos(i);

        ((ImageView) view.findViewById(R.id.file_image)).setImageDrawable(file.image);
        ((TextView) view.findViewById(R.id.file_title)).setText(file.fileName);

        view.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                TextView tv=(TextView)arg0.findViewById(R.id.file_title);
                //Log.w("App", tv.getText().toString());
                ((FileListInterface)context).getMessage(tv.getText().toString());
            }
        });

        if(rule.getSelect()) {

            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.file_check);
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setTag(file.fileName);

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    String fileStr = (String) checkBox.getTag();
                    //element.setSelected(buttonView.isChecked());
                    boolean exist = false;
                    for (String str : FileManager.checkedList) {
                        if (str.equals(fileStr)) {
                            exist = true;
                            FileManager.checkedList.remove(str);
                            break;
                        }
                    }
                    if (!exist) {
                        FileManager.checkedList.add(fileStr);
                    }
                }

            });
        }

        return view;
    }

    public FileInfo getFileByPos(int i){
        return ((FileInfo)getItem(i));
    }

}
