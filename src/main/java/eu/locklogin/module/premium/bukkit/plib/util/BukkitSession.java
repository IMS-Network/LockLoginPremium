package eu.locklogin.module.premium.bukkit.plib.util;

import com.github.games647.craftapi.model.skin.SkinProperty;

import java.util.Optional;

public class BukkitSession extends LoginSession {

    private final byte[] verifyToken;

    private boolean verified;

    private SkinProperty skinProperty;

    public BukkitSession(String username, byte[] verifyToken, boolean registered) {
        super(username, registered);
        this.verifyToken = verifyToken.clone();
    }

    /**
     * Gets the verify token the server sent to the client.
     *
     * Empty if it's a BungeeCord connection
     *
     * @return the verify token from the server
     */
    public synchronized byte[] getVerifyToken() {
        return verifyToken.clone();
    }

    /**
     * @return premium skin if available
     */
    public synchronized Optional<SkinProperty> getSkin() {
        return Optional.ofNullable(skinProperty);
    }

    /**
     * Sets the premium skin property which was retrieved by the session server
     * @param skinProperty premium skin
     */
    public synchronized void setSkinProperty(SkinProperty skinProperty) {
        this.skinProperty = skinProperty;
    }

    /**
     * Sets whether the player has a premium (paid account) account and valid session
     *
     * @param verified whether the player has valid session
     */
    public synchronized void setVerified(boolean verified) {
        this.verified = verified;
    }

    /**
     * Get whether the player has a premium (paid account) account and valid session
     *
     * @return whether the player has a valid session
     */
    public synchronized boolean isVerified() {
        return verified;
    }
}
