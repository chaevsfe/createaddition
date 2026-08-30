package com.mrh0.createaddition.config;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.catnip.config.ConfigBase;

public class CACommonConfig extends ConfigBase {
    public static CACommonConfig COMMON;

    public final ConfigGroup general = group(0, "general", "General Settings");
    public final ConfigInt FE_RPM = i(480, 0, Integer.MAX_VALUE, "fe_at_max_rpm", "Forge Energy conversion rate (in FE/t at 256 RPM, value is the FE/t generated and consumed is at 256rpm).");
    public final ConfigInt MAX_STRESS = i(16384, 0, Integer.MAX_VALUE, "max_stress", "Max stress for the Alternator and Electric Motor (in SU at 256 RPM).");
    public final ConfigBool AUDIO_ENABLED = b(true, "audio_enabled", "If audio should be enabled or not.");

    public final ConfigGroup electricMotor = group(0, "electric_motor", "Electric Motor");
    public final ConfigInt ELECTRIC_MOTOR_RPM_RANGE = i(256, 1, Integer.MAX_VALUE, "motor_rpm_range", "Electric Motor min/max RPM.");
    public final ConfigInt ELECTRIC_MOTOR_MINIMUM_CONSUMPTION = i(8, 0, Integer.MAX_VALUE, "motor_min_consumption", "Electric Motor minimum required energy consumption in FE/t.");
    public final ConfigInt ELECTRIC_MOTOR_MAX_INPUT = i(5000, 0, Integer.MAX_VALUE, "motor_max_input", "Electric Motor max input in FE (Energy transfer not consumption).");
    public final ConfigInt ELECTRIC_MOTOR_CAPACITY = i(5000, 0, Integer.MAX_VALUE, "motor_capacity", "Electric Motor internal capacity in FE.");

    public final ConfigGroup alternator = group(0, "alternator", "Alternator");
    public final ConfigInt ALTERNATOR_MAX_OUTPUT = i(5000, 0, Integer.MAX_VALUE, "generator_max_output", "Alternator max input in FE (Energy transfer, not generation).");
    public final ConfigInt ALTERNATOR_CAPACITY = i(5000, 0, Integer.MAX_VALUE, "generator_capacity", "Alternator internal capacity in FE.");
    public final ConfigFloat ALTERNATOR_EFFICIENCY = f(0.75f, 0.01f, 1.0f, "generator_efficiency", "Alternator efficiency relative to base conversion rate.");

    public final ConfigGroup rollingMill = group(0, "rolling_mill", "Rolling Mill");
    public final ConfigInt ROLLING_MILL_PROCESSING_DURATION = i(120, 0, Integer.MAX_VALUE, "rolling_mill_processing_duration", "Rolling Mill duration in ticks.");
    public final ConfigInt ROLLING_MILL_STRESS = i(8, 0, 1024, "rolling_mill_stress", "Rolling Mill base stress impact.");

    public final ConfigGroup wires = group(0, "wires", "Wires");
    public final ConfigInt CONNECTOR_NETWORK_INTERNAL_BUFFER = i(80000, 80000, Integer.MAX_VALUE, "connector_network_internal_buffer", "The maximum stored amount in the connector network internal buffer.");
    public final ConfigInt SMALL_CONNECTOR_MAX_INPUT = i(1000, 0, Integer.MAX_VALUE, "small_connector_max_input", "Small Connector max input in FE/t (Energy transfer).");
    public final ConfigInt SMALL_CONNECTOR_MAX_OUTPUT = i(1000, 0, Integer.MAX_VALUE, "small_connector_max_output", "Small Connector max output in FE/t (Energy transfer).");
    public final ConfigInt SMALL_CONNECTOR_MAX_LENGTH = i(16, 0, 256, "small_connector_wire_length", "Small Connector max wire length in blocks.");
    public final ConfigInt SMALL_LIGHT_CONNECTOR_CONSUMPTION = i(1, 0, Integer.MAX_VALUE, "small_light_connector_consumption", "Small Connector With Light energy consumption in FE/t.");
    public final ConfigInt LARGE_CONNECTOR_MAX_INPUT = i(5000, 0, Integer.MAX_VALUE, "large_connector_max_input", "Large Connector max input in FE/t (Energy transfer).");
    public final ConfigInt LARGE_CONNECTOR_MAX_OUTPUT = i(5000, 0, Integer.MAX_VALUE, "large_connector_max_output", "Large Connector max output in FE/t (Energy transfer).");
    public final ConfigInt LARGE_CONNECTOR_MAX_LENGTH = i(32, 0, 256, "large_connector_wire_length", "Large Connector max wire length in blocks.");
    public final ConfigBool CONNECTOR_IGNORE_FACE_CHECK = b(true, "connector_ignore_face_check", "Ignore checking if block face can support connector.");
    public final ConfigBool CONNECTOR_ALLOW_PASSIVE_IO = b(true, "connector_allow_passive_io", "Allows blocks attached to a connector to freely pass energy to and from the connector network.");

    public final ConfigGroup accumulator = group(0, "accumulator", "Accumulator");
    public final ConfigInt ACCUMULATOR_MAX_INPUT = i(5000, 0, Integer.MAX_VALUE, "accumulator_max_input", "Accumulator max input in FE/t (Energy transfer).");
    public final ConfigInt ACCUMULATOR_MAX_OUTPUT = i(5000, 0, Integer.MAX_VALUE, "accumulator_max_output", "Accumulator max output in FE/t (Energy transfer).");
    public final ConfigInt ACCUMULATOR_CAPACITY = i(2_000_000, 0, Integer.MAX_VALUE, "accumulator_capacity", "Accumulator internal capacity per block in FE.");
    public final ConfigInt ACCUMULATOR_MAX_HEIGHT = i(5, 1, 8, "accumulator_max_height", "Accumulator max multiblock height.");
    public final ConfigInt ACCUMULATOR_MAX_WIDTH = i(3, 1, 8, "accumulator_max_width", "Accumulator max multiblock width.");

