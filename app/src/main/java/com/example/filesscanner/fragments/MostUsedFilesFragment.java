package com.example.filesscanner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.filesscanner.R;
import com.example.filesscanner.adapter.MostUsedFilesAdapter;

import java.util.TreeMap;


public class MostUsedFilesFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private TreeMap<String, Integer> mostUsedFiles;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_layout, container, false);

        mRecyclerView = (RecyclerView) viewGroup.findViewById(R.id.recyclerView);

        return viewGroup;
    }

    public void setMostUsedFiles(TreeMap<String, Integer> mostUsedFiles) {
        this.mostUsedFiles = mostUsedFiles;

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.setAdapter(new MostUsedFilesAdapter(mostUsedFiles));

        updateView();
    }

    private void updateView() {

    }
}
