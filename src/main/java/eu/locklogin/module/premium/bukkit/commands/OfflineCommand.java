package eu.locklogin.module.premium.bukkit.commands;

import eu.locklogin.api.module.plugin.api.command.Command;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.module.plugin.javamodule.sender.ModuleSender;
import ml.karmaconfigs.api.common.utils.string.StringUtils;

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
        if (sender instanceof ModulePlayer) {
            ModulePlayer player = (ModulePlayer) sender;
            player.sendMessage(StringUtils.toColor("&cThis module is not ready to run on bukkit!"));
        } else {
            module.getConsole().sendMessage("&cThis command is for players only");
        }
    }
}
