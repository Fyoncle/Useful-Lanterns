package net.usefullanterns.render;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.usefullanterns.UsefulLanternsConfig;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class LanternTrinketRenderer implements TrinketRenderer {

    // ── Positioning ───────────────────────────────────────────────────────────
    private static final float POS_X = 0.29f;  // negated when flipped
    private static final float POS_Y = 0.73f;
    private static final float POS_Z = -0.19f;
    private static final float ROTATE_X = -17f;
    private static final float ROTATE_Y = 16f;    // negated when flipped
    private static final float ROTATE_Z = 10f;    // negated when flipped
    private static final float DROP = 0.3f;
    private static final float SCALE = 0.6f;

    // ── Physics ───────────────────────────────────────────────────────────────
    private static final float GRAVITY = 0.07f;
    private static final float DAMPING = 0.72f;

    private static final float YAW_FORCE = 0.14f;
    private static final float SWING_MAX = 28f;

    private static final float BOUNCE_FORCE = 8.0f;
    private static final float BOUNCE_MIN = 0.3f;
    private static final float BOUNCE_CAP = 7.0f;
    private static final float BOUNCE_MAX_ANGLE = 28f;

    // ── State ─────────────────────────────────────────────────────────────────
    private static final Map<Integer, PhysicsState> PHYSICS_STATES = new HashMap<>();

    private static PhysicsState getPhysics(int entityId) {
        return PHYSICS_STATES.computeIfAbsent(entityId, k -> new PhysicsState());
    }

    @Override
    public void render(
            ItemStack stack,
            SlotReference slotReference,
            EntityModel<? extends LivingEntity> contextModel,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            int light,
            LivingEntity entity,
            float limbAngle,
            float limbDistance,
            float tickDelta,
            float animationProgress,
            float headYaw,
            float headPitch
    ) {
        if (!(contextModel instanceof PlayerEntityModel<?> playerModel)) return;

        PhysicsState p = getPhysics(entity.getId());

        long currentTime = MinecraftClient.getInstance().world != null
                ? MinecraftClient.getInstance().world.getTime() : -1;

        if (currentTime != p.lastTickTime) {
            p.lastTickTime = currentTime;

            float verticalSpeed = (float) (entity.getY() - p.prevY);
            p.prevY = entity.getY();
            boolean onGround = entity.isOnGround();

            if (!onGround && verticalSpeed < 0) {
                p.peakFallSpeed = Math.max(p.peakFallSpeed, Math.abs(verticalSpeed));
                p.fallDistance += Math.abs(verticalSpeed);
            }

            // ── Side swing (Z) ────────────────────────────────────────────────
            float yawDelta = MathHelper.wrapDegrees(entity.bodyYaw - p.prevBodyYaw);
            p.prevBodyYaw = entity.bodyYaw;

            p.swingVel += yawDelta * YAW_FORCE;
            p.swingVel -= p.swingAngle * GRAVITY;
            p.swingVel *= DAMPING;
            p.swingAngle += p.swingVel;
            p.swingAngle = MathHelper.clamp(p.swingAngle, -SWING_MAX, SWING_MAX);

            // ── Fore-aft swing (X) ────────────────────────────────────────────
            if (!onGround && verticalSpeed < -0.05f) {
                float targetAngle = -(float) Math.min(Math.sqrt(p.fallDistance * 2.5f) * 26f, 90f);
                p.foreVel += (targetAngle - p.foreAngle) * 0.08f;
            }
            p.foreVel -= p.foreAngle * GRAVITY;
            p.foreVel *= DAMPING;
            p.foreAngle += p.foreVel;
            p.foreAngle = MathHelper.clamp(p.foreAngle, -90f, BOUNCE_MAX_ANGLE);

            // ── Landing bounce ────────────────────────────────────────────────
            if (onGround && !p.wasOnGround) {
                float kick = MathHelper.clamp(p.peakFallSpeed * BOUNCE_FORCE, BOUNCE_MIN, BOUNCE_CAP);
                float raiseReturn = -p.foreAngle * 0.35f;
                p.foreVel += kick + raiseReturn;
                p.swingVel += kick * 0.2f * (((entity.getId() & 1) == 0) ? 1f : -1f);
                p.peakFallSpeed = 0f;
                p.fallDistance = 0f;
            }
            if (onGround) {
                p.peakFallSpeed = 0f;
                p.fallDistance = 0f;
            }
            p.wasOnGround = onGround;
        }

        float smoothSwing = MathHelper.lerp(tickDelta, p.prevRenderSwing, p.swingAngle);
        float smoothFore = MathHelper.lerp(tickDelta, p.prevRenderFore, p.foreAngle);
        p.prevRenderSwing = p.swingAngle;
        p.prevRenderFore = p.foreAngle;

        // ── Flip config — mirrors lantern to right side ───────────────────────
        boolean flipped = UsefulLanternsConfig.get().flipLantern;
        float posX = flipped ? -POS_X : POS_X;
        float rotateY = flipped ? -ROTATE_Y : ROTATE_Y;
        float rotateZ = flipped ? -ROTATE_Z : ROTATE_Z;

        // ── Render ─────────────────────────────────────────────────────────────
        matrices.push();

        playerModel.body.rotate(matrices);
        matrices.translate(posX, POS_Y, POS_Z);

        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.toRadians(smoothSwing)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.toRadians(smoothFore)));

        matrices.translate(0.0f, DROP, 0.0f);
        matrices.scale(SCALE, SCALE, SCALE);

        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.toRadians(180.0)));
        matrices.multiply(RotationAxis.POSITIVE_X.rotation((float) Math.toRadians(ROTATE_X)));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) Math.toRadians(rotateY)));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotation((float) Math.toRadians(rotateZ)));

        matrices.translate(-0.5f, 0.0f, -0.5f);

        renderBlock(stack, matrices, vertexConsumers);

        matrices.pop();
    }

    private void renderBlock(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) return;
        Block block = blockItem.getBlock();
        BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
        blockRenderer.renderBlockAsEntity(
                block.getDefaultState(),
                matrices,
                vertexConsumers,
                15728880,
                OverlayTexture.DEFAULT_UV
        );
    }

    private static class PhysicsState {
        float swingAngle = 0f;
        float swingVel = 0f;
        float foreAngle = 0f;
        float foreVel = 0f;
        float prevBodyYaw = 0f;
        double prevY = 0;
        boolean wasOnGround = true;
        float peakFallSpeed = 0f;
        float fallDistance = 0f;
        float prevRenderSwing = 0f;
        float prevRenderFore = 0f;
        long lastTickTime = -1;
    }
}