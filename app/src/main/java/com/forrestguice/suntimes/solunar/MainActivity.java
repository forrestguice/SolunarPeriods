package com.forrestguice.suntimes.solunar;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;

import com.forrestguice.suntimes.calculator.core.CalculatorProviderContract;
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
    private LinearLayoutManager cardLayout;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context.getContentResolver());    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String appTheme = SuntimesInfo.queryAppTheme(getContentResolver());
        if (appTheme != null && !appTheme.equals(suntimesInfo.appTheme)) {
            recreate();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (suntimesInfo.appTheme != null) {    // override the theme
            setTheme(getThemeResID(suntimesInfo.appTheme));
        }
        super.onCreate(savedInstanceState);

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
        TooltipCompat.setTooltipText(fab, getString(R.string.action_today));

        cardView = (RecyclerView)findViewById(R.id.cardView);
        cardView.setHasFixedSize(true);
        cardView.setLayoutManager(cardLayout = new LinearLayoutManager(this));
        cardView.addItemDecoration(cardDecoration);
        cardView.setOnScrollListener(onCardScrollChanged);

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(cardView);

        if (checkVersion())
        {
            initData();
            updateViews();
            cardView.scrollToPosition(SolunarCardAdapter.TODAY_POSITION);
        }
    }

    private int getThemeResID(@NonNull String themeName) {
        return themeName.equals(SuntimesInfo.THEME_LIGHT) ? R.style.SolunarAppTheme_Light : R.style.SolunarAppTheme_Dark;
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

    private RecyclerView.OnScrollListener onCardScrollChanged = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            /**int[] position = new int[] { cardLayout.findFirstCompletelyVisibleItemPosition(), cardLayout.findLastVisibleItemPosition() };
            if (SolunarCardAdapter.TODAY_POSITION >= position[0] && SolunarCardAdapter.TODAY_POSITION < position[1]) {
                fab.hide();
            } else {
                fab.show();
            }*/
        }
    };

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
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra( SettingsActivity.EXTRA_THEMERESID, getThemeResID(suntimesInfo.appTheme) );
        intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
        startActivity(intent);
    }

    protected void showHelp()
    {
        HelpDialog dialog = new HelpDialog();
        dialog.setTheme(getThemeResID(suntimesInfo.appTheme));

        String[] help = new String[] { getString(R.string.help_major_periods), getString(R.string.help_minor_periods) };
        String helpContent = help[0];
        for (int i=1; i<help.length; i++) {
            helpContent = getString(R.string.format_help, helpContent, help[i]);
        }
        dialog.setContent(helpContent + "<br/>");
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
