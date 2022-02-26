package eu.locklogin.module.premium.bukkit.plib.util;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import eu.locklogin.module.premium.bukkit.Premium;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.security.PublicKey;
import java.util.Random;

public class NameCheck extends JoinManager<Player, CommandSender, LoginSource> implements Runnable {

    private final PacketEvent packetEvent;
    private final PublicKey publicKey;

    private final Random random;

    private final Player player;
    private final String username;

    public NameCheck(Random random, Player player, PacketEvent packetEvent, String username, PublicKey publicKey) {
        this.packetEvent = packetEvent;
        this.publicKey = publicKey;
        this.random = random;
        this.player = player;
        this.username = username;
    }

    @Override
    public void run() {
        try {
            super.onLogin(username, new LoginSource(player, random, publicKey));
        } finally {
            ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(packetEvent);
        }
    }

    //Minecraft server implementation
    //https://github.com/bergerkiller/CraftSource/blob/master/net.minecraft.server/LoginListener.java#L161
    @Override
    public void requestPremiumLogin(LoginSource source, String username, boolean registered) {
        try {
            source.enableOnlineMode();
        } catch (Throwable ex) {
            ex.printStackTrace();
            return;
        }

        byte[] verify = source.getVerifyToken();

        BukkitSession playerSession = new BukkitSession(username, verify, registered);
        Premium.putSession(player.getAddress(), playerSession);
        //cancel only if the player has a paid account otherwise login as normal offline player
        synchronized (packetEvent.getAsyncMarker().getProcessingLock()) {
            packetEvent.setCancelled(true);
        }
    }

    @Override
    public void startCrackedSession(LoginSource source, String username) {
        BukkitSession loginSession = new BukkitSession(username, source.getVerifyToken(), true);
        Premium.putSession(player.getAddress(), loginSession);
    }
}
