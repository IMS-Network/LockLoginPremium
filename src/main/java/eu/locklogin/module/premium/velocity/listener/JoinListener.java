package eu.locklogin.module.premium.velocity.listener;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.GameProfileRequestEvent;
import com.velocitypowered.api.proxy.Player;
import eu.locklogin.api.module.plugin.api.event.ModuleEventHandler;
import eu.locklogin.api.module.plugin.api.event.plugin.PluginIpValidationEvent;
import eu.locklogin.api.module.plugin.api.event.user.*;
import eu.locklogin.api.module.plugin.api.event.util.EventListener;
import eu.locklogin.api.module.plugin.javamodule.sender.ModulePlayer;
import eu.locklogin.module.premium.velocity.utils.BPremiumUtils;
import eu.locklogin.module.premium.velocity.utils.files.Config;
import eu.locklogin.module.premium.velocity.utils.files.Messages;
import eu.locklogin.module.premium.velocity.utils.playerdata.PremiumData;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static eu.locklogin.module.premium.LockLoginPremium.*;

public class JoinListener implements EventListener {

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onGameProfile(VelocityGameProfileEvent event) {
        if (mojangActive()) {
            GameProfileRequestEvent e = (GameProfileRequestEvent) event.getEvent();

            Config cfg = new Config();
            PremiumData data = new PremiumData(e.getOriginalProfile().getName());
            if (e.isOnlineMode() && cfg.keepOffline() && data.isPremium()) {
                e.setGameProfile(e.getGameProfile().withId(UUID.nameUUIDFromBytes(("OfflinePlayer:" + e.getOriginalProfile().getName()).getBytes(StandardCharsets.UTF_8))));
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onJoin(PluginIpValidationEvent e){
        if (mojangActive()) {
            PreLoginEvent eventObj = (PreLoginEvent) e.getEvent();

            PremiumData data = new PremiumData(eventObj.getUsername());

            if (data.isPremium()) {
                BPremiumUtils utils = new BPremiumUtils(eventObj);
                if (utils.check()) {
                    module.getConsole().sendMessage("&aClient {0} has passed premium check and his connection is now in online mode", eventObj.getUsername());
                }
            }
        }
    }

    @ModuleEventHandler(priority = ModuleEventHandler.Priority.LAST)
    public void onPostLogin(UserPostValidationEvent e) {
        Messages msg = new Messages();
        ModulePlayer player = e.getPlayer();
        PremiumData data = new PremiumData(((Player) player.getPlayer()).getGameProfile().getName());

        if (mojangActive()) {
            Player proxied = player.getPlayer();

            if (data.isPremium()) {
                if (!proxied.isOnlineMode())
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
                    Optional<Profile> profile = resolver.findProfile(((Player) player.getPlayer()).getGameProfile().getName());

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