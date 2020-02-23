package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;

/**
 * SolunarCalculator
 */
public class SolunarCalculator
{
    public static final long MINOR_PERIOD_MILLIS = 2 * 60 * 60 * 1000;
    public static final long MAJOR_PERIOD_MILLIS = 3 * 60 * 60 * 1000;

    public boolean calculateData(ContentResolver resolver, @NonNull SolunarData data)
    {
        if (resolver != null)
        {
            try
            {
                // query sunrise / sunset
                String[] sun_projection = new String[] { CalculatorProviderContract.COLUMN_SUN_ACTUAL_RISE, CalculatorProviderContract.COLUMN_SUN_ACTUAL_SET };
                Uri sun_uri = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_SUN );
                Cursor sun_cursor = resolver.query(sun_uri, sun_projection, null, null, null);
                if (sun_cursor != null)
                {
                    sun_cursor.moveToFirst();
                    data.sunrise = sun_cursor.getLong(0);
                    data.sunset = sun_cursor.getLong(1);
                    sun_cursor.close();
                }

                // query moonrise / moonset
                String[] moon_projection0 = new String[] { CalculatorProviderContract.COLUMN_MOON_RISE, CalculatorProviderContract.COLUMN_MOON_SET };
                Uri moon_uri0 = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOON );
                Cursor moon_cursor0 = resolver.query(moon_uri0, moon_projection0, null, null, null);
                if (moon_cursor0 != null)
                {
                    moon_cursor0.moveToFirst();
                    data.moonrise = moon_cursor0.getLong(0);
                    data.moonset = moon_cursor0.getLong(1);
                    moon_cursor0.close();
                }

                // query moon illumination
                String[] moon_projection1 = new String[] { CalculatorProviderContract.COLUMN_MOONPOS_ILLUMINATION };
                Uri moon_uri1 = Uri.parse("content://" + CalculatorProviderContract.AUTHORITY + "/" + CalculatorProviderContract.QUERY_MOONPOS );
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
