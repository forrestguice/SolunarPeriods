package com.forrestguice.suntimes.solunar.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

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
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG_PROJECTION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;
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

        // case 0
        Uri uri = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR);
        String[] projection = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor = resolver.query(uri, projection, null, null, null);
        cursor.moveToFirst();
        test_cursorHasColumns("QUERY_SOLUNAR", cursor, projection);
        assertTrue("QUERY_CONFIG should return 1 row.", cursor.getCount() == 1);
        test_solunar(cursor, resolver, calculator, Calendar.getInstance().getTimeInMillis(), latitude, longitude, altitude, timezone);
        cursor.close();

        // case 1: date
        Uri uri1 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR + "/" + date.getTimeInMillis());
        String[] projection1 = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor1 = resolver.query(uri1, projection1, null, null, null);
        cursor1.moveToFirst();
        test_cursorHasColumns("QUERY_SOLUNAR", cursor1, projection1);
        assertTrue("QUERY_CONFIG should return 1 row.", cursor.getCount() == 1);
        test_solunar(cursor1, resolver, calculator, date.getTimeInMillis(), latitude, longitude, altitude, timezone);
        cursor1.close();

        // case 2: range
        Uri uri2 = Uri.parse("content://" + AUTHORITY + "/" + QUERY_SOLUNAR + "/" + date.getTimeInMillis() + "-" + date1.getTimeInMillis());
        String[] projection2 = QUERY_SOLUNAR_PROJECTION;
        Cursor cursor2 = resolver.query(uri2, projection2, null, null, null);
        cursor2.moveToFirst();
        test_cursorHasColumns("QUERY_SOLUNAR", cursor2, projection2);
        assertTrue("QUERY_CONFIG should return " + n + " rows, has " + cursor2.getCount(), cursor2.getCount() == (n + 1));
        // TODO
        cursor2.close();
    }

    private void test_solunar(Cursor cursor, ContentResolver resolver, SolunarCalculator calculator, long date, double latitude, double longitude, double altitude, String timezone)
    {
        SolunarData oracle = new SolunarData(date, latitude, longitude, altitude, timezone);
        calculator.calculateData(resolver, oracle);

        if (cursor != null)
        {
            String latitude1 = cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_LATITUDE));
            assertTrue("latitude should match .. " + latitude + " != " + latitude1, latitude == Double.parseDouble(latitude1));

            String longitude1 = cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_LONGITUDE));
            assertTrue("longitude should match .. " + longitude + " != " + longitude1, longitude == Double.parseDouble(longitude1));

            String altitude1 = cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_ALTITUDE));
            assertTrue("altitude should match .. " + altitude + " != " + altitude1, altitude == Double.parseDouble(altitude1));

            String timezone1 = cursor.getString(cursor.getColumnIndex(COLUMN_SOLUNAR_TIMEZONE));
            assertTrue("timezone should match .. " + timezone + " != " + timezone1, timezone.equals(timezone1));

            long sunrise = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_SUNRISE));
            assertTrue("sunrise time should match .. " + sunrise + " != " + oracle.sunrise, sunrise == oracle.sunrise);

            long sunset = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_SUNSET));
            assertTrue("sunset time should match .. " + sunset + " != " + oracle.sunset, sunset == oracle.sunset);

            long moonrise = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONRISE));
            assertTrue("moonrise time should match .. " + moonrise + " != " + oracle.moonrise, moonrise == oracle.moonrise);

            long moonset = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONSET));
            assertTrue("moonset time should match .. " + moonset + " != " + oracle.moonset, moonset == oracle.moonset);

            long moonnoon = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONNOON));
            assertTrue("lunar noon time should match .. " + moonnoon + " != " + oracle.moonnoon, moonnoon == oracle.moonnoon);

            long moonnight = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MOONNIGHT));
            assertTrue("lunar midnight time should match .. " + moonnight + " != " + oracle.moonnight, moonnight == oracle.moonnight);

            double moonillum = cursor.getDouble(cursor.getColumnIndex(COLUMN_SOLUNAR_MOON_ILLUMINATION));
            assertTrue("moon illumination should match .. " + moonillum + " != " + oracle.moonillum, moonillum == oracle.moonillum);

            double rating = cursor.getDouble(cursor.getColumnIndex(COLUMN_SOLUNAR_RATING));
            assertTrue("rating should match .. " + rating + " != " + oracle.getDayRating(), rating == oracle.getDayRating());

            long major_length = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH));
            assertTrue("major length should match .. " + major_length + " != " + SolunarCalculator.MAJOR_PERIOD_MILLIS, major_length == SolunarCalculator.MAJOR_PERIOD_MILLIS);

            long minor_length = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH));
            assertTrue("minor length should match .. " + minor_length + " != " + SolunarCalculator.MINOR_PERIOD_MILLIS, minor_length == SolunarCalculator.MINOR_PERIOD_MILLIS);

            // TODO
            // long date1 = cursor.getLong(cursor.getColumnIndex(COLUMN_SOLUNAR_DATE));
            // assertTrue("date should match .. " + date1 + " != " + date, date1 == date);
        }
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
