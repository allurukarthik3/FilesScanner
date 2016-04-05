package com.example.filesscanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.filesscanner.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class MostUsedFilesAdapter extends RecyclerView.Adapter<MostUsedFilesAdapter.ViewHolder>{
    private ArrayList dataList;

    public MostUsedFilesAdapter(TreeMap<String, Integer> mostUsedFiles) {
        dataList = new ArrayList();
        dataList.addAll(mostUsedFiles.entrySet());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTv, valueTv;
        public ViewHolder(View v) {
            super(v);
            nameTv = (TextView)v.findViewById(R.id.nameTv);
            valueTv = (TextView)v.findViewById(R.id.valueTv);
        }
    }

    @Override
    public MostUsedFilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MostUsedFilesAdapter.ViewHolder holder, int position) {
        Map.Entry item = (Map.Entry) dataList.get(position);

        holder.nameTv.setText("Extension: "+(String)item.getKey());
        holder.valueTv.setText("Frequency: "+((Integer)item.getValue()));

    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }
}
