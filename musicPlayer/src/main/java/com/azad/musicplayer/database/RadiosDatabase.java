

package com.azad.musicplayer.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.azad.musicplayer.MusicPlayerApplication;

public class RadiosDatabase extends SQLiteOpenHelper {
	private static final String DB_NAME = "WebRadio";
	private static final int DB_VERSION = 1;
	
	public RadiosDatabase() {
		super(MusicPlayerApplication.getContext(), DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE WebRadio (url TEXT PRIMARY KEY, name TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
