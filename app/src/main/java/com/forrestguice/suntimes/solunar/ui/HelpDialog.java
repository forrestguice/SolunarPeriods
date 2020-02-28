package com.forrestguice.suntimes.solunar.ui;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.forrestguice.suntimes.solunar.R;

public class HelpDialog extends BottomSheetDialogFragment
{
    public static final String KEY_HELPTEXT = "helpText";

    private TextView txtView;
    private CharSequence rawContent = "";
    public CharSequence getContent() {
        return rawContent;
    }
    public void setContent( String content ) {
        setContent((CharSequence)fromHtml(content));
    }

    public void setContent( CharSequence content )
    {
        rawContent = content;
        if (txtView != null) {
            txtView.setText(content);
        }
    }

    public static Spanned fromHtml(String htmlString )
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY);
        else return Html.fromHtml(htmlString);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedState)
    {
        // TODO
        //ContextThemeWrapper contextWrapper = new ContextThemeWrapper(getActivity(), AppSettings.loadTheme(getContext()));    // hack: contextWrapper required because base theme is not properly applied
        View dialogContent = inflater.cloneInContext(getActivity()).inflate(R.layout.dialog_help, parent, false);
        txtView = (TextView) dialogContent.findViewById(R.id.help_content);

        if (savedState != null) {
            rawContent = savedState.getCharSequence(KEY_HELPTEXT);
        }
        return dialogContent;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        txtView.setText(getContent());
        expandSheet(getDialog());
    }

    private static void expandSheet(DialogInterface dialog)
    {
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
        out.putCharSequence(KEY_HELPTEXT, rawContent);
        super.onSaveInstanceState(out);
    }
}
