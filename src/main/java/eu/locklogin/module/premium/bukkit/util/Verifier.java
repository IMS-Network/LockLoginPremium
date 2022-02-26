package eu.locklogin.module.premium.bukkit.util;

import com.comphenix.protocol.PacketType;
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
import eu.locklogin.module.premium.bukkit.user.Session;
import org.bukkit.entity.Player;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

public final class Verifier implements Runnable {

    private final PacketEvent event;
    private final KeyPair serverKey;
    private final Session session;
    private final byte[] sharedKey;

    private static Method encryptMethod;
    private static Method cipherMethod;

    public Verifier(final PacketEvent owner, final KeyPair key, final Session user, final byte[] shared) {
        event = owner;
        serverKey = key;
        session = user;
        sharedKey = shared;
    }

    @Override
    public final void run() {
        if (session == null)
            return;

        PrivateKey secret = serverKey.getPrivate();
        SecretKey login = null;

        try {
            login = KeyGen.decryptSharedKey(secret, sharedKey);
        } catch (Throwable ignored) {}
        if (login == null)
            return;

        if (isTokenValid() && enableEncryption(login)) {
            String serverId = KeyGen.getServerIdHashString("", login, serverKey.getPublic());

            String name = session.getName();
            InetAddress address = session.getIp();
            try {
                MojangResolver resolver = new MojangResolver();
                Optional<Verification> response = resolver.hasJoined(name, serverId, address);
                if (response.isPresent()) {
                    Verification verification = response.get();
                    name = verification.getName();
                    if (name != null) {
                        session.setName(name);

                        SkinProperty[] properties = verification.getProperties();
                        if (properties.length > 0) {
                            session.setSkin(properties[0]);
                        }

                        session.updateUUID(verification.getId());
                        session.setPremium(true);

                        try {
                            Object networkManager = getNetworkManager();
                            if (networkManager != null) {
                                //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/NetworkManager.java#L69
                                FieldUtils.writeField(networkManager, "spoofedUUID", verification.getId(), true);
                            }
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }

                        PacketContainer startPacket = new PacketContainer(PacketType.Login.Client.START);

                        WrappedGameProfile fakeProfile = new WrappedGameProfile(UUID.randomUUID(), name);
                        startPacket.getGameProfiles().write(0, fakeProfile);
                        try {
                            ProtocolLibrary.getProtocolManager().recieveClientPacket(session.getPlayer().getPlayer(), startPacket, false);
                        } catch (Throwable ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
    }

    private boolean enableEncryption(SecretKey loginKey) throws IllegalArgumentException {
        if (encryptMethod == null) {
            Class<?> networkManagerClass = MinecraftReflection.getNetworkManagerClass();

            try {
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", SecretKey.class);
            } catch (IllegalArgumentException exception) {
                encryptMethod = FuzzyReflection.fromClass(networkManagerClass)
                        .getMethodByParameters("a", Cipher.class, Cipher.class);

                Class<?> encryptionClass = MinecraftReflection.getMinecraftClass("MinecraftEncryption");
                cipherMethod = FuzzyReflection.fromClass(encryptionClass)
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
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    private boolean isTokenValid() {
        try {
            byte[] token = session.getToken();
            byte[] eventToken = event.getPacket().getByteArrays().read(1);

            return Arrays.equals(token, KeyGen.decrypt(serverKey.getPrivate(), eventToken));
        } catch (Throwable ex) {
            return false;
        }
    }

    private Object getNetworkManager() {
        Player player = session.getPlayer().getPlayer();
        try {
            if (player != null) {
                Object injectorContainer = TemporaryPlayerFactory.getInjectorFromPlayer(player);

                //ChannelInjector
                Class<?> injectorClass = Class.forName("com.comphenix.protocol.injector.netty.Injector");
                Object rawInjector = FuzzyReflection.getFieldValue(injectorContainer, injectorClass, true);
                return FieldUtils.readField(rawInjector, "networkManager", true);
            }
        } catch (Throwable ignored) {}

        return null;
    }

}
