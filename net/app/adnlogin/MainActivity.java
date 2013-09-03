package net.app.adnlogin;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_AUTHORIZE = 1;

    //Comma separated, e.g. "basic,write_post,files,stream"
    private static final String SCOPE = "XXXXXXXX";

    //From your app page at https://account.app.net/developer/apps/
    private static final String CLIENT_ID = "XXXXXXXXX";

    private ProgressDialog mProgressDialog;

    private final BroadcastReceiver installReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String dataString = intent.getDataString();
            if(Intent.ACTION_PACKAGE_ADDED.equals(action) &&
               dataString.equals(String.format("package:%s", ADNPassportUtility.APP_PACKAGE))) {
                authenticateWithPassport();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerPassportInstallationReceiver();

        //Your app should probably do this in an OnClickListener for
        //a button, e.g. "Sign in with Passport"
        authenticateWithPassport();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(installReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_AUTHORIZE) {
            if(resultCode == 1) {
                String username = data.getStringExtra("username");
                String accessToken = data.getStringExtra("accessToken");
                String userId = data.getStringExtra("userId");

                //hooray, we're logged in!
                Log.d("MainActivity", "Authorized successfully!");
            } else {
                //error OR the user cancelled.
                Log.d("MainActivity", "Failed to authorize");
            }

            hideProgress();
        }
    }

    private void registerPassportInstallationReceiver() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addDataScheme("package");
        registerReceiver(installReceiver, filter);
    }

    protected void showProgress(String message) {
        if(mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.show();
        } else {
            mProgressDialog.setMessage(message);
        }
    }

    protected void hideProgress() {
        mProgressDialog.dismiss();
        mProgressDialog = null;
    }

    private void authenticateWithPassport() {
        //This is only safe when you have the Passport
        //app installed on the device already.
        if(ADNPassportUtility.isPassportAuthorizationAvailable(this)) {
            //convenience method for getting a net.app.adnpassport.authorize Intent
            Intent i = ADNPassportUtility.getAuthorizationIntent(CLIENT_ID, SCOPE);
            showProgress("Authorizing with App.net Passport");
            startActivityForResult(i, REQUEST_CODE_AUTHORIZE);
        } else {
            //Launch Google Play to install the Passport app.
            showProgress("Waiting for Passport to Install...");
            ADNPassportUtility.launchPassportInstallation(this);
        }
    }
}
