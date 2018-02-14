package com.example.shubhammundra.fromandto;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
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

public class ItemCategoryReport extends MainActivity {
    
    TableLayout TL1;
    TableRow TR1;
    TextView TV11, TV12, TV13;
    Connection connect;
    String ConnectionResult = "";
    Boolean isSuccess = false;

    TextView storeStartIC;
    TextView storeEndIC;

    ImageView refreshIC;

    String storeStartDateIC = "2001-01-01";

    String storeEndDateIC = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

    public ArrayList<Float> yData1 = new ArrayList<>();
    public ArrayList<String> xData1 = new ArrayList<>();

    public ArrayList<Float> amount = new ArrayList<Float>();
    public ArrayList<String> itemCode = new ArrayList<String>();

    public float a1 = 0;
    public String c1 = "", d1 = "";

    Button pieChart,itemPieChart,itemLineChart;
    BarChart barChart;
    PieChart viewPieChart;
    LineChart viewItemLineChart;

    Calendar startCalendarIC = Calendar.getInstance();
    Calendar endCalendarIC = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_categorty_report);

        doInBackground();

        pieChart = findViewById(R.id.btn_piechart1);

        pieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pieIntent = new Intent(ItemCategoryReport.this,DisplayBarchart.class);
                pieIntent.putExtra("X1 Values",xData1);
                pieIntent.putExtra("Y1 Values",yData1);
                startActivity(pieIntent);
            }
        });
        itemPieChart = findViewById(R.id.btn_itemPiechart);

        itemPieChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Float> tempAmount = new ArrayList<>();
                ArrayList<String> tempItemCode = new ArrayList<>();
                tempAmount = (ArrayList<Float>) amount.clone();
                tempItemCode = (ArrayList<String>) itemCode.clone();
                Intent itemPieIntent = new Intent(ItemCategoryReport.this,ItemPieChart.class);
                itemPieIntent.putExtra("amount",tempAmount);
                itemPieIntent.putExtra("itemCode",tempItemCode);
                startActivity(itemPieIntent);
            }
        });

        itemLineChart = findViewById(R.id.btn_itemLinechart);

        itemLineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<Float> tempLineItemAmount = new ArrayList<>();
                ArrayList<String> tempLineItemCode = new ArrayList<>();
                tempLineItemAmount = (ArrayList<Float>) amount.clone();
                tempLineItemCode = (ArrayList<String>) itemCode.clone();
                Intent storeLineIntent = new Intent(ItemCategoryReport.this,ItemLineChart.class);
                storeLineIntent.putExtra("Item Line Amount",tempLineItemAmount);
                storeLineIntent.putExtra("Item Line Code",tempLineItemCode);
                startActivity(storeLineIntent);
            }
        });
    }

    public void doInBackground() {
        final DatePickerDialog.OnDateSetListener DPstartDateIC = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                startCalendarIC.set(Calendar.YEAR, year);
                startCalendarIC.set(Calendar.MONTH, month);
                startCalendarIC.set(Calendar.DAY_OF_MONTH, day);
                StartUpdateLabelIC();

                storeStartDateIC = storeStartIC.getText().toString();
            }
        };

        final DatePickerDialog.OnDateSetListener DPendDate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                endCalendarIC.set(Calendar.YEAR, year);
                endCalendarIC.set(Calendar.MONTH, month);
                endCalendarIC.set(Calendar.DAY_OF_MONTH, day);
                EndUpdateLabelIC();

                storeEndDateIC = storeEndIC.getText().toString();
            }
        };

        refreshIC = findViewById(R.id.iv_refresh);
        storeStartIC = findViewById(R.id.tv_startdate1);
        storeEndIC = findViewById(R.id.tv_enddate1);

        refreshIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while(TL1.getChildCount() > 1)
                {
                    TL1.removeView(TL1.getChildAt(TL1.getChildCount() - 1));
                }
                xData1.clear();
                yData1.clear();

                doInBackground();
            }
        });

        storeStartIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ItemCategoryReport.this, DPstartDateIC, startCalendarIC.get(Calendar.YEAR),startCalendarIC.get(Calendar.MONTH),startCalendarIC.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeEndIC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DatePickerDialog(ItemCategoryReport.this, DPendDate, endCalendarIC.get(Calendar.YEAR),endCalendarIC.get(Calendar.MONTH),endCalendarIC.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        storeStartIC.setText(storeStartDateIC);
        storeEndIC.setText(storeEndDateIC);

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
            String query = "Select itemC.[Code],itemC.[Description],sum(transSE.[Net Amount]) as sumPrice from [CRONUS LS 1010 W1 Demo$Trans_ Sales Entry] as transSE,[CRONUS LS 1010 W1 Demo$Item Category] as itemC where transSE.[Item Category Code] = itemC.[Code] and transSE.[Date] between '"+storeStartDateIC+"' and '"+storeEndDateIC+"' group by transSE.[Item Category Code],itemC.[Code],itemC.[Description]";
            try
            {
                Statement stmt = connect.createStatement();
                ResultSet rs = stmt.executeQuery(query);

                TL1 = findViewById(R.id.tl_myTable1);
                TR1 = findViewById(R.id.tr_index1);
                TV11 = findViewById(R.id.tv_no1);
                TV12 = findViewById(R.id.tv_name1);
                TV13 = findViewById(R.id.tv_date1);

                Log.w("Android","database Connected");

                while (rs.next()) {
                    TL1.setColumnStretchable(0, true);
                    TL1.setColumnStretchable(1, true);
                    TL1.setColumnStretchable(2,true);

                    TR1 = new TableRow(this);

                    GradientDrawable gd = new GradientDrawable();
                    gd.setStroke(2, 0xFF000000);

                    TV11 = new TextView(this);
                    TV12 = new TextView(this);
                    TV13 = new TextView(this);

                    String netAmt = rs.getString("sumPrice");
                    Double amt = Double.parseDouble(netAmt);
                    Double roundAmt = (double) Math.round(amt*100.0)/100.0;
                    roundAmt = Math.abs(roundAmt);
                    netAmt = roundAmt.toString();

                    TV11.setText(rs.getString("Code"));
                    TV12.setText(rs.getString("Description"));
                    TV13.setText(netAmt);

                    TV11.setTextSize(15);
                    TV11.setPadding(10,0,10,0);
                    TV11.setGravity(Gravity.LEFT);
                    TV11.setTextColor(Color.BLACK);
                    TV11.setBackground(gd);

                    TV12.setTextSize(15);
                    TV12.setPadding(10,0,10,0);
                    TV12.setGravity(Gravity.LEFT);
                    TV12.setTextColor(Color.BLACK);
                    TV12.setBackground(gd);

                    TV13.setTextSize(15);
                    TV13.setPadding(10,0,10,0);
                    TV13.setGravity(Gravity.RIGHT);
                    TV13.setTextColor(Color.BLACK);
                    TV13.setBackground(gd);

                    TR1.addView(TV11);
                    TR1.addView(TV12);
                    TR1.addView(TV13);

                    TL1.addView(TR1);

                    c1 = TV13.getText().toString();
                    a1 = Float.parseFloat(c1);
                    d1 = TV11.getText().toString();

                    xData1.add(d1);
                    yData1.add(a1);

                    amount.add(a1);
                    itemCode.add(d1);
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
    public void StartUpdateLabelIC(){
        String myStartFormat = "yyyy-MM-dd";
        SimpleDateFormat STARTsdf = new SimpleDateFormat(myStartFormat,Locale.US);
        storeStartIC.setText(STARTsdf.format(startCalendarIC.getTime()));
    }

    public void EndUpdateLabelIC(){
        String myEndFormat = "yyyy-MM-dd";
        SimpleDateFormat ENDsdf = new SimpleDateFormat(myEndFormat,Locale.US);
        storeEndIC.setText(ENDsdf.format(endCalendarIC.getTime()));
    }
}

class DisplayBarchart extends ItemCategoryReport{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_barchart);

        barChart = findViewById(R.id.barchart);

        Intent intent = getIntent();

        ArrayList<Float> y11Data = (ArrayList<Float>) intent.getSerializableExtra("Y1 Values");
        ArrayList<String> x11Data = (ArrayList<String>) intent.getSerializableExtra("X1 Values");

        ArrayList<BarEntry> y1Entrys = new ArrayList<>();

        for(int i = 0; i < y11Data.size(); i++){
            y1Entrys.add(new BarEntry(y11Data.get(i),i));
        }

        BarDataSet barDataSet = new BarDataSet(y1Entrys,"");
        int[] color11 = {Color.parseColor("#D32F2F"),
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

        barDataSet.setColors(color11);
        barDataSet.setValueTextSize(14f);

        BarData barData = new BarData(x11Data, barDataSet);

        barChart.setData(barData);
        barChart.animateY(2000, Easing.EasingOption.EaseInOutBack);

        barChart.setDescription("Item Category Wise Entry");
        barChart.setDescriptionTextSize(18f);
    }
}

class ItemPieChart extends ItemCategoryReport{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_piechart);

        viewPieChart = findViewById(R.id.piechart);

        Intent getItemPieIntent = getIntent();

        ArrayList<Float> getamount = new ArrayList<Float>();
        getamount = (ArrayList<Float>) getItemPieIntent.getSerializableExtra("amount");
        ArrayList<String> itemCode = new ArrayList<String>();
        itemCode = (ArrayList<String>) getItemPieIntent.getSerializableExtra("itemCode");

        ArrayList<Entry> amount = new ArrayList<>();

        for (int i = 0 ; i < getamount.size() ; i++){
            amount.add(new Entry(getamount.get(i),i));
        }

        PieDataSet itemPieDataSet = new PieDataSet(amount,"");
        int[] itemPieColor = {Color.parseColor("#D32F2F"),
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

        itemPieDataSet.setColors(itemPieColor);
        itemPieDataSet.setValueTextSize(16f);
        itemPieDataSet.setValueTextColor(Color.WHITE);
        itemPieDataSet.setSliceSpace(0.5f);

        PieData itemPieData = new PieData(itemCode,itemPieDataSet);

        viewPieChart.setData(itemPieData);

        viewPieChart.setHoleColor(Color.TRANSPARENT);
        viewPieChart.setCenterText("Item Category");
        viewPieChart.setCenterTextColor(Color.BLACK);
        viewPieChart.setDragDecelerationFrictionCoef(0.90f);

        viewPieChart.animateY(2000,Easing.EasingOption.EaseInCirc);

        viewPieChart.setDescription("Item Category Wise Entry");
        viewPieChart.setDescriptionTextSize(18f);
    }
}

