package eu.locklogin.module.premium.bungee.commands;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import eu.locklogin.api.module.plugin.api.command.Command;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.module.plugin.javamodule.sender.ModuleSender;
import eu.locklogin.module.premium.bungee.utils.files.Messages;
import eu.locklogin.module.premium.bungee.utils.playerdata.PremiumData;

import java.util.Optional;

import static eu.locklogin.module.premium.LockLoginPremium.mojangActive;

public class PremiumCommand extends Command {

    public PremiumCommand() {
        super("Toggle premium mode on your account", "premium");
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
            try {
                if (mojangActive()) {
                    PremiumData data = new PremiumData(player.getName());
                    MojangResolver resolver = new MojangResolver();

                    Optional<Profile> profile = resolver.findProfile(player.getName());
                    if (!data.isPremium() && profile.isPresent()) {
                        data.add();
                        player.sendMessage(msg.getPrefix() + msg.getActivated());
                    } else {
                        if (data.isPremium()) {
                            player.sendMessage(msg.getPrefix() + msg.getAlready());
                        } else {
                            player.sendMessage(msg.getPrefix() + msg.getNotPremium());
                        }
                    }
                } else {
                    player.sendMessage(msg.getPrefix() + msg.mojangDown());
                }
            } catch (Throwable ex) {
                player.sendMessage(msg.getPrefix() + msg.getError());
                ex.printStackTrace();
            }
        } else {
            sender.sendMessage("&cThis command is for players only");
        }
    }
}
