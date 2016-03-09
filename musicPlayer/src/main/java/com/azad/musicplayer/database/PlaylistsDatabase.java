

package com.azad.musicplayer.database;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.azad.musicplayer.MusicPlayerApplication;

public class PlaylistsDatabase extends SQLiteOpenHelper {
	private static final String DB_NAME = "Playlists";
	private static final int DB_VERSION = 3;
	
	public PlaylistsDatabase() {
		super(MusicPlayerApplication.getContext(), DB_NAME, null, DB_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE Playlists (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, position INTEGER DEFAULT 0)");
		db.execSQL("CREATE TABLE SongsInPlaylist (idSong INTEGER, idPlaylist INTEGER, uri TEXT, artist TEXT, title TEXT, position INTEGER DEFAULT 0, hasImage INTEGER DEFAULT 1, PRIMARY KEY(idSong), FOREIGN KEY(idPlaylist) REFERENCES Playlists (id))");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if(oldVersion==1 && newVersion==2) from1to2(db);
		if(oldVersion==2 && newVersion==3) from2to3(db);
		if(oldVersion==1 && newVersion==3) {from1to2(db);from2to3(db);}
	}
	
	private void from1to2(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE Playlists ADD position INTEGER DEFAULT 0");
		db.execSQL("ALTER TABLE SongsInPlaylist ADD position INTEGER DEFAULT 0");
	}
	
	private void from2to3(SQLiteDatabase db) {
		db.execSQL("ALTER TABLE SongsInPlaylist ADD hasImage INTEGER DEFAULT 1");
	}
}
