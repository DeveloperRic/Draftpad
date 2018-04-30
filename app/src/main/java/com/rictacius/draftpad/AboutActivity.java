package com.rictacius.draftpad;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.ND_toolbar));

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.about_visitWebsite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://victorolaitan.xyz"));
                startActivity(Intent.createChooser(browserIntent, "Visit my website"));
            }
        });

        findViewById(R.id.about_creditsAFAB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://github.com/futuresimple/android-floating-action-button"));
                startActivity(Intent.createChooser(browserIntent, "Android Floating Action Button"));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_fade_in, R.anim.trans_fade_out);
    }

    public void gitHubClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DeveloperRic"));
        startActivity(Intent.createChooser(browserIntent, "Open GitHub page"));
    }

    public void twitterClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/navictoro"));
        startActivity(Intent.createChooser(browserIntent, "Open twitter page"));
    }

    public void emailClick(View view) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"victor.n.olaitan@gmail.com"});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "About your app Draftpad");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hi, \n Just wanted to tell you I love your project app Daraftpad :)");
        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }
}
