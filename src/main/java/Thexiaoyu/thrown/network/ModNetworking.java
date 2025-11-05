package Thexiaoyu.thrown.network;

import Thexiaoyu.thrown.Thrown;
import Thexiaoyu.thrown.player.PlayerDataComponent;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class ModNetworking {
    public static final Identifier TOGGLE_THROW_MODE = new Identifier(Thrown.MOD_ID, "toggle_throw_mode");
    public static final Identifier TOGGLE_PLACE_BLOCK_MODE = new Identifier(Thrown.MOD_ID, "toggle_place_block_mode");
    public static final Identifier SYNC_PLAYER_DATA = new Identifier(Thrown.MOD_ID, "sync_player_data");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_THROW_MODE, (server, player, handler, buf, responseSender) -> {
            boolean throwMode = buf.readBoolean();
            server.execute(() -> {
                PlayerDataComponent component = PlayerDataComponent.KEY.get(player);
                component.setThrowModeEnabled(throwMode);
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TOGGLE_PLACE_BLOCK_MODE, (server, player, handler, buf, responseSender) -> {
            boolean placeBlockMode = buf.readBoolean();
            server.execute(() -> {
                PlayerDataComponent component = PlayerDataComponent.KEY.get(player);
                component.setPlaceBlockModeEnabled(placeBlockMode);
            });
        });
    }

    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_PLAYER_DATA, (client, handler, buf, responseSender) -> {
            boolean throwMode = buf.readBoolean();
            boolean placeBlockMode = buf.readBoolean();
            client.execute(() -> {
                if (client.player != null) {
                    PlayerDataComponent component = PlayerDataComponent.KEY.get(client.player);
                    component.setThrowModeEnabled(throwMode);
                    component.setPlaceBlockModeEnabled(placeBlockMode);
                }
            });
        });
    }

    public static void sendToggleThrowMode(boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ClientPlayNetworking.send(TOGGLE_THROW_MODE, buf);
    }

    public static void sendTogglePlaceBlockMode(boolean enabled) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(enabled);
        ClientPlayNetworking.send(TOGGLE_PLACE_BLOCK_MODE, buf);
    }

    public static void sendSyncPlayerData(net.minecraft.server.network.ServerPlayerEntity player, boolean throwMode, boolean placeBlockMode) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBoolean(throwMode);
        buf.writeBoolean(placeBlockMode);
        ServerPlayNetworking.send(player, SYNC_PLAYER_DATA, buf);
    }
}