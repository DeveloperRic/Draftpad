package com.rictacius.draftpad;

import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Victor on 12/12/2017.
 */

public class ActivityManager {
    /**
     * Created by Victor on 10/12/2017.
     */

    public static class AppBar {

        enum VisibilityToken {
            SHOW_MORE, SHOW_EXTRA, HIDE_TEXT, HIDE_BUTTON, HIDE_TITLE
        }

        public static void init(AppCompatActivity activity, VisibilityToken... tokens) {

            List<VisibilityToken> tokenList = Arrays.asList(tokens);
            boolean title = !tokenList.contains(VisibilityToken.HIDE_TITLE);
            boolean more = tokenList.contains(VisibilityToken.SHOW_MORE);
            boolean extra = tokenList.contains(VisibilityToken.SHOW_EXTRA);
            boolean text = !tokenList.contains(VisibilityToken.HIDE_TEXT) && extra;
            boolean button = !tokenList.contains(VisibilityToken.HIDE_BUTTON) && extra;

            if (title) {
                activity.findViewById(R.id.appbar_title).setVisibility(View.VISIBLE);
            } else {
                activity.findViewById(R.id.appbar_title).setVisibility(View.GONE);
            }

            if (more) {
                activity.findViewById(R.id.appbar_more).setVisibility(View.VISIBLE);
            } else {
                activity.findViewById(R.id.appbar_more).setVisibility(View.INVISIBLE);
            }

            if (extra) {
                activity.findViewById(R.id.appbar_extra).setVisibility(View.VISIBLE);

                if (text) {
                    activity.findViewById(R.id.appbar_extraText).setVisibility(View.VISIBLE);
                } else {
                    activity.findViewById(R.id.appbar_extraText).setVisibility(View.GONE);
                }

                if (button) {
                    activity.findViewById(R.id.appbar_extraButton).setVisibility(View.VISIBLE);
                } else {
                    activity.findViewById(R.id.appbar_extraButton).setVisibility(View.GONE);
                }
            } else {
                activity.findViewById(R.id.appbar_extra).setVisibility(View.GONE);
            }

            setTitleText(activity, R.string.app_name);
            setInputText(activity, "");
        }

        public static void setTitleText(AppCompatActivity activity, int text) {
            ((TextView) activity.findViewById(R.id.appbar_title)).setText(text);
        }

        public static EditText getInputText(AppCompatActivity activity) {
            return (EditText) activity.findViewById(R.id.appbar_extraText);
        }

        public static void setInputText(AppCompatActivity activity, String text) {
            ((EditText) activity.findViewById(R.id.appbar_extraText)).setText(text);
        }

        public static void setInputTextHint(AppCompatActivity activity, int text) {
            ((EditText) activity.findViewById(R.id.appbar_extraText)).setHint(text);
        }

        public static TextView getButton(AppCompatActivity activity) {
            return (TextView) activity.findViewById(R.id.appbar_extraButton);
        }

        public static void setButtonName(AppCompatActivity activity, int name) {
            ((TextView) activity.findViewById(R.id.appbar_extraButton)).setText(name);
        }
    }

    public static void initActivity(AppCompatActivity activity) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setNavigationBarColor(activity.getResources().getColor(R.color.colorAccent));
        }
    }
}
