package com.cyberlight.pocketword.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.entity.WordBook;

import java.util.List;

public class WordBookRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // ViewHolder类型
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_CREATE = 1;

    private final List<WordBook> mWordBooks;
    private final OnClickCallback mOnClickCallback;

    private static class ItemViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mItemLayout;
        private final TextView mItemTv;

        public ItemViewHolder(View v) {
            super(v);
            mItemLayout = v.findViewById(R.id.rv_wordbook_item_layout);
            mItemTv = v.findViewById(R.id.rv_wordbook_item_tv);
        }
    }

    private static class CreateViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout mCreateLayout;

        public CreateViewHolder(View v) {
            super(v);
            mCreateLayout = v.findViewById(R.id.rv_wordbook_create_layout);
        }
    }

    public WordBookRecyclerAdapter(List<WordBook> wordBooks, OnClickCallback onClickCallback) {
        mWordBooks = wordBooks;
        mOnClickCallback = onClickCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_wordbook_item, parent, false);
            return new ItemViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.rv_wordbook_create, parent, false);
            return new CreateViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            WordBook wordBook = mWordBooks.get(position);
            itemViewHolder.mItemTv.setText(wordBook.getWordBookName());
            itemViewHolder.mItemLayout.setOnClickListener(view -> {
                if (mOnClickCallback != null) {
                    mOnClickCallback.onChoose(wordBook);
                }
            });
        } else if (holder instanceof CreateViewHolder) {
            CreateViewHolder createViewHolder = (CreateViewHolder) holder;
            createViewHolder.mCreateLayout.setOnClickListener(view -> {
                if (mOnClickCallback != null) {
                    mOnClickCallback.onCreate();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mWordBooks.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return mWordBooks.size() == position ? TYPE_CREATE : TYPE_ITEM;
    }

    public interface OnClickCallback {
        void onChoose(WordBook wordBook);

        void onCreate();
    }

}