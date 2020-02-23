package com.forrestguice.suntimes.solunar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;
import com.forrestguice.suntimes.solunar.data.SolunarCalculator;
import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.data.SolunarPeriod;
import com.forrestguice.suntimes.solunar.ui.SolunarCardAdapter;
import com.forrestguice.suntimes.solunar.ui.SolunarCardHolder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

    private RecyclerView cardView;
    private SolunarCardAdapter cardAdapter;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context.getContentResolver());    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(suntimesInfo.appTheme.equals(SuntimesInfo.THEME_LIGHT) ? com.forrestguice.suntimes.addon.R.style.AppTheme_Light : com.forrestguice.suntimes.addon.R.style.AppTheme_Dark);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        cardView = (RecyclerView)findViewById(R.id.cardView);
        cardView.setHasFixedSize(true);
        cardView.setLayoutManager(new LinearLayoutManager(this));
        cardView.addItemDecoration(cardDecoration);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(cardView);

        checkVersion();
        initData();
        cardView.scrollToPosition(SolunarCardAdapter.TODAY_POSITION);
    }

    private RecyclerView.ItemDecoration cardDecoration = new RecyclerView.ItemDecoration()
    {
        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state)
        {
            super.getItemOffsets(outRect, view, parent, state);
            Resources r = getResources();
            outRect.top = (int)r.getDimension(R.dimen.card_margin_top);
            outRect.bottom = (int)r.getDimension(R.dimen.card_margin_bottom);
            outRect.left = (int)r.getDimension(R.dimen.card_margin_left);
            outRect.right = (int)r.getDimension(R.dimen.card_margin_right);
        }
    };

    protected void initData()
    {


        double latitude = Double.parseDouble(suntimesInfo.location[1]);
        double longitude = Double.parseDouble(suntimesInfo.location[2]);
        double altitude = Double.parseDouble(suntimesInfo.location[3]);

        /**SolunarData data = new SolunarData(Calendar.getInstance().getTimeInMillis(), latitude, longitude, altitude, suntimesInfo.timezone);

        SolunarCalculator calculator = new SolunarCalculator();
        calculator.calculateData(getContentResolver(), data);

        TextView text = (TextView)findViewById(R.id.text_debug);
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddonHelper.startSuntimesActivity(MainActivity.this);
            }
        });

        if (data.isCalculated())
        {
            long sunrise = data.getDateMillis(SolunarData.KEY_SUNRISE);
            long sunset = data.getDateMillis(SolunarData.KEY_SUNSET);
            long moonrise = data.getDateMillis(SolunarData.KEY_MOONRISE);
            long moonset = data.getDateMillis(SolunarData.KEY_MOONSET);

            String debug = "date: " + SolunarCardHolder.formatDate(this, data.getDateMillis()) + "\n" +
                    "timezone: " + data.getTimezone() + "\n" +
                    "location: " + data.getLatitude() + ", " + data.getLongitude() + " [" + data.getAltitude() + "]\n\n" +
                    "sunrise: " + SolunarCardHolder.formatTime(this, sunrise, suntimesInfo.timezone, false) + "\n" +
                    "sunset: " + SolunarCardHolder.formatTime(this, sunset, suntimesInfo.timezone, false) + "\n\n" +
                    "moonrise: " + SolunarCardHolder.formatTime(this, moonrise, suntimesInfo.timezone, false) + "\n" +
                    "moonset: " + SolunarCardHolder.formatTime(this, moonset, suntimesInfo.timezone, false) + "\n" +
                    "moonillum: " + data.getMoonIllumination() + "\n\n" +
                    "rating: " + data.getDayRating();

            debug += "\n\n" + "minor periods:\n";
            SolunarPeriod[] minorPeriods = data.getMinorPeriods();
            for (int i=0; i<minorPeriods.length; i++)
            {
                if (minorPeriods[i] != null) {
                    debug += SolunarCardHolder.formatTime(this, minorPeriods[i].getStartMillis(), suntimesInfo.timezone, false) + " - " + SolunarCardHolder.formatTime(this, minorPeriods[i].getEndMillis(), suntimesInfo.timezone, false) + "\n";
                }
            }

            debug += "\n\n" + "major periods:\n";
            SolunarPeriod[] majorPeriods = data.getMajorPeriods();
            for (int i=0; i<majorPeriods.length; i++)
            {
                if (majorPeriods[i] != null) {
                    debug += SolunarCardHolder.formatTime(this, majorPeriods[i].getStartMillis(), suntimesInfo.timezone, false) + " - " + SolunarCardHolder.formatTime(this, majorPeriods[i].getEndMillis(), suntimesInfo.timezone, false) + "\n";
                }
            }

            text.setText(debug);

        } else {
            text.setText("not calculated");
        }*/


        cardAdapter = new SolunarCardAdapter(this, latitude, longitude, altitude, suntimesInfo.timezone);
        cardAdapter.setCardAdapterListener(cardListener);

        cardAdapter.initData();
        cardView.setAdapter(cardAdapter);
    }

    private SolunarCardAdapter.SolunarCardAdapterListener cardListener = new SolunarCardAdapter.SolunarCardAdapterListener()
    {
        @Override
        public void onDateClick(int i) {
            Snackbar.make(cardView, "date clicked " + i, Snackbar.LENGTH_LONG).setAction("TODO", null).show();
            // TODO
        }
    };

    protected void checkVersion()
    {
        if (!SuntimesInfo.checkVersion(this, suntimesInfo))
        {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission)
                Messages.showPermissionDeniedMessage(this, view);
            else Messages.showMissingDependencyMessage(this, view);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            // TODO
            //case R.id.action_settings:
            //  return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
