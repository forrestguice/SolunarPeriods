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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;

import com.forrestguice.suntimes.solunar.R;

import java.util.Calendar;
import java.util.TimeZone;

@SuppressWarnings("Convert2Diamond")
public class DateDialog extends BottomSheetDialogFragment
{
    public static final String KEY_DIALOGTHEME = "themeResID";
    protected static final int DEF_DIALOGTHEME = R.style.SolunarAppTheme_Dark;

    public static final String KEY_DATE = "dateMilis";
    public static final String KEY_DATE_MIN = "maxDateMillis";
    public static final String KEY_DATE_MAX = "minDateMillis";

    private DatePicker picker;

    public DateDialog()
    {
        super();

        Bundle defaultArgs = new Bundle();
        defaultArgs.putInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME);
        defaultArgs.putLong(KEY_DATE, Calendar.getInstance().getTimeInMillis());
        defaultArgs.putLong(KEY_DATE_MAX, -1);
        defaultArgs.putLong(KEY_DATE_MIN, -1);
        setArguments(defaultArgs);
    }

    public void setTheme(int themeResID)
    {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putInt(KEY_DIALOGTHEME, themeResID);
        setArguments(args);
    }
    public int getThemeResID() {
        return (getArguments() != null) ? getArguments().getInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME) : DEF_DIALOGTHEME;
    }

    public void setDateRange(long minDateMillis, long maxDateMillis) {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putLong(KEY_DATE_MIN, minDateMillis);
        args.putLong(KEY_DATE_MAX, maxDateMillis);
        setArguments(args);
    }
    public void setDate(long dateMillis) {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putLong(KEY_DATE, dateMillis);
        setArguments(args);
    }

    protected void init()
    {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(args.getLong(KEY_DATE, calendar.getTimeInMillis()));
        init(calendar);
    }
    protected void init(Calendar date) {
        init(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    protected void init(int year, int month, int day)
    {
        Bundle args = getArguments() != null ? getArguments() : new Bundle();
        long minDate = args.getLong(KEY_DATE_MIN, -1);
        if (minDate >= 0) {
            picker.setMinDate(minDate);
        }

        long maxDate = args.getLong(KEY_DATE_MAX, -1);
        if (maxDate >= 0) {
            picker.setMaxDate(maxDate);
        }

        picker.init(year, month, day, null);
    }

    /**
     * @param context a context used to access resources
     * @param dialogContent an inflated layout containing the dialog's other views
     */
    private void initViews(Context context, View dialogContent)
    {
        picker = (DatePicker) dialogContent.findViewById(R.id.appwidget_date_custom);

        Button btn_cancel = (Button) dialogContent.findViewById(R.id.dialog_button_cancel);
        btn_cancel.setOnClickListener(onDialogCancelClick);

        Button btn_accept = (Button) dialogContent.findViewById(R.id.dialog_button_accept);
        btn_accept.setOnClickListener(onDialogAcceptClick);

        Button btn_neutral = (Button) dialogContent.findViewById(R.id.dialog_button_neutral);
        btn_neutral.setOnClickListener(onDialogNeutralClick);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.layout_dialog_date1, parent, false);
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }
        init();
        return dialogContent;
    }

    @SuppressWarnings({"deprecation","RestrictedApi"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle) {
    }
    protected void saveSettings(Bundle bundle) {
    }

    public boolean isToday()
    {
        Calendar date = Calendar.getInstance(TimeZone.getDefault());
        return isToday(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
    }
    public boolean isToday(int year, int month, int day) {
        return (year == picker.getYear() && month == picker.getMonth() && day == picker.getDayOfMonth());
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog) {
            // EMPTY; placeholder
        }
    };

    private View.OnClickListener onDialogNeutralClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            boolean alreadyToday = isToday();
            init(Calendar.getInstance(TimeZone.getDefault()));
            if (alreadyToday) {
                onDialogAcceptClick.onClick(v);
            }
        }
    };

    private View.OnClickListener onDialogCancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            getDialog().cancel();
        }
    };

    @Override
    public void onCancel(DialogInterface dialog)
    {
        if (listener != null) {
            listener.onCanceled();
        }
    }

    private View.OnClickListener onDialogAcceptClick = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            if (getShowsDialog()) {
                dismiss();
            }
            if (listener != null && picker != null) {
                listener.onAccepted(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
            }
        }
    };

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
        if (layout != null)
        {
            BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    protected FragmentListener listener = null;
    public void setFragmentListener(FragmentListener l) {
        listener = l;
    }

    public interface FragmentListener {
        void onAccepted( int year, int month, int day );
        void onCanceled();
    }
}
