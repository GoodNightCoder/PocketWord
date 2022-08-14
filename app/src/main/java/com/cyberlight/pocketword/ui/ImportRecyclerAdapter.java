package com.cyberlight.pocketword.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.util.TextWatcherAdapter;

import java.util.List;

public class ImportRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;
    private static final String TAG = "ImportRecyclerAdapter";

    private final Context mContext;
    private final List<Word> mImportWordList;
    private OnAddClickListener mOnAddClickListener;

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView mWordTv;
        private final EditText mMeanEt;
        private final EditText mAccentEt;
        private final ImageView mDeleteIv;
        private final int mOriginalColor;

        public ItemViewHolder(View v) {
            super(v);
            mWordTv = v.findViewById(R.id.rv_import_word_tv);
            mMeanEt = v.findViewById(R.id.rv_import_mean_et);
            mAccentEt = v.findViewById(R.id.rv_import_accent_et);
            mDeleteIv = v.findViewById(R.id.rv_import_delete_iv);
            mOriginalColor = mWordTv.getCurrentTextColor();
        }

    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mFooterLayout;

        public FooterViewHolder(View v) {
            super(v);
            mFooterLayout = v.findViewById(R.id.rv_import_footer_layout);
        }
    }

    public ImportRecyclerAdapter(Context context, List<Word> importWordList) {
        mContext = context;
        mImportWordList = importWordList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_import_item, parent, false);
            return new ItemViewHolder(v);
        } else {// FOOTER
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_import_footer, parent, false);
            return new FooterViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
//        Log.d(TAG, "onBindViewHolder:" + position);
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.mDeleteIv.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition();
                // 用户快速点击删除item按钮时，getAdapterPosition()
                // 可能来不及返回最新的position，而是返回NO_POSITION
                if (pos != RecyclerView.NO_POSITION) {
                    Log.d(TAG, String.valueOf(pos));
                    Log.d(TAG, mImportWordList.get(pos).getWordStr());
                    mImportWordList.remove(pos);
                    notifyItemRemoved(pos);
                }
            });
            Word word = mImportWordList.get(holder.getAdapterPosition());
            itemViewHolder.mWordTv.setText(word.getWordStr());
            itemViewHolder.mMeanEt.setText(word.getMean());
            itemViewHolder.mAccentEt.setText(word.getAccent());
            // 初始化颜色
            if (word.getMean() == null || TextUtils.isEmpty(word.getMean().trim())) {
                itemViewHolder.mWordTv.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            } else if (word.getWordId() != -1) {
                itemViewHolder.mWordTv.setTextColor(ContextCompat.getColor(mContext, R.color.green));
            }
            itemViewHolder.mMeanEt.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    Word w = mImportWordList.get(holder.getAdapterPosition());
                    w.setMean(s.toString());
                    if (word.getMean() == null || TextUtils.isEmpty(w.getMean().trim())) {
                        itemViewHolder.mWordTv.setTextColor(ContextCompat.getColor(mContext, R.color.red));
                    } else if (w.getWordId() != -1) {
                        itemViewHolder.mWordTv.setTextColor(ContextCompat.getColor(mContext, R.color.green));
                    } else {
                        itemViewHolder.mWordTv.setTextColor(itemViewHolder.mOriginalColor);
                    }
                }
            });
            itemViewHolder.mAccentEt.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    mImportWordList.get(holder.getAdapterPosition()).setAccent(s.toString());
                }
            });
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.mFooterLayout.setOnClickListener(v -> {
                if (mOnAddClickListener != null)
                    mOnAddClickListener.onAddClick();
            });
        }
    }

    @Override
    public int getItemCount() {
        return mImportWordList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return position == mImportWordList.size() ? TYPE_FOOTER : TYPE_ITEM;
    }

    public void setOnAddClickListener(OnAddClickListener onAddClickListener) {
        mOnAddClickListener = onAddClickListener;
    }

    public interface OnAddClickListener {
        void onAddClick();
    }

}