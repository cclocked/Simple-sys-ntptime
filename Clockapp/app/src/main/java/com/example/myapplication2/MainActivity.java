package com.example.myapplication2;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.StrictMode;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.myapplication2.databinding.ActivityMainBinding;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

public class MainActivity extends AppCompatActivity{
    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
    Calendar calendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    TextView textview;
    long timeMillis;
    long diff;
    long returnTime;
    long actualTime;
    boolean switchCheck = true;
    final String TIME_SERVER = "0.se.pool.ntp.org";

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    public void SwitchControll(){
        Switch sb = new Switch(this);
        sb.setTextOff("OFF");
        sb.setTextOn("ON");
        sb.setChecked(true);

        Switch sw = (Switch) findViewById(R.id.Switch);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchCheck= true;
                } else switchCheck = false;
            }
        });
    }

public long sysTime(){
    calendar = Calendar.getInstance();
    timeMillis = calendar.getTimeInMillis();
    return timeMillis;
}
public long  ntpTime () throws IOException {
     NTPUDPClient timeClient =  new NTPUDPClient();
     timeClient.setDefaultTimeout(2000);
    InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
    TimeInfo timeInfo = timeClient.getTime(inetAddress);
    returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();
    return returnTime;
}

    public void getDiffTime() {
        try {
            long NtpTime = ntpTime();
            long sysTime2 = sysTime();
            diff = NtpTime - sysTime2;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

public void startApp1(){
    //since we can't acces the textview from outside the thread that created it
    // we use the handler to be able to show the time graphically on textview.
    Handler clockHandler = new Handler();
    Runnable clockRunner = () -> {
        textview.setText(dateFormat.format(actualTime));
    };
    Handler sysClockHandler = new Handler();
    Runnable sysClockRunner = () ->{
        textview.setText(dateFormat.format(sysTime()));
    };
            Thread mainThread1 = new Thread() {
                @Override
                public void run() {
                    while(true)
                    try {
                        // gets time from the ntp server from set sleep timeout.
                        // fetches diff time.
                        getDiffTime();
                        Thread.sleep(10000);
                        // System.out.println("we are connected");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            };
            Thread noConThread = new Thread() {
                @Override
                public void run() {
                    while(true)
                    try {
                        if (isNetworkConnected()&&switchCheck ) {
                            actualTime = sysTime() + diff;
                            System.out.println("systime : " + dateFormat.format(sysTime()));
                            clockHandler.post(clockRunner);
                            // textview.setText(df.format(actualTime));
                            // String time = df.format(actualTime)
                        } else if (!isNetworkConnected()&&!switchCheck)
                        {
                            dateFormat.format(sysTime());
                            System.out.println("this is systime: " + sysTime());
                            Thread.sleep(1000);
                           // clockHandler.post(clockRunner);
                            sysClockHandler.post(sysClockRunner);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            noConThread.start();
            mainThread1.start();

        }




            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
                // for those interested, this took me 3 hours to get right, there is a big difference
                // between "." and "=" :^)))))))
                textview=findViewById(R.id.text_view_date);
                setTitle("date and time");
                StrictMode.setThreadPolicy(policy);
                dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
                startApp1();

            }
        }
