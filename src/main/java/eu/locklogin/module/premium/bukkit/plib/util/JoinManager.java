package eu.locklogin.module.premium.bukkit.plib.util;

import eu.locklogin.module.premium.bukkit.utils.playerdata.PremiumData;

import static eu.locklogin.module.premium.LockLoginPremium.*;

public abstract class JoinManager<P extends C, C, S extends LoginSource> {

    public void onLogin(String username, S source) {
        PremiumData data = new PremiumData(username);

        if (data.isPremium()) {
            module.getConsole().sendMessage("&aClient {0} has passed premium check and his connection is now in online mode", username);
            requestPremiumLogin(source, username, true);
        } else {
            startCrackedSession(source, username);
        }
    }

    public abstract void requestPremiumLogin(S source, String username, boolean registered);

    public abstract void startCrackedSession(S source, String username);
}
