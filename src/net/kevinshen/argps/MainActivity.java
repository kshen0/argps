package net.kevinshen.argps;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
	private SensorManager sensorManager = null;
	private Sensor accelSensor = null;
	private Sensor orientationSensor = null;
	private Sensor magSensor = null;
	private TextView accelText;
	private TextView orientationText;
	float[] gravity = null;
	float[] magField = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // set up camera preview surface
        preview = (SurfaceView)findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        
        // set up sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        initSensors();
        accelText = (TextView)findViewById(R.id.accelText);
        orientationText = (TextView)findViewById(R.id.orientationText);
}
    
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {
    	// no op
    }
    
    public final void onSensorChanged(SensorEvent event) {
    	if (event.values == null) {
    		return;
    	}
		String valStr = (int)event.values[0] + ", " + (int)event.values[1] + ", " + (int)event.values[2];
		float[] R = new float[16];
		float[] I = new float[16];
		if (magField != null && gravity != null) {
			if (sensorManager.getRotationMatrix(R, I, gravity, magField)) {
				Log.i("MainActivity", "rotation matrix");
			}

		}
    	if (event.sensor.getType() == event.sensor.TYPE_ACCELEROMETER) {
    		gravity = event.values;
    		valStr = "Accelerometer: " + valStr;
    		accelText.setText(valStr);
	    	Log.i("MainActivity", "Accelerometer: " + valStr); 
    	}
    	else if (event.sensor.getType() == event.sensor.TYPE_ORIENTATION) {
    		valStr = "Orientation: " + valStr;
			orientationText.setText(valStr);
	    	Log.i("MainActivity", "Orientation sensor values: " + valStr); 
    	}
    	else if (event.sensor.getType() == event.sensor.TYPE_MAGNETIC_FIELD) {
    		magField = event.values;
    	}
    }
    
    private void initSensors() {
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
    	if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
    		accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	}
    	if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
    		magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    	}
    	if (sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION) != null){
    		orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
    	}
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	camera = Camera.open();
    	startPreview();
    	
    	sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
    	sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override public void onPause() {
    	if (inPreview) {
	    	camera.stopPreview();
	    	inPreview = false;
    	}

    	camera.release();
    	camera = null;
    	
    	sensorManager.unregisterListener(this);
    	
    	super.onPause();
    }
    
    private void startPreview() {
    	if (cameraConfigured && camera != null) {
    		camera.startPreview();
    		inPreview = true;
    	}
    }
    
	private Camera.Size getBestPreviewSize(int width, int height,
	            Camera.Parameters parameters) {
		Camera.Size result=null;
		
		for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
			if (size.width<=width && size.height<=height) {
				if (result==null) {
					result=size;
				}
				else {
					int resultArea=result.width*result.height;
					int newArea=size.width*size.height;
				
				if (newArea>resultArea) {
					result=size;
				}
			}
		}
	}
	
	return(result);
	}
    
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
          // no-op -- wait until surfaceChanged()
        }

        public void surfaceChanged(SurfaceHolder holder, int format,
                                   int width, int height) {
        	if (camera != null && previewHolder.getSurface() != null) {

	        	// bind SurfaceHolder to camera
		        try {
		            camera.setPreviewDisplay(previewHolder);
		        }
		        catch (Throwable t) {
		            Log.e("PreviewDemo-surfaceCallback",
	                   "Exception in setPreviewDisplay()", t);
			            Toast.makeText(MainActivity.this, t.getMessage(),
	                    Toast.LENGTH_LONG).show();
		        }
		        
		        // configure preview size 
		        if (!cameraConfigured) {
			        Camera.Parameters params = camera.getParameters();
			        Camera.Size bestSize = getBestPreviewSize(width, height, params);
			        if (bestSize != null) {
				        params.setPreviewSize(bestSize.width, bestSize.height);
			        	camera.setParameters(params);
			        	cameraConfigured = true;
			        }
		        }
        	}
	        
        	startPreview();
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
          // no-op
        }
    };
}
