package com.example.videorecordingapp;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class VideoCapture extends Activity {

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	public static final int REAR_CAMERA_ID = 0; //camera_Id for rear camera
	
	private final static String TAG = "VideoCapture";
	private Camera camera;
	private MediaRecorder mediaRecorder;
	private CameraSurfaceView cameraSurfaceView;
	private Button recButton;
	private Boolean recording;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		recording = false; //initialise recording
		
		setContentView(R.layout.activity_video_capture);
		
		setUpRecording();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.video_capture, menu);
		return true;
	}
	
	public void setUpRecording()
	{
		//get phone camera
		camera = getCameraInstance();
		
		if (camera == null) //no camera avialable
		{
			Log.i(TAG, "No camera available");
			return;
		}
		
		camera.setDisplayOrientation(CameraUtilities.getCurrentCameraOrientation(this, REAR_CAMERA_ID, camera));
		//create camera surface view
		cameraSurfaceView = new CameraSurfaceView(this, camera);		
		
		//add the cameraSurfaceView to the user interface
		FrameLayout cameraPreview = (FrameLayout)findViewById(R.id.videoView);
		cameraPreview.addView(cameraSurfaceView);
	}
	
	//Get the instance of the camera
	private Camera getCameraInstance(){
		Camera c = null;
		
		try
		{
			c = Camera.open();
		}
		catch (Exception ex)
		{
			//camera is not available
			Log.e(TAG, "Exception when opening camera: " + ex.getMessage());
		}
		
		return c;
	}
	
	
	//listener for the record button
	public void recordButtonOnClickListener(View view){
		if (recording) //stop recording
		{
			mediaRecorder.stop();
			releaseMediaRecorder();
			camera.lock();
			recording = false;
			((Button)findViewById(R.id.recordButton)).setText("Rec.");
			//camera.stopPreview();
		}
		else
		{
			//release the camera before media recorder starts
			releaseCamera();
			
			//try to prepare the media recorder
			if(!pepareMediaRecorder()){
				//failed to prepare media recorder
				Log.i(TAG, "Failed to prepare media recorder");
				finish();
			}
			
			//start the recorder
			mediaRecorder.start();
			recording = true;
			((Button)findViewById(R.id.recordButton)).setText("Stop");
		}
	}
	
	//Prepare the media recorder
	private boolean pepareMediaRecorder() {
		
		camera = getCameraInstance();
		
		int orientation = CameraUtilities.getCurrentCameraOrientation(this, REAR_CAMERA_ID, camera);
		camera.setDisplayOrientation(orientation); //set this before making the new MediaRecorder() call otherwise it wont work
		
		mediaRecorder = new MediaRecorder();
		
		//1. unlock and set camera to MediaRecorder
		//camera.setDisplayOrientation(getRequestedOrientation());
		camera.unlock();		
				
		mediaRecorder.setCamera(camera);
		
		//2. Set sources		
		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		
		//3. Set a CamcorderProfile (API Level 8 or higher)
		mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
		
		//4. Set output files
		mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());
		
		
		//5. Set the preview output
		mediaRecorder.setPreviewDisplay(cameraSurfaceView.getHolder().getSurface());
		
		//6. Prepare configuration MediaRecorder
		try
		{
			mediaRecorder.prepare();
		}catch (IllegalStateException e)
		{
			Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;			
		}catch (IOException e)
		{
			Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
			releaseMediaRecorder();
			return false;
		}
		
		return true;
	}

	//releasing the camera
	private void releaseCamera() {
		
		if (camera != null){
			camera.release();
			camera = null;
		}
		
	}

	//releasing the mediaRecorder
	private void releaseMediaRecorder(){
		if (mediaRecorder != null){
			mediaRecorder.reset(); //clear recorder config
			mediaRecorder.release(); //release this object
			
			mediaRecorder = null;
			camera.lock(); //lock camera for later use?
		}
	}

	@Override
	protected void onPause(){
		super.onPause();
		releaseMediaRecorder();
		releaseCamera();
	}
	
	
	//saving the file in the directory below

	/** Create a file Uri for saving an image or video */
	private static Uri getOutputMediaFileUri(int type){
	      return Uri.fromFile(getOutputMediaFile(type));
	}

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
	    // To be safe, you should check that the SDCard is mounted
	    // using Environment.getExternalStorageState() before doing this.

	    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
	              Environment.DIRECTORY_PICTURES), "MyCameraApp");
	    // This location works best if you want the created images to be shared
	    // between applications and persist after your app has been uninstalled.

	    // Create the storage directory if it does not exist
	    if (! mediaStorageDir.exists()){
	        if (! mediaStorageDir.mkdirs()){
	            Log.d("MyCameraApp", "failed to create directory");
	            return null;
	        }
	    }

	    // Create a media file name
	    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    File mediaFile;
	    if (type == MEDIA_TYPE_IMAGE){
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "IMG_"+ timeStamp + ".jpg");
	    } else if(type == MEDIA_TYPE_VIDEO) {
	        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
	        "VID_"+ timeStamp + ".mp4");
	    } else {
	        return null;
	    }

	    return mediaFile;
	}
}
