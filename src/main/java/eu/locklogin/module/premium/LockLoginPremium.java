package eu.locklogin.module.premium;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import eu.locklogin.api.module.PluginModule;
import eu.locklogin.api.module.plugin.javamodule.ModuleLoader;
import ml.karmaconfigs.api.common.utils.string.StringUtils;
import ml.karmaconfigs.api.common.utils.url.HttpUtil;
import ml.karmaconfigs.api.common.utils.url.URLUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface LockLoginPremium {

    @NotNull
    PluginModule module = Objects.requireNonNull(ModuleLoader.getByFile(Objects.requireNonNull(ModuleLoader.getModuleFile("LockLoginPremium"))));

    static boolean mojangActive() {
        try {
            HttpUtil util = URLUtils.extraUtils(URLUtils.getOrNull("https://authserver.mojang.com/"));
            if (util != null) {
                String response = util.getResponse();

                if (!StringUtils.isNullOrEmpty(response)) {
                    Gson gson = new GsonBuilder().setLenient().setPrettyPrinting().create();
                    JsonObject obj = gson.fromJson(response, JsonObject.class);

                    if (obj.has("Status")) {
                        return obj.get("Status").getAsString().equalsIgnoreCase("ok");
                    }
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }
}
