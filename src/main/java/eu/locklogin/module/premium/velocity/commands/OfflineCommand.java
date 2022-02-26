package eu.locklogin.module.premium.velocity.commands;

import com.velocitypowered.api.proxy.Player;
import eu.locklogin.api.module.plugin.api.command.Command;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.module.plugin.javamodule.sender.ModuleSender;
import eu.locklogin.module.premium.velocity.utils.files.Messages;
import eu.locklogin.module.premium.velocity.utils.playerdata.PremiumData;

import static eu.locklogin.module.premium.LockLoginPremium.module;

public class OfflineCommand extends Command {

    public OfflineCommand() {
        super("Toggle offline mode on your account", "offline");
    }

    /**
     * Process the command when
     * its fired
     *
     * @param arg        the used argument
     * @param sender     the command sender
     * @param parameters the command parameters
     */
    @Override
    public void processCommand(String arg, ModuleSender sender, String... parameters) {
        Messages msg = new Messages();

        if (sender instanceof ModulePlayer) {
            ModulePlayer player = (ModulePlayer) sender;
            Player velocityPlayer = player.getPlayer();

            try {
                PremiumData data = new PremiumData(velocityPlayer.getGameProfile().getName());

                if (data.isPremium()) {
                    data.remove();
                    player.sendMessage(msg.getPrefix() + msg.getDeActivated());
                } else {
                    player.sendMessage(msg.getPrefix() + msg.getNotPremium());
                }
            } catch (Throwable ex) {
                player.sendMessage(msg.getPrefix() + msg.getError());
            }
        } else {
            module.getConsole().sendMessage("&cThis command is for players only");
        }
    }
}
