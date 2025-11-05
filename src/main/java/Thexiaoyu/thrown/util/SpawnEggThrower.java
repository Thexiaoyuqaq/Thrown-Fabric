package Thexiaoyu.thrown.util;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SpawnEggThrower {
    public static boolean trySpawnMob(ItemStack itemStack, World world, Vec3d position) {
        if (world instanceof ServerWorld serverWorld && itemStack.getItem() instanceof SpawnEggItem spawnEggItem) {
            EntityType<?> entityType = spawnEggItem.getEntityType(itemStack.getNbt());
            BlockPos blockPos = BlockPos.ofFloored(position.x, position.y, position.z);

            NbtCompound nbt = itemStack.getNbt();

            net.minecraft.entity.Entity entity = entityType.spawn(
                    serverWorld,
                    nbt,
                    null,
                    blockPos,
                    SpawnReason.SPAWN_EGG,
                    true,
                    false
            );

            if (entity instanceof MobEntity mob) {
                mob.refreshPositionAndAngles(position.x, position.y, position.z, 0, 0);
                mob.playSpawnEffects();
                return false;
            }
        }
        return true;
    }
}