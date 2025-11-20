// SPDX-License-Identifier: GPL-3.0-or-later
/*
    Copyright (C) 2020-2023 Forrest Guice
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
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.forrestguice.suntimes.addon.AddonHelper;
import com.forrestguice.suntimes.solunar.R;
import com.forrestguice.suntimes.solunar.data.SolunarData;

public class SolunarDaySheet extends BottomSheetDialogFragment
{
    public static final String KEY_POSITION = "position";
    public static final String KEY_DATA = "data";
    public static final String KEY_DIALOGTHEME = "themeResID";
    protected static final int DEF_DIALOGTHEME = R.style.SolunarAppTheme_Dark;

    private SolunarCardHolder card;

    public SolunarDaySheet()
    {
        super();
        Bundle defaultArgs = new Bundle();
        defaultArgs.putInt(KEY_DIALOGTHEME, DEF_DIALOGTHEME);
        defaultArgs.putInt(KEY_POSITION, 0);
        setArguments(defaultArgs);
    }

    public void setData(int position, SolunarData data)
    {
        Bundle args = (getArguments() != null) ? getArguments() : new Bundle();
        args.putInt(KEY_POSITION, position);
        args.putParcelable(KEY_DATA, data);
        setArguments(args);
        updateViews(getContext());
    }
    public SolunarData getData() {
        return (getArguments() != null) ? ((SolunarData) getArguments().getParcelable(KEY_DATA)) : null;
    }
    public int getPosition(){
        return (getArguments() != null) ? getArguments().getInt(KEY_POSITION) : 0;
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
        androidx.appcompat.view.ContextThemeWrapper contextWrapper = new androidx.appcompat.view.ContextThemeWrapper(getActivity(), getThemeResID());    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.dialog_solunarday, parent, false);
        initViews(contextWrapper, dialogContent);
        if (savedState != null) {
            loadSettings(savedState);
        }
        return dialogContent;
    }
    private void initViews(Context context, View dialogContent)
    {
        card = new SolunarCardHolder(context, dialogContent, getCardOptions(context));

        ImageButton overflowButton = (ImageButton) dialogContent.findViewById(R.id.overflow);
        if (overflowButton != null){
            overflowButton.setOnClickListener(onOverflowButtonClicked);
        }

        ImageButton backButton = (ImageButton) dialogContent.findViewById(R.id.back_button);
        if (backButton != null){
            backButton.setOnClickListener(onBackButtonClicked);
        }

        ImageButton shareButton = (ImageButton) dialogContent.findViewById(R.id.share_button);
        if (shareButton != null){
            shareButton.setOnClickListener(onShareButtonClicked);
        }
    }
    public void updateViews(Context context) {
        if (card != null) {
            card.onBindViewHolder(context, getPosition(), getData(), getCardOptions(context));
        }
    }

    @SuppressWarnings({"RestrictedApi"})
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(onDialogShow);
        return dialog;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState )
    {
        saveSettings(outState);
        super.onSaveInstanceState(outState);
    }

    protected void loadSettings(Bundle bundle) {
        /* EMPTY */
    }
    protected void saveSettings(Bundle bundle) {
        /* EMPTY */
    }

    @Override
    public void onResume()
    {
        super.onResume();
        expandSheet(getDialog());
    }

    private final DialogInterface.OnShowListener onDialogShow = new DialogInterface.OnShowListener()
    {
        @Override
        public void onShow(DialogInterface dialog) {
            // EMPTY; placeholder
        }
    };

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        /* EMPTY */
    }
    private final View.OnClickListener onBackButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onBackClicked();
            }
        }
    };

    private void expandSheet(DialogInterface dialog)
    {
        if (dialog == null) {
            return;
        }

        BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
        FrameLayout layout = (FrameLayout) bottomSheet.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (layout != null)
        {
            BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(layout);
            behavior.setHideable(false);
            behavior.setSkipCollapsed(true);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void openCalendar() {
        // TODO
        Toast.makeText(getContext(), "TODO", Toast.LENGTH_LONG).show();
    }

    public void shareCard()
    {
        Context context = getActivity();
        if (context != null)
        {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, DisplayStrings.formatCardSummary(context, getData(), options.timezone, options.suntimes_options.time_is24));
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, null));
        }
    }
    private final View.OnClickListener onShareButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            shareCard();
        }
    };

    public void showDateSuntimes()
    {
        if (getActivity() != null)
        {
            Intent intent = AddonHelper.intentForMainActivity();
            intent.putExtra("dateMillis", getData().getDateMillis());
            intent.setAction("suntimes.action.SHOW_CARD");
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);    // needed to trigger onNewIntent on already running activity
            AddonHelper.startActivity(getActivity(), intent);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void showOverflowMenu(View v)
    {
        Context context = getContext();
        if (context != null)
        {
            PopupMenu popup = new PopupMenu(getContext(), v);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.menu_daysheet, popup.getMenu());
            DisplayStrings.forceActionBarIcons(popup.getMenu());
            updateOverflowMenu(popup.getMenu());
            popup.setOnMenuItemClickListener(onOverflowMenuItemSelected);
            popup.show();
        }
    }
    private void updateOverflowMenu(Menu menu) {
        /* EMPTY */
    }
    private final PopupMenu.OnMenuItemClickListener onOverflowMenuItemSelected = new PopupMenu.OnMenuItemClickListener()
    {
        @Override
        public boolean onMenuItemClick(MenuItem item)
        {
            int itemId = item.getItemId();
            if (itemId == R.id.action_date_suntimes) {
                showDateSuntimes();
                return true;

            } else if (itemId == R.id.action_calendar) {
                openCalendar();
                return true;

            } else if (itemId == R.id.action_share) {
                shareCard();
                return true;
            }
            return false;
        }
    };
    private final View.OnClickListener onOverflowButtonClicked = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        showOverflowMenu(v);
    }
};

    ////////////////////////////////////////////////////////////////////////////////////////////////

    protected FragmentListener listener = null;
    public void setFragmentListener(FragmentListener l) {
        listener = l;
    }

    public interface FragmentListener {
        void onBackClicked();
    }
}
