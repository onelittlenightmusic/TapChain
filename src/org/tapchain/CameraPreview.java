package org.tapchain;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private SurfaceHolder holder;
	protected Camera camera;

	CameraPreview(Context context) {
		super(context);
		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		camera = Camera.open();
		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		camera.stopPreview();
		Camera.Parameters parameters = camera.getParameters();
		List<Camera.Size> previewSizes = parameters
				.getSupportedPreviewSizes();

		// You need to choose the most appropriate previewSize for your app
		Camera.Size previewSize = previewSizes.get(0);// .... select one of
														// previewSizes here

		parameters.setPreviewSize(previewSize.width, previewSize.height);
		camera.setParameters(parameters);
		// params.setPreviewFormat(format);
		// params.setPreviewSize(width, height);
		// camera.setParameters(params);
		camera.startPreview();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		camera.stopPreview();
		camera.release();
	}
}