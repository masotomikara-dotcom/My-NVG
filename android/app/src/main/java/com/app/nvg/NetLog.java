package com.app.nvg;

import android.app.Activity;
import android.net.TrafficStats;
import android.widget.TextView;

public class NetLog {
	private final Activity activity;
    private final TextView tvSpeed;
    private long oldDown = 0;
    private long oldUp = 0;
    private boolean isRunning = true;
	
	public NetLog(Activity activity, TextView tvSpeed) {
		this.activity = activity;
		this.tvSpeed = tvSpeed;;
	}
	public void start() {
		oldDown = TrafficStats.getTotalRxBytes();
		oldUp = TrafficStats.getTotalTxBytes();
		
		new Thread(() -> {
			try  {
				while(isRunning) {
					Thread.sleep(1000);
					long newDown = TrafficStats.getTotalRxBytes();
					long newUp = TrafficStats.getTotalTxBytes();
					
					long SpeedDown = (newDown - oldDown) / 1024;
					long SpeedUp = (newUp - oldUp) / 1024;
					
					oldDown = newDown;
					oldUp = newUp;
					
					activity.runOnUiThread(() -> 
						tvSpeed.setText("↓" + SpeedDown + "KB/s | ↑ " + SpeedUp + "KB/s")
					);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}
	public void stop() {
		isRunning = false;
	}
}