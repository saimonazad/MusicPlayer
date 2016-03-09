

package com.azad.musicplayer;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.text.DecimalFormat;

public class Utils {
	/* Builds a simple message dialog with title */
	public static void showMessageDialog(Context context, int title, int message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.ok, null);
		builder.show();
	}

	/* Builds a simple message dialog without title */
	public static void showMessageDialog(Context context, int message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message);
		builder.setPositiveButton(R.string.ok, null);
		builder.show();
	}
	
	/* Gets file size from its uri */
	public static String getFileSize(String uri) {
		try {
			File file = new File(uri);
			return formatFileSize(file.length());
		} catch(Exception e) {
			return "";
		}
	}
	
	/* Converts size in byte to a more readable format */
	public static String formatFileSize(long size) {
		DecimalFormat df = new DecimalFormat("#.##");
		double kb = (double)(size)/1024.0;
		if(kb<1024) return df.format(kb) + " KiB";
		else return df.format(kb/1024.0) + " MiB";
	}
	
	/* Checks if the device is connected to a WiFi network */
	public static boolean isWifiConnected() {
		ConnectivityManager cm = (ConnectivityManager)(MusicPlayerApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE));
		NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi.isConnected();
	}
	
	public static String formatTime(int milliseconds) {
		String ret = "";
		int seconds = (milliseconds / 1000) % 60 ;
		int minutes = ((milliseconds / (1000*60)) % 60);
		int hours   = ((milliseconds / (1000*60*60)) % 24);
		if(hours>0) ret += hours+":";
		ret += minutes<10 ? "0"+minutes+":" : minutes+":";
		ret += seconds<10 ? "0"+seconds : seconds+"";
		return ret;
	}
	
	public static Bitmap getMusicFileImage(String uri) {
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(uri);
        } catch(Exception e) {
            return null;
        }
		byte[] imageBytes = mmr.getEmbeddedPicture();
		Bitmap image = null;
		if(imageBytes!=null) {
			image = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
		}
		mmr.release();
		return image;
	}
}
