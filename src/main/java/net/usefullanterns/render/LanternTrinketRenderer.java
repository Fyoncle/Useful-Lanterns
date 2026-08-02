package net.usefullanterns.render;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.usefullanterns.UsefulLanternsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.CLIENT)
public class LanternTrinketRenderer implements TrinketRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("useful-lanterns/render");

    // ── Positioning ───────────────────────────────────────────────────────────
    private static final float POS_X = 0.29f;  // negated when flipped
    private static final float POS_Y = 0.73f;
    private static final float POS_Z = -0.19f;
    private static final float ROTATE_X = -17f;
    private static final float ROTATE_Y = 16f;    // negated when flipped
    private static final float ROTATE_Z = 10f;    // negated when flipped
    private static final float DROP = 0.3f;

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
    private static final Map<LivingEntity, PhysicsState> PHYSICS_STATES =
            java.util.Collections.synchronizedMap(new WeakHashMap<>());

    private static final Map<Class<?>, Boolean> WARNED_ITEM_CLASSES = new ConcurrentHashMap<>();

    private static PhysicsState getPhysics(LivingEntity entity) {
        return PHYSICS_STATES.computeIfAbsent(entity, k -> new PhysicsState());
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

        PhysicsState p = getPhysics(entity);

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

            float yawDelta = MathHelper.wrapDegrees(entity.bodyYaw - p.prevBodyYaw);
            p.prevBodyYaw = entity.bodyYaw;

            p.swingVel += yawDelta * YAW_FORCE;
            p.swingVel -= p.swingAngle * GRAVITY;
            p.swingVel *= DAMPING;
            p.swingAngle += p.swingVel;
            p.swingAngle = MathHelper.clamp(p.swingAngle, -SWING_MAX, SWING_MAX);

            if (!onGround && verticalSpeed < -0.05f) {
                float targetAngle = -(float) Math.min(Math.sqrt(p.fallDistance * 2.5f) * 26f, 90f);
                p.foreVel += (targetAngle - p.foreAngle) * 0.08f;
            }
            p.foreVel -= p.foreAngle * GRAVITY;
            p.foreVel *= DAMPING;
            p.foreAngle += p.foreVel;
            p.foreAngle = MathHelper.clamp(p.foreAngle, -90f, BOUNCE_MAX_ANGLE);

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

        boolean flipped = UsefulLanternsConfig.config.lanternOnRightSide;
        float posX = flipped ? -POS_X : POS_X;
        float rotateY = flipped ? -ROTATE_Y : ROTATE_Y;
        float rotateZ = flipped ? -ROTATE_Z : ROTATE_Z;

        matrices.push();

        playerModel.body.rotate(matrices);
        matrices.translate(posX, POS_Y, POS_Z);

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(smoothSwing));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(smoothFore));

        matrices.translate(0.0f, DROP, 0.0f);
        matrices.scale(UsefulLanternsConfig.config.lanternScale, UsefulLanternsConfig.config.lanternScale, UsefulLanternsConfig.config.lanternScale);

        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(180.0f));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(ROTATE_X));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotateY));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotateZ));

        matrices.translate(-0.5f, 0.0f, -0.5f);

        renderLanternItem(stack, matrices, vertexConsumers, light);

        matrices.pop();
    }

    private void renderLanternItem(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            warnOnce(stack, "not a BlockItem — rendering via ItemRenderer fallback instead of a block model");
            renderAsGenericItem(stack, matrices, vertexConsumers, light);
            return;
        }

        Block block = blockItem.getBlock();
        BlockState state = resolveBlockState(stack, block);

        BlockEntity blockEntity = createTransientBlockEntity(block, state);
        if (blockEntity != null) {
            BlockEntityRenderDispatcher dispatcher = MinecraftClient.getInstance().getBlockEntityRenderDispatcher();
            if (dispatcher.get(blockEntity) != null) {
                matrices.push();
                dispatcher.render(blockEntity, MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), matrices, vertexConsumers);
                matrices.pop();
                return;
            }
        }

        BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
        blockRenderer.renderBlockAsEntity(state, matrices, vertexConsumers, light, OverlayTexture.DEFAULT_UV);
    }

    private BlockState resolveBlockState(ItemStack stack, Block block) {
        BlockState base = block.getDefaultState();
        BlockStateComponent component = stack.get(DataComponentTypes.BLOCK_STATE);
        if (component == null) {
            return base;
        }
        return component.applyToState(base);
    }

    private BlockEntity createTransientBlockEntity(Block block, BlockState state) {
        if (!state.hasBlockEntity()) return null;
        try {
            if (block instanceof net.minecraft.block.BlockEntityProvider provider) {
                return provider.createBlockEntity(BlockPos.ORIGIN, state);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to build transient BlockEntity for {} while rendering trinket lantern", block, e);
        }
        return null;
    }

    private void renderAsGenericItem(ItemStack stack, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        MinecraftClient client = MinecraftClient.getInstance();
        ItemRenderer itemRenderer = client.getItemRenderer();
        itemRenderer.renderItem(
                stack,
                ModelTransformationMode.FIXED,
                light,
                OverlayTexture.DEFAULT_UV,
                matrices,
                vertexConsumers,
                client.world,
                0
        );
    }

    private void warnOnce(ItemStack stack, String message) {
        Class<?> itemClass = stack.getItem().getClass();
        if (WARNED_ITEM_CLASSES.putIfAbsent(itemClass, Boolean.TRUE) == null) {
            LOGGER.warn("[LanternTrinketRenderer] {} ({}): {}", stack.getItem(), itemClass.getName(), message);
        }
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