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
            getEntitiesAtLocation(world, event.getTargetBlock().getX(), event.getTargetBlock().getY(), event.getTargetBlock().getZ(), entitiesAtLocation::add);

            for(Ref<EntityStore> entityRef : entitiesAtLocation) {
                var suppressionEntity = world.getEntityStore().getStore().getComponent(entityRef, SpawnSuppressionComponent.getComponentType());
                var entityLocation = store.getComponent(entityRef, TransformComponent.getComponentType());
                // Remove any SpawnSuppression entities at the location of the campfire that was broken.
                if (suppressionEntity != null
                        && entityLocation != null
                        && entityLocation.getPosition().equals(event.getTargetBlock().toVector3d())){
                    commandBuffer.removeEntity(entityRef, RemoveReason.REMOVE);
                }
            }
        }
    }

    private void getEntitiesAtLocation(World world, int x, int y, int z, @Nonnull Consumer<Ref<EntityStore>> action) {
        world.getEntityStore().getStore().forEachChunk(Archetype.of(new ComponentType[]{PrefabCopyableComponent.getComponentType(), TransformComponent.getComponentType()}), (archetypeChunk, commandBuffer) -> {
            int size = archetypeChunk.size();

            for(int index = 0; index < size; ++index) {
                Vector3d vector = ((TransformComponent)archetypeChunk.getComponent(index, TransformComponent.getComponentType())).getPosition();
                Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
                action.accept(ref);
            }
        });
    }

    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.empty();
    }
}
