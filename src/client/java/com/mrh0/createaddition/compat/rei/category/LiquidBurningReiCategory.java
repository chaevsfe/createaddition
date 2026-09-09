package com.mrh0.createaddition.compat.rei.category;

import com.mrh0.createaddition.compat.rei.CAReiCategories;
import com.mrh0.createaddition.index.CAItems;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import dev.chaevsfe.createreiviewer.client.category.CreateReiCategory;
import dev.chaevsfe.createreiviewer.client.widget.CreateReiLayout;
import dev.chaevsfe.createreiviewer.client.widget.Panel;
import dev.chaevsfe.createreiviewer.client.widget.TwoItemRenderer;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.chat.Component;

import java.util.List;

public class LiquidBurningReiCategory extends CreateReiCategory<CreateReiDisplay> {
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public LiquidBurningReiCategory() {
        super(CAReiCategories.titleKey("liquid_burning"));
    }

    @Override
    public CategoryIdentifier<? extends CreateReiDisplay> getCategoryIdentifier() {
        return CAReiCategories.LIQUID_BURNING;
    }

    @Override
    public Renderer getIcon() {
        return new TwoItemRenderer(AllItems.BLAZE_BURNER, CAItems.STRAW);
    }

    @Override
    protected int contentHeight() {
        return 53;
    }

    @Override
    protected void build(CreateReiDisplay display, Panel panel) {
        HeatCondition heat = CreateReiLayout.heatOf(display);
        List<EntryIngredient> inputs = display.inputs();
        for (int i = 0; i < Math.min(3, inputs.size()); i++) {
            panel.slot(32 + 20 * i, 3, inputs.get(i));
        }
        panel.texture(AllGuiTextures.JEI_HEAT_BAR, 4, 30);
        panel.texture(AllGuiTextures.JEI_LIGHT, 81, 38);
        CreateReiLayout.blazeBurner(panel, heat, 91, 14);
        panel.text(Component.translatable(heat.getTranslationKey()), 9, 36, heat.getColor());
        panel.text(Component.literal(Util.formatTime(display.duration())), 136, 36, TEXT_COLOR);
    }
}
