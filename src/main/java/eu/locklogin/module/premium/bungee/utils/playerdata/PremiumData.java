package eu.locklogin.module.premium.bungee.utils.playerdata;

import ml.karmaconfigs.api.common.karmafile.KarmaFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import static eu.locklogin.module.premium.LockLoginPremium.module;

public final class PremiumData {

    private final String data;
    private final KarmaFile file;

    public PremiumData(final String name) {
        data = name;

        file = new KarmaFile(module.getFile("premiums_v2.gsa"));
        if (!file.exists())
            file.create();
    }

    public static void migrateV1() {
        KarmaFile file = new KarmaFile(module.getFile("premiums_v2.gsa"));

        if (!file.exists())
            file.create();

        File old = new File(module.getDataFolder(), "premiums.gsa");

        if (old.exists()) {
            module.getConsole().sendMessage("Migrating from v1 premium data");

            try {
                List<String> users = Files.readAllLines(old.toPath());

                file.set("USERS", users);
            } catch (Throwable ex) {
                ex.printStackTrace();
                module.getConsole().sendMessage("&cFailed to migrate from v1 premium data, THERE MAY BE DATA LOST!");
            } finally {
                try {
                    Files.delete(old.toPath());
                } catch (Throwable ignored) {}
            }
        }
    }

    public final void add() {
        List<String> users = file.getStringList("USERS");
        if (users.stream().noneMatch(data::contains)) {
            users.add(data);
            file.set("USERS", users);
        }
    }

    public final void remove() {
        List<String> users = file.getStringList("USERS");
        if (users.stream().anyMatch(data::contains)) {
            users.remove(data);
            file.set("USERS", users);
        }
    }

    public final boolean isPremium() {
        return file.getStringList("USERS").stream().anyMatch(data::contains);
    }
}
