package com.example.v2a;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

public class SettingsFragment extends PreferenceFragment {

    public static final String AUDIO_QUALITY = "preference_quality";
    public static final String AUDIO_FORMAT = "preference_format";
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);


        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals(AUDIO_FORMAT)){
                    Preference formatPref = findPreference(key);
                    formatPref.setSummary(sharedPreferences.getString(key, ""));
                }

                if(key.equals(AUDIO_QUALITY)){
                    Preference qualityPref = findPreference(key);
                    qualityPref.setSummary(sharedPreferences.getString(key, ""));
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        Preference qualityPref = findPreference(AUDIO_QUALITY);
        qualityPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(AUDIO_QUALITY, ""));

        Preference formatPref = findPreference(AUDIO_FORMAT);
        formatPref.setSummary(getPreferenceScreen().getSharedPreferences().getString(AUDIO_FORMAT, ""));
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
