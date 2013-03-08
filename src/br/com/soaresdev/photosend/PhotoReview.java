package br.com.soaresdev.photosend;

import java.io.File;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class PhotoReview extends Activity {

	private Intent intent;
	private ProgressDialog pd;
	private String host, user, pass;

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
			return true;
		}
		return false;
	}

	public void toastInsideThread(final String msg) {
		PhotoReview.this.runOnUiThread(new Runnable() {
			public void run() {
				Toast.makeText(PhotoReview.this, msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	public void debug(String str) {
		Log.d("SURFACE", "PhotoReview - " + str);
	}

	public void cancel(View v) {
		debug("Cancel...");
		setResult(CameraSurface.ST_CANCELADO);
		finish();
	}

	private void uploadPhoto() {
		final String photoPath = intent.getStringExtra("PHOTO_PATH");

		pd = ProgressDialog.show(this, "Enviando foto", "Aguarde...", true,
				true);

		new Thread() {
			public void run() {
				try {
					Date d = new Date();
					Long ts = d.getTime();
					final String destination = "/home/slideshow/input/remote_image_"
							+ ts + ".jpg";

					// final String destination = "photo_party_image_" + ts +
					// ".jpg";

					Log.w("SURFACE", "Está na thread");
					Upload upload = new Upload();
					upload.uploadNow(host, user, pass, destination, photoPath);
					Log.w("SURFACE", "Foto enviada com sucesso!");
					toastInsideThread("Foto enviado com sucesso!");
					setResult(CameraSurface.ST_ENVIADO);
				} catch (Error msg) {
					Log.w("SURFACE",
							"Falha ao enviar foto: " + msg.getMessage());
					toastInsideThread("Falha ao enviar foto. Verifique sua conexão/configuração");
					setResult(CameraSurface.ST_FALHA_ENVIO);
				}

				pd.dismiss();
				finish();
			}
		}.start();
	}

	public void save(View v) {
		debug("Saving...");

		// TODO rotina para salvar a foto na galeria

		new AlertDialog.Builder(this)
				.setMessage("Deseja enviar esta foto agora?")
				.setCancelable(false)
				.setPositiveButton("Sim",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Log.w("SURFACE", "Deseja enviar essa foto");
								if (isOnline() == false) {
									Log.w("SURFACE", "Está offline");
									toastInsideThread("Ative a conexão e tente novamente");
								} else {
									uploadPhoto();
								}
							}
						}).setNegativeButton("Não", null).show();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.photo_review);

		intent = getIntent();
		String photoPath = intent.getStringExtra("PHOTO_PATH");
		host = intent.getStringExtra("HOST");
		user = intent.getStringExtra("USER");
		pass = intent.getStringExtra("PASS");

		debug("Chegou do intent o path: " + photoPath);

		File photo = new File(photoPath);

		ImageView iw = (ImageView) findViewById(R.id.imageView1);
		iw.setImageURI(Uri.fromFile(photo));
	}
}
