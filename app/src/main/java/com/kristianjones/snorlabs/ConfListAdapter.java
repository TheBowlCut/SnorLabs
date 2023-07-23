package com.kristianjones.snorlabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.LinkedList;

public class ConfListAdapter extends RecyclerView.Adapter<ConfListAdapter.ConfViewHolder> {

    private final LinkedList<String> mConfList;
    private LayoutInflater mInflater;

    class ConfViewHolder extends RecyclerView.ViewHolder {

        public final TextView confItemView;
        final ConfListAdapter mAdapter;

        public ConfViewHolder (View itemView, ConfListAdapter adapter) {
            super(itemView);
            confItemView= itemView.findViewById(R.id.confWord);
            this.mAdapter = adapter;

        }
    }

    @NonNull
    @Override
    public ConfListAdapter.ConfViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View mItemView = mInflater.inflate(R.layout.conflist_item, parent, false);

        return new ConfViewHolder(mItemView, this);

    }

    @Override
    public void onBindViewHolder(@NonNull ConfListAdapter.ConfViewHolder holder, int position) {

        // Connects your data to the view holder
        String mCurrent = mConfList.get(position);
        holder.confItemView.setText(mCurrent);

    }

    @Override
    public int getItemCount() {
        return mConfList.size();
    }

    public ConfListAdapter(Context context, LinkedList<String> confList) {
        mInflater = LayoutInflater.from(context);
        this.mConfList = confList;
    }
}
