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

interface Constants {
    String KEY_SHARED_PREFERENCES_TOKEN = "key_shared_preferences_token";
    String KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER = "key_shared_class_name_gcm_receiver";
    String KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_RECEIVER_UI_BACKGROUND = "key_shared_preferences_class_name_gcm_receiver_ui_background";
    String KEY_SHARED_PREFERENCES_CLASS_NAME_GCM_REFRESH_TOKEN = "key_shared_class_name_gcm_refresh_token";
    String NOT_RECEIVER_FOR_FOREGROUND_UI_NOTIFICATIONS = "A notification on foreground has been received, but it has not been supplied a class which implements GcmReceiverUIForeground";
    String NOT_RECEIVER_FOR_REFRESH_TOKEN = "Token has been refresh but it has not been supplied a class which implements GcmRefreshTokenReceiver";
    String ERROR_NOT_PUBLIC_EMPTY_CONSTRUCTOR_FOR_CLASS = "The class which you have supplied implementing $$$ can have only one public empty constructor";
}
