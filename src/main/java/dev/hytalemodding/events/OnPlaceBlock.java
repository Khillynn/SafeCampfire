package dev.hytalemodding.events;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.util.Objects;

public class OnPlaceBlock extends EntityEventSystem<EntityStore, PlaceBlockEvent> {
    public OnPlaceBlock() {
        super(PlaceBlockEvent.class);
    }

    @Override
    public void handle(int index,
                       @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull PlaceBlockEvent event) {
        if(Objects.equals(event.getItemInHand() != null ? event.getItemInHand().getBlockKey() : null, "Bench_Campfire")){
            Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);
            PlayerRef player = store.getComponent(entityStoreRef, PlayerRef.getComponentType());
            if(player == null) return;
            String vectorString = event.getTargetBlock().getX() + " " + event.getTargetBlock().getY() + " " + event.getTargetBlock().getZ();
            String suppressionFile = "Suppress_Aggressive_Spawns";
            CommandManager.get().handleCommand(player, "customSuppression add " + vectorString + " " + suppressionFile);
        }
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}