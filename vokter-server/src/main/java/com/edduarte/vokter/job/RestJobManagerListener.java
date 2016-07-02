/*
 * Copyright 2015 Eduardo Duarte
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.edduarte.vokter.job;

import com.edduarte.vokter.diff.Match;
import com.edduarte.vokter.persistence.Session;
import com.edduarte.vokter.rest.model.Notification;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.util.Set;


/**
 * @author Eduardo Duarte (<a href="mailto:hello@edduarte.com">hello@edduarte.com</a>)
 * @version 1.3.2
 * @since 1.0.0
 */
public class RestJobManagerListener implements JobManagerListener {

    @Override
    public boolean onNotification(String documentUrl, String documentContentType,
                                  Session session, Set<Match> diffs) {

        Response response = ClientBuilder.newClient()
                .target(session.getClientUrl())
                .request(session.getClientContentType())
                .header("Authorization", session.getToken())
                .post(Entity.entity(
                        Notification.ok(documentUrl, documentContentType, diffs),
                        session.getClientContentType()
                ));

        return response.getStatus() == 200;
    }


    @Override
    public boolean onTimeout(String documentUrl, String documentContentType,
                             Session session) {
        Response response = ClientBuilder.newClient()
                .target(session.getClientUrl())
                .request(session.getClientContentType())
                .header("Authorization", session.getToken())
                .post(Entity.entity(
                        Notification.timeout(documentUrl, documentContentType),
                        session.getClientContentType()
                ));

        return response.getStatus() == 200;
    }
}
