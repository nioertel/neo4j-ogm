/*
 * Copyright (c) 2002-2022 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
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
package org.neo4j.ogm.persistence.authentication;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assume.*;

import org.apache.http.client.HttpResponseException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.TestContainersTestBase;
import org.neo4j.ogm.transaction.Transaction;

/**
 * @author Vince Bickers
 * @author Luanne Misquitta
 */
@Ignore
public class AuthenticatingHttpDriverTest extends TestContainersTestBase {

    private Session session;

    @Before
    public void beforeMethod() {
        assumeTrue(isEmbeddedDriver());
    }

    @Test
    public void testUnauthorizedDriver() {
        try {
            session = new SessionFactory(getBaseConfigurationBuilder().credentials("", "").build(), "dummy")
                .openSession();
            Transaction tx = session.beginTransaction();
            tx.commit();
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            while (!(cause instanceof HttpResponseException)) {
                cause = cause.getCause();
            }
            assertThat(cause.getMessage().startsWith("Invalid username or password")).isTrue();
        }
    }

    @Test
    public void testAuthorizedDriver() {
        try {
            session = new SessionFactory(getDriver(), "dummy").openSession();
            Transaction ignored = session.beginTransaction();
            assertThat(ignored).isNotNull();
        } catch (Exception rpe) {
            fail("'" + rpe.getLocalizedMessage() + "' was not expected here");
        }
    }

    @Test // GH-35
    public void testInvalidCredentials() {

        try {
            session = new SessionFactory(getBaseConfigurationBuilder().credentials("neo4j", "invalid_password").build(),
                "dummy")
                .openSession();
            Transaction tx = session.beginTransaction();
            fail("Driver should not have authenticated");
        } catch (Exception rpe) {
            Throwable cause = rpe.getCause();
            while (!(cause instanceof HttpResponseException)) {
                cause = cause.getCause();
            }
            assertThat(cause.getMessage()).isEqualTo("Invalid username or password.");
        }
    }
}
