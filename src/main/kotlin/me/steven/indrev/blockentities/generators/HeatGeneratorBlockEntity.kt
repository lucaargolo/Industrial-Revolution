package me.steven.indrev.blockentities.generators

import me.steven.indrev.components.InventoryController
import me.steven.indrev.components.Property
import me.steven.indrev.components.TemperatureController
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.items.IRCoolerItem
import me.steven.indrev.items.rechargeable.IRRechargeableItem
import me.steven.indrev.registry.MachineRegistry
import me.steven.indrev.utils.Tier
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.ArrayPropertyDelegate
import net.minecraft.util.math.Direction

class HeatGeneratorBlockEntity(tier: Tier) : GeneratorBlockEntity(tier, MachineRegistry.HEAT_GENERATOR_REGISTRY) {
    init {
        this.propertyDelegate = ArrayPropertyDelegate(5)
        this.inventoryController = InventoryController {
            DefaultSidedInventory(2, intArrayOf(2), intArrayOf()) { slot, stack ->
                val item = stack?.item
                when {
                    item is IRRechargeableItem && item.canOutput -> slot == 0
                    item is IRCoolerItem -> slot == 1
                    else -> false
                }
            }
        }
        this.temperatureController = TemperatureController(
            { this },
            2.3,
            { if (burnTime > 0 && stableTemperature > 0) stableTemperature.toDouble() else this.temperatureController!!.explosionLimit },
            7000..9000,
            10000.0
        )
    }

    private var stableTemperature: Int = 0
    private var burnTime: Int by Property(3, 0)
    private var maxBurnTime: Int by Property(4, 0)

    override fun shouldGenerate(): Boolean {
        if (burnTime > 0) burnTime--
        else if (maxStoredPower > energy) {
            for (direction in Direction.values()) {
                val sidePos = pos.offset(direction)
                val blockState = world?.getBlockState(sidePos)
                if (TEMPERATURE_MAP.containsKey(blockState?.block)) {
                    stableTemperature = TEMPERATURE_MAP[blockState!!.block] ?: return false
                    burnTime = 1600
                    maxBurnTime = burnTime
                    world?.setBlockState(sidePos, Blocks.AIR.defaultState, 3)
                    break
                }
            }
        }
        markDirty()
        return burnTime > 0 && energy < maxStoredPower
    }

    override fun getGenerationRatio(): Double = 64.0 * (if (temperatureController?.isFullEfficiency() == true) stableTemperature / 1000 else 1)

    override fun fromTag(state: BlockState?, tag: CompoundTag?) {
        super.fromTag(state, tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        stableTemperature = tag?.getInt("StableTemperature") ?: 0
    }

    override fun toTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        tag?.putInt("StableTemperature", stableTemperature)
        return super.toTag(tag)
    }

    override fun fromClientTag(tag: CompoundTag?) {
        super.fromClientTag(tag)
        burnTime = tag?.getInt("BurnTime") ?: 0
        maxBurnTime = tag?.getInt("MaxBurnTime") ?: 0
        stableTemperature = tag?.getInt("StableTemperature") ?: 0
    }

    override fun toClientTag(tag: CompoundTag?): CompoundTag {
        tag?.putInt("BurnTime", burnTime)
        tag?.putInt("MaxBurnTime", maxBurnTime)
        tag?.putInt("StableTemperature", stableTemperature)
        return super.toClientTag(tag)
    }

    companion object {
        private val TEMPERATURE_MAP = mutableMapOf<Block, Int>().also {
            it[Blocks.LAVA] = 5500
        }
    }
}