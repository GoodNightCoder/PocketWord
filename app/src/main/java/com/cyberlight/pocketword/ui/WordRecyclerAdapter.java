package com.cyberlight.pocketword.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.R;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_WORD = 0;
    private static final int TYPE_NO_WORD = 1;

    private final Context mContext;
    private final List<CollectWord> mWords;

    private static class WordViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordTv;
        private final TextView meanTv;
        private final ImageView knownIv;
        private boolean showMean;
        private final Context context;

        public WordViewHolder(View v) {
            super(v);
            context = v.getContext();
            wordTv = v.findViewById(R.id.rv_word_tv);
            meanTv = v.findViewById(R.id.rv_mean_tv);
            knownIv = v.findViewById(R.id.rv_known_iv);
        }

        public void setShowMean(boolean showMean) {
            this.showMean = showMean;
            if (showMean) {
                meanTv.setForeground(null);
            } else {
                TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.wordForegroundColor});
                try {
                    meanTv.setForeground(a.getDrawable(0));
                } finally {
                    a.recycle();
                }
            }
        }

        public boolean isShowMean() {
            return showMean;
        }
    }

    private static class NoWordViewHolder extends RecyclerView.ViewHolder {
        public NoWordViewHolder(View v) {
            super(v);
        }
    }

    public WordRecyclerAdapter(Context context, List<CollectWord> words) {
        mContext = context;
        mWords = words;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_WORD) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_word, parent, false);
            return new WordViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_no_word, parent, false);
            return new NoWordViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WordViewHolder) {
            WordViewHolder wordViewHolder = (WordViewHolder) holder;
            CollectWord word = mWords.get(position);
            wordViewHolder.wordTv.setText(word.getWordStr());
            wordViewHolder.meanTv.setText(word.getMean());
            wordViewHolder.knownIv.setVisibility(word.isKnown() ? View.VISIBLE : View.INVISIBLE);
            wordViewHolder.setShowMean(false);// 初始隐藏释义
            wordViewHolder.wordTv.setOnClickListener(v -> {
                String path;
                if (!TextUtils.isEmpty(word.getAudio())) {
                    File temp = new File(mContext.getFilesDir(), word.getAudio());
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
            });
            wordViewHolder.meanTv.setOnClickListener(v -> {
                // 显示、隐藏释义
                wordViewHolder.setShowMean(!wordViewHolder.isShowMean());
            });
        }
    }

    @Override
    public int getItemCount() {
        return mWords.size() > 0 ? mWords.size() : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return mWords.size() > 0 ? TYPE_WORD : TYPE_NO_WORD;
    }

}