class ItemLineChart extends ItemCategoryReport{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_linechart);

        viewItemLineChart = findViewById(R.id.linechart);

        Intent getItemLineIntent = getIntent();

        ArrayList<Float> getItemLineamount = new ArrayList<Float>();
        getItemLineamount = (ArrayList<Float>) getItemLineIntent.getSerializableExtra("Item Line Amount");
        ArrayList<String> itemLineCode = new ArrayList<String>();
        itemLineCode = (ArrayList<String>) getItemLineIntent.getSerializableExtra("Item Line Code");

        ArrayList<Entry> ItemLineAmount = new ArrayList<>();

        for (int i = 0 ; i < getItemLineamount.size() ; i++){
            ItemLineAmount.add(new Entry(getItemLineamount.get(i),i));
        }

        LineDataSet ItemlineDataSet = new LineDataSet(ItemLineAmount,"");

        int[] itemLineColor = {Color.parseColor("#D32F2F"),
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

        ItemlineDataSet.setColors(itemLineColor);
        ItemlineDataSet.setValueTextSize(16f);
        ItemlineDataSet.setValueTextColor(Color.BLACK);

        LineData ItemlineData = new LineData(itemLineCode,ItemlineDataSet);

        viewItemLineChart.setData(ItemlineData);

        viewItemLineChart.setDescription("Item Category Wise Entry");

        viewItemLineChart.animateY(2000, Easing.EasingOption.EaseInCubic);
    }
}