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
import android.widget.Toast;

import com.example.user.myproject.PastJoined;
import com.example.user.myproject.R;
import com.example.user.myproject.Upcoming;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DetailedListAdapter extends ArrayAdapter<ApplicationEvent> {

    ViewHolder viewHolder;
    ApplicationEvent evt = new ApplicationEvent();

    public DetailedListAdapter(Context context, int resource, List<ApplicationEvent> objects) {
        super(context, resource, objects);
        //new ImageTask().execute(evt.getTimetableId());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
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

        evt = getItem(position);
        viewHolder.category.setText(evt.getActivityType());
        viewHolder.title.setText(evt.getEventTitle());
        viewHolder.date.setText(viewHolder.date.getText() + Action.displayDate(evt.getStartTime()));
        viewHolder.time.setText(viewHolder.time.getText() + ApplicationEvent.displayTime(evt.getStartTime()) + " - " + ApplicationEvent.displayTime(evt.getEndTime()));
        viewHolder.venue.setText(viewHolder.venue.getText() + evt.getVenueName());
        viewHolder.remainingTime.setText(viewHolder.remainingTime.getText() + calcRemainingTime(evt.getStartTime()));

        ImageTask task = new ImageTask(viewHolder.evtImg);
        task.execute(evt.getTimetableId());

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

    private class ImageTask extends AsyncTask<Integer, Void, Bitmap>
    {
        private final WeakReference<ImageView> imageViewReference;

        public ImageTask(ImageView imageView) {

            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        protected void onPreExecute() {
        }

        protected Bitmap doInBackground(Integer... params) {
            Bitmap myBitmap = null;
            String ngrok = "c3091b38.ngrok.io";
            try {
                URL url = new URL("http://"+ngrok+"/phpMQTT-master/files/get_image.php?timetableId="+params[0]);// + evt.getTimetableId());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);

            } catch (IOException e) {
                //e.printStackTrace();
                //e.getMessage();
            }
            return myBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if (imageViewReference != null && result != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(result);
                } else {
                    imageView.setImageResource(R.mipmap.ic_noimage);
                }
            }
        }
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
