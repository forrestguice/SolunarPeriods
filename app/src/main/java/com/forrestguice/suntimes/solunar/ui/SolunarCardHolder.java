package com.forrestguice.suntimes.solunar.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SolunarCardHolder extends RecyclerView.ViewHolder
{
    public int position = RecyclerView.NO_POSITION;

    public View layout;
    public TextView text_date;
    public TextView text_debug;

    public SolunarCardHolder(@NonNull View itemView)
    {
        super(itemView);
        layout = itemView.findViewById(R.id.card_layout);
        text_date = itemView.findViewById(R.id.text_date);
        text_debug = itemView.findViewById(R.id.text_debug);
        // TODO
    }

    public void onBindViewHolder(@NonNull Context context, int position, SolunarData data)
    {
        this.position = position;
        text_date.setText(formatDate(context, data.getDate()));

        if (data.isCalculated())
        {
            long sunrise = data.getDateMillis(SolunarData.KEY_SUNRISE);
            long sunset = data.getDateMillis(SolunarData.KEY_SUNSET);
            long moonrise = data.getDateMillis(SolunarData.KEY_MOONRISE);
            long moonset = data.getDateMillis(SolunarData.KEY_MOONSET);

            String debug = "date: " + SolunarCardHolder.formatDate(context, data.getDateMillis()) + "\n" +
                    "timezone: " + data.getTimezone() + "\n" +
                    "location: " + data.getLatitude() + ", " + data.getLongitude() + " [" + data.getAltitude() + "]\n\n" +
                    "sunrise: " + SolunarCardHolder.formatTime(context, sunrise, data.getTimezone(), false) + "\n" +
                    "sunset: " + SolunarCardHolder.formatTime(context, sunset, data.getTimezone(), false) + "\n\n" +
                    "moonrise: " + SolunarCardHolder.formatTime(context, moonrise, data.getTimezone(), false) + "\n" +
                    "moonset: " + SolunarCardHolder.formatTime(context, moonset, data.getTimezone(), false) + "\n" +
                    "moonillum: " + data.getMoonIllumination() + "\n\n" +
                    "rating: " + data.getDayRating();

            debug += "\n\n" + "minor periods:\n";
            SolunarPeriod[] minorPeriods = data.getMinorPeriods();
            for (int i=0; i<minorPeriods.length; i++)
            {
                if (minorPeriods[i] != null) {
                    debug += SolunarCardHolder.formatTime(context, minorPeriods[i].getStartMillis(), data.getTimezone(), false)
                            + " - " + SolunarCardHolder.formatTime(context, minorPeriods[i].getEndMillis(), data.getTimezone(), false) + "\n";
                }
            }

            debug += "\n\n" + "major periods:\n";
            SolunarPeriod[] majorPeriods = data.getMajorPeriods();
            for (int i=0; i<majorPeriods.length; i++)
            {
                if (majorPeriods[i] != null) {
                    debug += SolunarCardHolder.formatTime(context, majorPeriods[i].getStartMillis(), data.getTimezone(), false)
                            + " - " + SolunarCardHolder.formatTime(context, majorPeriods[i].getEndMillis(), data.getTimezone(), false) + "\n";
                }
            }

            text_debug.setText(debug);
        } else {
            text_debug.setText("not calculated");
        }

        // TODO
    }

    public static CharSequence formatDate(@NonNull Context context, long date)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        return formatDate(context, calendar);
    }

    public static CharSequence formatDate(@NonNull Context context, Calendar date)
    {
        Locale locale = Locale.getDefault();
        SimpleDateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.format_date), locale);
        dateFormat.setTimeZone(date.getTimeZone());
        return dateFormat.format(date.getTime());
    }

    public static CharSequence formatTime(@NonNull Context context, long dateTime, String timezone, boolean is24Hr)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateTime);
        String format = (is24Hr ? context.getString(R.string.format_time24) : context.getString(R.string.format_time12));
        SimpleDateFormat timeFormat = new SimpleDateFormat(format, Locale.getDefault());
        timeFormat.setTimeZone(TimeZone.getTimeZone(timezone));
        return timeFormat.format(calendar.getTime());
    }

}
