package com.forrestguice.suntimes.solunar;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;

import com.forrestguice.suntimes.solunar.ui.HelpDialog;
import com.forrestguice.suntimes.solunar.ui.SolunarCardAdapter;

import java.lang.reflect.Method;
public class MainActivity extends AppCompatActivity
{
    public static final String DIALOG_HELP = "helpDialog";

    private SuntimesInfo suntimesInfo = null;

    private FloatingActionButton fab;
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(suntimesInfo.appTheme.equals(SuntimesInfo.THEME_LIGHT) ? R.style.SolunarAppTheme_Light : R.style.SolunarAppTheme_Dark);
        }

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_action_suntimes);
        }


        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(fabOnClickListener);

        cardView = (RecyclerView)findViewById(R.id.cardView);
        cardView.setHasFixedSize(true);
        cardView.setLayoutManager(new LinearLayoutManager(this));
        cardView.addItemDecoration(cardDecoration);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(cardView);

        if (checkVersion())
        {
            initData();
            updateViews();
            cardView.scrollToPosition(SolunarCardAdapter.TODAY_POSITION);
        }
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
        cardAdapter = new SolunarCardAdapter(this, latitude, longitude, altitude, suntimesInfo.timezone);
        cardAdapter.setCardAdapterListener(cardListener);
        cardAdapter.setCardOptions(new SolunarCardAdapter.SolunarCardOptions(suntimesInfo.getOptions()));

        cardAdapter.initData();
        cardView.setAdapter(cardAdapter);
    }

    protected void updateViews()
    {
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(suntimesInfo.location[0]);
            toolbar.setSubtitle(getString(R.string.format_location, suntimesInfo.location[1], suntimesInfo.location[2], suntimesInfo.location[3]));
        }
    }

    private SolunarCardAdapter.SolunarCardAdapterListener cardListener = new SolunarCardAdapter.SolunarCardAdapterListener()
    {
        @Override
        public void onDateClick(int i) {
            Snackbar.make(cardView, "date clicked " + i, Snackbar.LENGTH_LONG).setAction("TODO", null).show();
            // TODO
        }

        @Override
        public void onCardClick(int i) {
            Snackbar.make(cardView, "card clicked " + i, Snackbar.LENGTH_LONG).setAction("TODO", null).show();
            // TODO
        }
    };

    private View.OnClickListener fabOnClickListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showToday();
        }
    };

    protected boolean checkVersion()
    {
        boolean checkVersion = SuntimesInfo.checkVersion(this, suntimesInfo);
        if (!checkVersion) {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.isInstalled) {
                Messages.showMissingDependencyMessage(this, view);
            } else if (!suntimesInfo.hasPermission) {
                Messages.showPermissionDeniedMessage(this, view);
            }
        }
        return checkVersion;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressWarnings("RestrictedApi")
    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu)
    {
        forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_today:
                showToday();
                return true;

            case R.id.action_settings:
                showSettings();
                return true;

            case R.id.action_help:
                showHelp();
                return true;

            case R.id.action_about:
                showAbout();
                return true;

            case android.R.id.home:
                onHomePressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onHomePressed() {
        AddonHelper.startSuntimesActivity(this);
    }

    protected void showToday() {
        cardView.scrollToPosition(SolunarCardAdapter.TODAY_POSITION);
    }

    protected void showSettings()
    {
        // TODO
        Snackbar.make(cardView, "show settings", Snackbar.LENGTH_LONG).setAction("TODO", null).show();
    }

    protected void showHelp()
    {
        HelpDialog dialog = new HelpDialog();
        dialog.setContent("TODO");    // TODO
        dialog.show(getSupportFragmentManager(), DIALOG_HELP);
    }

    protected void showAbout()
    {
        Snackbar.make(cardView, "show about", Snackbar.LENGTH_LONG).setAction("TODO", null).show();
        // TODO
    }

    /**
     * from http://stackoverflow.com/questions/18374183/how-to-show-icons-in-overflow-menu-in-actionbar
     */
    public static void forceActionBarIcons(Menu menu)
    {
        if (menu != null)
        {
            if (menu.getClass().getSimpleName().equals("MenuBuilder"))
            {
                try {
                    Method m = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    m.setAccessible(true);
                    m.invoke(menu, true);

                } catch (Exception e) {
                    Log.e(MainActivity.class.getSimpleName(), "failed to set show overflow icons", e);
                }
            }
        }
    }


}
