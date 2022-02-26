package eu.locklogin.module.premium.bukkit;

import eu.locklogin.api.module.PluginModule;
import eu.locklogin.api.module.plugin.javamodule.console.MessageLevel;
import eu.locklogin.module.premium.bukkit.commands.OfflineCommand;
import eu.locklogin.module.premium.bukkit.commands.PremiumCommand;
import eu.locklogin.module.premium.bukkit.plib.PLibListener;
import eu.locklogin.module.premium.bukkit.plib.util.BukkitSession;
import ml.karmaconfigs.api.common.karma.loader.component.NameComponent;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.utils.URLUtils;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class Premium extends PluginModule {

    private final static Map<String, BukkitSession> loginSession = new ConcurrentHashMap<>();

    @NotNull
    public static Plugin locklogin = Objects.requireNonNull(Bukkit.getServer().getPluginManager().getPlugin("LockLogin"));

    /**
     * On module enable logic
     */
    @Override
    public void enable() {
        getConsole().sendMessage(MessageLevel.ERROR, "Please note this module is not ready to run on bukkit or any of its forks, " +
                "please do not report this module is not working if you are running on bukkit/spigot/paper or any non-proxy type server, you can help this module to " +
                "be compatible by forking it at https://github.com/KarmaConfigs/LockLoginPremium, thanks and have a nice day!");

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

        getPlugin().registerCommand(new PremiumCommand());
        getPlugin().registerCommand(new OfflineCommand());

        //PLibListener.register();
    }

    /**
     * On module disable logic
     */
    @Override
    public void disable() {
        getConsole().sendMessage("&eLockLoginPremium &7>> &aDisabling LockLogin premium module");

        getPlugin().unregisterListeners();
        getPlugin().unregisterCommands();

        PLibListener.unregister();
    }

    public static BukkitSession getSession(InetSocketAddress addr) {
        String id = getSessionId(addr);
        return loginSession.get(id);
    }

    public static String getSessionId(InetSocketAddress addr) {
        return addr.getAddress().getHostAddress() + ':' + addr.getPort();
    }

    public static void putSession(InetSocketAddress addr, BukkitSession session) {
        String id = getSessionId(addr);
        loginSession.put(id, session);
    }

    public static void removeSession(InetSocketAddress addr) {
        String id = getSessionId(addr);
        loginSession.remove(id);
    }
}
