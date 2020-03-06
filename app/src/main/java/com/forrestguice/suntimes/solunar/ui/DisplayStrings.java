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

package com.forrestguice.suntimes.solunar.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DisplayStrings
{
    public static String formatRating(@NonNull Context context, double rating)
    {
        String[] labels = context.getResources().getStringArray(R.array.ratings_labels);
        int[] brackets = context.getResources().getIntArray(R.array.ratings_brackets);
        if (brackets.length != labels.length) {
            throw new ArrayIndexOutOfBoundsException("length of ratings_labels and ratings_brackets don't match");
        }

        int last = -1;
        for (int i=0; i<brackets.length; i++)
        {
            if (rating > (last * 0.01d)
                    && rating <= (brackets[i] * 0.01d)) {
                return labels[i];
            }
            last = brackets[i];
        }
        return "";
    }

    public static String formatType(Context context, int periodType) {
        if (periodType == SolunarPeriod.TYPE_MAJOR) {
            return context.getString(R.string.label_major_period);
        } else {
            return context.getString(R.string.label_minor_period);
        }
    }

    public static CharSequence formatDate(@NonNull Context context, long date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return formatDate(context, calendar);
    }

    public static CharSequence formatDate(@NonNull Context context, Calendar date)
    {
        Calendar now = Calendar.getInstance();
        boolean isThisYear = now.get(Calendar.YEAR) == date.get(Calendar.YEAR);

        Locale locale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString( isThisYear ? R.string.format_date : R.string.format_date_long), locale);
        dateFormat.setTimeZone(date.getTimeZone());
        return dateFormat.format(date.getTime());
    }

    public static CharSequence formatTime(@NonNull Context context, long dateTime, String timezone, boolean is24Hr)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        String format = (is24Hr ? context.getString(R.string.format_time24) : context.getString(R.string.format_time12));
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        String formatted = timeFormat.format(calendar.getTime());
        return formatted;
        //String padding = "        ";
        //return padding.substring(Math.min(formatted.length(), padding.length())) + formatted;
    }

    public static SpannableString formatLocation(@NonNull Context context, @NonNull SuntimesInfo info)
    {
        SuntimesInfo.SuntimesOptions options = info.getOptions();
        boolean useAltitude = options.use_altitude;
        if (!useAltitude || info.location[2] == null || info.location[2].equals("0") || info.location[2].isEmpty()) {
            return new SpannableString(context.getString(R.string.format_location, info.location[1], info.location[2]));

        } else {
            try {
                double meters = Double.parseDouble(info.location[3]);
                String altitude = formatHeight(context, meters, options.length_units, 0, true);
                String altitudeTag = context.getString(R.string.format_tag, altitude);
                String displayString = context.getString(R.string.format_location_long, info.location[1], info.location[2], altitudeTag);
                return createRelativeSpan(null, displayString, altitudeTag, 0.5f);

            } catch (NumberFormatException e) {
                Log.e("formatLocation", "invalid altitude! " + e);
                return new SpannableString(context.getString(R.string.format_location, info.location[1], info.location[2]));
            }
        }
    }

    public static String formatHeight(Context context, double meters, String units, int places, boolean shortForm)
    {
        double value;
        String unitsString;
        if (units != null && units.equals(SuntimesInfo.SuntimesOptions.UNITS_IMPERIAL)) {
            value = 3.28084d * meters;
            unitsString = (shortForm ? context.getString(R.string.units_feet_short) : context.getString(R.string.units_feet));

        } else {
            value = meters;
            unitsString = (shortForm ? context.getString(R.string.units_meters_short) : context.getString(R.string.units_meters));
        }

        NumberFormat formatter = NumberFormat.getInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(places);
        return context.getString(R.string.format_location_altitude, formatter.format(value), unitsString);
    }

    public static SpannableString createRelativeSpan(@Nullable SpannableString span, @NonNull String text, @NonNull String toRelative, float relativeSize)
    {
        if (span == null) {
            span = new SpannableString(text);
        }
        int start = text.indexOf(toRelative);
        if (start >= 0) {
            int end = start + toRelative.length();
            span.setSpan(new RelativeSizeSpan(relativeSize), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return span;
    }

}