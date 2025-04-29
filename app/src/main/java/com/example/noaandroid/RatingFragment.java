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

    /**
     * Inflates the layout for the rating fragment, finds views, and initializes the rating bar and submit button.
     *
     * @param inflater LayoutInflater used to inflate the view.
     * @param container The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState Bundle containing the fragment's state, if any.
     * @return The inflated view for the fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rating, container, false);
        ratingBar = view.findViewById(R.id.ratingBar);
        submitButton = view.findViewById(R.id.submitButton);
        return view;
    }

    /**
     * Sets up the click listener for the submit button.
     * When the button is clicked, the rating is passed to the listener, and the fragment is dismissed.
     *
     * @param view The root view for the fragment's UI.
     * @param savedInstanceState Bundle containing the fragment's previous state, if any.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        submitButton.setOnClickListener(v -> {
            // Get the rating from the rating bar
            float rating = ratingBar.getRating();
            if (listener != null) {
                // Notify the listener with the rating
                listener.onRatingSubmitted(rating);
            }
            dismiss(); // Close the fragment after submission
        });
    }

    /**
     * Sets the listener to handle the rating submission.
     *
     * @param listener The listener to notify when the rating is submitted.
     */
    public void setOnRatingSubmittedListener(OnRatingSubmittedListener listener) {
        this.listener = listener;
    }

    /**
     * Interface for communicating the rating submission to the activity.
     */
    public interface OnRatingSubmittedListener {
        /**
         * Called when the rating is submitted.
         *
         * @param rating The rating submitted by the user.
         */
        void onRatingSubmitted(float rating);
    }
}
