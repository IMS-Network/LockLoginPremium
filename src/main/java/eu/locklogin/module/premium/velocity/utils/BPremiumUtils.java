package eu.locklogin.module.premium.velocity.utils;

import com.github.games647.craftapi.model.Profile;
import com.github.games647.craftapi.resolver.MojangResolver;
import com.velocitypowered.api.event.connection.PreLoginEvent;

import java.util.Optional;

public class BPremiumUtils {

    private final PreLoginEvent event;

    public BPremiumUtils(PreLoginEvent e) {
        this.event = e;
    }

    public final boolean check() {
        String name = event.getUsername();

        try {
            MojangResolver resolver = new MojangResolver();
            Optional<Profile> tmp_profile = resolver.findProfile(name);
            if (tmp_profile.isPresent()) {
                event.setResult(PreLoginEvent.PreLoginComponentResult.forceOnlineMode());
                return true;
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
