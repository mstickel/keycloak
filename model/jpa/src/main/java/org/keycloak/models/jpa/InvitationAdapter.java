package org.keycloak.models.jpa;

import org.keycloak.authorization.AuthorizationProvider;
import org.keycloak.models.InvitationModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.InvitationEntity;

import javax.persistence.EntityManager;

public class InvitationAdapter implements InvitationModel, JpaModel<InvitationEntity> {

    protected InvitationEntity invitation;
    protected EntityManager em;
    protected RealmModel realm;
    private final KeycloakSession session;

    public InvitationAdapter(RealmModel realm, EntityManager em, InvitationEntity invitation,
                             KeycloakSession session) {
        this.em = em;
        this.realm = realm;
        this.invitation = invitation;
        this.session = session;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public UserModel getInviter() {
        return session.users().getUserById(invitation.getInviter().getId(), realm);
    }

    @Override
    public void setInviter(UserModel user) {
        invitation.setInviter(((UserAdapter)user).getEntity());
    }

    @Override
    public String getInviteeEmail() {
        return invitation.getInviteeEmail();
    }

    @Override
    public void setInviteeEmail(String email) {
        invitation.setInviteeEmail(email);
    }

    @Override
    public String getInvitationToken() {
        return invitation.getInvitationToken();
    }

    @Override
    public void setInvitationToken(String invitationToken) {
        invitation.setInvitationToken(invitationToken);
    }

    @Override
    public boolean isUsed() {
        return invitation.isUsed();
    }

    @Override
    public void setUsed(boolean used) {
        invitation.setUsed(used);
    }

    @Override
    public InvitationEntity getEntity() {
        return invitation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof InvitationModel)) return false;

        InvitationModel that = (InvitationModel) o;
        return that.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

}
