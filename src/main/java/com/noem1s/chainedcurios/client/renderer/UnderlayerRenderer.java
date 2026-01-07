package com.noem1s.chainedcurios.client.renderer;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.noem1s.chainedcurios.compat.AllTheTrimsCompat;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import com.noem1s.chainedcurios.ChainedCuriosConfig;

public class UnderlayerRenderer implements ICurioRenderer {
   private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
   private static HumanoidModel<LivingEntity> innerArmorModel;
   private static HumanoidModel<LivingEntity> outerArmorModel;
   private final EquipmentSlot slot;

   public UnderlayerRenderer(EquipmentSlot slot) {
      this.slot = slot;
   }

   public static void initArmorModels(EntityModelSet modelSet) {
      innerArmorModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
      outerArmorModel = new HumanoidModel<>(modelSet.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
   }

   @Override
   public <T extends LivingEntity, M extends EntityModel<T>> void render(
      ItemStack stack,
      SlotContext slotContext,
      PoseStack poseStack,
      RenderLayerParent<T, M> renderLayerParent,
      MultiBufferSource bufferSource,
      int light,
      float limbSwing,
      float limbSwingAmount,
      float partialTicks,
      float ageInTicks,
      float netHeadYaw,
      float headPitch
   ) {
      // Check config
      if (!ChainedCuriosConfig.RENDER_UNDERLAYER.get()) {
         return;
      }

      if (innerArmorModel == null || outerArmorModel == null) {
         initArmorModels(Minecraft.getInstance().getEntityModels());
      }

      LivingEntity entity = slotContext.entity();
      boolean isFirstPerson = Minecraft.getInstance().options.getCameraType().isFirstPerson();
      boolean isClientPlayer = entity == Minecraft.getInstance().player;
      boolean isGuiOpen = Minecraft.getInstance().screen != null;

      if (this.slot != EquipmentSlot.HEAD || !isClientPlayer || !isFirstPerson || isGuiOpen) {
         if (renderLayerParent.getModel() instanceof HumanoidModel<?> baseModel) {
            if (bufferSource instanceof BufferSource bs) {
               bs.endBatch(); // flush pending renders
            }

            ItemStack overArmorStack = entity.getItemBySlot(this.slot);
            if (!overArmorStack.isEmpty() && overArmorStack.getItem() instanceof ArmorItem) {
               // STENCIL RENDERING LOGIC
               Minecraft.getInstance().getMainRenderTarget().enableStencil();
               GL11.glEnable(GL11.GL_STENCIL_TEST);
               RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);

               if (this.slot == EquipmentSlot.CHEST) {
                  this.executeRenderCycle(true, false, stack, overArmorStack, entity, baseModel, poseStack, bufferSource, light);
                  RenderSystem.clear(GL11.GL_STENCIL_BUFFER_BIT, Minecraft.ON_OSX);
                  this.executeRenderCycle(false, true, stack, overArmorStack, entity, baseModel, poseStack, bufferSource, light);
               } else {
                  this.executeRenderCycle(true, true, stack, overArmorStack, entity, baseModel, poseStack, bufferSource, light);
               }

               GL11.glDisable(GL11.GL_STENCIL_TEST);
               RenderSystem.stencilFunc(GL11.GL_ALWAYS, 0, 0xFF);
            } else {
               // Normal rendering if no outer armor
               this.renderUnderlayer(true, true, stack, entity, baseModel, poseStack, bufferSource, light);
            }
         }
      }
   }

   private void executeRenderCycle(
      boolean renderBody,
      boolean renderLimbs,
      ItemStack underStack,
      ItemStack overStack,
      LivingEntity entity,
      HumanoidModel<?> baseModel,
      PoseStack poseStack,
      MultiBufferSource bufferSource,
      int light
   ) {
      poseStack.pushPose();
      Model maskModel = ForgeHooksClient.getArmorModel(entity, overStack, this.slot, this.slot == EquipmentSlot.LEGS ? innerArmorModel : outerArmorModel);

      if (maskModel instanceof HumanoidModel) {
         copyBaseModelProperties((HumanoidModel<LivingEntity>)maskModel, baseModel);
         setVisible((HumanoidModel<LivingEntity>)maskModel, this.slot, renderBody, renderLimbs);
      }

      // Write 1 to stencil where outer armor renders
      RenderSystem.stencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
      RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
      RenderSystem.colorMask(false, false, false, false); // Don't write color
      RenderSystem.depthMask(true); // Write depth

      ResourceLocation maskLoc = this.getArmorResource(entity, overStack, this.slot, null);
      RenderType maskType = ChainedCuriosRenderTypes.armorCutoutWithCull(maskLoc); // Use custom render type

      maskModel.renderToBuffer(poseStack, bufferSource.getBuffer(maskType), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

      if (bufferSource instanceof BufferSource bs) {
         bs.endBatch(maskType); // Ensure it draws to stencil
      }

      poseStack.popPose();

      // Render underlayer only where stencil != 1 (masked out by outer armor)
      RenderSystem.stencilFunc(GL11.GL_NOTEQUAL, 1, 0xFF);
      RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);

      this.renderUnderlayer(renderBody, renderLimbs, underStack, entity, baseModel, poseStack, bufferSource, light);

      if (bufferSource instanceof BufferSource bs) {
         bs.endBatch(); // Flush
      }
   }

