package com.example.user.myproject.Modal;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.user.myproject.R;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PastListAdapter extends ArrayAdapter<ApplicationEvent> {

    public PastListAdapter(Context context, int resource, List<ApplicationEvent> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pastlist_entry_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.category = (TextView) convertView.findViewById(R.id.evt_cat);
            viewHolder.title = (TextView) convertView.findViewById(R.id.evt_title);
            viewHolder.date = (TextView) convertView.findViewById(R.id.evt_date);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ApplicationEvent evt = getItem(position);
        viewHolder.category.setText(evt.getActivityType());
        viewHolder.title.setText(evt.getEventTitle());
        viewHolder.date.setText(Action.displayDate(evt.getStartTime()));
        return convertView;
    }

    public class ViewHolder {
        TextView category;
        TextView title;
        TextView date;
    }
}
