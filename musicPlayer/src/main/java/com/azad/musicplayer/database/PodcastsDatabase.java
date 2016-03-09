

package com.azad.musicplayer.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.azad.musicplayer.MusicPlayerApplication;

public class PodcastsDatabase extends SQLiteOpenHelper {
	private static final String DB_NAME = "Podcasts";
	private static final int DB_VERSION = 1;
	
	public PodcastsDatabase() {
		super(MusicPlayerApplication.getContext(), DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Podcasts (id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, name TEXT, image BLOB)");
		db.execSQL("CREATE TABLE ItemsInPodcast (idItem TEXT PRIMARY KEY, idPodcast INTEGER, title TEXT, status INTEGER, url TEXT, filename TEXT, pubDate INTEGER, duration TEXT, type TEXT)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
