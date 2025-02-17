package org.valkyrienskies.mod.common.item

import net.minecraft.Util
import net.minecraft.network.chat.TextComponent
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Rotation.NONE
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3i
import org.valkyrienskies.core.game.ChunkAllocator
import org.valkyrienskies.mod.common.dimensionId
import org.valkyrienskies.mod.common.shipObjectWorld
import org.valkyrienskies.mod.common.util.toBlockPos
import org.valkyrienskies.mod.common.util.toJOML
import org.valkyrienskies.mod.util.relocateBlock

class ShipCreatorItem(properties: Properties, private val scale: Double) : Item(properties) {
    override fun isFoil(stack: ItemStack?): Boolean {
        return true
    }

    override fun useOn(ctx: UseOnContext): InteractionResult {
        val level = ctx.level as? ServerLevel ?: return super.useOn(ctx)
        val blockPos = ctx.clickedPos
        val blockState: BlockState = level.getBlockState(blockPos)

        if (!level.isClientSide) {
            if (ChunkAllocator.isChunkInShipyard(blockPos.x shr 4, blockPos.z shr 4)) {
                ctx.player?.sendMessage(TextComponent("That chunk is already part of a ship!"), Util.NIL_UUID)
            } else if (!blockState.isAir) {
                // Make a ship
                val dimensionId = level.dimensionId
                val shipData = level.shipObjectWorld.createNewShipAtBlock(blockPos.toJOML(), false, scale, dimensionId)

                val centerPos = shipData.chunkClaim.getCenterBlockCoordinates(Vector3i()).toBlockPos()

                // Move the block from the world to a ship
                level.relocateBlock(blockPos, centerPos, true, shipData, NONE)

                ctx.player?.sendMessage(TextComponent("SHIPIFIED!"), Util.NIL_UUID)
            }
        }

        return super.useOn(ctx)
    }
}
