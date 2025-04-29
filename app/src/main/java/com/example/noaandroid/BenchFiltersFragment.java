package com.example.noaandroid;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * A Fragment that handles filters for benches.
 */
public class BenchFiltersFragment extends Fragment {

    /**
     * Inflates the layout and initializes the filter view.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bench_filters, container, false);

        // Initialize all UI views and set up listeners
        initViews(view);

        return view;
    }

    /**
     * Initializes the filter UI elements and sets the listener for the Apply button.
     */
    private void initViews(View view) {
        // Initialize RadioGroup and RadioButtons for bench size selection
        RadioGroup radioGroupBenchSize = view.findViewById(R.id.radioGroupBenchSize);
        RadioButton radioSingleSeat = view.findViewById(R.id.radioSingleSeat);
        RadioButton radioRegularSize = view.findViewById(R.id.radioRegularSize);
        RadioButton radioPicnicSize = view.findViewById(R.id.radioPicnicSize); // Newly added picnic size option

        // Initialize Switches for various filter criteria
        Switch switchShade = view.findViewById(R.id.switchShade);
        Switch switchQuietStreet = view.findViewById(R.id.switchQuietStreet);
        Switch switchNearCafe = view.findViewById(R.id.switchNearCafe);
        Switch switchShortDistance = view.findViewById(R.id.switchShortDistance);
        Switch switchHighRated = view.findViewById(R.id.switchHighRated);

        // Initialize the Apply Filters button
        Button applyFiltersButton = view.findViewById(R.id.applyFiltersButton);

        // Set up a click listener to handle applying the selected filters
        applyFiltersButton.setOnClickListener(v -> {
            // Save selected options and pass them via Intent to BenchesListActivity
            Intent intent = saveFilters(
                    radioGroupBenchSize,
                    switchShade,
                    switchQuietStreet,
                    switchNearCafe,
                    switchShortDistance,
                    switchHighRated
            );

            // Navigate to the BenchesListActivity with filter data
            startActivity(intent);
        });
    }

    /**
     * Collects selected filter values and returns an Intent with them.
     *
     * @return Intent containing filter data to be sent to BenchesListActivity
     */
    private Intent saveFilters(RadioGroup radioGroupBenchSize, Switch switchShade, Switch switchQuietStreet,
                               Switch switchNearCafe, Switch switchShortDistance, Switch switchHighRated) {
        // Create an Intent to launch BenchesListActivity
        Intent intent = new Intent(getActivity(), BenchesListActivity.class);

        // Get the selected bench size from the RadioGroup
        String selectedSize = null;
        int selectedId = radioGroupBenchSize.getCheckedRadioButtonId();
        Log.d("DEBUG", "selectedID " + selectedId);

        if (selectedId == R.id.radioSingleSeat) {
            selectedSize = "Single";
        } else if (selectedId == R.id.radioRegularSize) {
            selectedSize = "Regular";
        } else if (selectedId == R.id.radioPicnicSize) {
            selectedSize = "Picnic";
        } else {
            selectedSize = ""; // Default to empty if no option is selected
        }

        // Retrieve boolean values from the switches
        boolean inShade = switchShade.isChecked();
        boolean quietStreet = switchQuietStreet.isChecked();
        boolean nearCafe = switchNearCafe.isChecked();
        boolean shortDistance = switchShortDistance.isChecked();
        boolean highRated = switchHighRated.isChecked();

        // Add all filter data as extras to the Intent
        intent.putExtra("size", selectedSize);
        intent.putExtra("inShade", inShade);
        intent.putExtra("quietStreet", quietStreet);
        intent.putExtra("nearCafe", nearCafe);
        intent.putExtra("shortDistance", shortDistance);
        intent.putExtra("highRated", highRated);

        // Log the selected size for debugging
        Log.d("DEBUG", "Passing size filter: " + selectedSize);

        return intent;
    }
}
