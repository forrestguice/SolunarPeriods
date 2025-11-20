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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;

import androidx.annotation.NonNull;

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarCalculator;
import com.forrestguice.suntimes.solunar.data.SolunarData;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class SolunarCardAdapter extends RecyclerView.Adapter<SolunarCardHolder>
{
    public static final int MAX_POSITIONS = 7300;   // +- 10 yrs
    public static final int TODAY_POSITION = (MAX_POSITIONS / 2);

    protected final WeakReference<Context> contextRef;

    private final String location;
    private final double latitude;
    private final double longitude;
    private final double altitude;

    public SolunarCardAdapter(Context context, String location, double latitude, double longitude, double altitude, SolunarCardOptions options)
    {
        contextRef = new WeakReference<>(context);
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.options = options;
    }

    @NonNull
    @Override
    public SolunarCardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
        View view = layout.inflate(R.layout.card_solunarday, viewGroup, false);
        return new SolunarCardHolder(view.getContext(), view, options);
    }

    @Override
    public void onBindViewHolder(@NonNull SolunarCardHolder holder, int position)
    {
        Context context = contextRef.get();
        if (context != null)
        {
            holder.onBindViewHolder(context, position, initData(position), options);
            attachClickListeners(holder, position);
        }
    }

    @Override
    public void onViewRecycled(@NonNull SolunarCardHolder holder)
    {
        detachClickListeners(holder);
        holder.position = RecyclerView.NO_POSITION;
    }

    private SolunarCardOptions options;
    public void setCardOptions(SolunarCardOptions options) {
        this.options = options;
    }
    public SolunarCardOptions getOptions() {
        return options;
    }

    @Override
    public int getItemCount() {
        return MAX_POSITIONS;
    }

    @SuppressLint("UseSparseArrays")
    protected final HashMap<Integer, SolunarData> data = new HashMap<>();
    public HashMap<Integer, SolunarData> getData() {
        return data;
    }

    public SolunarData initData()
    {
        SolunarData d;
        data.clear();
        invalidated = false;

        initData(TODAY_POSITION - 1);
        d = initData(TODAY_POSITION);
        initData(TODAY_POSITION + 1);
        initData(TODAY_POSITION + 2);
        notifyDataSetChanged();
        return d;
    }

    public SolunarData initData(int position)
    {
        SolunarData d = data.get(position);
        if (d == null && !invalidated) {
            data.put(position, d = createData(position));   // data gets removed in onViewRecycled
            //Log.d("DEBUG", "add data " + position);
        }
        return d;
    }

    protected SolunarData createData(int position)
    {
        Calendar date = Calendar.getInstance(options.timezone);
        date.add(Calendar.DATE, position - TODAY_POSITION);
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        return calculateData(new SolunarData(date.getTimeInMillis(), location, latitude, longitude, altitude));
    }

    private SolunarData calculateData(SolunarData solunarData)
    {
        Context context = contextRef.get();
        if (context != null)
        {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                SolunarCalculator calculator = new SolunarCalculator();
                calculator.calculateData(context, resolver, solunarData, options.timezone);
            } else {
                Log.e(getClass().getSimpleName(), "createData: null contentResolver!");
            }
        } else {
            Log.e(getClass().getSimpleName(), "createData: null context!");
        }
        return solunarData;
    }

    private boolean invalidated = false;
    public void invalidateData()
    {
        invalidated = true;
        data.clear();
        notifyDataSetChanged();
    }

    public int findPositionForDate(Calendar date)
    {
        Calendar today = initData(SolunarCardAdapter.TODAY_POSITION).getDate(date.getTimeZone());
        today.set(Calendar.HOUR_OF_DAY, 12);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);

        long delta = date.getTimeInMillis() - today.getTimeInMillis();
        double offset = delta / (24 * 60 * 60 * 1000D);
        return SolunarCardAdapter.TODAY_POSITION + (int) Math.round(offset);
    }

    public TimeZone getTimeZone() {
        return options.timezone;
    }

    private void attachClickListeners(@NonNull final SolunarCardHolder holder, int position)
    {
        //holder.text_date.setOnClickListener(onDateClick(position));
        //holder.click_moonphase.setOnClickListener(onMoonPhaseClick(position));
        holder.layout_front.setOnClickListener(onCardClick(holder));
        holder.layout_front.setOnLongClickListener(onCardLongClick(holder));
    }

    private void detachClickListeners(@NonNull SolunarCardHolder holder)
    {
        //holder.text_date.setOnClickListener(null);
        //holder.click_moonphase.setOnClickListener(null);
        holder.layout_front.setOnClickListener(null);
        holder.layout_front.setOnLongClickListener(null);
    }

    /**
     * setCardAdapterListener
     * @param listener SolunarCardAdapterListener
     */
    public void setCardAdapterListener( @NonNull SolunarCardAdapterListener listener ) {
        adapterListener = listener;
    }
    private SolunarCardAdapterListener adapterListener = new SolunarCardAdapterListener();

    private View.OnClickListener onDateClick(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onDateClick(position);
            }
        };
    }
    private View.OnClickListener onMoonPhaseClick(final int position) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onMoonPhaseClick(position);
            }
        };
    }
    private View.OnClickListener onCardClick(@NonNull final SolunarCardHolder holder) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapterListener.onCardClick(holder.position);
            }
        };
    }
    private View.OnLongClickListener onCardLongClick(@NonNull final SolunarCardHolder holder) {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //holder.text_debug.setVisibility( holder.text_debug.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                return adapterListener.onCardLongClick(holder.position);
            }
        };
    }

    /**
     * SolunarCardAdapterListener
     */
    public static class SolunarCardAdapterListener
    {
        public void onDateClick(int position) {}
        public void onMoonPhaseClick(int position) {}
        public void onCardClick(int position) {}
        public boolean onCardLongClick(int position) { return false; }
    }

    /**
     * SolunarCardOptions
     */
    public static class SolunarCardOptions
    {
        public final SuntimesInfo.SuntimesOptions suntimes_options;
        public TimeZone timezone;
        public boolean show_dayDiff = false;

        public SolunarCardOptions(@NonNull Context context) {
            this.suntimes_options = new SuntimesInfo.SuntimesOptions(context);
            this.timezone = TimeZone.getDefault();
        }

        public SolunarCardOptions(SuntimesInfo.SuntimesOptions options, TimeZone timezone) {
            this.suntimes_options = options;
            this.timezone = timezone;
        }
        public SolunarCardOptions(SolunarCardOptions other)
        {
            this.suntimes_options = other.suntimes_options;
            this.timezone = other.timezone;
            this.show_dayDiff = other.show_dayDiff;
        }
    }

    /**
     * CardViewScroller
     */
    public static class CardScroller extends LinearSmoothScroller
    {
        private static final float MILLISECONDS_PER_INCH = 125f;

        public CardScroller(Context context) {
            super(context);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
        }

        @Override protected int getVerticalSnapPreference() {
            return LinearSmoothScroller.SNAP_TO_START;
        }
    }
}
