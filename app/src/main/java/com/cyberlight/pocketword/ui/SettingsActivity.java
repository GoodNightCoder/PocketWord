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
import androidx.preference.PreferenceManager;

import com.cyberlight.pocketword.R;
import com.cyberlight.pocketword.data.pref.PrefsConst;
import com.cyberlight.pocketword.data.pref.PrefsMgr;
import com.cyberlight.pocketword.data.pref.SharedPrefsMgr;
import com.cyberlight.pocketword.receiver.StudyRemindReceiver;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class SettingsActivity extends AppCompatActivity {
    private static final String PICK_STUDY_REMIND_TIME_REQUEST_KEY = "pick_fall_asleep_request_key";

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
            Preference ignoreBatteryOptimizationPref = findPreference(PrefsConst.IGNORE_BATTERY_OPTIMIZATION_KEY);
            Preference manageStartupAppsPref = findPreference(PrefsConst.MANAGE_STARTUP_APPS_KEY);
            // 检查各个Preference是否存在
            if (studyRemindTimePref == null || ignoreBatteryOptimizationPref == null
                    || manageStartupAppsPref == null)
                return;
            Context context = requireContext();
            PrefsMgr prefsMgr = SharedPrefsMgr.getInstance(context);
            // 初始化Summary
            long studyRemindSecOfDay = prefsMgr.getStudyRemindTime();
            LocalTime studyRemindTime = LocalTime.ofSecondOfDay(studyRemindSecOfDay);
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("H:mm");
            studyRemindTimePref.setSummary(studyRemindTime.format(formatter));
            // 设置几个自定义Preference的点击监听
            FragmentManager fragmentManager = getChildFragmentManager();
            studyRemindTimePref.setOnPreferenceClickListener(preference -> {
                if (fragmentManager.findFragmentByTag(HourMinutePickerDialogFragment.TAG) == null) {
                    int studyRemindTimeSecs = (int) prefsMgr.getStudyRemindTime();
                    DialogFragment dialogFragment = HourMinutePickerDialogFragment.newInstance(
                            PICK_STUDY_REMIND_TIME_REQUEST_KEY,
                            studyRemindTimeSecs / 3600,
                            studyRemindTimeSecs % 3600 / 60);
                    dialogFragment.show(fragmentManager, HourMinutePickerDialogFragment.TAG);
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
            fragmentManager.setFragmentResultListener(PICK_STUDY_REMIND_TIME_REQUEST_KEY,
                    this, (requestKey, result) -> {
                        int hour = result.getInt(HourMinutePickerDialogFragment.HM_HOUR_KEY);
                        int minute = result.getInt(HourMinutePickerDialogFragment.HM_MINUTE_KEY);
                        long newSecOfDay = hour * 3600L + minute * 60L;
                        LocalTime newTime = LocalTime.ofSecondOfDay(newSecOfDay);
                        prefsMgr.setStudyRemindTime(newSecOfDay);
                        studyRemindTimePref.setSummary(newTime.format(formatter));
                    });
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (key) {
                case PrefsConst.STUDY_REMIND_KEY:
                case PrefsConst.STUDY_REMIND_TIME_KEY:
                    StudyRemindReceiver.checkReminder(requireContext());
                    break;
            }
        }
    }
}