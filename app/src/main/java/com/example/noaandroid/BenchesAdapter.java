package com.example.noaandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BenchesAdapter extends RecyclerView.Adapter<BenchesAdapter.BenchesViewHolder> {

    private final List<Bench> benchesList;

    public BenchesAdapter(List<Bench> benchesList) {
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
        Bench bench = benchesList.get(position);
        holder.benchName.setText(bench.getBenchName());
        holder.ratingBar.setRating(bench.getRating());

        Glide.with(holder.itemView.getContext())
                .load(bench.getImageUri())
                .into(holder.benchImage);

    }

    @Override
    public int getItemCount() {
        return benchesList.size();
    }

    public static class BenchesViewHolder extends RecyclerView.ViewHolder {
        public RatingBar ratingBar;
        TextView benchName;
        ImageView benchImage;


        public BenchesViewHolder(@NonNull View itemView) {
            super(itemView);
            benchName = itemView.findViewById(R.id.textBench);
            ratingBar = itemView.findViewById(R.id.rating_bar);
            benchImage = itemView.findViewById(R.id.image_preview);

        }
    }
}
