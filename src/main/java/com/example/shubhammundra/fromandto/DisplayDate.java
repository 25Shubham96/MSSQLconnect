package com.example.shubhammundra.fromandto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DisplayDate extends MainActivity{

    TableLayout TL;
    TableRow TR;
    TextView TV1, TV2, TV5;
    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;

    TextView storeStart;
    TextView storeEnd;

    ImageView refresh;

    String storeStartDate = "2001-01-01";

    String storeEndDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    public ArrayList<Float> yData = new ArrayList<>();
    public ArrayList<String> xData = new ArrayList<>();

    public float a = 0;
    public String c = "", d = "";
    Button pieChart1,pieChart21,storeLineChart;
    BarChart barChart;
    PieChart storePieChart;
    LineChart viewStoreLineChart;

    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_date);

        doInBackground();

        pieChart1 = findViewById(R.id.btn_piechart);

        pieChart1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pieIntent = new Intent(DisplayDate.this,DisplayPiechart.class);
                pieIntent.putExtra("X Values",xData);
                pieIntent.putExtra("Y Values",yData);
                startActivity(pieIntent);
            }
        });

        pieChart21 = findViewById(R.id.btn_piechart2);

        pieChart21.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Float> tempStoreAmount = new ArrayList<>();
                ArrayList<String> tempStoreNo = new ArrayList<>();
                tempStoreAmount = (ArrayList<Float>) yData.clone();
                tempStoreNo = (ArrayList<String>) xData.clone();
                Intent storeIntent = new Intent(DisplayDate.this,StorePieChart.class);
                storeIntent.putExtra("Store Amount",tempStoreAmount);
                storeIntent.putExtra("Store No",tempStoreNo);
                startActivity(storeIntent);
            }
        });

        storeLineChart = findViewById(R.id.btn_storeLinechart);

        storeLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Float> tempLineStoreAmount = new ArrayList<>();
                ArrayList<String> tempLineStoreNo = new ArrayList<>();
                tempLineStoreAmount = (ArrayList<Float>) yData.clone();
                tempLineStoreNo = (ArrayList<String>) xData.clone();
                Intent storeLineIntent = new Intent(DisplayDate.this,StoreLineChart.class);
                storeLineIntent.putExtra("Store Line Amount",tempLineStoreAmount);
                storeLineIntent.putExtra("Store Line No",tempLineStoreNo);
                startActivity(storeLineIntent);
            }
        });
    }

    public void doInBackground() {

        final DatePickerDialog.OnDateSetListener DPstartDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                startCalendar.set(Calendar.YEAR, year);
                startCalendar.set(Calendar.MONTH, month);
                startCalendar.set(Calendar.DAY_OF_MONTH, day);
                StartUpdateLabel();

                storeStartDate = storeStart.getText().toString();
            }
        };

        final DatePickerDialog.OnDateSetListener DPendDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                endCalendar.set(Calendar.YEAR, year);
                endCalendar.set(Calendar.MONTH, month);
                endCalendar.set(Calendar.DAY_OF_MONTH, day);
                EndUpdateLabel();

                storeEndDate = storeEnd.getText().toString();
            }
        };

        refresh = findViewById(R.id.iv_refresh);
        storeStart = findViewById(R.id.tv_startdate);
        storeEnd = findViewById(R.id.tv_enddate);

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while(TL.getChildCount() > 1)
                {
                    TL.removeView(TL.getChildAt(TL.getChildCount() - 1));
                }
                xData.clear();
                yData.clear();

                doInBackground();
            }
        });

        storeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(DisplayDate.this, DPstartDate, startCalendar.get(Calendar.YEAR),startCalendar.get(Calendar.MONTH),startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(DisplayDate.this, DPendDate, endCalendar.get(Calendar.YEAR),endCalendar.get(Calendar.MONTH),endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeStart.setText(storeStartDate);
        storeEnd.setText(storeEndDate);

        ConnectionHelper connectString = new ConnectionHelper();
        connect = connectString.connectionClass();

        Log.w("Android","Connecting....");

        if (connect == null)
        {
            Log.w("Android","Connection Failed");
            ConnectionResult = "Check Your Internet Connection";
        }
        else
        {
            Log.w("Android","Connected");
            String query = "Select No_,Name,sum([Net Amount]) as sumNetAmt from [CRONUS LS 1010 W1 Demo$Store] as str,[CRONUS LS 1010 W1 Demo$Transaction Header] as trans where str.[No_] = trans.[Store No_] and trans.[Date] between '"+storeStartDate+"' and '"+storeEndDate+"' group by str.[No_],str.[Name],trans.[Store No_]";
            try
            {
                Statement stmt = connect.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                TL = findViewById(R.id.tl_myTable);
                TR = findViewById(R.id.tr_index);
                TV1 = findViewById(R.id.tv_no);
                TV2 = findViewById(R.id.tv_name);
                TV5 = findViewById(R.id.tv_netamt);

                Log.w("Android","database Connected");

                while (rs.next()) {
                    TL.setColumnStretchable(0, true);
                    TL.setColumnStretchable(1, true);
                    TL.setColumnStretchable(2,true);

                    TR = new TableRow(this);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(2, 0xFF000000);

                    TV1 = new TextView(this);
                    TV2 = new TextView(this);
                    TV5 = new TextView(this);

                    String netAmt = rs.getString("sumNetAmt");
                    Double amt = Double.parseDouble(netAmt);
                    Double roundAmt = (double) Math.round(amt*100.0)/100.0;
                    roundAmt = Math.abs(roundAmt);
                    netAmt = roundAmt.toString();

                    TV1.setText(rs.getString("No_"));
                    TV2.setText(rs.getString("Name"));
                    TV5.setText(netAmt);

                    TV1.setTextSize(15);
                    TV1.setPadding(10,0,10,0);
                    TV1.setGravity(Gravity.LEFT);
                    TV1.setTextColor(Color.BLACK);
                    TV1.setBackground(gd);

                    TV2.setTextSize(15);
                    TV2.setPadding(10,0,10,0);
                    TV2.setGravity(Gravity.LEFT);
                    TV2.setTextColor(Color.BLACK);
                    TV2.setBackground(gd);

                    TV5.setTextSize(15);
                    TV5.setPadding(10,0,10,0);
                    TV5.setGravity(Gravity.RIGHT);
                    TV5.setTextColor(Color.BLACK);
                    TV5.setBackground(gd);

                    TR.addView(TV1);
                    TR.addView(TV2);
                    TR.addView(TV5);

                    TL.addView(TR);

                    c = TV5.getText().toString();
                    a = Float.parseFloat(c);
                    d = TV1.getText().toString();

                    xData.add(d);
                    yData.add(a);
                }

                ConnectionResult = "Successful";
                isSuccess = true;
                connect.close();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void StartUpdateLabel(){
        String myStartFormat = "yyyy-MM-dd";
        SimpleDateFormat STARTsdf = new SimpleDateFormat(myStartFormat,Locale.US);
        storeStart.setText(STARTsdf.format(startCalendar.getTime()));
    }

    public void EndUpdateLabel(){
        String myEndFormat = "yyyy-MM-dd";
        SimpleDateFormat ENDsdf = new SimpleDateFormat(myEndFormat,Locale.US);
        storeEnd.setText(ENDsdf.format(endCalendar.getTime()));
    }
}

class DisplayPiechart extends DisplayDate{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_barchart);

        barChart = findViewById(R.id.barchart);

        Intent intent = getIntent();

        ArrayList<Float> y1Data = (ArrayList<Float>) intent.getSerializableExtra("Y Values");
        ArrayList<String> x1Data = (ArrayList<String>) intent.getSerializableExtra("X Values");

        ArrayList<BarEntry> yEntrys = new ArrayList<>();

        for(int i = 0; i < y1Data.size(); i++){
            yEntrys.add(new BarEntry(y1Data.get(i),i));
        }

        BarDataSet barDataSet = new BarDataSet(yEntrys,"");
        int[] color12 = {Color.parseColor("#D32F2F"),
                Color.parseColor("#C2185B"),
                Color.parseColor("#7B1FA2"),
                Color.parseColor("#512DA8"),
                Color.parseColor("#303F9F"),
                Color.parseColor("#1976D2"),
                Color.parseColor("#0288D1"),
                Color.parseColor("#0097A7"),
                Color.parseColor("#00796B"),
                Color.parseColor("#388E3C"),
                Color.parseColor("#689F38"),
                Color.parseColor("#AFB42B"),
                Color.parseColor("#FBC02D"),
                Color.parseColor("#FFA000"),
                Color.parseColor("#F57C00"),
                Color.parseColor("#E64A19"),
                Color.parseColor("#5D4037"),
                Color.parseColor("#616161"),
                Color.parseColor("#455A64")};

        barDataSet.setColors(color12);
        barDataSet.setValueTextSize(14f);

        BarData barData = new BarData(x1Data, barDataSet);

        barChart.setData(barData);
        barChart.animateY(2000, Easing.EasingOption.EaseInOutBack);

        barChart.setDescription("Store Wise Entry");
        barChart.setDescriptionTextSize(18f);
    }
}

class StorePieChart extends DisplayDate{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_piechart);

        storePieChart = findViewById(R.id.piechart);

        Intent getStorePieIntent = getIntent();

        ArrayList<Float> getStoreAmount = new ArrayList<>();
        getStoreAmount = (ArrayList<Float>) getStorePieIntent.getSerializableExtra("Store Amount");
        ArrayList<String> getStoreNo = new ArrayList<>();
        getStoreNo = (ArrayList<String>) getStorePieIntent.getSerializableExtra("Store No");

        ArrayList<Entry> PieStoreAmount = new ArrayList<>();

        for (int i = 0 ; i < getStoreAmount.size() ; i++)
        {
            PieStoreAmount.add(new Entry(getStoreAmount.get(i),i));
        }

        PieDataSet storePieDataSet = new PieDataSet(PieStoreAmount,"");

        int[] storePieColor = {Color.parseColor("#D32F2F"),
                Color.parseColor("#C2185B"),
                Color.parseColor("#7B1FA2"),
                Color.parseColor("#512DA8"),
                Color.parseColor("#303F9F"),
                Color.parseColor("#1976D2"),
                Color.parseColor("#0288D1"),
                Color.parseColor("#0097A7"),
                Color.parseColor("#00796B"),
                Color.parseColor("#388E3C"),
                Color.parseColor("#689F38"),
                Color.parseColor("#AFB42B"),
                Color.parseColor("#FBC02D"),
                Color.parseColor("#FFA000"),
                Color.parseColor("#F57C00"),
                Color.parseColor("#E64A19"),
                Color.parseColor("#5D4037"),
                Color.parseColor("#616161"),
                Color.parseColor("#455A64")};

        storePieDataSet.setColors(storePieColor);
        storePieDataSet.setValueTextSize(16f);
        storePieDataSet.setValueTextColor(Color.WHITE);
        storePieDataSet.setSliceSpace(0.5f);

        PieData storePieData = new PieData(getStoreNo,storePieDataSet);
        storePieChart.setData(storePieData);

        storePieChart.setHoleColor(Color.TRANSPARENT);
        storePieChart.setCenterText("Store");
        storePieChart.setCenterTextColor(Color.BLACK);
        storePieChart.setDragDecelerationFrictionCoef(0.90f);

        storePieChart.animateY(2000,Easing.EasingOption.EaseInCirc);

        storePieChart.setDescription("Store Wise Entry");
        storePieChart.setDescriptionTextSize(18f);
    }
}

