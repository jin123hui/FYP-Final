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

public class BasicListAdapter extends ArrayAdapter<ApplicationEvent> {

    public BasicListAdapter(Context context, int resource, List<ApplicationEvent> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.basiclist_entry_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.category = (TextView) convertView.findViewById(R.id.evt_cat);
            viewHolder.title = (TextView) convertView.findViewById(R.id.evt_title);
            viewHolder.date = (TextView) convertView.findViewById(R.id.evt_date);
            viewHolder.time = (TextView) convertView.findViewById(R.id.evt_ttime);
            viewHolder.evtImg = (ImageView) convertView.findViewById(R.id.evt_img);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ApplicationEvent evt = getItem(position);
        viewHolder.category.setText(evt.getActivityType());
        viewHolder.title.setText(evt.getEventTitle());
        viewHolder.date.setText(Action.displayDate(evt.getStartTime()));
        viewHolder.time.setText(ApplicationEvent.displayTime(evt.getStartTime()) + " - "  + ApplicationEvent.displayTime(evt.getEndTime()) );

        ImageTask task = new ImageTask(viewHolder.evtImg);
        task.execute(evt.getTimetableId());

        return convertView;
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
            //String ngrok = "c3091b38.ngrok.io";
            try {
                URL url = new URL("http://"+new SessionManager(getContext()).getUserDetails().get("address")+".ngrok.io/phpMQTT-master/files/get_image.php?timetableId="+params[0]);// + evt.getTimetableId());
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
            try {
                if (imageViewReference != null && result != null) {
                    final ImageView imageView = imageViewReference.get();
                    if (imageView != null) {
                        imageView.setImageBitmap(result);
                        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
                        marginParams.setMargins(25, 25, 25, 25);
                    } else {
                        imageView.setImageResource(R.mipmap.ic_noimage);
                    }
                }
            } catch (Exception e) {

            }
        }
    }

    public class ViewHolder {
        TextView category;
        TextView title;
        TextView date;
        TextView time;
        ImageView evtImg;
    }

}
