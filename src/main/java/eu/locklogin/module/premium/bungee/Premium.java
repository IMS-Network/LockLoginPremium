package eu.locklogin.module.premium.bungee;

import eu.locklogin.api.module.PluginModule;
import eu.locklogin.api.module.plugin.javamodule.console.MessageLevel;
import eu.locklogin.api.util.platform.CurrentPlatform;
import eu.locklogin.module.premium.bungee.commands.OfflineCommand;
import eu.locklogin.module.premium.bungee.commands.PremiumCommand;
import eu.locklogin.module.premium.bungee.listener.JoinListener;
import eu.locklogin.module.premium.bungee.utils.playerdata.PremiumData;
import ml.karmaconfigs.api.common.karma.loader.component.NameComponent;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.utils.URLUtils;

import java.io.File;

public class Premium extends PluginModule {

    /**
     * On module enable logic
     */
    @Override
    public void enable() {
        getConsole().sendMessage(MessageLevel.INFO, "&aEnabling LockLogin premium module");

        try {
            getAppender().downloadAndInject(
                    URLUtils.getOrNull("https://repo.codemc.io/repository/maven-public/com/github/games647/craftapi/0.5-SNAPSHOT/craftapi-0.5-20210414.163533-1.jar"),
                    NameComponent.forFile("CraftAPI", "jar", "LockLogin", "PremiumModule"));

            getAppender().downloadAndInject(
                    URLUtils.getOrNull("https://repo1.maven.org/maven2/javax/json/javax.json-api/1.1.4/javax.json-api-1.1.4.jar"),
                    NameComponent.forFile("JsonAPI", "jar", "LockLogin", "PremiumModule"));

            getAppender().downloadAndInject(
                    URLUtils.getOrNull("https://repo1.maven.org/maven2/org/glassfish/javax.json/1.1.4/javax.json-1.1.4.jar"),
                    NameComponent.forFile("JavaxJson", "jar", "LockLogin", "PremiumModule"));

            File messages = getFile("messages.yml");
            File config = getFile("config.yml");

            FileCopy copy_cfg = new FileCopy(this, "config.yml");
            FileCopy copy_msg = new FileCopy(this, "messages.yml");

            copy_cfg.copy(config);
            copy_msg.copy(messages);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        try {
            PremiumData.migrateV1();
        } catch (Throwable ex) {
            getConsole().sendMessage(MessageLevel.ERROR, "Failed to migrate from LockLoginPremium v1 data base");
        }

        getConsole().sendMessage("&7Registering commands ( Module commands starts with " + CurrentPlatform.getPrefix() + " )");
        getPlugin().registerCommand(new PremiumCommand());
        getPlugin().registerCommand(new OfflineCommand());

        getConsole().sendMessage("&7Registering listeners");
        getPlugin().registerListener(new JoinListener());
    }

    /**
     * On module disable logic
     */
    @Override
    public void disable() {
        getConsole().sendMessage("&eLockLoginPremium &7>> &aDisabling LockLogin premium module");

        getPlugin().unregisterCommands();
        getPlugin().unregisterListeners();
    }
}
