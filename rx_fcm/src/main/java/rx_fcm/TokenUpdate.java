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

/**
 * Entity received as the updated token value when it has been refreshed.
 */
public class TokenUpdate {
    private final String token;
    private final Application application;

    public TokenUpdate(String token, Application application) {
        this.token = token;
        this.application = application;
    }

    public String getToken() {
        return token;
    }

    public Application getApplication() {
        return application;
    }
}
