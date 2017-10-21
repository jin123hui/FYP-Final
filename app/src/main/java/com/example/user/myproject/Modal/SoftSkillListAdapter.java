package com.example.user.myproject.Modal;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.user.myproject.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class SoftSkillListAdapter extends ArrayAdapter<ApplicationEvent> {

    public SoftSkillListAdapter(Context context, int resource, List<ApplicationEvent> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.softskill_entry_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.softSkillEvtTitle);
            viewHolder.cs = (TextView) convertView.findViewById(R.id.ssCS);
            viewHolder.ctps = (TextView) convertView.findViewById(R.id.ssCTPS);
            viewHolder.ts = (TextView) convertView.findViewById(R.id.ssTS);
            viewHolder.ll = (TextView) convertView.findViewById(R.id.ssLL);
            viewHolder.kk = (TextView) convertView.findViewById(R.id.ssKK);
            viewHolder.em = (TextView) convertView.findViewById(R.id.ssEM);
            viewHolder.ls = (TextView) convertView.findViewById(R.id.ssLS);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final ApplicationEvent evt = getItem(position);
        viewHolder.title.setText(evt.getEventTitle());
        StringTokenizer tokens = new StringTokenizer(evt.getSoftSkillPoint(), ",");
        viewHolder.cs.setText(tokens.nextToken());
        viewHolder.ctps.setText(tokens.nextToken());
        viewHolder.ts.setText(tokens.nextToken());
        viewHolder.ll.setText(tokens.nextToken());
        viewHolder.kk.setText(tokens.nextToken());
        viewHolder.em.setText(tokens.nextToken());
        viewHolder.ls.setText(tokens.nextToken());

        return convertView;
    }

    public class ViewHolder {
        TextView title;
        TextView cs;
        TextView ctps;
        TextView ts;
        TextView ll;
        TextView kk;
        TextView em;
        TextView ls;
    }
}