   private void renderUnderlayer(
      boolean renderBody,
      boolean renderLimbs,
      ItemStack underlayerStack,
      LivingEntity entity,
      HumanoidModel<?> baseModel,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int light
   ) {
      if (underlayerStack.getItem() instanceof ArmorItem armorItem) {
         poseStack.pushPose();
         RenderSystem.enablePolygonOffset();
         RenderSystem.polygonOffset(1.0F, 1.0F);

         HumanoidModel<LivingEntity> armorModel = this.slot == EquipmentSlot.LEGS ? innerArmorModel : outerArmorModel;
         copyBaseModelProperties(armorModel, baseModel);
         setVisible(armorModel, this.slot, renderBody, renderLimbs);

         Model finalArmorModel = ForgeHooksClient.getArmorModel(entity, underlayerStack, this.slot, armorModel);
         ResourceLocation texture = this.getArmorResource(entity, underlayerStack, this.slot, null);
         RenderType renderType = RenderType.armorCutoutNoCull(texture);

         finalArmorModel.renderToBuffer(poseStack, buffer.getBuffer(renderType), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

         ArmorTrim.getTrim(entity.level().registryAccess(), underlayerStack)
            .ifPresent(
               trim -> {
                  boolean renderedWithCompat = false;
                  if (finalArmorModel instanceof HumanoidModel) {
                     renderedWithCompat = AllTheTrimsCompat.tryRender(
                        poseStack, buffer, light, (HumanoidModel<?>)finalArmorModel, armorItem.getMaterial(), trim, this.slot == EquipmentSlot.LEGS
                     );
                  }

                  if (!renderedWithCompat) {
                     TextureAtlasSprite sprite = Minecraft.getInstance()
                        .getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET)
                        .apply(this.slot == EquipmentSlot.LEGS ? trim.innerTexture(armorItem.getMaterial()) : trim.outerTexture(armorItem.getMaterial()));
                     VertexConsumer consumer = sprite.wrap(buffer.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value().decal())));
                     finalArmorModel.renderToBuffer(poseStack, consumer, light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                  }
               }
            );

         if (underlayerStack.hasFoil()) {
             finalArmorModel.renderToBuffer(poseStack, buffer.getBuffer(RenderType.armorEntityGlint()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
         }

         RenderSystem.disablePolygonOffset();
         poseStack.popPose();
      }
   }

   private static void copyBaseModelProperties(HumanoidModel<LivingEntity> armorModel, HumanoidModel<?> baseModel) {
      armorModel.attackTime = baseModel.attackTime;
      armorModel.riding = baseModel.riding;
      armorModel.young = baseModel.young;
      armorModel.leftArmPose = baseModel.leftArmPose;
      armorModel.rightArmPose = baseModel.rightArmPose;
      armorModel.crouching = baseModel.crouching;
      armorModel.head.copyFrom(baseModel.head);
      armorModel.hat.copyFrom(baseModel.hat);
      armorModel.body.copyFrom(baseModel.body);
      armorModel.rightArm.copyFrom(baseModel.rightArm);
      armorModel.leftArm.copyFrom(baseModel.leftArm);
      armorModel.rightLeg.copyFrom(baseModel.rightLeg);
      armorModel.leftLeg.copyFrom(baseModel.leftLeg);
   }

   private static void setVisible(HumanoidModel<LivingEntity> model, EquipmentSlot slot, boolean includeBody, boolean includeLimbs) {
      model.setAllVisible(false);
      switch (slot) {
         case HEAD:
            model.head.visible = includeLimbs;
            model.hat.visible = includeLimbs;
            break;
         case CHEST:
            model.body.visible = includeBody;
            model.rightArm.visible = includeLimbs;
            model.leftArm.visible = includeLimbs;
            break;
         case LEGS:
            model.body.visible = includeBody;
            model.rightLeg.visible = includeLimbs;
            model.leftLeg.visible = includeLimbs;
            break;
         case FEET:
            model.rightLeg.visible = includeLimbs;
            model.leftLeg.visible = includeLimbs;
      }
   }

   private ResourceLocation getArmorResource(LivingEntity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
      ArmorItem item = (ArmorItem)stack.getItem();
      String texture = item.getMaterial().getName();
      String domain = "minecraft";
      int idx = texture.indexOf(58); // ':'
      if (idx != -1) {
         domain = texture.substring(0, idx);
         texture = texture.substring(idx + 1);
      }

      String path = String.format(
         Locale.ROOT,
         "textures/models/armor/%s_layer_%d%s.png",
         texture,
         slot == EquipmentSlot.LEGS ? 2 : 1,
         type == null ? "" : String.format(Locale.ROOT, "_%s", type)
      );
      String fullPath = domain + ":" + path;
      fullPath = ForgeHooksClient.getArmorTexture(entity, stack, fullPath, slot, type);
      return ARMOR_LOCATION_CACHE.computeIfAbsent(fullPath, ResourceLocation::new);
   }
}
