package it.stefanodp91.android.stripedemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.stripe.android.Stripe;
import com.stripe.android.TokenCallback;
import com.stripe.android.model.Card;
import com.stripe.android.model.Token;
import com.stripe.android.view.CardInputWidget;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stefanodp91 on 07/10/17.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        OnResponseRetrievedCallback<String> {
    private static final String TAG = MainActivity.class.getName();

    // the croqqer-be stripe payment endpoint
    private static final String BACKEND_URL = "https://api.croqqer.io/ch-dev/_logic/payment/stripe/";

    // local dev environment
//    private static final String BACKEND_URL = "http://192.168.1.28:8080/_logic/payment/stripe/";

    // the stripe publishable key
    // TODO: 18/10/17 replace with a public production key
    private static final String PUBLISHABLE_KEY = "pk_test_Kw0OhxztY1RfT5DKRvWlm5pP"; // ch public test key

    // dummy auth data
    // TODO: 18/10/17 replace with valid user data
//    private static final String USERNAME = "dummyusername@dummyemail.com"; // user email
//    private static final String PASSWORD = "dummypassword"; // user password

    // local dev environment
    private static final String USERNAME = "dev@si.com"; // user email
//    private static final String PASSWORD = "Maya134)slim"; // user password
    private static final String PASSWORD = "x958bEcyLtN4"; // user password

    // dummy payment data
    // TODO: 18/10/17 replace with valid job data
    private static final String JOB_ID = "5a01ebbf7e62f7844f9e59ae";
    private static final String OFFER_ID = "5a216bce7e62f7844f76da68";

    // dummy payment data
    // for amount see this link:
    // https://stackoverflow.com/questions/35326710/stripe-currency-are-all-amounts-in-cents-100ths-or-does-it-depend-on-curren
    // TODO: 18/10/17 replace with real payment data
    private static final int amount = 1500; // cents = 15 eur
    private static final String currency = "eur";

    // Note that if the data in the widget is either incomplete or
    // fails client-side validity checks, the Card object will be null.
    private CardInputWidget mCardInputWidget;
    private Button mSaveBtn;
    private ProgressBar mProgress;
    private TextView mResponseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // card input stripe widget
        mCardInputWidget = (CardInputWidget) findViewById(R.id.card_input_widget);

        mProgress = findViewById(R.id.progress_bar);

        mResponseView = (TextView) findViewById(R.id.response);

        // save button
        mSaveBtn = (Button) findViewById(R.id.save);
        mSaveBtn.setOnClickListener(this);
    }

    // create the card
    private Card createCard() {
        Card card = mCardInputWidget.getCard();
        if (card == null) {

            Log.e(TAG, "Invalid Card Data");
        }

//        // according to stripe documentation it is possible to set optionals params
//        cardToSave.setName("Customer Name");
//        cardToSave.setAddressZip("12345");

        return card;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.save) {
            onSaveClickAction();
        }
    }

    // perfom
    private void onSaveClickAction() {

        mProgress.setVisibility(View.VISIBLE); // show a progress

        // if the response is already used, clean it
        mResponseView.setText(""); // clean the response textview

        // create card
        Card card = createCard();

        // Remember to validate the card object before you use it to save time.
        if (card.validateCard()) {

            // set the publishable key
            Stripe stripe = new Stripe(MainActivity.this, PUBLISHABLE_KEY);

            // create the stripe token
            stripe.createToken(card, onTokenRetrievedCallback);
        }
    }

    // callback called when the stripe token has been retrieved
    private TokenCallback onTokenRetrievedCallback = new TokenCallback() {
        @Override
        public void onError(Exception error) {
            Log.e(TAG, error.getMessage());
        }

        @Override
        public void onSuccess(Token token) {
            // retrieve the token id
            String tokenId = token.getId();
            Log.d(TAG, "stripeToken: " + tokenId);

            performPOSTRequest(tokenId);
        }
    };

    // perform the http post request
    private void performPOSTRequest(String token) {

        String basicAuth = Utils.getBase64BasicAuth(USERNAME, PASSWORD);
        Log.d(TAG, "basicAuth: " + basicAuth);

        // parameters for the POST request
        JSONObject json = new JSONObject();
        try {
            json.put("stripeToken", token);
            json.put("amount", amount);
            json.put("currency", currency);
            json.put("jobid", JOB_ID);
            json.put("offerid", OFFER_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO: 06/11/17
        }


        // start the POST request
        HttpPostTask httpPostTask = new HttpPostTask(json.toString(), this);
        httpPostTask.setAuth(basicAuth); // set the encoded authorization
        httpPostTask.addHeaders(getHeaderMap());
        httpPostTask.execute(BACKEND_URL);
    }

    private Map<String, String> getHeaderMap() {
        Map<String, String> headerMap = new HashMap<>();

        headerMap.put("Content-Type", "application/json");
        headerMap.put("x-api-key", "nTF2CgR5hmPIULE3C5Ty2KnmIlvqlbW1pvrxXmCf");
        headerMap.put("Accept", "image/*,application/hal+json,application/json,*/*");

        return headerMap;
    }

    // callback called when the post request returns a response
    @Override
    public void onResponseRetrievedSuccess(String response) {
        if (mResponseView != null) {
            mResponseView.setTextColor(Color.parseColor("#9E9E9E")); // text color gray
            mResponseView.setText(response); // response
        }

        if (mProgress != null)
            mProgress.setVisibility(View.GONE); // dismiss the progress
    }

    // callback called when the post request returns an error
    @Override
    public void onResponseRetrievedError(Exception e) {
        if (mResponseView != null) {
            mResponseView.setTextColor(Color.parseColor("#F44336")); // text color red
            mResponseView.setText(e.toString()); // error message
        }

        if (mProgress != null)
            mProgress.setVisibility(View.GONE); // dismiss the progress
    }
}