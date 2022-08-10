package com.cyberlight.pocketword.receiver;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.ui.MainActivity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@SuppressLint("UnspecifiedImmutableFlag")
public class StudyRemindReceiver extends BroadcastReceiver {
    public static final CharSequence STUDY_CHANNEL_NAME = "Study notifications";
    public static final String STUDY_CHANNEL_ID = "study_channel";
    public static final int STUDY_CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_HIGH;
    private static final int STUDY_NOTIFICATION_ID = 83;
    private static final int STUDY_NOTIFICATION_REQUEST_CODE = 883;
    private static final int STUDY_ALARM_REQUEST_CODE = 8881;
    private static final String STUDY_REMIND_ACTION = "study_remind_action";
    private static final String TAG = "StudyRemindReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // 发送事件通知
        NotificationChannel channel = new NotificationChannel(STUDY_CHANNEL_ID, STUDY_CHANNEL_NAME, STUDY_CHANNEL_IMPORTANCE);
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Intent ni = new Intent(context, MainActivity.class);
        PendingIntent npi = PendingIntent.getActivity(context,
                STUDY_NOTIFICATION_REQUEST_CODE,
                ni,
                PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new Notification.Builder(context, STUDY_CHANNEL_ID)
                .setContentTitle("Let's recite words!")
                .setContentText("Never give up!")
                .setContentIntent(npi)
                .setAutoCancel(true)
                .setOnlyAlertOnce(false)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(STUDY_NOTIFICATION_ID, notification);
    }

    /**
     * 更新提醒闹钟
     *
     * @param context 可用Context对象
     */
    public static void checkReminder(Context context) {
        PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
        boolean studyRemind = prefsMgr.isStudyRemind();
        if (studyRemind) {
            // 启动闹钟
            long triggerAtMillis;
            long studyRemindSecOfDay = prefsMgr.getStudyRemindTime();
            long curSecOfDay = LocalTime.now().toSecondOfDay();
            if (curSecOfDay < studyRemindSecOfDay) {
                LocalDateTime dateTime = LocalTime.ofSecondOfDay(studyRemindSecOfDay).atDate(LocalDate.now());
                triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            } else {
                LocalDateTime dateTime = LocalTime.ofSecondOfDay(studyRemindSecOfDay).atDate(LocalDate.now().plusDays(1));
                triggerAtMillis = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            }
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(context, StudyRemindReceiver.class);
            intent.setAction(STUDY_REMIND_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    STUDY_ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            Log.d(TAG,LocalDateTime.ofInstant(Instant.ofEpochMilli(triggerAtMillis),ZoneId.systemDefault()).toString());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis, 86400000, pendingIntent);
        } else {
            // 取消闹钟
            Intent intent = new Intent(context, StudyRemindReceiver.class);
            intent.setAction(STUDY_REMIND_ACTION);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    STUDY_ALARM_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_NO_CREATE);
            if (pendingIntent != null) {
                Log.d(TAG,"取消学习提醒");
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                pendingIntent.cancel();
            }
        }
    }
}
