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
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class RedeemListAdapter extends ArrayAdapter<EventRegistration> {

    public RedeemListAdapter(Context context, int resource, List<EventRegistration> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.redeemlist_entry_layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.evt_title_redeem);
            viewHolder.btnRedeem = (Button) convertView.findViewById(R.id.redeem_button);
            viewHolder.dueDate = (TextView) convertView.findViewById(R.id.due_date);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final EventRegistration evt = getItem(position);
        viewHolder.title.setText(evt.getEventTitle());
        //viewHolder.dueDate.setText(Action.displayDate(evt.getEbTicketDueDate()));
        return convertView;
    }

    public class ViewHolder {
        TextView title;
        Button btnRedeem;
        TextView dueDate;
    }
}
