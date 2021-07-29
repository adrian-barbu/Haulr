package com.haulr.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.parse.model.User;
import com.haulr.ui.login.fragment.BaseFragment;
import com.haulr.ui.login.fragment.PhoneNumberFragment;
import com.haulr.ui.login.fragment.VerifyPhoneNumberFragment;
import com.haulr.parse.ParseSession;
import com.haulr.ui.MainActivity;

/**
 * @description 	Login Info Activity
 * 
 * @author 		Adrian
 *
 */
public class LoginInfoActivity extends AppCompatActivity {

	// Intent Params
	public final static String LOGIN_FROM_FACEBOOK = "isLoginFromFacebook";		// Params
	public final static String WHERE_TO_GO = "WhereToGo";						// Params
	public final static String LOGIN_AS_DRIVER = "LoginAsDriver";

	// Internal Static Variables
	public final static int FRAGMENT_PHONE_NUMBER = 100;
	public final static int FRAGMENT_VERIFY_NUMBER = 101;

	private int mCurrentPageNumber = 99;

	TextView tvTitle;

	BaseFragment currentFragment;
	boolean isLoginFromFacebook;
	boolean isLoginAsDriver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_info);

		isLoginFromFacebook = getIntent().getBooleanExtra(LOGIN_FROM_FACEBOOK, false);
		isLoginAsDriver = getIntent().getBooleanExtra(LOGIN_AS_DRIVER, false);

		initUI();

		// Check the verification status when user logined from facebook
		if (isLoginFromFacebook) {
			mCurrentPageNumber = getIntent().getIntExtra(WHERE_TO_GO, FRAGMENT_PHONE_NUMBER) - 1;
			replaceFragment(true);
		}
		else {
			replaceFragment(true);
		}
	}

	private void initUI() {
		tvTitle = (TextView) findViewById(R.id.tvTitle);
	}

	/**
	 * 	On Back Event Handler
	 *
	 * @param v
	 */
	public void onBack(View v) {
		if (mCurrentPageNumber == FRAGMENT_PHONE_NUMBER)
		{
			startActivity(new Intent(this, LoginActivity.class));
			ParseSession.logOut(this);
			finish();
		}
		else {
			replaceFragment(false);
		}
	}

	/**
	 * 	On Next Event Handler
	 *
	 * @param v
	 */
	public void onNext(View v) {
		if (currentFragment != null) {
			currentFragment.requestGoNext(mOnNextStepRequestListener);
		}
	}

	/**
	 * Step Callback Listener
	 * This callback will process several messages from sub fragments
	 */
	BaseFragment.OnNextStepRequestListener mOnNextStepRequestListener = new BaseFragment.OnNextStepRequestListener() {

		@Override
		public void OnAllow() {
			// Go Next
			replaceFragment(true);
		}

		@Override
		public void OnError() {
		}

		@Override
		public void OnGoMain() {
			// Set User's Manner (as customer or driver)
			User user = ParseSession.getCurrentUser(getApplicationContext());
			if (user == null)
				return;

			user.setAsDriver(isLoginAsDriver);
			user.setOnline(false);
			user.saveInBackground();

			// Go to Main Screen
			Intent intent = new Intent(LoginInfoActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			finish();
		}
	};

	/**
	 * Replace Fragment
	 *
	 * @param direction
	 * 			true: next, false: back
	 */
	private void replaceFragment(boolean direction) {
		if (direction)	// to next
			mCurrentPageNumber++;
		else			// to back
			mCurrentPageNumber--;

		BaseFragment newFragment = null;

		if (mCurrentPageNumber == FRAGMENT_PHONE_NUMBER) {
			newFragment = new PhoneNumberFragment();
			tvTitle.setText(R.string.page_phone_number_title);
		}
		else if (mCurrentPageNumber == FRAGMENT_VERIFY_NUMBER) {
			newFragment = new VerifyPhoneNumberFragment();
			tvTitle.setText(R.string.page_verify_number_title);
		}
		else {
			return;
		}

		// Parameter
		Bundle params = new Bundle();
		params.putBoolean(BaseFragment.LOGIN_FROM_FACEBOOK, isLoginFromFacebook);
		params.putBoolean(BaseFragment.LOGIN_AS_DRIVER, isLoginAsDriver);
		newFragment.setArguments(params);

		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		if (direction)
			transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
		else
			transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);

		transaction.replace(R.id.fragment_container, newFragment);
		transaction.addToBackStack(null);

		// Commit the transaction
		transaction.commit();

		currentFragment = newFragment;
	}
}
