package com.cyberlight.pocketword.ui;

import android.annotation.SuppressLint;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Record;
import com.cyberlight.pocketword.data.db.entity.WordBook;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class PlayActivity extends AppCompatActivity {

    private static final long MIN_DELAY = 1000;
    private static final long MAX_DELAY = 15000;
    private static final long STEP = 500;
    private static final long DEFAULT_DELAY = 3000;

    private boolean mSound = true;
    private boolean mSkip = true;
    private boolean mShowWord = true;
    private boolean mShowMean = true;

    private DataRepository mRepository;

    // 播放延迟毫秒数
    private long mDelayMillis;
    // 记录学习开始时间，结束时将学习时长保存
    private long mStartTime;
    // 已学习单词个数
    private int mLearnedCount;
    // 当天已有的学习记录，学习结束时增加已有记录
    // 的学习时长、背词个数
    private Record mTodayRecord;
    // 使用的词书，结束时更新该词书的学习进度
    private WordBook mUsingWordBook;
    private List<CollectWord> mWordsList;
    private int mCurIndex = 0;

    private TextView mWordTv;
    private TextView mMeanTv;
    private TextView mAccentTv;
    private TextView mInfoTv;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // 循环播放，并保证单词删除后不越界
            if (mCurIndex >= mWordsList.size()) mCurIndex = 0;
            CollectWord word = mWordsList.get(mCurIndex);
            // 跳过已掌握词汇
            while (mSkip && word.isKnown()) {
                mCurIndex++;
                word = mWordsList.get(mCurIndex);
            }
            // 朗读单词
            if (mSound) {
                String path;
                if (!TextUtils.isEmpty(word.getAudio())) {
                    File temp = new File(getFilesDir(), word.getAudio());
                    path = temp.getPath();
                } else {
                    path = "http://dict.youdao.com/dictvoice?audio=" + word.getWordStr();
                }
                MediaPlayer mediaPlayer = new MediaPlayer();
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.setOnPreparedListener(mp -> {
                        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                        mediaPlayer.start();
                    });
                    mediaPlayer.prepareAsync(); // might take long! (for buffering, etc)
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // 显示单词、释义、音标
            mWordTv.setText(word.getWordStr());
            mMeanTv.setText(word.getMean());
            mAccentTv.setText(word.getAccent());
            // 更新学习进度、已学词数
            mInfoTv.setText(getString(R.string.play_info_tv_text, mCurIndex + 1, mWordsList.size()));
            mUsingWordBook.setLearningProgress(mCurIndex);
            mLearnedCount++;
            // 准备下一个单词
            mCurIndex++;
            mHandler.postDelayed(this, mDelayMillis);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mWordTv = findViewById(R.id.play_word_tv);
        mMeanTv = findViewById(R.id.play_mean_tv);
        mAccentTv = findViewById(R.id.play_accent_tv);
        mInfoTv = findViewById(R.id.play_info_tv);
        ImageView backIv = findViewById(R.id.play_back_iv);
        TextView delayTv = findViewById(R.id.play_delay_tv);
        SeekBar seekBar = findViewById(R.id.seek_bar);
        SwitchCompat soundSwitch = findViewById(R.id.play_sound_switch);
        SwitchCompat skipSwitch = findViewById(R.id.play_skip_switch);
        SwitchCompat wordSwitch = findViewById(R.id.play_word_switch);
        SwitchCompat meanSwitch = findViewById(R.id.play_mean_switch);
        LinearLayout controlPanel = findViewById(R.id.play_control_panel);
        ImageView settingsIv = findViewById(R.id.play_settings_iv);
        mRepository = DataRepository.getInstance(this);

        backIv.setOnClickListener(v -> finish());

        if (mShowWord) {
            mWordTv.setVisibility(View.VISIBLE);
        } else {
            mWordTv.setVisibility(View.INVISIBLE);
        }
        if (mShowMean) {
            mMeanTv.setVisibility(View.VISIBLE);
        } else {
            mMeanTv.setVisibility(View.INVISIBLE);
        }

        seekBar.setMax((int) ((MAX_DELAY - MIN_DELAY) / STEP));
        int defaultProgress = (int) ((DEFAULT_DELAY - MIN_DELAY) / STEP);
        seekBar.setProgress(defaultProgress);
        mDelayMillis = MIN_DELAY + defaultProgress * STEP;
        delayTv.setText(getString(R.string.play_delay_tv_text, String.valueOf((float) mDelayMillis / 1000)));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDelayMillis = MIN_DELAY + progress * STEP;
                delayTv.setText(getString(R.string.play_delay_tv_text, String.valueOf((float) mDelayMillis / 1000)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        soundSwitch.setChecked(mSound);
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSound = isChecked);

        skipSwitch.setChecked(mSkip);
        skipSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mSkip = isChecked);

        wordSwitch.setChecked(mShowWord);
        wordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowWord = isChecked;
            if (mShowWord) {
                mWordTv.setVisibility(View.VISIBLE);
            } else {
                mWordTv.setVisibility(View.INVISIBLE);
            }
        });

        meanSwitch.setChecked(mShowMean);
        meanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowMean = isChecked;
            if (mShowMean) {
                mMeanTv.setVisibility(View.VISIBLE);
            } else {
                mMeanTv.setVisibility(View.INVISIBLE);
            }
        });

        settingsIv.setOnClickListener(new View.OnClickListener() {
            boolean hideSettings = true;

            @Override
            public void onClick(View v) {
                hideSettings = !hideSettings;
                if (hideSettings) {
                    TypedArray a = obtainStyledAttributes(new int[]{com.google.android.material.R.attr.colorControlNormal});
                    try {
                        settingsIv.setColorFilter(a.getColor(0, Color.RED));
                    } finally {
                        a.recycle();
                    }
                    controlPanel.setVisibility(View.INVISIBLE);
                } else {
                    TypedArray a = obtainStyledAttributes(new int[]{androidx.appcompat.R.attr.colorPrimary});
                    try {
                        settingsIv.setColorFilter(a.getColor(0, Color.RED));
                    } finally {
                        a.recycle();
                    }
                    controlPanel.setVisibility(View.VISIBLE);
                }
            }
        });
        new Thread(() -> {
            mUsingWordBook = mRepository.getUsingWordBookSync();
            if (mUsingWordBook == null) {
                finish();
                return;
            }
            mTodayRecord = mRepository.getRecordByDateSync(LocalDate.now());
            mWordsList = mRepository.getCollectWordsFromWordBookSync(mUsingWordBook.getWordBookId());
            if (mWordsList.size() > 0) {
                // 初始化学习任务
                mCurIndex = mUsingWordBook.getLearningProgress();
                mHandler.post(mRunnable);
                mStartTime = System.currentTimeMillis();
                mLearnedCount = -1;
            } else {
                finish();
            }
        }).start();

    }


    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        // mUsingWordBook非空说明成功进行了学习
        if (mUsingWordBook != null) {
            mRepository.updateWordBook(mUsingWordBook);
            if (mTodayRecord != null) {
                long newDuration = mTodayRecord.getStudyDuration() + System.currentTimeMillis() - mStartTime;
                int newLearnedCount = mTodayRecord.getStudyCount() + mLearnedCount;
                mTodayRecord.setStudyDuration(newDuration);
                mTodayRecord.setStudyCount(newLearnedCount);
                mRepository.insertRecord(mTodayRecord);
            } else {
                // 今天不存在已有记录，创建记录
                Record record = new Record(LocalDate.now(), mLearnedCount, System.currentTimeMillis() - mStartTime);
                mRepository.insertRecord(record);
            }
        }
        super.onDestroy();
    }

}