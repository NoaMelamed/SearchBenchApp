package com.example.noaandroid;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A Fragment that handles filters for benches.
 */
public class BenchFiltersFragment extends Fragment {

    // Keys for the arguments used to pass data into the fragment.
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Parameters passed to the fragment during initialization.
    private String mParam1;
    private String mParam2;

    /**
     * Default constructor required for Fragment subclasses.
     */
    public BenchFiltersFragment() {
        // Required empty public constructor
    }

    /**
     * Factory method to create a new instance of this fragment
     * using the provided parameters.
     *
     * @param param1 First parameter for the fragment.
     * @param param2 Second parameter for the fragment.
     * @return A new instance of BenchFiltersFragment.
     */
    public static BenchFiltersFragment newInstance(String param1, String param2) {
        BenchFiltersFragment fragment = new BenchFiltersFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the parameters passed to the fragment during its creation.
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_bench_filters, container, false);

        // Set up the "Apply Filters" button and its click listener.
        Button applyFiltersButton = view.findViewById(R.id.applyFiltersButton);
        applyFiltersButton.setOnClickListener(v -> {
            // Logic to apply filters when the button is clicked.
            // Note: This logic is currently not implemented.
        });

        return view;
    }
}
