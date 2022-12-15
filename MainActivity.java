package com.example.a5725finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private int i = 0;
    private String ipAddress = "10.49.10.99";
    private String port = "5000";
    private EditText et1;
    private EditText et2;
    private Intent startIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TextView tv1;
        Button btn1;
        Button btn2;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        et1 = (EditText) findViewById(R.id.Et1);
        et2 = (EditText) findViewById(R.id.Et2);
        et1.setHint(ipAddress);
        et2.setHint(port);

        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        startIntent = new Intent(getApplicationContext(), Activity2.class);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(startIntent);
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = et1.getText().toString();
                if (!inputText.equals(""))
                    ipAddress = inputText;
                inputText = et2.getText().toString();
                if (!inputText.equals(""))
                    port = inputText;

                startIntent.putExtra("ip", ipAddress);
                startIntent.putExtra("port", port);
                showInfoDialog("Connection info inputed! " + ipAddress + ":" + port);
            }
        });
    }

    private void showInfoDialog(String message){
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                .setTitle("Success")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create();
        dialog.show();
    }
}