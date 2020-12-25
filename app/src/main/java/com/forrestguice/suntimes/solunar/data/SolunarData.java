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

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;

public class SolunarData implements Parcelable
{
    public static final String KEY_DATE = "date";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ALTITUDE = "altitude";

    public static final String KEY_SUNRISE = "sunrise";
    public static final String KEY_SUNSET = "sunset";
    public static final String KEY_NOON = "noon";

    public static final String KEY_MOONRISE = "moonrise";
    public static final String KEY_MOONSET = "moonset";
    public static final String KEY_MOONNOON = "moonnoon";
    public static final String KEY_MOONNIGHT = "moonnight";
    public static final String KEY_MOONILLUM = "moonillum";
    public static final String KEY_MOONPHASE = "moonphase";
    public static final String KEY_MOONPERIOD = "moonperiod";
    public static final String KEY_MOONFULL = "moonfull";
    public static final String KEY_MOONNEW = "moonnew";

    public static final String KEY_MAJOR0_START = "major0_start";
    public static final String KEY_MAJOR0_END = "major0_end";
    public static final String KEY_MAJOR1_START = "major1_start";
    public static final String KEY_MAJOR1_END = "major1_end";

    public static final String KEY_MINOR0_START = "minor0_start";
    public static final String KEY_MINOR0_END = "minor0_end";
    public static final String KEY_MINOR1_START = "minor1_start";
    public static final String KEY_MINOR1_END = "minor1_end";

    public static final String KEY_DAY_RATING = "dayrating";
    public static final String KEY_DAY_RATING_REASONS = "dayrating_reasons";

    public static final String KEY_CALCULATED = "iscalculated";

    protected long date;
    protected double latitude, longitude;    // dd (decimal degrees)
    protected double altitude;               // meters
    protected String location;               // place label

    protected long sunrise = -1, sunset = -1, noon = -1;
    protected long moonrise = -1, moonset = -1;
    protected long moonnoon = -1, moonnight = -1;
    protected long moonnew = -1, moonfull = -1, moonperiod = -1;
    protected double moonillum = -1;  // [0,1]
    protected String moonphase;       // display string

    protected SolunarRating dayRating = new SolunarRating();  // [0,1]
    protected SolunarPeriod[] major_periods = new SolunarPeriod[] { null, null };
    protected SolunarPeriod[] minor_periods = new SolunarPeriod[] { null, null };

    protected boolean calculated = false;

