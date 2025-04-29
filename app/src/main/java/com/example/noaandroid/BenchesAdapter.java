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

    private List<Bench> benchesList; // List of Bench objects to be displayed in the RecyclerView

    /**
     * Constructor to initialize the adapter with a list of benches.
     *
     * @param benchesList List of benches to be displayed in the RecyclerView.
     */
    public BenchesAdapter(List<Bench> benchesList) {
        this.benchesList = benchesList;
    }

    /**
     * Creates a new ViewHolder for a single bench item.
     *
     * @param parent The parent ViewGroup (RecyclerView).
     * @param viewType The view type of the item.
     * @return A new instance of BenchesViewHolder.
     */
    @NonNull
    @Override
    public BenchesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_bench_item, parent, false);
        return new BenchesViewHolder(view);
    }

    /**
     * Binds data to the ViewHolder for a specific position.
     *
     * @param holder The ViewHolder to which data will be bound.
     * @param position The position of the item in the data list.
     */
    @Override
    public void onBindViewHolder(@NonNull BenchesViewHolder holder, int position) {
        Bench bench = benchesList.get(position);
        holder.benchName.setText(bench.getName()); // Set the name of the bench
        holder.ratingBar.setRating((float)bench.getAverageRating()); // Set the average rating of the bench

        // Get the first image URL if available and load it into the ImageView
        if (bench.getImageUri() != null && !bench.getImageUri().isEmpty()) {
            String firstImageUrl = bench.getImageUri().get(0);

            // Load the first image URL into the ImageView using Glide
            Glide.with(holder.itemView.getContext())
                    .load(firstImageUrl)
                    .into(holder.benchImage);
        } else {
            // Load a placeholder image if there are no images
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.new_logo_icon_only) // Replace with your placeholder image resource
                    .into(holder.benchImage);
        }

        // Set the onClickListener to open the SingleBenchActivity with the selected bench's ID
        holder.tvOpenBenchActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), SingleBenchActivity.class);
                String benchDocId = bench.getBenchId(); // Firestore document ID
                Log.d("BenchActivity", "benchDocId: " + benchDocId);

                intent.putExtra("benchDocId", benchDocId); // Pass the bench document ID to the activity

                Log.d("BenchActivity", "Launching SingleBenchActivity...");
                holder.itemView.getContext().startActivity(intent); // Start the SingleBenchActivity
            }
        });
    }

    /**
     * Gets the total number of items in the list.
     *
     * @return The number of items in the benches list.
     */
    @Override
    public int getItemCount() {
        return benchesList.size();
    }

    /**
     * Updates the data in the adapter with a new list of benches.
     *
     * @param newBenchesList The new list of benches to be displayed.
     */
    public void updateData(List<Bench> newBenchesList) {
        this.benchesList.clear(); // Clear the old list
        this.benchesList.addAll(newBenchesList); // Add all new items to the list
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    /**
     * ViewHolder for the individual bench items in the RecyclerView.
     */
    public static class BenchesViewHolder extends RecyclerView.ViewHolder {
        public RatingBar ratingBar; // Rating bar to show the average rating of the bench
        TextView benchName; // TextView to display the name of the bench
        ImageView benchImage; // ImageView to display the bench image
        TextView tvOpenBenchActivity; // TextView to open the SingleBenchActivity

        /**
         * Constructor to initialize the ViewHolder with the view for a single item.
         *
         * @param itemView The view representing a single bench item.
         */
        public BenchesViewHolder(@NonNull View itemView) {
            super(itemView);
            benchName = itemView.findViewById(R.id.textBench); // Initialize the bench name view
            ratingBar = itemView.findViewById(R.id.rating_bar); // Initialize the rating bar
            benchImage = itemView.findViewById(R.id.benchImage); // Initialize the image view
            tvOpenBenchActivity = itemView.findViewById(R.id.tv_open_bench_activity); // Initialize the TextView
        }
    }
}
