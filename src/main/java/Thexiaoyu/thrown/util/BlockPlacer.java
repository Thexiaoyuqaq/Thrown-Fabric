package Thexiaoyu.thrown.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockPlacer {
    public static boolean placeBlock(ItemStack itemStack, World world, BlockHitResult hitResult, Direction playerFacing, Vec3d throwDirection) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            BlockPos pos = hitResult.getBlockPos().offset(hitResult.getSide());
            BlockState state = blockItem.getBlock().getDefaultState();
            Direction facingDirection = getFacingDirection(throwDirection, playerFacing);

            if (state.contains(Properties.FACING)) {
                state = state.with(Properties.FACING, facingDirection);
            } else if (state.contains(Properties.HORIZONTAL_FACING)) {
                state = state.with(Properties.HORIZONTAL_FACING,
                        facingDirection.getAxis().isHorizontal() ? facingDirection : playerFacing.getOpposite());
            }

            if (world.setBlockState(pos, state, 3)) {
                if (itemStack.hasNbt()) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (blockEntity != null) {
                        NbtCompound nbt = itemStack.getNbt().copy();
                        nbt.remove("x");
                        nbt.remove("y");
                        nbt.remove("z");

                        if (nbt.contains("BlockEntityTag")) {
                            NbtCompound blockEntityTag = nbt.getCompound("BlockEntityTag");
                            if (blockEntityTag.contains("Items")) {
                                blockEntity.readNbt(blockEntityTag);
                            }
                        } else {
                            blockEntity.readNbt(nbt);
                        }
                        blockEntity.markDirty();
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static Direction getFacingDirection(Vec3d throwDirection, Direction playerFacing) {
        if (Math.abs(throwDirection.y) > Math.abs(throwDirection.x) && Math.abs(throwDirection.y) > Math.abs(throwDirection.z)) {
            return throwDirection.y > 0 ? Direction.DOWN : Direction.UP;
        }
        return playerFacing.getOpposite();
    }
}