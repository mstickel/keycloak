package org.keycloak.models.jpa;

import org.keycloak.invite.InvitationProvider;
import org.keycloak.models.InvitationModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.InvitationEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.models.utils.KeycloakModelUtils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;
import java.util.stream.Collectors;

public class JpaInvitationProvider implements InvitationProvider {

    private final EntityManager entityManager;
    private final KeycloakSession session;

    public JpaInvitationProvider(EntityManager entityManager, KeycloakSession session) {
        this.entityManager = entityManager;
        this.session = session;
    }

    @Override
    public InvitationModel create(RealmModel realm, String id, UserModel inviter, String email, String invitationToken,
                                  Long dateSent) {
        if (id == null) id = KeycloakModelUtils.generateId();
        InvitationEntity invitationEntity = new InvitationEntity();
        invitationEntity.setId(id);
        invitationEntity.setRealmId(realm.getId());
        invitationEntity.setInviter(entityManager.getReference(UserEntity.class, inviter.getId()));
        invitationEntity.setInviteeEmail(email);
        invitationEntity.setInvitationToken(invitationToken);
        invitationEntity.setUsed(false);
        invitationEntity.setDateSentTimestamp(dateSent);

        this.entityManager.persist(invitationEntity);
        this.entityManager.flush();

        return new InvitationAdapter(realm, entityManager, invitationEntity, session);
    }

    @Override
    public List<InvitationModel> findForUser(RealmModel realm, UserModel user) {
        List<InvitationEntity> invitations = entityManager.createNamedQuery("findInvitesSentByUser", InvitationEntity.class)
            .setParameter("id", user.getId())
            .getResultList();
        return invitations.stream().map(ie -> new InvitationAdapter(realm, entityManager, ie, session)).collect(Collectors.toList());
    }

    @Override
    public InvitationModel findByToken(String invitationToken) {
        try {
            InvitationEntity entity = this.entityManager.createNamedQuery("findInvitationByToken", InvitationEntity.class)
                    .setParameter("token", invitationToken)
                    .getSingleResult();
            RealmModel realm = session.realms().getRealm(entity.getRealmId());
            return new InvitationAdapter(realm, entityManager, entity, session);
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public boolean markUsed(String invitationToken) {
        try {
            InvitationEntity entity = this.entityManager.createNamedQuery("findInvitationByToken", InvitationEntity.class)
                    .setParameter("token", invitationToken)
                    .getSingleResult();
            if (!entity.isUsed()) {
                entity.setUsed(true);
                this.entityManager.persist(entity);
                this.entityManager.flush();
                return true;
            } else {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        }
    }

    @Override
    public void close() {

    }
}
