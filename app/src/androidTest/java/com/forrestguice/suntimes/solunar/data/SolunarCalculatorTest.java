package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.TimeZone;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SolunarCalculatorTest
{
    @Test
    public void test_solunarCalculator()
    {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        assertNotNull(resolver);

        double latitude = 35;
        double longitude = -112;
        double altitude = 0;
        String timezone = "US/Arizona";
        TimeZone tz = TimeZone.getTimeZone(timezone);

        Calendar date_11 = Calendar.getInstance(tz);
        date_11.set(Calendar.MONTH, 3);
        date_11.set(Calendar.DAY_OF_MONTH, 11);
        date_11.set(Calendar.HOUR_OF_DAY, 12);
        date_11.set(Calendar.MINUTE, 0);
        date_11.set(Calendar.SECOND, 0);

        SolunarData data_11 = new SolunarData(date_11.getTimeInMillis(), "test", latitude, longitude, altitude);
        SolunarCalculator calculator = new SolunarCalculator();
        calculator.calculateData(context, resolver, data_11, tz);
        Log.d("TEST", "moonrise 11: " + data_11.moonrise);
        Log.d("TEST", "moonset 11: " + data_11.moonset);

        Calendar date_12 = Calendar.getInstance(tz);
        date_12.set(Calendar.MONTH, 3);
        date_12.set(Calendar.DAY_OF_MONTH, 12);
        date_12.set(Calendar.HOUR_OF_DAY, 12);
        date_12.set(Calendar.MINUTE, 0);
        date_12.set(Calendar.SECOND, 0);

        SolunarData data_12 = new SolunarData(date_12.getTimeInMillis(), "test", latitude, longitude, altitude);
        calculator.calculateData(context, resolver, data_12, tz);
        Log.d("TEST", "moonrise 12: " + data_12.moonrise);
        Log.d("TEST", "moonset 12: " + data_12.moonset);
    }
}
