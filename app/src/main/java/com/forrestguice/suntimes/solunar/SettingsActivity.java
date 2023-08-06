// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020 Forrest Guice
    This file is part of SolunarPeriods.

    SolunarPeriods is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    SolunarPeriods is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with SolunarPeriods.  If not, see <http://www.gnu.org/licenses/>.
*/

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

import com.forrestguice.suntimes.addon.AppThemeInfo;

import java.util.List;

/**
 * SettingsActivity
 */
public class SettingsActivity extends PreferenceActivity
{
    public static final String EXTRA_THEME_RESID = "themeresid";
    public static final String EXTRA_THEME_NIGHTMODE = "themenightmode";

    private static AppThemes themes;

    @Override
    protected void attachBaseContext(Context context)
    {
        AppThemeInfo.setFactory(themes = new AppThemes());
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        int themeResID = getIntent().getIntExtra(EXTRA_THEME_RESID, -1);
        if (themeResID != -1)
        {
            setTheme(themeResID);
            themes.setDefaultNightMode(getIntent().getIntExtra(EXTRA_THEME_NIGHTMODE, AppThemeInfo.MODE_NIGHT_NO));
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
            if (item.getItemId() == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }


}
