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


import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import java.util.ArrayList;
import java.util.List;

import rx_fcm.FcmReceiverUIForeground;

class GetFcmReceiversUIForeground {
    private List<Fragment> fcmReceiversUIForegroundNotTargetScreen;

    Wrapper retrieve(String screenName, Activity activity){
        Wrapper receiverCandidate = null;

        if (activity == null) return receiverCandidate;

        if (activity instanceof FcmReceiverUIForeground) {
            FcmReceiverUIForeground fcmReceiverUIForeground = (FcmReceiverUIForeground) activity;

            boolean targetScreen = fcmReceiverUIForeground.matchesTarget(screenName);
            receiverCandidate = new Wrapper(fcmReceiverUIForeground, targetScreen);

            if (targetScreen) return receiverCandidate;
        }

        if (!(activity instanceof FragmentActivity)) return receiverCandidate;

        FragmentActivity fragmentActivity = (FragmentActivity) activity;
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();

        fcmReceiversUIForegroundNotTargetScreen = new ArrayList<>();
        Fragment fragment = getFcmReceiverUIForeground(fragmentManager.getFragments(), screenName);
        fcmReceiversUIForegroundNotTargetScreen.clear();

        if (fragment != null) {
            FcmReceiverUIForeground fcmReceiverUIForeground = (FcmReceiverUIForeground) fragment;
            boolean isTargetScreen = fcmReceiverUIForeground.matchesTarget(screenName);
            return new Wrapper(fcmReceiverUIForeground, isTargetScreen);
        } else {
            return receiverCandidate;
        }
    }

    @Nullable
    private Fragment getFcmReceiverUIForeground(List<Fragment> fragments, String screenName) {
        if (fragments == null) return null;

        for (Fragment fragment : fragments) {
            if (fragment != null && isVisible(fragment) && fragment instanceof FcmReceiverUIForeground) {
                FcmReceiverUIForeground fcmReceiverUIForeground = (FcmReceiverUIForeground) fragment;
                boolean isTargetScreen = fcmReceiverUIForeground.matchesTarget(screenName);

                if (isTargetScreen) return fragment;

                fcmReceiversUIForegroundNotTargetScreen.add(fragment);

                if (fragment.getChildFragmentManager() != null) {
                    Fragment candidate = getFcmReceiverUIForegroundFromChild(fragment, screenName);
                    if (candidate != null) return candidate;
                }
            } else if (fragment != null && isVisible(fragment) && fragment.getChildFragmentManager() != null) {
                Fragment candidate = getFcmReceiverUIForegroundFromChild(fragment, screenName);
                if (candidate != null) return candidate;
            }
        }

        if (!fcmReceiversUIForegroundNotTargetScreen.isEmpty()) return fcmReceiversUIForegroundNotTargetScreen
            .get(0);
        else return null;
    }

    private Fragment getFcmReceiverUIForegroundFromChild(Fragment fragment, String screenName) {
        List<Fragment> childFragments = fragment.getChildFragmentManager().getFragments();
        Fragment candidate = getFcmReceiverUIForeground(childFragments, screenName);

        if (candidate == null) return null;

        FcmReceiverUIForeground fcmReceiverUIForeground = (FcmReceiverUIForeground) candidate;
        boolean isTargetScreen = fcmReceiverUIForeground.matchesTarget(screenName);
        if (isTargetScreen) return candidate;

        fcmReceiversUIForegroundNotTargetScreen.add(candidate);

        return null;
    }

    static class Wrapper {
        private final FcmReceiverUIForeground fcmReceiverUIForeground;
        private final boolean targetScreen;

        public Wrapper(FcmReceiverUIForeground fcmReceiverUIForeground, boolean targetScreen) {
            this.fcmReceiverUIForeground = fcmReceiverUIForeground;
            this.targetScreen = targetScreen;
        }

        public FcmReceiverUIForeground fcmReceiverUIForeground() {
            return fcmReceiverUIForeground;
        }

        public boolean isTargetScreen() {
            return targetScreen;
        }
    }

    //exists for testing purposes
    private boolean mock;
    void mockForTestingPurposes() {
        mock = true;
    }

    //exists for testing purposes
    boolean isVisible(Fragment fragment) {
        if (mock) return true;
        return fragment.getUserVisibleHint();
    }
}
