package org.keycloak.forms.account.freemarker.model;

import com.google.common.collect.Lists;
import org.keycloak.common.util.Time;
import org.keycloak.models.InvitationModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class InvitationsBean {

    private final List<InvitationEntry> invitations;

    public InvitationsBean(KeycloakSession session, RealmModel realm, UserModel user, List<InvitationModel> invitations) {
        this.invitations = invitations != null ? invitations.stream().map(this::convertToEntry).collect(Collectors.toList()) : Lists.newArrayList();
    }

    public List<InvitationEntry> getInvitations() {
        return invitations;
    }

    private InvitationEntry convertToEntry(InvitationModel model) {
        return new InvitationEntry(model.getInviteeEmail(), System.currentTimeMillis(), model.isUsed() ? "USED" : "OPEN");
    }

    public static class InvitationEntry {

        private String sentToEmail;
        private Long date;
        private String status;

        public InvitationEntry(String sentToEmail, Long date, String status) {
            this.sentToEmail = sentToEmail;
            this.date = date;
            this.status = status;
        }

        public String getSentToEmail() {
            return sentToEmail;
        }

        public Date getDate() {
            return Time.toDate(date);
        }

        public String getStatus() {
            return status;
        }
    }
}
