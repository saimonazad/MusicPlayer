

package com.azad.musicplayer;

import android.content.pm.PackageManager.*;
import android.content.res.*;
import android.os.*;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.*;
import android.widget.*;

public class AboutActivity extends ActionBarActivity {
	private TextView textViewAbout;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);       
        setContentView(R.layout.activity_about);
        textViewAbout = (TextView)findViewById(R.id.textViewAbout);
        textViewAbout.setMovementMethod(LinkMovementMethod.getInstance());
        Resources resources = getResources();
        
        String version = "";
        try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {}
        
        String about = "<h1>"+resources.getString(R.string.app_name)+"</h1>";
        about += "<p>"+resources.getString(R.string.version, version)+"</p>";
        about += "<p>&copy; 2012-2016 Azad</p>";
        about += "<p><a href=\"https://www.fiverr.com/saimonazad/make-you-a-music-player-app-for-android-with-source-code\">https://www.fiverr.com/saimonazad/make-you-a-music-player-app-for-android-with-source-code</a></p>";

        

        textViewAbout.setText(Html.fromHtml(about));
	}
}
