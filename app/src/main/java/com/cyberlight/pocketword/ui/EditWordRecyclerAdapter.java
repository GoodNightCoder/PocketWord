package com.cyberlight.pocketword.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.model.CollectWord;
import com.cyberlight.pocketword.widget.ToggleRadioButton;

import java.util.List;

public class EditWordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_WORD = 0;

    private final List<CollectWord> mWords;
    private final List<Boolean> mSelectStates;
    private final OnSelectListener mOnSelectListener;

    private static class WordViewHolder extends RecyclerView.ViewHolder {
        private final ToggleRadioButton selectTrbtn;
        private final TextView wordTv;
        private final TextView meanTv;
        private final ImageView knownIv;

        public WordViewHolder(View v) {
            super(v);
            selectTrbtn = v.findViewById(R.id.rv_select_trbtn);
            wordTv = v.findViewById(R.id.rv_word_tv);
            meanTv = v.findViewById(R.id.rv_mean_tv);
            knownIv = v.findViewById(R.id.rv_known_iv);
        }
    }

    public EditWordRecyclerAdapter(List<CollectWord> words, List<Boolean> selectStates, OnSelectListener onSelectListener) {
        mWords = words;
        mSelectStates = selectStates;
        mOnSelectListener = onSelectListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_edit_word, parent, false);
        return new WordViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof WordViewHolder) {
            WordViewHolder wordViewHolder = (WordViewHolder) holder;
            CollectWord word = mWords.get(holder.getAdapterPosition());
            wordViewHolder.selectTrbtn.setChecked(mSelectStates.get(holder.getAdapterPosition()));
            wordViewHolder.wordTv.setText(word.getWordStr());
            wordViewHolder.meanTv.setText(word.getMean());
            wordViewHolder.knownIv.setVisibility(word.isKnown() ? View.VISIBLE : View.INVISIBLE);
            wordViewHolder.selectTrbtn.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mOnSelectListener != null) {
                    mOnSelectListener.onSelect(holder.getAdapterPosition(), isChecked);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mWords.size();
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_WORD;
    }

    public interface OnSelectListener {
        void onSelect(int position, boolean isChecked);
    }
}