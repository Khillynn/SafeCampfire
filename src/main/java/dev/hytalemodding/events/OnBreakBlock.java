package dev.hytalemodding.events;

import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabCopyableComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.suppression.component.SpawnSuppressionComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

public class OnBreakBlock extends EntityEventSystem<EntityStore, BreakBlockEvent> {
    public OnBreakBlock() {
        super(BreakBlockEvent.class);
    }

    @Override
    public void handle(int index,
                       @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
                       @Nonnull Store<EntityStore> store,
                       @Nonnull CommandBuffer<EntityStore> commandBuffer,
                       @Nonnull BreakBlockEvent event) {
        Ref<EntityStore> entityStoreRef = archetypeChunk.getReferenceTo(index);
        PlayerRef player = store.getComponent(entityStoreRef, PlayerRef.getComponentType());
        if (player == null) return;
        if (Objects.equals(event.getBlockType().getId(), "Bench_Campfire")) {
            World world = Universe.get().getWorld(player.getWorldUuid());
            if (world == null) return;
            ArrayList<Ref<EntityStore>> entitiesAtLocation = new ArrayList<>();
            getSuppressionEntitiesAtLocation(world, event.getTargetBlock().toVector3d(), entitiesAtLocation::add);

            for(Ref<EntityStore> entityRef : entitiesAtLocation) {
                commandBuffer.removeEntity(entityRef, RemoveReason.REMOVE);
            }
        }
    }

    private void getSuppressionEntitiesAtLocation(World world, Vector3d campfireLocation, @Nonnull Consumer<Ref<EntityStore>> action) {
        var worldStore = world.getEntityStore().getStore();
        worldStore.forEachChunk(Archetype.of(new ComponentType[]{PrefabCopyableComponent.getComponentType(), TransformComponent.getComponentType()}), (archetypeChunk, commandBuffer) -> {
            int size = archetypeChunk.size();

            for(int index = 0; index < size; ++index) {
                Vector3d entityLocation = ((TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType())).getPosition();
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                var suppressionEntity = worldStore.getComponent(ref, SpawnSuppressionComponent.getComponentType());
                // Remove any SpawnSuppression entities at the location of the campfire that was broken.
                if (suppressionEntity != null && entityLocation.equals(campfireLocation)) {
                    action.accept(ref);
                }
            }
        });
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
