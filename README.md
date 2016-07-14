# RxFcm
RxJava extension for Firebase Cloud Messaging which acts as an architectural approach to easily satisfy the requirements of an android app when dealing with push notifications.

## Features:
* Remove android boilerplate code (not need for `Manifest` or `Service(s)` configuration).
* Decouple presentation responsibilities from data responsibilities when receiving notifications.
* Deploy a targeting strategy to aim for the desired Activity/Fragment when receiving notifications.

## Setup
Add RxFcm dependency and Google Services plugin to project level build.gradle.

```gradle
apply plugin: 'com.google.gms.google-services'

dependencies {
    compile 'com.github.VictorAlbertos:RxFcm:0.0.1'
    compile 'io.reactivex:rxjava:1.1.7'
}
```

Add Google Services to classpath and jitpack repository to root level build.gradle.

```gradle
dependencies {
    classpath 'com.google.gms:google-services:3.0.0'
}

allprojects {
    repositories {
        //..
        maven { url "https://jitpack.io" }
    }
}

```

There is, thought, one step behind which RxFcm can't do for you. You have to go to [firebase consolse](https://console.firebase.google.com),  create a google-services.json configuration file and place it in your Android application module. ([This](https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/) is a simple tutorial about how to do it)

## Usage

### FcmReceiverData
[FcmReceiverData](https://github.com/VictorAlbertos/RxFcm/blob/master/rx_fcm/src/main/java/rx_fcm/FcmReceiverData.java) implementation should be responsible for **updating the data models**. The `onNotification` method requires to return an instance of the `observable` supplied as argument, after applying `doOnNext` operator to perform the update action: 

```java
public class AppFcmReceiverData implements FcmReceiverData {    
 
 	@Override public Observable<Message> onNotification(Observable<Message> oMessage) {         
 		return oMessage.doOnNext(message -> {});     
 	} 
 	
 }
```

The `observable` type is an instance of [Message](https://github.com/VictorAlbertos/RxFcm/blob/master/rx_fcm/src/main/java/rx_fcm/Message.java), which holds a reference to the android `Application` instance, the `Bundle` notification and a method called `target()`, which returns the key associated with this notification.  

```java
public class AppFcmReceiverData implements FcmReceiverData {

    @Override public Observable<Message> onNotification(Observable<Message> oMessage) {
        return oMessage.doOnNext(message -> {
            Bundle payload = message.payload();

            String title = payload.getString("title");
            String body = payload.getString("body");

            if (message.target().equals("issues")) SimpleCache.addIssue(new Notification(title, body));
            else if (message.target().equals("supplies")) SimpleCache.addSupply(new Notification(title, body));
        });
    }
    
} 
```

To RxFcm be able to return a not null `string` value when calling `target()` method, you need to add the key rx_fcm_key_target to the payload of the push notification: 
 
```json            
{ 
  "data": {
    "title":"A title 4",
    "body":"A body 4",
    "rx_fcm_key_target":"supplies"
  },
  "to":"token_device"
  }
}
```

If rx_fcm_key_target is not added to the json payload, you will get a null value when calling the `target()` method. So, you can ignore this, but you would be missing the benefits of the targeting strategy.

### FcmReceiverUIBackground and FcmReceiverUIForeground
Both of them will be called only after `FcmReceiverData` `observable` has reached `onCompleted()` state. This way it’s safe to assume that any operation related to updating the data model has been successfully achieved, and now it’s time to reflect these updates in the presentation layer. 

#### FcmReceiverUIBackground
`FcmReceiverUIBackground` implementation will be called when a notification is received and the application is in the background. Probably the implementation class will be responsable for building and showing system notifications. 

```java
public class AppFcmReceiverUIBackground implements FcmReceiverUIBackground {   
   
	@Override public void onNotification(Observable<Message> oMessage) {         
		oMessage.subscribe(message -> buildAndShowNotification(message));     
	}
	
}
```

#### FcmReceiverUIForeground
`FcmReceiverUIForeground` implementation will be called when a notification is received and the application is in the foreground. The implementation class must be an `Activity` or an `android.support.v4.app.Fragment`. `FcmReceiverUIForeground` exposes a method called `matchesTarget()`, which receives an string (the value of the rx_fcm_key_target node payload notification) and forces to the implementation class to return a boolean. 

If the current `Activity` or visible `Fragment` `matchesTarget()` method returns true, `onTargetNotification()` method will be called, otherwise `onMismatchTargetNotification()` method will be called. 

```java
public abstract class BaseFragment extends android.support.v4.app.Fragment implements FcmReceiverUIForeground {      
	
    @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            showAlert(message);
        });
    }  
	  
}
```

```java
public class FragmentIssues extends BaseFragment {      
	
    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }   
	 
    @Override public boolean matchesTarget(String key) {
        return "issues".equals(key);
    }
	  
}
```

```java
public class FragmentSupplies extends android.support.v4.app.Fragment implements FcmReceiverUIForeground {      
	
    @Override public void onTargetNotification(Observable<Message> oMessage) {
        oMessage.subscribe(message -> {
            notificationAdapter.notifyDataSetChanged();
        });
    }     
	  
	@Override public boolean matchesTarget(String key) {
        return "supplies".equals(key);
    }
}
```

**Limitation:**: Your fragments need to extend from `android.support.v4.app.Fragment` instead of `android.app.Fragment`, otherwise they won't be notified. 

### RefreshTokenReceiver
[FcmRefreshTokenReceiver](https://github.com/VictorAlbertos/RxFcm/blob/master/rx_fcm/src/main/java/rx_fcm/FcmRefreshTokenReceiver.java) implementation will be called when the token has been updated. As the [documentation](https://developers.google.com/android/reference/com/google/android/gms/iid/InstanceIDListenerService#onTokenRefresh) points out, the token device may need to be refreshed for some particular reason. 

```java
public class RefreshTokenReceiver implements FcmRefreshTokenReceiver {
    
    @Override public void onTokenReceive(Observable<TokenUpdate> oTokenUpdate) {
        oTokenUpdate.subscribe(tokenUpdate -> {}, error -> {});
    }
    
}
```
 
### Retrieving current token 
If at some point you need to retrieve the fcm token device -e.g for updating the value on your server, you could do it easily calling [RxFcm.Notifications.currentToken](https://github.com/VictorAlbertos/RxFcm/blob/master/rx_fcm/src/main/java/rx_fcm/internal/RxFcm.java#L89):

```java
    RxFcm.Notifications.currentToken().subscribe(token -> {}, error -> {});
```


### Register RxFcm classes
Once you have implemented `FcmReceiverData` and `FcmReceiverUIBackground` interfaces is time to register them in your Android `Application` class calling [RxFcm.Notifications.init](https://github.com/VictorAlbertos/RxFcm/blob/master/rx_fcm/src/main/java/rx_fcm/internal/RxFcm.java#L76). Plus, register `RefreshTokenReceiver` implementation too at this point. 
   
```java
public class RxSampleApp extends Application {

    @Override public void onCreate() {
        super.onCreate();

        RxFcm.Notifications.init(this, AppFcmReceiverData.class, AppFcmReceiverUIBackground.class);   
                
        RxFcm.Notifications.onRefreshToken(RefreshTokenReceiver.class);
    }

}
```

## Examples
There is a complete example of RxFcm in the [app module](https://github.com/VictorAlbertos/RxFcm/tree/master/app). Plus, it has an integration test managed by [Espresso test kit](https://google.github.io/android-testing-support-library/) which shows several uses cases.

## Testing notification
You can easily [send http post request](https://firebase.google.com/docs/cloud-messaging/downstream) to Firebase Cloud Messaging server using Postman or Advanced Rest Client.

## Author
**Víctor Albertos**
* <https://twitter.com/_victorAlbertos>
* <https://linkedin.com/in/victoralbertos>
* <https://github.com/VictorAlbertos>

## Another author's libraries:
* [Mockery](https://github.com/VictorAlbertos/Mockery): Android and Java library for mocking and testing networking layers with built-in support for Retrofit.
* [RxCache](https://github.com/VictorAlbertos/RxCache): Reactive caching library for Android and Java.
* [RxActivityResult](https://github.com/VictorAlbertos/RxActivityResult): A reactive-tiny-badass-vindictive library to break with the OnActivityResult implementation as it breaks the observables chain. 
* [RxSocialConnect](https://github.com/FuckBoilerplate/RxSocialConnect-Android): OAuth RxJava extension for Android.
