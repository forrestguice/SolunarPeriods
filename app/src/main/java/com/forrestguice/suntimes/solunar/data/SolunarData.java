package com.forrestguice.suntimes.solunar.data;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.TimeZone;

public class SolunarData implements Parcelable
{
    public static final String KEY_DATE = "date";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_ALTITUDE = "altitude";
    public static final String KEY_TIMEZONE = "timezone";

    public static final String KEY_SUNRISE = "sunrise";
    public static final String KEY_SUNSET = "sunset";
    public static final String KEY_NOON = "noon";

    public static final String KEY_MOONRISE = "moonrise";
    public static final String KEY_MOONSET = "moonset";
    public static final String KEY_MOONNOON = "moonnoon";
    public static final String KEY_MOONNIGHT = "moonnight";
    public static final String KEY_MOONILLUM = "moonillum";
    public static final String KEY_MOONPHASE = "moonphase";
    public static final String KEY_MOONAGE = "moonage";
    public static final String KEY_MOONPERIOD = "moonperiod";

    public static final String KEY_MAJOR0_START = "major0_start";
    public static final String KEY_MAJOR0_END = "major0_end";
    public static final String KEY_MAJOR1_START = "major1_start";
    public static final String KEY_MAJOR1_END = "major1_end";

    public static final String KEY_MINOR0_START = "minor0_start";
    public static final String KEY_MINOR0_END = "minor0_end";
    public static final String KEY_MINOR1_START = "minor1_start";
    public static final String KEY_MINOR1_END = "minor1_end";

    public static final String KEY_DAY_RATING = "dayrating";

    public static final String KEY_CALCULATED = "iscalculated";

    protected long date;
    protected double latitude, longitude, altitude;
    protected String timezone;

    protected long sunrise = -1, sunset = -1, noon = -1;
    protected long moonrise = -1, moonset = -1;
    protected long moonnoon = -1, moonnight = -1;
    protected long moonage = -1, moonperiod = -1;
    protected double moonillum = -1;  // [0,1]
    protected String moonphase;       // display string

    protected double dayRating;  // [0,1]
    protected SolunarPeriod[] major_periods = new SolunarPeriod[] { null, null };
    protected SolunarPeriod[] minor_periods = new SolunarPeriod[] { null, null };

    protected boolean calculated = false;

    public SolunarData(long date, double latitude, double longitude, double altitude, String timezone)
    {
        this.calculated = false;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timezone = timezone;
    }

    public SolunarData(ContentValues values) {
        initFromContentValues(values);
    }

    private SolunarData(Parcel in) {
        initFromParcel(in);
    }

    public void initFromContentValues(ContentValues values)
    {
        this.calculated = values.getAsBoolean(KEY_CALCULATED);
        this.date = values.getAsLong(KEY_DATE);
        this.latitude = values.getAsDouble(KEY_LATITUDE);
        this.longitude = values.getAsDouble(KEY_LONGITUDE);
        this.altitude = values.getAsDouble(KEY_ALTITUDE);
        this.timezone = values.getAsString(KEY_TIMEZONE);

        this.sunrise = values.getAsLong(KEY_SUNRISE);
        this.sunset = values.getAsLong(KEY_SUNSET);
        this.noon = values.getAsLong(KEY_NOON);

        this.moonrise = values.getAsLong(KEY_MOONRISE);
        this.moonset = values.getAsLong(KEY_MOONSET);
        this.moonnoon = values.getAsLong(KEY_MOONNOON);
        this.moonnight = values.getAsLong(KEY_MOONNIGHT);

        this.moonillum = values.getAsDouble(KEY_MOONILLUM);
        this.moonphase = values.getAsString(KEY_MOONPHASE);
        this.moonage = values.getAsLong(KEY_MOONILLUM);
        this.moonperiod = values.getAsLong(KEY_MOONPERIOD);

        this.dayRating = values.getAsDouble(KEY_DAY_RATING);
        this.major_periods = new SolunarPeriod[] {
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MAJOR, values, KEY_MAJOR0_START, KEY_MAJOR0_END),
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MAJOR, values, KEY_MAJOR1_START, KEY_MAJOR1_END)
        };
        this.minor_periods = new SolunarPeriod[] {
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MINOR, values, KEY_MINOR0_START, KEY_MINOR0_END),
                SolunarPeriod.createPeriod(SolunarPeriod.TYPE_MINOR, values, KEY_MINOR1_START, KEY_MINOR1_END)
        };
    }

    public ContentValues asContentValues()
    {
        ContentValues values = new ContentValues();
        values.put(KEY_CALCULATED, calculated);
        values.put(KEY_DATE, date);
        values.put(KEY_LATITUDE, latitude);
        values.put(KEY_LONGITUDE, longitude);
        values.put(KEY_ALTITUDE, altitude);
        values.put(KEY_TIMEZONE, timezone);

        values.put(KEY_SUNRISE, sunrise);
        values.put(KEY_SUNSET, sunset);
        values.put(KEY_NOON, noon);

        values.put(KEY_MOONRISE, moonrise);
        values.put(KEY_MOONSET, moonset);
        values.put(KEY_MOONNOON, moonnoon);
        values.put(KEY_MOONNIGHT, moonnight);

        values.put(KEY_MOONILLUM, moonillum);
        values.put(KEY_MOONPHASE, moonphase);
        values.put(KEY_MOONAGE, moonage);
        values.put(KEY_MOONPERIOD, moonperiod);

        values.put(KEY_DAY_RATING, dayRating);
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
    }

    public void initFromParcel(Parcel in)
    {
        calculated = (in.readByte() != 0);
        date = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        altitude = in.readDouble();
        timezone = in.readString();

        sunrise = in.readLong();
        sunset = in.readLong();
        noon = in.readLong();

        moonrise = in.readLong();
        moonset = in.readLong();
        moonnoon = in.readLong();
        moonnight = in.readLong();

        moonillum = in.readDouble();
        moonphase = in.readString();
        moonage = in.readLong();
        moonperiod = in.readLong();

        dayRating = in.readDouble();
        major_periods = (SolunarPeriod[])in.readParcelableArray(ClassLoader.getSystemClassLoader());
        minor_periods = (SolunarPeriod[])in.readParcelableArray(ClassLoader.getSystemClassLoader());
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeByte((byte)(calculated ? 1 : 0));
        out.writeLong(date);
        out.writeDouble(latitude);
        out.writeDouble(longitude);
        out.writeDouble(altitude);
        out.writeString(timezone);

        out.writeLong(sunrise);
        out.writeLong(sunset);
        out.writeLong(noon);

        out.writeLong(moonrise);
        out.writeLong(moonset);
        out.writeLong(moonnoon);
        out.writeLong(moonnight);

        out.writeDouble(moonillum);
        out.writeString(moonphase);
        out.writeLong(moonage);
        out.writeLong(moonperiod);

        out.writeDouble(dayRating);
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

            case KEY_DATE:
            default: return date;
        }
    }

    /**
     * @return Calendar obj
     */
    public Calendar getDate() {
        return getDate(null);
    }
    public Calendar getDate(String key) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(timezone));
        calendar.setTimeInMillis(getDateMillis(key));
        return calendar;
    }

    /**
     * @return tzID
     */
    public String getTimezone() {
        return timezone;
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
     * @return milliseconds since the new moon
     */
    public long getMoonAge() {
        return moonage;
    }

    /**
     * @return milliseconds between new moons
     */
    public long getMoonPeriod() {
        return moonperiod;
    }

    /**
     * @return percentage [0, 1]
     */
    public double getDayRating() {
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
