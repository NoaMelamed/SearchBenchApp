package com.example.noaandroid;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ImageGalleryFragment extends Fragment {

    private List<String> imageUrls;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private Button closeButton;

    /**
     * Creates a new instance of ImageGalleryFragment with a list of image URLs
     */
    public static ImageGalleryFragment newInstance(List<String> imageUrls) {
        ImageGalleryFragment fragment = new ImageGalleryFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("imageUrls", (ArrayList<String>) imageUrls);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Retrieves the image URLs passed via arguments
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrls = getArguments().getStringArrayList("imageUrls");
        }
    }

    /**
     * Inflates the fragment layout and initializes views
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_gallery, container, false);
        initViews(view);
        return view;
    }

    /**
     * Sets up the RecyclerView and close button
     */
    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        closeButton = view.findViewById(R.id.close_button);

        // Set RecyclerView to use a grid layout with 3 columns
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        imageAdapter = new ImageAdapter(getContext(), imageUrls);
        recyclerView.setAdapter(imageAdapter);

        // Close button removes the fragment and pops the back stack
        closeButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().remove(this).commit(); // Remove this fragment
            fragmentManager.popBackStack(); // Pop from back stack
        });
    }
}
