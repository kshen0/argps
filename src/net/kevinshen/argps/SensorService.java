package net.kevinshen.argps;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SensorService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// Not a bound service; return null
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handleCommand(intent);
		return START_STICKY;
	}
	
	private void handleCommand(Intent intent) {
		
	}
	
	@Override
	public void onDestroy() {
		// TODO: unregister listener
		super.onDestroy();
	}

}
