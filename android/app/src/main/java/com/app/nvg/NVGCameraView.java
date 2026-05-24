package com.app.nvg;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLES11Ext;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.Buffer;

public class NVGCameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
	private int uExposureHandle;
	private NVGLightSensor nvgLightSensor; 
    private SurfaceTexture st;
    private int TextureId;
    private int program;
    private int uModeLocation;
    private Camera mCamera;
    private int currentMode = 0;
    private boolean updateSurface = false;
    private FloatBuffer vertexBuffer;
    private FloatBuffer textureBuffer;
	private float[] mSTMatrix = new float[16];
    private int uSTMatrixLocation;
    private final String vertexShaderCode = Shader.VERTEX_SHADER;
    private final String fragmentShaderCode = Shader.FRAGMENT_SHADER;


    private final float[] vertices = {
        -1.0f, -1.0f,
         1.0f, -1.0f,
        -1.0f,  1.0f,
         1.0f,  1.0f
    };
    
    private final float[] textureVertices = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f
    };
	
    public NVGCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);
		nvgLightSensor = new NVGLightSensor(context);
		nvgLightSensor.start();
		
        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertices);
        ((Buffer) vertexBuffer).position(0);

        textureBuffer = ByteBuffer.allocateDirect(textureVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureVertices);
        ((Buffer) textureBuffer).position(0);
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
		if (mCamera != null && st != null) {
			post(() -> {
				try {
					mCamera.setPreviewTexture(st);
					mCamera.startPreview();
					requestRender();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
    }

    public void setMode(int mode) {
        this.currentMode = mode;
        queueEvent(() -> GLES20.glUniform1i(uModeLocation, currentMode));
        requestRender();
    }
	
	public void onPauseView() {
		if (nvgLightSensor != null) {
			nvgLightSensor.stop();
		}
	}
	
	public void onResumeView() {
		if (nvgLightSensor != null) {
			nvgLightSensor.start();
		}
	}

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);
        GLES20.glCompileShader(vertexShader);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
		
        uModeLocation = GLES20.glGetUniformLocation(program, "uMode");
        uSTMatrixLocation = GLES20.glGetUniformLocation(program, "uSTMatrix");
		uExposureHandle = GLES20.glGetUniformLocation(program, "uExposure");
		
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        TextureId = textures[0];
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, TextureId);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        st = new SurfaceTexture(TextureId);
        st.setOnFrameAvailableListener(this);

        post(() -> {
            if (mCamera != null) {
                try {
                    mCamera.setPreviewTexture(st);
                    mCamera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        synchronized (this) {
            if (updateSurface && st != null) {
                try {
                    st.updateTexImage();
					st.getTransformMatrix(mSTMatrix);
                } catch (Exception e) {
                    return;
                }
                updateSurface = false;
            }
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glUseProgram(program);
		float exposure = nvgLightSensor.getCurrentExposure();
		GLES20.glUniform1f(uExposureHandle, exposure);
		GLES20.glUniformMatrix4fv(uSTMatrixLocation, 1, false, mSTMatrix, 0);

        int ph = GLES20.glGetAttribLocation(program, "position");
        GLES20.glEnableVertexAttribArray(ph);
        ((Buffer) vertexBuffer).position(0);
        GLES20.glVertexAttribPointer(ph, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer);

        int tch = GLES20.glGetAttribLocation(program, "inputTextureCoordinate");
        GLES20.glEnableVertexAttribArray(tch);
        ((Buffer) textureBuffer).position(0);
        GLES20.glVertexAttribPointer(tch, 2, GLES20.GL_FLOAT, false, 8, textureBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, TextureId);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        synchronized (this) {
			updateSurface = true;
		}
		requestRender();
    }
}