package com.cyberlight.pocketword.ui;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.entity.Word;
import com.cyberlight.pocketword.util.TextWatcherAdapter;

import java.util.List;

public class ImportRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    private final List<Word> mImportWordList;
    private OnAddClickListener mOnAddClickListener;

    private static class ItemViewHolder extends RecyclerView.ViewHolder {

        private final EditText mWordEt;
        private final EditText mMeanEt;
        private final EditText mAccentEt;
        private final ImageView mDeleteIv;

        public ItemViewHolder(View v) {
            super(v);
            mWordEt = v.findViewById(R.id.rv_import_word_et);
            mMeanEt = v.findViewById(R.id.rv_import_mean_et);
            mAccentEt = v.findViewById(R.id.rv_import_accent_et);
            mDeleteIv = v.findViewById(R.id.rv_import_delete_iv);
        }

    }

    private static class FooterViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mFooterLayout;

        public FooterViewHolder(View v) {
            super(v);
            mFooterLayout = v.findViewById(R.id.rv_import_footer_layout);
        }
    }

    public ImportRecyclerAdapter(List<Word> importWordList) {
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
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.mDeleteIv.setOnClickListener(v -> {
                mImportWordList.remove(holder.getAdapterPosition());
                notifyDataSetChanged();
            });
            itemViewHolder.mWordEt.setText(mImportWordList.get(holder.getAdapterPosition()).getWordStr());
            itemViewHolder.mWordEt.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    mImportWordList.get(holder.getAdapterPosition()).setWordStr(s.toString());
                }
            });
            itemViewHolder.mMeanEt.setText(mImportWordList.get(holder.getAdapterPosition()).getMean());
            itemViewHolder.mMeanEt.addTextChangedListener(new TextWatcherAdapter() {
                @Override
                public void afterTextChanged(Editable s) {
                    mImportWordList.get(holder.getAdapterPosition()).setMean(s.toString());
                }
            });

            itemViewHolder.mAccentEt.setText(mImportWordList.get(holder.getAdapterPosition()).getAccent());
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