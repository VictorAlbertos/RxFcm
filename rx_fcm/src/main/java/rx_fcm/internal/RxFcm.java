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
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import rx_fcm.FcmReceiverData;
import rx_fcm.FcmReceiverUIBackground;
import rx_fcm.FcmRefreshTokenReceiver;
import rx_fcm.Message;
import rx_fcm.TokenUpdate;
import victoralbertos.io.rx_fcm.R;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Ensures a single instance of RxFcm for the entire Application.
 */
public enum RxFcm {
    Notifications;

    private String rxFcmKeyTarget = "rx_fcm_key_target";
    private ActivitiesLifecycleCallbacks activitiesLifecycle;
    private GetFcmServerToken getFcmServerToken;
    private Persistence persistence;
    private GetFcmReceiversUIForeground getFcmReceiversUIForeground;
    private boolean testing;
    private Scheduler mainThreadScheduler;

    //VisibleForTesting
    void initForTesting(GetFcmServerToken getFcmServerToken, Persistence persistence, ActivitiesLifecycleCallbacks activitiesLifecycle, GetFcmReceiversUIForeground getFcmReceiversUIForeground) {
        this.testing = true;
        this.getFcmServerToken = getFcmServerToken;
        this.persistence = persistence;
        this.mainThreadScheduler = Schedulers.io();
        this.activitiesLifecycle = activitiesLifecycle;
        this.getFcmReceiversUIForeground = getFcmReceiversUIForeground;
    }

    void init(Application application) {
        if (testing || activitiesLifecycle != null) return;
        this.getFcmServerToken = new GetFcmServerToken();
        this.persistence = new Persistence();
        this.getFcmReceiversUIForeground = new GetFcmReceiversUIForeground();
        this.activitiesLifecycle = new ActivitiesLifecycleCallbacks(application);
        this.mainThreadScheduler = AndroidSchedulers.mainThread();
    }

    /**
     *
     * @param application The Android Application instance.
     * @param gcmReceiverDataClass A class which implements {@link FcmReceiverData}
     * @param gcmReceiverUIBackgroundClass A class which implements {@link FcmReceiverUIBackground}
     */
    public <T extends FcmReceiverData, U extends FcmReceiverUIBackground> void init(final Application application,
                                                                                    final Class<T> gcmReceiverDataClass,
                                                                                    final Class<U> gcmReceiverUIBackgroundClass) {
        init(application);

        Context context = activitiesLifecycle.getApplication();
        persistence.saveClassNameFcmReceiverAndFcmReceiverUIBackground(gcmReceiverDataClass.getName(),
            gcmReceiverUIBackgroundClass.getName(), context);
    }

  /**
   *
   * @param application The Android Application instance.
   * @param gcmReceiverDataClass A class which implements {@link FcmReceiverData}
   * @param gcmReceiverUIBackgroundClass A class which implements {@link FcmReceiverUIBackground}
   * @param rxFcmKeyTarget The name of the json node which contains the name of the target screen
   */
    public <T extends FcmReceiverData, U extends FcmReceiverUIBackground> void init(final Application application,
                                                                                    final Class<T> gcmReceiverDataClass,
                                                                                    final Class<U> gcmReceiverUIBackgroundClass,
                                                                                    final String rxFcmKeyTarget) {
        init(application);

        Context context = activitiesLifecycle.getApplication();
        persistence.saveClassNameFcmReceiverAndFcmReceiverUIBackground(gcmReceiverDataClass.getName(),
            gcmReceiverUIBackgroundClass.getName(), context);

        this.rxFcmKeyTarget = rxFcmKeyTarget;
    }

    /**
     * @return Current token associated with the device on FCM serve.
     */
    public Observable<String> currentToken() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                Context context = activitiesLifecycle.getApplication();
                String token = persistence.getToken(context);

                if (token != null) {
                    emitter.onNext(token);
                } else {
                    try {
                        token = getFcmServerToken.retrieve(activitiesLifecycle.getApplication());
                        persistence.saveToken(token, context);
                        emitter.onNext(token);
                    } catch (Exception e) {
                        emitter.onError(e);
                    }
                }

                emitter.onComplete();
            }
        });
    }

    /**
     * @param aClass The class which implements {@link FcmRefreshTokenReceiver}.
     */
    public <T extends FcmRefreshTokenReceiver> void onRefreshToken(Class<T> aClass) {
        persistence.saveClassNameGcmRefreshTokenReceiver(aClass.getName(), activitiesLifecycle.getApplication());
    }

    void onTokenRefreshed() {
        String newToken;
        Observable oExceptionGcmServer;
        try {
            newToken = getFcmServerToken.retrieve(activitiesLifecycle.getApplication());
            persistence.saveToken(newToken, activitiesLifecycle.getApplication());
            oExceptionGcmServer = null;
        } catch (final Exception exception) {
            newToken = null;
            oExceptionGcmServer = Observable.create(new ObservableOnSubscribe() {
                @Override public void subscribe(ObservableEmitter emitter) throws Exception {
                    emitter.onError(new RuntimeException(exception.getMessage()));
                }
            });
        }

        String className = persistence.getClassNameFcmRefreshTokenReceiver(activitiesLifecycle.getApplication());
        if (className == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_REFRESH_TOKEN);
            return;
        }

        FcmRefreshTokenReceiver tokenReceiver = getInstanceClassByName(className);
        if (newToken != null) {
            TokenUpdate tokenUpdate = new TokenUpdate(newToken, activitiesLifecycle.getApplication());
            tokenReceiver.onTokenReceive(Observable.just(tokenUpdate));
        } else {
            tokenReceiver.onTokenReceive(oExceptionGcmServer);
        }
    }

    void onNotificationReceived(String from, Bundle payload) {
        Application application = activitiesLifecycle.getApplication();
        String target = payload != null ? payload.getString(rxFcmKeyTarget, null) : "";

        Observable<Message> oMessage = Observable.just(new Message(from, payload, target, application));

        String className = persistence.getClassNameFcmReceiver(activitiesLifecycle.getApplication());
        FcmReceiverData fcmReceiverData = getInstanceClassByName(className);

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
        String className = persistence.getClassNameFcmReceiverUIBackground(activitiesLifecycle.getApplication());
        final FcmReceiverUIBackground fcmReceiverUIBackground = getInstanceClassByName(className);

        fcmReceiverUIBackground
            .onNotification(Observable.just(message));
    }

    private void notifyGcmReceiverForegroundMessage(Message message) {
        String className = persistence.getClassNameFcmReceiver(activitiesLifecycle.getApplication());

        if (className == null) {
            Log.w(getAppName(), Constants.NOT_RECEIVER_FOR_FOREGROUND_UI_NOTIFICATIONS);
            return;
        }

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

    <T> Class<T> getClassByName(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    <T> T getInstanceClassByName(String className) {
        try {
            T instance = (T) getClassByName(className).newInstance();
            return instance;
        } catch (Exception e) {
            String error = Constants.ERROR_NOT_PUBLIC_EMPTY_CONSTRUCTOR_FOR_CLASS;
            error = error.replace("$$$", className);
            throw new IllegalStateException(error);
        }
    }

    private String getAppName() {
        return activitiesLifecycle.getApplication().getString(R.string.app_name);
    }
}
