package com.forrestguice.suntimes.solunar.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.forrestguice.suntimes.calculator.MoonPhaseDisplay;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarCalculator;
import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

public class SolunarCardHolder extends RecyclerView.ViewHolder
{
    public int position = RecyclerView.NO_POSITION;

    public View layout_font;
    public TextView text_date;
    public TextView text_debug;
    public RatingBar rating;

    public TextView text_sunrise, text_sunset;
    public TextView text_moonrise, text_moonset;
    public TextView text_moonrise_period, text_moonset_period;
    public TextView text_moonnight, text_moonnoon;
    public TextView text_moonphase, text_moonillum;
    public HashMap<MoonPhaseDisplay, ImageView> icon_moonphases;

    public SolunarCardHolder(@NonNull View itemView)
    {
        super(itemView);
        layout_font = itemView.findViewById(R.id.card_front);
        text_date = itemView.findViewById(R.id.text_date);
        text_debug = itemView.findViewById(R.id.text_debug);
        rating = itemView.findViewById(R.id.rating);

        text_sunrise = itemView.findViewById(R.id.text_sunrise);
        text_sunset = itemView.findViewById(R.id.text_sunset);
        text_moonrise = itemView.findViewById(R.id.text_moonrise);
        text_moonrise_period = itemView.findViewById(R.id.text_moonrise_period);
        text_moonset = itemView.findViewById(R.id.text_moonset);
        text_moonset_period = itemView.findViewById(R.id.text_moonset_period);
        text_moonnight = itemView.findViewById(R.id.text_moonnight);
        text_moonnoon = itemView.findViewById(R.id.text_moonnoon);

        text_moonphase = itemView.findViewById(R.id.text_moonphase);
        text_moonillum = itemView.findViewById(R.id.text_moonillum);

        icon_moonphases = new HashMap<>();
        for (MoonPhaseDisplay phaseDisplay : MoonPhaseDisplay.values()) {
            icon_moonphases.put(phaseDisplay, (ImageView)itemView.findViewById(phaseDisplay.getView()));
        }

        // TODO
    }

