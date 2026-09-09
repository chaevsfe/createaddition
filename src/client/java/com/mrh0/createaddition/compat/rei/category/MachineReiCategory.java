package com.mrh0.createaddition.compat.rei.category;

import com.mrh0.createaddition.compat.rei.CAReiCategories;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import dev.chaevsfe.createreiviewer.client.category.CreateReiCategory;
import dev.chaevsfe.createreiviewer.client.widget.CreateReiLayout;
import dev.chaevsfe.createreiviewer.client.widget.OneItemRenderer;
import dev.chaevsfe.createreiviewer.client.widget.Panel;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import net.minecraft.world.level.block.Block;

public abstract class MachineReiCategory extends CreateReiCategory<CreateReiDisplay> {
    private final CategoryIdentifier<CreateReiDisplay> identifier;
    private final Block machine;

    protected MachineReiCategory(CategoryIdentifier<CreateReiDisplay> identifier, String path, Block machine) {
        super(CAReiCategories.titleKey(path));
        this.identifier = identifier;
        this.machine = machine;
    }

    @Override
    public CategoryIdentifier<? extends CreateReiDisplay> getCategoryIdentifier() {
        return identifier;
    }

    @Override
    public Renderer getIcon() {
        return new OneItemRenderer(machine);
    }

    @Override
    protected int contentHeight() {
        return 53;
    }

    @Override
    protected void build(CreateReiDisplay display, Panel panel) {
        panel.texture(AllGuiTextures.JEI_ARROW, 85, 32);
        panel.texture(AllGuiTextures.JEI_DOWN_ARROW, 43, 4);
        panel.texture(AllGuiTextures.JEI_SHADOW, 32, 40);
        panel.blockPip(42, 19, machine.defaultBlockState());
        if (!display.inputs().isEmpty()) {
            panel.slot(15, 9, display.inputs().get(0));
        }
        CreateReiLayout.outputGrid(panel, display, 139, 27);
    }
}
