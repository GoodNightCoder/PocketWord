package com.cyberlight.pocketword.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.data.db.entity.WordBook;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.ui.MainActivity;
import com.cyberlight.pocketword.util.DownloadUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DownloadAudioService extends Service {
    private static final String TAG = "DownloadAudioService";

    private static final int DOWNLOAD_NOTIFICATION_REQUEST_CODE = 66;
    private static final String DOWNLOAD_CHANNEL_ID = "download_audio_channel";
    private static final String DOWNLOAD_CHANNEL_NAME = "Download Audio";
    private static final int DOWNLOAD_NOTIFICATION_ID = 77;

    private static final String TARGET_TYPE = "mp3";
    private static final String AUDIO_HOST = "http://dict.youdao.com/dictvoice?audio=";

    @Override
    public void onCreate() {
        // 判断网络是否连接
        if (!DownloadUtil.isOnline(this)) {
            Toast.makeText(this, R.string.network_unavailable_toast, Toast.LENGTH_SHORT).show();
            stopSelf();
            return;
        }

        // 创建通知渠道
        NotificationChannel channel = new NotificationChannel(DOWNLOAD_CHANNEL_ID, DOWNLOAD_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // 设置为前台服务
        Intent ni = new Intent(this, MainActivity.class);
        PendingIntent npi = PendingIntent.getActivity(this,
                DOWNLOAD_NOTIFICATION_REQUEST_CODE,
                ni,
                PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder = new Notification.Builder(this, DOWNLOAD_CHANNEL_ID)
                .setContentTitle(getString(R.string.download_title))
                .setContentText(getString(R.string.download_ready))
                .setContentIntent(npi)
                .setSmallIcon(R.drawable.ic_baseline_arrow_downward_24)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setProgress(100, 0, true);
        startForeground(DOWNLOAD_NOTIFICATION_ID, builder.build());

        // 开始下载
        new Thread(() -> {
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(this);
            long usingWordBookId = prefsMgr.getUsingWordBookId();
            DataRepository repository = DataRepository.getInstance(this);
            WordBook usingWordBook = repository.getWordBookByIdSync(usingWordBookId);
            if (usingWordBook == null) {
                stopSelf();
                return;
            }
            List<Word> words = repository.getWordsFromBookSync(usingWordBook.getWordBookId());
            if (words == null || words.size() == 0) {
                stopSelf();
                return;
            }
            // 遍历单词并下载
            for (int i = 0; i < words.size(); i++) {
                Word word = words.get(i);
                float progress = (float) (i + 1) * 100 / words.size();
                builder.setContentText(String.format("%.1f", progress) + "%");
                builder.setProgress(100, (int) progress, false);
                notificationManager.notify(DOWNLOAD_NOTIFICATION_ID, builder.build());
                String filename = word.getWordStr() + "." + TARGET_TYPE;
                File temp = new File(getFilesDir(), filename);
                if (!temp.exists()) {
                    try {
                        if (temp.createNewFile()) {
                            while (!DownloadUtil.download(DownloadAudioService.this,
                                    AUDIO_HOST + word.getWordStr(), filename)) {
                                // 下载失败，一段时间后重试
                                Thread.sleep(5000);
                            }
                            word.setAudio(filename);
                            repository.updateWord(word);
                            Log.d(TAG, word.getWordStr() + "下载成功");
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, word.getWordStr() + "已下载");
                }
            }
            stopSelf();
        }).start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}