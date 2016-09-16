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

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import java.util.ArrayList;
import java.util.List;
import rx_fcm.FcmReceiverData;
import rx_fcm.Message;

public class FcmReceiverDataMock implements FcmReceiverData {
    private static List<Message> messages;
    private static long onNotificationFinishTimeStamp;

    public static void initSubscriber() {
        messages = new ArrayList<>();
    }

    @Override public Observable<Message> onNotification(Observable<Message> oMessage) {
        return oMessage.doOnNext(new Consumer<Message>() {
            @Override public void accept(Message foregroundMessage) throws Exception {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}

                messages.add(foregroundMessage);
                onNotificationFinishTimeStamp = System.currentTimeMillis();

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {}
            }
        });
    }

    public static List<Message> getMessages() {
        return messages;
    }

    public static long getOnNotificationFinishTimeStamp() {
        return onNotificationFinishTimeStamp;
    }
}
