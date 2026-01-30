package dev.hytalemodding;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.hytalemodding.commands.CustomSpawnSuppressionCommand;
import dev.hytalemodding.events.OnBreakBlock;
import dev.hytalemodding.events.OnPlaceBlock;

import javax.annotation.Nonnull;

public class SafeCampfire extends JavaPlugin {

    public SafeCampfire(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        this.getCommandRegistry().registerCommand(new CustomSpawnSuppressionCommand());
        this.getEntityStoreRegistry().registerSystem(new OnPlaceBlock());
        this.getEntityStoreRegistry().registerSystem(new OnBreakBlock());
    }
}