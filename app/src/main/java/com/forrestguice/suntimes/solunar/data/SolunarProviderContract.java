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

public interface SolunarProviderContract
{
    String AUTHORITY = "solunarperiods.calculator.provider";
    String READ_PERMISSION = "suntimes.permission.READ_CALCULATOR";
    String VERSION_NAME = "v0.0.0";
    int VERSION_CODE = 0;

    /**
     * CONFIG
     */
    String COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION = "provider_version";             // String (provider version string)
    String COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE = "provider_version_code";   // int (provider version code)
    String COLUMN_SOLUNAR_CONFIG_APP_VERSION = "app_version";                       // String (app version string)
    String COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE = "app_version_code";             // int (app version code)

    String COLUMN_SOLUNAR_CONFIG_LOCATION = "location";            // String (locationName)
    String COLUMN_SOLUNAR_CONFIG_LATITUDE = "latitude";            // String (dd)
    String COLUMN_SOLUNAR_CONFIG_LONGITUDE = "longitude";          // String (dd)
    String COLUMN_SOLUNAR_CONFIG_ALTITUDE = "altitude";            // String (meters)
    String COLUMN_SOLUNAR_CONFIG_TIMEZONE = "timezone";            // String (timezoneID)

    String COLUMN_SOLUNAR_CONFIG_MAJOR_LENGTH = "majorlength";     // long (duration) major period millis
    String COLUMN_SOLUNAR_CONFIG_MINOR_LENGTH = "minorlength";     // long (duration) minor period millis

    String QUERY_SOLUNAR_CONFIG = "solunar_config";
    String[] QUERY_SOLUNAR_CONFIG_PROJECTION = new String[] {
            COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION, COLUMN_SOLUNAR_CONFIG_PROVIDER_VERSION_CODE,
            COLUMN_SOLUNAR_CONFIG_APP_VERSION, COLUMN_SOLUNAR_CONFIG_APP_VERSION_CODE,
            COLUMN_SOLUNAR_CONFIG_LOCATION, COLUMN_SOLUNAR_CONFIG_LATITUDE, COLUMN_SOLUNAR_CONFIG_LONGITUDE, COLUMN_SOLUNAR_CONFIG_ALTITUDE, COLUMN_SOLUNAR_CONFIG_TIMEZONE,
            COLUMN_SOLUNAR_CONFIG_MAJOR_LENGTH, COLUMN_SOLUNAR_CONFIG_MINOR_LENGTH
    };

    /**
     * SOLUNAR INFO
     */
    String COLUMN_SOLUNAR_DATE = "date";                        // long (timestamp)
    String COLUMN_SOLUNAR_RATING = "rating";                    // double [0,1] percent

    String COLUMN_SOLUNAR_SUNRISE = "sunrise";                  // long (timestamp) sunrise millis
    String COLUMN_SOLUNAR_SUNSET = "sunset";                    // long (timestamp) sunset millis

    String COLUMN_SOLUNAR_MOON_RISE = "moonrise";               // long (timestamp) moonrise millis; minor period start
    String COLUMN_SOLUNAR_MOON_SET = "moonset";                 // long (timestamp) moonset millis; minor period start
    String COLUMN_SOLUNAR_MOON_NOON = "moonnoon";               // long (timestamp) lunar noon millis; major period start
    String COLUMN_SOLUNAR_MOON_NIGHT = "moonnight";             // long (timestamp) lunar midnight millis; major period start
    String COLUMN_SOLUNAR_MOON_ILLUMINATION = "moonpos_illum";  // double [0,1]

    String QUERY_SOLUNAR = "solunar";
    String[] QUERY_SOLUNAR_PROJECTION = new String[] {
            COLUMN_SOLUNAR_DATE, COLUMN_SOLUNAR_RATING,
            COLUMN_SOLUNAR_SUNRISE, COLUMN_SOLUNAR_SUNSET,
            COLUMN_SOLUNAR_MOON_RISE, COLUMN_SOLUNAR_MOON_SET, COLUMN_SOLUNAR_MOON_NOON, COLUMN_SOLUNAR_MOON_NIGHT, COLUMN_SOLUNAR_MOON_ILLUMINATION
    };
}
