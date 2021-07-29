package com.haulr.ui.login;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.parse.ParseEngine;
import com.haulr.parse.ParseLoginConfig;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.User;
import com.haulr.ui.MainActivity;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import org.json.JSONObject;

/**
 * @description 	Login Activity
 * 
 * @author 		Adrian
 *
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

	// UI Members
	TextView tvDriverLogin;
	TextView tvLogin;
	Button btnFacebookLogin;

	// Variables
	private Bundle configOptions;
	private ParseLoginConfig config;

	WaitDialog mWaitDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Combine options from incoming intent and the activity metadata
		configOptions = getMergedOptions();
		config = ParseLoginConfig.fromBundle(configOptions, getApplicationContext());

		// if user keep session, then no need login
		if (ParseSession.getCurrentUser(this) != null) {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
			finish();
			return;
		}

		setContentView(R.layout.activity_login);

		mWaitDialog = new WaitDialog(this);

		initUI();
	}

	private void initUI() {
		tvDriverLogin = (TextView) findViewById(R.id.tvDriverLogin);
		tvDriverLogin.setOnClickListener(this);

		tvLogin = (TextView) findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);

		btnFacebookLogin =  (Button) findViewById(R.id.btnFacebookLogin);
		btnFacebookLogin.setOnClickListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.tvDriverLogin:
				startActivity(new Intent(this, LoginDriverActivity.class));
				finish();
				break;

			case R.id.tvLogin:
				goLogInfoPage(false, LoginInfoActivity.FRAGMENT_PHONE_NUMBER);
				break;

			case R.id.btnFacebookLogin:
				facebookLogin();
				break;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Required for making Facebook login work
		ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
	}

	private void facebookLogin() {
		if (!ParseSession.checkNetworkConnectivity(this))
			return;

		if (config.isFacebookLoginNeedPublishPermissions()) {
			ParseFacebookUtils.logInWithPublishPermissionsInBackground(this,
					config.getFacebookLoginPermissions(), facebookLoginCallbackV4);
		} else {
			ParseFacebookUtils.logInWithReadPermissionsInBackground(this,
					config.getFacebookLoginPermissions(), facebookLoginCallbackV4);
		}
	}

	/*
	 * Face Login Callback
	 */
	private LogInCallback facebookLoginCallbackV4 = new LogInCallback() {
		@Override
		public void done(final ParseUser pUser, ParseException e) {
			if (isActivityDestroyed()) {
				return;
			}

			if (e != null) {
				//Toast.makeText(LoginActivity.this, R.string.error_login, Toast.LENGTH_LONG).show();
				return;
			}

			final User user = (User) pUser;
			if (user == null) {
				//Toast.makeText(LoginActivity.this, R.string.error_login, Toast.LENGTH_LONG).show();
				return;
			} else if (user.isNew()) {
				mWaitDialog.show();

				GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
						new GraphRequest.GraphJSONObjectCallback() {
							@Override
							public void onCompleted(JSONObject fbUser,
													GraphResponse response) {

								String firstName = "", lastName = "";
								try {
									String name = fbUser.optString("name");
									String[] names = name.split(" ");
									firstName = names[0];
									lastName = names[1];
								} catch (Exception e)
								{}

								String email = "";
								try {
								//	email = (String) response.get.getProperty("email");
								} catch (Exception e)
								{}

								ParseEngine.setUserInfo(LoginActivity.this, firstName, lastName, email, "", new ParseEngine.OnParseOperationListener() {
									@Override
									public void onSuccess(Object data) {
										mWaitDialog.dismiss();
										goLogInfoPage(true, LoginInfoActivity.FRAGMENT_PHONE_NUMBER);
									}

									@Override
									public void onFailed(String error) {
										mWaitDialog.dismiss();
										goLogInfoPage(true, LoginInfoActivity.FRAGMENT_PHONE_NUMBER);
									}
								});

							}
						}
				).executeAsync();
			} else {
				// Check phoneNumber

				String phoneNumber = user.getPhoneNumber();
				if (phoneNumber != null && !phoneNumber.isEmpty()) {
					boolean verified = user.isVerified();
					if (verified) {
						// Go to main page
						Intent intent = new Intent(LoginActivity.this, MainActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);

						finish();
					} else {
						goLogInfoPage(true, LoginInfoActivity.FRAGMENT_VERIFY_NUMBER);
					}
				} else {
					goLogInfoPage(true, LoginInfoActivity.FRAGMENT_PHONE_NUMBER);
				}
			}
		}
	};

	/**
	 * Go Login Info Page
	 *
	 * @param
	 * 		isLoginFromFacebook : the flag indicates user logined from facebook
	 * 	    fragment_where:			 this user go to where? only when use logined from facebook
	 */
	private void goLogInfoPage(boolean isLoginFromFacebook, int fragment_where) {
		Intent intent = new Intent(LoginActivity.this, LoginInfoActivity.class);
		intent.putExtra(LoginInfoActivity.LOGIN_AS_DRIVER, false);
		intent.putExtra(LoginInfoActivity.LOGIN_FROM_FACEBOOK, isLoginFromFacebook);
		intent.putExtra(LoginInfoActivity.WHERE_TO_GO, fragment_where);
		startActivity(intent);

		finish();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	protected boolean isActivityDestroyed() {
		FragmentActivity activity = this;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			return activity == null || activity.isDestroyed();
		} else {
			return activity == null;
		}
	}

	private Bundle getMergedOptions() {
		// Read activity metadata from AndroidManifest.xml
		ActivityInfo activityInfo = null;
		try {
			activityInfo = getPackageManager().getActivityInfo(
					this.getComponentName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			if (Parse.getLogLevel() <= Parse.LOG_LEVEL_ERROR &&
					Log.isLoggable("Warning", Log.WARN)) {
				Log.w("Warning", e.getMessage());
			}
		}

		// The options specified in the Intent (from ParseLoginBuilder) will
		// override any duplicate options specified in the activity metadata
		Bundle mergedOptions = new Bundle();
		if (activityInfo != null && activityInfo.metaData != null) {
			mergedOptions.putAll(activityInfo.metaData);
		}
		if (getIntent().getExtras() != null) {
			mergedOptions.putAll(getIntent().getExtras());
		}

		return mergedOptions;
	}
}