    public final ConfigGroup pei = group(0, "portable_energy_interface", "Portable Energy Interface");
    public final ConfigInt PEI_MAX_INPUT = i(5000, 0, Integer.MAX_VALUE, "pei_max_input", "PEI max input in FE/t (Energy transfer).");
    public final ConfigInt PEI_MAX_OUTPUT = i(5000, 0, Integer.MAX_VALUE, "pei_max_output", "PEI max output in FE/t (Energy transfer).");

    public final ConfigGroup teslaCoil = group(0, "tesla_coil", "Tesla Coil");
    public final ConfigInt TESLA_COIL_MAX_INPUT = i(10000, 0, Integer.MAX_VALUE, "tesla_coil_max_input", "Tesla Coil max input in FE/t (Energy transfer).");
    public final ConfigInt TESLA_COIL_CHARGE_RATE = i(5000, 0, Integer.MAX_VALUE, "tesla_coil_charge_rate", "Tesla Coil charge rate in FE/t.");
    public final ConfigInt TESLA_COIL_RECIPE_CHARGE_RATE = i(2000, 0, Integer.MAX_VALUE, "tesla_coil_recipe_charge_rate", "Tesla Coil charge rate in FE/t for recipes.");
    public final ConfigInt TESLA_COIL_CAPACITY = i(40_000, 0, Integer.MAX_VALUE, "tesla_coil_capacity", "Tesla Coil internal capacity in FE.");
    public final ConfigInt TESLA_COIL_HURT_ENERGY_REQUIRED = i(1000, 0, Integer.MAX_VALUE, "tesla_coil_hurt_energy_required", "Energy consumed when Tesla Coil is fired (in FE).");
    public final ConfigInt TESLA_COIL_HURT_RANGE = i(3, 0, Integer.MAX_VALUE, "tesla_coil_hurt_range", "Hurt range (in blocks/meters).");
    public final ConfigInt TESLA_COIL_HURT_DMG_MOB = i(3, 0, Integer.MAX_VALUE, "tesla_coil_hurt_mob", "Damaged dealt to mobs when Tesla Coil is fired (in half hearts).");
    public final ConfigInt TESLA_COIL_HURT_EFFECT_TIME_MOB = i(20, 0, Integer.MAX_VALUE, "tesla_coil_effect_time_mob", "The duration of the Shocked effect for mobs (in ticks).");
    public final ConfigInt TESLA_COIL_HURT_DMG_PLAYER = i(2, 0, Integer.MAX_VALUE, "tesla_coil_hurt_player", "Damaged dealt to players when Tesla Coil is fired (in half hearts).");
    public final ConfigInt TESLA_COIL_HURT_EFFECT_TIME_PLAYER = i(20, 0, Integer.MAX_VALUE, "tesla_coil_effect_time_player", "The duration of the Shocked effect for players (in ticks).");
    public final ConfigInt TESLA_COIL_HURT_FIRE_COOLDOWN = i(20, 1, Integer.MAX_VALUE, "tesla_coil_fire_cooldown", "Tesla Coil fire interval (in ticks).");

    public final ConfigGroup liquidBlazeBurner = group(0, "liquid_blaze_burner", "Liquid Blaze Burner");
    public final ConfigInt LIQUID_BLAZE_BURNER_MAX_LIQUID_CAPACITY = i(4000, 100, Integer.MAX_VALUE, "liquid_blaze_burner_max_liquid_capacity", "Liquid Blaze Burner internal liquid storage capacity (in mB). A value less than 1000 prevents players from refilling with a bucket.");
    public final ConfigInt LIQUID_BLAZE_BURNER_MAX_HEAT_CAPACITY = i(10000, 0, Integer.MAX_VALUE, "liquid_blaze_burner_max_heat_capacity", "Liquid Blaze Burner internal heat capacity (in ticks).");

    public final ConfigGroup misc = group(0, "misc", "Misc");
    public final ConfigInt DIAMOND_GRIT_SANDPAPER_USES = i(1024, 3, Integer.MAX_VALUE, "diamond_grit_sandpaper_uses", "Diamond Grit Sandpaper durability (number of uses).");
    public final ConfigFloat BARBED_WIRE_DAMAGE = f(2f, 0f, Float.MAX_VALUE, "barbed_wire_damage", "Barbed Wire Damage.");
    public final ConfigBool AMULET_EFFECT_ENABLED = b(true, "amulet_effect_enabled", "If the effects of the amulets should be enabled or not.");
    public final ConfigInt ELECTRUM_AMULET_CHARGE_RATE = i(2, 0, Integer.MAX_VALUE, "electrum_amulet_charge_rate", "Passive charge rate of the Electrum Amulet in average FE/t when held in main or offhand.");
    public final ConfigInt CAPACITOR_CAPACITY = i(5000, 1, Integer.MAX_VALUE, "capacitor_capacity", "Maximum energy the Capacitor item can store in FE.");
    public final ConfigInt CAPACITOR_CHARGE_RATE = i(500, 1, Integer.MAX_VALUE, "capacitor_charge_rate", "Max FE per transfer operation for the Capacitor item.");

    @Override
    public String getName() {
        return "common";
    }

    public static void register() {
        COMMON = Builder.create(CACommonConfig::new, "createaddition", "common", true);
    }
}
