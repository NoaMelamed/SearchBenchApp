package com.example.noaandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private Context context;
    private List<String> imageUrls;

    /**
     * Constructor to initialize adapter with context and list of image URLs
     */
    public ImageAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    /**
     * Inflates the layout for each item in the RecyclerView
     */
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    /**
     * Binds the image URL at the current position to the ImageView using Glide
     */
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        // Load image into ImageView and display a fallback on error
        Glide.with(context)
                .load(imageUrls.get(position))
                .error(R.drawable.error_image)
                .into(holder.imageView);
    }

    /**
     * Returns the number of images to be displayed
     */
    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    /**
     * ViewHolder class for holding image view references
     */
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        /**
         * Binds the image_view from the layout to this holder
         */
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
