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

    public static ImageGalleryFragment newInstance(List<String> imageUrls) {
        ImageGalleryFragment fragment = new ImageGalleryFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("imageUrls", (ArrayList<String>) imageUrls);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            imageUrls = getArguments().getStringArrayList("imageUrls");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_gallery, container, false);
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        closeButton = view.findViewById(R.id.close_button);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3 columns for small images
        imageAdapter = new ImageAdapter(getContext(), imageUrls);
        recyclerView.setAdapter(imageAdapter);


        closeButton.setOnClickListener(v -> {
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction().remove(this).commit(); // Ensure it's removed
            fragmentManager.popBackStack(); // Clear from back stack
        });

    }


}
