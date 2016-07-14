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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx_fcm.FcmReceiverUIForeground;
import rx_fcm.Message;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

public class GetFcmReceiversUIForegroundTestData {
    private GetFcmReceiversUIForeground getFcmReceiversUIForegroundUT;
    @Mock protected ActivityMockReceiver activityMockReceiver;
    @Mock protected ActivityMock activityMock;

    @Mock protected FragmentManager fragmentManagerMock;

    @Before public void setUp()  {
        MockitoAnnotations.initMocks(this);

        getFcmReceiversUIForegroundUT = new GetFcmReceiversUIForeground();
        getFcmReceiversUIForegroundUT.mockForTestingPurposes();
    }

    @Test public void When_Several_GcmReceiverUIForeground_Return_One_Which_Is_Target_Screen() {
        when(fragmentManagerMock.getFragments()).thenReturn(getFragmentsReceivers());
        when(activityMockReceiver.matchesTarget(ActivityMockReceiver.SCREEN_NAME)).thenReturn(true);
        when(activityMockReceiver.getSupportFragmentManager()).thenReturn(fragmentManagerMock);

        String targetScreen = FragmentMock1Receiver.SCREEN_NAME;
        GetFcmReceiversUIForeground.Wrapper wrapper = getFcmReceiversUIForegroundUT.retrieve(targetScreen, activityMockReceiver);
        assertThat(wrapper.isTargetScreen(), is(true));
        assertThat(wrapper.fcmReceiverUIForeground() instanceof FragmentMock1Receiver, is(true));

        targetScreen = FragmentMock2Receiver.SCREEN_NAME;
        wrapper = getFcmReceiversUIForegroundUT.retrieve(targetScreen, activityMockReceiver);
        assertThat(wrapper.isTargetScreen(), is(true));
        assertThat(wrapper.fcmReceiverUIForeground() instanceof FragmentMock2Receiver, is(true));

        reset(fragmentManagerMock);
        when(fragmentManagerMock.getFragments()).thenReturn(new ArrayList<Fragment>());

        targetScreen = ActivityMockReceiver.SCREEN_NAME;
        wrapper = getFcmReceiversUIForegroundUT.retrieve(targetScreen, activityMockReceiver);
        assertThat(wrapper.isTargetScreen(), is(true));
        assertThat(wrapper.fcmReceiverUIForeground() instanceof ActivityMockReceiver, is(true));
    }

    @Test public void When_Several_GcmReceiverUIForeground_But_No_One_Is_Target_Screen_Return_Some_One() {
        when(fragmentManagerMock.getFragments()).thenReturn(getFragmentsReceivers());
        when(activityMockReceiver.matchesTarget(ActivityMockReceiver.SCREEN_NAME)).thenReturn(true);
        when(activityMockReceiver.getSupportFragmentManager()).thenReturn(fragmentManagerMock);

        String targetScreen = "no one";
        GetFcmReceiversUIForeground.Wrapper wrapper = getFcmReceiversUIForegroundUT.retrieve(targetScreen, activityMockReceiver);
        assertThat(wrapper.isTargetScreen(), is(false));
        assertThat(wrapper.fcmReceiverUIForeground() instanceof FragmentMock1Receiver, is(true));

        reset(fragmentManagerMock);
        when(fragmentManagerMock.getFragments()).thenReturn(new ArrayList<Fragment>());

        wrapper = getFcmReceiversUIForegroundUT.retrieve(targetScreen, activityMockReceiver);
        assertThat(wrapper.isTargetScreen(), is(false));
        assertThat(wrapper.fcmReceiverUIForeground() instanceof ActivityMockReceiver, is(true));
    }

    @Test public void When_No_One_GcmReceiverUIForeground_Return_Null_Wrapper() {
        when(fragmentManagerMock.getFragments()).thenReturn(getFragments());
        when(activityMock.getSupportFragmentManager()).thenReturn(fragmentManagerMock);

        GetFcmReceiversUIForeground.Wrapper wrapper = getFcmReceiversUIForegroundUT.retrieve("no one", activityMock);
        assertNull(wrapper);
    }

    private List<Fragment> getFragmentsReceivers() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FragmentMock1Receiver());
        fragments.add(new FragmentMock2Receiver());
        return fragments;
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new FragmentMock1());
        fragments.add(new FragmentMock2());
        return fragments;
    }

    public static class ActivityMockReceiver extends FragmentActivity implements
        FcmReceiverUIForeground {
        private static final String SCREEN_NAME = ActivityMockReceiver.class.getName();

        @Override public void onTargetNotification(Observable<Message> oMessage) {}

        @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {}

        @Override public boolean matchesTarget(String key) {
            return false;
        }
    }

    public static class FragmentMock1Receiver extends Fragment implements FcmReceiverUIForeground {
        private static final String SCREEN_NAME = FragmentMock1Receiver.class.getName();

        @Override public void onTargetNotification(Observable<Message> oMessage) {}

        @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {}

        @Override public boolean matchesTarget(String key) {
            return key.equals(SCREEN_NAME);
        }
    }

    public static class FragmentMock2Receiver extends Fragment implements FcmReceiverUIForeground {
        private static final String SCREEN_NAME = FragmentMock2Receiver.class.getName();

        @Override public void onTargetNotification(Observable<Message> oMessage) {}

        @Override public void onMismatchTargetNotification(Observable<Message> oMessage) {}

        @Override public boolean matchesTarget(String key) {
            return key.equals(SCREEN_NAME);
        }
    }

    public static class ActivityMock extends FragmentActivity {

    }

    public static class FragmentMock1 extends Fragment {

    }

    public static class FragmentMock2 extends Fragment {

    }
}

