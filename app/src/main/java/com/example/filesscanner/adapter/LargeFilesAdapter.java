package com.example.filesscanner.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.filesscanner.R;
import com.example.filesscanner.common.TConstants;
import com.example.filesscanner.utils.SDCardFile;

import java.util.ArrayList;
import java.util.Locale;


public class LargeFilesAdapter extends RecyclerView.Adapter<LargeFilesAdapter.ViewHolder>{
    private final ArrayList<SDCardFile> fileExtensions;

    public LargeFilesAdapter(ArrayList<SDCardFile> fileExtensions) {
        this.fileExtensions = fileExtensions;
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
    public LargeFilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_row, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(LargeFilesAdapter.ViewHolder holder, int position) {
        SDCardFile item = fileExtensions.get(position);

        holder.nameTv.setText("FileName: "+item.getFileName());
        StringBuffer sb = new StringBuffer();
        String size = String.format(Locale.getDefault(),
                TConstants.TWO_DECIMALFORMATER, item.getFormattedSize());
        sb.append(size).append(item.getMetric());
        holder.valueTv.setText("Size: "+size.toString());

    }

    @Override
    public int getItemCount() {
        return fileExtensions.size();
    }
}
