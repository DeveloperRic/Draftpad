package com.rictacius.draftpad;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        new GetDetailsTask().execute("http://victorolaitan.xyz/projects/draftpad/about_me.html", "1");

        findViewById(R.id.about_visitWebsite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://victorolaitan.xyz"));
                startActivity(Intent.createChooser(browserIntent, "Visit my website"));
            }
        });
    }

    public class GetDetailsTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(final String... urlString) {
            try {
                URL url = new URL(urlString[0]);

                URLConnection con = url.openConnection();
                InputStream is = con.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                String line = br.readLine();
                StringBuilder text = new StringBuilder(line != null ? line : "");

                while ((line = br.readLine()) != null) {
                    text.append("\n").append(line);
                }

                return text.toString();
            } catch (Exception e) {
                this.exception = e;
                final int tries = Integer.parseInt(urlString[1]);
                Snackbar snackbar = null;
                if (tries == 1) {
                    snackbar = Snackbar.make(findViewById(R.id.activity_about), "Offline info", Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(Color.GREEN)
                            .setAction("VIEW ONLINE", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new GetDetailsTask().execute(urlString[0], String.valueOf(tries + 1));
                                }
                            });
                } else if (tries < 3) {
                    snackbar = Snackbar.make(findViewById(R.id.activity_about), "Retry failed!", Snackbar.LENGTH_INDEFINITE)
                            .setActionTextColor(Color.RED)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    new GetDetailsTask().execute(urlString[0], String.valueOf(tries + 1));
                                }
                            });
                } else {
                    snackbar = Snackbar.make(findViewById(R.id.activity_about), "Viewing offline", Snackbar.LENGTH_INDEFINITE);
                }
                snackbar.show();
                return "I'm a student who loves to code. I code in in VB, JavaScript, HTML, CSS, not forgetting the obvious; Android and Java.";
            }
        }

        protected void onPostExecute(String details) {
            ((TextView) findViewById(R.id.about_txtAboutMe)).setText(details);
        }
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
