package com.example.smarthome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FanController#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LightController extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private TextView deviceNameTextView;
    private EditText startLux;
    private EditText endLux;
    private SwitchCompat autoSwitch;
    private Button saveButton;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public LightController() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FanController.
     */
    // TODO: Rename and change types and number of parameters
    public static FanController newInstance(String param1, String param2) {
        FanController fragment = new FanController();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_light_controller, container, false);

        // Initialize views
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        startLux = view.findViewById(R.id.on_lux);
        endLux = view.findViewById(R.id.off_lux);
        autoSwitch = view.findViewById(R.id.auto_light_sw);
        saveButton = view.findViewById(R.id.save_light_button);
        startLux.setText(sharedPreferences.getString("startLux", ""));
        endLux.setText(sharedPreferences.getString("endLux", ""));
        autoSwitch.setChecked(sharedPreferences.getBoolean("autoLightMode", false));
        // Set up click listener for save button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle save button click event here
                String startTempValue = startLux.getText().toString();
                String endTempValue = endLux.getText().toString();
                boolean autoModeEnabled = autoSwitch.isChecked();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("startLux", startTempValue);
                editor.putString("endLux", endTempValue);
                editor.putBoolean("autoLightMode", autoModeEnabled);
                editor.apply();
                Toast.makeText(getActivity(), "Saved successfully", Toast.LENGTH_SHORT).show();
                replaceFragmentWithDelay(new HomeFragment(), 1500);
            }
        });
        return view;
    }
    private void replaceFragmentWithDelay(Fragment fragment, long delayMillis) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                replaceFragment(fragment);
            }
        }, delayMillis);
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}