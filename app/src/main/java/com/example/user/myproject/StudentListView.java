package com.example.user.myproject;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.user.myproject.Modal.Student;

import java.util.ArrayList;

/**
 * Created by User on 12/10/2017.
 */

public class StudentListView extends ArrayAdapter<Student>{

    private Student[] student;
    private int mResource;
    private Context context;
    private ArrayList<Student> studentList;
    private SparseBooleanArray mSelectedItemsIds;
    public ArrayList<Integer> selectedIds = new ArrayList<Integer>();
    boolean checking;

    public StudentListView(@NonNull Context context, @LayoutRes int resource, ArrayList<Student> student){
        super(context,resource,student);
        mSelectedItemsIds = new SparseBooleanArray();
        mResource = resource;
        this.context = context;
        studentList = student;
        checking = false;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int location = position;
        String studentName = getItem(position).getStudenetName();
        String studentId = getItem(position).getStudentId();
        ViewHolder viewHolder=null;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(mResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder) convertView.getTag();

        }

        viewHolder.txtStudentName.setText(studentName);
        viewHolder.txtStudentId.setText(studentId.toUpperCase());
        //viewHolder.txtStudentName.setBackgroundColor(Color.CYAN);

        viewHolder.linearLayout.setBackgroundColor(selectedIds.contains(position)?Color.LTGRAY:Color.WHITE);
        viewHolder.checkBox.setChecked(selectedIds.contains(position)?true:false);
        if(checking)
            viewHolder.checkBox.setVisibility(View.VISIBLE);
        else
            viewHolder.checkBox.setVisibility(View.GONE);


        return convertView;

    }





    public void remove(Student student){
        studentList.remove(student);
        notifyDataSetChanged();
    }

    public ArrayList<Student> getStudentList(){
        return studentList;

    }

    public void toggleSelection(int position){
        selectView(position,!mSelectedItemsIds.get(position));
    }


    public void removeSelection(){
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();

    }

    public void selectView(int position,boolean value){
        if(value){
            mSelectedItemsIds.put(position,value);
        }else{
            mSelectedItemsIds.delete(position);

        }
        notifyDataSetChanged();

    }





    public int getSelectedCount(){
        return mSelectedItemsIds.size();

    }

    public SparseBooleanArray getSparseBooleanArray(){
        return mSelectedItemsIds;
    }



    class ViewHolder {
        TextView txtStudentId;
        TextView txtStudentName;
        LinearLayout linearLayout;
        CheckBox checkBox;


        ViewHolder(View v){
            txtStudentId = (TextView)(v.findViewById(R.id.txtGroupStudentId));
            txtStudentName = (TextView)(v.findViewById(R.id.txtGroupStudentName));
            linearLayout = (LinearLayout)(v.findViewById(R.id.linearBackground));
            checkBox = (CheckBox)(v.findViewById(R.id.checkBoxSelection));
        }

    }



}
