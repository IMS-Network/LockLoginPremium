package eu.locklogin.module.premium.bukkit.plib;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.server.TemporaryPlayerFactory;
import com.comphenix.protocol.reflect.FieldUtils;
import com.comphenix.protocol.reflect.FuzzyReflection;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.github.games647.craftapi.model.auth.Verification;
import com.github.games647.craftapi.model.skin.SkinProperty;
import com.github.games647.craftapi.resolver.MojangResolver;
import eu.locklogin.api.module.plugin.javamodule.console.MessageLevel;
import eu.locklogin.module.premium.bukkit.Premium;
import eu.locklogin.module.premium.bukkit.plib.util.BukkitSession;
import eu.locklogin.module.premium.bukkit.plib.util.EncUtil;
import eu.locklogin.module.premium.bukkit.utils.files.Config;
import org.bukkit.entity.Player;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static com.comphenix.protocol.PacketType.Status.Client.START;
import static eu.locklogin.module.premium.bukkit.Premium.locklogin;
import static eu.locklogin.module.premium.LockLoginPremium.*;

public final class VerifyResponse implements Runnable {

    private static final String ENCRYPTION_CLASS_NAME = "MinecraftEncryption";
    private static final Class<?> ENCRYPTION_CLASS;

    static {
        ENCRYPTION_CLASS = MinecraftReflection.getMinecraftClass("util." + ENCRYPTION_CLASS_NAME, ENCRYPTION_CLASS_NAME);
    }

    private final PacketEvent packetEvent;
    private final KeyPair serverKey;

    private final Player player;

    private final byte[] sharedSecret;

    private static Method encryptMethod;
    private static Method cipherMethod;

    public VerifyResponse(PacketEvent packetEvent, Player player, byte[] sharedSecret, KeyPair keyPair) {
        this.packetEvent = packetEvent;
        this.player = player;
        this.sharedSecret = Arrays.copyOf(sharedSecret, sharedSecret.length);
        this.serverKey = keyPair;
    }

    @Override
    public void run() {
        try {
            BukkitSession session = Premium.getSession(player.getAddress());
            if (session == null) {
                player.kickPlayer("Invalid request!");
            } else {
                verifyResponse(session);
            }
        } finally {
            //this is a fake packet; it shouldn't be send to the server
            synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
                packetEvent.setCancelled(true);
            }

            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    private void verifyResponse(BukkitSession session) {
        PrivateKey privateKey = serverKey.getPrivate();

        SecretKey loginKey;
        try {
            loginKey = EncUtil.decryptSharedKey(privateKey, sharedSecret);
        } catch (GeneralSecurityException securityEx) {
            player.kickPlayer("Error: " + securityEx);
            return;
        }

        try {
            if (!checkVerifyToken(session) || !enableEncryption(loginKey)) {
                return;
            }
        } catch (Exception ex) {
            player.kickPlayer("Invalid session! Err: 01");
            return;
        }

        String serverId = EncUtil.getServerIdHashString("", loginKey, serverKey.getPublic());

        String requestedUsername = session.getRequestUsername();
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress != null) {
            try {
                MojangResolver resolver = new MojangResolver();
                InetAddress address = socketAddress.getAddress();
                Optional<Verification> response = resolver.hasJoined(requestedUsername, serverId, address);
                if (response.isPresent()) {
                    Verification verification = response.get();
                    module.getConsole().sendMessage(MessageLevel.INFO, "Player {0} has a verified premium account", verification.getName());
                    String realUsername = verification.getName();
                    if (realUsername == null) {
                        player.kickPlayer("Invalid session! Err: 02");
                        return;
                    }

                    SkinProperty[] properties = verification.getProperties();
                    if (properties.length > 0) {
                        session.setSkinProperty(properties[0]);
                    }

                    session.setVerifiedUsername(realUsername);
                    session.setUuid(verification.getId());
                    session.setVerified(true);

                    setPremiumUUID(session.getUuid());
                    receiveFakeStartPacket(realUsername);
                } else {
                    player.kickPlayer("Invalid session! Err: 03");
                }
            } catch (Throwable ex) {
                ex.printStackTrace();
                player.kickPlayer("Error: " + ex);
            }
        }
    }

    private void setPremiumUUID(UUID premiumUUID) {
        Config config = new Config();

        if (!config.keepOffline() && premiumUUID != null) {
            try {
                Object networkManager = getNetworkManager();
                //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/NetworkManager.java#L69
                FieldUtils.writeField(networkManager, "spoofedUUID", premiumUUID, true);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean checkVerifyToken(BukkitSession session) throws GeneralSecurityException {
        byte[] requestVerify = session.getVerifyToken();
        byte[] responseVerify = packetEvent.getPacket().getByteArrays().read(1);

        if (!Arrays.equals(requestVerify, EncUtil.decrypt(serverKey.getPrivate(), responseVerify))) {
            player.kickPlayer("Error: Invalid verify token");
            return false;
        }

        return true;
    }

    //try to get the networkManager from ProtocolLib
    private Object getNetworkManager() throws IllegalAccessException, ClassNotFoundException {
        Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(player);

        // ChannelInjector
        Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
        Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);
        return FieldUtils.readField(rawInjector, "networkManager", true);
    }

    private boolean enableEncryption(SecretKey loginKey) throws IllegalArgumentException {
        // Initialize method reflections
        if (encryptMethod == null) {
            Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();

            try {
                // Try to get the old (pre MC 1.16.4) encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", SecretKey.class);
            } catch (IllegalArgumentException exception) {
                // Get the new encryption method
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", Cipher.class, Cipher.class);

                // Get the needed Cipher helper method (used to generate ciphers from login key)
                cipherMethod = FuzzyReflection.fromClass(ENCRYPTION_CLASS)
                        .getMethodByParameters("a", int.class, Key.class);
            }
        }

        try {
            Object networkManager = this.getNetworkManager();

            // If cipherMethod is null - use old encryption (pre MC 1.16.4), otherwise use the new cipher one
            if (cipherMethod == null) {
                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, loginKey);
            } else {
                // Create ciphers from login key
                Object decryptionCipher = cipherMethod.invoke(null, Cipher.DECRYPT_MODE, loginKey);
                Object encryptionCipher = cipherMethod.invoke(null, Cipher.ENCRYPT_MODE, loginKey);

                // Encrypt/decrypt packet flow, this behaviour is expected by the client
                encryptMethod.invoke(networkManager, decryptionCipher, encryptionCipher);
            }
        } catch (Throwable ex) {
            player.kickPlayer("Error: Couldn't toggle encryption");
            return false;
        }

        return true;
    }

    //fake a new login packet in order to let the server handle all the other stuff
    private void receiveFakeStartPacket(String username) {
        //see StartPacketListener for packet information
        PacketContainer startPacket = new PacketContainer(START);

        //uuid is ignored by the packet definition
        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), username);
        startPacket.getGameProfiles().write(0, fakeProfile);
        try {
            //we don't want to handle our own packets so ignore filters
            startPacket.setMeta("source", locklogin.getName());
            ProtocolLibrary.getProtocolManager().recieveClientPacket(player, startPacket, true);
        } catch (Throwable ex) {
            ex.printStackTrace();
            player.kickPlayer("Unknown error!");
        }
    }
}
