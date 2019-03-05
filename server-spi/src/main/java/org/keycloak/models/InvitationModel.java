package org.keycloak.models;

public interface InvitationModel {

    String getId();

    UserModel getInviter();

    void setInviter(UserModel user);

    String getInviteeEmail();

    void setInviteeEmail(String email);

    String getInvitationToken();

    void setInvitationToken(String invitationToken);

    boolean isUsed();

    void setUsed(boolean used);

    default void markUsed() {
        this.setUsed(true);
    }
}
