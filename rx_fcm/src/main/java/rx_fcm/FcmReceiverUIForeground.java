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

package rx_fcm;

import io.reactivex.Observable;

/**
 * The Activity or Fragments which implement this interface will receive the push notifications when the application is onForeground state
 * just after GcmReceiverData has completed its task.
 * @see FcmReceiverData
 */
public interface FcmReceiverUIForeground {
    /**
     * Called when Activity or Fragment matches with the desired target specified in the bundle notification.
     * @see FcmReceiverUIForeground
     */
    void onTargetNotification(Observable<Message> oMessage);

    /**
     * Called when Activity or Fragment does not match with the desired target specified in the bundle notification.
     * @see FcmReceiverUIForeground
     */
    void onMismatchTargetNotification(Observable<Message> oMessage);

    /**
     * Determines if the implementing class is interested on be notified when updating the data model or seeking for the activity/fragment to be notified.
     * @param key The value provided in the bundle notification by the server
     * @return true if the implementing class is interested on be notified
     */
    boolean matchesTarget(String key);
}
