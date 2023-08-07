package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.AUTHORITY;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_ALTITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_DATE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LATITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LOCATION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LONGITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_ILLUMINATION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_PHASE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_RATING;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_SUNRISE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_SUNSET;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_TIMEZONE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.OVERLAP_NONE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.OVERLAP_SUNRISE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.OVERLAP_SUNSET;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG_PROJECTION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SolunarProviderTest
{
    private Context context;

    @Before
    public void setup()
    {
        //context = new RenamingDelegatingContext(InstrumentationRegistry.getTargetContext(), "test_");
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void test_columns()
    {
        List<String> columns = new ArrayList<>();
        Collections.addAll(columns, QUERY_SOLUNAR_PROJECTION);
        Collections.addAll(columns, QUERY_SOLUNAR_CONFIG_PROJECTION);
        test_projectionHasUniqueColumns(columns.toArray(new String[columns.size()]));

        test_query_config_projection();
        test_query_solunar_projection();
    }

    @Test
    public void test_query_config_projection()
    {
        String[] TEST_CONFIG_PROJECTION = new String[] {
                COLUMN_SOLUNAR_CONFIG_APP_VERSION, COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE,
                COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION, COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE
        };

        List<String> projection = Arrays.asList(QUERY_SOLUNAR_CONFIG_PROJECTION);
        for (String column : TEST_CONFIG_PROJECTION) {
            test_projectionContainsColumn(column, projection);
        }
    }

    @Test
    public void test_query_config()
    {
        test_query_config_projection();

        ContentResolver resolver = context.getContentResolver();
        assertTrue("Unable to getContentResolver!", resolver != null);

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR_CONFIG);
        String[] projection = QUERY_SOLUNAR_CONFIG_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        test_cursorHasColumns("QUERY_CONFIG", cursor, projection);
        assertTrue("QUERY_CONFIG should return one row.", cursor.getCount() == 1);

        assertTrue("COLUMN_CONFIG_APP_VERSION should be " + BuildConfig.VERSION_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_CONFIG_APP_VERSION)).startsWith(BuildConfig.VERSION_NAME));
        assertTrue("COLUMN_CONFIG_APP_VERSION_CODE should be " + BuildConfig.VERSION_CODE,cursor.getInt(cursor.getColumnIndex(COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE)) == BuildConfig.VERSION_CODE);
        assertTrue("COLUMN_CONFIG_PROVIDER_VERSION should be " + SolunarProviderContract.VERSION_NAME, cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION)).equals(SolunarProviderContract.VERSION_NAME));
        assertTrue("COLUMN_CONFIG_PROVIDER_VERSION_CODE should be " +  SolunarProviderContract.VERSION_CODE,cursor.getInt(cursor.getColumnIndex(COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE)) == SolunarProviderContract.VERSION_CODE);

        cursor.close();
    }

    @Test
    public void test_query_solunar_projection()
    {
        String[] TEST_SOLUNAR_PROJECTION = new String[] {
                COLUMN_SOLUNAR_DATE, COLUMN_SOLUNAR_RATING,
                COLUMN_SOLUNAR_SUNRISE, COLUMN_SOLUNAR_SUNSET,
                COLUMN_SOLUNAR_MOON_ILLUMINATION, COLUMN_SOLUNAR_MOON_PHASE,
                COLUMN_SOLUNAR_PERIOD_MOONRISE, COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP,
                COLUMN_SOLUNAR_PERIOD_MOONSET, COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP,
                COLUMN_SOLUNAR_PERIOD_MOONNOON, COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP,
                COLUMN_SOLUNAR_PERIOD_MOONNIGHT, COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP,
                COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH, COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH,
                COLUMN_SOLUNAR_LOCATION, COLUMN_SOLUNAR_LATITUDE, COLUMN_SOLUNAR_LONGITUDE, COLUMN_SOLUNAR_ALTITUDE, COLUMN_SOLUNAR_TIMEZONE
        };

        List<String> projection = Arrays.asList(QUERY_SOLUNAR_PROJECTION);
        for (String column : TEST_SOLUNAR_PROJECTION) {
            test_projectionContainsColumn(column, projection);
        }
    }

    @Test
    public void test_query_solunar()
    {
        test_query_solunar_today();
        test_query_solunar_date();
        test_query_solunar_range();
    }

    @Test
    public void test_query_solunar_date()
    {
        test_query_solunar_projection();

        SolunarCalculator calculator = new SolunarCalculator();
        Context context = InstrumentationRegistry.getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        assertNotNull(resolver);

        SuntimesInfo config = SuntimesInfo.queryInfo(context);
        double latitude = Double.parseDouble(config.location[1]);
        double longitude = Double.parseDouble(config.location[2]);
        double altitude = Double.parseDouble(config.location[3]);
        String timezone = config.timezone;

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        date.set(Calendar.MONTH, 3);
        date.set(Calendar.DAY_OF_MONTH, 11);
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        int n = 30;
        Calendar date1 = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        date1.setTimeInMillis(date.getTimeInMillis());
        date1.add(Calendar.DATE, n);

        SolunarData oracle = new SolunarData(date.getTimeInMillis(), latitude, longitude, altitude, timezone);
        calculator.calculateData(resolver, oracle);
        test_solunar_data(oracle, date.getTimeInMillis(), latitude, longitude, altitude, timezone);

        SolunarPeriod period = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, oracle.moonnoon, oracle.moonnoon + SolunarCalculator.MAJOR_PERIOD_MILLIS, oracle.sunrise, oracle.sunset);
        assertTrue("moonnoon_overlap expected at sunrise!", period.occursAtSunrise());

        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR + "/" + date.getTimeInMillis());
        String[] projection1 = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor = resolver.query(uri1, projection1, null, null, null);
        assertNotNull(cursor);
        cursor.moveToFirst();

        test_cursorHasColumns("QUERY_SOLUNAR", cursor, projection1);
        assertEquals("QUERY_CONFIG should return 1 row.", 1, cursor.getCount());
        test_solunar(cursor, resolver, oracle, date.getTimeInMillis(), latitude, longitude, altitude, timezone);

        int moonnoon_overlap = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP));
        long sunrise1 = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_SUNRISE));
        assertEquals("sunrise times should match", sunrise1, oracle.sunrise);
        assertEquals("moonnoon overlap should match SUNRISE(" + OVERLAP_SUNRISE + ") != " + moonnoon_overlap, moonnoon_overlap, OVERLAP_SUNRISE);

        cursor.close();
    }


    @Test
    public void test_query_solunar_today()
    {
        test_query_solunar_projection();

        SolunarCalculator calculator = new SolunarCalculator();
        Context context = InstrumentationRegistry.getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        assertNotNull(resolver);

        SuntimesInfo config = SuntimesInfo.queryInfo(context);
        double latitude = Double.parseDouble(config.location[1]);
        double longitude = Double.parseDouble(config.location[2]);
        double altitude = Double.parseDouble(config.location[3]);
        String timezone = config.timezone;

        // case 0
        long date0 = Calendar.getInstance().getTimeInMillis();
        SolunarData oracle0 = new SolunarData(date0, latitude, longitude, altitude, timezone);
        calculator.calculateData(resolver, oracle0);
        test_solunar_data(oracle0, date0, latitude, longitude, altitude, timezone);

        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR);
        String[] projection = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        assertNotNull(cursor);
        cursor.moveToFirst();
        test_cursorHasColumns("QUERY_SOLUNAR", cursor, projection);
        assertEquals("QUERY_CONFIG should return 1 row.", 1, cursor.getCount());
        test_solunar(cursor, resolver, oracle0, Calendar.getInstance().getTimeInMillis(), latitude, longitude, altitude, timezone);
        cursor.close();
    }


    @Test
    public void test_query_solunar_range()
    {
        test_query_solunar_projection();

        Context context = InstrumentationRegistry.getTargetContext();
        ContentResolver resolver = context.getContentResolver();
        assertNotNull(resolver);

        SuntimesInfo config = SuntimesInfo.queryInfo(context);
        String timezone = config.timezone;

        Calendar date = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        date.set(Calendar.MONTH, 3);
        date.set(Calendar.DAY_OF_MONTH, 11);
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        int n = 30;
        Calendar date1 = Calendar.getInstance(TimeZone.getTimeZone(timezone));
        date1.setTimeInMillis(date.getTimeInMillis());
        date1.add(Calendar.DATE, n);

        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR + "/" + date.getTimeInMillis() + "-" + date1.getTimeInMillis());
        String[] projection2 = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        assertNotNull(cursor2);
        cursor2.moveToFirst();
        test_cursorHasColumns("QUERY_SOLUNAR", cursor2, projection2);
        assertEquals("QUERY_CONFIG should return " + n + " rows, has " + cursor2.getCount(), cursor2.getCount(), (n + 1));
        // TODO
        cursor2.close();
    }

    private static void test_solunar_data(SolunarData data, long date, double latitude, double longitude, double altitude, String timezone)
    {
        assertEquals("date should match", data.getDateMillis(), date);
        assertEquals("latitude should match", data.getLatitude(), latitude, 0.0);
        assertEquals("longitude should match", data.getLongitude(), longitude, 0.0);
        assertEquals("altitude should match", data.getAltitude(), altitude, 0.0);
        //assertEquals("timezone should match", data.getTimezone(), timezone);
    }

    private static void test_solunar(Cursor cursor, ContentResolver resolver, SolunarData oracle, long date, double latitude, double longitude, double altitude, String timezone)
    {
        assertNotNull(cursor);

        String latitude1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_LATITUDE));
        assertTrue("latitude should match .. " + latitude + " != " + latitude1, latitude == Double.parseDouble(latitude1));

        String longitude1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_LONGITUDE));
        assertTrue("longitude should match .. " + longitude + " != " + longitude1, longitude == Double.parseDouble(longitude1));

        String altitude1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_ALTITUDE));
        assertTrue("altitude should match .. " + altitude + " != " + altitude1, altitude == Double.parseDouble(altitude1));

        String timezone1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_TIMEZONE));
        assertTrue("timezone should match .. " + timezone + " != " + timezone1, timezone.equals(timezone1));

        long sunrise = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_SUNRISE));
        assertTrue("sunrise time should match .. " + sunrise + " != " + oracle.sunrise, sunrise == oracle.sunrise);

        long sunset = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_SUNSET));
        assertTrue("sunset time should match .. " + sunset + " != " + oracle.sunset, sunset == oracle.sunset);

        long moonrise = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONRISE));
        assertTrue("moonrise time should match .. " + moonrise + " != " + oracle.moonrise, moonrise == oracle.moonrise);
        int moonrise_overlap = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP));
        int moonrise_overlap_oracle = (oracle.getMinorPeriods()[0].occursAtSunrise() ? OVERLAP_SUNRISE : (oracle.getMinorPeriods()[0].occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
        assertTrue("moonrise overlap should match .. " + moonrise_overlap + " != " + moonrise_overlap_oracle, moonrise_overlap == moonrise_overlap_oracle);

        long moonset = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONSET));
        assertTrue("moonset time should match .. " + moonset + " != " + oracle.moonset, moonset == oracle.moonset);
        int moonset_overlap = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP));
        int moonset_overlap_oracle = (oracle.getMinorPeriods()[1].occursAtSunrise() ? OVERLAP_SUNRISE : (oracle.getMinorPeriods()[1].occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
        assertTrue("moonset overlap should match .. " + moonset_overlap + " != " + moonset_overlap_oracle, moonrise_overlap == moonset_overlap_oracle);

        long moonnoon = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONNOON));
        assertTrue("lunar noon time should match .. " + moonnoon + " != " + oracle.moonnoon, moonnoon == oracle.moonnoon);
        int moonnoon_overlap = cursor.getInt(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP));
        int moonnoon_overlap_oracle = (oracle.getMajorPeriods()[0].occursAtSunrise() ? OVERLAP_SUNRISE : (oracle.getMajorPeriods()[0].occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
        assertTrue("moonnoon overlap should match .. " + moonnoon_overlap + " != " + moonnoon_overlap_oracle, moonnoon_overlap == moonnoon_overlap_oracle);

        long moonnight = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MOONNIGHT));
        assertTrue("lunar midnight time should match .. " + moonnight + " != " + oracle.moonnight, moonnight == oracle.moonnight);
        int moonnight_overlap = cursor.getInt(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP));
        int moonnight_overlap_oracle = (oracle.getMajorPeriods()[1].occursAtSunrise() ? OVERLAP_SUNRISE : (oracle.getMajorPeriods()[1].occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
        assertTrue("moonnight overlap should match .. " + moonnight_overlap + " != " + moonnight_overlap_oracle, moonnight_overlap == moonnight_overlap_oracle);

        double moonillum = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_MOON_ILLUMINATION));
        assertTrue("moon illumination should match .. " + moonillum + " != " + oracle.moonillum, moonillum == oracle.moonillum);

        double rating = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_RATING));
        assertTrue("rating should match .. " + rating + " != " + oracle.getDayRating(), rating == oracle.getDayRating());

        long major_length = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH));
        assertTrue("major length should match .. " + major_length + " != " + SolunarCalculator.MAJOR_PERIOD_MILLIS, major_length == SolunarCalculator.MAJOR_PERIOD_MILLIS);

        long minor_length = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH));
        assertTrue("minor length should match .. " + minor_length + " != " + SolunarCalculator.MINOR_PERIOD_MILLIS, minor_length == SolunarCalculator.MINOR_PERIOD_MILLIS);

        // TODO
        // long date1 = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_DATE));
        // assertTrue("date should match .. " + date1 + " != " + date, date1 == date);
    }


    private void test_projectionContainsColumn(String column, List<String> projection) {
        assertTrue("projection contains " + column, projection.contains(column));
    }

    private void test_projectionHasUniqueColumns(String[] projection)
    {
        Set<String> uniqueColumns = new HashSet<>();
        for (String column : projection) {
            assertTrue("Column names are not unique! \"" + column + "\" is used more than once.", !uniqueColumns.contains(column));
            uniqueColumns.add(column);
        }
    }

    private void test_cursorHasColumns(@NonNull String tag, @Nullable Cursor cursor, @NonNull String[] projection)
    {
        assertTrue(tag + " should return non-null cursor.", cursor != null);
        assertTrue(tag + " should have same number of columns as the projection", cursor.getColumnCount() == projection.length);
        assertTrue(tag + " should return one or more rows.", cursor.getCount() >= 1);
        cursor.moveToFirst();
        for (String column : projection) {
            assertTrue(tag + " results should contain " + column, cursor.getColumnIndex(column) >= 0);
        }
    }
}
