

package com.azad.musicplayer.models;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import com.azad.musicplayer.R;
import com.azad.musicplayer.Utils;

import java.util.ArrayList;
import java.util.Random;

/* Class representing a song in a playlist */
public class PlaylistSong implements PlayableItem {
	private String uri;
	private long id;
	private Playlist playlist;
	private String artist;
	private String title;
	private boolean hasImage;
	
	public PlaylistSong(String uri, String artist, String title, long id, boolean hasImage, Playlist playlist) {
		this.uri = uri;
		this.id = id;
		this.playlist = playlist;
		this.title = title;
		this.artist = artist;
		this.hasImage = hasImage;
	}
	
	public long getId() {
		return id;
	}
	
	public Playlist getPlaylist() {
		return playlist;
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof PlaylistSong)) return false;
		PlaylistSong s2 = (PlaylistSong)o;
		return id==s2.id;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	@Override
	public String getPlayableUri() {
		return uri;
	}
	
	@Override
	public boolean hasImage() {
		return hasImage;
	}
	
	@Override
	public Bitmap getImage() {
		return Utils.getMusicFileImage(uri);
	}

	@Override
	public PlayableItem getNext(boolean repeatAll) {
		ArrayList<PlaylistSong> songs = playlist.getSongs();
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
		ArrayList<PlaylistSong> songs = playlist.getSongs();
		int index = songs.indexOf(this);
		if(index>0) {
			return songs.get(index-1);
		} else {
			return null;
		}
	}

	@Override
	public PlayableItem getRandom(Random random) {
		ArrayList<PlaylistSong> songs = playlist.getSongs();
		return songs.get(random.nextInt(songs.size()));
	}

	@Override
	public boolean isLengthAvailable() {
		return true;
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
		info.add(new Information(R.string.playlist, playlist.getName()));
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
