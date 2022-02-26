package eu.locklogin.module.premium.bungee.listener;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import eu.locklogin.api.module.plugin.api.event.ModuleEventHandler;
import eu.locklogin.api.module.plugin.api.event.user.UserAuthenticateEvent;
import eu.locklogin.api.module.plugin.api.event.user.UserPostValidationEvent;
import eu.locklogin.api.module.plugin.api.event.user.UserPreJoinEvent;
import eu.locklogin.api.module.plugin.api.event.util.EventListener;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.api.util.platform.CurrentPlatform;
import eu.locklogin.module.premium.LockLoginPremium;
import eu.locklogin.module.premium.bungee.utils.BPremiumUtils;
import eu.locklogin.module.premium.bungee.utils.files.Config;
import eu.locklogin.module.premium.bungee.utils.files.Messages;
import eu.locklogin.module.premium.bungee.utils.playerdata.PremiumData;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static eu.locklogin.module.premium.LockLoginPremium.*;

public class JoinListener implements EventListener {

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onJoin(UserPreJoinEvent e) {
        if (mojangActive()) {
            PremiumData data = new PremiumData(e.getName());

            PreLoginEvent eventObj = (PreLoginEvent) e.getEvent();

            if (data.isPremium()) {
                BPremiumUtils utils = new BPremiumUtils(eventObj);
                utils.check();

                Config cfg = new Config();
                UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getName()).getBytes(StandardCharsets.UTF_8));
                if (cfg.keepOffline() && !CurrentPlatform.isOnline()) {
                    try {
                        utils.setUniqueId().invokeExact((InitialHandler) eventObj.getConnection(), offlineId);
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onPostLogin(UserPostValidationEvent e) {
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