package com.thalitamoia.appbeberagua;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewGroupCompat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private Button btn_notify;
    private EditText edit_intervalo;
    private TimePicker time_pick;
    private int hour;
    private int minutes;
    private int intervalo;
    private boolean actived = false;
    private SharedPreferences preferences;
    private Toolbar toolbar;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_notify = findViewById(R.id.btn_notify);
        edit_intervalo = findViewById(R.id.edit_intervalo);
        time_pick = findViewById(R.id.time_pick);



        time_pick.setIs24HourView(true);
        preferences = getSharedPreferences("db", Context.MODE_PRIVATE);

        actived = preferences.getBoolean("actived",false);

        if (actived){
            btn_notify.setText(R.string.pause);
            int color = ContextCompat.getColor(this, android.R.color.black);
            btn_notify.setBackgroundColor(color);

          int intervalo = preferences.getInt("Intervalo",0);
            int hour = preferences.getInt("Hora",time_pick.getCurrentHour());
            int minutes = preferences.getInt("Minutos",time_pick.getCurrentMinute());

            edit_intervalo.setText(String.valueOf(intervalo));
            time_pick.setCurrentHour(hour);
            time_pick.setCurrentMinute(minutes);

        }
    }

    public void notifyClick(View view){
       String minutos = edit_intervalo.getText().toString();
       if (minutos.isEmpty()){
           Toast.makeText(this,R.string.erro_msg,Toast.LENGTH_SHORT).show();
           return;
       }

        hour = time_pick.getCurrentHour();
        minutes = time_pick.getCurrentMinute();
        intervalo = Integer.parseInt(minutos);
        if (!actived) {

            btn_notify.setText(R.string.pause);
            int color = ContextCompat.getColor(this, android.R.color.black);
            btn_notify.setBackgroundColor(color);
            actived = true;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("actived",true);
            editor.putInt("Intervalo",intervalo);
            editor.putInt("Minutos", minutes);
            editor.putInt("Hora", hour);
            editor.apply();

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.HOUR_OF_DAY,hour);
            calendar.set(calendar.MINUTE,minutes);


            Intent notificationIntent = new Intent(MainActivity.this,NotificationPublisher.class);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION_ID,1);
            notificationIntent.putExtra(NotificationPublisher.KEY_NOTIFICATION,"Hora de Beber água!");
            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);


            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), intervalo * 60 * 1000,broadcast);


        } else {

            btn_notify.setText(R.string.notify);
            int color = ContextCompat.getColor(this,R.color.colorAccent);
            btn_notify.setBackgroundColor(color);

            actived = false;
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("actived",false);
            editor.remove("Intervalo");
            editor.remove("Minutos");
            editor.remove("Hora");
            editor.apply();

            Intent notificationIntent = new Intent(MainActivity.this,NotificationPublisher.class);
            PendingIntent broadcast = PendingIntent.getBroadcast(MainActivity.this, 0, notificationIntent,0);
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.cancel(broadcast);

        }


    }
}