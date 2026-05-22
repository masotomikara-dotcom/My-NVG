package com.app.nvg;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.net.TrafficStats;
import android.view.TextureView;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.widget.PopupMenu;
import android.graphics.Color;

public class MainActivity extends Activity implements TextureView.SurfaceTextureListener {

    private Button btnCamera;
	private Button btnSettings;
    private final int CAMERA_CODE = 100;
    private TextView tvSpeed;
    private TextureView cameraView;
    private Camera mCamera;
	private View colorFilter;
	private int currentMode = 0;
    private long lastUpdateTime = 0;

    private long oldDown = 0;
    private long oldUp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        btnCamera = findViewById(R.id.btnCamera);
        tvSpeed = findViewById(R.id.speed);
        cameraView = findViewById(R.id.cameraView);
        colorFilter = findViewById(R.id.colorFilter);
        btnSettings = findViewById(R.id.btnSettings);
        cameraView.setSurfaceTextureListener(this);
		
        startNetLog();
        
        btnCamera.setOnClickListener(v -> {
            Toast.makeText(this, "Đang chạy chế độ Video NVG!", Toast.LENGTH_SHORT).show();
        });
		btnSettings.setOnClickListener(v -> {
			PopupMenu popup = new PopupMenu(this, v);
            popup.getMenu().add("Chế độ: Mặc định");
            popup.getMenu().add("Chế độ: Xanh NVG");
            popup.getMenu().add("Chế độ: Cyan");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                
                if (title.contains("Mặc định")) {
                    currentMode = 0;
                } else if (title.contains("Xanh NVG")) {
                    currentMode = 1;
                } else if (title.contains("Cyan")) {
                    currentMode = 2;
                }
                refreshCamera();
                return true;
            });
            popup.show();
        });
    }
    private void refreshCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        startCameraPreview();
    }
    private void checkAndOpenCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraPreview();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
        }
    }

    private void startCameraPreview() {
    try {
        if (mCamera == null && cameraView.isAvailable()) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
            
            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            if (currentMode == 1) {
                        colorFilter.setBackgroundColor(Color.parseColor("#7700FF00"));
                    } else if (currentMode == 2) {
                        colorFilter.setBackgroundColor(Color.parseColor("#7700FFFF")); 
                    }
                }

            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(cameraView.getSurfaceTexture());
            mCamera.startPreview();
        }
    } catch (Exception e) {
        Toast.makeText(this, "Không thể mở luồng Camera!", Toast.LENGTH_SHORT).show();
    }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCameraPreview();
            } else {
                Toast.makeText(this, "Bạn đã từ chối cấp quyền Camera!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        checkAndOpenCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		checkAndOpenCamera();
	}

    private void startNetLog() {
        oldDown = TrafficStats.getTotalRxBytes();
        oldUp = TrafficStats.getTotalTxBytes();
        
        new Thread(() -> {
            try {
                while(true) {
                    Thread.sleep(1000);
                    long newDown = TrafficStats.getTotalRxBytes();
                    long newUp = TrafficStats.getTotalTxBytes();
                    
                    long speedDown = (newDown - oldDown) / 1024;
                    long speedUp = (newUp - oldUp) / 1024;
                    
                    oldDown = newDown;
                    oldUp = newUp;
                    
                    runOnUiThread(() -> tvSpeed.setText("↓ " + speedDown + " KB/s | ↑ " + speedUp + " KB/s"));
                }
            } catch (Exception e) {}
        }).start();
    }
	private void fixCameraRatio(int viewWidth, int viewHeight) {
    if (mCamera == null) return;
    Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
    if (previewSize == null) return;
    double cameraRatio = (double) previewSize.height / previewSize.width;
    double viewRatio = (double) viewWidth / viewHeight;
    android.graphics.Matrix matrix = new android.graphics.Matrix();
    if (viewRatio > cameraRatio) {
        matrix.setScale(1.0f, (float) (viewRatio / cameraRatio), viewWidth / 2f, viewHeight / 2f);
    } else {
        matrix.setScale((float) (cameraRatio / viewRatio), 1.0f, viewWidth / 2f, viewHeight / 2f);
    }   
    cameraView.setTransform(matrix);
    }
}