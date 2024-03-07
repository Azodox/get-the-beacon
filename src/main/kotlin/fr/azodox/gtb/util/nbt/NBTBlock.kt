package fr.azodox.gtb.util.nbt

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Material
import org.bukkit.block.Beacon
import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.Sign
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
            if (compound.getString("Id") == "minecraft:chest") {
                if (compound["Items"] != null) {
                    val items = compound["Items"] as ListTag?
                    for (i in items!!.indices) {
                        val anItem = items.getCompound(i)
                        val mat = Material.valueOf(anItem.getString("id").replace("minecraft:", "").uppercase(Locale.getDefault()))
                        val item = ItemStack(mat, anItem.getInt("Count"))
                        allItems[anItem.getInt("Slot")] = item
                    }
                }
            }
            return allItems
        }
}

class NBTSignBlock(nbtTag: CompoundTag) : NBTBlock(nbtTag) {
    private val lines: Map<Position, String> = mapOf()

    override fun setData(state: BlockState) {
        val sign = state as Sign
        var current = 0
        for (line in this.getLines()) {
            if (line != null) {
                sign.setLine(current, line)
            }
            current++
        }
    }

    override val isEmpty: Boolean
        get() {
            return getLines().isEmpty()
        }

    /**
     * @param position - position of text to read from
     * @return text at the specified position on the sign
     */
    fun getLine(position: Position): String? {
        if (lines.containsKey(position)) {
            return lines[position]
        }

        val compound = this.nbtTag
        if (compound.getString("Id") == "minecraft:sign") {
            val s1 = compound.getString(position.id)
            val jsonObject = Gson().fromJson(s1, JsonObject::class.java)
            if (jsonObject["extra"] != null) {
                val array = jsonObject["extra"].asJsonArray
                return array[0].asJsonObject["text"].asString
            }
        }
        return null
    }

    fun getLines(): List<String?> {
        val lines: MutableList<String?> = ArrayList()
        for (position in Position.entries) {
            lines.add(getLine(position))
        }
        return lines
    }

    /**
     * Utility class for NBT sign positions
     * @author SamB440
     */
    enum class Position(val id: String) {
        TEXT_ONE("Text1"),
        TEXT_TWO("Text2"),
        TEXT_THREE("Text3"),
        TEXT_FOUR("Text4")
    }
}