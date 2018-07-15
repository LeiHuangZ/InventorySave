package com.inventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huang
 */
public class RcvAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> mEpcStrList = new ArrayList<>();
    private static int ITEM_HEADER = 0;
    private static int ITEM_VIEW = 1;

    private Context mContext;

    public void setList(List<String> list) {
        mEpcStrList.clear();
        mEpcStrList.addAll(list);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_HEADER;
        } else {
            return ITEM_VIEW;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        if (viewType == ITEM_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.epc_rc_head, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.epc_rc_tem, parent, false);
            return new MyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if (position != 0) {
            final String epc = mEpcStrList.get(position - 1);
            MyViewHolder viewHolder = (MyViewHolder) holder;
            viewHolder.mRcTvId.setText(String.valueOf(position));
            viewHolder.mRcTvRfid.setText(epc);
        }
    }

    @Override
    public int getItemCount() {
        Log.i("Huang", "getItemCount = " + mEpcStrList.size());
        return mEpcStrList.size() + 1;
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mRcTvId;
        TextView mRcTvRfid;

        MyViewHolder(View itemView) {
            super(itemView);
            mRcTvId = itemView.findViewById(R.id.rc_tv_id);
            mRcTvRfid = itemView.findViewById(R.id.rc_tv_rfid);
        }
    }
}
