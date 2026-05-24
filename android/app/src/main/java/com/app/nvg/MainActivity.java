package com.app.nvg;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.TextView;
import android.hardware.Camera;
import android.widget.PopupMenu;
import android.view.View;

public class MainActivity extends Activity {

    private Button btnCamera;
    private Button btnSettings;
    private final int CAMERA_CODE = 100;
    private TextView tvSpeed;
    private Camera mCamera;
    private NVGCameraView nvgCameraView;
    private NVGLightSensor nvgLightSensor;
    private TextView txtWarning;
    private Button btnFlash;
    private boolean isFlashOn = false;
    private int currentMode = 0;
    private NetLog netlog;

    private Button btnTopSettings;
    private View layoutSettingsScreen;
    private Button btnSettingsBack;
    private TextView tvLanguageTitle;
    private Button btnSelectLanguage;
    private String currentLanguage = "English";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        
        btnCamera = findViewById(R.id.btnCamera);
        tvSpeed = findViewById(R.id.speed);
        btnSettings = findViewById(R.id.btnSettings);
        btnFlash = findViewById(R.id.btnFlash);
        txtWarning = findViewById(R.id.txtWarning);
        nvgCameraView = findViewById(R.id.nvgCameraView);
        
        btnTopSettings = findViewById(R.id.btnTopSettings);
        layoutSettingsScreen = findViewById(R.id.layoutSettingsScreen);
        btnSettingsBack = findViewById(R.id.btnSettingsBack);
        tvLanguageTitle = findViewById(R.id.tvLanguageTitle);
        btnSelectLanguage = findViewById(R.id.btnSelectLanguage);

        btnCamera.setText("Video NVG");
        btnSettings.setText("Mode");
        btnFlash.setText("Turn On Flash");
        txtWarning.setText("Environment too dark, cannot amplify!");
        tvLanguageTitle.setText("Language");
        btnSelectLanguage.setText("English");
        
        nvgLightSensor = new NVGLightSensor(this);
        nvgLightSensor.start();
        
        netlog = new NetLog(this, tvSpeed);
        netlog.start();
        
        btnFlash.setOnClickListener(v -> {
            toggleFlashLight();
        });
        
        btnTopSettings.setOnClickListener(v -> {
            if (layoutSettingsScreen != null) {
                layoutSettingsScreen.setVisibility(View.VISIBLE);
            }
        });
        
        btnSettingsBack.setOnClickListener(v -> {
            if (layoutSettingsScreen != null) {
                layoutSettingsScreen.setVisibility(View.GONE);
            }
        });
        
        btnSelectLanguage.setOnClickListener(v -> {
            if (currentLanguage.equals("English")) {
                currentLanguage = "Vietnamese";
                btnSelectLanguage.setText("Vietnamese");
                updateAppLanguage("Vietnamese");
            } else {
                currentLanguage = "English";
                btnSelectLanguage.setText("English");
                updateAppLanguage("English");
            }
        });
        
        new Thread(() -> {
            while(true) {
                try {
                    Thread.sleep(800);
                    runOnUiThread(() -> {
                        if (nvgLightSensor != null) {
                            float lux = nvgLightSensor.getCurrentLux();
                            if (lux < 0.5f) {
                                txtWarning.setVisibility(View.VISIBLE);
                            } else {
                                txtWarning.setVisibility(View.GONE);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        
        btnCamera.setOnClickListener(v -> {
            if (currentLanguage.equals("English")) {
                Toast.makeText(this, "Running NVG Video mode using GPU!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đang chạy chế độ Video NVG bằng GPU!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSettings.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, v);
            if (currentLanguage.equals("English")) {
                popup.getMenu().add("Mode: Default");
                popup.getMenu().add("Mode: Green NVG");
                popup.getMenu().add("Mode: Cyan");
            } else {
                popup.getMenu().add("Chế độ: Mặc định");
                popup.getMenu().add("Chế độ: Xanh NVG");
                popup.getMenu().add("Chế độ: Cyan");
            }

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle().toString();
                if (title.contains("Mặc định") || title.contains("Default")) currentMode = 0;
                else if (title.contains("Xanh NVG") || title.contains("Green NVG")) currentMode = 1;
                else if (title.contains("Cyan")) currentMode = 2;
                
                if (nvgCameraView != null) {
                    nvgCameraView.setMode(currentMode);
                }
                return true;
            });
            popup.show();
        });

        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
        }
    }
    
    private void updateAppLanguage(String lang) {
        if (lang.equals("Vietnamese")) {
            tvLanguageTitle.setText("Ngôn ngữ");
            btnCamera.setText("Ghi hình");
            btnSettings.setText("Chế độ");
            txtWarning.setText("Môi trường quá tối không khuếch đại được!");
            if (isFlashOn) {
                btnFlash.setText("Tắt Đèn Mồi");
            } else {
                btnFlash.setText("Bật Đèn Mồi");
            }
        } else {
            tvLanguageTitle.setText("Language");
            btnCamera.setText("Video NVG");
            btnSettings.setText("Mode");
            txtWarning.setText("Environment too dark, cannot amplify!");
            if (isFlashOn) {
                btnFlash.setText("Turn Off Flash");
            } else {
                btnFlash.setText("Turn On Flash");
            }
        }
    }
    
    private void toggleFlashLight() {
        try {
            if (mCamera == null) {
                return;
            }
            Camera.Parameters p = mCamera.getParameters();
            if (!isFlashOn) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
                isFlashOn = true;
                if (currentLanguage.equals("English")) {
                    btnFlash.setText("Turn Off Flash");
                } else {
                    btnFlash.setText("Tắt Đèn Mồi");
                }
            } else {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
                isFlashOn = false;
                if (currentLanguage.equals("English")) {
                    btnFlash.setText("Turn On Flash");
                } else {
                    btnFlash.setText("Bật Đèn Mồi");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initCamera() {
        try {
            if (mCamera == null) {
                mCamera = Camera.open();
                mCamera.setDisplayOrientation(90);
                
                Camera.Parameters parameters = mCamera.getParameters();
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                }
                mCamera.setParameters(parameters);
                
                if (nvgCameraView != null) {
                    nvgCameraView.setCamera(mCamera);
                }
            }
        } catch (Exception e) {
            if (currentLanguage.equals("English")) {
                Toast.makeText(this, "Camera initialization error!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi khởi tạo Camera!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (nvgCameraView != null) {
            nvgCameraView.onPauseView();
        }
        if (nvgLightSensor != null) {
            nvgLightSensor.stop();
        }
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nvgCameraView != null) {
            nvgCameraView.onResumeView();
        }
        if (nvgLightSensor != null) {
            nvgLightSensor.start();
        }
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initCamera();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (netlog != null) {
            netlog.stop();
        }
    }
}