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

package rx_fcm;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Entity received as the message on notifications events.
 */
public class Message {
    private final String from;
    private final Bundle payload;
    private final String target;
    private final Application application;

    public Message(String from, Bundle payload, String target, Application application) {
        this.from = from;
        this.payload = payload;
        this.target = target;
        this.application = application;
    }

    public String from() {
        return from;
    }

    @Nullable public String target() {
        return target;
    }

    public Bundle payload() {
        return payload;
    }

    public Application application() {
        return application;
    }
}
