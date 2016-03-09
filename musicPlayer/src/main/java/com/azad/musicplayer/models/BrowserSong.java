

package com.azad.musicplayer.models;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.azad.musicplayer.R;
import com.azad.musicplayer.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class BrowserSong implements PlayableItem, Serializable {
	private static final long serialVersionUID = 1L;
	private String title, artist;
	private int trackNumber = 0;
	private String uri;
	private BrowserDirectory browserDirectory;

	public BrowserSong(String uri, String artist, String title, String trackNumberString, BrowserDirectory browserDirectory) {
		this.uri = uri;
		this.artist = artist;
		this.title = title;
        try {
            this.trackNumber = Integer.parseInt(trackNumberString);
        } catch(Exception e) {}
		this.browserDirectory = browserDirectory;
	}

	public BrowserSong(String uri, BrowserDirectory browserDirectory) {
		this.uri = uri;
		this.browserDirectory = browserDirectory;
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(uri);
			title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
			artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
			if (title == null || title.equals("")) title = new File(uri).getName();
			if (artist == null) artist = "";
			try {
				trackNumber = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER));
			} catch(Exception ex) {}
		} catch(Exception e) {
			title = new File(uri).getName();
			artist = "";
		} finally {
			mmr.release();
		}
	}
	
	public BrowserSong(String uri) {
		this(uri, new BrowserDirectory(new File(uri).getParentFile()));
	}

	public void setBrowser(BrowserDirectory browserDirectory) {
		this.browserDirectory = browserDirectory;
	}
	
	@Override
	public String getArtist() {
		return artist;
	}
	
	@Override
	public String getTitle() {
		return title;
	}

	public int getTrackNumber() {
		return trackNumber;
	}

	public String getUri() {
		return uri;
	}
	
	@Override
	public boolean hasImage() {
		return true;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	@Override
	public Bitmap getImage() {
		return Utils.getMusicFileImage(uri);
	}

	@Override
	public boolean equals(Object o) {
		if(!(o instanceof BrowserSong)) return false;
		BrowserSong s2 = (BrowserSong)o;
		return uri.equals(s2.uri);
	}

	@Override
	public String getPlayableUri() {
		return uri;
	}

	@Override
	public PlayableItem getNext(boolean repeatAll) {
		ArrayList<BrowserSong> songs = browserDirectory.getSongs();
		int index = songs.indexOf(this);
		if(index<songs.size()-1) {
			return songs.get(index+1);
		} else {
			if(repeatAll) return songs.get(0);
		}
		return null;
	}

	@Override
	public PlayableItem getPrevious() {
		ArrayList<BrowserSong> songs = browserDirectory.getSongs();
		int index = songs.indexOf(this);
		if(index>0) {
			return songs.get(index-1);
		} else {
			return null;
		}
	}

	@Override
	public PlayableItem getRandom(Random random) {
		ArrayList<BrowserSong> songs = browserDirectory.getSongs();
		return songs.get(random.nextInt(songs.size()));
	}

	@Override
	public boolean isLengthAvailable() {
		return true;
	}

    public BrowserDirectory getBrowserDirectory() {
        return browserDirectory;
    }
	
	@Override
	public ArrayList<Information> getInformation() {
		String bitrate=null, album=null, year=null;
		
		// Get additional information from file
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(uri);
			bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
			album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
			year = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_YEAR);
		} catch(Exception e) {
		} finally {
			mmr.release();
		}
		
		ArrayList<Information> info = new ArrayList<>();
		info.add(new Information(R.string.artist, artist));
		info.add(new Information(R.string.title, title));
		if(year!=null) info.add(new Information(R.string.year, year));
		if(album!=null) info.add(new Information(R.string.album, album));
		if(trackNumber>0) {
			info.add(new Information(R.string.trackNumber, trackNumber+""));
		} else {
			info.add(new Information(R.string.trackNumber, "-"));
		}
		info.add(new Information(R.string.fileName, uri));
		info.add(new Information(R.string.fileSize, Utils.getFileSize(uri)));
		if(bitrate!=null) {
			try {
				int kbps = Integer.parseInt(bitrate)/1000;
				info.add(new Information(R.string.bitrate, kbps+" kbps"));
			} catch(Exception e) {}
		}
		
		return info;
	}
}
