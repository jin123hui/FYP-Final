package com.example.user.myproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.user.myproject.Modal.ApplicationEvent;
import com.example.user.myproject.Modal.EventRegistration;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Report extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private HashMap<String, Integer> evtMap;
    private HashMap<Integer, String> evtMap2;
    private ArrayList<ApplicationEvent> evt;
    private final String[] category = new String[]{"Business", "Education", "Game", "IT", "Music", "Sports"};
    private View mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        evt = new ArrayList<>();

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            if (!extras.isEmpty()) {
                evt = (ArrayList<ApplicationEvent>) extras.getSerializable("EVENTS");
            }
        } else {

        }

        evtMap = new HashMap<>();
        for(String cat:category) {
            evtMap.put(cat, 0);
        }
        for(int i=0; i<evt.size(); i++) {
            for(String cat:category) {
                if(evt.get(i).getActivityType().equals(cat)) {
                    evtMap.put(cat, evtMap.get(cat) + 1);
                }
            }
        }

        setPieChart();
        setBarChart();
    }

    private void setPieChart() {
        pieChart = (PieChart) findViewById(R.id.chart);
        //pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        ArrayList<PieEntry> yValues = new ArrayList<>();
        for(String cat:category) {
            if(evtMap.get(cat)>0)
                yValues.add(new PieEntry(evtMap.get(cat), cat));
        }

        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);
        PieDataSet dataSet = new PieDataSet(yValues, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(20f);
        data.setValueTextColor(Color.WHITE);
        data.setValueFormatter(new PCValueFormatter());

        Legend l = pieChart.getLegend();
        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        l.setDirection(Legend.LegendDirection.LEFT_TO_RIGHT);
        l.setTextSize(15f);
        l.setWordWrapEnabled(true);

        if(!yValues.isEmpty())
            pieChart.setData(data);
    }

    private void setBarChart() {
        barChart = (BarChart) findViewById(R.id.chart2);
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.animateXY(2000, 2000);

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> catStr = new ArrayList<>();
        int count = 0;
        for(String cat:category) {
            if(evtMap.get(cat)>0) {
                entries.add(new BarEntry(count, evtMap.get(cat)));
                count++;
                catStr.add(cat);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        ArrayList<String> labels = new ArrayList<>();
        for(String cat:category) {
            labels.add(cat);
        }

        BarData data = new BarData(dataSet);
        data.setValueTextSize(20f);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new PCValueFormatter());

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new ReportChartXAxisValueFormatter(catStr));

        Legend l = barChart.getLegend();
        l.setEnabled(false);

        if(!entries.isEmpty())
            barChart.setData(data);

        /*YAxis yAxis = barChart.getAxis(YAxis.AxisDependency.LEFT);

        float labelInterval = 1.0f;

        int sections;
        do {
            labelInterval *= 2;
            sections = ((int) Math.ceil(barChart.getYMax() / labelInterval));
        } while (sections > 10);

        if (barChart.getYMax() == sections * labelInterval) {
            sections++;
        }

        yAxis.setAxisMaximum(labelInterval * sections);
        yAxis.setLabelCount(sections + 1, true);

        YAxis yAxisR = barChart.getAxis(YAxis.AxisDependency.RIGHT);
        yAxisR.setEnabled(false);*/

        barChart.invalidate();
    }

    private void savePdf() {
        String state = Environment.getExternalStorageState();
        if (!Environment.MEDIA_MOUNTED.equals(state)) {
        }

        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MyApp");
        if (!pdfDir.exists()){
            pdfDir.mkdir();
        }

       // mRootView = inflater.inflate(R.layout.fragment_blank, container, false);
        mRootView = getWindow().getDecorView().getRootView();
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
        LinearLayout root = (LinearLayout) inflater.inflate(R.layout.content_report, null);
        View v1 = mRootView.getRootView();
        v1.setDrawingCacheEnabled(true);
        Bitmap screen = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        File pdfFile = new File(pdfDir, "pastEventsReport.pdf");
        Toast.makeText(getApplicationContext(),"HI", Toast.LENGTH_LONG).show();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();
            Toast.makeText(getApplicationContext(),"HI2", Toast.LENGTH_LONG).show();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            screen.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            addImage(document,byteArray);
            Toast.makeText(getApplicationContext(), pdfFile.toString(), Toast.LENGTH_LONG).show();
            document.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //Uri uri = Uri.fromFile(pdfFile);
        //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //intent.setDataAndType(uri, "application/pdf");
        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //startActivity(intent);
    }

    private static void addImage(Document document,byte[] byteArray)
    {
        Image image = null;
        try
        {
            image = Image.getInstance(byteArray);
        }
        catch (BadElementException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            document.add(image);
        } catch (DocumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.save_report_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //case R.id.action_save_report:
            //    savePdf();
            //    break;
            case R.id.home:
                this.finish();
                break;
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }


}

class PCValueFormatter implements IValueFormatter {

    private DecimalFormat mFormat;

    public PCValueFormatter() {
        mFormat = new DecimalFormat("###,###,##0");
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {

        if(value > 0) {
            return mFormat.format(value);
        } else {
            return "";
        }
    }
}

class ReportChartXAxisValueFormatter implements IAxisValueFormatter {
    private List labels;

    public ReportChartXAxisValueFormatter(List<String> labels) {
        this.labels = labels;
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return (String)labels.get((int)value);
    }
}