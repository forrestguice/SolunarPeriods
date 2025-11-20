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
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SolunarPeriod
 */
public class SolunarRating implements Parcelable, Comparable<SolunarRating>
{
    protected final double rating;  // [0,1]
    protected final ArrayList<String> reasons = new ArrayList<>();

    public SolunarRating() {
        this(0, (List<String>) null);
    }
    public SolunarRating(double dayRating) {
        this(dayRating, (List<String>) null);
    }
    public SolunarRating(double dayRating, String[] reasons) {
        this(dayRating, Arrays.asList(reasons));
    }
    public SolunarRating(double dayRating, List<String> reasons)
    {
        this.rating = dayRating;
        if (reasons != null) {
            this.reasons.addAll(reasons);
        }
    }
    private SolunarRating(Parcel in)
    {
        this.rating = in.readDouble();
        in.readStringList(this.reasons);
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeDouble(rating);
        out.writeStringList(reasons);
    }

    public String toString() {
        return getClass().getSimpleName() + " [" + rating + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @return percentage [0, 1]
     */
    public double getDayRating() {
        return rating;
    }

    /**
     * @return array of display strings
     */
    public String[] getReasons() {
        return reasons.toArray(new String[0]);
    }

    public static final Creator<SolunarRating> CREATOR = new Creator<SolunarRating>()
    {
        public SolunarRating createFromParcel(Parcel in) {
            return new SolunarRating(in);
        }
        public SolunarRating[] newArray(int size) {
            return new SolunarRating[size];
        }
    };

    @Override
    public int compareTo(@NonNull SolunarRating o)
    {
        if (rating == o.rating) {
            return 0;
        } else if (rating > o.rating) {
            return 1;
        } else {
            return -1;
        }
    }
}
