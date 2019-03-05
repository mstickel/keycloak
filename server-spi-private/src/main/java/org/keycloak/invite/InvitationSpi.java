package org.keycloak.invite;

import org.keycloak.provider.Provider;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.provider.Spi;

public class InvitationSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "invitation";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return InvitationProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return InvitationProviderFactory.class;
    }
}
