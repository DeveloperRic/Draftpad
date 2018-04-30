package com.rictacius.draftpad;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.ImageViewCompat;
import android.text.Editable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;

/**
 * Created by Victor on 20/04/2018.
 */

class FormatBar {
    private boolean bold;
    private boolean italic;
    private boolean underline;
    private boolean alignLeft;
    private boolean alignCentre;
    private boolean alignRight;
    private boolean alignJustify;

    private HorizontalScrollView formatBar;

    private int start, end;

    FormatBar(final NoteDetailsActivity context, Note note, final EditText noteBody) {
        formatBar = context.findViewById(R.id.formatbar);
        final Editable bodyText = noteBody.getText();

        final ImageButton btnBold = context.findViewById(R.id.formatbar_bold);
        btnBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateSelection(noteBody)) {
                    bold = !bold;
                    setFormat(bold, bodyText, Typeface.BOLD);
                    toggleTint(context, btnBold, bold);
                }
            }
        });

        final ImageButton btnItalic = context.findViewById(R.id.formatbar_italic);
        btnItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateSelection(noteBody)) {
                    italic = !italic;
                    setFormat(italic, bodyText, Typeface.ITALIC);
                    toggleTint(context, btnItalic, italic);
                }
            }
        });

        final ImageButton btnUnderline = context.findViewById(R.id.formatbar_underline);
        /*btnUnderline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                italic = !italic;
                setFormat(underline, bodyText, Typeface.);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    btnBold.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.colorPrimary));
                }
            }
        });*/

        final ImageButton btnAlignLeft = context.findViewById(R.id.formatbar_alignLeft);
        final ImageButton btnAlignCentre = context.findViewById(R.id.formatbar_alignCentre);
        final ImageButton btnAlignRight = context.findViewById(R.id.formatbar_alignRight);
        final ImageButton btnAlignJustify = context.findViewById(R.id.formatbar_alignJustify);
    }

    void show() {
        formatBar.setVisibility(View.VISIBLE);
    }

    void hide() {
        formatBar.setVisibility(View.GONE);
    }

    private void toggleTint(Context context, ImageButton view, boolean toggle) {
        if (toggle) {
            ImageViewCompat.setImageTintList(view, ContextCompat.getColorStateList(context, R.color.colorPrimary));
        } else {
            ImageViewCompat.setImageTintList(view, null);
        }
    }

    private boolean updateSelection(EditText editText) {
        //TODO update format states & toggle tint for enabled formats
        start = editText.getSelectionStart();
        end = editText.getSelectionEnd();
        return start != -1 && end != -1;
    }

    private void setFormat(boolean toggle, Editable bodyText, int format) {
        try {
            if (toggle) {
                bodyText.setSpan(new StyleSpan(format),
                        start,
                        end,
                        format);
            } else {
                StyleSpan[] styleSpans = bodyText.getSpans(start,
                        end, StyleSpan.class);
                for (CharacterStyle span : styleSpans) {
                    if (span instanceof StyleSpan && ((StyleSpan) span).getStyle() == format)
                        bodyText.removeSpan(span);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setFormat(boolean toggleA, boolean toggleB, Editable bodyText, int formatA, int formatB, int formatAB) {
        if (toggleA && toggleB) {
            setFormat(false, bodyText, formatA);
            setFormat(false, bodyText, formatB);
            setFormat(true, bodyText, formatAB);
        } else if (toggleA) {
            setFormat(true, bodyText, formatA);
            setFormat(false, bodyText, formatB);
        } else if (toggleB) {
            setFormat(false, bodyText, formatA);
            setFormat(true, bodyText, formatB);
        } else {
            setFormat(false, bodyText, formatA);
            setFormat(false, bodyText, formatB);
        }
    }
}
