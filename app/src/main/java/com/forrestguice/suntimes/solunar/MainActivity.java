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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.TooltipCompat;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.addon.AppThemeInfo;
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

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity
{
    public static final String DIALOG_DATE = "dateDialog";
    public static final String DIALOG_HELP = "helpDialog";
    public static final String DIALOG_ABOUT = "aboutDialog";

    private SuntimesInfo suntimesInfo = null;
    private AppThemes themes;

    private FloatingActionButton fab;
    private RecyclerView cardView;
    private SolunarCardAdapter cardAdapter;
    private LinearLayoutManager cardLayout;
    private BottomSheetBehavior<View> bottomSheet;
    private SolunarDaySheet daySheet;

    @Override
    protected void attachBaseContext(Context context)
    {
        AppThemeInfo.setFactory(themes = new AppThemes());
        suntimesInfo = SuntimesInfo.queryInfo(context);    // obtain Suntimes version info
        super.attachBaseContext( (suntimesInfo != null && suntimesInfo.appLocale != null) ?    // override the locale
                LocaleHelper.loadLocale(context, suntimesInfo.appLocale) : context );
    }

    public void onSaveInstanceState( @NonNull Bundle outState )
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
            AppThemeInfo.setTheme(this, suntimesInfo);
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
            daySheet.setTheme(AppThemeInfo.themePrefToStyleId(MainActivity.this, AppThemeInfo.themeNameFromInfo(suntimesInfo)));
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

    private final RecyclerView.ItemDecoration cardDecoration = new RecyclerView.ItemDecoration()
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
        cardAdapter = new SolunarCardAdapter(this, suntimesInfo.location[0], latitude, longitude, altitude, new SolunarCardAdapter.SolunarCardOptions(suntimesInfo.getOptions(this), timezone));
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

    private final View.OnClickListener onFabClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v) {
            showToday();
        }
    };

    private final View.OnClickListener onTimeZoneClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showTimeZonePopup(v);
            //Snackbar.make(cardView, DisplayStrings.fromHtml(getString(R.string.snack_timezone, suntimesInfo.timezone)), Snackbar.LENGTH_LONG).show();
        }
    };

    private final SolunarCardAdapter.SolunarCardAdapterListener cardListener = new SolunarCardAdapter.SolunarCardAdapterListener()
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
            if (!suntimesInfo.hasPermission) {
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
    protected boolean onPrepareOptionsPanel(View view, @NonNull Menu menu)
    {
        DisplayStrings.forceActionBarIcons(menu);
        return super.onPrepareOptionsPanel(view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == R.id.action_today) {
            showToday();
            return true;

        } else if (id == R.id.action_date) {
            showDateDialog();
            return true;

        } else if (id == R.id.action_calendars) {
            showCalendarIntegration();
            return true;

        } else if (id == R.id.action_settings) {
            showSettings();
            return true;

        } else if (id == R.id.action_help) {
            showHelp();
            return true;

        } else if (id == R.id.action_about) {
            showAbout();
            return true;

        } else if (id == android.R.id.home) {
            onHomePressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    protected void showToday()
    {
        scrollToPosition(SolunarCardAdapter.TODAY_POSITION, false);
        if (cardAdapter != null) {
            showBottomSheet(SolunarCardAdapter.TODAY_POSITION, cardAdapter.initData(SolunarCardAdapter.TODAY_POSITION));
        }
    }

    protected void showDateDialog()
    {
        final long todayMillis = cardAdapter != null ? cardAdapter.initData(SolunarCardAdapter.TODAY_POSITION).getDateMillis() : Calendar.getInstance().getTimeInMillis();
        long rangeMillis = (((SolunarCardAdapter.MAX_POSITIONS / 2L) - 2) * (24 * 60 * 60 * 1000L));

        DateDialog dialog = new DateDialog();
        dialog.setTheme(AppThemeInfo.themePrefToStyleId(MainActivity.this, AppThemeInfo.themeNameFromInfo(suntimesInfo)));
        dialog.setDateRange(todayMillis - rangeMillis, todayMillis + rangeMillis);
        dialog.setFragmentListener(dateDialogListener);

        int firstVisiblePosition = cardLayout.findFirstVisibleItemPosition();
        dialog.setDate((firstVisiblePosition >= 0 && cardAdapter != null) ? cardAdapter.initData(firstVisiblePosition).getDateMillis() : todayMillis);
        dialog.show(getSupportFragmentManager(), DIALOG_DATE);
    }

    private final DateDialog.FragmentListener dateDialogListener = new DateDialog.FragmentListener()
    {
        @Override
        public void onAccepted(int year, int month, int day)
        {
            if (cardAdapter != null)
            {
                Calendar date = Calendar.getInstance(cardAdapter.getTimeZone());
                date.set(year, month, day);
                int position = cardAdapter.findPositionForDate(date);
                scrollToPosition(position, false);

                if (!isBottomSheetShowing()) {
                    showBottomSheet(position, cardAdapter.initData(position));
                }
            }
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

    protected final BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback()
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
        AddonHelper.startSuntimesSettingsActivity_calendarIntegration(MainActivity.this, ((suntimesInfo != null && suntimesInfo.appCode != null) ? suntimesInfo.appCode : -1));
    }

    protected void showSettings()
    {
        String themeName = AppThemeInfo.themeNameFromInfo(suntimesInfo);
        int themeResID = AppThemeInfo.themePrefToStyleId(MainActivity.this, themeName);
        int themeNightMode = themes.loadThemeInfo(themeName).getDefaultNightMode();

        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra( SettingsActivity.EXTRA_THEME_RESID, themeResID );
        intent.putExtra( SettingsActivity.EXTRA_THEME_NIGHTMODE, themeNightMode );
        intent.putExtra( PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.GeneralPreferenceFragment.class.getName() );
        intent.putExtra( PreferenceActivity.EXTRA_NO_HEADERS, true );
        startActivity(intent);
    }

    private static final int HELP_PATH_ID = R.string.help_main_path;

    protected void showHelp() {
        HelpDialog dialog = createHelpDialog(this, suntimesInfo, R.array.help_topics, DIALOG_HELP, HELP_PATH_ID);
        dialog.show(getSupportFragmentManager(), DIALOG_HELP);
    }
    public static HelpDialog createHelpDialog(Context context, @Nullable SuntimesInfo suntimesInfo, int helpTopicsArrayRes, String dialogTag, Integer helpPathID)
    {
        HelpDialog dialog = new HelpDialog();
        if (suntimesInfo != null && suntimesInfo.appTheme != null) {
            dialog.setTheme(AppThemeInfo.themePrefToStyleId(context, AppThemeInfo.themeNameFromInfo(suntimesInfo)));
        }
        if (helpPathID != null) {
            dialog.setShowNeutralButton(context.getString(R.string.action_onlineHelp));
            dialog.setNeutralButtonListener(HelpDialog.getOnlineHelpClickListener(context, helpPathID), dialogTag);
        }

        String[] help = context.getResources().getStringArray(helpTopicsArrayRes);
        String helpContent = help[0];
        for (int i=1; i<help.length; i++) {
            helpContent = context.getString(R.string.format_help, helpContent, help[i]);
        }
        dialog.setContent(helpContent + "<br/>");
        return dialog;
    }

    protected void showAbout()
    {
        AboutDialog dialog = new AboutDialog();
        dialog.setTheme(AppThemeInfo.themePrefToStyleId(MainActivity.this, AppThemeInfo.themeNameFromInfo(suntimesInfo)));
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
        MenuItem[] items = new MenuItem[] {itemSystem, itemSuntimes, menu.findItem(R.id.action_timezone_localmean), menu.findItem(R.id.action_timezone_apparentsolar), menu.findItem(R.id.action_timezone_utc)};

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
    private final PopupMenu.OnMenuItemClickListener onTimeZonePopupMenuItemSelected = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            int itemId = item.getItemId();
            if (itemId == R.id.action_timezone_utc || itemId == R.id.action_timezone_system || itemId == R.id.action_timezone_suntimes || itemId == R.id.action_timezone_localmean || itemId == R.id.action_timezone_apparentsolar)
            {
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
        int itemId = item.getItemId();
        if (itemId == R.id.action_timezone_utc) {
            return AppSettings.TZMODE_UTC;

        } else if (itemId == R.id.action_timezone_suntimes) {
            return AppSettings.TZMODE_SUNTIMES;

        } else if (itemId == R.id.action_timezone_localmean) {
            return AppSettings.TZMODE_LOCALMEAN;

        } else if (itemId == R.id.action_timezone_system) {
            return AppSettings.TZMODE_SYSTEM;
        }
        return AppSettings.TZMODE_APPARENTSOLAR;
    }


}