    public void onBindViewHolder(@NonNull Context context, int position, SolunarData data, SolunarCardAdapter.SolunarCardOptions options)
    {
        String timezone = data.getTimezone();

        this.position = position;
        text_date.setText(formatDate(context, data.getDate()));
        if (position == SolunarCardAdapter.TODAY_POSITION)
        {
            text_date.setTypeface(text_date.getTypeface(), Typeface.BOLD);

        } else {
            text_date.setTypeface(text_date.getTypeface(), Typeface.NORMAL);
            if (position < SolunarCardAdapter.TODAY_POSITION) {
                // TODO
            } else {
                // TODO
            }
        }

        if (data.isCalculated())
        {
            long sunrise = data.getDateMillis(SolunarData.KEY_SUNRISE);
            long sunset = data.getDateMillis(SolunarData.KEY_SUNSET);
            long moonrise = data.getDateMillis(SolunarData.KEY_MOONRISE);
            long moonset = data.getDateMillis(SolunarData.KEY_MOONSET);

            text_sunrise.setText(formatTime(context, sunrise, timezone, options.suntimes_options.time_is24));
            text_sunset.setText(formatTime(context, sunset, timezone, options.suntimes_options.time_is24));
            text_moonrise.setText(formatTime(context, moonrise, timezone, options.suntimes_options.time_is24));
            text_moonset.setText(formatTime(context, moonset, timezone, options.suntimes_options.time_is24));

            text_moonillum.setText((int)(data.getMoonIllumination() * 100) + "%");

            MoonPhaseDisplay phase = MoonPhaseDisplay.valueOf(data.getMoonPhase());
            boolean isNewMoon = SolunarCalculator.isSameDay(data.getDate(), data.getDate(SolunarData.KEY_MOONNEW));
            boolean isFullMoon = SolunarCalculator.isSameDay(data.getDate(), data.getDate(SolunarData.KEY_MOONFULL));

            if (isNewMoon) {
                text_moonphase.setText(phase.getLongDisplayString() + "\n" + formatTime(context, data.getDateMillis(SolunarData.KEY_MOONNEW), timezone, options.suntimes_options.time_is24));

            } else if (isFullMoon) {
                text_moonphase.setText(phase.getLongDisplayString() + "\n" + formatTime(context, data.getDateMillis(SolunarData.KEY_MOONFULL), timezone, options.suntimes_options.time_is24));

            } else {
                text_moonphase.setText(phase.getLongDisplayString());
            }

            hideMoonPhaseIcons();
            ImageView icon = icon_moonphases.get(phase);
            icon.setVisibility(View.VISIBLE);

            long moonAgeMillis = data.getMoonAge();
            double moonAgeDays = moonAgeMillis / 1000d / 60d / 60d / 24d;
            double moonPeriodDays = data.getMoonPeriod() / 1000d / 60d / 60d / 24d;

            String debug = "moon age: " + moonAgeDays + "\n" + //; //"date: " + SolunarCardHolder.formatDate(context, data.getDateMillis()) + "\n" +
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
            text_debug.setText(debug);

            SolunarPeriod[] majorPeriods = data.getMajorPeriods();
            SolunarPeriod[] minorPeriods = data.getMinorPeriods();

            if (minorPeriods[0] != null)
            {
                text_moonrise_period.setText(
                        context.getString(R.string.format_period, SolunarCardHolder.formatTime(context, minorPeriods[0].getStartMillis(), data.getTimezone(), options.suntimes_options.time_is24),
                                                                  SolunarCardHolder.formatTime(context, minorPeriods[0].getEndMillis(), data.getTimezone(), options.suntimes_options.time_is24)) );
            } else {
                text_moonset_period.setText(context.getString(R.string.time_none));
            }

            if (minorPeriods[1] != null)
            {
                text_moonset_period.setText(
                        context.getString(R.string.format_period,  SolunarCardHolder.formatTime(context, minorPeriods[1].getStartMillis(), data.getTimezone(), options.suntimes_options.time_is24),
                                                                   SolunarCardHolder.formatTime(context, minorPeriods[1].getEndMillis(), data.getTimezone(), options.suntimes_options.time_is24)) );
            } else {
                text_moonset_period.setText(context.getString(R.string.time_none));
            }

            if (majorPeriods[0] != null)
            {
                text_moonnoon.setText(
                        context.getString(R.string.format_period, SolunarCardHolder.formatTime(context, majorPeriods[0].getStartMillis(), data.getTimezone(), options.suntimes_options.time_is24),
                                                                  SolunarCardHolder.formatTime(context, majorPeriods[0].getEndMillis(), data.getTimezone(), options.suntimes_options.time_is24)) );
            } else {
                text_moonnoon.setText(context.getString(R.string.time_none));
            }
            if (majorPeriods[1] != null)
            {
                text_moonnight.setText(
                        context.getString(R.string.format_period, SolunarCardHolder.formatTime(context, majorPeriods[1].getStartMillis(), data.getTimezone(), options.suntimes_options.time_is24),
                                                                  SolunarCardHolder.formatTime(context, majorPeriods[1].getEndMillis(), data.getTimezone(), options.suntimes_options.time_is24)) );
            } else {
                text_moonnight.setText(context.getString(R.string.time_none));
            }

            double dayRating = data.getDayRating();
            if (dayRating > 0)
            {
                float numStars = (float)(data.getDayRating() * 5);
                rating.setNumStars((int)Math.ceil(numStars));
                rating.setRating(numStars);

            } else {
                rating.setNumStars(1);
                rating.setRating(1);
            }

        } else {
            text_debug.setText("not calculated");
            rating.setNumStars(0);
        }

        // TODO
    }

    private void hideMoonPhaseIcons()
    {
        for (ImageView view : icon_moonphases.values()) {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
        }
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
