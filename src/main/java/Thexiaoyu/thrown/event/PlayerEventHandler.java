package Thexiaoyu.thrown.event;

import Thexiaoyu.thrown.entity.ThrownItemEntity;
import Thexiaoyu.thrown.network.ModNetworking;
import Thexiaoyu.thrown.player.PlayerDataComponent;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class PlayerEventHandler {
    public static void register() {
        UseItemCallback.EVENT.register(PlayerEventHandler::onUseItem);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            PlayerDataComponent component = PlayerDataComponent.KEY.get(handler.player);
            component.resetState();
            ModNetworking.sendSyncPlayerData(
                    handler.player,
                    component.isThrowModeEnabled(),
                    component.isPlaceBlockModeEnabled()
            );
        });
    }

    private static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack mainHandItem = player.getMainHandStack();
        ItemStack offHandItem = player.getOffHandStack();

        if (offHandItem.getItem() == Items.FLINT_AND_STEEL && mainHandItem.getItem() != Items.TNT) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        if (mainHandItem.getItem() instanceof ArmorItem) {
            return TypedActionResult.pass(player.getStackInHand(hand));
        }

        PlayerDataComponent component = PlayerDataComponent.KEY.get(player);

        if (component.isThrowModeEnabled()) {
            if (!world.isClient()) {
                if (mainHandItem.getItem() == Items.TNT && offHandItem.getItem() == Items.FLINT_AND_STEEL) {
                    ThrownItemEntity thrownItem = new ThrownItemEntity(world, player);
                    thrownItem.setItem(new ItemStack(Items.TNT, 1));
                    thrownItem.setPlaceBlockMode(component.isPlaceBlockModeEnabled());
                    thrownItem.setShouldExplode(true);
                    thrownItem.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);
                    world.spawnEntity(thrownItem);

                    offHandItem.damage(1, player, (p) -> p.sendToolBreakStatus(Hand.OFF_HAND));

                    if (!player.getAbilities().creativeMode) {
                        mainHandItem.decrement(1);
                    }
                } else {
                    ItemStack itemToThrow = hand == Hand.MAIN_HAND ? mainHandItem : offHandItem;
                    throwItem(player, itemToThrow, hand, component.isPlaceBlockModeEnabled());
                }
                return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
            }
            return TypedActionResult.success(player.getStackInHand(hand), world.isClient());
        }

        return TypedActionResult.pass(player.getStackInHand(hand));
    }

    private static void throwItem(PlayerEntity player, ItemStack itemStack, Hand hand, boolean placeBlockMode) {
        if (!itemStack.isEmpty()) {
            World world = player.getWorld();
            ThrownItemEntity thrownItem = new ThrownItemEntity(world, player);
            thrownItem.setItem(itemStack.copy());
            thrownItem.setPlaceBlockMode(placeBlockMode);
            thrownItem.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 1.5F, 1.0F);
            world.spawnEntity(thrownItem);
            player.swingHand(hand, true);

            if (!player.getAbilities().creativeMode) {
                itemStack.decrement(1);
            }
        }
    }
}