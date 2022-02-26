package eu.locklogin.module.premium.bukkit.plib.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import org.bukkit.entity.Player;

import java.net.InetSocketAddress;
import java.security.PublicKey;
import java.util.Random;

import static com.comphenix.protocol.PacketType.Login.Client.ENCRYPTION_BEGIN;

public final class LoginSource {

    private final Player player;

    private final Random random;
    private final PublicKey publicKey;

    private final String serverId = "";
    private byte[] verifyToken;

    public LoginSource(Player player, Random random, PublicKey publicKey) {
        this.player = player;
        this.random = random;
        this.publicKey = publicKey;
        verifyToken = EncUtil.generateVerifyToken(random);
    }

    public void enableOnlineMode() {
        try {
            /*
             * Packet Information: https://wiki.vg/Protocol#Encryption_Request
             *
             * ServerID="" (String) key=public server key verifyToken=random 4 byte array
             */
            PacketContainer newPacket = new PacketContainer(ENCRYPTION_BEGIN);

            try {
                newPacket.getStrings().write(0, "");
            } catch (Throwable ex) {
                newPacket.getByteArrays().write(0, new byte[0]);
            }
            StructureModifier<PublicKey> keyModifier = newPacket.getSpecificModifier(PublicKey.class);
            int verifyField = 0;
            if (keyModifier.getFields().isEmpty()) {
                // Since 1.16.4 this is now a byte field
                newPacket.getByteArrays().write(0, publicKey.getEncoded());
                verifyField++;
            } else {
                keyModifier.write(0, publicKey);
            }

            newPacket.getByteArrays().write(verifyField, verifyToken);

            //serverId is a empty string
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, newPacket);
            } catch (Throwable ex) {
                ProtocolLibrary.getProtocolManager().recieveClientPacket(player, newPacket);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public InetSocketAddress getAddress() {
        return player.getAddress();
    }

    public String getServerId() {
        return serverId;
    }

    public byte[] getVerifyToken() {
        return verifyToken.clone();
    }
}
