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
import rx_fcm.FcmReceiverUIForeground;
import rx_fcm.Message;

public class FcmReceiverMockUIForeground implements FcmReceiverUIForeground {
    private final List<Message> messages;
    private long onNotificationStartTimeStamp;

    public FcmReceiverMockUIForeground() {
        this.messages = new ArrayList<>();
    }

    @Override public void onTargetNotification(Observable<Message> oMessage) {}

    @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {
        onNotificationStartTimeStamp = System.currentTimeMillis();

        oMessage.subscribe(new Consumer<Message>() {
            @Override public void accept(Message message) throws Exception {
                messages.add(message);
            }
        });
    }

    @Override public boolean matchesTarget(String key) {
        return key.equals("GcmReceiverMockUI");
    }

    public List<Message> getMessages() {
        return messages;
    }

    public long getOnNotificationStartTimeStamp() {
        return onNotificationStartTimeStamp;
    }
}
