package com.example.noaandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BenchesAdapter extends RecyclerView.Adapter<BenchesAdapter.BenchesViewHolder> {

    private final List<String> benchesList;

    public BenchesAdapter(List<String> benchesList) {
        this.benchesList = benchesList;
    }

    @NonNull
    @Override
    public BenchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_bench_item, parent, false);
        return new BenchesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BenchesViewHolder holder, int position) {
        String thisBench = benchesList.get(position);
        // Assume `thisBench` is formatted as "Name:Description"
        String[] parts = thisBench.split(":");
        String name = parts.length > 0 ? parts[0] : "Unknown";
        String desc = parts.length > 1 ? parts[1] : "No description";
        holder.benchName.setText(name);
        holder.benchDescription.setText(desc);
    }

    @Override
    public int getItemCount() {
        return benchesList.size();
    }

    public static class BenchesViewHolder extends RecyclerView.ViewHolder {
        TextView benchName;
        TextView benchDescription;

        public BenchesViewHolder(@NonNull View itemView) {
            super(itemView);
            benchName = itemView.findViewById(R.id.textBench);
            benchDescription = itemView.findViewById(R.id.subTextBench);
        }
    }
}
