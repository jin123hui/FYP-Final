package com.example.user.myproject.Modal;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.user.myproject.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DetailedListAdapter extends ArrayAdapter<ApplicationEvent> {

    public DetailedListAdapter(Context context, int resource, List<ApplicationEvent> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.detailedlist_entry_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.category = (TextView) convertView.findViewById(R.id.evt_cat);
            viewHolder.title = (TextView) convertView.findViewById(R.id.evt_title);
            viewHolder.date = (TextView) convertView.findViewById(R.id.evt_date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.evt_time);
            viewHolder.venue = (TextView) convertView.findViewById(R.id.evt_venue);
            viewHolder.remainingTime = (TextView) convertView.findViewById(R.id.evt_remaining);
            viewHolder.evtImg = (ImageView) convertView.findViewById(R.id.evt_img);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ApplicationEvent evt = getItem(position);
        viewHolder.category.setText(evt.getActivityType());
        viewHolder.title.setText(evt.getEventTitle());
        viewHolder.date.setText(viewHolder.date.getText() + Action.displayDate(evt.getStartTime()));
        viewHolder.time.setText(viewHolder.time.getText() + ApplicationEvent.displayTime(evt.getStartTime()) + " - " + ApplicationEvent.displayTime(evt.getEndTime()));
        viewHolder.venue.setText(viewHolder.venue.getText() + evt.getVenueName());
        viewHolder.remainingTime.setText(viewHolder.remainingTime.getText() + calcRemainingTime(evt.getStartTime()));
        //viewHolder.evtImg.setImageResource(R.mipmap.ic_test1);
        return convertView;
    }

    public class ViewHolder {
        TextView category;
        TextView title;
        TextView date;
        TextView time;
        TextView venue;
        TextView remainingTime;
        ImageView evtImg;
    }

    public String calcRemainingTime(GregorianCalendar evtDate) {
        Date todayDate = new Date();
        String timeRemaining;

        long diff = evtDate.getTimeInMillis() - todayDate.getTime();
        long diffSeconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60;
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60;
        long diffHours = TimeUnit.MILLISECONDS.toHours(diff) % 24;
        long diffDays = TimeUnit.MILLISECONDS.toDays(diff);

        if(diffDays>365)
            timeRemaining = (int)(diffDays/365) + " YR(s)";
        //else if(diffDays>7)
         //   timeRemaining = (int)(diffDays/7) + " WK";
        else if(diffDays>0)
            timeRemaining = diffDays + " DAY(s)";
        else if(diffHours>0)
            timeRemaining = diffHours + " HR(s)";
        else if(diffMinutes>0)
            timeRemaining = diffMinutes + " MIN(s)";
        else
            timeRemaining = diffSeconds + " SEC(s)";
        return timeRemaining;
    }

}
