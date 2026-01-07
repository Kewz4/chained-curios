package com.noem1s.chainedcurios.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.slf4j.Logger;

public class AllTheTrimsCompat {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static boolean initialized = false;
   private static boolean available = false;
   private static boolean hasLoggedSuccess = false;
   private static Method dynamicTrimRenderer_renderTrim;

   private static void initialize() {
      if (!initialized) {
         initialized = true;
         LOGGER.info("[ChainedCurios] Initializing AllTheTrims DynamicRenderer compatibility layer...");

         try {
            Class<?> dynamicTrimRendererClass = Class.forName("com.bawnorton.allthetrims.client.api.DynamicTrimRenderer");
            Class<?> armorMaterialClass = Class.forName("net.minecraft.world.item.ArmorMaterial");
            Class<?> poseStackClass = Class.forName("com.mojang.blaze3d.vertex.PoseStack");
            Class<?> multiBufferSourceClass = Class.forName("net.minecraft.client.renderer.MultiBufferSource");
            Class<?> armorTrimClass = Class.forName("net.minecraft.world.item.armortrim.ArmorTrim");
            Class<?> humanoidModelClass = Class.forName("net.minecraft.client.model.HumanoidModel");
            dynamicTrimRenderer_renderTrim = dynamicTrimRendererClass.getMethod(
               "renderTrim", armorMaterialClass, poseStackClass, multiBufferSourceClass, int.class, armorTrimClass, humanoidModelClass, boolean.class
            );
            available = true;
            LOGGER.info("[ChainedCurios] AllTheTrims DynamicRenderer compatibility layer initialized successfully.");
         } catch (Exception var6) {
            LOGGER.error("[ChainedCurios] Failed to initialize AllTheTrims DynamicRenderer compatibility layer.", var6);
            available = false;
         }
      }
   }

   public static boolean tryRender(
      PoseStack poseStack, MultiBufferSource bufferSource, int light, HumanoidModel<?> model, ArmorMaterial material, ArmorTrim trim, boolean leggings
   ) {
      initialize();
      if (!available) {
         return false;
      } else {
         TextureAtlasSprite sprite = Minecraft.getInstance()
            .getTextureAtlas(Sheets.ARMOR_TRIMS_SHEET)
            .apply(leggings ? trim.innerTexture(material) : trim.outerTexture(material));
         if (sprite.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            try {
               if (!hasLoggedSuccess) {
                  LOGGER.info("[ChainedCurios] Detected AllTheTrims item via missing sprite. Rendering with dynamic renderer.");
                  hasLoggedSuccess = true;
               }

               dynamicTrimRenderer_renderTrim.invoke(null, material, poseStack, bufferSource, light, trim, model, leggings);
               return true;
            } catch (Exception var9) {
               LOGGER.error("[ChainedCurios] Failed to render AllTheTrims trim via reflection. Disabling for this session.", var9);
               available = false;
            }
         }

         return false;
      }
   }
}
