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
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.BuildConfig;
import com.forrestguice.suntimes.solunar.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.AUTHORITY;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_CALENDAR_NAME;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_CALENDAR_SUMMARY;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_CALENDAR_TITLE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_ALTITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LATITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LOCATION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_LONGITUDE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.COLUMN_SOLUNAR_DATE;
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
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_CALENDAR_CONTENT;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_CALENDAR_CONTENT_PROJECTION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_CALENDAR_INFO;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_CALENDAR_INFO_PROJECTION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_CONFIG_PROJECTION;
import static com.forrestguice.suntimes.solunar.data.SolunarProviderContract.QUERY_SOLUNAR_PROJECTION;

public class SolunarProvider extends ContentProvider
{
    private static final int URIMATCH_CONFIG = 0;
    private static final int URIMATCH_SOLUNAR = 10;
    private static final int URIMATCH_SOLUNAR_FOR_DATE = 20;
    private static final int URIMATCH_SOLUNAR_FOR_RANGE = 30;
    private static final int URIMATCH_CALENDAR_INFO = 40;
    private static final int URIMATCH_CALENDAR_CONTENT_FOR_RANGE = 50;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR_CONFIG, URIMATCH_CONFIG);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR, URIMATCH_SOLUNAR);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/#", URIMATCH_SOLUNAR_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/*", URIMATCH_SOLUNAR_FOR_RANGE);
        uriMatcher.addURI(AUTHORITY, QUERY_CALENDAR_INFO, URIMATCH_CALENDAR_INFO);
        uriMatcher.addURI(AUTHORITY, QUERY_CALENDAR_CONTENT + "/*", URIMATCH_CALENDAR_CONTENT_FOR_RANGE);
    }

    public static final String CALENDAR_NAME = "solunarCalendar";

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
        long[] range;
        Cursor cursor = null;
        int uriMatch = uriMatcher.match(uri);
        switch (uriMatch)
        {
            case URIMATCH_CALENDAR_INFO:
                Log.i(getClass().getSimpleName(), "URIMATCH_CALENDAR_INFO");
                cursor = queryCalendarInfo(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_CALENDAR_CONTENT_FOR_RANGE:
                Log.i(getClass().getSimpleName(), "URIMATCH_CALENDAR_CONTENT");
                range = parseDateRange(uri.getLastPathSegment());
                cursor = queryCalendarContent(range, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_CONFIG:
                Log.i(getClass().getSimpleName(), "URIMATCH_CONFIG");
                cursor = querySolunarConfig(uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR:
                Log.i(getClass().getSimpleName(), "URIMATCH_SOLUNAR");
                long now = Calendar.getInstance().getTimeInMillis();
                cursor = querySolunar(new long[] {now, now}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR_FOR_DATE:
                Log.i(getClass().getSimpleName(), "URIMATCH_SOLUNAR_FOR_DATE");
                long date = ContentUris.parseId(uri);
                cursor = querySolunar(new long[] {date, date}, uri, projection, selection, selectionArgs, sortOrder);
                break;

            case URIMATCH_SOLUNAR_FOR_RANGE:
                Log.i(getClass().getSimpleName(), "URIMATCH_SOLUNAR_FOR_RANGE");
                range = parseDateRange(uri.getLastPathSegment());
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
                SolunarPeriod period;
                Calendar calendar;
                SolunarData data = null;
                Object[] row = new Object[columns.length];
                for (int i=0; i<columns.length; i++)
                {
                    switch (columns[i])
                    {
                        case COLUMN_SOLUNAR_LOCATION:
                            row[i] = config.location[0];
                            break;

                        case COLUMN_SOLUNAR_LATITUDE:
                            row[i] = Double.parseDouble(config.location[1]);
                            break;

                        case COLUMN_SOLUNAR_LONGITUDE:
                            row[i] = Double.parseDouble(config.location[2]);
                            break;

                        case COLUMN_SOLUNAR_ALTITUDE:
                            row[i] = Double.parseDouble(config.location[3]);
                            break;

                        case COLUMN_SOLUNAR_TIMEZONE:
                            row[i] = config.timezone;
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH:
                            row[i] = SolunarCalculator.MAJOR_PERIOD_MILLIS;  // TODO: configurable
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH:
                            row[i] = SolunarCalculator.MINOR_PERIOD_MILLIS;  // TODO: configurable
                            break;

                        case COLUMN_SOLUNAR_SUNRISE:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.sunrise;
                            break;
                        case COLUMN_SOLUNAR_SUNSET:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.sunset;
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONRISE:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonrise;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONSET:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonset;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONNOON:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonnoon;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONNIGHT:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.moonnight;
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, data.moonrise, data.moonrise + SolunarCalculator.MINOR_PERIOD_MILLIS, data.getTimezone(), data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, data.moonset, data.moonset + SolunarCalculator.MINOR_PERIOD_MILLIS, data.getTimezone(), data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, data.moonnoon, data.moonnoon + SolunarCalculator.MAJOR_PERIOD_MILLIS, data.getTimezone(), data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, data.moonnight, data.moonnight + SolunarCalculator.MAJOR_PERIOD_MILLIS, data.getTimezone(), data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_MOON_ILLUMINATION:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.getMoonIllumination();
                            break;

                        case COLUMN_SOLUNAR_MOON_PHASE:
                            data = initData(day.getTimeInMillis(), resolver, calculator, data, latitude, longitude, altitude, config.timezone);
                            row[i] = data.getMoonPhase();
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


    public Cursor queryCalendarInfo(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_CALENDAR_INFO_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            Object[] row = new Object[columns.length];
            for (int i=0; i<columns.length; i++)
            {
                switch (columns[i])
                {
                    case COLUMN_CALENDAR_NAME:
                        row[i] = CALENDAR_NAME;
                        break;

                    case COLUMN_CALENDAR_TITLE:
                        row[i] = context.getString(R.string.calendar_solunar_displayName);
                        break;

                    case COLUMN_CALENDAR_SUMMARY:
                        row[i] = context.getString(R.string.calendar_solunar_summary);
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

    private HashMap<String, String> moonPhaseDisplay = new HashMap<>();
    private String majorTitle;
    private String minorTitle;
    private String descPattern;
    private String[] overlapDisplay;
    private String[] minorTitles;
    private String[] minorDesc;
    private String[] majorTitles;
    private String[] majorDesc;
    private String[] ratingLabels;
    private int[] ratingBrackets;

    private void initResources(Context context)
    {
        moonPhaseDisplay = new HashMap<>();
        moonPhaseDisplay.put("NEW", context.getString(R.string.timeMode_moon_new));
        moonPhaseDisplay.put("WAXING_CRESCENT", context.getString(R.string.timeMode_moon_waxingcrescent));
        moonPhaseDisplay.put("FIRST_QUARTER", context.getString(R.string.timeMode_moon_firstquarter));
        moonPhaseDisplay.put("WAXING_GIBBOUS", context.getString(R.string.timeMode_moon_waxinggibbous));
        moonPhaseDisplay.put("FULL", context.getString(R.string.timeMode_moon_full));
        moonPhaseDisplay.put("WANING_GIBBOUS", context.getString(R.string.timeMode_moon_waninggibbous));
        moonPhaseDisplay.put("THIRD_QUARTER", context.getString(R.string.timeMode_moon_thirdquarter));
        moonPhaseDisplay.put("WANING_CRESCENT", context.getString(R.string.timeMode_moon_waningcrescent));

        majorTitle = context.getString(R.string.calendar_event_title_major);
        majorTitles = new String[] {majorTitle, majorTitle};
        minorTitle = context.getString(R.string.calendar_event_title_minor);
        minorTitles = new String[] {minorTitle, minorTitle};

        overlapDisplay = context.getResources().getStringArray(R.array.solunarevent_overlap);
        ratingLabels = context.getResources().getStringArray(R.array.ratings_labels);
        ratingBrackets = context.getResources().getIntArray(R.array.ratings_brackets);

        String lunarRise = context.getString(R.string.label_moonrise);
        String lunarSet = context.getString(R.string.label_moonset);
        String lunarNoon = context.getString(R.string.label_moonnoon);
        String lunarNight = context.getString(R.string.label_moonnight);
        descPattern = context.getString(R.string.calendar_event_desc_pattern1);
        minorDesc = new String[] {context.getString(R.string.calendar_event_desc_pattern0, lunarRise, descPattern), context.getString(R.string.calendar_event_desc_pattern0, lunarSet, descPattern)};
        majorDesc = new String[] {context.getString(R.string.calendar_event_desc_pattern0, lunarNoon, descPattern), context.getString(R.string.calendar_event_desc_pattern0, lunarNight, descPattern)};
    }

    public Cursor queryCalendarContent(long[] range, @NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_CALENDAR_CONTENT_PROJECTION);
        MatrixCursor cursor = new MatrixCursor(columns);

        Context context = getContext();
        if (context != null)
        {
            initResources(context);
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null)
            {
                ArrayList<ContentValues> values = readCursor(queryCursor(resolver, new long[] {range[0], range[1]}));
                for (int j=0; j<values.size(); j++)
                {
                    ContentValues v = values.get(j);
                    cursor.addRow(new Object[] { v.get(CalendarContract.Events.TITLE), v.get(CalendarContract.Events.DESCRIPTION), v.get(CalendarContract.Events.EVENT_TIMEZONE),
                            v.get(CalendarContract.Events.DTSTART), v.get(CalendarContract.Events.DTEND), v.get(CalendarContract.Events.EVENT_LOCATION),
                            v.get(CalendarContract.Events.AVAILABILITY), v.get(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS), v.get(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS), v.get(CalendarContract.Events.GUESTS_CAN_MODIFY) });
                }

            } else {
                Log.e(getClass().getSimpleName(),"Unable to getContentResolver!");
            }
        } else Log.d("DEBUG", "context is null!");
        return cursor;
    }

    private Cursor queryCursor(ContentResolver resolver, long[] window)
    {
        Uri uri = Uri.parse("content://" + SolunarProviderContract.AUTHORITY + "/" + SolunarProviderContract.QUERY_SOLUNAR + "/" + window[0] + "-" + window[1]);
        Cursor cursor = resolver.query(uri, SolunarProviderContract.QUERY_SOLUNAR_PROJECTION, null, null, null);
        if (cursor == null) {
            Log.e(getClass().getSimpleName(), "Failed to resolve URI! " + uri);
        }
        return cursor;
    }

    private ArrayList<ContentValues> readCursor(Cursor cursor)
    {
        if (cursor == null) {
            return null;
        }
        cursor.moveToFirst();

        int[] i_minor = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET) };
        int[] i_minor_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP) };

        int[] i_major = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT) };
        int[] i_major_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP) };

        int i_minor_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH);
        int i_major_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH);
        int i_dayRating = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_RATING);
        int i_moonPhase = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_PHASE);
        //int i_moonIllum = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_ILLUMINATION);

        ArrayList<ContentValues> eventValues = new ArrayList<>();
        while (!cursor.isAfterLast())
        {
            double dayRating = cursor.getDouble(i_dayRating);
            String dayRatingDisplay = formatRating(dayRating);
            String dayRatingDisplay = DisplayStrings.formatRating(dayRating, ratingBrackets, ratingLabels) + " :: " + dayRating;

            String phase = cursor.getString(i_moonPhase);
            String phaseDisplay = moonPhaseDisplay.get(phase);
            //double moonIllum = cursor.getDouble(i_moonIllum);

            for (int i=0; i<2; i++)
            {
                String minor_overlap = cursor.getString(i_minor_overlap[i]);
                String major_overlap = cursor.getString(i_major_overlap[i]);
                minorDesc[i] = String.format(minorDesc[i], phaseDisplay, dayRatingDisplay, minor_overlap + "(" + i_minor_overlap[i] + ") boogers");
                majorDesc[i] = String.format(majorDesc[i], phaseDisplay, dayRatingDisplay, major_overlap + "(" + i_major_overlap[i] + ") boogers");
            }

            addPeriods(eventValues, cursor, i_minor, minorTitles, minorDesc, cursor.getLong(i_minor_length));
            addPeriods(eventValues, cursor, i_major, majorTitles, majorDesc, cursor.getLong(i_major_length));
            cursor.moveToNext();
        }
        cursor.close();
        return eventValues;
    }

    /**
     * addPeriods
     */
    private void addPeriods(@NonNull ArrayList<ContentValues> eventValues, @NonNull Cursor cursor, int[] index, String[] titles, String[] desc, long periodLength )
    {
        for (int j=0; j<index.length; j++)
        {
            int i = index[j];
            if (i != -1 && !cursor.isNull(i))
            {
                Calendar eventStart = Calendar.getInstance();
                Calendar eventEnd = Calendar.getInstance();
                eventStart.setTimeInMillis(cursor.getLong(i));
                eventEnd.setTimeInMillis(eventStart.getTimeInMillis() + periodLength);
                eventValues.add(createEventContentValues(titles[j], desc[j], null, eventStart, eventEnd));
            }
        }
    }

    public ContentValues createEventContentValues(String title, String description, @Nullable String location, Calendar... time)
    {
        ContentValues v = new ContentValues();
        v.put(CalendarContract.Events.TITLE, title);
        v.put(CalendarContract.Events.DESCRIPTION, description);

        if (time.length > 0)
        {
            v.put(CalendarContract.Events.EVENT_TIMEZONE, time[0].getTimeZone().getID());
            if (time.length >= 2)
            {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[1].getTimeInMillis());
            } else {
                v.put(CalendarContract.Events.DTSTART, time[0].getTimeInMillis());
                v.put(CalendarContract.Events.DTEND, time[0].getTimeInMillis());
            }
        } else {
            Log.w(getClass().getSimpleName(), "createEventContentValues: missing time arg (empty array); creating event without start or end time.");
        }

        if (location != null) {
            v.put(CalendarContract.Events.EVENT_LOCATION, location);
        }

        v.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
        v.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_SEE_GUESTS, "0");
        v.put(CalendarContract.Events.GUESTS_CAN_MODIFY, "0");
        return v;
    }

    /**
     * querySolunarConfig
     */
    public Cursor querySolunarConfig(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder)
    {
        String[] columns = (projection != null ? projection : QUERY_SOLUNAR_CONFIG_PROJECTION);
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
