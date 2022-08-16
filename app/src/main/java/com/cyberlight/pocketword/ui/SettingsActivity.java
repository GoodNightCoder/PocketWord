package com.cyberlight.pocketword.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.pref.PrefsConst;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.receiver.StudyRemindReceiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ImageView backIv = findViewById(R.id.settings_back_iv);
        backIv.setOnClickListener(v -> finish());

    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private final DateTimeFormatter studyRemindTimeFormatter = DateTimeFormatter.ofPattern("H:mm");

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference studyRemindTimePref = findPreference(PrefsConst.STUDY_REMIND_TIME_KEY);
            Preference dailyGoalPref = findPreference(PrefsConst.DAILY_GOAL_KEY);
            Preference ignoreBatteryOptimizationPref = findPreference(PrefsConst.IGNORE_BATTERY_OPTIMIZATION_KEY);
            Preference manageStartupAppsPref = findPreference(PrefsConst.MANAGE_STARTUP_APPS_KEY);
            // 检查各个Preference是否存在
            if (studyRemindTimePref == null
                    || dailyGoalPref == null
                    || ignoreBatteryOptimizationPref == null
                    || manageStartupAppsPref == null)
                return;
            Context context = requireContext();
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
            // 初始化学习提醒时间的Summary
            long studyRemindSecOfDay = prefsMgr.getStudyRemindTime();
            LocalTime studyRemindTime = LocalTime.ofSecondOfDay(studyRemindSecOfDay);
            studyRemindTimePref.setSummary(studyRemindTime.format(studyRemindTimeFormatter));
            // 初始化每日目标的summary
            int dailyGoal = prefsMgr.getDailyGoal();
            dailyGoalPref.setSummary(String.valueOf(dailyGoal));
            // 设置各个自定义Preference的点击监听
            FragmentManager fragmentManager = getChildFragmentManager();
            studyRemindTimePref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(SetStudyRemindTimeDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new SetStudyRemindTimeDialogFragment();
                    dialogFragment.show(fragmentManager, SetStudyRemindTimeDialogFragment.TAG);
                }
                return true;
            });
            dailyGoalPref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(SetGoalDialogFragment.TAG) == null) {
                    DialogFragment dialogFragment = new SetGoalDialogFragment();
                    dialogFragment.show(fragmentManager, SetGoalDialogFragment.TAG);
                }
                return true;
            });
            ignoreBatteryOptimizationPref.setOnPreferenceClickListener(preference -> {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                if (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName())) {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(context, "No matching activity", Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            });
            manageStartupAppsPref.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No matching activity", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Context context = requireContext();
            switch (key) {
                case PrefsConst.STUDY_REMIND_KEY:
                    // 更新学习提醒AlarmManager
                    StudyRemindReceiver.checkReminder(context);
                    break;
                case PrefsConst.STUDY_REMIND_TIME_KEY:
                    // 更新学习提醒AlarmManager
                    StudyRemindReceiver.checkReminder(context);
                    // 更新summary
                    Preference studyRemindTimePref = findPreference(PrefsConst.STUDY_REMIND_TIME_KEY);
                    if (studyRemindTimePref != null) {
                        PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
                        long newSecOfDay = prefsMgr.getStudyRemindTime();
                        LocalTime newTime = LocalTime.ofSecondOfDay(newSecOfDay);
                        studyRemindTimePref.setSummary(newTime.format(studyRemindTimeFormatter));
                    }
                    break;
                case PrefsConst.DAILY_GOAL_KEY:
                    // 更新summary
                    Preference dailyGoalPref = findPreference(PrefsConst.DAILY_GOAL_KEY);
                    if (dailyGoalPref != null) {
                        PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
                        int dailyGoal = prefsMgr.getDailyGoal();
                        dailyGoalPref.setSummary(String.valueOf(dailyGoal));
                    }
                    break;
            }
        }
    }
}