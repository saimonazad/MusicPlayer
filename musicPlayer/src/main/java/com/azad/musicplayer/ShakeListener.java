

package com.azad.musicplayer;

import android.content.*;
import android.hardware.*;
import android.preference.*;

/* Shake sensor listener */
class ShakeListener implements SensorEventListener {
	private MusicService musicService;
	private SharedPreferences preferences;
	private boolean enabled = false;
	private SensorManager sensorManager;
	
	private long lastUpdate = -1;
    private float x, y, z;
    private float last_x, last_y, last_z;
    private long lastShake = -1;
    
    private long shakeInterval;
    private int shakeThreshold;
	
	public ShakeListener(MusicService musicService) {
		this.musicService = musicService;
		preferences = PreferenceManager.getDefaultSharedPreferences(musicService.getApplicationContext());
		sensorManager = (SensorManager)musicService.getSystemService(Context.SENSOR_SERVICE);
		
		String shakeIntervalString = preferences.getString(Constants.PREFERENCE_SHAKEINTERVAL, Constants.DEFAULT_SHAKEINTERVAL);
		if(shakeIntervalString==null) {
			shakeInterval = 1000;
		} else {
			try {
				shakeInterval = Integer.parseInt(shakeIntervalString);
			} catch(NumberFormatException e) {
				shakeInterval = 1000;
			}
		}
		
		String shakeThresholdString = preferences.getString(Constants.PREFERENCE_SHAKETHRESHOLD, Constants.DEFAULT_SHAKETHRESHOLD);
		if(shakeThresholdString==null) {
			shakeThreshold = 1000;
		} else {
			try {
				shakeThreshold = Integer.parseInt(shakeThresholdString);
			} catch(NumberFormatException e) {
				shakeThreshold = 1000;
			}
		}
	}
	
	public void enable() {
		if(enabled) return;
		sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		enabled = true;
	}
	
	public void disable() {
		if(!enabled) return;
		sensorManager.unregisterListener(this);
		enabled = false;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		long currTime = System.currentTimeMillis();
	    // Only allow one update every 100ms.
	    if ((currTime-lastUpdate)>100 && (currTime-lastShake)>shakeInterval) {
			long diffTime = (currTime - lastUpdate);
			lastUpdate = currTime;
			
			x = event.values[0];
			y = event.values[1];
			x = event.values[2];
	 
			float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;
			if (speed > shakeThreshold) {
				String shakeAction = preferences.getString(Constants.PREFERENCE_SHAKEACTION, Constants.DEFAULT_SHAKEACTION);
				switch(shakeAction) {
                    case "playpause":
                        musicService.playPause();
                        break;
                    case "next":
                        musicService.nextItem();
                        break;
                    case "previous":
                        musicService.previousItem(true);
                        break;
                }
			    lastShake = currTime;
			}
			last_x = x;
			last_y = y;
			last_z = z;
	    }
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
