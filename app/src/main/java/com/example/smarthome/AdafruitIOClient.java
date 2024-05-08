package com.example.smarthome;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AdafruitIOClient {
    // TODO: username and key here
    OkHttpClient client = new OkHttpClient();
    public String fetchDataFromAdafruitIO(String feedKey) throws Exception {

        String url = "https://io.adafruit.com/api/v2/" + username + "/feeds/" + feedKey + "/data/last";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-AIO-Key", aioKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
    public String fetchRecentDataFromAdafruitIO(String feedKey) throws Exception {
        // Lấy ngày hiện tại
        Log.d("RECEIDATA", "start get data");
        Calendar calendar = Calendar.getInstance();
        Date endDate = calendar.getTime();

        // Đặt ngày bắt đầu là ngày đầu tiên của tháng hiện tại
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startDate = calendar.getTime();

        // Định dạng ngày tháng cho URL
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String startDateString = sdf.format(startDate);
        String endDateString = sdf.format(endDate);

        // Tạo URL cho yêu cầu HTTP
        String url = "https://io.adafruit.com/api/v2/" + username + "/feeds/" + feedKey + "/data?" +
                "start_date=" + startDateString + "&end_date=" + endDateString;

        // Tạo yêu cầu HTTP
        Request request = new Request.Builder()
                .url(url)
                .addHeader("X-AIO-Key", aioKey)
                .build();

        // Thực hiện yêu cầu và trả về dữ liệu
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    public String calculateDailyAverage(String responseData) {
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            JSONArray resultArray = new JSONArray();

            // Khởi tạo biến cho việc tính trung bình cộng theo ngày
            float dailyTotal = 0;
            int dataCount = 0;
            String currentDate = null;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String timestamp = jsonObject.getString("created_at");
                String date = timestamp.substring(0, 10); // Lấy ngày từ timestamp

                // Nếu là ngày mới, tính trung bình cộng và reset biến
                if (!date.equals(currentDate)) {
                    if (currentDate != null) {
                        float dailyAverage = dailyTotal / dataCount;
                        JSONObject dailyData = new JSONObject();
                        dailyData.put("date", currentDate);
                        dailyData.put("average", dailyAverage);
                        resultArray.put(dailyData);
                    }
                    currentDate = date;
                    dailyTotal = 0;
                    dataCount = 0;
                }

                float value = (float) jsonObject.getDouble("value");
                dailyTotal += value;
                dataCount++;
            }

            // Xử lý ngày cuối cùng
            if (currentDate != null) {
                float dailyAverage = dailyTotal / dataCount;
                JSONObject dailyData = new JSONObject();
                dailyData.put("date", currentDate);
                dailyData.put("average", dailyAverage);
                resultArray.put(dailyData);
            }

            return resultArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}

