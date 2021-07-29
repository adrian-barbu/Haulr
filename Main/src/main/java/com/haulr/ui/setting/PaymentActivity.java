package com.haulr.ui.setting;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.haulr.R;
import com.haulr.control.dialog.WaitDialog;
import com.haulr.control.creditcard.CreditCardEditText;
import com.haulr.parse.ParseSession;
import com.haulr.parse.model.CreditCard;
import com.haulr.parse.model.User;
import com.haulr.ui.BaseActivity;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @description     Payment Activity (Will be include card informations)
 * @author           Adrian
 */
public class PaymentActivity extends BaseActivity {

    // Intent Params
    public final static String PARAM_SELECT_CARD = "SelectCard";           // This will be called from PayHaulActivity
    public final static String PARAM_SELECTED_CARD = "SelectedCard";    // This will be transfered to PayHaulActivity

    // UI Members
    ListView lvCards;
    CardListAdapter mListAdapter;
    ArrayList<CreditCard> mCardList;

    // Variables
    WaitDialog mWaitDialog;
    boolean isCardSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_payment);

        isCardSelection = getIntent().getBooleanExtra(PARAM_SELECT_CARD, false);

        mWaitDialog = new WaitDialog(this);

        initUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        prepareData();
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
        lvCards = (ListView) findViewById(R.id.lvCards);
        mListAdapter = new CardListAdapter(this);
        lvCards.setAdapter(mListAdapter);
    }

    /**
     * Get Data From Parse
     */
    private void prepareData() {
        if (mCardList != null)
        {
            mCardList.clear();
            mCardList = null;
            System.gc();
        }

        mCardList = new ArrayList<CreditCard>();

        // Get Credit Cards From Server
        User user = ParseSession.getCurrentUser(this);
        if (user == null)
            return;

        ParseQuery<CreditCard> query = ParseQuery.getQuery(CreditCard.CREDITCARD_TABLE_NAME);
        query.whereEqualTo(CreditCard.FIELD_USER_ID, user.getObjectId());
        query.addDescendingOrder("createdAt");

        mWaitDialog.show();
        query.findInBackground(new FindCallback<CreditCard>() {
            public void done(List<CreditCard> cards, ParseException e) {
                mWaitDialog.dismiss();

                if (e == null) {
                    mListAdapter.setData(cards);
                    mListAdapter.notifyDataSetChanged();
                } else {

                }
            }
        });

        return;
    }

    /**
     *      On Item Click Listener
     *
     * @param view
     */
    public void onItemClick(View view) {
        switch(view.getId()) {
            case R.id.layoutAddCard:
                startActivity(new Intent(this, AddCardActivity.class));
                break;

            case R.id.ivBack:
                finish();
                break;
        }
    }

    public static class ViewHolder {
        public View rootView;
        public CreditCardEditText etCreditCardNumber;
        public ImageView ivDelete;
    }

    /**
     * @description     Card List Adapter
     * @author          Adrian
     */
    public class CardListAdapter extends ArrayAdapter<CreditCard> {

        private Context mContext;
        private LayoutInflater mInflater;

        private ArrayList<CreditCard> mCardList;	// Card List

        public CardListAdapter(Context context) {
            super(context, R.layout.list_credit_card_item);

            mContext = context;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        /*
         * Set List Item Datas
         *
         */
        public void setData(List<CreditCard> data) {
            clear();
            if (data != null) {
                addAll(data);
            }
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_credit_card_item, parent, false);

                holder = new ViewHolder();
                holder.rootView = convertView;
                holder.etCreditCardNumber = (CreditCardEditText) convertView.findViewById(R.id.etCreditCardNumber);
                holder.ivDelete = (ImageView) convertView.findViewById(R.id.ivDelete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

			// Set ui contents based on CreditCard
            final CreditCard item = getItem(position);
            holder.etCreditCardNumber.setText(item.getCardNumber());

            holder.etCreditCardNumber.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isCardSelection) {
                        // return this card
                        Intent intent = new Intent();
                        intent.putExtra(PARAM_SELECTED_CARD, item);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    else {
                        // Edit Card
                        Intent intent = new Intent(mContext, AddCardActivity.class);
                        intent.putExtra(AddCardActivity.PARAM_CARD_EDIT, true);
                        intent.putExtra(AddCardActivity.PARAM_CARD_ID, item.getObjectId());
                        startActivity(intent);
                    }
                }
            });

            /**
             * Add Delete Action
             */
            holder.ivDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.app_name)
                            .setMessage(R.string.setting_payment_card_delete_title)
                            .setCancelable(false)
                            .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    deleteCard(position);
                                }
                            })
                            .setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.cancel();
                                }
                            });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });
            return convertView;
        }

        /**
         * Delete Action
         */
        private void deleteCard(int position) {
            mWaitDialog.show();

            final CreditCard item = getItem(position);
            item.deleteInBackground(new DeleteCallback() {
                @Override
                public void done(ParseException e) {
                    mWaitDialog.dismiss();
                    remove(item);
                    notifyDataSetChanged();
                }
            });
        }
    }
}