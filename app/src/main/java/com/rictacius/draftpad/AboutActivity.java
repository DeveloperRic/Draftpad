package com.rictacius.draftpad;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
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
