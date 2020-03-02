package com.forrestguice.suntimes.solunar.data;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.Calendar;

/**
 * SolunarPeriod
 */
public class SolunarPeriod implements Parcelable, Comparable<SolunarPeriod>
{
    public static final int TYPE_MAJOR = 0;
    public static final int TYPE_MINOR = 1;

    protected int type;
    protected long start, end;
    protected String timezone;

    public SolunarPeriod(int type, long start, long end, @NonNull String timezone)
    {
        this.type = type;
        this.start = start;
        this.end = end;
        this.timezone = timezone;
    }

    private SolunarPeriod(Parcel in)
    {
        this.type = in.readInt();
        this.start = in.readLong();
        this.end = in.readLong();
        this.timezone = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(type);
        out.writeLong(start);
        out.writeLong(end);
        out.writeString(timezone);
    }

    public int getType() {
        return type;
    }

    public long getStartMillis() {
        return start;
    }

    public long getMidpointMillis() {
        return start + ((end - start) / 2L);
    }

    public long getEndMillis() {
        return end;
    }

    public long getLength() {
        return end - start;
    }

    public boolean contains(long event) {
        return (event >= start) && (event <= end);
    }

    public boolean withinStart(long event, long millis) {
        return (Math.abs(start - event) < millis);
    }

    public String getTimezone() {
        return timezone;
    }

    public Calendar[] getCalendar()
    {
        Calendar[] calendar = new Calendar[] { Calendar.getInstance(), Calendar.getInstance() };
        calendar[0].setTimeInMillis(start);
        calendar[1].setTimeInMillis(end);
        return calendar;
    }

    public String toString() {
        return getClass().getSimpleName() + " [" + (type == TYPE_MAJOR ? "MAJOR" : "MINOR") + ": " + start + ", " + end + "," + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SolunarPeriod> CREATOR = new Creator<SolunarPeriod>()
    {
        public SolunarPeriod createFromParcel(Parcel in) {
            return new SolunarPeriod(in);
        }
        public SolunarPeriod[] newArray(int size) {
            return new SolunarPeriod[size];
        }
    };

    public static SolunarPeriod createPeriod(int type, ContentValues values, String keyStart, String keyEnd, String timezone)
    {
        Long start = values.getAsLong(keyStart);
        Long end = values.getAsLong(keyEnd);
        if (start != null && end != null && timezone != null) {
            return new SolunarPeriod(type, start, end, timezone);
        } else return null;
    }

    @Override
    public int compareTo(@NonNull SolunarPeriod o)
    {
        if (start == o.start && end == o.end) {
            return 0;
        } else if (start > o.start) {
            return 1;
        } else {
            return -1;
        }
    }
}
