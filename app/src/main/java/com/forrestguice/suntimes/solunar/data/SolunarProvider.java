// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2023 Forrest Guice
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.forrestguice.suntimes.calendar.CalendarHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.AppSettings;
import com.forrestguice.suntimes.solunar.BuildConfig;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.ui.DisplayStrings;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

import static com.forrestguice.suntimes.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_DESCRIPTION;
import static com.forrestguice.suntimes.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_LOCATION;
import static com.forrestguice.suntimes.calendar.CalendarEventTemplateContract.COLUMN_TEMPLATE_TITLE;
import static com.forrestguice.suntimes.calendar.CalendarHelper.QUERY_CALENDAR_TEMPLATE_STRINGS;
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
    private static final int URIMATCH_CALENDAR_TEMPLATE_STRINGS = 60;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static
    {
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR_CONFIG, URIMATCH_CONFIG);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR, URIMATCH_SOLUNAR);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/#", URIMATCH_SOLUNAR_FOR_DATE);
        uriMatcher.addURI(AUTHORITY, QUERY_SOLUNAR + "/*", URIMATCH_SOLUNAR_FOR_RANGE);
        uriMatcher.addURI(AUTHORITY, QUERY_CALENDAR_INFO, URIMATCH_CALENDAR_INFO);
        uriMatcher.addURI(AUTHORITY, QUERY_CALENDAR_TEMPLATE_STRINGS, URIMATCH_CALENDAR_TEMPLATE_STRINGS);
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

            case URIMATCH_CALENDAR_TEMPLATE_STRINGS:
                Log.i(getClass().getSimpleName(), "URIMATCH_CALENDAR_TEMPLATE_STRINGS");
                cursor = queryCalendarTemplateStrings(uri, projection, selection, selectionArgs, sortOrder);
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
            SuntimesInfo config = SuntimesInfo.queryInfo(context);
            double latitude = Double.parseDouble(config.location[1]);
            double longitude = Double.parseDouble(config.location[2]);
            double altitude = Double.parseDouble(config.location[3]);

            TimeZone timezone = AppSettings.fromTimeZoneMode(context, AppSettings.getTimeZoneMode(context), config);
            Calendar day = Calendar.getInstance(timezone);
            Calendar endDay = Calendar.getInstance(timezone);
            day.setTimeInMillis(range[0]);
            endDay.setTimeInMillis(range[1] + 1000);      // +1000ms (make range[1] inclusive)

            SolunarCalculator calculator = new SolunarCalculator();
            do {
                SolunarData data = new SolunarData(day.getTimeInMillis(), config.location[0], latitude, longitude, altitude);
                calculator.calculateData(context, context.getContentResolver(), data, timezone);

                SolunarPeriod period;
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
                            row[i] = timezone.getID();
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH:
                            row[i] = SolunarCalculator.MAJOR_PERIOD_MILLIS;  // TODO: configurable
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH:
                            row[i] = SolunarCalculator.MINOR_PERIOD_MILLIS;  // TODO: configurable
                            break;

                        case COLUMN_SOLUNAR_SUNRISE:
                            row[i] = data.sunrise;
                            break;
                        case COLUMN_SOLUNAR_SUNSET:
                            row[i] = data.sunset;
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONRISE:
                            row[i] = data.moonrise;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONSET:
                            row[i] = data.moonset;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONNOON:
                            row[i] = data.moonnoon;
                            break;
                        case COLUMN_SOLUNAR_PERIOD_MOONNIGHT:
                            row[i] = data.moonnight;
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP:
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, context.getString(R.string.label_moonrise), data.moonrise, data.moonrise + SolunarCalculator.MINOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP:
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MINOR, context.getString(R.string.label_moonset), data.moonset, data.moonset + SolunarCalculator.MINOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP:
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, context.getString(R.string.label_moonnoon), data.moonnoon, data.moonnoon + SolunarCalculator.MAJOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP:
                            period = new SolunarPeriod(SolunarPeriod.TYPE_MAJOR, context.getString(R.string.label_moonnight), data.moonnight, data.moonnight + SolunarCalculator.MAJOR_PERIOD_MILLIS, data.sunrise, data.sunset);
                            row[i] = (Integer)(period.occursAtSunrise() ? OVERLAP_SUNRISE : (period.occursAtSunset() ? OVERLAP_SUNSET : OVERLAP_NONE));
                            break;

                        case COLUMN_SOLUNAR_MOON_ILLUMINATION:
                            row[i] = data.getMoonIllumination();
                            break;

                        case COLUMN_SOLUNAR_MOON_PHASE:
                            row[i] = data.getMoonPhase();
                            break;

                        case COLUMN_SOLUNAR_RATING:
                            row[i] = data.getRating().getDayRating();
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

                    case COLUMN_TEMPLATE_TITLE:
                        row[i] = DEF_TEMPLATE_TITLE;
                        break;

                    case COLUMN_TEMPLATE_DESCRIPTION:
                        row[i] = DEF_TEMPLATE_DESCRIPTION;
                        break;

                    case COLUMN_TEMPLATE_LOCATION:
                        row[i] = DEF_TEMPLATE_LOCATION;
                        break;

                    default:
                        row[i] = null;
                        break;
                }
            }
            cursor.addRow(row);

        } else Log.w(getClass().getSimpleName(), "context is null!");
        return cursor;
    }

    public Cursor queryCalendarTemplateStrings(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return CalendarHelper.createTemplateStringsCursor(templateStrings, projection);
    }

    private HashMap<String, String> moonPhaseDisplay = new HashMap<>();
    private String majorTitle;
    private String minorTitle;
    private String titlePattern;
    private String descPattern;
    private String[] templateStrings = new String[] {majorTitle, minorTitle};  // TODO
    private String[] overlapDisplay, overlapDisplay1;
    private String[] minorTitles;
    private String[] minorDesc;
    private String[] majorTitles;
    private String[] majorDesc;
    private String[] ratingLabels;
    private int[] ratingBrackets;
    private boolean is24 = false;
    private String location = "";

    private String template_title = null, template_desc = null, template_location = null;
    public static final String DEF_TEMPLATE_TITLE = "%M";
    public static final String DEF_TEMPLATE_DESCRIPTION = "%M";    // TODO
    public static final String DEF_TEMPLATE_LOCATION = "%loc";

    private void initResources(Context context)
    {
        SuntimesInfo config = SuntimesInfo.queryInfo(context);
        is24 = config.getOptions(context).time_is24;
        location = config.location[0];

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
        overlapDisplay1 = context.getResources().getStringArray(R.array.solunarevent_overlap1);
        ratingLabels = context.getResources().getStringArray(R.array.ratings_labels);
        ratingBrackets = context.getResources().getIntArray(R.array.ratings_brackets);

        String lunarRise = context.getString(R.string.label_moonrise);
        String lunarSet = context.getString(R.string.label_moonset);
        String lunarNoon = context.getString(R.string.label_moonnoon);
        String lunarNight = context.getString(R.string.label_moonnight);
        titlePattern = context.getString(R.string.calendar_event_title_pattern0);
        descPattern = context.getString(R.string.calendar_event_desc_pattern1);
        minorDesc = new String[] {context.getString(R.string.calendar_event_desc_pattern0, lunarRise, descPattern), context.getString(R.string.calendar_event_desc_pattern0, lunarSet, descPattern)};
        majorDesc = new String[] {context.getString(R.string.calendar_event_desc_pattern0, lunarNoon, descPattern), context.getString(R.string.calendar_event_desc_pattern0, lunarNight, descPattern)};

        String[] template_values = CalendarHelper.queryCalendarTemplate(context, CALENDAR_NAME);
        template_title = ((template_values[0] != null) ? template_values[0] : DEF_TEMPLATE_TITLE);
        template_desc = ((template_values[1] != null) ? template_values[1] : DEF_TEMPLATE_DESCRIPTION);
        template_location = ((template_values[2] != null) ? template_values[2] : DEF_TEMPLATE_LOCATION);
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
            if (resolver != null) {
                CalendarHelper.addEventValuesToCursor(cursor, readCursor(queryCursor(resolver, new long[] {range[0], range[1]})));

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
        Context context = getContext();
        if (cursor == null || context == null) {
            return null;
        }
        cursor.moveToFirst();

        int[] i_riseset = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_SUNRISE), cursor.getColumnIndex(COLUMN_SOLUNAR_SUNSET) };
        int[] i_minor = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET) };
        int[] i_minor_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONRISE_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONSET_OVERLAP) };

        int[] i_major = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT) };
        int[] i_major_overlap = new int[] { cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNOON_OVERLAP), cursor.getColumnIndex(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MOONNIGHT_OVERLAP) };

        int i_minor_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MINOR_LENGTH);
        int i_major_length = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_PERIOD_MAJOR_LENGTH);
        int i_dayRating = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_RATING);
        int i_moonPhase = cursor.getColumnIndexOrThrow(SolunarProviderContract.COLUMN_SOLUNAR_MOON_PHASE);

        long minor_period_length = cursor.getLong(i_minor_length);
        long major_period_length = cursor.getLong(i_major_length);

        ArrayList<ContentValues> eventValues = new ArrayList<>();
        while (!cursor.isAfterLast())
        {
            double dayRating = cursor.getDouble(i_dayRating);
            String dayRatingDisplay = DisplayStrings.formatRating(dayRating, ratingBrackets, ratingLabels);

            String phase = cursor.getString(i_moonPhase);
            String phaseDisplay = moonPhaseDisplay.get(phase);

            String[] majorTitles1 = new String[majorTitles.length];
            String[] majorDisplay = new String[majorDesc.length];

            String[] minorTitles1 = new String[minorTitles.length];
            String[] minorDisplay = new String[minorDesc.length];


            CharSequence[] riseset = new CharSequence[] { "",
                    DisplayStrings.formatTime(context, cursor.getLong(i_riseset[0]), TimeZone.getDefault(), is24),
                    DisplayStrings.formatTime(context, cursor.getLong(i_riseset[1]), TimeZone.getDefault(), is24) };

            for (int i=0; i<2; i++)
            {
                int minor_overlap = cursor.getInt(i_minor_overlap[i]);

                minorTitles1[i] = String.format(titlePattern, minorTitles[i], overlapDisplay1[minor_overlap]);
                minorDisplay[i] = String.format(minorDesc[i], phaseDisplay, dayRatingDisplay, String.format(overlapDisplay[minor_overlap], riseset[minor_overlap]));

                int major_overlap = cursor.getInt(i_major_overlap[i]);
                majorTitles1[i] = String.format(titlePattern, majorTitles[i], overlapDisplay1[major_overlap]);
                majorDisplay[i] = String.format(majorDesc[i], phaseDisplay, dayRatingDisplay, String.format(overlapDisplay[major_overlap], riseset[major_overlap]));
            }

            addPeriods(eventValues, cursor, i_minor, minorTitles1, minorDisplay, minor_period_length);
            addPeriods(eventValues, cursor, i_major, majorTitles1, majorDisplay, major_period_length);
            cursor.moveToNext();
        }
        cursor.close();
        return eventValues;
    }

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
                eventValues.add(CalendarHelper.createEventValues(titles[j], desc[j], null, eventStart, eventEnd));
            }
        }
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
