package eu.locklogin.module.premium.bukkit.plib.util;

import eu.locklogin.module.premium.bukkit.utils.playerdata.PremiumData;

import java.util.UUID;

public abstract class LoginSession {

    private final String requestUsername;
    private String username;
    private UUID uuid;

    protected boolean registered;

    public LoginSession(String requestUsername, boolean registered) {
        this.requestUsername = requestUsername;
        this.username = requestUsername;

        this.registered = registered;
    }

    public String getRequestUsername() {
        return requestUsername;
    }

    public String getUsername() {
        return username;
    }

    public synchronized void setVerifiedUsername(String username) {
        this.username = username;
    }

    /**
     * @return This value is always false if we authenticate the player with a cracked authentication
     */
    public synchronized boolean needsRegistration() {
        return !registered;
    }

    public boolean isPremium() {
        return new PremiumData(getUsername()).isPremium();
    }

    /**
     * Get the premium UUID of this player
     *
     * @return the premium UUID or null if not fetched
     */
    public synchronized UUID getUuid() {
        return uuid;
    }

    /**
     * Set the online UUID if it's fetched
     *
     * @param uuid premium UUID
     */
    public synchronized void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
