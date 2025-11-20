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

package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.forrestguice.suntimes.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.ui.DisplayStrings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * SolunarCalculator
 */
public class SolunarCalculator
{
    public static final long MINOR_PERIOD_MILLIS = 60L * 60 * 1000;           // 1 hr
    public static final long MAJOR_PERIOD_MILLIS = 2L * 60 * 60 * 1000;       // 2 hr

    public static final long SUN_PERIOD_MILLIS = 24L * 60 * 60 * 1000;        // 24 hr
    public static final long MOON_PERIOD_MILLIS = 24L * 60 * 60 * 1000 +
                                                       50 * 60 * 1000;        // 24 hr 50 m

    public static final long AVG_MONTH_MILLIS = 30L * 24 * 60 * 60 * 1000;    // 30 d (must be greater than max(synodicPeriod)) (~29.93 d)

    /**
     * calculateData
     */
    public boolean calculateData(Context context, ContentResolver resolver, @NonNull SolunarData data, @NonNull TimeZone timezone)
    {
        if (queryData(context, resolver, data, timezone))
        {
            calculateRating(context, data, timezone);
            data.calculated = true;
        }
        return data.calculated;
    }

    public void calculateRating(@NonNull Context context, @NonNull SolunarData data, @NonNull TimeZone timezone)
    {
        long noonMillis = noon(data.getDate(timezone)).getTimeInMillis();
        double monthDays = data.getMoonPeriod() / 1000d / 60d / 60d / 24d;

        long newMoonMillis = data.getDateMillis(SolunarData.KEY_MOONNEW);
        long millisToNew = Math.abs(newMoonMillis - noonMillis);
        double daysToNew = (millisToNew / 1000d / 60d / 60d / 24d);

        long fullMoonMillis = data.getDateMillis(SolunarData.KEY_MOONFULL);
        long millisToFull = Math.abs(fullMoonMillis - noonMillis);
        double daysToFull = (millisToFull / 1000d / 60d / 60d / 24d);

        double c0 = 1;
        ArrayList<Pair<Double, Integer>> components = new ArrayList<>();
        components.add(new Pair<>(0.5d, 3));
        components.add(new Pair<>(2.5d, 2));
        components.add(new Pair<>(3.5d, 1));
        for (int i=0; i<components.size(); i++)
        {
            Pair<Double, Integer> p = components.get(i);
            if (daysToNew <= p.first || daysToNew >= (monthDays - p.first) ||
                daysToFull <= p.first || daysToFull >= (monthDays - p.first))
            {
                c0 += p.second;
                break;
            }
        }
        c0 /= 4;

        String reason0 = DisplayStrings.formatHeightenedMoonNote(context, monthDays, daysToNew, daysToFull);
        ArrayList<String> reasons = new ArrayList<>();
        if (!reason0.isEmpty()) {
            reasons.add(reason0);
        }

        double c1 = (tallyPeriods(context, data.getMajorPeriods(), reasons) / 2d) * 0.1;
        double c2 = (tallyPeriods(context, data.getMinorPeriods(), reasons) / 2d) * 0.1;

        //data.dayRating = new SolunarRating(c0 + c1 + c2, reasons);
        data.dayRating = new SolunarRating(c0, reasons);
    }


    protected int tallyPeriods(Context context, SolunarPeriod[] periods, ArrayList<String> reasons)
    {
        int c = 0;
        for (SolunarPeriod period : periods)
        {
            if (period == null) {
                continue;
            }
            if (period.occursAtSunrise()) {
                reasons.add(DisplayStrings.formatHeightenedPeriodNote(context, period));
                c++;
            }
            if (period.occursAtSunset()) {
                reasons.add(DisplayStrings.formatHeightenedPeriodNote(context, period));
                c++;
            }
        }
        return c;
    }

