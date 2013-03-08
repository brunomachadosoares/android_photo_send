package br.com.soaresdev.photosend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class CameraSurface extends Activity implements SurfaceHolder.Callback,
		Camera.ShutterCallback, Camera.PictureCallback {

	Camera mCamera;
	SurfaceView mPreview;
	String user, pass, host;

	private int PHOTO_VIEW_CODE = 100;

	public static int ST_CANCELADO = 0x001;
	public static int ST_ENVIADO = 0x002;
	public static int ST_FALHA_ENVIO = 0x003;
	public static String PHOTO_PARTY_DIR = "PhotoParty";

	public void debug(String str) {
		Log.d("SURFACE", str);
	}

	private void startPhotoView(String photoPath) {
		Intent intent = new Intent(this, PhotoReview.class);
		intent.putExtra("PHOTO_PATH", photoPath);
		intent.putExtra("HOST", host);
		intent.putExtra("USER", user);
		intent.putExtra("PASS", pass);
		startActivityForResult(intent, PHOTO_VIEW_CODE);
	}

	AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

		@Override
		public void onAutoFocus(boolean arg0, Camera arg1) {
			// TODO Auto-generated method stub
			// buttonTakePicture.setEnabled(true);
			mCamera.takePicture(CameraSurface.this, null, null,
					CameraSurface.this);
			debug("Focus OK!");
		}
	};

	private void checkDirs() {
		File photoPartyDir = new File(
				Environment.getExternalStorageDirectory(), PHOTO_PARTY_DIR);
		photoPartyDir.mkdir();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		debug("onCreate begin");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.camera_surface);

		loadConfig();
		checkDirs();

		mPreview = (SurfaceView) findViewById(R.id.preview);
		mPreview.getHolder().addCallback(this);
		mPreview.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		debug("Camera open");
		mCamera = Camera.open();
	}

	@Override
	public void onPause() {
		debug("onPause");
		super.onPause();
		mCamera.stopPreview();
	}

	@Override
	public void onDestroy() {
		debug("onDestroy");
		super.onDestroy();
		mCamera.release();
	}

	public void onSnapClick(View v) {
		ImageView iv = (ImageView) v;
		iv.setBackgroundColor(0xFFFFFFFF);
		mCamera.autoFocus(myAutoFocusCallback);
	}

	@Override
	public void onShutter() {
		debug("OnShutter - Click");
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		iv.setBackgroundColor(0x00);
		try {
			Date d = new Date();
			Long ts = d.getTime();
			final String filename = "photo_party_" + ts + ".jpg";

			File photo = new File(Environment.getExternalStorageDirectory(),
					PHOTO_PARTY_DIR + "/" + filename);

			debug("Path: " + photo.getPath());
			FileOutputStream out = new FileOutputStream(photo.getPath());
			out.write(data);
			out.flush();
			out.close();
			startPhotoView(photo.getPath());

		} catch (FileNotFoundException e) {
			debug("FileNotFound: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			debug("IOExcpt: " + e.getMessage());
			e.printStackTrace();
		}

		// debug("Camera startpreview again");
		// camera.startPreview();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Camera.Parameters params = mCamera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();
		Camera.Size selected = sizes.get(0);
		params.setPreviewSize(selected.width, selected.height);

		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE))
				.getDefaultDisplay();

		if (display.getRotation() == Surface.ROTATION_0) {
			params.setPreviewSize(height, width);
			mCamera.setDisplayOrientation(90);
		}

		if (display.getRotation() == Surface.ROTATION_90) {
			params.setPreviewSize(width, height);
		}

		if (display.getRotation() == Surface.ROTATION_180) {
			params.setPreviewSize(height, width);
		}

		if (display.getRotation() == Surface.ROTATION_270) {
			params.setPreviewSize(width, height);
			mCamera.setDisplayOrientation(180);
		}

		// mCamera.setDisplayOrientation(0);
		try {
			mCamera.setParameters(params);
			mCamera.startPreview();
		} catch (RuntimeException e) {
			debug("Runtime fail: " + e.getMessage());
			Toast.makeText(getApplicationContext(), "Falha ao abrir a câmera.",
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			mCamera.setPreviewDisplay(mPreview.getHolder());
		} catch (Exception e) {
			debug("surfaceCreated exception: " + e.getMessage());
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		debug("surfaceDestroyed");
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {

		super.onActivityResult(requestCode, resultCode, intent);

		// debug("Resultado do activity: " + requestCode + " result: " +
		// resultCode);

		if (requestCode == PHOTO_VIEW_CODE) {
			debug("PHOTO_VIEW_CODE result: " + resultCode);
			if (resultCode == CameraSurface.ST_ENVIADO) {
				debug("Foto já foi enviada =)");
			} else if (resultCode == CameraSurface.ST_CANCELADO) {
				debug("Apagar a foto da memória");
			} else if (resultCode == CameraSurface.ST_FALHA_ENVIO) {
				debug("Falhou ao enviar a foto, verificar configurações");
			}
		}
	}

	private void loadConfig() {
		Intent intent = getIntent();

		host = intent.getStringExtra("HOST");
		user = intent.getStringExtra("USER");
		pass = intent.getStringExtra("PASSWD");
	}
}
