package idm.test.com.internetdownloadmanager.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import java.util.Calendar;

import idm.test.com.internetdownloadmanager.InternetDownloadManagerActivity;

/**
 * Created by erfan on 11/15/2017.
 */

public class StartDownload extends BroadcastReceiver {
    InternetDownloadManagerActivity idmActivity = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("START_DOWNLOAD"));
//        idmActivity.testing();
//        Toast.makeText(context, "Download Started Automatically...!", Toast.LENGTH_LONG).show();
    }

    public void setAlarm(Context context, Calendar alarmTime) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, StartDownload.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 3333, i, PendingIntent.FLAG_ONE_SHOT);
        am.set(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pi);
    }

    public void setMainActivityHandler(InternetDownloadManagerActivity main){
        idmActivity = main;
    }
}