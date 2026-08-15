package com.mrh0.createaddition;

import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAFluids;
import com.zurrtum.create.api.registry.CreateRegisterPlugin;

public final class CreateAdditionPlugin implements CreateRegisterPlugin {
    private static boolean blocksRegistered;
    private static boolean fluidsRegistered;

    @Override
    public void onBlockRegister() {
        if (blocksRegistered) {
            throw new IllegalStateException("Create Fly invoked Crafts & Additions block registration more than once");
        }
        CABlocks.register();
        CAFluids.registerFluidBlocks();
        blocksRegistered = true;
    }

    @Override
    public void onFluidRegister() {
        if (fluidsRegistered) {
            throw new IllegalStateException("Create Fly invoked Crafts & Additions fluid registration more than once");
        }
        fluidsRegistered = true;
        CAFluids.register();
    }

    public static void verifyEarlyRegistrationComplete() {
        if (!blocksRegistered || !fluidsRegistered) {
            throw new IllegalStateException(
                "Create Fly did not invoke Crafts & Additions early registration (blocks=" + blocksRegistered
                    + ", fluids=" + fluidsRegistered + ")"
            );
        }
    }
}
