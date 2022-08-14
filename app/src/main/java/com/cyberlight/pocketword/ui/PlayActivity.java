package com.cyberlight.pocketword.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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

import com.cyberlight.pocketword.data.db.entity.WordBookWord;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Record;
import com.cyberlight.pocketword.data.db.entity.WordBook;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class PlayActivity extends AppCompatActivity {

    private static final String TAG = "PlayActivity";

    private static final long ANIM_DURATION = 150;
    private static final long MIN_DELAY = 1000;
    private static final long MAX_DELAY = 15000;
    private static final long STEP = 500;
    private static final long DEFAULT_DELAY = 3000;
    private DataRepository mRepository;

    private boolean mPlaySound = true;
    private boolean mShowWord = true;
    private boolean mShowMean = true;
    private boolean mSkipKnown;
    // 播放延迟毫秒数
    private long mDelayMillis;

    // 记录学习开始时间，结束时将学习时长保存
    private long mStartTime;
    // 已学习单词个数
    private int mLearnedCount = -1;
    // 当前日期，用于判断是否跨天
    private LocalDate mToday;
    // 使用的词书，结束时更新该词书的学习进度
    private WordBook mUsingWordBook;
    private List<CollectWord> mPlayWords;
    private List<Integer> mUnknownIndices;
    private int mCurUnknownProgress;
    private int mLastUnknownProgress;
    private int mCurProgress;
    private int mLastProgress;

    private TextView mWordTv;
    private TextView mMeanTv;
    private TextView mAccentTv;
    private TextView mInfoTv;
    private TextView mKnownTv;
    private final Handler mHandler = new Handler();
    private final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            CollectWord word = mPlayWords.get(mCurProgress);
            // 朗读单词
            if (mPlaySound) {
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
            // 显示单词信息
            fadeOutAndIn(word.getWordStr(),
                    word.getMean(),
                    word.getAccent(),
                    word.isKnown(),
                    ANIM_DURATION);
            // 更新学习进度信息
            mInfoTv.setText(getString(R.string.play_info_tv_text,
                    mSkipKnown ? mCurUnknownProgress + 1 : mCurProgress + 1,
                    mSkipKnown ? mUnknownIndices.size() : mPlayWords.size()));
            mLearnedCount++;
            // 准备下一个单词
            progressNext();
            // 检查跨天
            checkToday();
            mHandler.postDelayed(this, mDelayMillis);
        }
    };

    private void fadeOutAndIn(String wordStr, String mean, String accent, boolean known, long animationDuration) {
        mWordTv.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mWordTv.setText(wordStr);
                        mWordTv.animate()
                                .alpha(1f)
                                .setDuration(animationDuration);
                    }
                });

        mMeanTv.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mMeanTv.setText(mean);
                        mMeanTv.animate()
                                .alpha(1f)
                                .setDuration(animationDuration);
                    }
                });

        mAccentTv.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mAccentTv.setText(accent);
                        mAccentTv.animate()
                                .alpha(1f)
                                .setDuration(animationDuration);
                    }
                });

        // 动画期间用户点击会导致状态异常
        mKnownTv.setClickable(false);
        mKnownTv.animate()
                .alpha(0f)
                .setDuration(animationDuration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mKnownTv.setText(known
                                ? getString(R.string.play_known_tv_known)
                                : getString(R.string.play_known_tv_unknown));
                        mKnownTv.animate()
                                .alpha(1f)
                                .setDuration(animationDuration)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        mKnownTv.setClickable(true);
                                    }
                                });
                    }
                });
    }

    private void progressNext() {
        mLastProgress = mCurProgress;
        mLastUnknownProgress = mCurUnknownProgress;
        if (mSkipKnown) {
            mCurUnknownProgress++;
            if (mCurUnknownProgress >= mUnknownIndices.size()) mCurUnknownProgress = 0;
            mCurProgress = mUnknownIndices.get(mCurUnknownProgress);
        } else {
            mCurProgress++;
            // 循环播放，保证不越界
            if (mCurProgress >= mPlayWords.size()) mCurProgress = 0;
        }
    }

    private void checkToday() {
        LocalDate today = LocalDate.now();
        if (!today.equals(mToday)) {
            // 发生跨天，将当前学习记录保存并重置学习记录
            long start = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long durationToAdd = start - mStartTime;
            mStartTime = start;
            int countToAdd = mLearnedCount;
            mLearnedCount = 0;
            LocalDate dateToUpdateRecord = mToday;
            mToday = today;
            new Thread(() -> {
                Record record = mRepository.getRecordByDateSync(dateToUpdateRecord);
                if (record != null) {
                    record.setStudyDuration(record.getStudyDuration() + durationToAdd);
                    record.setStudyCount(record.getStudyCount() + countToAdd);
                    mRepository.updateRecord(record);
                } else {
                    record = new Record(dateToUpdateRecord, countToAdd, durationToAdd);
                    mRepository.insertRecord(record);
                }
            }).start();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mWordTv = findViewById(R.id.play_word_tv);
        mMeanTv = findViewById(R.id.play_mean_tv);
        mAccentTv = findViewById(R.id.play_accent_tv);
        mInfoTv = findViewById(R.id.play_info_tv);
        mKnownTv = findViewById(R.id.play_known_tv);
        ImageView backIv = findViewById(R.id.play_back_iv);
        TextView delayTv = findViewById(R.id.play_delay_tv);
        SeekBar seekBar = findViewById(R.id.seek_bar);
        SwitchCompat soundSwitch = findViewById(R.id.play_sound_switch);
        SwitchCompat wordSwitch = findViewById(R.id.play_word_switch);
        SwitchCompat meanSwitch = findViewById(R.id.play_mean_switch);
        LinearLayout controlPanel = findViewById(R.id.play_control_panel);
        ImageView settingsIv = findViewById(R.id.play_settings_iv);
        mRepository = DataRepository.getInstance(this);

        backIv.setOnClickListener(v -> finish());

        mWordTv.setVisibility(mShowWord ? View.VISIBLE : View.INVISIBLE);
        mMeanTv.setVisibility(mShowMean ? View.VISIBLE : View.INVISIBLE);

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

        mKnownTv.setOnClickListener(v -> {
            CollectWord curWord = mPlayWords.get(mLastProgress);
            if (curWord.isKnown()) return;
            // 更新mPlayWords中该单词的known
            curWord.setKnown(true);
            mKnownTv.setText(getString(R.string.play_known_tv_known));
            // 更新数据库中该单词的known
            new Thread(() -> {
                WordBookWord wordBookWord = mRepository.getWordBookWordSync(
                        curWord.getWordId(), mUsingWordBook.getWordBookId());
                wordBookWord.setKnown(true);
                mRepository.updateWordBookWordSync(wordBookWord);
            }).start();
            if (mSkipKnown) {
                // 从未掌握单词下标表中删除当前单词
                mCurUnknownProgress = mLastUnknownProgress;
                mUnknownIndices.remove(mCurUnknownProgress);
                // 检查
                if (mUnknownIndices.size() == 0) {
                    finish();
                    return;
                }
                // 修正，防止越界
                if (mCurUnknownProgress >= mUnknownIndices.size()) {
                    mCurUnknownProgress = 0;
                }
                mCurProgress = mUnknownIndices.get(mCurUnknownProgress);
                // 直接进入下一个单词
                mHandler.removeCallbacks(mRunnable);
                mHandler.post(mRunnable);
            }
        });

        soundSwitch.setChecked(mPlaySound);
        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> mPlaySound = isChecked);

        wordSwitch.setChecked(mShowWord);
        wordSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowWord = isChecked;
            mWordTv.setVisibility(mShowWord ? View.VISIBLE : View.INVISIBLE);
        });

        meanSwitch.setChecked(mShowMean);
        meanSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mShowMean = isChecked;
            mMeanTv.setVisibility(mShowMean ? View.VISIBLE : View.INVISIBLE);
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
        // 加载数据，加载好之后启动mRunnable任务
        new Thread(() -> {
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(this);
            // 获取并检查使用中词书
            long usingWordBookId = prefsMgr.getUsingWordBookId();
            mUsingWordBook = mRepository.getWordBookByIdSync(usingWordBookId);
            if (mUsingWordBook == null) {
                finish();
                return;
            }
            // 获取并检查单词表
            mPlayWords = mRepository.getCollectWordsSync(mUsingWordBook.getWordBookId());
            if (mPlayWords == null || mPlayWords.size() == 0) {
                finish();
                return;
            }
            // 获取是否跳过已掌握
            mSkipKnown = prefsMgr.isSkipKnown();
            // 如果跳过已掌握，则需构建mUnknownIndices表，
            // 指示所有未掌握单词在mPlayWords里的下标
            if (mSkipKnown) {
                mUnknownIndices = new ArrayList<>();
                for (int i = 0; i < mPlayWords.size(); i++) {
                    if (!mPlayWords.get(i).isKnown()) {
                        mUnknownIndices.add(i);
                    }
                }
                // 检查未掌握下标表
                if (mUnknownIndices.size() == 0) {
                    finish();
                    return;
                }
            }
            // 获取词书学习进度
            mCurProgress = mUsingWordBook.getLearningProgress();
            // 修正学习进度（由于单词可能会被用户删除，数据库中原有的进度可能会越界；
            // 另外用户如果设置了跳过已掌握，就必须修正进度并计算mCurUnknownProgress）
            if (mCurProgress >= mPlayWords.size()) mCurProgress = 0;
            if (mSkipKnown) {
                for (mCurUnknownProgress = 0; mCurUnknownProgress < mUnknownIndices.size(); mCurUnknownProgress++) {
                    int p = mUnknownIndices.get(mCurUnknownProgress);
                    if (mCurProgress <= p) {
                        mCurProgress = p;
                        break;
                    }
                }
                if (mCurUnknownProgress >= mUnknownIndices.size()) {
                    mCurUnknownProgress = 0;
                    mCurProgress = mUnknownIndices.get(mCurUnknownProgress);
                }
            }
            mToday = LocalDate.now();
            mStartTime = System.currentTimeMillis();
            mLearnedCount = -1;
            mHandler.post(mRunnable);
        }).start();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mRunnable);
        // mLearnedCount > 0说明进行了学习
        if (mLearnedCount > 0) {
            // 保存学习进度
            mUsingWordBook.setLearningProgress(mLastProgress);
            mRepository.updateWordBook(mUsingWordBook);
            // 保存学习记录
            long durationToAdd = System.currentTimeMillis() - mStartTime;
            int countToAdd = mLearnedCount;
            new Thread(() -> {
                Record record = mRepository.getRecordByDateSync(mToday);
                if (record != null) {
                    record.setStudyDuration(record.getStudyDuration() + durationToAdd);
                    record.setStudyCount(record.getStudyCount() + countToAdd);
                    mRepository.updateRecord(record);
                } else {
                    record = new Record(mToday, countToAdd, durationToAdd);
                    mRepository.insertRecord(record);
                }
            }).start();
        }
        super.onDestroy();
    }

}