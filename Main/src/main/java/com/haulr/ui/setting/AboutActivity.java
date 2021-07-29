package com.haulr.ui.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.haulr.R;
import com.haulr.ui.BaseActivity;

/**
 * @description     About Activity
 * @author           Adrian
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return true;
    }

    private void initUI()
    {
    }

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.layoutFAQ:
                goWithLink(getString(R.string.setting_about_faq), "http://www.google.com");     // Replace this
                break;

            case R.id.layoutTerms:
                goWithLink(getString(R.string.setting_about_terms), "http://www.google.ca");     // Replace this
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    private void goWithLink(String title, String link) {
        Intent intent = new Intent(this, AboutLinkActivity.class);
        intent.putExtra(AboutLinkActivity.PARAM_TITLE, title);
        intent.putExtra(AboutLinkActivity.PARAM_WEB_LINK, link);
        startActivity(intent);
    }
}