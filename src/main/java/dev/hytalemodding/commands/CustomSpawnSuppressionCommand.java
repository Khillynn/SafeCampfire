package dev.hytalemodding.commands;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.assets.spawnsuppression.SpawnSuppression;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;

import javax.annotation.Nonnull;

public class CustomSpawnSuppressionCommand extends AbstractCommandCollection {
    @Nonnull
    private static final AssetArgumentType<SpawnSuppression, ?> SPAWN_SUPPRESSION_ASSET_TYPE = new AssetArgumentType("server.commands.spawning.suppression.arg.suppression.name", SpawnSuppression.class, "server.commands.spawning.suppression.arg.suppression.usage");

    public CustomSpawnSuppressionCommand() {
        super("customSuppression", "Used when a campfire is placed. By default will generate a spawn suppression area 45 block radius around campfire, which prevents aggressive spawns.");
        this.addSubCommand(new Add());
    }

    private static class Add extends AbstractPlayerCommand {
        @Override
        protected boolean canGeneratePermission() {
            return false;  // Anyone can use this command
        }

        @Nonnull
        private final RequiredArg<Vector3i> targetBlock;

        @Nonnull
        private final RequiredArg<SpawnSuppression> suppressionArg;

        public Add() {
            super("add", "server.commands.spawning.suppression.add.desc");
            this.targetBlock = this.withRequiredArg("Target Block", "Block to spawn suppression block. Should be a string of 3 integers separated by spaces.", ArgTypes.VECTOR3I);
            this.suppressionArg = this.withRequiredArg("suppression", "server.commands.spawning.suppression.add.arg.suppression.desc", SPAWN_SUPPRESSION_ASSET_TYPE);
        }

        protected void execute(@Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
            TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
            if(transformComponent != null) {
                transformComponent.setPosition(targetBlock.get(context).toVector3d());
                transformComponent.setRotation(new Vector3f());

                SpawnSuppression spawnSuppression = (SpawnSuppression)this.suppressionArg.get(context);
                Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
                holder.addComponent(SpawnSuppressionComponent.getComponentType(), new SpawnSuppressionComponent(spawnSuppression.getId()));
                holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(transformComponent.getPosition(), transformComponent.getRotation()));
                holder.ensureComponent(UUIDComponent.getComponentType());
                holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
                Model model = SpawningPlugin.get().getSpawnMarkerModel();
                holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
                holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
                Nameplate nameplate = new Nameplate("SpawnSuppression: " + String.valueOf(spawnSuppression));
                holder.addComponent(Nameplate.getComponentType(), nameplate);
                store.addEntity(holder, AddReason.SPAWN);
            }
        }
    }
}
