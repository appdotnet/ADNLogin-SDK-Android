ADNLogin-SDK-Android
====================

This is the documentation for the App.net Login SDK for Android. It allows users to forgo entering passwords into each app and instead authorize from the App.net Passport Android application. Passport allows you to browse the App.net directory and perform account management functions.

Another important function of the SDK is that it allows you to seamlessly offer an option to install Passport, so that users without accounts can sign up for App.net.

We call this an SDK, but in reality, this is just a demonstration of how to use an Intent to authorize from the Passport app. If you're familiar with how Android works, this should be pretty straightforward. The main functionality is contained in static methods in the class ADNPassportUtility.

## Usage

The MainActivity class included in this repository contains code to achieve the tasks outlined above. The following method can be hooked up to a button in your app to kick off the authorization flow. If the Passport app is not present on the device and the `net.app.adnpassport.authorize` Intent cannot be handled, then this method brings the user to market to install the Passport. 

```java
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
```

After the Passport app is installed, the authenticateWithPassport() method is called again from the BroadcastReceiver.

```java
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
```

Finally, when authorization is complete, your Activity will receive the user's username, user ID, and access token in the Intent returned in `onActivityResult`.

```java
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
```

## Find Friends, Invite Friends, and Recommended Users

ADNPassportUtility also contains methods to help you to hook into other features in the App.net Passport app. The following three methods can be used to direct your users to follow or invite friends:

```java
public static boolean launchFindFriends(Context context, String clientId);
public static boolean launchRecommendedUsers(Context context, String clientId);
public static boolean launchInviteFriends(Context context, String clientId);
```

Each of these methods can be used to start their respective Activities from the Passport app. If you're conditionally including some UI (e.g. an 'Invite Friends' button) to hook into one of these features, then you can use these convenience methods to check whether Passport is installed:

```java
public static boolean isFindFriendsAvailable(Context context);
public static boolean isRecommendedUsersAvailable(Context context);
public static boolean isInviteFriendsAvailable(Context context);
```
