package ninja.leaping.landiscovery;

import com.google.inject.Inject;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.event.Subscribe;

/**
 * A simple sponge plugin
 */
@Plugin(id = PomData.ARTIFACT_ID, name = PomData.NAME, version = PomData.VERSION)
public class MyPlugin {

    // These are all injected on plugin load for users to work from
    @Inject private Logger logger;
    // Give us a configuration to work from
    @Inject @DefaultConfig(sharedRoot = true) private ConfigurationLoader<CommentedConfigurationNode> configLoader;
    @Inject private Game game;

    @Subscribe
    public void onPreInit(PreInitializationEvent event) {
        // Perform initialization tasks here
    }

    @Subscribe
    public void disable(ServerStoppingEvent event) {
        // Perform shutdown tasks here
    }
}
