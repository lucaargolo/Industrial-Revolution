package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WSlider
import io.github.cottonmc.cotton.gui.widget.WSprite
import io.github.cottonmc.cotton.gui.widget.data.Axis
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class FarmerController(
    syncId: Int,
    playerInventory: PlayerInventory,
    ctx: ScreenHandlerContext
) :
    IRGuiController(
        IndustrialRevolution.FARMER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    private var value = -1
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.farmer", ctx, playerInventory, blockInventory, propertyDelegate)

        val inputFrame = WSprite(identifier("textures/gui/input_frame.png"))
        root.add(inputFrame, 1.4, 0.7)
        inputFrame.setSize(40, 44)

        val outputFrame = WSprite(identifier("textures/gui/output_frame.png"))
        root.add(outputFrame, 4.1, 0.7)
        outputFrame.setSize(58, 62)

        val outputSlot = WTooltipedItemSlot.of(blockInventory, (blockInventory as IRInventory).outputSlots.first(), 3, 3, TranslatableText("gui.indrev.output_slot_type"))
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 4.2, 1.0)
        val inputSlot = WTooltipedItemSlot.of(blockInventory, (blockInventory as IRInventory).inputSlots.first(), 2, 2, TranslatableText("gui.indrev.farmer_input_slot_type"))
        root.add(inputSlot, 1.5, 1.0)

        val slider = WSlider(1, 10, Axis.HORIZONTAL)
        root.add(slider, 1.4, 4.0)
        slider.setSize(35, 20)
        ctx.run { world, pos ->
            val blockEntity = world.getBlockEntity(pos) as? AOEMachineBlockEntity<*> ?: return@run
            slider.value = blockEntity.range
        }
        slider.setValueChangeListener { newValue -> this.value = newValue }

        val text = WText({
            TranslatableText("block.indrev.aoe.range", slider.value)
        })
        root.add(text, 2.0, 3.7)

        root.validate(this)
    }

    override fun close(player: PlayerEntity?) {
        super.close(player)
        AOEMachineBlockEntity.sendValueUpdatePacket(value, ctx)
    }

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 1

    companion object {
        val SCREEN_ID = identifier("farmer_screen")
    }
}