package eu.locklogin.module.premium.bukkit.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import eu.locklogin.module.premium.bukkit.user.Session;
import org.bukkit.entity.Player;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Random;

import static com.comphenix.protocol.PacketType.Login.Client.ENCRYPTION_BEGIN;

public class NameCheck implements Runnable {

    private final PacketEvent packetEvent;
    private final PublicKey publicKey;

    private final Random random;

    private final Player player;
    private final String username;

    public NameCheck(PacketEvent packetEvent, Random random, Player player, String username, PublicKey publicKey) {
        this.packetEvent = packetEvent;
        this.publicKey = publicKey;
        this.random = random;
        this.player = player;
        this.username = username;
    }

    @Override
    public final void run() {
        assert player.getAddress() != null;

        try {
            try {
                byte[] verifyToken = KeyGen.generateVerifyToken(random);

                System.out.println(Arrays.toString(verifyToken));

                PacketContainer newPacket = new PacketContainer(ENCRYPTION_BEGIN);

                System.out.println("New packet is: " + newPacket);

                StructureModifier<PublicKey> keyModifier = newPacket.getSpecificModifier(PublicKey.class);
                int verifyField = 0;
                if (keyModifier.getFields().isEmpty()) {
                    System.out.println("Modifiers empty");

                    newPacket.getByteArrays().write(0, publicKey.getEncoded());
                    verifyField++;
                } else {
                    System.out.println("Not empty");

                    keyModifier.write(0, publicKey);
                }
                new Session(player, "", verifyToken);
                System.out.println("Built session");

                newPacket.getByteArrays().write(verifyField, verifyToken);

                System.out.println("Preparing packet");

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, newPacket);

                System.out.println("Packet sent!");
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            /*
            synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
                System.out.println("Packet cancelled");

                packetEvent.setCancelled(true);
            }*/
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
