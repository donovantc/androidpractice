package com.example.videorecordingapp;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder surfaceHolder;
	private Camera camera;
	
	public CameraSurfaceView(Context context, Camera camera) {
		super(context);
		
		this.camera = camera;
		
		//install a surfaceholder.callback get notified when the surface is created and destroyed
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		
		//only relevant to Android versions 3.0 and lower
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

		if(surfaceHolder.getSurface() == null) //surface does not exist
		{
			return;
		}
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//surface has been created now the camera needs to know where to draw the preview
		try
		{
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e)
		{
			
		}
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

}
