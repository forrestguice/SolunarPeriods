package com.forrestguice.suntimes.solunar;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.Calendar;

public class MainActivity extends AppCompatActivity
{
    private SuntimesInfo suntimesInfo = null;

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

        checkVersion();
        initData();
    }

    protected void initData()
    {


        double latitude = Double.parseDouble(suntimesInfo.location[1]);
        double longitude = Double.parseDouble(suntimesInfo.location[2]);
        double altitude = Double.parseDouble(suntimesInfo.location[3]);

        SolunarData data = new SolunarData(Calendar.getInstance().getTimeInMillis(), latitude, longitude, altitude, suntimesInfo.timezone);

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
            String debug = "date: " + data.getDateMillis() + "\n" +
                    "timezone: " + data.getTimezone() + "\n" +
                    "location: " + data.getLatitude() + ", " + data.getLongitude() + " [" + data.getAltitude() + "]\n\n" +
                    "sunrise: " + data.getDateMillis(SolunarData.KEY_SUNRISE) + "\n" +
                    "sunset: " + data.getDateMillis(SolunarData.KEY_SUNSET) + "\n\n" +
                    "moonrise: " + data.getDateMillis(SolunarData.KEY_MOONRISE) + "\n" +
                    "moonset: " + data.getDateMillis(SolunarData.KEY_MOONSET) + "\n" +
                    "moonillum: " + data.getMoonIllumination() + "\n\n" +
                    "rating: " + data.getDayRating();

            debug += "\n\n" + "minor periods:\n";
            SolunarPeriod[] minorPeriods = data.getMinorPeriods();
            for (int i=0; i<minorPeriods.length; i++)
            {
                if (minorPeriods[i] != null) {
                    debug += minorPeriods[i].getStartMillis() + " - " + minorPeriods[i].getEndMillis() + "\n";
                }
            }

            debug += "\n\n" + "major periods:\n";
            SolunarPeriod[] majorPeriods = data.getMajorPeriods();
            for (int i=0; i<majorPeriods.length; i++)
            {
                if (majorPeriods[i] != null) {
                    debug += majorPeriods[i].getStartMillis() + " - " + majorPeriods[i].getEndMillis() + "\n";
                }
            }

            text.setText(debug);

        } else {
            text.setText("not calculated");
        }
    }

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
