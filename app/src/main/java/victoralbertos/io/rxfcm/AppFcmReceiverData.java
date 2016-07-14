package victoralbertos.io.rxfcm;

import android.os.Bundle;

import rx.Observable;
import rx_fcm.FcmReceiverData;
import rx_fcm.Message;
import victoralbertos.io.rxfcm.data.Cache;
import victoralbertos.io.rxfcm.data.api.FcmServerService;
import victoralbertos.io.rxfcm.data.entities.Notification;

/**
 * Created by victor on 01/04/16.
 */
public class AppFcmReceiverData implements FcmReceiverData {

    @Override public Observable<Message> onNotification(Observable<Message> oMessage) {
        return oMessage.doOnNext(message -> {
            Bundle payload = message.payload();

            String title = payload.getString(FcmServerService.TITLE);
            String body = payload.getString(FcmServerService.BODY);

            if (message.target().equals(FcmServerService.TARGET_ISSUE_GCM)) Cache.Pool.addIssue(new Notification(title, body));
            else if (message.target().equals(FcmServerService.TARGET_SUPPLY_GCM)) Cache.Pool.addSupply(new Notification(title, body));
            else if (message.target().equals(FcmServerService.TARGET_NESTED_SUPPLY_GCM)) Cache.Pool.addNestedSupply(new Notification(title, body));
        });
    }
}
