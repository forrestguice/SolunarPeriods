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
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarData;

@SuppressWarnings("Convert2Diamond")
public class SolunarDaySheet extends BottomSheetDialogFragment
{
    public static final String KEY_DATA = "data";
    public static final String KEY_DIALOGTHEME = "themeResID";
    protected static final int DEF_DIALOGTHEME = R.style.SolunarAppTheme_Dark;

    private SolunarCardHolder card;

    public SolunarDaySheet()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME);
        setArguments(defaultArgs);
    }

    public void setData(SolunarData data)
    {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putParcelable(KEY_DATA, data);
        setArguments(args);
        updateViews(getContext());
    }
    public SolunarData getData() {
        return (getArguments() != null) ? ((SolunarData) getArguments().getParcelable(KEY_DATA)) : null;
    }

    private SolunarCardAdapter.SolunarCardOptions options;
    protected SolunarCardAdapter.SolunarCardOptions getCardOptions(Context context) {
        if (options == null) {
            options = new SolunarCardAdapter.SolunarCardOptions(context);
        }
        return options;
    }
    public void setCardOptions(SolunarCardAdapter.SolunarCardOptions options) {
        this.options = options;
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

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        android.support.v7.view.ContextThemeWrapper contextWrapper = new android.support.v7.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.dialog_solunarday, parent, false);
        initViews(getContext(), dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }
        return dialogContent;
    }
    private void initViews(Context context, View dialogContent)
    {
        card = new SolunarCardHolder(dialogContent, getCardOptions(context));
    }
    public void updateViews(Context context) {
        if (card != null) {
            card.onBindViewHolder(context, 0, getData(), getCardOptions(context));
        }
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

    @Override
    public void onCancel(DialogInterface dialog) {
    }

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
    }
}