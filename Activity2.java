package com.example.a5725finalproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

public class Activity2 extends AppCompatActivity {
    private Button rightBtn;
    private Button leftBtn;
    private Button forwardBtn;
    private Button backwardBtn;
    private Button updateBtn;
    private ImageButton refreshBtn;
    private String ipAddress = " ";
    private String port = " ";
    private TextView tv1;

    private EditText et1;
    private EditText et2;

    private String audioPath = "@raw/";
    private String forwardAudioPath = audioPath + "en_us_forward.mp3";
    private String backwardAudioPath = audioPath + "en_us_backward_1.mp3";
    private String leftAudioPath = audioPath + "en_us_left_1.mp3";
    private String rightAudioPath = audioPath + "en_us_right_1.mp3";

    MediaPlayer mpForward;
    MediaPlayer mpBackward;
    MediaPlayer mpLeft;
    MediaPlayer mpRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        ipAddress = "10.49.10.99";
        port = "5000";
        tv1 = (TextView) findViewById(R.id.connInfo);

        if (getIntent().hasExtra("ip")){
            ipAddress = getIntent().getExtras().getString("ip");
            port = getIntent().getExtras().getString("port");
            tv1.setText("Connection: " + ipAddress + ":" + port);
        }

        //String vPath = "android.resource://" + getPackageName() + "/raw/boids";
        //String vPath = "https://www.google.com";

        String vPath = "http://" + ipAddress + ":" + "8000";

        WebView webView = findViewById(R.id.wv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(vPath);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());


        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        MessageSender messageSender = new MessageSender(ipAddress, port);

        Context context = getApplicationContext();
        mpForward = MediaPlayer.create(context, R.raw.en_us_forward);
        mpBackward = MediaPlayer.create(context, R.raw.en_us_backward_1);
        mpLeft = MediaPlayer.create(context, R.raw.en_us_left_1);
        mpRight = MediaPlayer.create(context, R.raw.en_us_right_1);


        et1 = (EditText) findViewById(R.id.Et1);
        et2 = (EditText) findViewById(R.id.Et2);

        updateBtn = (Button) findViewById(R.id.btn3);
        forwardBtn = (Button) findViewById(R.id.button1);
        leftBtn = (Button) findViewById(R.id.button2);
        backwardBtn = (Button) findViewById(R.id.button4);
        rightBtn = (Button) findViewById(R.id.button3);
        refreshBtn = (ImageButton) findViewById(R.id.btn4);

        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.reload();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String inputText = et1.getText().toString();
                if (!inputText.equals(""))
                    ipAddress = inputText;
                inputText = et2.getText().toString();
                if (!inputText.equals(""))
                    port = inputText;
                messageSender.updateConnectionInfo(ipAddress, port);
                webView.reload();
                showInfoDialog("Connection info updated successfully!" + ipAddress + ":" + port);
                tv1.setText("Connection: " + ipAddress + ":" + port);
            }
        });

        forwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    messageSender.sendWithNoReply(1);
                    vibe.vibrate(10000);
                    mpForward.start();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    messageSender.sendWithNoReply(0);
                    vibe.cancel();
                }
                return false;
            }
        });

        leftBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    messageSender.sendWithNoReply(2);
                    vibe.vibrate(10000);
                    mpLeft.start();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    messageSender.sendWithNoReply(0);
                    vibe.cancel();
                }
                return false;
            }
        });
        rightBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    messageSender.sendWithNoReply(3);
                    vibe.vibrate(10000);
                    mpRight.start();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    messageSender.sendWithNoReply(0);
                    vibe.cancel();
                }
                return false;
            }
        });
        backwardBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    messageSender.sendWithNoReply(4);
                    vibe.vibrate(10000);
                    mpBackward.start();
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    messageSender.sendWithNoReply(0);
                    vibe.cancel();
                }
                return false;
            }
        });
    }

    private void showInfoDialog(String message){
        AlertDialog dialog = new AlertDialog.Builder(Activity2.this)
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