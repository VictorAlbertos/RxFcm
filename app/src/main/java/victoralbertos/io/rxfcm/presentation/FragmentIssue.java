package victoralbertos.io.rxfcm.presentation;

import rx.Observable;
import rx_fcm.Message;
import victoralbertos.io.rxfcm.data.api.FcmServerService;

/**
 * Created by victor on 03/04/16.
 */
public class FragmentIssue extends FragmentBase {

    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override public boolean matchesTarget(String key) {
        return FcmServerService.TARGET_ISSUE_GCM.equals(key);
    }
}
