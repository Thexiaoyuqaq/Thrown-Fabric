package Thexiaoyu.thrown.player;

import Thexiaoyu.thrown.Thrown;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.CommonTickingComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public class PlayerDataComponent implements AutoSyncedComponent, CommonTickingComponent {
    public static final ComponentKey<PlayerDataComponent> KEY =
            ComponentRegistry.getOrCreate(new Identifier(Thrown.MOD_ID, "player_data"), PlayerDataComponent.class);

    private final PlayerEntity player;
    private boolean throwModeEnabled = false;
    private boolean placeBlockModeEnabled = false;

    public PlayerDataComponent(PlayerEntity player) {
        this.player = player;
    }

    public boolean isThrowModeEnabled() {
        return throwModeEnabled;
    }

    public void setThrowModeEnabled(boolean enabled) {
        this.throwModeEnabled = enabled;
        KEY.sync(this.player);
    }

    public boolean isPlaceBlockModeEnabled() {
        return placeBlockModeEnabled;
    }

    public void setPlaceBlockModeEnabled(boolean enabled) {
        this.placeBlockModeEnabled = enabled;
        KEY.sync(this.player);
    }

    public void resetState() {
        throwModeEnabled = false;
        placeBlockModeEnabled = false;
        KEY.sync(this.player);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        throwModeEnabled = tag.getBoolean("throwModeEnabled");
        placeBlockModeEnabled = tag.getBoolean("placeBlockModeEnabled");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("throwModeEnabled", throwModeEnabled);
        tag.putBoolean("placeBlockModeEnabled", placeBlockModeEnabled);
    }

    @Override
    public void tick() {
    }
}