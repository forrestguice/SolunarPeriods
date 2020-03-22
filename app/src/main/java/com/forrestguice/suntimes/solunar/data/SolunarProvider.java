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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.BuildConfig;

import java.util.Calendar;
import java.util.TimeZone;

import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.AUTHORITY;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_ALTITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_LATITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_LOCATION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_LONGITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_MAJOR_LENGTH;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_MINOR_LENGTH;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_DATE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_ILLUMINATION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_NIGHT;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_NOON;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_RISE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_MOON_SET;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_RATING;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_SUNRISE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_SUNSET;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;

public class SolunarProvider extends ContentProvider
{
    private static final int URIMATCH_CONFIG = 0;
    private static final int URIMATCH_SOLUNAR = 10;
    private static final int URIMATCH_SOLUNAR_FOR_DATE = 20;
    private static final int URIMATCH_SOLUNAR_FOR_RANGE = 30;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR_CONFIG, URIMATCH_CONFIG);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR, URIMATCH_SOLUNAR);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/#", URIMATCH_SOLUNAR_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/*", URIMATCH_SOLUNAR_FOR_RANGE);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        Cursor cursor = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_CONFIG:
                Log.e(getClass().getSimpleName(), "URIMATCH_CONFIG");
                cursor = querySolunarConfig(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR:
                Log.e(getClass().getSimpleName(), "URIMATCH_SOLUNAR");
                long now = Calendar.getInstance().getTimeInMillis();
                cursor = querySolunar(new long[] {now, now}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR_FOR_DATE:
                Log.e(getClass().getSimpleName(), "URIMATCH_SOLUNAR_FOR_DATE");
                long date = ContentUris.parseId(uri);
                cursor = querySolunar(new long[] {date, date}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR_FOR_RANGE:
                Log.e(getClass().getSimpleName(), "URIMATCH_SOLUNAR_FOR_RANGE");
                long[] range = parseDateRange(uri.getLastPathSegment());
                cursor = querySolunar(range, uri, projection, selection, selectionArgs, sortOrder);
                break;

            default:
                Log.e(getClass().getSimpleName(), "Unrecognized URI! " + uri);
                break;
        }
        return cursor;
    }

    /**
     * querySolunar
     */
    public Cursor querySolunar(long[] range, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SOLUNAR_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            ContentResolver resolver = context.getContentResolver();
            SuntimesInfo config = SuntimesInfo.queryInfo(context);
            double latitude = Double.parseDouble(config.location[1]);
            double longitude = Double.parseDouble(config.location[2]);
            double altitude = Double.parseDouble(config.location[3]);

            Calendar day = Calendar.getInstance(TimeZone.getTimeZone(config.timezone));
            Calendar endDay = Calendar.getInstance(TimeZone.getTimeZone(config.timezone));
            day.setTimeInMillis(range[0]);
            endDay.setTimeInMillis(range[1] + 1000);      // +1000ms (make range[1] inclusive)

            SolunarCalculator calculator = new SolunarCalculator();
            do {
                Calendar calendar;
                SolunarData data = null;
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_SOLUNAR_SUNRISE:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.sunrise;
                            break;

                        case COLUMN_SOLUNAR_SUNSET:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.sunset;
                            break;

                        case COLUMN_SOLUNAR_MOON_RISE:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonrise;
                            break;

                        case COLUMN_SOLUNAR_MOON_SET:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonset;
                            break;

                        case COLUMN_SOLUNAR_MOON_NOON:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonnoon;
                            break;

                        case COLUMN_SOLUNAR_MOON_NIGHT:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonnight;
                            break;

                        case COLUMN_SOLUNAR_MOON_ILLUMINATION:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.getMoonIllumination();
                            break;

                        case COLUMN_SOLUNAR_RATING:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.getDayRating();
                            break;

                        case COLUMN_SOLUNAR_DATE:
                            row[i] = day.getTimeInMillis();
                            break;

                        default:
                            row[i] = null;
                            break;
                    }
                }
                cursor.addRow(row);
                day.add(Calendar.DAY_OF_YEAR, 1);
            } while (day.before(endDay));

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }


    /**
     * querySolnarConfig
     */
    public Cursor querySolunarConfig(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SOLUNAR_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            SuntimesInfo config = SuntimesInfo.queryInfo(context);
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION:
                        row[i] = SolunarProviderContract.VERSION_NAME;
                        break;

                    case COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE:
                        row[i] = SolunarProviderContract.VERSION_CODE;
                        break;

                    case COLUMN_SOLUNAR_CONFIG_APP_VERSION:
                        row[i] = BuildConfig.VERSION_NAME + (BuildConfig.DEBUG ? " [" + BuildConfig.BUILD_TYPE + "]" : "");
                        break;

                    case COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE:
                        row[i] = BuildConfig.VERSION_CODE;
                        break;

                    case COLUMN_SOLUNAR_CONFIG_LOCATION:
                        row[i] = config.location[0];
                        break;

                    case COLUMN_SOLUNAR_CONFIG_LATITUDE:
                        row[i] = Double.parseDouble(config.location[1]);
                        break;

                    case COLUMN_SOLUNAR_CONFIG_LONGITUDE:
                        row[i] = Double.parseDouble(config.location[2]);
                        break;

                    case COLUMN_SOLUNAR_CONFIG_ALTITUDE:
                        row[i] = Double.parseDouble(config.location[3]);
                        break;

                    case COLUMN_SOLUNAR_CONFIG_MAJOR_LENGTH:
                        row[i] = SolunarCalculator.MAJOR_PERIOD_MILLIS;  // TODO: configurable
                        break;

                    case COLUMN_SOLUNAR_CONFIG_MINOR_LENGTH:
                        row[i] = SolunarCalculator.MINOR_PERIOD_MILLIS;  // TODO: configurable
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    /**
     * initData
     */
    private SolunarData initData(long dateMillis, ContentResolver resolver, SolunarCalculator calculator, SolunarData data, double latitude, double longitude, double altitude, String timezone)
    {
        if (data == null) {
            data = new SolunarData(dateMillis, latitude, longitude, altitude, timezone);
            calculator.calculateData(resolver, data);
        }
        return data;
    }

    /**
     * parseDateRange
     */
    public static long[] parseDateRange(@Nullable String rangeSegment)
    {
        long[] retValue = new long[2];
        String[] rangeString = ((rangeSegment != null) ? rangeSegment.split("-") : new String[0]);
        if (rangeString.length == 2)
        {
            try {
                retValue[0] = Long.parseLong(rangeString[0]);
                retValue[1] = Long.parseLong(rangeString[1]);

            } catch (NumberFormatException e) {
                Log.w("parseDateRange", "Invalid range! " + rangeSegment);
                retValue[0] = retValue[1] = Calendar.getInstance().getTimeInMillis();
            }
        } else {
            Log.w("parseDateRange", "Invalid range! " + rangeSegment);
            retValue[0] = retValue[1] = Calendar.getInstance().getTimeInMillis();
        }
        return retValue;
    }
}
