package Thexiaoyu.thrown;

import Thexiaoyu.thrown.init.ModEntities;
import Thexiaoyu.thrown.init.ModItems;
import Thexiaoyu.thrown.network.ModNetworking;
import Thexiaoyu.thrown.event.PlayerEventHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thrown implements ModInitializer {
    public static final String MOD_ID = "thrown";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModItems.register();
        ModEntities.register();
        ModNetworking.registerC2SPackets();
        PlayerEventHandler.register();
    }
}