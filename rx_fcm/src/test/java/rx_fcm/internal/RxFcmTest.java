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
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.observers.TestSubscriber;
import rx_fcm.Message;
import rx_fcm.TokenUpdate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class RxFcmTest {
    @Mock protected Application applicationMock;
    @Mock protected GetFcmServerToken getFcmServerTokenMock;
    @Mock protected Persistence persistenceMock;
    @Mock protected ActivitiesLifecycleCallbacks activitiesLifecycle;
    @Mock protected GetFcmReceiversUIForeground getFcmForegroundReceivers;

    private final static String MOCK_TOKEN = "mock_token";

    @Before public void setUp()  {
        MockitoAnnotations.initMocks(this);
        RxFcm.Notifications.initForTesting(getFcmServerTokenMock, persistenceMock, activitiesLifecycle, getFcmForegroundReceivers);
        when(activitiesLifecycle.getApplication()).thenReturn(applicationMock);
    }

    @Test public void When_Call_Current_Token_Get_Token() {
        when(persistenceMock.getToken(applicationMock)).thenReturn(MOCK_TOKEN);

        TestSubscriber<String> subscriberMock = new TestSubscriber<>();
        RxFcm.Notifications.currentToken().subscribe(subscriberMock);
        subscriberMock.awaitTerminalEvent();

        subscriberMock.assertValue(MOCK_TOKEN);
        subscriberMock.assertNoErrors();
    }

    @Test public void When_Call_On_Token_Refresh_Emit_Properly_Item() throws Exception {
        TestSubscriber<TokenUpdate> subscriberMock = FcmRefreshTokenReceiverMock.initSubscriber();
        when(persistenceMock.getClassNameFcmRefreshTokenReceiver(applicationMock)).thenReturn(FcmRefreshTokenReceiverMock.class.getName());

        when(getFcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN);
        RxFcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        subscriberMock.assertNoErrors();
        TokenUpdate token1 = subscriberMock.getOnNextEvents().get(0);
        assertThat(token1.getToken(), is(MOCK_TOKEN));

        subscriberMock = FcmRefreshTokenReceiverMock.initSubscriber();
        reset(getFcmServerTokenMock);
        String errorMessage = "GCM not available";
        when(getFcmServerTokenMock.retrieve(applicationMock)).thenThrow(new RuntimeException(errorMessage));        RxFcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        subscriberMock.assertNoValues();
        assertThat(subscriberMock.getOnErrorEvents().get(0).getMessage(), is(errorMessage));

        subscriberMock = FcmRefreshTokenReceiverMock.initSubscriber();
        reset(getFcmServerTokenMock);
        when(getFcmServerTokenMock.retrieve(applicationMock)).thenReturn(MOCK_TOKEN + 1);
        RxFcm.Notifications.onTokenRefreshed();
        subscriberMock.awaitTerminalEvent();
        subscriberMock.assertNoErrors();
        TokenUpdate token2 = subscriberMock.getOnNextEvents().get(0);
        assertThat(token2.getToken(), is(MOCK_TOKEN + 1));

        reset(getFcmServerTokenMock);
        when(persistenceMock.getClassNameFcmRefreshTokenReceiver(applicationMock)).thenReturn(null);
        try {
            RxFcm.Notifications.onTokenRefreshed();
            subscriberMock.awaitTerminalEvent();
        } catch (Exception ignore) {
            assertThat(subscriberMock.getOnErrorEvents().size(), is(1));
            subscriberMock.assertValueCount(2);
        }
    }

    @Test public void When_Call_On_Fcm_Receiver_UI_Background_Notification_Emit_Properly_Item() {
        when(activitiesLifecycle.isAppOnBackground()).thenReturn(true);

        //FcmReceiver
        FcmReceiverDataMock.initSubscriber();
        when(persistenceMock.getClassNameFcmReceiver(applicationMock)).thenReturn(FcmReceiverDataMock.class.getName());

        //FcmReceiverUiBackground
        FcmReceiverMockUIBackground.initSubscriber();
        when(persistenceMock.getClassNameFcmReceiverUIBackground(applicationMock)).thenReturn(FcmReceiverMockUIBackground.class.getName());

        Bundle payload = new Bundle();
        String from1 = "MockServer1";
        RxFcm.Notifications.onNotificationReceived(from1, payload);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        String from2 = "MockServer2";
        RxFcm.Notifications.onNotificationReceived(from2, payload);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        //Check FcmReceiver
        List<Message> receiverMessages = FcmReceiverDataMock.getMessages();
        assertThat(receiverMessages.get(0).from(), is(from1));
        assertThat(receiverMessages.get(1).from(), is(from2));
        assertThat(receiverMessages.size(), is(2));

        //Check FcmReceiverBakgroundUI
        List<Message> receiverUIBackgroundMessages = FcmReceiverMockUIBackground.getMessages();
        assertThat(receiverUIBackgroundMessages.get(0).from(), is(from1));
        assertThat(receiverUIBackgroundMessages.get(1).from(), is(from2));
        assertThat(receiverUIBackgroundMessages.size(), is(2));

        //Check uireceiversbackground has been called only after receiver task has completed
        long onNotificationStartTimeStamp = FcmReceiverMockUIBackground.getOnNotificationStartTimeStamp();
        long onNotificationFinishTimeStamp = FcmReceiverDataMock.getOnNotificationFinishTimeStamp();

        assert onNotificationStartTimeStamp > onNotificationFinishTimeStamp;
    }


    @Test public void When_Call_On_Fcm_Receiver_UI_Foreground_Notification_Emit_Properly_Item() {
        when(activitiesLifecycle.isAppOnBackground()).thenReturn(false);

        //FcmReceiver
        FcmReceiverDataMock.initSubscriber();
        when(persistenceMock.getClassNameFcmReceiver(applicationMock)).thenReturn(FcmReceiverDataMock.class.getName());

        //FcmReceiverUI
        GetFcmReceiversUIForeground.Wrapper wrapperFcmReceiverUIForeground = new GetFcmReceiversUIForeground.Wrapper(new FcmReceiverMockUIForeground(), false);
        when(getFcmForegroundReceivers.retrieve(null, null)).thenReturn(wrapperFcmReceiverUIForeground);

        Bundle payload = new Bundle();
        String from1 = "MockServer1";
        RxFcm.Notifications.onNotificationReceived(from1, payload);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        String from2 = "MockServer2";
        RxFcm.Notifications.onNotificationReceived(from2, payload);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        //Check FcmReceiver
        List<Message> receiverMessages = FcmReceiverDataMock.getMessages();
        assertThat(receiverMessages.get(0).from(), is(from1));
        assertThat(receiverMessages.get(1).from(), is(from2));
        assertThat(receiverMessages.size(), is(2));

        //Check FcmReceiverForegroundUI
        FcmReceiverMockUIForeground fcmReceiverMockUIForeground = (FcmReceiverMockUIForeground) wrapperFcmReceiverUIForeground.fcmReceiverUIForeground();
        List<Message> messages = fcmReceiverMockUIForeground.getMessages();
        assertThat(messages.get(0).from(), is(from1));
        assertThat(messages.get(1).from(), is(from2));
        assertThat(messages.size(), is(2));

        //Check uireceiversforeground has been called only after receiver task has completed
        long onNotificationStartTimeStamp = fcmReceiverMockUIForeground.getOnNotificationStartTimeStamp();
        long onNotificationFinishTimeStamp = FcmReceiverDataMock.getOnNotificationFinishTimeStamp();

        assert onNotificationStartTimeStamp > onNotificationFinishTimeStamp;
    }

    @Test(expected=IllegalStateException.class) public void When_Call_Class_With_No_Public_Empty_Constructor_Get_Exception() {
        RxFcm.Notifications.getInstanceClassByName(ClassWithNoPublicEmptyConstructor.class.getName());
    }

    @Test public void When_Call_Class_With_Public_Empty_Constructor_Not_Get_Exception() {
        RxFcm.Notifications.getInstanceClassByName(ClassWithPublicEmptyConstructor.class.getName());
    }
}
