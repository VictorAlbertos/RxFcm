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

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Action1;
import rx_fcm.FcmReceiverUIBackground;
import rx_fcm.Message;

public class FcmReceiverMockUIBackground implements FcmReceiverUIBackground {
    private static List<Message> messages;
    private static long onNotificationStartTimeStamp;

    public static void initSubscriber() {
        messages = new ArrayList<>();
    }

    @Override public void onNotification(Observable<Message> oMessage) {
        onNotificationStartTimeStamp = System.currentTimeMillis();

        oMessage.subscribe(new Action1<Message>() {
            @Override public void call(Message message) {
                messages.add(message);
            }
        });
    }

    public static List<Message> getMessages() {
        return messages;
    }

    public static long getOnNotificationStartTimeStamp() {
        return onNotificationStartTimeStamp;
    }
}
