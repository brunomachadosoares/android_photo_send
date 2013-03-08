package br.com.soaresdev.photosend;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

public class MainActivity extends FragmentActivity {

	private final String PENDING_ACTION_BUNDLE_KEY = "br.com.soaresdev.photosend:PendingAction";

	private LoginButton loginButton;
	private PendingAction pendingAction = PendingAction.NONE;
	private GraphUser user;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	public void debug(String str) {
		Log.w("PHOTOSEND_MAIN", str);
	}
	
	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {

		@Override
		public void call(Session session, SessionState state, Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		debug("onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// soh pra debug:
		//startActivity(new Intent(MainActivity.this, Auth.class));
		
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_main);

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {

			@Override
			public void onUserInfoFetched(GraphUser user) {
				
				MainActivity.this.user = user;
				updateUI();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);
		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
		debug("onPause");
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		debug("onDestroy");
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(Session session, SessionState state, Exception exception) {

		debug("State: " + state.toString());

		if (pendingAction != PendingAction.NONE
			&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(MainActivity.this).setTitle("Cancelado").setMessage("Permissao nao concedida")
				.setPositiveButton("OK", null).show();
			pendingAction = PendingAction.NONE;
		}
		
		if(session.isClosed() && exception != null) {
			debug("Excpt: " + exception.getMessage());
			Toast.makeText(getApplicationContext(), "Falha ao logar no Facebook", Toast.LENGTH_SHORT).show();
		}

		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		if (session.isOpened() && user != null) {
			showAlert("Olá!", "Bem vindo " + user.getFirstName());
			Intent intent = new Intent(this, Auth.class);
			intent.putExtra("FB_USER_ID", user.getId());
			intent.putExtra("FB_USER_FIRST_NAME", user.getFirstName());
			intent.putExtra("FB_USER_USERNAME", user.getUsername());
			intent.putExtra("FB_USER_CITY", user.getLocation().getCity());
			startActivity(intent);
		}
		
	}

	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message).setPositiveButton("OK", null).show();
	}
}