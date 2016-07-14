package victoralbertos.io.rxfcm;

import rx.Observable;
import rx_fcm.FcmRefreshTokenReceiver;
import rx_fcm.TokenUpdate;

/**
 * Created by victor on 08/02/16.
 */
public class RefreshTokenReceiver implements FcmRefreshTokenReceiver {

    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.subscribe(tokenUpdate -> {}, error -> {});
    }

}
