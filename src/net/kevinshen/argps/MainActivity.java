package net.kevinshen.argps;

import android.os.Bundle;
import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.util.Log;
import android.widget.Toast;

public class MainActivity extends Activity {
	private SurfaceView preview = null;
	private SurfaceHolder previewHolder = null;
	private Camera camera = null;
	private boolean inPreview = false;
	private boolean cameraConfigured = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.preview);
        setContentView(R.layout.activity_main);

        preview = (SurfaceView)findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
    }
    
    @Override
    public void onResume() {
    	super.onResume();

    	camera = Camera.open();
    	startPreview();
    }
    
    @Override public void onPause() {
    	if (inPreview) {
	    	camera.stopPreview();
	    	inPreview = false;
    	}

    	camera.release();
    	camera = null;
    	
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
		        /*
		        // Set best size
		        Camera.Parameters params = camera.getParameters();
		        Camera.Size bestSize = null;
		        int largestArea = -1;
		        for (Camera.Size size : params.getSupportedPreviewSizes()) {
		        	int area = size.height * size.width;
		        	if (area > largestArea) {
		        		largestArea = area;
		        		bestSize = size;
		        	}
		        }
		        */
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
