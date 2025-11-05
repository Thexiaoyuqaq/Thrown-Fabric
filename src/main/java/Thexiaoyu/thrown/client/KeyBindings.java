package Thexiaoyu.thrown.client;

import Thexiaoyu.thrown.Thrown;
import Thexiaoyu.thrown.network.ModNetworking;
import Thexiaoyu.thrown.player.PlayerDataComponent;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static KeyBinding THROW_ITEM_KEY;
    public static KeyBinding PLACE_BLOCK_KEY;

    public static void register() {
        THROW_ITEM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.throwitem",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "key.categories." + Thrown.MOD_ID
        ));

        PLACE_BLOCK_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.placeblock",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories." + Thrown.MOD_ID
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            PlayerDataComponent component = PlayerDataComponent.KEY.get(client.player);

            while (THROW_ITEM_KEY.wasPressed()) {
                boolean newThrowMode = !component.isThrowModeEnabled();
                component.setThrowModeEnabled(newThrowMode);
                client.player.sendMessage(
                        Text.literal("Throw mode: " + (newThrowMode ? "Enabled" : "Disabled")),
                        true
                );
                ModNetworking.sendToggleThrowMode(newThrowMode);
            }

            while (PLACE_BLOCK_KEY.wasPressed()) {
                boolean newPlaceBlockMode = !component.isPlaceBlockModeEnabled();
                component.setPlaceBlockModeEnabled(newPlaceBlockMode);
                client.player.sendMessage(
                        Text.literal("Place block mode: " + (newPlaceBlockMode ? "Enabled" : "Disabled")),
                        true
                );
                ModNetworking.sendTogglePlaceBlockMode(newPlaceBlockMode);
            }
        });
    }
}