    public SolunarData(long date, String placeName, double latitude, double longitude, double altitude)
    {
        this.calculated = false;
        this.date = date;
        this.location = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /*public SolunarData(ContentValues values) {
        initFromContentValues(values);
    }*/

    private SolunarData(Parcel in) {
        initFromParcel(in);
    }

    /*public void initFromContentValues(ContentValues values)
    {
        this.calculated = values.getAsBoolean(KEY_CALCULATED);
        this.date = values.getAsLong(KEY_DATE);
        this.location = values.getAsString(KEY_LOCATION);
        this.latitude = values.getAsDouble(KEY_LATITUDE);
        this.longitude = values.getAsDouble(KEY_LONGITUDE);
        this.altitude = values.getAsDouble(KEY_ALTITUDE);

        this.sunrise = values.getAsLong(KEY_SUNRISE);
        this.sunset = values.getAsLong(KEY_SUNSET);
        this.noon = values.getAsLong(KEY_NOON);

        this.moonrise = values.getAsLong(KEY_MOONRISE);
        this.moonset = values.getAsLong(KEY_MOONSET);
        this.moonnoon = values.getAsLong(KEY_MOONNOON);
        this.moonnight = values.getAsLong(KEY_MOONNIGHT);
        this.moonnew = values.getAsLong(KEY_MOONNEW);
        this.moonfull = values.getAsLong(KEY_MOONFULL);

        this.moonillum = values.getAsDouble(KEY_MOONILLUM);
        this.moonphase = values.getAsString(KEY_MOONPHASE);
        this.moonperiod = values.getAsLong(KEY_MOONPERIOD);

        this.dayRating = new SolunarRating(values.getAsDouble(KEY_DAY_RATING));
        this.major_periods = new SolunarPeriod[] {
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MAJOR, values, KEY_MAJOR0_START, KEY_MAJOR0_END, KEY_SUNRISE, KEY_SUNSET),
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MAJOR, values, KEY_MAJOR1_START, KEY_MAJOR1_END, KEY_SUNRISE, KEY_SUNSET)
        };
        this.minor_periods = new SolunarPeriod[] {
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MINOR, values, KEY_MINOR0_START, KEY_MINOR0_END, KEY_SUNRISE, KEY_SUNSET),
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MINOR, values, KEY_MINOR1_START, KEY_MINOR1_END, KEY_SUNRISE, KEY_SUNSET)
        };
    }

    public ContentValues asContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(KEY_CALCULATED, calculated);
        values.put(KEY_DATE, date);
        values.put(KEY_LOCATION, location);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_ALTITUDE, altitude);

        values.put(KEY_SUNRISE, sunrise);
        values.put(KEY_SUNSET, sunset);
        values.put(KEY_NOON, noon);

        values.put(KEY_MOONRISE, moonrise);
        values.put(KEY_MOONSET, moonset);
        values.put(KEY_MOONNOON, moonnoon);
        values.put(KEY_MOONNIGHT, moonnight);
        values.put(KEY_MOONNEW, moonnew);
        values.put(KEY_MOONFULL, moonfull);

        values.put(KEY_MOONILLUM, moonillum);
        values.put(KEY_MOONPHASE, moonphase);
        values.put(KEY_MOONPERIOD, moonperiod);

        values.put(KEY_DAY_RATING, dayRating.getDayRating());

        if (major_periods[0] != null) {
            values.put(KEY_MAJOR0_START, major_periods[0].getStartMillis());
            values.put(KEY_MAJOR0_END, major_periods[0].getEndMillis());
        }
        if (major_periods[1] != null) {
            values.put(KEY_MAJOR1_START, major_periods[1].getStartMillis());
            values.put(KEY_MAJOR1_END, major_periods[1].getEndMillis());
        }
        if (minor_periods[0] != null) {
            values.put(KEY_MINOR0_START, minor_periods[0].getStartMillis());
            values.put(KEY_MINOR0_END, minor_periods[0].getEndMillis());
        }
        if (minor_periods[1] != null) {
            values.put(KEY_MINOR1_START, minor_periods[1].getStartMillis());
            values.put(KEY_MINOR1_END, minor_periods[1].getEndMillis());
        }

        return values;
    }*/

    public void initFromParcel(Parcel in)
    {
        calculated = (in.readByte() != 0);
        date = in.readLong();
        location = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        altitude = in.readDouble();

        sunrise = in.readLong();
        sunset = in.readLong();
        noon = in.readLong();

        moonrise = in.readLong();
        moonset = in.readLong();
        moonnoon = in.readLong();
        moonnight = in.readLong();
        moonnew = in.readLong();
        moonfull = in.readLong();

        moonillum = in.readDouble();
        moonphase = in.readString();
        moonperiod = in.readLong();

        dayRating = in.readParcelable(ClassLoader.getSystemClassLoader());
        major_periods = (SolunarPeriod[])in.readParcelableArray(ClassLoader.getSystemClassLoader());
        minor_periods = (SolunarPeriod[])in.readParcelableArray(ClassLoader.getSystemClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeByte((byte)(calculated ? 1 : 0));
        out.writeLong(date);
        out.writeString(location);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(altitude);

        out.writeLong(sunrise);
        out.writeLong(sunset);
        out.writeLong(noon);

        out.writeLong(moonrise);
        out.writeLong(moonset);
        out.writeLong(moonnoon);
        out.writeLong(moonnight);
        out.writeLong(moonnew);
        out.writeLong(moonfull);

        out.writeDouble(moonillum);
        out.writeString(moonphase);
        out.writeLong(moonperiod);

        out.writeParcelable(dayRating, 0);
        out.writeParcelableArray(major_periods, 0);
        out.writeParcelableArray(minor_periods, 0);
    }

    /**
     * @return true data obj is calculated (completed), false not calculated (incomplete)
     */
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * @return millis
     */
    public long getDateMillis() {
        return date;
    }
    public long getDateMillis(@Nullable String key)
    {
        if (key == null) {
            return date;
        }
        switch (key)
        {
            case KEY_SUNRISE: return sunrise;
            case KEY_SUNSET: return sunset;
            case KEY_NOON: return noon;

            case KEY_MOONRISE: return moonrise;
            case KEY_MOONSET: return moonset;
            case KEY_MOONNOON: return moonnoon;
            case KEY_MOONNIGHT: return moonnight;
            case KEY_MOONNEW: return moonnew;
            case KEY_MOONFULL: return moonfull;

            case KEY_DATE:
            default: return date;
        }
    }

    /**
     * @return Calendar obj
     */
    public Calendar getDate(@NonNull TimeZone timezone) {
        return getDate(null, timezone);
    }
    public Calendar getDate(String key, @NonNull TimeZone timezone) {
        Calendar calendar = Calendar.getInstance(timezone);
        calendar.setTimeInMillis(getDateMillis(key));
        return calendar;
    }

    /**
     * @return location name
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return decimal degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return decimal degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * @return meters
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * @return percentage [0, 1]
     */
    public double getMoonIllumination() {
        return moonillum;
    }

    public String getMoonPhase() {
        return moonphase;
    }

    /**
     * @return milliseconds between new moons
     */
    public long getMoonPeriod() {
        return moonperiod;
    }

    /**
     * @return SolunarRating
     */
    public SolunarRating getRating() {
        return dayRating;
    }

    /**
     * @return array minor periods (may contain nulls)
     */
    public SolunarPeriod[] getMajorPeriods() {
        return major_periods;
    }

    /**
     * @return array minor periods (may contain nulls)
     */
    public SolunarPeriod[] getMinorPeriods() {
        return minor_periods;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<SolunarData> CREATOR = new Parcelable.Creator<SolunarData>()
    {
        public SolunarData createFromParcel(Parcel in) {
            return new SolunarData(in);
        }
        public SolunarData[] newArray(int size) {
            return new SolunarData[size];
        }
    };

}
