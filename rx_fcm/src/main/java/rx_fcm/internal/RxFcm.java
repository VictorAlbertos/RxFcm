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

import android.app.Application;
import android.os.Bundle;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.Callable;
import rx_fcm.FcmReceiverData;
import rx_fcm.FcmReceiverUIBackground;
import rx_fcm.FcmRefreshTokenReceiver;
import rx_fcm.Message;
import rx_fcm.TokenUpdate;
import victoralbertos.io.rx_fcm.R;

/**
 * Ensures a single instance of RxFcm for the entire Application.
 */
public enum RxFcm {
    Notifications;

    private String rxFcmKeyTarget = "rx_fcm_key_target";
    private ActivitiesLifecycleCallbacks activitiesLifecycle;
    private GetFcmServerToken getFcmServerToken;
    private GetFcmReceiversUIForeground getFcmReceiversUIForeground;
    private FcmReceiverData fcmReceiverData;
    private FcmReceiverUIBackground fcmReceiverUIBackground;
    private FcmRefreshTokenReceiver fcmRefreshTokenReceiver;
    private boolean testing;
    private Scheduler mainThreadScheduler;

    //VisibleForTesting
    void initForTesting(GetFcmServerToken getFcmServerToken,
        ActivitiesLifecycleCallbacks activitiesLifecycle, GetFcmReceiversUIForeground getFcmReceiversUIForeground,
        FcmReceiverData fcmReceiverData, FcmReceiverUIBackground fcmReceiverUIBackground,
        FcmRefreshTokenReceiver fcmRefreshTokenReceiver) {
        this.testing = true;
        this.getFcmServerToken = getFcmServerToken;
        this.mainThreadScheduler = Schedulers.io();
        this.activitiesLifecycle = activitiesLifecycle;
        this.getFcmReceiversUIForeground = getFcmReceiversUIForeground;
        this.fcmReceiverData = fcmReceiverData;
        this.fcmReceiverUIBackground = fcmReceiverUIBackground;
        this.fcmRefreshTokenReceiver = fcmRefreshTokenReceiver;
    }

    void init(Application application) {
        if (testing || activitiesLifecycle != null) return;
        this.getFcmServerToken = new GetFcmServerToken();
        this.getFcmReceiversUIForeground = new GetFcmReceiversUIForeground();
        this.activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);
        this.mainThreadScheduler = AndroidSchedulers.mainThread();
    }

    /**
     *
     * @param application The Android Application instance.
     * @param fcmReceiverData A class which implements {@link FcmReceiverData}
     * @param fcmReceiverUIBackground A class which implements {@link FcmReceiverUIBackground}
     */
    public void init(Application application,
        FcmReceiverData fcmReceiverData, FcmReceiverUIBackground fcmReceiverUIBackground) {
        this.fcmReceiverData = fcmReceiverData;
        this.fcmReceiverUIBackground = fcmReceiverUIBackground;
        init(application);
    }

  /**
   *
   * @param application The Android Application instance.
   * @param fcmReceiverData A class which implements {@link FcmReceiverData}
   * @param fcmReceiverUIBackground A class which implements {@link FcmReceiverUIBackground}
   * @param rxFcmKeyTarget The name of the json node which contains the name of the target screen
   */
    public void init(Application application,
        FcmReceiverData fcmReceiverData, FcmReceiverUIBackground fcmReceiverUIBackground, String rxFcmKeyTarget) {
        this.fcmReceiverData = fcmReceiverData;
        this.fcmReceiverUIBackground = fcmReceiverUIBackground;
        this.rxFcmKeyTarget = rxFcmKeyTarget;
        init(application);
    }

    /**
     * @return Current token associated with the device on FCM serve.
     */
    public Observable<String> currentToken() {
        return Observable.fromCallable(new Callable<String>() {
            @Override public String call() throws Exception {
                return getFcmServerToken.retrieve();
            }
        });
    }

    /**
     * @param fcmRefreshTokenReceiver A class which implements {@link FcmRefreshTokenReceiver}.
     */
    public void onRefreshToken(FcmRefreshTokenReceiver fcmRefreshTokenReceiver) {
        this.fcmRefreshTokenReceiver = fcmRefreshTokenReceiver;
    }

    void onTokenRefreshed() {
        if (fcmRefreshTokenReceiver == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_REFRESH_TOKEN);
            return;
        }

        TokenUpdate tokenUpdate = new TokenUpdate(getFcmServerToken.retrieve(), activitiesLifecycle.getApplication());
        fcmRefreshTokenReceiver.onTokenReceive(Observable.just(tokenUpdate));
    }

    void onNotificationReceived(String from, Bundle payload) {
        Application application = activitiesLifecycle.getApplication();
        String target = payload != null ? payload.getString(rxFcmKeyTarget, null) : "";

        Observable<Message> oMessage = Observable.just(new Message(from, payload, target, application));

        fcmReceiverData.onNotification(oMessage)
                .doOnNext(new Consumer<Message>() {
                    @Override public void accept(Message message) throws Exception {
                        if (activitiesLifecycle.isAppOnBackground()) {
                            notifyGcmReceiverBackgroundMessage(message);
                        } else {
                            notifyGcmReceiverForegroundMessage(message);
                        }
                    }
                })
                .subscribe(new Consumer<Message>() {
                    @Override public void accept(Message message) throws Exception {
                    }
                }, new Consumer<Throwable>() {
                    @Override public void accept(Throwable throwable) throws Exception {
                        String message =
                            "Error thrown from GcmReceiverData subscription. Cause exception: "
                                + throwable.getMessage();
                        Log.e("RxGcm", message);
                    }
                });
    }

    private void notifyGcmReceiverBackgroundMessage(Message message) {
        fcmReceiverUIBackground
            .onNotification(Observable.just(message));
    }

    private void notifyGcmReceiverForegroundMessage(Message message) {
        final GetFcmReceiversUIForeground.Wrapper wrapperGcmReceiverUIForeground = getFcmReceiversUIForeground
            .retrieve(message.target(), activitiesLifecycle.getLiveActivityOrNull());
        if (wrapperGcmReceiverUIForeground == null) return;

        Observable<Message> oNotification = Observable.just(message)
            .observeOn(mainThreadScheduler);

        if (wrapperGcmReceiverUIForeground.isTargetScreen()) {
            wrapperGcmReceiverUIForeground.fcmReceiverUIForeground()
                .onTargetNotification(oNotification);
        } else {
            wrapperGcmReceiverUIForeground.fcmReceiverUIForeground()
                .onMismatchTargetNotification(oNotification);
        }
    }

    private String getAppName() {
        return activitiesLifecycle.getApplication().getString(R.string.app_name);
    }
}
