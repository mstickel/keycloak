package org.keycloak.invite;

import org.keycloak.models.InvitationModel;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.Provider;

import java.util.List;

public interface InvitationProvider extends Provider {

    InvitationModel create(RealmModel realm, String id, UserModel inviter, String email, String invitationToken,
                           Long dateSent);

    List<InvitationModel> findForUser(RealmModel realmModel, UserModel userModel);
}
