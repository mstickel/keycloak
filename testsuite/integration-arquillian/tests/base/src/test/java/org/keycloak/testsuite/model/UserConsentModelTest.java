/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
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

package org.keycloak.testsuite.model;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.storage.client.ClientStorageProviderModel;
import org.keycloak.testsuite.AbstractTestRealmKeycloakTest;
import org.keycloak.testsuite.arquillian.annotation.ModelTest;
import org.keycloak.testsuite.federation.HardcodedClientStorageProviderFactory;
import org.keycloak.testsuite.runonserver.RunOnServerDeployment;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.keycloak.testsuite.arquillian.DeploymentTargetModifier.AUTH_SERVER_CURRENT;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserConsentModelTest extends AbstractTestRealmKeycloakTest {

    private static ComponentModel clientStorageComponent;

    @Deployment
    @TargetsContainer(AUTH_SERVER_CURRENT)
    public static WebArchive deploy() {
        return RunOnServerDeployment.create(UserResource.class, UserConsentModelTest.class)
                .addPackages(true,
                        "org.keycloak.testsuite",
                        "org.keycloak.testsuite.model");
    }

    @Before
    public void before() {
        testingClient.server().run(session -> {
            setupEnv(session);
        });
    }

    @After
    public void after() {
        testingClient.server().run(session -> {

            RealmManager realmManager = new RealmManager(session);
            RealmModel realm = realmManager.getRealmByName("original");

            if (realm != null) {

                session.sessions().removeUserSessions(realm);
                UserModel user = session.users().getUserByUsername("user", realm);
                UserModel user1 = session.users().getUserByUsername("user1", realm);
                UserModel user2 = session.users().getUserByUsername("user2", realm);
                UserModel user3 = session.users().getUserByUsername("user3", realm);

                UserManager um = new UserManager(session);
                if (user != null) {
                    um.removeUser(realm, user);
                }
                if (user1 != null) {
                    um.removeUser(realm, user1);
                }
                if (user2 != null) {
                    um.removeUser(realm, user2);
                }
                if (user3 != null) {
                    um.removeUser(realm, user3);
                }
                realmManager.removeRealm(realm);
            }
        });
    }

    public static void setupEnv(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionEnv) -> {
            KeycloakSession currentSession = sessionEnv;

            RealmManager realmManager = new RealmManager(currentSession);
            RealmModel realm = realmManager.createRealm("original");

            ClientModel fooClient = realm.addClient("foo-client");
            ClientModel barClient = realm.addClient("bar-client");

            ClientScopeModel fooScope = realm.addClientScope("foo");
            fooScope.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);

            ClientScopeModel barScope = realm.addClientScope("bar");
            fooScope.setProtocol(OIDCLoginProtocol.LOGIN_PROTOCOL);

            UserModel john = currentSession.users().addUser(realm, "john");
            UserModel mary = currentSession.users().addUser(realm, "mary");

            UserConsentModel johnFooGrant = new UserConsentModel(fooClient);
            johnFooGrant.addGrantedClientScope(fooScope);
            realmManager.getSession().users().addConsent(realm, john.getId(), johnFooGrant);

            UserConsentModel johnBarGrant = new UserConsentModel(barClient);
            johnBarGrant.addGrantedClientScope(barScope);

            // Update should fail as grant doesn't yet exists
            try {
                realmManager.getSession().users().updateConsent(realm, john.getId(), johnBarGrant);
                Assert.fail("Not expected to end here");
            } catch (ModelException expected) {
            }

            realmManager.getSession().users().addConsent(realm, john.getId(), johnBarGrant);

            UserConsentModel maryFooGrant = new UserConsentModel(fooClient);
            maryFooGrant.addGrantedClientScope(fooScope);
            realmManager.getSession().users().addConsent(realm, mary.getId(), maryFooGrant);

            ClientStorageProviderModel clientStorage = new ClientStorageProviderModel();
            clientStorage.setProviderId(HardcodedClientStorageProviderFactory.PROVIDER_ID);
            clientStorage.getConfig().putSingle(HardcodedClientStorageProviderFactory.CLIENT_ID, "hardcoded-client");
            clientStorage.getConfig().putSingle(HardcodedClientStorageProviderFactory.REDIRECT_URI, "http://localhost:8081/*");
            clientStorage.getConfig().putSingle(HardcodedClientStorageProviderFactory.CONSENT, "true");
            clientStorage.setParentId(realm.getId());
            clientStorageComponent = realm.addComponentModel(clientStorage);

            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);

            Assert.assertNotNull(hardcodedClient);

            UserConsentModel maryHardcodedGrant = new UserConsentModel(hardcodedClient);
            realmManager.getSession().users().addConsent(realm, mary.getId(), maryHardcodedGrant);
        });
    }

    @Test
    @ModelTest
    public void basicConsentTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionCT) -> {
            KeycloakSession currentSession = sessionCT;

            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            ClientModel barClient = realm.getClientByClientId("bar-client");

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            UserModel mary = currentSession.users().getUserByUsername("mary", realm);

            UserConsentModel johnFooConsent = currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId());
            Assert.assertEquals(johnFooConsent.getGrantedClientScopes().size(), 1);
            Assert.assertTrue(isClientScopeGranted(realm, "foo", johnFooConsent));
            Assert.assertNotNull("Created Date should be set", johnFooConsent.getCreatedDate());
            Assert.assertNotNull("Last Updated Date should be set", johnFooConsent.getLastUpdatedDate());

            UserConsentModel johnBarConsent = currentSession.users().getConsentByClient(realm, john.getId(), barClient.getId());
            Assert.assertEquals(johnBarConsent.getGrantedClientScopes().size(), 1);
            Assert.assertTrue(isClientScopeGranted(realm, "bar", johnBarConsent));
            Assert.assertNotNull("Created Date should be set", johnBarConsent.getCreatedDate());
            Assert.assertNotNull("Last Updated Date should be set", johnBarConsent.getLastUpdatedDate());

            UserConsentModel maryConsent = currentSession.users().getConsentByClient(realm, mary.getId(), fooClient.getId());
            Assert.assertEquals(maryConsent.getGrantedClientScopes().size(), 1);
            Assert.assertTrue(isClientScopeGranted(realm, "foo", maryConsent));
            Assert.assertNotNull("Created Date should be set", maryConsent.getCreatedDate());
            Assert.assertNotNull("Last Updated Date should be set", maryConsent.getLastUpdatedDate());

            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);
            UserConsentModel maryHardcodedConsent = currentSession.users().getConsentByClient(realm, mary.getId(), hardcodedClient.getId());
            Assert.assertEquals(maryHardcodedConsent.getGrantedClientScopes().size(), 0);
            Assert.assertNotNull("Created Date should be set", maryHardcodedConsent.getCreatedDate());
            Assert.assertNotNull("Last Updated Date should be set", maryHardcodedConsent.getLastUpdatedDate());

            Assert.assertNull(currentSession.users().getConsentByClient(realm, mary.getId(), barClient.getId()));
            Assert.assertNull(currentSession.users().getConsentByClient(realm, john.getId(), hardcodedClient.getId()));
        });
    }

    @Test
    @ModelTest
    public void getAllConsentTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionACT) -> {
            KeycloakSession currentSession = sessionACT;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            UserModel mary = currentSession.users().getUserByUsername("mary", realm);

            List<UserConsentModel> johnConsents = currentSession.users().getConsents(realm, john.getId());
            Assert.assertEquals(2, johnConsents.size());

            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);

            List<UserConsentModel> maryConsents = currentSession.users().getConsents(realm, mary.getId());
            Assert.assertEquals(2, maryConsents.size());
            UserConsentModel maryConsent = maryConsents.get(0);
            UserConsentModel maryHardcodedConsent = maryConsents.get(1);
            if (maryConsents.get(0).getClient().getId().equals(hardcodedClient.getId())) {
                maryConsent = maryConsents.get(1);
                maryHardcodedConsent = maryConsents.get(0);

            }
            Assert.assertEquals(maryConsent.getClient().getId(), fooClient.getId());
            Assert.assertEquals(maryConsent.getGrantedClientScopes().size(), 1);
            Assert.assertTrue(isClientScopeGranted(realm, "foo", maryConsent));

            Assert.assertEquals(maryHardcodedConsent.getClient().getId(), hardcodedClient.getId());
            Assert.assertEquals(maryHardcodedConsent.getGrantedClientScopes().size(), 0);
        });
    }

    @Test
    @ModelTest
    public void updateWithClientScopeRemovalTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession removalTestSession1) -> {
            KeycloakSession currentSession = removalTestSession1;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            UserModel john = currentSession.users().getUserByUsername("john", realm);

            UserConsentModel johnConsent = currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId());
            Assert.assertEquals(1, johnConsent.getGrantedClientScopes().size());

            // Remove foo protocol mapper from johnConsent
            ClientScopeModel fooScope = KeycloakModelUtils.getClientScopeByName(realm, "foo");
            johnConsent.getGrantedClientScopes().remove(fooScope);

            currentSession.users().updateConsent(realm, john.getId(), johnConsent);
        });

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession removalTestSession2) -> {
            KeycloakSession currentSession = removalTestSession2;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            UserModel john = currentSession.users().getUserByUsername("john", realm);
            UserConsentModel johnConsent = currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId());

            Assert.assertEquals(johnConsent.getGrantedClientScopes().size(), 0);
            Assert.assertTrue("Created date should be less than last updated date", johnConsent.getCreatedDate() < johnConsent.getLastUpdatedDate());
        });
    }

    @Test
    @ModelTest
    public void revokeTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionRT1) -> {
            KeycloakSession currentSession = sessionRT1;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            UserModel john = currentSession.users().getUserByUsername("john", realm);
            UserModel mary = currentSession.users().getUserByUsername("mary", realm);

            currentSession.users().revokeConsentForClient(realm, john.getId(), fooClient.getId());
            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);
            currentSession.users().revokeConsentForClient(realm, mary.getId(), hardcodedClient.getId());
        });

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionRT2) -> {
            KeycloakSession currentSession = sessionRT2;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            Assert.assertNull(currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId()));
            UserModel mary = currentSession.users().getUserByUsername("mary", realm);
            Assert.assertNull(currentSession.users().getConsentByClient(realm, mary.getId(), hardcodedClient.getId()));
        });
    }

    @Test
    @ModelTest
    public void deleteUserTest(KeycloakSession session) {
        // Validate user deleted without any referential constraint errors
        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionUT) -> {
            KeycloakSession currentSession = sessionUT;
            RealmModel realm = currentSession.realms().getRealm("original");

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            currentSession.users().removeUser(realm, john);
            UserModel mary = currentSession.users().getUserByUsername("mary", realm);
            currentSession.users().removeUser(realm, mary);
        });
    }

    @Test
    @ModelTest
    public void deleteClientScopeTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionST1) -> {
            KeycloakSession currentSession = sessionST1;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientScopeModel fooScope = KeycloakModelUtils.getClientScopeByName(realm, "foo");
            realm.removeClientScope(fooScope.getId());
        });

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionST2) -> {
            KeycloakSession currentSession = sessionST2;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            UserConsentModel johnConsent = currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId());

            Assert.assertEquals(johnConsent.getGrantedClientScopes().size(), 0);
        });
    }

    @Test
    @ModelTest
    public void deleteClientTest(KeycloakSession session) {

        AtomicReference<String> barClientID = new AtomicReference<>();

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionDCT1) -> {
            KeycloakSession currentSession = sessionDCT1;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel barClient = realm.getClientByClientId("bar-client");
            barClientID.set(barClient.getId());

            realm.removeClient(barClient.getId());
        });

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionDCT2) -> {
            KeycloakSession currentSession = sessionDCT2;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel fooClient = realm.getClientByClientId("foo-client");
            Assert.assertNull(realm.getClientByClientId("bar-client"));

            UserModel john = currentSession.users().getUserByUsername("john", realm);
            ClientModel barClient = realm.getClientByClientId("bar-client");

            UserConsentModel johnFooConsent = currentSession.users().getConsentByClient(realm, john.getId(), fooClient.getId());
            Assert.assertEquals(johnFooConsent.getGrantedClientScopes().size(), 1);
            Assert.assertTrue(isClientScopeGranted(realm, "foo", johnFooConsent));

            Assert.assertNull(currentSession.users().getConsentByClient(realm, john.getId(), barClientID.get()));
        });
    }

    @Test
    @ModelTest
    public void deleteClientStorageTest(KeycloakSession session) {

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionCST1) -> {
            KeycloakSession currentSession = sessionCST1;
            RealmModel realm = currentSession.realms().getRealm("original");

            realm.removeComponent(clientStorageComponent);
        });

        KeycloakModelUtils.runJobInTransaction(session.getKeycloakSessionFactory(), (KeycloakSession sessionCST2) -> {
            KeycloakSession currentSession = sessionCST2;
            RealmModel realm = currentSession.realms().getRealm("original");

            ClientModel hardcodedClient = currentSession.realms().getClientByClientId("hardcoded-client", realm);
            Assert.assertNull(hardcodedClient);

            UserModel mary = currentSession.users().getUserByUsername("mary", realm);

            List<UserConsentModel> maryConsents = currentSession.users().getConsents(realm, mary.getId());
            Assert.assertEquals(1, maryConsents.size());
        });
    }

    private boolean isClientScopeGranted(RealmModel realm, String scopeName, UserConsentModel consentModel) {
        ClientScopeModel clientScope = KeycloakModelUtils.getClientScopeByName(realm, scopeName);
        return consentModel.isClientScopeGranted(clientScope);
    }

    @Override
    public void configureTestRealm(RealmRepresentation testRealm) {

    }
}
