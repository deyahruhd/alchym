package jard.alchym.client.render.model;

import com.google.common.base.Charsets;
import com.mojang.datafixers.util.Pair;
import jard.alchym.AlchymReference;
import jard.alchym.api.ingredient.SolutionGroup;
import jard.alchym.fluids.MaterialFluid;
import jard.alchym.items.ChymicalFlaskItem;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.Resource;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/***
 *  ChymicalFlaskBakedModel
 *  Dynamically generates the chymical flask model depending on its containing {@link SolutionGroup}.
 *
 *  Created by jard at 22:34 on February, 10, 2021.
 ***/
public class ChymicalFlaskBakedModel implements UnbakedModel, BakedModel, FabricBakedModel {
    private static final Map <AlchymReference.Materials, FabricBakedModel> SOLUTION_LAYERS = new HashMap<> ();
    private static final Map <AlchymReference.Items, FabricBakedModel> FLASK_BASES = new HashMap<> ();

    public ChymicalFlaskBakedModel () {
    }

    @Override
    public void emitItemQuads (ItemStack itemStack, Supplier<Random> supplier, RenderContext renderContext) {
        FLASK_BASES.get (AlchymReference.Items.CHYMICAL_FLASK).emitItemQuads (itemStack, supplier, renderContext);

        Fluid solvent = ChymicalFlaskItem.getSolvent (itemStack);
        if (solvent != null && solvent instanceof MaterialFluid)
            SOLUTION_LAYERS.get (((MaterialFluid) solvent).material).emitItemQuads (itemStack, supplier, renderContext);
    }

    @Override
    public void emitBlockQuads (BlockRenderView blockRenderView, BlockState blockState, BlockPos blockPos, Supplier<Random> supplier, RenderContext renderContext) {
        // No-op
    }

    @Override
    public boolean isVanillaAdapter () {
        return false;
    }

    @Override
    public List<BakedQuad> getQuads (BlockState blockState, Direction direction, Random random) {
        return Collections.emptyList ();
    }

    @Override
    public boolean useAmbientOcclusion () {
        return false;
    }

    @Override
    public boolean hasDepth () {
        return false;
    }

    @Override
    public boolean isSideLit () {
        return false;
    }

    @Override
    public boolean isBuiltin () {
        return false;
    }

    @Override
    public Sprite getSprite () {
        return MinecraftClient.getInstance ().getSpriteAtlas (PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).apply (new Identifier ("missingno"));
    }

    @Override
    public ModelTransformation getTransformation () {
        try {
            Resource baseItemModel = MinecraftClient.getInstance ().getResourceManager ()
                    .getResource (new Identifier ("minecraft", "models/item/generated.json"));
            return JsonUnbakedModel.deserialize (new BufferedReader (new InputStreamReader (baseItemModel.getInputStream (), Charsets.UTF_8)))
                    .getTransformations ();
        } catch (IOException e) {
            // Should never happen.
            return ModelTransformation.NONE;
        }
    }

    @Override
    public ModelOverrideList getOverrides () {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public Collection<Identifier> getModelDependencies () {
        return Collections.emptyList ();
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies (Function<Identifier, UnbakedModel> function, Set<Pair<String, String>> set) {
        return Collections.emptyList ();
    }

    @Override
    public BakedModel bake (ModelLoader modelLoader, Function<SpriteIdentifier, Sprite> function, ModelBakeSettings modelBakeSettings, Identifier identifier) {
        Identifier chymicalFlaskId = new Identifier (AlchymReference.MODID,
                String.format ("%s_base", AlchymReference.Items.CHYMICAL_FLASK.getName ()));

        FLASK_BASES.put (AlchymReference.Items.CHYMICAL_FLASK,
                (FabricBakedModel) modelLoader.bake (new ModelIdentifier (chymicalFlaskId, "inventory"), ModelRotation.X0_Y0));

        for (AlchymReference.Materials material : AlchymReference.Materials.values ()) {
            if (material.forms.contains (AlchymReference.Materials.Forms.LIQUID)) {
                // Proceed to bake every material layer
                String id = String.format ("%s_flask_layer", material.getName ());
                SOLUTION_LAYERS.put (material,
                        (FabricBakedModel) modelLoader.bake (new ModelIdentifier (new Identifier (AlchymReference.MODID, id), "inventory"), ModelRotation.X0_Y0));
            }
        }

        return this;
    }
}
