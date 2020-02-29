package com.forrestguice.suntimes.solunar;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import java.util.List;

/**
 * SettingsActivity
 */
public class SettingsActivity extends PreferenceActivity
{
    public static final String EXTRA_THEMERESID = "themeresid";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        int themeResID = getIntent().getIntExtra(EXTRA_THEMERESID, -1);
        if (themeResID != -1) {
            setTheme(themeResID);
        }
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName) || GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * GeneralPreferenceFragment
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        public static final String PREFKEY_LENGTH_MINOR_PERIOD = "pref_length_minor_period";
        public static final String PREFKEY_LENGTH_MAJOR_PERIOD = "pref_length_major_period";

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            Context context = getActivity();
            Preference minorPeriod =findPreference(PREFKEY_LENGTH_MINOR_PERIOD);
            if (minorPeriod != null) {
                minorPeriod.setOnPreferenceChangeListener(onMinutesChangedListener);
                onMinutesChangedListener.onPreferenceChange(minorPeriod, PreferenceManager.getDefaultSharedPreferences(context).getString(minorPeriod.getKey(), ""));
            }

            Preference majorPeriod =findPreference(PREFKEY_LENGTH_MAJOR_PERIOD);
            if (majorPeriod != null) {
                majorPeriod.setOnPreferenceChangeListener(onMinutesChangedListener);
                onMinutesChangedListener.onPreferenceChange(majorPeriod, PreferenceManager.getDefaultSharedPreferences(context).getString(majorPeriod.getKey(), ""));
            }
        }

        private static Preference.OnPreferenceChangeListener onMinutesChangedListener = new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                preference.setSummary(preference.getContext().getString(R.string.format_summary_minutes, value.toString()));
                return true;
            }
        };

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            switch (item.getItemId())
            {
                case android.R.id.home:
                    startActivity(new Intent(getActivity(), SettingsActivity.class));
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }
    }


}
