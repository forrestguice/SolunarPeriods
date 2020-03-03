package com.forrestguice.suntimes.solunar.ui;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;

import android.support.annotation.NonNull;

import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
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
    public static final int MAX_POSITIONS = 2000;
    public static final int TODAY_POSITION = (MAX_POSITIONS / 2);

    protected WeakReference<Context> contextRef;

    private double latitude;
    private double longitude;
    private double altitude;
    private String timezone;

    public SolunarCardAdapter(Context context, double latitude, double longitude, double altitude, String timezone, SolunarCardOptions options)
    {
        contextRef = new WeakReference<>(context);
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.timezone = timezone;
        this.options = options;
    }

    @NonNull
    @Override
    public SolunarCardHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        LayoutInflater layout = LayoutInflater.from(viewGroup.getContext());
        View view = layout.inflate(R.layout.card_solunarday, viewGroup, false);
        return new SolunarCardHolder(view, options);
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

    private SolunarCardOptions options = new SolunarCardOptions();
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
    protected HashMap<Integer, SolunarData> data = new HashMap<>();
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
        Calendar date = Calendar.getInstance();
        date.setTimeZone(TimeZone.getTimeZone(timezone));
        date.add(Calendar.DATE, position - TODAY_POSITION);
        date.set(Calendar.HOUR_OF_DAY, 12);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        return calculateData(new SolunarData(date.getTimeInMillis(), latitude, longitude, altitude, timezone));
    }

    private SolunarData calculateData(SolunarData solunarData)
    {
        Context context = contextRef.get();
        if (context != null)
        {
            ContentResolver resolver = context.getContentResolver();
            if (resolver != null) {
                SolunarCalculator calculator = new SolunarCalculator();
                calculator.calculateData(resolver, solunarData);
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

    private void attachClickListeners(@NonNull final SolunarCardHolder holder, int position)
    {
        holder.text_date.setOnClickListener(onDateClick(position));
        holder.click_moonphase.setOnClickListener(onMoonPhaseClick(position));
        holder.layout_front.setOnClickListener(onCardClick(holder));
    }

    private void detachClickListeners(@NonNull SolunarCardHolder holder)
    {
        holder.text_date.setOnClickListener(null);
        holder.click_moonphase.setOnClickListener(null);
        holder.layout_front.setOnClickListener(null);
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
                holder.text_debug.setVisibility( holder.text_debug.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                adapterListener.onCardClick(holder.position);
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
    }

    /**
     * SolunarCardOptions
     */
    public static class SolunarCardOptions
    {
        public SuntimesInfo.SuntimesOptions suntimes_options = new SuntimesInfo.SuntimesOptions();

        public SolunarCardOptions() {}
        public SolunarCardOptions(SuntimesInfo.SuntimesOptions options) {
            suntimes_options = options;
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
