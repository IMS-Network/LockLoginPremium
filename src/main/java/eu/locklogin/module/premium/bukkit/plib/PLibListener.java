package eu.locklogin.module.premium.bukkit.plib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import eu.locklogin.api.module.plugin.javamodule.console.MessageLevel;
import eu.locklogin.module.premium.bukkit.Premium;
import eu.locklogin.module.premium.bukkit.plib.util.EncUtil;
import eu.locklogin.module.premium.bukkit.plib.util.NameCheck;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.SecureRandom;

import static eu.locklogin.module.premium.LockLoginPremium.*;
import static com.comphenix.protocol.PacketType.Login.Client.*;

public final class PLibListener extends PacketAdapter {

    private static PLibListener listener = null;

    private final SecureRandom random = new SecureRandom();
    private final KeyPair keyPair = EncUtil.generateKeyPair();

    PLibListener() {
        super(params()
                .plugin(Premium.locklogin)
                .types(START, ENCRYPTION_BEGIN)
                .optionAsync());
    }

    public static void register() {
        if (listener == null) {
            listener = new PLibListener();

            ProtocolLibrary.getProtocolManager()
                    .getAsynchronousManager()
                    .registerAsyncHandler(listener)
                    .start();
        }
    }

    public static void unregister() {
        if (listener != null) {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().unregisterAsyncHandler(listener);
        }
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        if (!event.isCancelled()) {
            Player sender = event.getPlayer();
            PacketType type = event.getPacketType();
            if (type == START) {
                InetSocketAddress address = sender.getAddress();
                if (address != null) {
                    PacketContainer container = event.getPacket();

                    String userName = container.getGameProfiles().read(0).getName();
                    event.getAsyncMarker().incrementProcessingDelay();
                    if (keyPair != null) {
                        Runnable nameCheck = new NameCheck(random, sender, event, userName, keyPair.getPublic());
                        module.async().queue(nameCheck);
                    } else {
                        module.getConsole().sendMessage(MessageLevel.ERROR, "Couldn't validate name of {0} because the public key is null", userName);
                    }
                } else {
                    module.getConsole().sendMessage(MessageLevel.ERROR, "Couldn't initialize session key for player {0}", sender.getUniqueId().toString());
                }
            } else {
                byte[] sharedSecret = event.getPacket().getByteArrays().read(0);

                event.getAsyncMarker().incrementProcessingDelay();
                Runnable verifyTask = new VerifyResponse(event, sender, sharedSecret, keyPair);
                module.async().queue(verifyTask);
            }
        }
    }
}
