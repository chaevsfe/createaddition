package com.mrh0.createaddition.compat.viewer;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlock;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.chaevsfe.createreiviewer.api.ViewerRecipe;
import dev.chaevsfe.createreiviewer.api.client.CreateViewerCategories;
import dev.chaevsfe.createreiviewer.api.client.CreateViewerClientPlugin;
import dev.chaevsfe.createreiviewer.api.client.ViewerCanvas;
import dev.chaevsfe.createreiviewer.api.client.ViewerCategory;
import dev.chaevsfe.createreiviewer.api.client.ViewerCategoryRegistry;
import dev.chaevsfe.createreiviewer.api.client.ViewerLayouts;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class CAViewerClientPlugin implements CreateViewerClientPlugin {
    private static final int HEIGHT = 53;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    @Override
    public void registerCategories(ViewerCategoryRegistry registry) {
        BlockState teslaCoil = CABlocks.TESLA_COIL.defaultBlockState()
            .setValue(TeslaCoilBlock.FACING, Direction.DOWN)
            .setValue(TeslaCoilBlock.POWERED, true);
        BlockState rollingMill = CABlocks.ROLLING_MILL.defaultBlockState();
        registry.add(ViewerCategory.builder(CAViewerPlugin.CHARGING)
            .title(titleKey("charging"))
            .icon(CABlocks.TESLA_COIL)
            .height(HEIGHT)
            .workstations(CABlocks.TESLA_COIL)
            .layout((recipe, canvas) -> {
                machine(recipe, canvas, teslaCoil);
                canvas.text(Component.literal(Util.format(recipe.duration()) + "⚡"), 86, 9, TEXT_COLOR, true);
            })
            .build());
        registry.add(ViewerCategory.builder(CAViewerPlugin.ROLLING)
            .title(titleKey("rolling"))
            .icon(CABlocks.ROLLING_MILL)
            .height(HEIGHT)
            .workstations(CABlocks.ROLLING_MILL)
            .layout((recipe, canvas) -> machine(recipe, canvas, rollingMill))
            .build());
        registry.add(ViewerCategory.builder(CAViewerPlugin.LIQUID_BURNING)
            .title(titleKey("liquid_burning"))
            .icon(AllItems.BLAZE_BURNER, CAItems.STRAW)
            .height(HEIGHT)
            .workstations(AllItems.BLAZE_BURNER)
            .layout(CAViewerClientPlugin::liquidBurning)
            .build());
        registry.addWorkstations(CreateViewerCategories.SANDPAPER_POLISHING, CAItems.DIAMOND_GRIT_SANDPAPER);
    }

    private static String titleKey(String path) {
        return CreateAddition.MODID + ".recipe." + path;
    }

    private static void machine(ViewerRecipe recipe, ViewerCanvas canvas, BlockState machine) {
        canvas.texture(AllGuiTextures.JEI_ARROW, 85, 32);
        canvas.texture(AllGuiTextures.JEI_DOWN_ARROW, 43, 4);
        canvas.texture(AllGuiTextures.JEI_SHADOW, 32, 40);
        canvas.blockPip(42, 19, machine);
        if (!recipe.inputs().isEmpty()) {
            canvas.slot(15, 9, recipe.input(0));
        }
        ViewerLayouts.outputGrid(canvas, recipe, 139, 27);
    }

    private static void liquidBurning(ViewerRecipe recipe, ViewerCanvas canvas) {
        HeatCondition heat = recipe.heat();
        for (int i = 0; i < Math.min(3, recipe.inputs().size()); i++) {
            canvas.slot(32 + 20 * i, 3, recipe.input(i));
        }
        canvas.texture(AllGuiTextures.JEI_HEAT_BAR, 4, 30);
        canvas.texture(AllGuiTextures.JEI_LIGHT, 81, 38);
        ViewerLayouts.blazeBurner(canvas, heat, 91, 14);
        canvas.texture(AllGuiTextures.JEI_DOWN_ARROW, 91, 8);
        canvas.text(ViewerLayouts.heatLabel(heat), 9, 36, heat.getColor(), true);
        canvas.text(Component.literal(Util.formatTime(recipe.duration())), 136, 36, TEXT_COLOR, true);
    }
}
