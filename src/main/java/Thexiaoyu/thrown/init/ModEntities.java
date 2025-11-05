package Thexiaoyu.thrown.init;

import Thexiaoyu.thrown.Thrown;
import Thexiaoyu.thrown.entity.ThrownItemEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<ThrownItemEntity> THROWN_ITEM = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(Thrown.MOD_ID, "thrown_item"),
            FabricEntityTypeBuilder.<ThrownItemEntity>create(SpawnGroup.MISC, ThrownItemEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
                    .trackRangeBlocks(4)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void register() {
        Thrown.LOGGER.info("Registering entities for " + Thrown.MOD_ID);
    }
}