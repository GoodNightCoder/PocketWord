package com.cyberlight.pocketword.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.model.LineChartData;
import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.db.DataRepository;
import com.cyberlight.pocketword.data.db.entity.Record;
import com.cyberlight.pocketword.widget.LineChartView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private static final String TAG = "StatisticsActivity";
    private static final int DAYS_NUM = 30;
    private DataRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        mRepository = DataRepository.getInstance(this);
        ImageView backIv = findViewById(R.id.statistics_back_iv);
        LineChartView countLineChartView = findViewById(R.id.statistics_count_lc);
        LineChartView durationLineChartView = findViewById(R.id.statistics_duration_lc);
        TextView continuousCompletionTv = findViewById(R.id.continuous_completion_tv);
        TextView totalCompletionTv = findViewById(R.id.total_completion_tv);
        TextView dailyAverCountTv = findViewById(R.id.daily_aver_count_tv);
        TextView dailyAverDuraTv = findViewById(R.id.daily_aver_dura_tv);
        backIv.setOnClickListener(v -> finish());
        countLineChartView.setBgDateFormat(date -> {
            String pattern = getString(R.string.statistics_count_lc_bg_date_format);
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return date.format(formatter);
        });
        countLineChartView.setDetailFormat(new LineChartView.DetailFormat() {
            @Override
            public String dateFormat(LocalDate date) {
                String pattern = getString(R.string.statistics_count_lc_detail_date_format);
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return date.format(formatter);
            }

            @Override
            public String numFormat(int num) {
                return getString(R.string.statistics_count_lc_detail_num_format, num);
            }
        });
        durationLineChartView.setBgNumFormat(num -> {
            // 将分钟值转换成小时-分钟
            int hour = num / 60;
            int min = num % 60;
            return getString(R.string.statistics_dura_lc_bg_num_format, hour, min);
        });
        durationLineChartView.setBgDateFormat(date -> {
            String pattern = getString(R.string.statistics_dura_lc_bg_date_format);
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return date.format(formatter);
        });
        durationLineChartView.setDetailFormat(new LineChartView.DetailFormat() {
            @Override
            public String dateFormat(LocalDate date) {
                String pattern = getString(R.string.statistics_dura_lc_detail_date_format);
                final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                return date.format(formatter);
            }

            @Override
            public String numFormat(int num) {
                // 将分钟值转换成小时-分钟
                int hour = num / 60;
                int min = num % 60;
                return getString(R.string.statistics_dura_lc_detail_num_format, hour, min);
            }
        });

        LocalDate today = LocalDate.now();
        mRepository.getRecordsAfter(today.minusDays(DAYS_NUM)).observe(this, records -> {
            List<LineChartData> studyCounts = new ArrayList<>();
            List<LineChartData> studyMins = new ArrayList<>();
            if (records != null && records.size() > 0) {
                int count = 0;
                LocalDate date = today.minusDays(DAYS_NUM - 1);
                for (int i = 0; i < records.size(); i++) {
                    Record record = records.get(i);
                    while (!record.getDate().equals(date) && count < DAYS_NUM) {
                        studyCounts.add(new LineChartData(0, date));
                        studyMins.add(new LineChartData(0, date));
                        date = date.plusDays(1);
                        count++;
                    }
                    if (count >= DAYS_NUM) break;
                    studyCounts.add(new LineChartData(record.getStudyCount(), date));
                    studyMins.add(new LineChartData((int) ((double) record.getStudyDuration() / 60000), date));
                    date = date.plusDays(1);
                    count++;
                }
                while (count < DAYS_NUM) {
                    studyCounts.add(new LineChartData(0, date));
                    studyMins.add(new LineChartData(0, date));
                    date = date.plusDays(1);
                    count++;
                }
            } else {
                for (int i = DAYS_NUM - 1; i >= 0; i--) {
                    LocalDate date = today.minusDays(i);
                    studyCounts.add(new LineChartData(0, date));
                    studyMins.add(new LineChartData(0, date));
                }
            }
            int sumCount = 0;
            int sumMin = 0;
            for (LineChartData data : studyCounts) {
                sumCount += data.num;
            }
            for (LineChartData data : studyMins) {
                sumMin += data.num;
            }
            dailyAverCountTv.setText(String.valueOf(sumCount / studyCounts.size()));
            int averMin = sumMin / studyMins.size();
            dailyAverDuraTv.setText(getString(R.string.statistics_daily_aver_dura_format, averMin / 60, averMin % 60));
            countLineChartView.setDataList(studyCounts);
            durationLineChartView.setDataList(studyMins);
        });
        PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(this);
        int dailyGoal = prefsMgr.getDailyGoal();
        mRepository.getRecordsByStudyCount(dailyGoal).observe(this, records -> {
            if (records != null && records.size() > 0) {
                LocalDate date = LocalDate.now();
                int continuousCompletionsCount = 0;
                for (int i = records.size() - 1; i >= 0; i--) {
                    if (!date.equals(records.get(i).getDate())) break;
                    continuousCompletionsCount++;
                    date = date.minusDays(1);
                }
                totalCompletionTv.setText(String.valueOf(records.size()));
                continuousCompletionTv.setText(String.valueOf(continuousCompletionsCount));
            } else {
                totalCompletionTv.setText(String.valueOf(0));
                continuousCompletionTv.setText(String.valueOf(0));
            }
        });
    }
}