package io.github.townyadvanced.flagwar.battle_tracking.util;

import org.flintstqne.adminCore.identity.IdentityApi;

/**
 * Resolves names through AdminCore's public identity API.
 */
public final class IdentityNameResolver {

    private final IdentityApi identityApi;

    public IdentityNameResolver(IdentityApi identityApi) {
        this.identityApi = identityApi;
    }

    /**
     * Returns the public identity for a known backend name, or the supplied
     * name unchanged when AdminCore's identity API is unavailable.
     */
    public String publicName(String backendName) {
        if (backendName == null || backendName.isBlank()) return backendName;

        if (identityApi == null || !identityApi.isEnabled()) return backendName;
        String publicName = identityApi.publicName(backendName);
        return publicName == null || publicName.isBlank() ? backendName : publicName;
    }
}
