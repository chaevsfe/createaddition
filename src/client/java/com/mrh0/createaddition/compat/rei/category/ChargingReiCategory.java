package com.mrh0.createaddition.compat.rei.category;

import com.mrh0.createaddition.compat.rei.CAReiCategories;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.util.Util;
import dev.chaevsfe.createreiviewer.client.widget.Panel;
import dev.chaevsfe.createreiviewer.display.CreateReiDisplay;
import net.minecraft.network.chat.Component;

public class ChargingReiCategory extends MachineReiCategory {
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    public ChargingReiCategory() {
        super(CAReiCategories.CHARGING, "charging", CABlocks.TESLA_COIL);
    }

    @Override
    protected void build(CreateReiDisplay display, Panel panel) {
        super.build(display, panel);
        panel.text(Component.literal(Util.format(display.duration()) + "\u26A1"), 86, 9, TEXT_COLOR);
    }
}
