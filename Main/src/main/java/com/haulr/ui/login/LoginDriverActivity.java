package com.haulr.ui.login;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.ParseLoginConfig;
import com.parse.Parse;

/**
 * @description 	Login Activity
 * 
 * @author 		Adrian
 *
 */
public class LoginDriverActivity extends AppCompatActivity implements View.OnClickListener {
	TextView tvNormalLogin;
	TextView tvLogin;

	private Bundle configOptions;
	private ParseLoginConfig config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_driver);

		// Combine options from incoming intent and the activity metadata
		configOptions = getMergedOptions();
		config = ParseLoginConfig.fromBundle(configOptions, getApplicationContext());

		initUI();
	}

	private void initUI() {
		tvNormalLogin = (TextView) findViewById(R.id.tvNormalLogin);
		tvNormalLogin.setOnClickListener(this);

		tvLogin = (TextView) findViewById(R.id.tvLogin);
		tvLogin.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.tvNormalLogin:
				startActivity(new Intent(this, LoginActivity.class));
				finish();
				break;

			case R.id.tvLogin:
				goLogInfoPage(false, LoginInfoActivity.FRAGMENT_PHONE_NUMBER);
				break;
		}
	}

	/**
	 * Go Login Info Page
	 *
	 * @param
	 * 		isLoginFromFacebook : 	the flag indicates user logined from facebook
	 * 	    fragment_where:			 this user go to where? only when use logined from facebook
	 */
	private void goLogInfoPage(boolean isLoginFromFacebook, int fragment_where) {
		Intent intent = new Intent(LoginDriverActivity.this, LoginInfoActivity.class);
		intent.putExtra(LoginInfoActivity.LOGIN_AS_DRIVER, true);
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
		} catch (PackageManager.NameNotFoundException e) {
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
