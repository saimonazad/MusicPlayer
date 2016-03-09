
package com.azad.musicplayer.models;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.azad.musicplayer.MusicPlayerApplication;
import com.azad.musicplayer.R;
import com.azad.musicplayer.Utils;
import com.azad.musicplayer.database.PodcastsDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PodcastEpisode implements PlayableItem {
	public static final int STATUS_NEW=0, STATUS_DOWNLOADING=1, STATUS_DOWNLOADED=2;
	
	private String id;
	private String title;
	private Podcast podcast;
	private int status;
	private String url;
	private String filename;
	private long pubDate;
	private String duration;
	private String type;
	
	public PodcastEpisode(String url, String filename, String title, String id, Podcast podcast, int status, long pubDate, String duration, String type) {
		this.id = id;
		this.url = url;
		this.title = title;
		this.podcast = podcast;
		this.status = status;
		this.filename = filename;
		this.pubDate = pubDate;
		this.duration = duration;
		this.type = type;
	}
	
	public void setPodcast(Podcast podcast) {
		this.podcast = podcast;
	}
	
	public String getId() {
		return id;
	}
	
	public String getUrl() {
		return url;
	}
	
	public String getTitle() {
		return title;
	}
	
	public long getPubDate() {
		return pubDate;
	}
	
	public String getDuration() {
		return duration;
	}
	
	public Podcast getPodcast() {
		return podcast;
	}
	
	public int getStatus() {
		if(status==STATUS_DOWNLOADED) { // Checks if file is still available
			if(!new File(filename).exists()) {
				setStatus(STATUS_NEW);
			}
		}
		return status;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void setStatus(int status) {
		this.status = status;
		update();
	}
	
	public static void setDownloadedFile(String podcastItemId, String filename) {
		ContentValues values = new ContentValues();
		values.put("status", PodcastEpisode.STATUS_DOWNLOADED);
		values.put("filename", filename);
		PodcastsDatabase podcastsDatabase = new PodcastsDatabase();
		SQLiteDatabase db = podcastsDatabase.getWritableDatabase();
		db.update("ItemsInPodcast", values, "idItem=\""+podcastItemId+"\"", null);
		db.close();
	}
	
	public static void setDownloadCanceled(String podcastItemId) {
		ContentValues values = new ContentValues();
		values.put("status", PodcastEpisode.STATUS_NEW);
		PodcastsDatabase podcastsDatabase = new PodcastsDatabase();
		SQLiteDatabase db = podcastsDatabase.getWritableDatabase();
		db.update("ItemsInPodcast", values, "idItem=\""+podcastItemId+"\"", null);
		db.close();
	}
	
	public String getStatusString() {
		Resources resources = MusicPlayerApplication.getContext().getResources();
		switch(status) {
		case STATUS_NEW:
			return resources.getString(R.string.podcastStatusNew);
		case STATUS_DOWNLOADING:
			return resources.getString(R.string.podcastStatusDownloading);
		case STATUS_DOWNLOADED:
			return resources.getString(R.string.podcastStatusDownloaded);
		}
		return null;
	}
	
	public String getType() {
		return type;
	}
	
	public void deleteDownloadedFile() {
		if((status==STATUS_DOWNLOADED) && filename!=null) {
			File file = new File(filename);
			try {
				file.delete();
			} catch(Exception e) {}
		}
	}
	
	private void update() {
		PodcastsDatabase podcastsDatabase = new PodcastsDatabase();
		SQLiteDatabase db = podcastsDatabase.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("status", status);
		values.put("filename", filename);
		db.update("ItemsInPodcast", values, "idItem=\""+id+"\"", null);
		db.close();
	}
	
	public void setStreaming() {
		filename = url;
	}

	@Override
	public String getArtist() {
		return podcast.getName();
	}

	@Override
	public String getPlayableUri() {
		return filename;
	}
	
	@Override
	public boolean hasImage() {
		return podcast.getImage()!=null;
	}
	
	@Override
	public Bitmap getImage() {
		return podcast.getImage();
	}

	@Override
	public PlayableItem getNext(boolean repeatAll) {
		return null;
	}

	@Override
	public PlayableItem getPrevious() {
		return null;
	}

	@Override
	public PlayableItem getRandom(Random random) {
		return null;
	}

	@Override
	public boolean isLengthAvailable() {
		return true;
	}
	
	@Override
	public ArrayList<Information> getInformation() {
		ArrayList<Information> info = new ArrayList<>();
		info.add(new Information(R.string.title, title));
		info.add(new Information(R.string.podcast, podcast.getName()));
		info.add(new Information(R.string.url, url));
		info.add(new Information(R.string.status, getStatusString()));
		if(status==STATUS_DOWNLOADED) {
			info.add(new Information(R.string.fileName, filename));
			info.add(new Information(R.string.fileSize, Utils.getFileSize(filename)));
		}
		return info;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PodcastEpisode)) return false;
		PodcastEpisode p2 = (PodcastEpisode)o;
		return p2.getId().equals(getId());
	}
}
