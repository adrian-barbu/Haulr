package com.haulr.ui.setting;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.ui.BaseActivity;

/**
 * @description     About Link Activity
 *
 * @author          Adrian
 */
public class AboutLinkActivity extends BaseActivity {

    // Intent Params
    public final static String PARAM_TITLE = "Title";         // Title
    public final static String PARAM_WEB_LINK = "WebLink";    // Link of web address

    // UI Members
    TextView tvTitle;
    WebView wvContent;

    // Variables
    WaitDialog mWaitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_about_link);

        mWaitDialog = new WaitDialog(this);

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvTitle.setText(getIntent().getStringExtra(PARAM_TITLE));

        String link = getIntent().getStringExtra(PARAM_WEB_LINK);

        wvContent = (WebView)findViewById(R.id.wvContent);
        wvContent.getSettings().setJavaScriptEnabled(true);
        wvContent.getSettings().setSupportZoom(true);
        wvContent.getSettings().setBuiltInZoomControls(true);
        wvContent.getSettings().setLoadWithOverviewMode(true);
        wvContent.getSettings().setUseWideViewPort(true);
        wvContent.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWaitDialog.show();
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, final String url) {
                mWaitDialog.dismiss();
            }
        });
        wvContent.setBackgroundColor(Color.TRANSPARENT);
        wvContent.loadUrl(link);
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

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.ivBack:
                finish();
                break;
        }
    }
}