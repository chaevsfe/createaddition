package com.mrh0.createaddition.ponder;

import com.simibubi.create.AllFluids;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class DebugScenes {
    public static void controllerBEDebug(SceneBuilder builder, SceneBuildingUtil util){
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("debug.fluidtank_controller_be_issue", "Debug Fluid Tank Controller BE");
        scene.configureBasePlate(0, 0, 3);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);

        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(AllFluids.CHOCOLATE, 5000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(2, 2, 2), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(AllFluids.CHOCOLATE, 5000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
        scene.world().modifyBlockEntity(util.grid().at(0, 3, 0), FluidTankBlockEntity.class,
                be -> be.getControllerBE().getTankInventory().fill(new FluidStack(AllFluids.CHOCOLATE, 5000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(20);
    }
}
