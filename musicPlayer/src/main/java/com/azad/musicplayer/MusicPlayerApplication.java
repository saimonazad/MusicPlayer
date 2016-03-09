

package com.azad.musicplayer;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.azad.musicplayer.models.BrowserDirectory;

import java.io.File;

public class MusicPlayerApplication extends Application {
	private static Context context;
	private BrowserDirectory currentDirectory;
    public int currentPage = -1;
	private String lastSearch;
    public ImagesCache imagesCache;

	@Override
    public void onCreate(){
        super.onCreate();
        context = getApplicationContext();
        imagesCache = new ImagesCache(context);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("MusicPlayer", "Low memory condition!");
        imagesCache.clearCache();
    }

    public static Context getContext() {
        return context;
    }
    
    public BrowserDirectory getCurrentDirectory() {
		return currentDirectory;
	}
    
    /* Moves to a new directory */
	public void gotoDirectory(File directory) {
		currentDirectory = new BrowserDirectory(directory);
	}
	
	public void setLastSearch(String lastSearch) {
		this.lastSearch = lastSearch;
	}
	
	public String getLastSearch() {
		return lastSearch;
	}
}
