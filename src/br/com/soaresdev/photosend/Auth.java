package br.com.soaresdev.photosend;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class Auth extends Activity {

	Configure cfg = null;
	
	private void debug(String str) {
		Log.w("PHOTOSEND_AUTH", str);
	}

	private void runCamera() {
		if(cfg == null) {
			debug("Camera null");
			return;
		}
		
		debug("Preparando intent da CameraSurface");
		Intent intent = new Intent(this, CameraSurface.class);
		intent.putExtra("HOST", cfg.getHost());
		intent.putExtra("USER", cfg.getUser());
		intent.putExtra("PASSWD", cfg.getPasswd());
		startActivity(intent);
	}
	
	private String readHttpResponse(HttpResponse response)  throws Exception {

		Reader reader = null;
		try {
			reader = new InputStreamReader(response.getEntity().getContent());

			StringBuffer sb = new StringBuffer();
			{
				int read;
				char[] cbuf = new char[1024];
				while ((read = reader.read(cbuf)) != -1)
					sb.append(cbuf, 0, read);
			}

			return sb.toString();

		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void httpRequest(String eventKey, String fbUserId, String fbUserFirstName, String fbUsername) {
		debug("Criando cliente http");
		HttpClient client = new DefaultHttpClient();
		HttpPost post = new HttpPost(
				"http://www.yourdomain.com/readpost.php");

		debug("Criando chaves de valores");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
		pairs.add(new BasicNameValuePair("EVENT_KEY", eventKey));
		pairs.add(new BasicNameValuePair("FB_USER_ID", fbUserId));
		pairs.add(new BasicNameValuePair("FB_USER_FIRSTNAME", fbUserFirstName));
		pairs.add(new BasicNameValuePair("FB_USER_USERNAME", fbUsername));

		try {
			debug("Setando entidades e executando");
			post.setEntity(new UrlEncodedFormEntity(pairs));

			HttpResponse response = client.execute(post);
			
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				debug("Http request OK 1");
				try {
					String responseString = readHttpResponse(response);
					if(responseString.indexOf("OK") > 0) {
						debug("Http request OK 2");
						cfg = new Configure(responseString);
						//debug("CFG: " + cfg.getHost());
						//Autenticação OK, chama câmera
						runCamera();
						
					} else {
						debug("Http request failed: " + responseString);
					}
				}catch (Exception e) {
					debug("IO excpt: " + e.getMessage());
				}
					
			} else {
				debug("Http request Failed");
			}
			
		} catch (UnsupportedEncodingException e) {
			debug("Unsupport: " + e.getMessage());
		} catch (ClientProtocolException e) {
			debug("ClientProtocol: " + e.getMessage());
		} catch (IOException e) {
			debug("IOExcp: " + e.getMessage());
		}

	}

	public void eventAccess(View v) {
		debug("clicado no event access");
		
		EditText et = (EditText)findViewById(R.id.editText1);
		Intent intent = getIntent();
		
		String eventKey = et.getText().toString();
		String fbUserId = intent.getStringExtra("FB_USER_ID");
		String fbUserFirstName = intent.getStringExtra("FB_USER_FIRST_NAME");
		String fbUsername = intent.getStringExtra("FB_USER_USERNAME");

		debug("eventKey = " + eventKey);
		this.httpRequest(eventKey, fbUserId, fbUserFirstName, fbUsername);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_auth);
	}

}
