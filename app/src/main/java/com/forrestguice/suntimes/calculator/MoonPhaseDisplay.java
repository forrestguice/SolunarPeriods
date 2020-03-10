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

package com.forrestguice.suntimes.calculator;

import android.content.Context;

import com.forrestguice.suntimes.solunar.R;

/**
 * MoonPhaseDisplay
 */
public enum MoonPhaseDisplay
{
    NEW("New Moon", R.drawable.ic_moon_new, R.id.icon_info_moonphase_new),
    WAXING_CRESCENT("Waxing Crescent", R.drawable.ic_moon_waxing_crescent, R.id.icon_info_moonphase_waxing_crescent),
    FIRST_QUARTER("First Quarter", R.drawable.ic_moon_waxing_quarter, R.id.icon_info_moonphase_waxing_quarter),
    WAXING_GIBBOUS("Waxing Gibbous", R.drawable.ic_moon_waxing_gibbous, R.id.icon_info_moonphase_waxing_gibbous),
    FULL("Full Moon", R.drawable.ic_moon_full, R.id.icon_info_moonphase_full),
    WANING_GIBBOUS("Waning Gibbous", R.drawable.ic_moon_waning_gibbous, R.id.icon_info_moonphase_waning_gibbous),
    THIRD_QUARTER("Third Quarter", R.drawable.ic_moon_waning_quarter, R.id.icon_info_moonphase_waning_quarter),
    WANING_CRESCENT("Waning Crescent", R.drawable.ic_moon_waning_crescent, R.id.icon_info_moonphase_waning_crescent);

    private int iconResource, viewResource;
    private String displayString;

    MoonPhaseDisplay(String displayString, int iconResource, int viewResource)
    {
        this.displayString = displayString;
        this.iconResource = iconResource;
        this.viewResource = viewResource;
    }

    public String toString()
    {
        return displayString;
    }

    public int getIcon()
    {
        return iconResource;
    }

    public int getView()
    {
        return viewResource;
    }

    public String getDisplayString()
    {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public static void initDisplayStrings(Context context)
    {
         NEW.setDisplayString(context.getString(R.string.timeMode_moon_new));
         WAXING_CRESCENT.setDisplayString(context.getString(R.string.timeMode_moon_waxingcrescent));
         FIRST_QUARTER.setDisplayString(context.getString(R.string.timeMode_moon_firstquarter));
         WAXING_GIBBOUS.setDisplayString(context.getString(R.string.timeMode_moon_waxinggibbous));
         FULL.setDisplayString(context.getString(R.string.timeMode_moon_full));
         WANING_GIBBOUS.setDisplayString(context.getString(R.string.timeMode_moon_waninggibbous));
         THIRD_QUARTER.setDisplayString(context.getString(R.string.timeMode_moon_thirdquarter));
         WANING_CRESCENT.setDisplayString(context.getString(R.string.timeMode_moon_waningcrescent));
    }

}
