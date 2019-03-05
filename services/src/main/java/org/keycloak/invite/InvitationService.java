package org.keycloak.invite;

import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.UUID;

public class InvitationService {

    private final InvitationProvider invitationProvider;

    public InvitationService(InvitationProvider invitationProvider) {
        this.invitationProvider = invitationProvider;
    }

    public void invite(RealmModel realm, UserModel inviter, String email) {
        String token = generateInviteToken();
        Long now = System.currentTimeMillis();
        invitationProvider.create(realm, null, inviter, email, token, now);
    }

    String generateInviteToken() {
        return UUID.randomUUID().toString();
    }
}
