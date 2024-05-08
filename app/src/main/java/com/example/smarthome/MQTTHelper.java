package com.example.smarthome;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import info.mqtt.android.service.Ack;
import info.mqtt.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;

    public final String[] arrayTopics = {"duydao0604/feeds/s-temperature", "duydao0604/feeds/s-humid", "duydao0604/feeds/s-lumi",
                                         "duydao0604/feeds/btn-fan", "duydao0604/feeds/btn-light", "duydao0604/feeds/btn-tv"};

    final String clientId = "abc231aka";
    final String username = "duydao0604";
    final String password = "????";
    // password 
    final String serverUri = "tcp://io.adafruit.com:1883";

    public MQTTHelper(Context context){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId, Ack.AUTO_ACK);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        connect();
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());
        mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {

                DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                disconnectedBufferOptions.setBufferEnabled(true);
                disconnectedBufferOptions.setBufferSize(100);
                disconnectedBufferOptions.setPersistBuffer(false);
                disconnectedBufferOptions.setDeleteOldestMessages(false);
                mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                subscribeToTopic();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
            }
        });
    }

    public void subscribeToTopic() {
        for(int i = 0; i < arrayTopics.length; i++) {
            mqttAndroidClient.subscribe(arrayTopics[i], 0, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                        Log.d("TEST", "Subscribed!");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d("TEST", "Subscribed fail!");
                    }
            });
        }


    }
}