package com.example.noaandroid;

import android.content.Intent;
import android.os.Bundle;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.fragment_bench_filters, container, false);

        // Initialize views and set up listeners
        initViews(view);

        return view;
    }

    private void initViews(View view) {
        // Initialize RadioGroup and RadioButtons for bench size
        RadioGroup radioGroupBenchSize = view.findViewById(R.id.radioGroupBenchSize);
        RadioButton radioSingleSeat = view.findViewById(R.id.radioSingleSeat);
        RadioButton radioRegularSize = view.findViewById(R.id.radioRegularSize);

        // Initialize Switches
        Switch switchShade = view.findViewById(R.id.switchShade);
        Switch switchQuietStreet = view.findViewById(R.id.switchQuietStreet);
        Switch switchNearCafe = view.findViewById(R.id.switchNearCafe);
        Switch switchShortDistance = view.findViewById(R.id.switchShortDistance);
        Switch switchHighRated = view.findViewById(R.id.switchHighRated);

        // Initialize Apply Filters button
        Button applyFiltersButton = view.findViewById(R.id.applyFiltersButton);

        // Set click listener for the Apply Filters button
        applyFiltersButton.setOnClickListener(v -> {
            // Save the selected filter options and pass them to the next activity
            Intent intent = saveFilters(
                    radioGroupBenchSize,
                    switchShade,
                    switchQuietStreet,
                    switchNearCafe,
                    switchShortDistance,
                    switchHighRated
            );

            // Navigate to the BenchesListActivity
            startActivity(intent);
        });
    }

    private Intent saveFilters(RadioGroup radioGroupBenchSize, Switch switchShade, Switch switchQuietStreet,
                               Switch switchNearCafe, Switch switchShortDistance, Switch switchHighRated) {
        // Create an Intent to pass the filters to BenchesListActivity
        Intent intent = new Intent(getActivity(), BenchesListActivity.class);

        // Determine the selected bench size from the RadioGroup
        String selectedSize = null;
        int selectedId = radioGroupBenchSize.getCheckedRadioButtonId();
        if (selectedId == R.id.radioSingleSeat) {
            selectedSize = "Single Seat";
        } else if (selectedId == R.id.radioRegularSize) {
            selectedSize = "Regular Size";
        }
     else {
        selectedSize = ""; // Set an empty string if no size is selected
    }

        // Retrieve values from the switches
        boolean inShade = switchShade.isChecked();
        boolean quietStreet = switchQuietStreet.isChecked();
        boolean nearCafe = switchNearCafe.isChecked();
        boolean shortDistance = switchShortDistance.isChecked();
        boolean highRated = switchHighRated.isChecked();

        // Add all filter values to the Intent
        intent.putExtra("size", selectedSize);
        intent.putExtra("inShade", inShade);
        intent.putExtra("quietStreet", quietStreet);
        intent.putExtra("nearCafe", nearCafe);
        intent.putExtra("shortDistance", shortDistance);
        intent.putExtra("highRated", highRated);

        return intent;
    }
}
