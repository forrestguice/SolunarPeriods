package com.forrestguice.suntimes.solunar.data;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Calendar;

/**
 * SolunarPeriod
 */
public class SolunarPeriod implements Parcelable
{
    public static final int TYPE_MAJOR = 0;
    public static final int TYPE_MINOR = 1;

    protected int type;
    protected long start, end;

    public SolunarPeriod(int type, long start, long end)
    {
        this.type = type;
        this.start = start;
        this.end = end;
    }

    private SolunarPeriod(Parcel in)
    {
        this.type = in.readInt();
        this.start = in.readLong();
        this.end = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(type);
        out.writeLong(start);
        out.writeLong(end);
    }

    public int getType() {
        return type;
    }

    public long getStartMillis() {
        return start;
    }

    public long getEndMillis() {
        return end;
    }

    public Calendar[] getCalendar()
    {
        Calendar[] calendar = new Calendar[] { Calendar.getInstance(), Calendar.getInstance() };
        calendar[0].setTimeInMillis(start);
        calendar[1].setTimeInMillis(end);
        return calendar;
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

    public static SolunarPeriod createPeriod(int type, ContentValues values, String keyStart, String keyEnd)
    {
        Long start = values.getAsLong(keyStart);
        Long end = values.getAsLong(keyEnd);
        if (start != null && end != null) {
            return new SolunarPeriod(type, start, end);
        } else return null;
    }
}
