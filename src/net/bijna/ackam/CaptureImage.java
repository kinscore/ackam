package net.bijna.ackam;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.IOException;
import java.io.OutputStream;

public class CaptureImage extends Activity
	implements SurfaceHolder.Callback,
	           Camera.PictureCallback
{
	private static final String TAG = "ackam";

	private Camera camera;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.d(TAG, "onCreate()");

		super.onCreate(savedInstanceState);

		final SurfaceView view = new SurfaceView(this);
		setContentView(view);

		final SurfaceHolder holder = view.getHolder();

		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceCreated()");

		camera = Camera.open();

		try {
			camera.setPreviewDisplay(holder);
		} catch (IOException ex) {
			Log.e(TAG, "Failed to set camera preview: " + ex.getMessage());

			camera.release();
			camera = null;
			finish();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format,
	                           int width, int height)
	{
		Log.d(TAG, "surfaceChanged()");

		camera.startPreview();
		camera.takePicture(null, null, this);
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera)
	{
		Log.d(TAG, "onPictureTaken()");

		final ContentResolver contentResolver = getContentResolver();

		final Uri uri = contentResolver.insert(
			MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
			new ContentValues());

		try {
			final OutputStream store =
				contentResolver.openOutputStream(uri);

			store.write(data);
			store.flush();
			store.close();
		} catch (IOException ex) {
			Log.e(TAG, "Failed to store image: " + ex.getMessage());
		}

		String type = contentResolver.getType(uri);
		if (type == null || type.equals("")) {
			Log.d(TAG, "No type; assuming JPEG");
			type = "image/jpeg";
		}

		final Intent share = new Intent(Intent.ACTION_SEND);
		share.setType(type);
		share.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(share);

		finish();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		Log.d(TAG, "surfaceDestroyed()");

		camera.stopPreview();
		camera.release();
		camera = null;
	}
}