class StoreLineChart extends DisplayDate{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_linechart);

        viewStoreLineChart = findViewById(R.id.linechart);

        Intent getStoreLineIntent = getIntent();

        ArrayList<Float> getStoreLineAmount = new ArrayList<Float>();
        getStoreLineAmount = (ArrayList<Float>) getStoreLineIntent.getSerializableExtra("Store Line Amount");
        ArrayList<String> storeLineCode = new ArrayList<String>();
        storeLineCode = (ArrayList<String>) getStoreLineIntent.getSerializableExtra("Store Line No");

        ArrayList<Entry> storeLineAmount = new ArrayList<>();

        for (int i = 0 ; i < getStoreLineAmount.size() ; i++){
            storeLineAmount.add(new Entry(getStoreLineAmount.get(i),i));
        }

        LineDataSet storelineDataSet = new LineDataSet(storeLineAmount,"");

        int[] storeLineColor = {Color.parseColor("#D32F2F"),
                Color.parseColor("#C2185B"),
                Color.parseColor("#7B1FA2"),
                Color.parseColor("#512DA8"),
                Color.parseColor("#303F9F"),
                Color.parseColor("#1976D2"),
                Color.parseColor("#0288D1"),
                Color.parseColor("#0097A7"),
                Color.parseColor("#00796B"),
                Color.parseColor("#388E3C"),
                Color.parseColor("#689F38"),
                Color.parseColor("#AFB42B"),
                Color.parseColor("#FBC02D"),
                Color.parseColor("#FFA000"),
                Color.parseColor("#F57C00"),
                Color.parseColor("#E64A19"),
                Color.parseColor("#5D4037"),
                Color.parseColor("#616161"),
                Color.parseColor("#455A64")};

        storelineDataSet.setColors(storeLineColor);
        storelineDataSet.setValueTextSize(16f);
        storelineDataSet.setValueTextColor(Color.BLACK);

        LineData ItemlineData = new LineData(storeLineCode,storelineDataSet);

        viewStoreLineChart.setData(ItemlineData);

        viewStoreLineChart.setDescription("Store Wise Entry");

        viewStoreLineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
    }
}