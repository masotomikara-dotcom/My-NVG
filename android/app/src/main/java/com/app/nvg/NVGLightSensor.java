package com.app.nvg;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.hardware.lights.Light;

public class NVGLightSensor implements SensorEventListener {
	private SensorManager sensorManager;
	private Sensor lightSensor;
	private float currentExposure = 4.0f;
	private float currentLux = 0.0f;
	
	public NVGLightSensor(Context context) {
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager != null) {
			lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		}
	}
	public void start() {
		if (lightSensor != null) {
			sensorManager.registerListener(this, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
		}
	}
	public void stop() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(this);
		}
	}
	public float getCurrentExposure() {
		return currentExposure;
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
			float lux = event.values[0];
			this.currentLux = lux;
			
			if (lux > 100.0) {
				currentExposure = 1.0f;
			} else if (lux < 5.0f) {
				currentExposure = 8.5f;
			} else {
				currentExposure = 4.0f * (1.0f - (lux / 100.0f));
				if (currentExposure < 1.0f) currentExposure = 1.0f;
			}
		}
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		
	}
	public float getCurrentLux() {
		return currentLux;
	}
}