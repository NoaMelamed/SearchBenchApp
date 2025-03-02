package com.example.noaandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.noaandroid.R;

public class RatingFragment extends DialogFragment {
    private RatingBar ratingBar;
    private Button submitButton;
    private OnRatingSubmittedListener listener; // Interface to communicate with the activity

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);
        ratingBar = view.findViewById(R.id.ratingBar);
        submitButton = view.findViewById(R.id.submitButton);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        submitButton.setOnClickListener(v -> {
            float rating = ratingBar.getRating();
            if (listener != null) {
                listener.onRatingSubmitted(rating);
            }
            dismiss(); // Close the fragment after submission
        });
    }

    public void setOnRatingSubmittedListener(OnRatingSubmittedListener listener) {
        this.listener = listener;
    }


    public interface OnRatingSubmittedListener {
        void onRatingSubmitted(float rating);
    }
}
