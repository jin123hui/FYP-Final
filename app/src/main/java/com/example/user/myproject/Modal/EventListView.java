package com.example.user.myproject.Modal;

/**
 * Created by User on 30/9/2017.
 */


import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.user.myproject.R;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by User on 29/7/2017.
 */

public class EventListView extends ArrayAdapter<ApplicationEvent> {

    private ApplicationEvent[] events;
    private int mResource;
    private Context context;


    public EventListView(@NonNull Context context, @LayoutRes int resource, ArrayList<ApplicationEvent> event) {
        super(context, resource,event);
        //this.events = event;
        mResource = resource;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final int location = position;
        String eventName = getItem(position).getEventTitle();
        String startTimeString = Action.getTime(getItem(position).getStartTIme());
        GregorianCalendar startTime = getItem(position).getStartTIme();
        GregorianCalendar endTime = getItem(position).getEndTime();
        String endTimeString = Action.getTime(getItem(position).getEndTime());
        // String eventName = events[position].getEventTitle();
        //String startTimeString = ApplicationEvent.getTime(events[position].getStartTIme());
        //String endTimeString = ApplicationEvent.getTime(events[position].getEndTime());
        ViewHolder viewHolder=null;


        if (convertView == null) {

            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(mResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);




        } else {
            viewHolder = (ViewHolder) convertView.getTag();



        }
        viewHolder.txtEventName.setText(eventName);
        viewHolder.txtEventName.setTypeface(null, Typeface.BOLD);
        viewHolder.txtStartTime.setText("Date: "+ Action.displayDate(startTime));
        viewHolder.txtEndTime.setText("Time:" + ApplicationEvent.displayTime(startTime) + " - "  + ApplicationEvent.displayTime(endTime) );


        return convertView;
        //return super.getView(position, convertView, parent);
    }

    class ViewHolder {
        TextView txtEventName;
        TextView txtStartTime;
        TextView txtEndTime;

        ViewHolder(View v){
            txtEventName = (TextView)(v.findViewById(R.id.txtEventName));
            txtStartTime = (TextView)(v.findViewById(R.id.txtStartTime));
            txtEndTime = (TextView)(v.findViewById(R.id.txtEndTime));

        }

    }

}