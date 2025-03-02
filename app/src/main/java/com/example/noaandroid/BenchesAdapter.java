package com.example.noaandroid;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class BenchesAdapter extends RecyclerView.Adapter<BenchesAdapter.BenchesViewHolder> {

    private List<Bench> benchesList;

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
        holder.benchName.setText(bench.getName());
        holder.ratingBar.setRating((float)bench.getAverageRating());

        // Get the first image URL if available
        if (bench.getImageUri() != null && !bench.getImageUri().isEmpty()) {
            String firstImageUrl = bench.getImageUri().get(0);

            // Load the first image URL into the ImageView
            Glide.with(holder.itemView.getContext())
                    .load(firstImageUrl)
                    .into(holder.benchImage);
        } else {
            // Load a placeholder image if there are no images
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.new_logo_icon_only) // Replace with your placeholder image resource
                    .into(holder.benchImage);
        }

        holder.tvOpenBenchActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(holder.itemView.getContext(), SingleBenchActivity.class);
                String benchDocId = bench.getBenchId(); // this is the Firestore ID, not a local one!!!!
                Log.d("BenchActivity", "benchDocId: " + benchDocId);


                intent.putExtra("benchDocId", benchDocId);

                Log.d("BenchActivity", "Launching SingleBenchActivity...");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return benchesList.size();
    }

    public void updateData(List<Bench> newBenchesList) {
        this.benchesList.clear();
        this.benchesList.addAll(newBenchesList);
        notifyDataSetChanged(); // Notify the adapter of data changes
    }

    public static class BenchesViewHolder extends RecyclerView.ViewHolder {
        public RatingBar ratingBar;
        TextView benchName;
        ImageView benchImage;
        TextView tvOpenBenchActivity;

        public BenchesViewHolder(@NonNull View itemView) {
            super(itemView);
            benchName = itemView.findViewById(R.id.textBench);ratingBar = itemView.findViewById(R.id.rating_bar);
            benchImage = itemView.findViewById(R.id.benchImage);
            tvOpenBenchActivity = itemView.findViewById(R.id.tv_open_bench_activity); // Add this line
        }
    }
}
