package com.limor.app.scenes.notifications;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.installations.FirebaseInstallations;
import com.limor.app.scenes.utils.Commons;

public class UtilsRegistrationIntentService extends IntentService {

    private String PREFS_NAME = "limorv2pref";
    private String PUSH_NEW_KEY = "pushnewtoken";

    public UtilsRegistrationIntentService() {
        super(Commons.TAG_PUSH_REGISTER_INTENT);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Intent registrationComplete = new Intent(Commons.TAG_PUSH_REG_COMPLETED);
        FirebaseInstallations.getInstance().getToken(true).addOnSuccessListener(instanceIdResult -> {
            String pushToken = instanceIdResult.getToken();
            //SharedPreferences.getInstance().savePushToken(getApplicationContext(), pushToken);
            sendPushTokenToServer(pushToken);
        });
//        try {
//            LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
    }

    private void sendPushTokenToServer(String pushToken) {
//        AddDeviceRequest body = new AddDeviceRequest(new UserDevice(pushToken, Commons.PLATFORM_NAME, pushToken));
//        Call<SHOBaseResponse<SHOBaseData>> call = SHOApiBuilder.getApiBuilder(true).addDevice(body);
//        call.enqueue(new SHOCallback<SHOBaseData>(getApplicationContext(), false, false) {
//            @Override
//            public void onSuccess(Response<SHOBaseResponse<SHOBaseData>> response) {
//                // success silently
//            }
//            @Override
//            public void onError(SHOBaseResponse errorResponse) {
//                // fail silently
//            }
//        });

        //Initialize Shared Preferences to store device firebase token
        SharedPreferences sharedPref = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Save the instance ID inside shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PUSH_NEW_KEY, pushToken);
        editor.apply();

    }
}