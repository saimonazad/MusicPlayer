

package com.azad.musicplayer;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;

import com.azad.musicplayer.models.PodcastEpisode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class PodcastEpisodeDownloaderService extends IntentService {
	private final static int NOTIFICATION_INTERVAL = 1000; // Milliseconds
	private final static String STOP_DOWNLOAD_INTENT = "com.azad.musicplayer.stopdownload";
	private NotificationManager notificationManager;
	private NotificationCompat.Builder notificationBuilder;
	private PendingIntent stopDownloadPendingIntent;
	
	private InputStream input;
	private FileOutputStream output;
	
	private int totalRead;
	private int length;
	private String lengthString;
	private boolean downloadInProgress = false;
	
	public PodcastEpisodeDownloaderService() {
		super("PodcastItemDownloader");
	}
	
	@Override
	public void onCreate () {
		super.onCreate();
		notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setSmallIcon(android.R.drawable.stat_sys_download);
		notificationBuilder.setOngoing(true);
		notificationBuilder.setProgress(100, 0, true);
		stopDownloadPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(STOP_DOWNLOAD_INTENT), 0);
		notificationBuilder.addAction(R.drawable.quit, getResources().getString(R.string.stopDownload), stopDownloadPendingIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(STOP_DOWNLOAD_INTENT);
		BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
            	if(intent.getAction().equals(STOP_DOWNLOAD_INTENT)) {
            		downloadInProgress = false;
            	}
            }
        };
        registerReceiver(broadcastReceiver, intentFilter);
		
		
		String type = intent.getStringExtra("type");
		String title = intent.getStringExtra("title");
		String idItem = intent.getStringExtra("idItem");
		String podcastsDirectory = intent.getStringExtra("podcastsDirectory");
		notificationBuilder.setContentTitle(getResources().getString(R.string.podcastDownloading, title));
		notificationBuilder.setContentText(getResources().getString(R.string.podcastDownloading, title));
		notificationManager.notify(Constants.NOTIFICATION_PODCAST_ITEM_DOWNLOAD_ONGOING, notificationBuilder.build());

		String filename;
		do {
			filename = podcastsDirectory+"/"+UUID.randomUUID().toString();
		} while(new File(filename).exists()); // To avoid accidentally override an already existing file
		Intent intentCompleted = new Intent("com.azad.musicplayer.podcastdownloadcompleted");
		
		HttpURLConnection httpConnection = null;
		
		try {
			if(type.equalsIgnoreCase("audio/mpeg") || type.equalsIgnoreCase("audio/.mp3")) filename+=".mp3";
			else if(type.equalsIgnoreCase("audio/ogg")) filename+=".ogg";
			else throw new Exception("Unsupported format");
			
			URL url = new URL(intent.getStringExtra("url"));
	        httpConnection = (HttpURLConnection)url.openConnection();
	        if(httpConnection.getResponseCode()!=200) throw new Exception("Failed to connect");
	        length = httpConnection.getContentLength();
	        lengthString = Utils.formatFileSize(length);
	        input = httpConnection.getInputStream();
	        output = new FileOutputStream(filename);
	        
	        byte[] buffer = new byte[1024];
	        int read;
	        totalRead = 0;
	        downloadInProgress = true;
	        new NotificationThread().start();
	        while((read = input.read(buffer))>0) {
	        	if(!downloadInProgress) {
	        		input.close();
	        		throw new Exception();
	        	}
	        	output.write(buffer, 0, read);
	        	totalRead += read;
	        }
	        
	        intentCompleted.putExtra("success", true);
			PodcastEpisode.setDownloadedFile(idItem, filename);
			
			output.flush();
		    output.close();
		} catch(Exception e) {
			new File(filename).delete();
			PodcastEpisode.setDownloadCanceled(idItem);
			intentCompleted.putExtra("success", false);
			intentCompleted.putExtra("reason", e.getMessage());
			showErrorNotification(e.getMessage());
		} finally {
			try {
				httpConnection.disconnect();
			} catch(Exception e) {}
		}
		
		downloadInProgress = false;
		unregisterReceiver(broadcastReceiver);
		
		sendBroadcast(intentCompleted);
		notificationManager.cancel(Constants.NOTIFICATION_PODCAST_ITEM_DOWNLOAD_ONGOING);
	}

	private void showErrorNotification(String msg) {
		NotificationCompat.Builder errorNotification = new NotificationCompat.Builder(this);
		errorNotification.setSmallIcon(android.R.drawable.stat_sys_download_done);
		errorNotification.setContentTitle(getResources().getString(R.string.error));
		errorNotification.setContentText(getResources().getString(R.string.podcastDownloadError)+": "+msg);
		notificationManager.notify(Constants.NOTIFICATION_PODCAST_ITEM_DOWNLOAD_ERROR, errorNotification.build());
	}
	
	private class NotificationThread extends Thread {
		public void run() {
			while(downloadInProgress) {
				String progress = Utils.formatFileSize(totalRead)+"/"+lengthString;
	    		notificationBuilder.setContentText(progress);
	        	notificationBuilder.setProgress(length, totalRead, false);
	        	notificationManager.notify(Constants.NOTIFICATION_PODCAST_ITEM_DOWNLOAD_ONGOING, notificationBuilder.build());
	        	try { Thread.sleep(NOTIFICATION_INTERVAL); } catch(Exception e) {}
			}
		}
	}
}
