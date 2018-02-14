package com.example.shubhammundra.fromandto;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionHelper extends AppCompatActivity{

    String server_ip,db_name,db_username,db_password;
    EditText ServerIP;
    public String myIP = "192.168.2.45";
    Button IP;
    TextView demo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_server_ip);

        ServerIP = findViewById(R.id.et_serverip);

        demo = findViewById(R.id.tv_info);

        IP = findViewById(R.id.btn_getip);

        IP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myIP = ServerIP.getText().toString();
                demo.setText(myIP);
                Intent intentToReport = new Intent(ConnectionHelper.this,ReportOption.class);
                startActivity(intentToReport);
            }
        });
    }

    @SuppressLint("NewApi")
    public Connection connectionClass(){

        //server_ip = "192.168.2.45";
        server_ip = myIP;
        db_name = "LSDEMODATA";
        db_username = "sa";
        db_password = "mssql123";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        java.sql.Connection connection = null;
        String ConnectionURL = null;

        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnectionURL = "jdbc:jtds:sqlserver://" + server_ip +";databaseName="+ db_name + ";user=" + db_username+ ";password=" + db_password + ";";
            connection = DriverManager.getConnection(ConnectionURL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}