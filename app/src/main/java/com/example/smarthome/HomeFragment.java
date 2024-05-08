package com.example.smarthome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    SwitchCompat swFan, swLight, swTV;
    TextView txtTemp, txtHumid, txtLux;
    MQTTHelper mqttHelper;
    boolean enableSendSwFanData = true;
    boolean enableSendSwTVData = true;
    boolean enableSendSwLightData = true;
    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        swFan = view.findViewById(R.id.swFan);
        swLight = view.findViewById(R.id.swLight);
        swTV = view.findViewById(R.id.swTV);
        txtTemp = view.findViewById(R.id.currentTemperature);
        txtHumid = view.findViewById(R.id.currentHumid);
        txtLux = view.findViewById(R.id.currentLumi);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout fanLayout = view.findViewById(R.id.fan_area);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) LinearLayout lightLayout = view.findViewById(R.id.light_area);
        fanLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToFanControllerFragment();
            }
        });
        lightLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLightControllerFragment();
            }
        });
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            renderInitialData();
            mqttHelper = mainActivity.getMQTTHelper();
            controlDevices();
            getDataFromMQTT();
        }

        return view;
    }
    public void getDataFromMQTT(){
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {

            }
            @Override
            public void connectionLost(Throwable cause) {

            }
            @Override
            public void messageArrived(String topic, MqttMessage message){
                SharedPreferences sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE);
                if (topic.contains("s-temperature")) {
                    txtTemp.setText(message.toString());
                    String startTempValue = sharedPreferences.getString("startTemp", "-999");
                    String endTempValue = sharedPreferences.getString("endTemp", "-999");
                    boolean autoFanMode = sharedPreferences.getBoolean("autoFanMode", false);

                    double currentTemp = Double.parseDouble(message.toString());
                    double startTempThreshold = Double.parseDouble(startTempValue);
                    double endTempThreshold = Double.parseDouble(endTempValue);
                    if (autoFanMode) {
                        if (currentTemp > startTempThreshold) {
                            if(!swFan.isChecked()){
                                enableSendSwFanData = true;
                                swFan.setChecked(true);
                            }
                        } else if (currentTemp < endTempThreshold) {
                            if(swFan.isChecked()){
                                enableSendSwFanData = true;
                                swFan.setChecked(false);
                            }
                        }
                    }


                } else if (topic.contains("s-humid")) {
                    txtHumid.setText(message.toString());
                } else if (topic.contains("s-lumi")) {
                    txtLux.setText(message.toString());
                    String startLuxValue = sharedPreferences.getString("startLux", "-999");
                    String endLuxValue = sharedPreferences.getString("endLux", "-999");
                    boolean autoLightMode = sharedPreferences.getBoolean("autoLightMode", false);
                    double currentLux = Double.parseDouble(message.toString());
                    double startLuxThreshold = Double.parseDouble(startLuxValue);
                    double endLuxThreshold = Double.parseDouble(endLuxValue);
                    if (autoLightMode) {
                        if (currentLux < startLuxThreshold) {
                            if(!swLight.isChecked()){
                                enableSendSwLightData = true;
                                swLight.setChecked(true);
                            }
                        } else if (currentLux > endLuxThreshold) {
                            if(swLight.isChecked()){
                                enableSendSwLightData = true;
                                swLight.setChecked(false);
                            }
                        }
                    }
                } else if (topic.contains("btn-light")) {
                    boolean result = message.toString().equals("1");
                    if(swLight.isChecked() ^ result) {
                        enableSendSwLightData = false;
                        swLight.setChecked(result);
                        enableSendSwLightData = true;
                    }
                } else if (topic.contains("btn-fan")) {
                    boolean result = message.toString().equals("1");
                    if(swFan.isChecked() ^ result) {
                        enableSendSwFanData = false;
                        swFan.setChecked(result);
                        enableSendSwFanData = true;
                    }
                } else if (topic.contains("btn-tv")) {
                    boolean result = message.toString().equals("1");
                    if(swTV.isChecked() ^ result) {
                        enableSendSwTVData = false;
                        swTV.setChecked(result);
                        enableSendSwTVData = true;
                    }
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
    }
    public void controlDevices(){
        swLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // on below line we are checking
                // if switch is checked or not.
                if (isChecked) {
//                    Log.d("TEST", "is true");
                    if (enableSendSwLightData) {
                        sendDataMQTT("duydao0604/feeds/btn-light", "1");
                    }
                } else {
//                    Log.d("TEST", "is false");
                    if (enableSendSwLightData) {
                        sendDataMQTT("duydao0604/feeds/btn-light", "0");
                    }
                }
            }
        });
        swFan.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // on below line we are checking
                // if switch is checked or not.
                if (isChecked) {
//                    Log.d("TEST", "is true");
                    if (enableSendSwFanData) {
                        sendDataMQTT("duydao0604/feeds/btn-fan", "1");
                    }
                } else {
//                    Log.d("TEST", "is false");

                    if (enableSendSwFanData) {
                        sendDataMQTT("duydao0604/feeds/btn-fan", "0");
                    }
                }
            }
        });
        swTV.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // on below line we are checking
                // if switch is checked or not.
                if (isChecked) {
//                    Log.d("TEST", "is true");
                    if (enableSendSwTVData) {
                        sendDataMQTT("duydao0604/feeds/btn-tv", "1");
                    }

                } else {
//                    Log.d("TEST", "is false");
                    if (enableSendSwTVData) {
                        sendDataMQTT("duydao0604/feeds/btn-tv", "0");
                    }
                }
            }
        });

    }
    public void sendDataMQTT(String topic, String value){
        MqttMessage msg = new MqttMessage();
        msg.setId(1234);
        msg.setQos(0);
        msg.setRetained(false);

        byte[] b = value.getBytes(Charset.forName("UTF-8"));
        msg.setPayload(b);
        mqttHelper.mqttAndroidClient.publish(topic, msg);

    }
    public void renderInitialData() {
        try {
            new FetchDataAsyncTask().execute("s-temperature");
            new FetchDataAsyncTask().execute("s-humid");
            new FetchDataAsyncTask().execute("s-lumi");
            new FetchDataAsyncTask().execute("btn-light");
            new FetchDataAsyncTask().execute("btn-fan");
            new FetchDataAsyncTask().execute("btn-tv");
        }
        finally {

        }
    }
    private class FetchDataAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            AdafruitIOClient client = new AdafruitIOClient();
            try {
                return client.fetchDataFromAdafruitIO(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String key = jsonObject.getString("feed_id");
                    String value = jsonObject.getString("value");
                    Log.d("TEST", result);
                    boolean prev = true;
                    switch (key) {
                        case "2721137":
//                        Log.d("TEST", key + ": " + data);
                            txtTemp.setText(value);
                            break;
                        case "2721140":
                            txtHumid.setText(value);
                            break;
                        case "2721142":
                            txtLux.setText(value);
                            break;
                        case "2721143":
                            prev = enableSendSwLightData;
                            enableSendSwLightData = false;
                            swLight.setChecked(value.equals("1"));
                            enableSendSwLightData = prev;
                            break;
                        case "2721144":
                            prev = enableSendSwFanData;
                            enableSendSwFanData = false;
                            swFan.setChecked(value.equals("1"));
                            enableSendSwFanData = prev;
                            break;
                        case "2784277":
                            prev = enableSendSwTVData;
                            enableSendSwTVData = false;
                            swTV.setChecked(value.equals("1"));
                            enableSendSwTVData = prev;
                            break;
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }

            } else {
                Log.d("TEST", "FAILED");
            }
        }
    }
    public void goToFanControllerFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, new FanController());
        fragmentTransaction.commit();
    }
    public void goToLightControllerFragment() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, new LightController());
        fragmentTransaction.commit();
    }
}