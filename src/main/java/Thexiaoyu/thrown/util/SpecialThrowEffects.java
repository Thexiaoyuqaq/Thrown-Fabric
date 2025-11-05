package Thexiaoyu.thrown.util;

import Thexiaoyu.thrown.Thrown;
import Thexiaoyu.thrown.entity.ThrownItemEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class SpecialThrowEffects {
    private static final Random RANDOM = new Random();
    private static boolean crashTriggered;

    public static boolean handleSpecialItem(ItemStack itemStack, World world, Vec3d pos, Vec3d motion) {
        if (itemStack.getItem() == Items.NETHER_STAR) {
            spawnWither(world, pos);
            return true;
        } else if (itemStack.getItem() == Items.DRAGON_EGG) {
            spawnEnderDragon(world, pos);
            return true;
        } else if (itemStack.getItem() == Items.ICE) {
            freezeNearbyEntities(world, pos);
            return true;
        } else if (itemStack.getItem() == Items.LIGHTNING_ROD) {
            summonLightning(world, pos);
            return true;
        } else if (itemStack.getItem() == Items.WITHER_SKELETON_SKULL) {
            shootWitherSkull(world, pos, motion);
            return true;
        } else if (itemStack.getItem() == Items.FLINT_AND_STEEL) {
            igniteArea(world, pos);
            return true;
        } else if (itemStack.getItem() == Items.END_CRYSTAL) {
            spawnEndCrystal(world, pos);
            return true;
        } else if (itemStack.getItem() == Registries.ITEM.get(new Identifier(Thrown.MOD_ID, "kzzyc"))) {
            triggerCrash(world, pos);
            return true;
        }
        return false;
    }

    private static void igniteArea(World world, Vec3d pos) {
        if (!world.isClient()) {
            BlockPos center = BlockPos.ofFloored(pos.x, pos.y, pos.z);
            int radius = 5;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos targetPos = center.add(x, 0, z);
                    BlockState targetState = world.getBlockState(targetPos);

                    if (targetState.isAir() || targetState.isReplaceable()) {
                        world.setBlockState(targetPos, Blocks.FIRE.getDefaultState());
                    }
                }
            }
        }
    }

    private static void spawnWither(World world, Vec3d pos) {
        if (world instanceof ServerWorld) {
            WitherEntity wither = EntityType.WITHER.create(world);
            if (wither != null) {
                wither.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                world.spawnEntity(wither);
            }
        }
    }

    private static void spawnEnderDragon(World world, Vec3d pos) {
        if (world instanceof ServerWorld) {
            EnderDragonEntity dragon = EntityType.ENDER_DRAGON.create(world);
            if (dragon != null) {
                dragon.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                world.spawnEntity(dragon);
            }
        }
    }

    private static void freezeNearbyEntities(World world, Vec3d pos) {
        Box box = new Box(pos.x - 10, pos.y - 10, pos.z - 10, pos.x + 10, pos.y + 10, pos.z + 10);
        List<Entity> entities = world.getOtherEntities(null, box);

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                livingEntity.setVelocity(Vec3d.ZERO);
                livingEntity.setNoGravity(true);
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 200, 255, false, false));

                BlockPos entityPos = entity.getBlockPos();
                for (int x = -1; x <= 1; x++) {
                    for (int y = 0; y <= 2; y++) {
                        for (int z = -1; z <= 1; z++) {
                            BlockPos icePos = entityPos.add(x, y, z);
                            if (world.getBlockState(icePos).isAir()) {
                                world.setBlockState(icePos, Blocks.ICE.getDefaultState(), 3);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void summonLightning(World world, Vec3d pos) {
        if (world instanceof ServerWorld serverWorld) {
            LightningEntity lightning = EntityType.LIGHTNING_BOLT.create(world);
            if (lightning != null) {
                lightning.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                serverWorld.spawnEntity(lightning);

                Box box = new Box(pos.x - 5, pos.y - 5, pos.z - 5, pos.x + 5, pos.y + 5, pos.z + 5);
                List<Entity> entities = world.getOtherEntities(null, box);

                for (Entity entity : entities) {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity instanceof PlayerEntity) {
                            livingEntity.damage(world.getDamageSources().lightningBolt(), 500);
                        } else {
                            livingEntity.kill();
                        }
                    }
                }
            }
        }
    }

    private static void shootWitherSkull(World world, Vec3d pos, Vec3d motion) {
        if (!world.isClient()) {
            WitherSkullEntity witherSkull = EntityType.WITHER_SKULL.create(world);
            if (witherSkull != null) {
                witherSkull.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
                witherSkull.setVelocity(motion);

                if (RANDOM.nextFloat() < 0.3f) {
                    witherSkull.setCharged(true);
                }

                witherSkull.setFrozenTicks(200);
                world.spawnEntity(witherSkull);
            }
        }
    }

    private static void spawnEndCrystal(World world, Vec3d pos) {
        if (world instanceof ServerWorld serverWorld) {
            BlockPos blockPos = BlockPos.ofFloored(pos.x, pos.y, pos.z);
            EndCrystalEntity endCrystal = EntityType.END_CRYSTAL.create(serverWorld);
            if (endCrystal != null) {
                endCrystal.refreshPositionAndAngles(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0.0F, 0.0F);
                serverWorld.spawnEntity(endCrystal);
            }
        }
    }

    private static void triggerCrash(World world, Vec3d pos) {
        if (world instanceof ServerWorld serverWorld) {
            // 防止重复触发
            if (crashTriggered) {
                return;
            }

            crashTriggered = true;

            serverWorld.getServer().execute(() -> {
                try {
                    Box box = new Box(pos.x - 5, pos.y - 5, pos.z - 5, pos.x + 5, pos.y + 5, pos.z + 5);
                    List<Entity> entities = world.getOtherEntities(null, box);

                    for (Entity entity : entities) {
                        if (entity instanceof ThrownItemEntity thrownItem) {
                            ItemStack stack = thrownItem.getStack();
                            if (stack.getItem() == Registries.ITEM.get(new Identifier(Thrown.MOD_ID, "kzzyc"))) {
                                entity.remove(Entity.RemovalReason.KILLED);
                            }
                        }
                    }

                    crashTriggered = false;

                    throw new RuntimeException(
                            "\n" +
                                    "§c§l╔═══════════════════════════════════════╗\n" +
                                    "§c§l║   CRITICAL SYSTEM FAILURE            ║\n" +
                                    "§c§l╚═══════════════════════════════════════╝\n" +
                                    "\n" +
                                    "§4Cause: §fKZZYC Item Effect\n" +
                                    "§7Exception: §fjava.lang.NullPointerException\n" +
                                    "§8Location: §7Thexiaoyu.thrown.util.SpecialThrowEffects.triggerCrash()\n" +
                                    "\n" +
                                    "§8This is an intentional crash for entertainment purposes.\n" +
                                    "§8Your world data has been saved and is safe.\n" +
                                    "§8Simply restart the game to continue playing.\n"
                    );
                } catch (Exception e) {
                    if (e instanceof RuntimeException) {
                        throw (RuntimeException) e;
                    }
                    crashTriggered = false;
                    throw new RuntimeException("Failed to trigger KZZYC crash", e);
                }
            });
        }
    }
}