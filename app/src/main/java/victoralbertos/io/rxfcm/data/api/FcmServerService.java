package victoralbertos.io.rxfcm.data.api;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx_fcm.internal.RxFcm;

/**
 * Created by victor on 08/02/16.
 */
public class FcmServerService {
    private final ApiFcmServer apiFcmServer;
    public final static String TARGET_ISSUE_GCM = "target_issue_gcm";
    public final static String TARGET_SUPPLY_GCM = "target_supply_gcm";
    public final static String TARGET_NESTED_SUPPLY_GCM = "target_nested_supply_gcm";
    public final static String TITLE = "title", BODY = "body";

    public FcmServerService() {
        this.apiFcmServer = new Retrofit.Builder()
                .baseUrl(ApiFcmServer.URL_BASE)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(ApiFcmServer.class);
    }

    public Observable<Boolean> sendFcmNotificationRequestingIssue(String title, String body) {
        return RxFcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_ISSUE_GCM))
                .concatMap(payload -> apiFcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> sendFcmNotificationRequestingSupply(String title, String body) {
        return RxFcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_SUPPLY_GCM))
                .concatMap(payload -> apiFcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> sendFcmNotificationRequestingNestedSupply(String title, String body) {
        return RxFcm.Notifications.currentToken()
                .map(token -> new Payload(token, title, body, TARGET_NESTED_SUPPLY_GCM))
                .concatMap(payload -> apiFcmServer.sendNotification(payload))
                .map(gcmResponseServerResponse -> gcmResponseServerResponse.body().success())
                .onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    static class FcmResponseServer {
        private final int success;

        public FcmResponseServer(int success) {
            this.success = success;
        }

        boolean success() {
            return success != 0;
        }
    }

    static class Payload {
        private final String to;
        private Notification data;

        public Payload(String to, String title, String body, String target) {
            this.to = to;
            data = new Notification(title, body, target);
        }

        private class Notification {
            private final String title, body, rx_fcm_key_target;

            public Notification(String title, String body, String target) {
                this.title = title;
                this.body = body;
                this.rx_fcm_key_target = target;
            }
        }

    }

}
