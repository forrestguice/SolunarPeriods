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

package com.forrestguice.suntimes.solunar.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.forrestguice.suntimes.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarCalculator;
import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class SolunarCardHolder extends RecyclerView.ViewHolder
{
    public int position = RecyclerView.NO_POSITION;

    public View layout_card;
    public View layout_front;
    public TextView text_date;
    public TextView text_debug;
    public RatingBar rating;
    public TextView text_rating;

    public LinearLayout layout_rows;
    public ArrayList<SolunarPeriodRow> rows;
    public SolunarPeriodRow row_moonrise;
    public SolunarPeriodRow row_moonnoon;
    public SolunarPeriodRow row_moonset;
    public SolunarPeriodRow row_moonnight;

    public TextView text_sunrise, text_sunset;
    public TextView text_moonrise, text_moonset;

    public View click_moonphase;
    public TextView text_moonphase, text_moonillum;
    public HashMap<MoonPhaseDisplay, ImageView> icon_moonphases;

    protected static int color_sunrise, color_sunset;

    public SolunarCardHolder(@NonNull View itemView, @NonNull SolunarCardAdapter.SolunarCardOptions options)
    {
        super(itemView);

        Context context = itemView.getContext();
        int[] attrs = new int[] {
                R.attr.sunriseColor,
                R.attr.sunsetColor,
        };
        TypedArray a = context.obtainStyledAttributes(attrs);
        color_sunrise = ContextCompat.getColor(context, a.getResourceId(0, R.color.sun_rising_dark));
        color_sunset = ContextCompat.getColor(context, a.getResourceId(1, R.color.sun_setting_dark));
        a.recycle();

        layout_card = itemView.findViewById(R.id.card);
        layout_front = itemView.findViewById(R.id.card_front);
        text_date = itemView.findViewById(R.id.text_date);
        text_debug = itemView.findViewById(R.id.text_debug);
        rating = itemView.findViewById(R.id.rating);
        text_rating = itemView.findViewById(R.id.text_rating);

        text_sunrise = itemView.findViewById(R.id.text_sunrise);
        text_sunset = itemView.findViewById(R.id.text_sunset);
        text_moonrise = itemView.findViewById(R.id.text_moonrise);
        text_moonset = itemView.findViewById(R.id.text_moonset);

        layout_rows = itemView.findViewById(R.id.layout_moon);
        rows = new ArrayList<>();
        rows.add(row_moonset = new SolunarPeriodRow(itemView, R.id.layout_moonset_period, R.id.text_moonset_label, R.id.text_moonset_start, R.id.text_moonset_end, R.id.text_moonset_plus, options));
        rows.add(row_moonnight = new SolunarPeriodRow(itemView, R.id.layout_moonnight_period, R.id.text_moonnight_label, R.id.text_moonnight_start, R.id.text_moonnight_end, R.id.text_moonnight_plus, options));
        rows.add(row_moonrise = new SolunarPeriodRow(itemView, R.id.layout_moonrise_period,  R.id.text_moonrise_label, R.id.text_moonrise_start, R.id.text_moonrise_end, R.id.text_moonrise_plus, options));
        rows.add(row_moonnoon = new SolunarPeriodRow(itemView, R.id.layout_moonnoon_period, R.id.text_moonnoon_label, R.id.text_moonnoon_start, R.id.text_moonnoon_end, R.id.text_moonnoon_plus, options));

        click_moonphase = itemView.findViewById(R.id.clickarea_moonphase);
        text_moonphase = itemView.findViewById(R.id.text_moonphase);
        text_moonillum = itemView.findViewById(R.id.text_moonillum);

        icon_moonphases = new HashMap<>();
        for (MoonPhaseDisplay phaseDisplay : MoonPhaseDisplay.values()) {
            icon_moonphases.put(phaseDisplay, (ImageView)itemView.findViewById(phaseDisplay.getView()));
        }
    }

    public void onBindViewHolder(@NonNull Context context, int position, SolunarData data, SolunarCardAdapter.SolunarCardOptions options)
    {
        String timezone = data.getTimezone();

        this.position = position;
        text_date.setText(DisplayStrings.formatDate(context, data.getDate()));
        if (position == SolunarCardAdapter.TODAY_POSITION)
        {
            layout_card.setSelected(true);
            text_date.setTypeface(text_date.getTypeface(), Typeface.BOLD);

        } else {
            layout_card.setSelected(false);
            text_date.setTypeface(Typeface.create(text_date.getTypeface(), Typeface.NORMAL));
            /*if (position < SolunarCardAdapter.TODAY_POSITION) {
                // TODO: "past" appearance
            } else {
                // TODO: "future" appearance
            }*/
        }

        if (data.isCalculated())
        {
            long sunrise = data.getDateMillis(SolunarData.KEY_SUNRISE);
            long sunset = data.getDateMillis(SolunarData.KEY_SUNSET);
            long moonrise = data.getDateMillis(SolunarData.KEY_MOONRISE);
            long moonset = data.getDateMillis(SolunarData.KEY_MOONSET);

            text_sunrise.setText(DisplayStrings.formatTime(context, sunrise, timezone, options.suntimes_options.time_is24));
            text_sunset.setText(DisplayStrings.formatTime(context, sunset, timezone, options.suntimes_options.time_is24));
            text_moonrise.setText(DisplayStrings.formatTime(context, moonrise, timezone, options.suntimes_options.time_is24));
            text_moonset.setText(DisplayStrings.formatTime(context, moonset, timezone, options.suntimes_options.time_is24));

            text_moonillum.setText( context.getString(R.string.format_illumination, (int)(data.getMoonIllumination() * 100) + "") );

            MoonPhaseDisplay phase = MoonPhaseDisplay.valueOf(data.getMoonPhase());
            boolean isNewMoon = SolunarCalculator.isSameDay(data.getDate(), data.getDate(SolunarData.KEY_MOONNEW));
            boolean isFullMoon = SolunarCalculator.isSameDay(data.getDate(), data.getDate(SolunarData.KEY_MOONFULL));

            if (isNewMoon || isFullMoon) {
                long event = (isNewMoon ? data.getDateMillis(SolunarData.KEY_MOONNEW) : data.getDateMillis(SolunarData.KEY_MOONFULL));
                text_moonphase.setText(context.getString(R.string.format_moonphase_long,phase.getDisplayString(), DisplayStrings.formatTime(context, event, timezone, options.suntimes_options.time_is24)));
            } else {
                text_moonphase.setText(phase.getDisplayString());
            }

            hideMoonPhaseIcons();
            ImageView icon = icon_moonphases.get(phase);
            icon.setVisibility(View.VISIBLE);

            double moonPeriodDays = data.getMoonPeriod() / 1000d / 60d / 60d / 24d;

            String debug =
                    "moon period: " + moonPeriodDays + "\n" + //; //"date: " + SolunarCardHolder.formatDate(context, data.getDateMillis()) + "\n" +
                    //"timezone: " + data.getTimezone() + "\n" +
                    //"location: " + data.getLatitude() + ", " + data.getLongitude() + " [" + data.getAltitude() + "]\n\n" +
                    //"sunrise: " + SolunarCardHolder.formatTime(context, sunrise, data.getTimezone(), false) + "\n" +
                    //"sunset: " + SolunarCardHolder.formatTime(context, sunset, data.getTimezone(), false) + "\n\n" +
                    //"moonrise: " + SolunarCardHolder.formatTime(context, moonrise, data.getTimezone(), false) + "\n" +
                    //"moonset: " + SolunarCardHolder.formatTime(context, moonset, data.getTimezone(), false) + "\n" +
                    //"moonillum: " + data.getMoonIllumination() + "\n\n" +
                    "rating: " + data.getDayRating();
                            //;
            text_debug.setText(debug);  // TODO

            SolunarPeriod[] majorPeriods = data.getMajorPeriods();
            SolunarPeriod[] minorPeriods = data.getMinorPeriods();
            row_moonrise.setPeriod(context, minorPeriods[0]);    // moonrise
            row_moonset.setPeriod(context, minorPeriods[1]);     // moonset
            row_moonnoon.setPeriod(context, majorPeriods[0]);    // lunar noon
            row_moonnight.setPeriod(context, majorPeriods[1]);   // lunar midnight
            SolunarPeriodRow.reorderLayout(layout_rows, rows);

            double dayRating = data.getDayRating();
            if (dayRating > 0)
            {
                float numStars = (float)(data.getDayRating() * 4);
                rating.setNumStars((int)Math.ceil(numStars));
                rating.setRating(numStars);

            } else {
                rating.setNumStars(1);
                rating.setRating(0.25f);
            }
            text_rating.setText(DisplayStrings.formatRating(context, dayRating));

        } else {
            text_debug.setText(context.getString(R.string.time_none));
            rating.setNumStars(0);
        }
    }

    private void hideMoonPhaseIcons()
    {
        for (ImageView view : icon_moonphases.values()) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
    }

    /**
     * SolunarPeriodRow
     */
    public static class SolunarPeriodRow implements Comparable<SolunarPeriodRow>
    {
        public View layout;
        public TextView label;
        public TextView start, end;
        public TextView plus;
        private SolunarCardAdapter.SolunarCardOptions options;

        public SolunarPeriodRow(View parent, int layoutID, int labelViewID, int startViewID, int endViewID, int plusViewID, SolunarCardAdapter.SolunarCardOptions options) {
            layout = parent.findViewById(layoutID);
            label = parent.findViewById(labelViewID);
            start = parent.findViewById(startViewID);
            end = parent.findViewById(endViewID);
            plus = parent.findViewById(plusViewID);
            this.options = options;
        }

        public SolunarPeriod period = null;
        public void setPeriod(Context context, SolunarPeriod period)
        {
            this.period = period;
            if (period != null)
            {
                if (period.occursAtSunrise())
                {
                    plus.setVisibility(View.VISIBLE);
                    plus.setTextColor(color_sunrise);

                } else if (period.occursAtSunset()) {
                    plus.setVisibility(View.VISIBLE);
                    plus.setTextColor(color_sunset);
                } else {
                    plus.setVisibility(View.INVISIBLE);
                    plus.setTextColor(Color.TRANSPARENT);
                }
                label.setText(DisplayStrings.formatType(context, period.getType()));
                start.setText(DisplayStrings.formatTime(context, period.getStartMillis(), period.getTimezone(), options.suntimes_options.time_is24));
                end.setText(DisplayStrings.formatTime(context, period.getEndMillis(), period.getTimezone(), options.suntimes_options.time_is24));
                layout.setVisibility(View.VISIBLE);

            } else {
                start.setText(context.getString(R.string.time_none));
                end.setText(context.getString(R.string.time_none));
                layout.setVisibility(View.GONE);
            }
        }

        public static void reorderLayout(@NonNull LinearLayout layout, ArrayList<SolunarPeriodRow> rows)
        {
            ArrayList<SolunarPeriodRow> periodRows = (ArrayList<SolunarPeriodRow>)rows.clone();
            Collections.sort(periodRows);

            layout.removeAllViews();
            for (int i=0; i<periodRows.size(); i++) {
                layout.addView(periodRows.get(i).layout, i);
            }
        }

        @Override
        public int compareTo(@NonNull SolunarPeriodRow other) {
            if (period != null && other.period != null)
            {
                return period.compareTo(other.period);

            } else if (period != null) {
                return 1;

            } else if (other.period != null) {
                return -1;

            } else {
                return 0;
            }
        }
    }

}
