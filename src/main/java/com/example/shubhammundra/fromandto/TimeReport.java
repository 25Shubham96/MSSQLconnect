package com.example.shubhammundra.fromandto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class TimeReport extends AppCompatActivity implements OnItemSelectedListener {

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

    String selectStoreNo = "S0001";

    String dropDownStoreNo;

    public ArrayList<String> Hour = new ArrayList<>();
    public ArrayList<Float> NetAmt = new ArrayList<>();
    public ArrayList<Float> AvgNetAmt = new ArrayList<>();

    public float a = 0,b = 0,c = 0;
    public String a1 = "",b1 = "",c1 = "";

    Button btnChart;
    CombinedChart viewTimeChart;

    Calendar startCalendar = Calendar.getInstance();
    Calendar endCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.time_report_table);

        Spinner spinner = findViewById(R.id.spin_storeselect);
        spinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.StoreNo, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        doInBackground();

        btnChart = findViewById(R.id.btn_timewisechart);

        btnChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chartIntent = new Intent(TimeReport.this,DisplayChart.class);
                chartIntent.putExtra("Net Amount",NetAmt);
                chartIntent.putExtra("Avg Net Amount",AvgNetAmt);
                chartIntent.putExtra("Hour",Hour);
                chartIntent.putExtra("Store No.",selectStoreNo);
                startActivity(chartIntent);
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        dropDownStoreNo = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(adapterView.getContext(), "Selected Store : " + dropDownStoreNo, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    private void doInBackground()
    {
        selectStoreNo = dropDownStoreNo;

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
                while (TL.getChildCount() > 1) {
                    TL.removeView(TL.getChildAt(TL.getChildCount() - 1));
                }
                NetAmt.clear();
                AvgNetAmt.clear();
                Hour.clear();

                doInBackground();
            }
        });

        storeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(TimeReport.this, DPstartDate, startCalendar.get(Calendar.YEAR), startCalendar.get(Calendar.MONTH), startCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(TimeReport.this, DPendDate, endCalendar.get(Calendar.YEAR), endCalendar.get(Calendar.MONTH), endCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeStart.setText(storeStartDate);
        storeEnd.setText(storeEndDate);

        ConnectionHelper connectString = new ConnectionHelper();
        connect = connectString.connectionClass();

        Log.w("Android", "Connecting....");

        if (connect == null) {
            Log.w("Android", "Connection Failed");
            ConnectionResult = "Check Your Internet Connection";
        } else {
            Log.w("Android", "Connected");
            String query = "Select datepart(hh,[Time]) as POS, sum([Net Amount]) as sumNetAmt, count([Receipt No_]) as noOfReceipt from [CRONUS LS 1010 W1 Demo$Transaction Header] where [Store No_] = '"+selectStoreNo+"' and [Date] between '" + storeStartDate + "' and '" + storeEndDate + "' group by datepart(hh,[Time])";
            try {
                Statement stmt = connect.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                TL = findViewById(R.id.tl_myTable);
                TR = findViewById(R.id.tr_index);
                TV1 = findViewById(R.id.tv_pos);
                TV2 = findViewById(R.id.tv_netamt);
                TV5 = findViewById(R.id.tv_avgnetamt);

                Log.w("Android", "database Connected");

                while (rs.next()) {
                    TL.setColumnStretchable(0, true);
                    TL.setColumnStretchable(1, true);
                    TL.setColumnStretchable(2, true);

                    TR = new TableRow(this);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(2, 0xFF000000);

                    TV1 = new TextView(this);
                    TV2 = new TextView(this);
                    TV5 = new TextView(this);

                    String netAmt = rs.getString("sumNetAmt");
                    Double amt = Double.parseDouble(netAmt);
                    Double roundAmt = (double) Math.round(amt * 100.0) / 100.0;
                    roundAmt = Math.abs(roundAmt);
                    netAmt = roundAmt.toString();

                    String avgNetAmt = rs.getString("noOfReceipt");
                    Double count = Double.parseDouble(avgNetAmt);
                    count = roundAmt / count;
                    count = (double) Math.round(count * 1.0) / 1.0;
                    avgNetAmt = count.toString();

                    TV1.setText(rs.getString("POS"));
                    TV2.setText(netAmt);
                    TV5.setText(avgNetAmt);

                    TV1.setTextSize(15);
                    TV1.setPadding(10, 0, 10, 0);
                    TV1.setGravity(Gravity.RIGHT);
                    TV1.setTextColor(Color.BLACK);
                    TV1.setBackground(gd);

                    TV2.setTextSize(15);
                    TV2.setPadding(10, 0, 10, 0);
                    TV2.setGravity(Gravity.RIGHT);
                    TV2.setTextColor(Color.BLACK);
                    TV2.setBackground(gd);

                    TV5.setTextSize(15);
                    TV5.setPadding(10, 0, 10, 0);
                    TV5.setGravity(Gravity.RIGHT);
                    TV5.setTextColor(Color.BLACK);
                    TV5.setBackground(gd);

                    TR.addView(TV1);
                    TR.addView(TV2);
                    TR.addView(TV5);

                    TL.addView(TR);

                    a1 = TV1.getText().toString();
                    b1 = TV2.getText().toString();
                    c1 = TV5.getText().toString();

                    //a = Float.parseFloat(a1);
                    b = Float.parseFloat(b1);
                    c = Float.parseFloat(c1);

                    Hour.add(a1);
                    NetAmt.add(b);
                    AvgNetAmt.add(c);
                }

                ConnectionResult = "Successful";
                isSuccess = true;
                connect.close();
            } catch (SQLException e) {
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

class DisplayChart extends TimeReport{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_multilinechart);

        viewTimeChart = findViewById(R.id.combinedchart);

        Intent getTimeChartIntent = getIntent();

        ArrayList<String> getTimePOS = (ArrayList<String>) getTimeChartIntent.getSerializableExtra("Hour");
        ArrayList<Float> getTimeNetAmt = (ArrayList<Float>) getTimeChartIntent.getSerializableExtra("Net Amount");
        ArrayList<Float> getTimeAvgNetAmt = (ArrayList<Float>) getTimeChartIntent.getSerializableExtra("Avg Net Amount");
        String getStoreNo = getTimeChartIntent.getStringExtra("Store No.");

        TextView textView = findViewById(R.id.tv_storeNo);
        textView.setText(getStoreNo);

        ArrayList<Entry> NetAmount = new ArrayList<>();
        ArrayList<BarEntry> AvgNetAmount = new ArrayList<>();

        for (int i = 0 ; i < getTimeNetAmt.size() ; i++)
        {
            NetAmount.add(new Entry(getTimeNetAmt.get(i),i));
        }

        for (int i = 0 ; i < getTimeAvgNetAmt.size() ; i++)
        {
            AvgNetAmount.add(new BarEntry(getTimeAvgNetAmt.get(i),i));
        }

        LineDataSet lineDataSet = new LineDataSet(NetAmount,"Net Amount");
        lineDataSet.setColor(Color.BLUE);
        LineData lineData = new LineData(getTimePOS, lineDataSet);
        lineData.setValueTextSize(16f);

        BarDataSet barDataSet = new BarDataSet(AvgNetAmount,"Avg Net Amount");
        barDataSet.setColor(Color.MAGENTA);
        BarData barData = new BarData(getTimePOS,barDataSet);
        barData.setValueTextSize(16f);

        CombinedData data = new CombinedData(getTimePOS);
        data.setData(lineData);
        data.setData(barData);

        viewTimeChart.setData(data);

        viewTimeChart.setDescription("Time wise sales");
    }
}

class Time2Report extends TimeReport{

    TableLayout time2TL;
    TableRow time2TR;
    TextView TV;

    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        time2DoInBackground();

    }

    public void time2DoInBackground()
    {
        ConnectionHelper connectString = new ConnectionHelper();
        connect = connectString.connectionClass();

        Log.w("Android", "Connecting....");

        if (connect == null) {
            Log.w("Android", "Connection Failed");
            ConnectionResult = "Check Your Internet Connection";
        } else {
            Log.w("Android", "Connected");
            String query = "Select datepart(hh,[Time]) as POS, sum([Net Amount]) as sumNetAmt, count([Receipt No_]) as noOfReceipt from [CRONUS LS 1010 W1 Demo$Transaction Header] where [Store No_] = '" + selectStoreNo + "' and [Date] between '" + storeStartDate + "' and '" + storeEndDate + "' group by datepart(hh,[Time])";
            try {
                Statement stmt = connect.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                TL = findViewById(R.id.tl_myTable);
                TR = findViewById(R.id.tr_index);
                TV1 = findViewById(R.id.tv_pos);
                TV2 = findViewById(R.id.tv_netamt);
                TV5 = findViewById(R.id.tv_avgnetamt);

                Log.w("Android", "database Connected");

                while (rs.next()) {
                    TL.setColumnStretchable(0, true);
                    TL.setColumnStretchable(1, true);
                    TL.setColumnStretchable(2, true);

                    TR = new TableRow(this);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(2, 0xFF000000);

                    TV1 = new TextView(this);
                    TV2 = new TextView(this);
                    TV5 = new TextView(this);

                    String netAmt = rs.getString("sumNetAmt");
                    Double amt = Double.parseDouble(netAmt);
                    Double roundAmt = (double) Math.round(amt * 100.0) / 100.0;
                    roundAmt = Math.abs(roundAmt);
                    netAmt = roundAmt.toString();

                    String avgNetAmt = rs.getString("noOfReceipt");
                    Double count = Double.parseDouble(avgNetAmt);
                    count = roundAmt / count;
                    count = (double) Math.round(count * 1.0) / 1.0;
                    avgNetAmt = count.toString();

                    TV1.setText(rs.getString("POS"));
                    TV2.setText(netAmt);
                    TV5.setText(avgNetAmt);

                    TV1.setTextSize(15);
                    TV1.setPadding(10, 0, 10, 0);
                    TV1.setGravity(Gravity.RIGHT);
                    TV1.setTextColor(Color.BLACK);
                    TV1.setBackground(gd);

                    TV2.setTextSize(15);
                    TV2.setPadding(10, 0, 10, 0);
                    TV2.setGravity(Gravity.RIGHT);
                    TV2.setTextColor(Color.BLACK);
                    TV2.setBackground(gd);

                    TV5.setTextSize(15);
                    TV5.setPadding(10, 0, 10, 0);
                    TV5.setGravity(Gravity.RIGHT);
                    TV5.setTextColor(Color.BLACK);
                    TV5.setBackground(gd);

                    TR.addView(TV1);
                    TR.addView(TV2);
                    TR.addView(TV5);

                    TL.addView(TR);

                    a1 = TV1.getText().toString();
                    b1 = TV2.getText().toString();
                    c1 = TV5.getText().toString();

                    //a = Float.parseFloat(a1);
                    b = Float.parseFloat(b1);
                    c = Float.parseFloat(c1);

                    Hour.add(a1);
                    NetAmt.add(b);
                    AvgNetAmt.add(c);
                }

                ConnectionResult = "Successful";
                isSuccess = true;
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}