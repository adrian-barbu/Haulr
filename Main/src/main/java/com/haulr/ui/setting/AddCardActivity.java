package com.haulr.ui.setting;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.control.creditcard.CreditCardEditText;
import com.haulr.control.creditcard.TwoDigitsCardTextWatcher;
import com.haulr.parse.ParseEngine;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.CreditCard;
import com.haulr.parse.model.User;
import com.haulr.ui.BaseActivity;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @description     Add/Update Card Activity
 *
 * @author          Adrian
 */
public class AddCardActivity extends BaseActivity {
    // Intent Params
    public final static String PARAM_CARD_EDIT = "CardEdit";   // This will be passed when user try to edit card info
    public final static String PARAM_CARD_ID = "CardID";   // This will be passed when user try to edit card info

    // UI Member
    private CreditCardEditText etCreditCardNumber;
    private EditText etCreditCardExpiry, etCreditCardCVC;

    // Variables
    WaitDialog mWaitDialog;

    boolean isCardEdit;
    CreditCard mCard;
    String mCardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_payment_add_card);

        mWaitDialog = new WaitDialog(this);

        isCardEdit = getIntent().getBooleanExtra(PARAM_CARD_EDIT, false);
        mCardId = getIntent().getStringExtra(PARAM_CARD_ID);

        if (isCardEdit)
            prepareData();
        else
            initUI();
    }

    /**
     * Get Data From Parse
     */
    private void prepareData() {
        mWaitDialog.show();

        ParseQuery<CreditCard> query = ParseQuery.getQuery(CreditCard.CREDITCARD_TABLE_NAME);
        query.getInBackground(mCardId, new GetCallback<CreditCard>() {
            @Override
            public void done(CreditCard creditCard, ParseException e) {
                mWaitDialog.dismiss();

                if (creditCard != null) {
                    mCard = creditCard;
                    initUI();
                }
            }
        });

        return;
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
        etCreditCardNumber = (CreditCardEditText) findViewById(R.id.etCreditCardNumber);

        etCreditCardExpiry = (EditText) findViewById(R.id.etCreditCardExpiry);
        etCreditCardExpiry.addTextChangedListener(new TwoDigitsCardTextWatcher(etCreditCardExpiry));

        etCreditCardCVC = (EditText) findViewById(R.id.etCreditCardCVC);

        if (isCardEdit && mCard != null) {
            etCreditCardNumber.setText(mCard.getCardNumber());
            etCreditCardExpiry.setText(mCard.getExpireDate());
            etCreditCardCVC.setText(mCard.getCVC());
        }
    }

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.tvSave:
                onCardSave();
                break;

            case R.id.tvBack:
                finish();
                break;
        }
    }

    /**
     * Save new card
     */
    private void onCardSave() {
        String cardNumber = etCreditCardNumber.getText().toString();
        String expireDate = etCreditCardExpiry.getText().toString();
        String cvc = etCreditCardCVC.getText().toString();

        // Check values
        if (cardNumber == null || cardNumber.isEmpty()) {
            showToast(R.string.error_input_credit_card_number);
            return;
        }

        if (expireDate == null || expireDate.isEmpty()) {
            showToast(R.string.error_input_credit_card_expire_date);
            return;
        }

        // Validate Card Info

        // Now try to put card info
        String expireYear = "", expireMonth = "";

        String[] expires = expireDate.split("/");
        if (expires.length >= 2) {
            expireMonth = expires[0];
            expireYear = expires[1];
        }

        boolean validateExpireDate = false;
        try {
            int eY, eM;
            eY = Integer.parseInt(expireYear);
            eM = Integer.parseInt(expireMonth);

            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR) - 2000;
            int month = calendar.get(Calendar.MONTH) + 1;

            validateExpireDate = (eY > 0 && eM > 0);
            validateExpireDate &= (eY >= year && eM <= 12);
            if (eY == year)
                validateExpireDate &= (eM >= month);

        } catch (Exception e) {
        }

        if (!validateExpireDate)
        {
            showToast(R.string.error_validation_credit_card_expire_date);
            return;
        }

        mWaitDialog.show();

        if (isCardEdit) {
            // Update Card
            mCard.setCardNumber(cardNumber);
            mCard.setExpireYear(expireYear);
            mCard.setExpireMonth(expireMonth);
            mCard.setCVC(cvc);
            mCard.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    mWaitDialog.dismiss();

                    if (e == null) {
                        showToast(R.string.success_update_credit_card);
                        finish();
                    } else {
                        showToast(R.string.failed_update_credit_card);
                    }
                }
            });
        }
        else {
            // Create New
            ParseEngine.createNewCard(this, cardNumber, expireYear, expireMonth, cvc, new ParseEngine.OnParseOperationListener() {
                @Override
                public void onSuccess(Object data) {
                    mWaitDialog.dismiss();
                    showToast(R.string.success_create_credit_card);
                    finish();
                }

                @Override
                public void onFailed(String error) {
                    mWaitDialog.dismiss();
                    showToast(R.string.failed_create_credit_card);
                }
            });
        }
    }
}