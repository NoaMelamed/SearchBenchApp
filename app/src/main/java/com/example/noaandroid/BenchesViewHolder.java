package com.example.noaandroid;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class BenchesViewHolder extends RecyclerView.ViewHolder {

    TextView benchName;

    TextView benchDesc;

    ImageView imageView;



    public BenchesViewHolder(View itemView) {

        super(itemView);

        benchName = itemView.findViewById(R.id.textBench);

        benchDesc = itemView.findViewById(R.id.subTextBench);

        imageView = itemView.findViewById(R.id.benchImage);

    }


}
