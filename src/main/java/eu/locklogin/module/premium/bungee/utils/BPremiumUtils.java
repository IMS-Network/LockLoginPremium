package eu.locklogin.module.premium.bungee.utils;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.connection.InitialHandler;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Optional;

public class BPremiumUtils {

    private final PreLoginEvent event;

    public BPremiumUtils(PreLoginEvent e) {
        this.event = e;
    }

    public void check() {
        PendingConnection connection = event.getConnection();
        String name = connection.getName();

        try {
            MojangResolver resolver = new MojangResolver();
            Optional<Profile> tmp_profile = resolver.findProfile(name);
            if (tmp_profile.isPresent()) {
                event.getConnection().setOnlineMode(true);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }
}
