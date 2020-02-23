package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;

import java.util.Calendar;

/**
 * SolunarCalculator
 */
public class SolunarCalculator
{
    public static final long MINOR_PERIOD_MILLIS = 1 * 60 * 60 * 1000;       // 1 hr
    public static final long MAJOR_PERIOD_MILLIS = 2 * 60 * 60 * 1000;       // 2 hr
    public static final long MOON_PERIOD_MILLIS = (24 * 60 * 60 * 1000 +
                                                       50 * 60 * 1000);      // 24 hr 50 m

    public boolean calculateData(ContentResolver resolver, @NonNull SolunarData data)
    {
        if (resolver != null)
        {
            try
            {
                // query sunrise / sunset
                String[] sun_projection = new String[] { CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET };
                Uri sun_uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN + "/" + data.getDateMillis() );
                Cursor sun_cursor = resolver.query(sun_uri, sun_projection, null, null, null);
                if (sun_cursor != null)
                {
                    sun_cursor.moveToFirst();
                    data.sunrise = sun_cursor.isNull(0) ? -1 : sun_cursor.getLong(0);
                    data.sunset = sun_cursor.isNull(1) ? -1 : sun_cursor.getLong(1);
                    sun_cursor.close();
                }

                // query moonrise / moonset
                String[] moon_projection0 = new String[] { CalculatorProviderContract.COLUMN_MOON_RISE, CalculatorProviderContract.COLUMN_MOON_SET };
                Uri moon_uri0 = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON + "/" + data.getDateMillis() );
                Cursor moon_cursor0 = resolver.query(moon_uri0, moon_projection0, null, null, null);
                if (moon_cursor0 != null)
                {
                    moon_cursor0.moveToFirst();
                    data.moonrise = moon_cursor0.isNull(0) ? -1 : moon_cursor0.getLong(0);
                    data.moonset = moon_cursor0.isNull(1) ? -1 : moon_cursor0.getLong(1);
                    moon_cursor0.close();
                }

                if (data.moonrise != -1) {
                    data.minor_periods[0] = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, data.moonrise, data.moonrise + MINOR_PERIOD_MILLIS);
                }
                if (data.moonset != -1) {
                    data.minor_periods[1] = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, data.moonset, data.moonset + MINOR_PERIOD_MILLIS);
                }


                if (data.moonrise != -1 && data.moonset != -1)
                {
                    long lunarDay, lunarNight;
                    Calendar lunarNoon = Calendar.getInstance(), lunarMidnight = Calendar.getInstance();

                    if (data.moonset > data.moonrise)
                    {
                        lunarDay = (data.moonset - data.moonrise);
                        lunarNight = (MOON_PERIOD_MILLIS - lunarDay);

                    } else {
                        lunarNight = data.moonrise - data.moonset;
                        lunarDay = MOON_PERIOD_MILLIS - lunarNight;
                    }

                    lunarNoon.setTimeInMillis(data.moonrise + (lunarDay / 2L));
                    lunarMidnight.setTimeInMillis(data.moonrise - (lunarNight / 2L));

                    int today = data.getDate(null).get(Calendar.DAY_OF_YEAR);

                    if (lunarNoon.get(Calendar.DAY_OF_YEAR) == today) {
                        data.major_periods[0] = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, lunarNoon.getTimeInMillis(), lunarNoon.getTimeInMillis() + MAJOR_PERIOD_MILLIS);
                    }
                    if (lunarMidnight.get(Calendar.DAY_OF_YEAR) == today) {
                        data.major_periods[1] = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, lunarMidnight.getTimeInMillis(), lunarMidnight.getTimeInMillis() + MAJOR_PERIOD_MILLIS);
                    }

                } // else  // TODO: edge cases

                // query moon illumination
                String[] moon_projection1 = new String[] { CalculatorProviderContract.COLUMN_MOONPOS_ILLUMINATION };
                Uri moon_uri1 = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS + "/" + data.getDateMillis() );
                Cursor moon_cursor1 = resolver.query(moon_uri1, moon_projection1, null, null, null);
                if (moon_cursor1 != null)
                {
                    moon_cursor1.moveToFirst();
                    data.moonillum = moon_cursor1.getDouble(0);
                    moon_cursor1.close();
                }



                data.dayRating = .5;  // TODO
                data.calculated = true;

            } catch (SecurityException e) {
                Log.e(getClass().getSimpleName(), "calculateData: Unable to access " + CalculatorProviderContract.AUTHORITY + "! " + e);
                data.calculated = false;
            }
        }
        return data.isCalculated();
    }





}
