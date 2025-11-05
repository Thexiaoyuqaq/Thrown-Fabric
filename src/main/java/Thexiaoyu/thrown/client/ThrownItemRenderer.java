package Thexiaoyu.thrown.client;

import Thexiaoyu.thrown.entity.ThrownItemEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ThrownItemRenderer extends EntityRenderer<ThrownItemEntity> {
    private final ItemRenderer itemRenderer;

    public ThrownItemRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.itemRenderer = context.getItemRenderer();
        this.shadowRadius = 0.15F;
        this.shadowOpacity = 0.75F;
    }

    @Override
    public void render(ThrownItemEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        matrices.translate(0.0D, 0.15D, 0.0D);

        if (entity.isReturning()) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float) (entity.age * 20)));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevYaw, entity.getYaw()) - 90.0F));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.lerp(tickDelta, entity.prevPitch, entity.getPitch())));
        }

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45.0F));
        matrices.scale(0.5F, 0.5F, 0.5F);
        matrices.translate(0.0D, -0.3D, 0.0D);

        ItemStack itemStack = entity.getStack();
        BakedModel bakedModel = this.itemRenderer.getModel(itemStack, entity.getWorld(), null, entity.getId());
        this.itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV, bakedModel);

        matrices.pop();
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);
    }

    @Override
    public Identifier getTexture(ThrownItemEntity entity) {
        return PlayerScreenHandler.BLOCK_ATLAS_TEXTURE;
    }
}