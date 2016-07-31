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

import android.os.Bundle;

/**
 * Use to mock the reception of Fcm notifications.
 */
public enum RxFcmMock {
  Notifications;

  /**
   * Mock a call to RxFcm with a bundle which will be process as a real Fcm notification.
   *
   * @param bundle the bundle containing the desired data.
   */
  public void newNotification(Bundle bundle) {
    RxFcm.Notifications.onNotificationReceived("mock", bundle);
  }

  /**
   * Mock a call to RxFcm requesting it to update the token device.
   */
  public void updateToken() {
    RxFcm.Notifications.onTokenRefreshed();
  }
}
