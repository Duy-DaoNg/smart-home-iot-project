package com.example.smarthome;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChartsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LineChart lineChart;
    private AdafruitIOClient adafruitIOClient;
    private TextView valueTextView;

    public ChartsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChartFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChartsFragment newInstance(String param1, String param2) {
        ChartsFragment fragment = new ChartsFragment();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_charts, container, false);
        adafruitIOClient = new AdafruitIOClient();

        lineChart = view.findViewById(R.id.lineChart);

        // Lấy dữ liệu từ Adafruit IO và hiển thị trên biểu đồ
        try {
            new FetchDataAsyncTask().execute("s-temperature");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
    private class FetchDataAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            AdafruitIOClient client = new AdafruitIOClient();
            try {
                Log.d("RECEIDATA", "Start get data");
                return client.fetchRecentDataFromAdafruitIO(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String averagedData = adafruitIOClient.calculateDailyAverage(result);
                Log.d("RECEIDATA", averagedData);
                displayDataOnChart(averagedData);

            } else {
                Log.d("TEST", "FAILED");
            }
        }
    }
    private void displayDataOnChart(String responseData) {
        // Hiển thị dữ liệu trên biểu đồ
        try {
            JSONArray jsonArray = new JSONArray(responseData);
            List<Entry> entries = new ArrayList<>();

            // Phân tích dữ liệu JSON và thêm vào danh sách các điểm dữ liệu
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                float average = (float) jsonObject.getDouble("average");
                String xLabel = jsonObject.getString("date");
                entries.add(new Entry(i, average));
            }

            // Tạo đối tượng LineDataSet
            LineDataSet dataSet = new LineDataSet(entries, "Daily Average Temperature");
            dataSet.setColor(Color.BLUE);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setDrawValues(false);

            // Tạo đối tượng LineData và đặt dữ liệu cho biểu đồ
            LineData lineData = new LineData(dataSet);
            lineChart.setData(lineData);

            // Thiết lập định dạng cho trục X
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setGranularity(1f);
            xAxis.setValueFormatter(new MyXAxisValueFormatter());

            // Thiết lập định dạng cho trục Y
            YAxis yAxisLeft = lineChart.getAxisLeft();
            yAxisLeft.setGranularity(1f);

            // Tắt đường kẻ lưới
            lineChart.getAxisLeft().setDrawGridLines(false);
            lineChart.getXAxis().setDrawGridLines(false);
            lineChart.getAxisRight().setDrawGridLines(false);

            lineChart.invalidate();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class MyXAxisValueFormatter extends ValueFormatter {
        @Override
        public String getAxisLabel(float value, com.github.mikephil.charting.components.AxisBase axis) {
            // Định dạng giá trị trục X ở đây, ví dụ: "Ngày 1", "Ngày 2", ...
            return "Ngày " + ((int)value + 1); // Bắt đầu từ Ngày 1
        }
    }
}