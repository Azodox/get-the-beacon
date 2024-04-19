package fr.azodox.gtb.util.nbt

import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Material
import org.bukkit.block.Beacon
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*

abstract class NBTBlock(val nbtTag: CompoundTag) {
    val offset: Vector
        get() {
            val compound = this.nbtTag
            val pos = compound.getIntArray("Pos")
            return Vector(pos[0], pos[1], pos[2])
        }

    abstract fun setData(state: BlockState)

    abstract val isEmpty: Boolean
}

class NBTBeaconBlock(nbtTag: CompoundTag): NBTBlock(nbtTag){
    override fun setData(state: BlockState) {
        val beacon = state as Beacon
        nbtTag["CustomName"]?.let {
            beacon.customName(Component.text(it.asString))
        }
        nbtTag["Lock"]?.let {
            beacon.setLock(it.asString)
        }
        nbtTag["Primary"]?.let {
            beacon.setPrimaryEffect(PotionEffectType.getById(nbtTag.getInt("Primary")))
        }
        nbtTag["Secondary"]?.let {
            beacon.setSecondaryEffect(PotionEffectType.getById(nbtTag.getInt("Secondary")))
        }
    }

    override val isEmpty: Boolean
        get() = true

}

class NBTChestBlock(nbtTag: CompoundTag) : NBTBlock(nbtTag) {
    private val allItems: MutableMap<Int, ItemStack> = HashMap()

    override fun setData(state: BlockState) {
        val chest = state as Chest
        for (location in allItems.keys) {
            chest.snapshotInventory.setItem(location, allItems[location])
        }
    }

    override val isEmpty: Boolean
        get() {
            return items.isEmpty()
        }

    val items: Map<Int, ItemStack>
        /**
         * @return a map, with the key as the slot and the value as the item
         */
        get() {
            if (allItems.isNotEmpty()) return allItems

            val compound = this.nbtTag
            if (compound.getString("Id") == "minecraft:chest" && compound["Items"] != null) {
                val items = compound["Items"] as ListTag?
                for (i in items!!.indices) {
                    val anItem = items.getCompound(i)
                    val mat = Material.valueOf(
                        anItem.getString("id").replace("minecraft:", "").uppercase(Locale.getDefault())
                    )
                    val item = ItemStack(mat, anItem.getInt("Count"))
                    allItems[anItem.getInt("Slot")] = item
                }
            }
            return allItems
        }
}