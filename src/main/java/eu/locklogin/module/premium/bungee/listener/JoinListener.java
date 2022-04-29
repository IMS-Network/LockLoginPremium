package eu.locklogin.module.premium.bungee.listener;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import eu.locklogin.api.module.plugin.api.event.ModuleEventHandler;
import eu.locklogin.api.module.plugin.api.event.user.*;
import eu.locklogin.api.module.plugin.api.event.util.EventListener;
import eu.locklogin.api.module.plugin.javamodule.console.MessageLevel;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.util.platform.CurrentPlatform;
import eu.locklogin.module.premium.bungee.utils.BPremiumUtils;
import eu.locklogin.module.premium.bungee.utils.files.Config;
import eu.locklogin.module.premium.bungee.utils.files.Messages;
import eu.locklogin.module.premium.bungee.utils.playerdata.PremiumData;
import ml.karmaconfigs.api.common.utils.enums.Level;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.connection.LoginResult;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static eu.locklogin.module.premium.LockLoginPremium.*;
import static java.lang.invoke.MethodHandles.*;

public class JoinListener implements EventListener {

    private final static MethodHandle uuidHandle;
    private final static LoginResult.Property[] empty = {};

    static {
        MethodHandle handle = null;
        try {
            Lookup lookup = lookup();

            Field uuidField = InitialHandler.class.getDeclaredField("uniqueId");
            uuidField.setAccessible(true);
            handle = lookup.unreflectSetter(uuidField);
            module.getConsole().sendMessage(MessageLevel.INFO, "Unreflected setter for online mode UUID");
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        uuidHandle = handle;
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onJoin(UserPreJoinEvent e) {
        //PluginIpValidationEvent fires the first, is better than using UserPreJoinEvent
        if (mojangActive()) {
            PreLoginEvent eventObj = (PreLoginEvent) e.getEvent();
            PremiumData data = new PremiumData(eventObj.getConnection().getName());

            if (data.isPremium()) {
                BPremiumUtils utils = new BPremiumUtils(eventObj);
                utils.check();
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.FIRST)
    public void onLogin(UserJoinEvent e) {
        LoginEvent login = (LoginEvent) e.getEvent();
        if (login.getConnection().isOnlineMode()) {
            InitialHandler handler = (InitialHandler) login.getConnection();

            Config cfg = new Config();
            UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + login.getConnection().getName()).getBytes(StandardCharsets.UTF_8));
            if (cfg.keepOffline() && !CurrentPlatform.isOnline()) {
                try {
                    uuidHandle.invokeExact(handler, offlineId);
                } catch (Throwable ex) {
                    module.logger().scheduleLog(Level.GRAVE, ex);
                    module.getConsole().sendMessage(MessageLevel.ERROR, "Failed to forward offline UUID for premium user {0}", handler.getName());
                }

                if (cfg.skin()) {
                    if (cfg.debug()) {
                        module.getConsole().sendMessage(MessageLevel.WARNING, "Forwarding original skin of {0}", handler.getName());
                    }
                } else {
                    if (cfg.debug()) {
                        module.getConsole().sendMessage(MessageLevel.WARNING, "Disabling original skin of {0}", handler.getName());
                    }

                    LoginResult result = handler.getLoginProfile();
                    result.setProperties(empty);
                }
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onPostValidation(UserPostJoinEvent e) {
        ModulePlayer player = e.getPlayer();
        Messages msg = new Messages();
        PremiumData data = new PremiumData(player.getName());

        if (mojangActive()) {
            ProxiedPlayer proxied = player.getPlayer();

            if (data.isPremium()) {
                if (!proxied.getPendingConnection().isOnlineMode())
                    return;

                Config cfg = new Config();
                player.requestLogin();

                player.sendMessage(msg.getPrefix() + msg.getAutoLogged());
                if (cfg.debug())
                    module.getConsole().sendMessage(msg.getPrefix() + msg.getLoggedConsole(player));
            }
        } else {
            if (data.isPremium()) {
                player.sendMessage(msg.getPrefix() + msg.mojangDown());
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onAuth(UserAuthenticateEvent e) {
        if (mojangActive()) {
            try {
                if (!e.getAuthType().equals(UserAuthenticateEvent.AuthType.API)) {
                    ModulePlayer player = e.getPlayer();
                    MojangResolver resolver = new MojangResolver();
                    Optional<Profile> profile = resolver.findProfile(player.getName());

                    Messages msg = new Messages();

                    if (e.getAuthResult().equals(UserAuthenticateEvent.Result.SUCCESS)) {
                        PremiumData data = new PremiumData(player.getName());

                        if (!data.isPremium() && profile.isPresent()) {
                            player.sendMessage(msg.getPrefix() + msg.premiumAvailable());
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }
}