package eu.locklogin.module.premium.bukkit.user;

import com.github.games647.craftapi.model.skin.SkinProperty;
import org.bukkit.OfflinePlayer;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Session {

    private final byte[] token;
    private final InetAddress address;
    private final String id;
    private final OfflinePlayer offline;

    private SkinProperty skin;
    private String name;
    private UUID uuid;
    private boolean premium = false;

    private final static Map<String, Session> sessions = new HashMap<>();

    public Session(final OfflinePlayer player, final String server, final byte[] key) {
        offline = player;

        name = player.getName();
        if (!sessions.containsKey(name)) {
            try {
                uuid = player.getUniqueId();
            } catch (Throwable ignored) {}
            if (player.getPlayer() != null && player.getPlayer().getAddress() != null)
                address = player.getPlayer().getAddress().getAddress();
            else
                address = null;

            id = server;
            token = key;

            skin = null;
        } else {
            Session stored = sessions.get(name);
            if (stored.uuid != null)
                uuid = stored.uuid;
            else
                uuid = offline.getUniqueId();

            if (stored.address != null)
                address = stored.address;
            else
                if (player.getPlayer() != null && player.getPlayer().getAddress() != null)
                    address = player.getPlayer().getAddress().getAddress();
                else
                    address = null;

            if (stored.id != null && !stored.id.replaceAll("\\s", "").isEmpty())
                id = stored.id;
            else
                id = server;

            if (stored.token != null && stored.token.length > 0)
                token = stored.token;
            else
                token = key;

            skin = sessions.get(name).skin;
        }

        sessions.put(name, this);
    }

    public final void setName(final String user) {
        name = user;
    }

    public final void updateUUID(final UUID newId) {
        uuid = newId;
    }

    public final void setSkin(final SkinProperty newSkin) {
        skin = newSkin;
    }

    public final void setPremium(final boolean success) {
        premium = success;
    }

    public final OfflinePlayer getPlayer() {
        return offline;
    }

    public final byte[] getToken() {
        return token.clone();
    }

    public final InetAddress getIp() {
        return address;
    }

    public final String getName() {
        return name;
    }

    public final UUID getUUID() {
        return uuid;
    }

    public final SkinProperty getSkin() {
        return skin;
    }

    public final boolean isPremium() {
        return premium;
    }

    public final String getId() {
        return id;
    }
}
