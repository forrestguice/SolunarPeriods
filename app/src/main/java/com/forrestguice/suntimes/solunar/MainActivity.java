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

package com.forrestguice.suntimes.solunar;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.TooltipCompat;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.LocaleHelper;
import com.forrestguice.suntimes.addon.SuntimesInfo;
import com.forrestguice.suntimes.addon.ui.Messages;

import com.forrestguice.suntimes.solunar.data.SolunarData;
import com.forrestguice.suntimes.solunar.ui.AboutDialog;
import com.forrestguice.suntimes.solunar.ui.DateDialog;
import com.forrestguice.suntimes.solunar.ui.DisplayStrings;
import com.forrestguice.suntimes.solunar.ui.HelpDialog;
import com.forrestguice.suntimes.solunar.ui.SolunarCardAdapter;
import com.forrestguice.suntimes.solunar.ui.SolunarDaySheet;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
    public static final String DIALOG_DATE = "dateDialog";
    public static final String DIALOG_HELP = "helpDialog";
    public static final String DIALOG_ABOUT = "aboutDialog";

    private SuntimesInfo suntimesInfo = null;

    private FloatingActionButton fab;
    private RecyclerView cardView;
    private SolunarCardAdapter cardAdapter;
    private LinearLayoutManager cardLayout;
    private BottomSheetBehavior<View> bottomSheet;
    private SolunarDaySheet daySheet;

    @Override
    protected void attachBaseContext(Context context)
    {
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    public void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState(outState);
        //outState.putInt("bottomSheet", bottomSheet.getState());
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedState)
    {
        super.onRestoreInstanceState(savedState);
        //int sheetState = savedState.getInt("bottomSheet", BottomSheetBehavior.STATE_HIDDEN);
        //bottomSheet.setState(sheetState);
    }

    protected void restoreDialogs()
    {
        FragmentManager fragments = getSupportFragmentManager();
        DateDialog dateDialog = (DateDialog) fragments.findFragmentByTag(DIALOG_DATE);
        if (dateDialog != null) {
            dateDialog.setFragmentListener(dateDialogListener);
        }
    }

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

        View bottomSheetView = findViewById(R.id.app_bottomsheet);
        bottomSheet = BottomSheetBehavior.from(bottomSheetView);
        bottomSheet.setHideable(true);
        bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheet.setBottomSheetCallback(bottomSheetCallback);
        daySheet = new SolunarDaySheet();
        daySheet.setFragmentListener(new SolunarDaySheet.FragmentListener() {
            @Override
            public void onBackClicked() {
                hideBottomSheet();
            }
        });
        if (suntimesInfo.appTheme != null) {    // override the theme
            daySheet.setTheme(getThemeResID(suntimesInfo.appTheme));
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.app_bottomsheet, daySheet).commit();

        TextView text_timezone = findViewById(R.id.text_timezone);
        if (text_timezone != null) {
            text_timezone.setOnClickListener(onTimeZoneClicked);
        }

        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(onFabClicked);
        TooltipCompat.setTooltipText(fab, getString(R.string.action_today));

        cardView = (RecyclerView)findViewById(R.id.cardView);
        cardView.setHasFixedSize(true);
        cardView.setLayoutManager(cardLayout = new LinearLayoutManager(this));
        cardView.addItemDecoration(cardDecoration);
        //cardView.setOnScrollListener(onCardScrollChanged);
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        String currentTheme = suntimesInfo.appTheme;
        suntimesInfo = SuntimesInfo.queryInfo(MainActivity.this);
        if (suntimesInfo.appTheme != null && !suntimesInfo.appTheme.equals(currentTheme)) {
            recreate();

        } else {
            if (checkVersion())
            {
                initData();
                updateViews();
                cardView.scrollToPosition(SolunarCardAdapter.TODAY_POSITION);
            }
            restoreDialogs();
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
        TimeZone timezone = AppSettings.fromTimeZoneMode(MainActivity.this, AppSettings.getTimeZoneMode(MainActivity.this), suntimesInfo);
        double latitude = Double.parseDouble(suntimesInfo.location[1]);
        double longitude = Double.parseDouble(suntimesInfo.location[2]);
        double altitude = Double.parseDouble(suntimesInfo.location[3]);
        cardAdapter = new SolunarCardAdapter(this, latitude, longitude, altitude, new SolunarCardAdapter.SolunarCardOptions(suntimesInfo.getOptions(this), timezone));
        cardAdapter.setCardAdapterListener(cardListener);

        cardAdapter.initData();
        cardView.setAdapter(cardAdapter);
    }

    protected void updateViews()
    {
        ActionBar toolbar = getSupportActionBar();
        if (toolbar != null) {
            toolbar.setTitle(suntimesInfo.location[0]);
            toolbar.setSubtitle(DisplayStrings.formatLocation(this, suntimesInfo));
        }

        TextView text_timezone = findViewById(R.id.text_timezone);
        if (text_timezone != null) {
            text_timezone.setText(cardAdapter.getTimeZone().getID());
        }
    }

    public static final int SMOOTHSCROLL_ITEMLIMIT = 28;
    public void scrollToPosition(int position) {
        scrollToPosition(position, true);
    }
    public void scrollToPosition(int position, boolean skipAnimation)
    {
        int current = cardLayout.findFirstVisibleItemPosition() + ((cardLayout.findLastVisibleItemPosition() - cardLayout.findFirstVisibleItemPosition()) / 2);

        //if (skipAnimation) {
            cardView.scrollToPosition(position);

        //} else if (Math.abs(position - current) >= SMOOTHSCROLL_ITEMLIMIT) {
        //    cardView.scrollToPosition(position < current ? position + SMOOTHSCROLL_ITEMLIMIT : position - SMOOTHSCROLL_ITEMLIMIT);
        //    cardView.smoothScrollToPosition(position);

        //} else {
        //    cardView.smoothScrollToPosition(position);
        //}
    }

    /*private RecyclerView.OnScrollListener onCardScrollChanged = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
        {
            super.onScrollStateChanged(recyclerView, newState);
            int[] position = new int[] { cardLayout.findFirstCompletelyVisibleItemPosition(), cardLayout.findLastVisibleItemPosition() };
            if (SolunarCardAdapter.TODAY_POSITION >= position[0] && SolunarCardAdapter.TODAY_POSITION < position[1]) {
                fab.hide();
            } else {
                fab.show();
            }
        }
    };*/

    private View.OnClickListener onFabClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showToday();
        }
    };

    private View.OnClickListener onTimeZoneClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showTimeZonePopup(v);
            //Snackbar.make(cardView, DisplayStrings.fromHtml(getString(R.string.snack_timezone, suntimesInfo.timezone)), Snackbar.LENGTH_LONG).show();
        }
    };

    private SolunarCardAdapter.SolunarCardAdapterListener cardListener = new SolunarCardAdapter.SolunarCardAdapterListener()
    {
        @Override
        public void onDateClick(int i)
        {
            //SolunarData data = cardAdapter.initData(i);
            //CharSequence dateDisplay = DisplayStrings.formatDate(MainActivity.this, data.getDate());
            //Snackbar.make(cardView, dateDisplay, Snackbar.LENGTH_LONG).show();
        }

        @Override
        public void onMoonPhaseClick(int i) {
            //Snackbar.make(cardView, "phase clicked " + i, Snackbar.LENGTH_LONG).setAction("TODO", null).show();
            // TODO
        }

        @Override
        public void onCardClick(int i) {
            showBottomSheet(i, cardAdapter.initData(i));
        }

        @Override
        public boolean onCardLongClick(int i) {
            //scrollToPosition(i);
            //Snackbar.make(cardView, "card clicked " + i, Snackbar.LENGTH_LONG).setAction("TODO", null).show();
            return false;
            // TODO
        }
    };
    
    protected boolean checkVersion()
    {
        boolean checkVersion = SuntimesInfo.checkVersion(this, suntimesInfo);
        if (!checkVersion) {
            View view = getWindow().getDecorView().findViewById(android.R.id.content);
            if (!suntimesInfo.hasPermission && suntimesInfo.isInstalled) {
                Messages.showPermissionDeniedMessage(this, view);
            } else {
                Messages.showMissingDependencyMessage(this, view);
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
        DisplayStrings.forceActionBarIcons(menu);
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

            case R.id.action_date:
                showDateDialog();
                return true;

            case R.id.action_calendars:
                showCalendarIntegration();
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

    @Override
    public void onBackPressed()
    {
        if (isBottomSheetShowing()) {
            hideBottomSheet();
        } else super.onBackPressed();
    }

    protected void showToday() {
        scrollToPosition(SolunarCardAdapter.TODAY_POSITION, false);
        showBottomSheet(SolunarCardAdapter.TODAY_POSITION, cardAdapter.initData(SolunarCardAdapter.TODAY_POSITION));
    }

    protected void showDateDialog()
    {
        final long todayMillis = cardAdapter.initData(SolunarCardAdapter.TODAY_POSITION).getDateMillis();
        long rangeMillis = (((SolunarCardAdapter.MAX_POSITIONS / 2L) - 2) * (24 * 60 * 60 * 1000L));

        DateDialog dialog = new DateDialog();
        dialog.setTheme(getThemeResID(suntimesInfo.appTheme));
        dialog.setDateRange(todayMillis - rangeMillis, todayMillis + rangeMillis);
        dialog.setFragmentListener(dateDialogListener);

        int firstVisiblePosition = cardLayout.findFirstVisibleItemPosition();
        dialog.setDate((firstVisiblePosition >= 0) ? cardAdapter.initData(firstVisiblePosition).getDateMillis() : Calendar.getInstance().getTimeInMillis());
        dialog.show(getSupportFragmentManager(), DIALOG_DATE);
    }

    private DateDialog.FragmentListener dateDialogListener = new DateDialog.FragmentListener()
    {
        @Override
        public void onAccepted(int year, int month, int day)
        {
            final long todayMillis = cardAdapter.initData(SolunarCardAdapter.TODAY_POSITION).getDateMillis();
            Calendar date = Calendar.getInstance(cardAdapter.getTimeZone());
            date.set(year, month, day);

            double offset = Math.ceil(date.getTimeInMillis() - todayMillis) / (24 * 60 * 60 * 1000D);
            int position = SolunarCardAdapter.TODAY_POSITION + (int)offset + (2 * (int) Math.signum(offset));
            scrollToPosition(position, false);
            //showBottomSheet(cardAdapter.initData(position));
            //Toast.makeText(MainActivity.this, "TODO: " + year + "-" + month + "-" + day, Toast.LENGTH_SHORT).show();  // TODO
        }
        @Override
        public void onCanceled() {}
    };

    protected void showBottomSheet(int position, SolunarData data)
    {
        final FragmentManager fragments = getSupportFragmentManager();
        if (daySheet != null)
        {
            SolunarCardAdapter.SolunarCardOptions options = new SolunarCardAdapter.SolunarCardOptions(cardAdapter.getOptions());
            options.show_dayDiff = true;
            daySheet.setCardOptions(options);
            daySheet.setData(position, data);
            bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    protected void hideBottomSheet() {
        bottomSheet.setHideable(true);
        bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    protected BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback()
    {
        @Override
        public void onStateChanged(@NonNull View view, int newState) {}
        @Override
        public void onSlide(@NonNull View view, float v) {}
    };

    protected boolean isBottomSheetShowing() {
        return bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    protected void showCalendarIntegration() {
        AddonHelper.startSuntimesSettingsActivity(MainActivity.this, AddonHelper.FRAGMENT_SETTINGS_CALENDARS);
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

        String[] help = getResources().getStringArray(R.array.help_topics);
        String helpContent = help[0];
        for (int i=1; i<help.length; i++) {
            helpContent = getString(R.string.format_help, helpContent, help[i]);
        }
        dialog.setContent(helpContent + "<br/>");
        dialog.show(getSupportFragmentManager(), DIALOG_HELP);
    }

    protected void showAbout()
    {
        AboutDialog dialog = new AboutDialog();
        dialog.setTheme(getThemeResID(suntimesInfo.appTheme));
        dialog.setVersion(suntimesInfo);
        dialog.show(getSupportFragmentManager(), DIALOG_ABOUT);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void showTimeZonePopup(View v)
    {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_timezone, popup.getMenu());
        updateTimeZonePopupMenu(popup.getMenu());
        popup.setOnMenuItemClickListener(onTimeZonePopupMenuItemSelected);
        popup.show();
    }
    private void updateTimeZonePopupMenu(Menu menu)
    {
        MenuItem itemSystem = menu.findItem(R.id.action_timezone_system);
        MenuItem itemSuntimes = menu.findItem(R.id.action_timezone_suntimes);
        MenuItem[] items = new MenuItem[] {itemSystem, itemSuntimes, menu.findItem(R.id.action_timezone_localmean), menu.findItem(R.id.action_timezone_apparentsolar)};

        if (itemSystem != null) {
            String tzID = getString(R.string.action_timezone_system_format, TimeZone.getDefault().getID());
            String tzString = getString(R.string.action_timezone_system, tzID);
            itemSystem.setTitle(DisplayStrings.createRelativeSpan(null, tzString, tzID, 0.65f));
        }

        if (itemSuntimes != null) {
            String tzID = getString(R.string.action_timezone_system_format, AppSettings.getTimeZone(MainActivity.this, suntimesInfo).getID());
            String tzString = getString(R.string.action_timezone_suntimes, tzID);
            itemSuntimes.setTitle(DisplayStrings.createRelativeSpan(null, tzString, tzID, 0.65f));
        }

        items[AppSettings.getTimeZoneMode(MainActivity.this)].setChecked(true);
    }
    private PopupMenu.OnMenuItemClickListener onTimeZonePopupMenuItemSelected = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {

            switch (item.getItemId())
            {
                case R.id.action_timezone_system:
                case R.id.action_timezone_suntimes:
                case R.id.action_timezone_localmean:
                case R.id.action_timezone_apparentsolar:
                    item.setChecked(true);
                    AppSettings.setTimeZoneMode(MainActivity.this, menuItemToTimeZoneMode(item));
                    cardAdapter.invalidateData();
                    cardAdapter.getOptions().timezone = AppSettings.fromTimeZoneMode(MainActivity.this, AppSettings.getTimeZoneMode(MainActivity.this), suntimesInfo);
                    cardAdapter.initData();
                    updateViews();
                    return true;
            }
            return false;
        }
    };
    public static int menuItemToTimeZoneMode(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_timezone_suntimes: return AppSettings.TZMODE_SUNTIMES;
            case R.id.action_timezone_localmean: return AppSettings.TZMODE_LOCALMEAN;
            case R.id.action_timezone_system: return AppSettings.TZMODE_SYSTEM;
            case R.id.action_timezone_apparentsolar: default: return AppSettings.TZMODE_APPARENTSOLAR;
        }
    }


}
