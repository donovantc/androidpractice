package com.example.videorecordingapp;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;

public class CameraUtilities {

	public static int getCurrentCameraOrientation(Activity activity, int cameraId, Camera camera)
	{
		CameraInfo info = new android.hardware.Camera.CameraInfo();
	    Camera.getCameraInfo(cameraId, info);
	    
	    int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	    
	    int degrees = 0;
	    
	    switch (rotation) {
	        case Surface.ROTATION_0: degrees = 0; break;
	        case Surface.ROTATION_90: degrees = 90; break;
	        case Surface.ROTATION_180: degrees = 180; break;
	        case Surface.ROTATION_270: degrees = 270; break;
	    }

	    int result;
	    
	    if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	        result = (info.orientation + degrees) % 360;
	        result = (360 - result) % 360;  // compensate the mirror
	    } else {  // back-facing
	        result = (info.orientation - degrees + 360) % 360;
	    }
	     
	    //Log.i("CameraUtilities: The current orientation: ", String.valueOf(result));
	    
	    return result;
	}
}
