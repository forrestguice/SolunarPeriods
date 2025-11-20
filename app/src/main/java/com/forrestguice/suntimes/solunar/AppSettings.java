// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2023 Forrest Guice
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

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.TimeZoneHelper;

import java.util.TimeZone;

public class AppSettings
{
    public static final String KEY_MODE_TIMEZONE = "timezonemode";
    public static final int TZMODE_SYSTEM = 0, TZMODE_SUNTIMES = 1, TZMODE_LOCALMEAN = 2, TZMODE_APPARENTSOLAR = 3, TZMODE_UTC = 4;
    public static final int TZMODE_DEFAULT = TZMODE_SUNTIMES;

    public static void setTimeZoneMode(Context context, int mode) {
        SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefs.putInt(KEY_MODE_TIMEZONE, mode);
        prefs.apply();
    }
    public static int getTimeZoneMode(Context context)
    {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(KEY_MODE_TIMEZONE, TZMODE_DEFAULT);
    }

    public static TimeZone fromTimeZoneMode(@NonNull Context context, int mode, @Nullable SuntimesInfo suntimesInfo)
    {
        boolean hasLocation = (suntimesInfo != null && suntimesInfo.location != null && suntimesInfo.location.length >= 4);
        switch (mode) {
            case TZMODE_UTC: return getUtcTZ();
            case TZMODE_LOCALMEAN: return getLocalMeanTZ(context, hasLocation ? suntimesInfo.location[2] : "0");
            case TZMODE_APPARENTSOLAR: return getApparantSolarTZ(context, hasLocation ? suntimesInfo.location[2] : "0");
            case TZMODE_SUNTIMES: return getTimeZone(context, suntimesInfo);
            case TZMODE_SYSTEM: default: return TimeZone.getDefault();
        }
    }

    public static TimeZone getUtcTZ() {
        return TimeZone.getTimeZone("UTC");
    }

    public static TimeZone getLocalMeanTZ(Context context, String longitude) {
        return new TimeZoneHelper.LocalMeanTime(Double.parseDouble(longitude), context.getString(R.string.solartime_localmean));
    }

    public static TimeZone getApparantSolarTZ(Context context, String longitude) {
        return new TimeZoneHelper.ApparentSolarTime(Double.parseDouble(longitude), context.getString(R.string.solartime_apparent));
    }

    public static TimeZone getTimeZone(@NonNull Context context, @Nullable SuntimesInfo info)
    {
        if (info == null) {
            return TimeZone.getDefault();

        } else if (info.timezoneMode == null || info.timezoneMode.equals("CUSTOM_TIMEZONE") && info.timezone != null) {
            return TimeZone.getTimeZone(info.timezone);

        } else if (info.timezoneMode.equals("SOLAR_TIME")) {
            if (info.solartimeMode.equals("UTC")) {
                return getUtcTZ();

            } else {
                if (info.location != null && info.location.length >= 3)
                {
                    if (info.solartimeMode.equals("LOCAL_MEAN_TIME")) {
                        return getLocalMeanTZ(context, info.location[2]);

                    } else if (info.solartimeMode.equals("APPARENT_SOLAR_TIME")) {
                        return getApparantSolarTZ(context, info.location[2]);
                    } else return TimeZone.getDefault();
                } else return TimeZone.getDefault();
            }

        } else {
            return TimeZone.getDefault();
        }
    }

}
