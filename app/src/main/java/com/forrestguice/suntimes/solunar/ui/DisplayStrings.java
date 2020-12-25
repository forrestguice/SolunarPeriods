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
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Menu;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimes.solunar.MainActivity;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;
import com.forrestguice.suntimes.solunar.data.SolunarRating;

import java.lang.reflect.Method;
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
        return formatRating(rating, brackets, labels);
    }

    public static String formatRatingExplanation(@NonNull Context context, SolunarRating rating)
    {
        StringBuilder retValue = new StringBuilder();
        String[] reasons = rating.getReasons();
        for (int i=0; i<reasons.length; i++) {
            retValue.append(reasons[i]);
            retValue.append(" ");
        }
        return retValue.toString();
    }

    public static CharSequence formatCardSummary(@NonNull Context context, @Nullable SolunarData data, @NonNull TimeZone timezone, boolean is24Hour)
    {
        if (data != null)
        {
            CharSequence dateDisplay = formatDate(context, data.getDateMillis());
            double dayRating = data.getRating().getDayRating();
            String ratingLabel = formatRating(context, dayRating);
            double[] ratingStars = formatRatingStars(dayRating);

            MoonPhaseDisplay moonphase = MoonPhaseDisplay.valueOf(data.getMoonPhase());
            CharSequence moonillum = formatIllumination(context, data.getMoonIllumination());
            CharSequence sunrise = formatTime(context, data.getDateMillis(SolunarData.KEY_SUNRISE), timezone, is24Hour);
            CharSequence sunset = formatTime(context, data.getDateMillis(SolunarData.KEY_SUNSET), timezone, is24Hour);

            // TODO: major/minor periods

            return context.getString(R.string.format_card_summary, data.getLocation(), dateDisplay, ratingLabel, ((int)ratingStars[0] + ""), moonphase, moonillum, sunrise, sunset);

        } else {
            return "";
        }
    }

    public static String formatHeightenedPeriodNote(@NonNull Context context, @NonNull SolunarPeriod period)
    {
        if (period.occursAtSunrise()) {
            return (period.getType() == SolunarPeriod.TYPE_MAJOR)
                    ? context.getString(R.string.note_major_period_at_sunrise)
                    : context.getString(R.string.note_minor_period_at_sunrise);

        } else if (period.occursAtSunset()) {
            return (period.getType() == SolunarPeriod.TYPE_MAJOR)
                    ? context.getString(R.string.note_major_period_at_sunset)
                    : context.getString(R.string.note_minor_period_at_sunset);
        } else {
            return "";
        }
    }

    public static String formatHeightenedMoonNote(@NonNull Context context, double monthDays, double daysToNew, double daysToFull)
    {
        if (daysToNew <= 0.5d || daysToNew >= (monthDays - 0.5d)) {
            return context.getString(R.string.note_new_moon_today);
        } else if (daysToNew <= 3.5d) {
            return context.getString(R.string.note_new_moon_soon);
        } else if (daysToNew >= (monthDays - 3.5d)) {
            return context.getString(R.string.note_new_moon_recent);
        } else if (daysToFull <= 0.5d || daysToFull >= (monthDays - 0.5d)) {
            return context.getString(R.string.note_full_moon_today);
        } else if (daysToFull <= 3.5d) {
            return context.getString(R.string.note_full_moon_soon);
        } else if (daysToFull >= (monthDays - 3.5d)) {
            return context.getString(R.string.note_full_moon_recent);
        } else {
            return "";
        }
    }

    public static String formatRating(double rating, int[] brackets, String[] labels)
    {
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

    public static double[] formatRatingStars(double dayRating)
    {
        if (dayRating > 0)
        {
            float numStars = (float)(dayRating * 4);
            return new double[] { (int)Math.ceil(numStars), 4d };

        } else return new double[] { 0.25d, 4 };
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

    public static CharSequence formatTime(@NonNull Context context, long dateTime, String timezone, boolean is24Hr) {
        return formatTime(context, dateTime, TimeZone.getTimeZone(timezone), is24Hr);
    }
    public static CharSequence formatTime(@NonNull Context context, long dateTime, TimeZone timezone, boolean is24Hr)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        String format = (is24Hr ? context.getString(R.string.format_time24) : context.getString(R.string.format_time12));
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        timeFormat.setTimeZone(timezone);
        return timeFormat.format(calendar.getTime());
    }

    public static SpannableString formatLocation(@NonNull Context context, @NonNull SuntimesInfo info)
    {
        SuntimesInfo.SuntimesOptions options = info.getOptions(context);
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

    public static String formatIllumination(@NonNull Context context, double illumination)
    {
        NumberFormat formatter = NumberFormat.getPercentInstance();
        formatter.setMinimumFractionDigits(0);
        formatter.setMaximumFractionDigits(0);
        return context.getString(R.string.format_illumination, formatter.format(illumination));
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

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    /**
     * from http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
     */
    public static void forceActionBarIcons(Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);

                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "failed to set show overflow icons", e);
                }
            }
        }
    }

}
