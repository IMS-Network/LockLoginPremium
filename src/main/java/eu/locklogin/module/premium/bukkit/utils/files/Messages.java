package eu.locklogin.module.premium.bukkit.utils.files;

import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.util.platform.CurrentPlatform;
import eu.locklogin.module.premium.bukkit.Premium;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.karmafile.karmayaml.KarmaYamlManager;

import java.io.File;

import static eu.locklogin.module.premium.LockLoginPremium.module;

public class Messages {

    private static KarmaYamlManager messages;

    public Messages() {
        try {
            File file = module.getFile("messages.yml");
            if (!file.exists()) {
                FileCopy copy = new FileCopy(Premium.class, "messages.yml");
                copy.copy(file);
            }

            messages = new KarmaYamlManager(file);
        } catch (Throwable ignored) {
        }
    }

    public final String getPrefix() {
        return messages.getString("Prefix", "&7( &ePremium &7) ");
    }

    public final String getActivated() {
        return messages.getString("Activated", "&aPremium mode activated");
    }

    public final String getDeActivated() {
        return messages.getString("DeActivated", "&aPremium mode disabled");
    }

    public final String getAlready() {
        return messages.getString("Already", "&cYou're already premium");
    }

    public final String getNotPremium() {
        return messages.getString("NotPremium", "&cYour account is cracked");
    }

    public final String getError() {
        return messages.getString("Error", "&4Unknown error occurred, contact server admins.");
    }

    public final String premiumAvailable() {
        return messages.getString("PremiumAvailable", "&bType &9&o{command}premium &bto enable premium's autologin").replace("{command}", CurrentPlatform.getPrefix());
    }

    public final String getAutoLogged() {
        return messages.getString("AutoLogged", "&aYou've been auto-logged");
    }

    public final String getLoggedConsole(final ModulePlayer player) {
        String value = messages.getString("AutoLoggedConsole", "&eThe player &6{player}&e has been auto-logged");
        assert value != null;

        return value.replace("{player}", player.getName());
    }
}
