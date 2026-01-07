package com.noem1s.chainedcurios.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.resources.ResourceLocation;

public class ChainedCuriosRenderTypes extends RenderType {
   private ChainedCuriosRenderTypes(
      String p_173178_, VertexFormat p_173179_, Mode p_173180_, int p_173181_, boolean p_173182_, boolean p_173183_, Runnable p_173184_, Runnable p_173185_
   ) {
      super(p_173178_, p_173179_, p_173180_, p_173181_, p_173182_, p_173183_, p_173184_, p_173185_);
   }

   public static RenderType armorCutoutWithCull(ResourceLocation location) {
      CompositeState state = CompositeState.builder()
         .setShaderState(RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
         .setTextureState(new TextureStateShard(location, false, false))
         .setTransparencyState(NO_TRANSPARENCY)
         .setCullState(CULL)
         .setLightmapState(LIGHTMAP)
         .setOverlayState(OVERLAY)
         .setLayeringState(VIEW_OFFSET_Z_LAYERING)
         .createCompositeState(true);
      return create("armor_cutout_with_cull", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, true, state);
   }
}
