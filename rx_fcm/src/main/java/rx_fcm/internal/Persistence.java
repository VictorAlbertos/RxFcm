/*
 * Copyright 2016 Victor Albertos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx_fcm.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

class Persistence {

    void saveClassNameFcmReceiverAndFcmReceiverUIBackground(String fcmReceiverClassName, String fcmReceiverUIBackgroundClassName, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        sharedPreferences.edit()
                .putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER, fcmReceiverClassName)
                .putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER_UI_BACKGROUND, fcmReceiverUIBackgroundClassName)
                .apply();
    }

    String getClassNameFcmReceiver(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER, null);
    }

    String getClassNameFcmReceiverUIBackground(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER_UI_BACKGROUND, null);
    }

    void saveClassNameGcmRefreshTokenReceiver(String name, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_REFRESH_TOKEN, name).apply();
    }

    String getClassNameFcmRefreshTokenReceiver(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_REFRESH_TOKEN, null);
    }

    void saveToken(String token, Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.edit().putString(Constants.KEY_SHARED_PREFERENCES_TOKEN, token).apply();
    }

    String getToken(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(Constants.KEY_SHARED_PREFERENCES_TOKEN, null);
    }

}
