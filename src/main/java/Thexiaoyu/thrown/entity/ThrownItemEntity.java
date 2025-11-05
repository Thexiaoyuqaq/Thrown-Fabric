package Thexiaoyu.thrown.entity;

import Thexiaoyu.thrown.init.ModEntities;
import Thexiaoyu.thrown.util.BlockPlacer;
import Thexiaoyu.thrown.util.SpawnEggThrower;
import Thexiaoyu.thrown.util.SpecialThrowEffects;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Random;

public class ThrownItemEntity extends net.minecraft.entity.projectile.thrown.ThrownItemEntity {
    private static final Random RANDOM = new Random();
    private static final int MAX_RETURN_TICKS = 100;
    private boolean placeBlockMode = false;
    private boolean hitEntity = false;
    private boolean isReturning = false;
    private boolean shouldExplode = false;
    private int returnTicks = 0;

    public ThrownItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
        super(entityType, world);
    }

    public ThrownItemEntity(World world, LivingEntity owner) {
        super(ModEntities.THROWN_ITEM, owner, world);
    }

    public ThrownItemEntity(World world, double x, double y, double z) {
        super(ModEntities.THROWN_ITEM, x, y, z, world);
    }

    public void setPlaceBlockMode(boolean mode) {
        this.placeBlockMode = mode;
    }

    public void setShouldExplode(boolean explode) {
        this.shouldExplode = explode;
    }

    public boolean isReturning() {
        return isReturning;
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    public void tick() {
        super.tick();
        if (isReturning) {
            returnTicks++;
            if (returnTicks > MAX_RETURN_TICKS || this.getOwner() == null || this.squaredDistanceTo(this.getOwner()) < 1.0) {
                if (this.getOwner() instanceof PlayerEntity player) {
                    player.getInventory().insertStack(this.getStack());
                }
                this.discard();
            } else {
                this.setVelocity(this.getOwner().getPos().subtract(this.getPos()).normalize().multiply(0.5));
            }
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (this.getWorld().isClient()) return;

        ItemStack itemStack = this.getStack();
        Item item = itemStack.getItem();

        if (item instanceof SwordItem && hitEntity) {
            isReturning = true;
            return;
        }

        if (item instanceof BucketItem && hitResult instanceof BlockHitResult) {
            handleBucketHit((BlockHitResult) hitResult, (BucketItem) item);
        } else if (item == Items.WITHER_SKELETON_SKULL) {
            spawnWitherSkull();
        } else if (placeBlockMode && hitResult instanceof BlockHitResult && item instanceof BlockItem) {
            handleBlockPlacement(itemStack, (BlockHitResult) hitResult);
        } else if (SpecialThrowEffects.handleSpecialItem(itemStack, this.getWorld(), this.getPos(), this.getVelocity())) {
            this.discard();
        } else if (item instanceof PickaxeItem && hitResult instanceof BlockHitResult && mineBlock(itemStack, (BlockHitResult) hitResult)) {
            this.discard();
        } else {
            handleRegularItemHit(item, itemStack);
        }
    }

    private void handleBucketHit(BlockHitResult result, BucketItem bucketItem) {
        BlockPos placePos = result.getBlockPos().offset(result.getSide());

        // 使用反射获取fluid字段
        Fluid fluid = getFluidFromBucket(bucketItem);

        if (fluid != Fluids.EMPTY) {
            BlockState fluidState = fluid.getDefaultState().getBlockState();
            if (fluidState != null) {
                this.getWorld().setBlockState(placePos, fluidState, 3);
                spawnItemDrop(new ItemStack(Items.BUCKET));
                if (bucketItem instanceof EntityBucketItem) {
                    handleMobBucket(bucketItem, placePos);
                }
            } else {
                spawnItemDrop(this.getStack());
            }
        } else {
            spawnItemDrop(this.getStack());
        }
        this.discard();
    }

    private Fluid getFluidFromBucket(BucketItem bucketItem) {
        try {
            Field fluidField = BucketItem.class.getDeclaredField("fluid");
            fluidField.setAccessible(true);
            return (Fluid) fluidField.get(bucketItem);
        } catch (Exception e) {
            return Fluids.EMPTY;
        }
    }

    private void handleMobBucket(BucketItem bucketItem, BlockPos placePos) {
        try {
            Method getEntityTypeMethod = EntityBucketItem.class.getDeclaredMethod("getEntityType");
            getEntityTypeMethod.setAccessible(true);
            EntityType<?> entityType = (EntityType<?>) getEntityTypeMethod.invoke(bucketItem);
            net.minecraft.entity.Entity entity = entityType.create(this.getWorld());
            if (entity != null) {
                NbtCompound bucketTag = this.getStack().getNbt();
                if (bucketTag != null) {
                    if (bucketTag.contains("Variant")) {
                        entity.readNbt(bucketTag);
                    } else if (bucketTag.contains("BucketVariantTag")) {
                        entity.readNbt(bucketTag.getCompound("BucketVariantTag"));
                    } else if (bucketTag.contains("EntityTag")) {
                        entity.readNbt(bucketTag.getCompound("EntityTag"));
                    }
                }
                entity.refreshPositionAndAngles(placePos.getX() + 0.5, placePos.getY(), placePos.getZ() + 0.5, 0, 0);
                this.getWorld().spawnEntity(entity);
            }
        } catch (Exception e) {
            // Silent fail
        }
    }

    private void spawnWitherSkull() {
        WitherSkullEntity witherSkull = EntityType.WITHER_SKULL.create(getWorld());
        if (witherSkull != null) {
            witherSkull.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), 0, 0);
            witherSkull.setVelocity(this.getVelocity());
            if (RANDOM.nextFloat() < 0.3f) {
                witherSkull.setCharged(true);
            }
            getWorld().spawnEntity(witherSkull);
        }
        this.discard();
    }

    private void handleBlockPlacement(ItemStack itemStack, BlockHitResult blockHitResult) {
        net.minecraft.entity.Entity owner = this.getOwner();
        Direction playerFacing = (owner instanceof PlayerEntity) ? ((PlayerEntity) owner).getHorizontalFacing() : Direction.NORTH;
        if (BlockPlacer.placeBlock(itemStack, this.getWorld(), blockHitResult, playerFacing, this.getVelocity().normalize())) {
            this.discard();
        }
    }

    private boolean mineBlock(ItemStack itemStack, BlockHitResult blockHitResult) {
        BlockPos blockPos = blockHitResult.getBlockPos();
        BlockState blockState = this.getWorld().getBlockState(blockPos);
        if (blockState.getHardness(this.getWorld(), blockPos) >= 0) {
            this.getWorld().breakBlock(blockPos, true, this);
            itemStack.damage(1, (LivingEntity) this.getOwner(), (entity) -> {});
            if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                itemStack.decrement(1);
            }
            if (!itemStack.isEmpty()) {
                spawnItemDrop(itemStack);
            }
            return true;
        }
        return false;
    }

    private void handleRegularItemHit(Item item, ItemStack itemStack) {
        if (item == Items.TNT) {
            if (shouldExplode) {
                net.minecraft.entity.TntEntity primedTnt = new net.minecraft.entity.TntEntity(
                        this.getWorld(), this.getX(), this.getY(), this.getZ(),
                        this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : null
                );
                primedTnt.setFuse(0);
                this.getWorld().spawnEntity(primedTnt);
            } else {
                spawnItemDrop(new ItemStack(Items.TNT));
            }
        } else if (item == Items.BLAZE_POWDER) {
            this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0F, World.ExplosionSourceType.NONE);
        } else if (item instanceof SpawnEggItem) {
            if (SpawnEggThrower.trySpawnMob(itemStack, this.getWorld(), this.getPos())) {
                spawnItemDrop(itemStack);
            }
        } else {
            spawnItemDrop(itemStack);
        }
        this.discard();
    }

    private void spawnItemDrop(ItemStack itemStack) {
        this.getWorld().spawnEntity(new ItemEntity(this.getWorld(), this.getX(), this.getY(), this.getZ(), itemStack));
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        if (this.getWorld().isClient()) return;

        net.minecraft.entity.Entity entity = entityHitResult.getEntity();
        ItemStack itemStack = this.getStack();
        Item item = itemStack.getItem();

        if (item instanceof SwordItem) {
            hitEntity = true;
            itemStack.damage(1, (LivingEntity) this.getOwner(), (player) -> {});
        }

        float damage = itemStack.getName().getString().length();
        if (item == Items.WITHER_SKELETON_SKULL && entity instanceof LivingEntity) {
            entity.damage(this.getDamageSources().create(DamageTypes.MAGIC), 80f);
        } else {
            entity.damage(this.getDamageSources().thrown(this, this.getOwner()), damage);
            if (item == Items.BLAZE_POWDER) {
                this.getWorld().createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0F, World.ExplosionSourceType.NONE);
            }
        }

        if (item instanceof SpawnEggItem) {
            SpawnEggThrower.trySpawnMob(itemStack, this.getWorld(), this.getPos());
        }
    }

    public ItemStack getStack() {
        ItemStack itemstack = this.getItem();
        return itemstack.isEmpty() ? new ItemStack(this.getDefaultItem()) : itemstack;
    }
}