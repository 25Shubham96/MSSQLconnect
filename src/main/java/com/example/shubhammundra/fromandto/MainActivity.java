package com.example.shubhammundra.fromandto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DatePicker Start ;
    DatePicker End ;

    TextView tv_start1 ;
    TextView tv_end1 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button getTable = findViewById(R.id.btn_getTable);
        Button itemTable = findViewById(R.id.btn_itemTable);

        Start = findViewById(R.id.dp_start);
        End = findViewById(R.id.dp_end);

        tv_start1 = findViewById(R.id.tv_start);
        tv_end1 = findViewById(R.id.tv_end);

        getTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_start1.setText(Start.getYear()+"-"+(Start.getMonth() + 1)+"-"+Start.getDayOfMonth());
                tv_end1.setText(End.getYear()+"-"+(End.getMonth()+1)+"-"+End.getDayOfMonth());

                Intent intent = new Intent(MainActivity.this,DisplayDate.class);
                intent.putExtra("Start Date",tv_start1.getText().toString());
                intent.putExtra("End Date",tv_end1.getText().toString());
                startActivity(intent);
            }
        });

        itemTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_start1.setText(Start.getYear()+"-"+(Start.getMonth() + 1)+"-"+Start.getDayOfMonth());
                tv_end1.setText(End.getYear()+"-"+(End.getMonth()+1)+"-"+End.getDayOfMonth());

                Intent itemIntent = new Intent(MainActivity.this,ItemCategoryReport.class);
                itemIntent.putExtra("New Start Date",tv_start1.getText().toString());
                itemIntent.putExtra("New End Date",tv_end1.getText().toString());
                startActivity(itemIntent);
            }
        });
    }
}