package eu.locklogin.module.premium.bukkit.utils.files;

import eu.locklogin.module.premium.bukkit.Premium;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.karmafile.karmayaml.KarmaYamlManager;

import java.io.File;

import static eu.locklogin.module.premium.LockLoginPremium.module;

public final class Config {

    private static KarmaYamlManager cfg;

    public Config() {
        try {
            File config = module.getFile("config.yml");
            if (!config.exists()) {
                FileCopy copy = new FileCopy(Premium.class, "config.yml");
                copy.copy(config);
            }

            cfg = new KarmaYamlManager(config);
        } catch (Throwable ignored) {}
    }

    public final boolean debug() {
        return cfg.getBoolean("Debug", true);
    }

    public final boolean keepOffline() {
        return cfg.getBoolean("KeepOffline", true);
    }
}
