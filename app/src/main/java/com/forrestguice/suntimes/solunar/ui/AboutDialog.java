package com.forrestguice.suntimes.solunar.ui;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimes.solunar.BuildConfig;
import com.forrestguice.suntimes.solunar.R;

public class AboutDialog extends BottomSheetDialogFragment
{
    public static final String KEY_DIALOGTHEME = "themeResID";

    private int themeResID = R.style.SolunarAppTheme_Dark;
    public void setTheme(int themeResID) {
        this.themeResID = themeResID;
    }

    //private TextView txtView;

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        themeResID = ((savedState != null) ? savedState.getInt(KEY_DIALOGTHEME) : themeResID);
        ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), themeResID);    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(contextWrapper).inflate(R.layout.dialog_about, parent, false);

        TextView version = (TextView)dialogContent.findViewById(R.id.txt_about_version);
        version.setMovementMethod(LinkMovementMethod.getInstance());
        version.setText(fromHtml(htmlVersionString()));

        TextView support = (TextView)dialogContent.findViewById(R.id.txt_about_support);
        support.setMovementMethod(LinkMovementMethod.getInstance());
        support.setText(fromHtml(getString(R.string.app_support_url)));

        TextView url = (TextView)dialogContent.findViewById(R.id.txt_about_url);
        url.setMovementMethod(LinkMovementMethod.getInstance());
        url.setText(fromHtml(getString(R.string.app_url)));

        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        DialogInterface dialog = getDialog();
        if (dialog != null) {
            BottomSheetDialog bottomSheet = (BottomSheetDialog) dialog;
            FrameLayout layout = (FrameLayout) bottomSheet.findViewById(android.support.design.R.id.design_bottom_sheet);  // for AndroidX, resource is renamed to com.google.android.material.R.id.design_bottom_sheet
            if (layout != null) {
                BottomSheetBehavior behavior = BottomSheetBehavior.from(layout);
                behavior.setHideable(false);
                behavior.setSkipCollapsed(false);
                behavior.setPeekHeight(200);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle out ) {
        out.putInt(KEY_DIALOGTHEME, themeResID);
        super.onSaveInstanceState(out);
    }

    public String htmlVersionString()
    {
        String buildString = anchor(getString(R.string.app_commit_url) + BuildConfig.GIT_HASH, BuildConfig.GIT_HASH);
        String versionString = anchor(getString(R.string.app_changelog_url), BuildConfig.VERSION_NAME) + " " + smallText("(" + buildString + ")");
        if (BuildConfig.DEBUG) {
            versionString += " " + smallText("[" + BuildConfig.BUILD_TYPE + "]");
        }
        return getString(R.string.app_version, versionString);
    }


    public static String anchor(String url, String text) {
        return "<a href=\"" + url + "\">" + text + "</a>";
    }

    protected static String smallText(String text)
    {
        return "<small>" + text + "</small>";
    }

}
