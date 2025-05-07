package com.example.noaandroid;

import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * A Fragment that handles filters for benches.
 */
public class BenchFiltersFragment extends Fragment {

    private final Rect ICON_BOUNDS = new Rect(0, 0, 58, 58);


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
        RadioButton radioPicnicSize = view.findViewById(R.id.radioPicnicSize);

        initSizeIcons(radioSingleSeat, radioRegularSize, radioPicnicSize);

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

    private void initSizeIcons(RadioButton radioSingleSeat, RadioButton radioRegularSize, RadioButton radioPicnicSize) {
        applyIconToRadioButton(radioSingleSeat, R.drawable.ic_single_bench, "Single seat");
        applyIconToRadioButton(radioRegularSize, R.drawable.bench_icon, "Regular size");
        applyIconToRadioButton(radioPicnicSize, R.drawable.ic_picnic_table, "Picnic bench");
    }

    private void applyIconToRadioButton(RadioButton button, int drawableRes, String label) {
        Drawable icon = ContextCompat.getDrawable(requireContext(), drawableRes);
        if (icon != null) {
            icon.setBounds(new Rect(ICON_BOUNDS)); // clone to avoid shared mutation
            ImageSpan imageSpan = new ImageSpan(icon, ImageSpan.ALIGN_BOTTOM);
            SpannableString spannableString = new SpannableString("   " + label);
            spannableString.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            button.setText(spannableString);
        }

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
        int selectedId = radioGroupBenchSize.getCheckedRadioButtonId();
        Log.d("DEBUG", "selectedID " + selectedId);

        String size = null;
        if (selectedId == R.id.radioSingleSeat) {
            size = "Single";
        } else if (selectedId == R.id.radioRegularSize) {
            size = "Regular";
        } else if (selectedId == R.id.radioPicnicSize) {
            size = "Picnic";
        }

        // Retrieve boolean values from the switches
        // We don't accept false - either user filters or not (true / don't care)
        boolean isShaded = switchShade.isChecked();
        boolean quietStreet = switchQuietStreet.isChecked();
        boolean nearCafe = switchNearCafe.isChecked();
        boolean shortDistance = switchShortDistance.isChecked();
        boolean highRated = switchHighRated.isChecked();

        // Add all filter data as extras to the Intent
        intent.putExtra("size", size); // this extra is the only nullable in the bundle
        intent.putExtra("isShaded", isShaded);
        intent.putExtra("quietStreet", quietStreet);
        intent.putExtra("nearCafe", nearCafe);
        intent.putExtra("shortDistance", shortDistance);
        intent.putExtra("highRated", highRated);

        // Log the selected size for debugging
        Log.w("Noa's", "bundleFilters: " + intent.getExtras());

        return intent;
    }


}
