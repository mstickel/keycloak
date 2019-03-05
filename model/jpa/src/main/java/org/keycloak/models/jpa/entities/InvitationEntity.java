package org.keycloak.models.jpa.entities;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author Mark Stickel
 */
@Entity
@Table(name="INVITATION_ENTITY", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "USER_ID", "INVITEE_EMAIL" })
})
@NamedQueries({
        @NamedQuery(name="findInvitationByToken", query="select invitation from InvitationEntity invitation where invitation.invitationToken = :token"),
        @NamedQuery(name="findInvitesSentByUser", query="select invitation from InvitationEntity invitation where invitation.inviter.id = :id")
})
public class InvitationEntity {
    @Id
    @Column(name="ID", length = 36)
    @Access(AccessType.PROPERTY) // we do this because relationships often fetch id, but not entity.  This avoids an extra SQL
    protected String id;
    @Column(name = "REALM_ID")
    protected String realmId;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name="USER_ID")
    protected UserEntity inviter;
    @Column(name = "INVITEE_EMAIL")
    protected String inviteeEmail;
    @Column(name = "INVITATION_TOKEN")
    protected String invitationToken;
    @Column(name = "DATE_SENT")
    protected long dateSentTimestamp;
    @Column(name = "USED")
    protected boolean used;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public UserEntity getInviter() {
        return inviter;
    }

    public void setInviter(UserEntity inviter) {
        this.inviter = inviter;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }

    public long getDateSentTimestamp() {
        return dateSentTimestamp;
    }

    public void setDateSentTimestamp(long dateSentTimestamp) {
        this.dateSentTimestamp = dateSentTimestamp;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvitationEntity that = (InvitationEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
