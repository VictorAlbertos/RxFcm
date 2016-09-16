package victoralbertos.io.rxfcm.presentation;

import io.reactivex.Observable;
import rx_fcm.Message;
import victoralbertos.io.rxfcm.data.api.FcmServerService;

/**
 * Created by victor on 08/02/16.
 */
public class FragmentSupplies extends FragmentBase {

    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }

    @Override public boolean matchesTarget(String key) {
        return FcmServerService.TARGET_SUPPLY_GCM.equals(key);
    }
}
