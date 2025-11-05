package Thexiaoyu.thrown.client;

import Thexiaoyu.thrown.init.ModEntities;
import Thexiaoyu.thrown.network.ModNetworking;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class ThrownClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindings.register();
        ModNetworking.registerS2CPackets();

        EntityRendererRegistry.register(ModEntities.THROWN_ITEM, ThrownItemRenderer::new);
    }
}