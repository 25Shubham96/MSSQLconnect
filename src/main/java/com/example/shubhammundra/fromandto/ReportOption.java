package com.example.shubhammundra.fromandto;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ReportOption extends AppCompatActivity {

    Button StoreBTN,CategoryBTN,TimeBTN,PaymentsBTN,StaffBTN;

    @SuppressLint("CutPasteId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_option);

        StoreBTN = findViewById(R.id.btn_storeReport);
        CategoryBTN = findViewById(R.id.btn_categoryReport);
        TimeBTN = findViewById(R.id.btn_timeReport);
        PaymentsBTN = findViewById(R.id.btn_paymentsReport);
        StaffBTN = findViewById(R.id.btn_staffReport);

        StoreBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent storeintent = new Intent(ReportOption.this,DisplayDate.class);
                startActivity(storeintent);
            }
        });

        CategoryBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent categoryintent = new Intent(ReportOption.this,ItemCategoryReport.class);
                startActivity(categoryintent);
            }
        });

        TimeBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent timeintent = new Intent(ReportOption.this,TimeReport.class);
                startActivity(timeintent);
            }
        });

        PaymentsBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent paymentsintent = new Intent(ReportOption.this,PaymentsReport.class);
                startActivity(paymentsintent);
            }
        });

        StaffBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent staffintent = new Intent(ReportOption.this,StaffReport.class);
                startActivity(staffintent);
            }
        });
    }
}
