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

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.Calendar;

/**
 * SolunarPeriod
 */
public class SolunarPeriod implements Parcelable, Comparable<SolunarPeriod>
{
    public static final long SOLUNAR_CONCURRENCE_MILLIS = 30 * 60 * 1000;     // 30 m

    public static final int TYPE_MAJOR = 0;
    public static final int TYPE_MINOR = 1;

    protected final int type;
    protected final long start, end;
    protected final long toSunrise, toSunset;
    protected final String label;

    public SolunarPeriod(int type, String label, long start, long end, long sunrise, long sunset)
    {
        this.type = type;
        this.label = label;
        this.start = start;
        this.end = end;
        this.toSunrise = sunrise - start;
        this.toSunset = sunset - start;
    }

    private SolunarPeriod(Parcel in)
    {
        this.type = in.readInt();
        this.label = in.readString();
        this.start = in.readLong();
        this.end = in.readLong();
        this.toSunrise = in.readLong();
        this.toSunset = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(type);
        out.writeString(label);
        out.writeLong(start);
        out.writeLong(end);
        out.writeLong(toSunrise);
        out.writeLong(toSunset);
    }

    public int getType() {
        return type;
    }

    public String getLabel() {
        return label;
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

    public long getMillisToSunrise() {
        return toSunrise;
    }
    public boolean occursAtSunrise() {
        return toSunrise >= (-1 * SOLUNAR_CONCURRENCE_MILLIS) && (toSunrise <= getLength() + SOLUNAR_CONCURRENCE_MILLIS);
    }

    public long getMillisToSunset() {
        return toSunset;
    }
    public boolean occursAtSunset() {
        return toSunset >= (-1 * SOLUNAR_CONCURRENCE_MILLIS) && (toSunset <= getLength() + SOLUNAR_CONCURRENCE_MILLIS);
    }

    public Calendar[] getCalendar()
    {
        Calendar[] calendar = new Calendar[] { Calendar.getInstance(), Calendar.getInstance() };
        calendar[0].setTimeInMillis(start);
        calendar[1].setTimeInMillis(end);
        return calendar;
    }

    public String toString() {
        return getClass().getSimpleName() + "\"" + label + "\"" + " [" + (type == TYPE_MAJOR ? "MAJOR" : "MINOR") + ": " + start + ", " + end + "," + "]";
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

    public static SolunarPeriod createPeriod(int type, ContentValues values, String keyLabel, String keyStart, String keyEnd, String keySunrise, String keySunset)
    {
        String label = values.getAsString(keyLabel);
        Long start = values.getAsLong(keyStart);
        Long end = values.getAsLong(keyEnd);
        Long sunrise = values.getAsLong(keySunrise);
        Long sunset = values.getAsLong(keySunset);
        if (start != null && end != null) {
            return new SolunarPeriod(type, label, start, end, sunrise, sunset);
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