    /**
     * queryData
     */
    public boolean queryData(Context context, ContentResolver resolver, @NonNull SolunarData data, @NonNull TimeZone timezone)
    {
        if (context != null && resolver != null)
        {
            Pair<Calendar,Calendar>[] riseSet = new Pair[3];  // [0] yesterday, [1] today, and [2] tomorrow
            try
            {
                querySunriseSunset(resolver, data);
                queryMoonriseMoonset(resolver, data, riseSet);

                // determine lunar noon
                Calendar lunarNoon = null;
                ArrayList<Calendar> noons = findNoon(riseSet);
                if (noons.size() >= 1) {
                    lunarNoon = noons.get(noons.size() - 1);
                    for (Calendar noon : noons) {
                        if (noon.get(Calendar.DAY_OF_YEAR) == data.getDate(timezone).get(Calendar.DAY_OF_YEAR)) {
                            lunarNoon = noon;
                            break;
                        }
                    }
                }
                data.moonnoon = (lunarNoon == null) ? -1 : lunarNoon.getTimeInMillis();

                // determine lunar midnight
                Calendar lunarMidnight = null;
                ArrayList<Calendar> midnights = findMidnight(riseSet);
                if (midnights.size() >= 1) {
                    lunarMidnight = midnights.get(midnights.size() - 1);
                    for (Calendar event : midnights) {
                        if (event.get(Calendar.DAY_OF_YEAR) == data.getDate(timezone).get(Calendar.DAY_OF_YEAR)) {
                            lunarMidnight = event;
                            break;
                        }
                    }
                }
                data.moonnight = (lunarMidnight == null) ? - 1 : lunarMidnight.getTimeInMillis();

                // illumination
                long illuminationAt = lunarNoon != null ? lunarNoon.getTimeInMillis()
                                                        : data.moonrise != -1 ? data.moonrise
                                                        : data.moonset != -1 ? data.moonset : data.date;
                double moonillum = queryIlluminationAt(resolver, illuminationAt);
                data.moonillum = moonillum >= 0 ? moonillum : 0;

                // phase
                HashMap<MoonPhase, Calendar> phases = new HashMap<>(4);
                Calendar midnightBefore = midnight(data.getDate(timezone));
                queryMoonPhases(resolver, data, midnightBefore, phases);
                data.moonnew = phases.get(MoonPhase.NEW).getTimeInMillis();
                data.moonfull = phases.get(MoonPhase.FULL).getTimeInMillis();
                data.moonphase = findPhaseOf(midnightBefore, phases).name();

                long nextNewMoon = phases.get(MoonPhase.NEW).getTimeInMillis();
                long prevNewMoon = queryMoonPhase(resolver, MoonPhase.NEW, (nextNewMoon - AVG_MONTH_MILLIS));
                data.moonperiod = (nextNewMoon - prevNewMoon);

                // minor periods at moonrise and moonset
                if (data.moonrise != -1) {
                    data.minor_periods[0] = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, context.getString(R.string.label_moonrise), data.moonrise, data.moonrise + MINOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                }
                if (data.moonset != -1) {
                    data.minor_periods[1] = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, context.getString(R.string.label_moonset), data.moonset, data.moonset + MINOR_PERIOD_MILLIS,  data.sunrise, data.sunset);
                }

                // major periods at lunar noon and lunar midnight
                int today = data.getDate(timezone).get(Calendar.DAY_OF_YEAR);
                if (lunarNoon != null && lunarNoon.get(Calendar.DAY_OF_YEAR) == today) {
                    data.major_periods[0] = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, context.getString(R.string.label_moonnoon), lunarNoon.getTimeInMillis(), lunarNoon.getTimeInMillis() + MAJOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                }
                if (lunarMidnight != null && lunarMidnight.get(Calendar.DAY_OF_YEAR) == today) {
                    data.major_periods[1] = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, context.getString(R.string.label_moonnight), lunarMidnight.getTimeInMillis(), lunarMidnight.getTimeInMillis() + MAJOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                }

            } catch (SecurityException e) {
                Log.e(getClass().getSimpleName(), "calculateData: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                data.calculated = false;
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private static final String[] PROJECTION_SUNRISESET = new String[] { CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET };
    protected void querySunriseSunset(ContentResolver resolver, SolunarData data)
    {
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + data.getDateMillis() );
        Cursor cursor = resolver.query(uri, PROJECTION_SUNRISESET, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            data.sunrise = cursor.isNull(0) ? -1 : cursor.getLong(0);
            data.sunset = cursor.isNull(1) ? -1 : cursor.getLong(1);
            data.noon = (data.sunrise == -1 || data.sunset == -1) ? -1 : data.sunrise;
            cursor.close();
        }
    }

    private static final String[] PROJECTION_MOONRISESET = new String[] { CalculatorProviderContract.COLUMN_MOON_RISE, CalculatorProviderContract.COLUMN_MOON_SET };
    protected void queryMoonriseMoonset(ContentResolver resolver, SolunarData data, Pair<Calendar,Calendar>[] riseSet)
    {
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/"
                + (data.getDateMillis() - SUN_PERIOD_MILLIS) + "-" + (data.getDateMillis() + SUN_PERIOD_MILLIS) );
        Cursor cursor = resolver.query(uri, PROJECTION_MOONRISESET, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            for (int i=0; i<3; i++) {
                riseSet[i] = new Pair<>(null, null);
            }
            for (int i=0; i<3; i++)
            {
                if (cursor.isAfterLast())
                {
                    Log.w(getClass().getSimpleName(), "queryMoonriseMoonset: cursor contains fewer rows than expected (3); got " + i + ": " + data.getDateMillis());
                    break;
                }
                Calendar rising = null, setting = null;
                if (!cursor.isNull(0)) {
                    rising = Calendar.getInstance();
                    rising.setTimeInMillis(cursor.getLong(0));
                }
                if (!cursor.isNull(1)) {
                    setting = Calendar.getInstance();
                    setting.setTimeInMillis(cursor.getLong(1));
                }
                if (i == 1) {
                    data.moonrise = (rising == null) ? -1 : rising.getTimeInMillis();
                    data.moonset = (setting == null) ? -1 : setting.getTimeInMillis();
                }
                riseSet[i] = new Pair<>(rising, setting);
                cursor.moveToNext();
            }
            cursor.close();
        }
    }

    protected long queryMoonPhase(ContentResolver resolver, MoonPhase phase, long after)
    {
        long eventMillis = -1;
        String[] projection = new String[] { toProviderColumn(phase) };
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + after );
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            eventMillis = (cursor.isNull(0)) ? -1 : cursor.getLong(0);
            cursor.close();

        } else {
            Log.w("queryData", "null cursor! " + uri);
        }
        return eventMillis;
    }

    private static final String[] PROJECTION_MOONPHASES = new String[] { CalculatorProviderContract.COLUMN_MOON_NEW, CalculatorProviderContract.COLUMN_MOON_FIRST, CalculatorProviderContract.COLUMN_MOON_FULL, CalculatorProviderContract.COLUMN_MOON_THIRD };
    protected void queryMoonPhases(ContentResolver resolver, SolunarData data, Calendar after, HashMap<MoonPhase, Calendar> phases)
    {
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPHASE + "/" + after.getTimeInMillis() );
        Cursor cursor = resolver.query(uri, PROJECTION_MOONPHASES, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            MoonPhase[] allPhases = MoonPhase.values();
            for (int i=0; i<allPhases.length; i++)
            {
                if (!cursor.isNull(i))
                {
                    Calendar event = Calendar.getInstance();
                    event.setTimeInMillis(cursor.getLong(i));
                    phases.put(allPhases[i], event);
                } else {
                    Log.w("queryData", "null phase! " + allPhases[i]);
                    phases.put(allPhases[i], null);
                }
            }
            cursor.close();
        } else {
            Log.w("queryData", "null cursor! " + uri);
        }
    }

    private static final String[] PROJECTION_ILLUMINATION = new String[] { CalculatorProviderContract.COLUMN_MOONPOS_ILLUMINATION };
    protected double queryIlluminationAt(ContentResolver resolver, long illuminationAt)
    {
        double retValue = -1;
        Uri uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS + "/" + illuminationAt );
        Cursor cursor = resolver.query(uri, PROJECTION_ILLUMINATION, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            retValue = cursor.isNull(0) ? -1 : cursor.getDouble(0);
            cursor.close();
        }
        return retValue;
    }

    private ArrayList<Calendar> findMidnight( Pair<Calendar,Calendar>[] riseSet )
    {
        ArrayList<Calendar> events = new ArrayList<>();
        for (int i=0; i<riseSet.length; i++)  // for yesterday [0], today [1], and tomorrow [2]
        {
            if (riseSet[i] == null) {
                continue;
            }

            Calendar set = riseSet[i].second;
            if (set != null)                          // check for moonset..
            {
                Calendar rise = riseSet[i].first;
                if (rise != null && rise.after(set))    // check for moonrise same day..
                {
                    events.add(midpoint(set, rise));         // case0: moonset / moonrise same day

                } else if ((i+1) < riseSet.length) {
                    rise = riseSet[i+1].first;
                    if (rise != null)                  // check for moonrise next day..
                    {
                        events.add(midpoint(set, rise));     // case 1: moonset / moonrise straddles next day
                    }
                }
            }
        }
        return events;
    }

    private ArrayList<Calendar> findNoon( Pair<Calendar,Calendar>[] riseSet)
    {
        ArrayList<Calendar> noon = new ArrayList<>();
        for (int i=0; i<riseSet.length; i++)  // for yesterday [0], today [1], and tomorrow [2]
        {
            if (riseSet[i] == null) {
                continue;
            }

            Calendar rise = riseSet[i].first;
            if (rise != null)                          // check for moonrise..
            {
                Calendar set = riseSet[i].second;
                if (set != null && set.after(rise))    // check for moonset same day..
                {
                    noon.add(midpoint(rise, set));         // case0: moonrise / moonset same day

                } else if ((i+1) < riseSet.length) {
                    //Log.d("DEBUG", "i: " + i + " .. " + riseSet[i] + ".." + riseSet[i+1]);
                    set = riseSet[i+1].second;
                    if (set != null)                  // check for moonset next day..
                    {
                        noon.add(midpoint(rise, set));     // case 1: moonrise / moonset straddles next day
                    }
                }
            }
        }
        return noon;
    }

    protected Calendar midpoint(Calendar c1, Calendar c2)
    {
        int midpoint = (int)((c2.getTimeInMillis() - c1.getTimeInMillis()) / 2);   // int: capacity ~24 days
        Calendar retValue = (Calendar)c1.clone();
        retValue.add(Calendar.MILLISECOND, midpoint);
        return retValue;
    }

    protected MoonPhaseDisplay findPhaseOf(Calendar calendar, HashMap<MoonPhase, Calendar> phases)
    {
        MoonPhase nextPhase = nextPhase(calendar, phases);
        return (isSameDay(calendar, phases.get(nextPhase)) ? toPhase(nextPhase) : prevMinorPhase(nextPhase));
    }

    public static boolean isSameDay(Calendar calendar, Calendar otherCalendar)
    {
        int year = calendar.get(Calendar.YEAR);
        int otherYear = otherCalendar.get(Calendar.YEAR);
        int day = calendar.get(Calendar.DAY_OF_YEAR);
        int otherDay = otherCalendar.get(Calendar.DAY_OF_YEAR);
        return (year == otherYear) && (day == otherDay);
    }

    public MoonPhase nextPhase(Calendar calendar, HashMap<MoonPhase, Calendar> phases) {
        return nextPhase(phases, calendar);
    }

    public MoonPhase nextPhase(HashMap<MoonPhase, Calendar> moonPhases, Calendar calendar)
    {
        MoonPhase result = MoonPhase.FULL;
        long date = calendar.getTimeInMillis();

        long least = Long.MAX_VALUE;
        for (MoonPhase phase : moonPhases.keySet())
        {
            Calendar phaseDate = moonPhases.get(phase);
            if (phaseDate != null)
            {
                long delta = phaseDate.getTimeInMillis() - date;
                if (delta >= 0 && delta < least)
                {
                    least = delta;
                    result = phase;
                }
            }
        }
        return result;
    }

    public static String toProviderColumn(MoonPhase input)
    {
        switch (input) {
            case FIRST_QUARTER: return CalculatorProviderContract.COLUMN_MOON_FIRST;
            case THIRD_QUARTER: return CalculatorProviderContract.COLUMN_MOON_THIRD;
            case FULL: return CalculatorProviderContract.COLUMN_MOON_FULL;
            case NEW:
            default: return CalculatorProviderContract.COLUMN_MOON_NEW;
        }
    }

    public static MoonPhaseDisplay toPhase( MoonPhase input )
    {
        switch (input) {
            case NEW: return MoonPhaseDisplay.NEW;
            case FIRST_QUARTER: return MoonPhaseDisplay.FIRST_QUARTER;
            case THIRD_QUARTER: return MoonPhaseDisplay.THIRD_QUARTER;
            case FULL:
            default: return MoonPhaseDisplay.FULL;
        }
    }

    public static MoonPhaseDisplay prevMinorPhase(MoonPhase input)
    {
        switch (input)
        {
            case NEW: return MoonPhaseDisplay.WANING_CRESCENT;
            case FIRST_QUARTER: return MoonPhaseDisplay.WAXING_CRESCENT;
            case THIRD_QUARTER: return MoonPhaseDisplay.WANING_GIBBOUS;
            case FULL:
            default: return MoonPhaseDisplay.WAXING_GIBBOUS;
        }
    }

    public Calendar midnight(Calendar calendar)
    {
        Calendar midnight = null;
        if (calendar != null)
        {
            midnight = (Calendar) calendar.clone();
            midnight.set(Calendar.HOUR_OF_DAY, 0);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
        }
        return midnight;
    }
    public Calendar noon(Calendar calendar)
    {
        Calendar midnight = null;
        if (calendar != null)
        {
            midnight = (Calendar) calendar.clone();
            midnight.set(Calendar.HOUR_OF_DAY, 12);
            midnight.set(Calendar.MINUTE, 0);
            midnight.set(Calendar.SECOND, 0);
        }
        return midnight;
    }


}
