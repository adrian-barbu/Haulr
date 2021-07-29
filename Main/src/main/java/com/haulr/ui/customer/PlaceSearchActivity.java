package com.haulr.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.haulr.R;
import com.haulr.googleapi.MyLocation;
import com.haulr.googleapi.PlaceAutocomplete;
import com.haulr.parse.model.PlaceInfo;
import com.haulr.ui.BaseActivity;
import com.haulr.ui.placesearch.adapters.PlacesAutoCompleteAdapter;
import com.haulr.ui.placesearch.listeners.RecyclerItemClickListener;
import com.haulr.ui.placesearch.utility.Constants;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @description     Place Search Activity
 * @author
 */

public class PlaceSearchActivity extends BaseActivity
                                   implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected GoogleApiClient mGoogleApiClient;

    private static final LatLngBounds BONUDS_AMERICA = new LatLngBounds(
            new LatLng(45.512269, -73.596007), new LatLng(46.860204, -119.620180));

    private EditText mAutocompleteView;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private PlacesAutoCompleteAdapter mAutoCompleteAdapter;

    TextView tvCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
        setContentView(R.layout.activity_search);

        mAutocompleteView = (EditText)findViewById(R.id.autocomplete_places);

        Collection<Integer> filterTypes = new ArrayList<Integer>();
//        filterTypes.add(Place.TYPE_STREET_ADDRESS);
//        filterTypes.add(Place.TYPE_GEOCODE);
//        filterTypes.add(Place.TYPE_UNIVERSITY);
        AutocompleteFilter filter = AutocompleteFilter.create(filterTypes);

        mAutoCompleteAdapter =  new PlacesAutoCompleteAdapter(this, R.layout.search_item,
                mGoogleApiClient, BONUDS_AMERICA, filter);

        mRecyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        mLinearLayoutManager=new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAutoCompleteAdapter);
        mAutocompleteView.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (!s.toString().equals("") && mGoogleApiClient.isConnected()) {
                    mAutoCompleteAdapter.getFilter().filter(s.toString());
                } else if (!mGoogleApiClient.isConnected()) {
                    Toast.makeText(getApplicationContext(), Constants.API_NOT_CONNECTED, Toast.LENGTH_SHORT).show();
                    Log.e(Constants.PlacesTag, Constants.API_NOT_CONNECTED);
                }

            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            public void afterTextChanged(Editable s) {

            }
        });
        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        try {
                            final PlaceAutocomplete item = mAutoCompleteAdapter.getItem(position);
                            final String placeId = String.valueOf(item.placeId);

                            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                                    .getPlaceById(mGoogleApiClient, placeId);
                            placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                                @Override
                                public void onResult(PlaceBuffer places) {
                                    if (places.getCount() == 1) {
                                        //Do the things here on Click.....
                                        setData(places.get(0));
                                    } else {
                                    }
                                }
                            });
                        } catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                })
        );

        tvCancel = (TextView) findViewById(R.id.tvCancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("Google API Callback", "Connection Done");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, Constants.API_NOT_CONNECTED,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()){
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mRecyclerView != null) {
            mRecyclerView = null;
        }

        System.gc();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Set Data
     */
    private void setData(Place place) {
        PlaceInfo pi = new PlaceInfo(place.getName().toString(),
                                     place.getAddress().toString(),
                                     place.getLatLng().latitude,
                                     place.getLatLng().longitude);

        Intent intent = new Intent();
        intent.putExtra(PARAM_LOCATION, pi);
        setResult(RESULT_OK, intent);
        finish();

    }

    /**
     * Get Address From Current Location
     */
    public void onCurrentLocation(View v) {
        MyLocation myLocation = MyLocation.getInstance(this);
        if (myLocation.getLocation() == null) {
            showToast(R.string.error_gps_provide);
            return;
        }

        PlaceInfo pi = new PlaceInfo(myLocation.getName(),
                myLocation.getAddress(),
                myLocation.getLatitude(),
                myLocation.getLongitude());

        Intent intent = new Intent();
        intent.putExtra(PARAM_LOCATION, pi);
        setResult(RESULT_OK, intent);
        finish();
    }
}
