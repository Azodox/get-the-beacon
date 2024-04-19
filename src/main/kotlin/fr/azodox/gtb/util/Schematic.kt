package fr.azodox.gtb.util


import fr.azodox.gtb.util.nbt.NBTBlock
import fr.azodox.gtb.util.nbt.NBTMaterial
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.BlockData
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer


/**
 * A utility class that previews and pastes schematics block-by-block with asynchronous support.
 * <br></br>
 * @version 2.0.5
 * @author SamB440 - Schematic previews, centering and pasting block-by-block, class itself
 * @author brainsynder - 1.13+ Palette Schematic Reader
 * @author Math0424 - Rotation calculations
 * @author Jojodmo - Legacy (< 1.12) Schematic Reader
 */
class Schematic(private val plugin: Plugin, private val schematic: File) {

    private var width: Short = 0
    private var height: Short = 0
    private var length: Short = 0

    private lateinit var blockDatas: ByteArray

    val materials: MutableList<Material> = ArrayList()
    private val nbtBlocks: LinkedHashMap<Vector, NBTBlock> = LinkedHashMap<Vector, NBTBlock>()

    /**
     * Returns the palette key from the schematic file.
     * The key is the unique id that corresponds to an index in the BlockData array.
     *
     * Note that the palette does not contain every block present in the schematic,
     * it is only a "preview" of what kind of blocks the schematic contains.
     *
     * If you are looking to read the material count and type from the schematic,
     * use [.getSchematicMaterialData].
     *
     * @see .getMaterials
     * @return a linked hashmap by the unique id of the block in the palette key
     */
    val blocks: LinkedHashMap<Int, BlockData> = LinkedHashMap()

    /**
     * Pastes a schematic, with a specified time
     * @param paster player pasting
     * @param time time in ticks to paste blocks
     * @return collection of locations where schematic blocks will be pasted, null if schematic locations will replace blocks
     * @see .loadSchematic
     */
    fun pasteSchematic(loc: Location): Collection<Location> {
        checkIfSchematicIsLoaded()

        val tracker = Data()

        val indexLocations = LinkedHashMap<Int, Location>()
        val delayedIndexLocations = LinkedHashMap<Int, Location>()

        val nbtData: LinkedHashMap<Int, NBTBlock> = LinkedHashMap()

        /*
         * Loop through all the blocks within schematic size.
         */
        for (x in 0 until width) {
            for (y in 0 until height) {
                for (z in 0 until length) {
                    val index = y * width * length + z * width + x
                    val point = Vector(x, y, z)

                    val location = Location(
                        loc.world,
                        (loc.blockX - z).toDouble(),
                        (y + loc.blockY).toDouble(),
                        ((loc.blockZ - width) + (x + 1)).toDouble()
                    )

                    val blockData = blocks[blockDatas[index].toInt()] ?: continue

                    /*
                     * Ignore blocks that aren't air. Change this if you want the air to destroy blocks too.
                     * Add items to delayedBlocks if you want them placed last, or if they get broken.
                     */
                    val material = blockData.material
                    if (material != Material.AIR) {
                        val nbtMaterial: NBTMaterial = NBTMaterial.fromBukkit(material)
                        if (!nbtMaterial.isDelayed) {
                            indexLocations[index] = location
                        } else {
                            delayedIndexLocations[index] = location
                        }
                    }

                    if (nbtBlocks.containsKey(point)) {
                        nbtData[index] = nbtBlocks[point] ?: continue
                    }
                }
            }
        }

        // Make sure delayed blocks are placed last
        indexLocations.putAll(delayedIndexLocations)
        delayedIndexLocations.clear()

        // Start pasting each block every tick
        val task: AtomicReference<BukkitTask> = AtomicReference<BukkitTask>()

        tracker.trackCurrentBlock = 0

        // List of block faces to update *after* the schematic is done pasting.
        val toUpdate: MutableList<Block> = ArrayList()
        indexLocations.forEach { (index: Int, location: Location) ->
            val block = location.block
            val blockData = blocks[blockDatas[index].toInt()]
            if (Tag.STAIRS.values.contains(blockData!!.material) || Tag.FENCES.values
                    .contains(blockData.material) || blockData.material == Material.IRON_BARS
            ) {
                toUpdate.add(block)
            }
        }

        val pasteTask = Runnable {
            // Get the block, set the type, data, and then update the state.
            val locations: List<Location> = ArrayList(indexLocations.values)
            val indexes: List<Int> = ArrayList(indexLocations.keys)

            val block = locations[tracker.trackCurrentBlock].block
            val blockData = blocks[blockDatas[indexes[tracker.trackCurrentBlock]].toInt()]

            blockData?.let {
                block.setType(blockData.material, false)
                block.blockData = blockData
            }

            if (nbtData.containsKey(indexes[tracker.trackCurrentBlock])) {
                val nbtBlock: NBTBlock? = nbtData[indexes[tracker.trackCurrentBlock]]
                val state = block.state
                nbtBlock?.setData(state)
                state.update()
            }

            block.state.update(true, false)

            // Play block effects. Change to what you want.
            block.location.world?.spawnParticle(Particle.CLOUD, block.location, 6)
            block.location.world?.playEffect(block.location, Effect.STEP_SOUND, block.type)

            tracker.trackCurrentBlock++
            if (tracker.trackCurrentBlock >= locations.size || tracker.trackCurrentBlock >= indexes.size) {
                task.get().cancel()
                tracker.trackCurrentBlock = 0
                toUpdate.forEach(Consumer { b: Block ->
                    b.state.update(true, true)
                })
            }
        }

        task.set(
            plugin.server.scheduler.runTaskTimer(
                plugin,
                pasteTask,
                0,
                1L
            )
        )
        return indexLocations.values
    }

    /**
     * Undoes the schematic paste. This should be used after loading and pasting the schematic.
     * @param loc the location where the schematic was pasted
     * @param ignoredMaterials block types that won't be deleted (e.g. beacon)
     */
    fun undo(loc: Location, ignoredMaterials: List<Material> = listOf()) {
        checkIfSchematicIsLoaded()

        val tracker = Data()

        val indexLocations = LinkedHashMap<Int, Location>()
        for (x in 0 until width) {
            for (y in 0 until height) {
                for (z in 0 until length) {
                    val index = y * width * length + z * width + x

                    val location = Location(
                        loc.world,
                        (loc.blockX - z).toDouble(),
                        (y + loc.blockY).toDouble(),
                        ((loc.blockZ - width) + (x + 1)).toDouble()
                    )

                    val blockData = blocks[blockDatas[index].toInt()] ?: continue

                    /*
                     * Ignore blocks that aren't air and are in the ignoredMaterials list.
                     */
                    val material = blockData.material
                    if (material != Material.AIR && !ignoredMaterials.contains(material)) {
                        indexLocations[index] = location
                    }
                }
            }
        }

        // Start removing each block every tick
        val task: AtomicReference<BukkitTask> = AtomicReference<BukkitTask>()

        tracker.trackCurrentBlock = 0

        val undoTask = Runnable {
            val locations: List<Location> = ArrayList(indexLocations.values)
            val indexes: List<Int> = ArrayList(indexLocations.keys)

            val block = locations[tracker.trackCurrentBlock].block

            block.type = Material.AIR
            tracker.trackCurrentBlock++
            if (tracker.trackCurrentBlock >= locations.size || tracker.trackCurrentBlock >= indexes.size) {
                task.get().cancel()
                tracker.trackCurrentBlock = 0
            }
        }

        task.set(
            plugin.server.scheduler.runTaskTimer(
                plugin,
                undoTask,
                0,
                1L
            )
        )
    }

    private fun checkIfSchematicIsLoaded() {
        if (width.toInt() == 0 || height.toInt() == 0 || length.toInt() == 0 || blocks.isEmpty()) {
            throw Exception("Data has not been loaded yet")
        }
    }

    /**
     * Loads the schematic file. This should **always** be used before pasting a schematic.
     * @return schematic (self)
     */
    fun loadSchematic(): Schematic {
        // Read the schematic file. Get the width, height, length, blocks, and block data.
        val nbt: CompoundTag = NbtIo.readCompressed(schematic.toPath(), NbtAccounter.unlimitedHeap())

        width = nbt.getShort("Width")
        height = nbt.getShort("Height")
        length = nbt.getShort("Length")

        blockDatas = nbt.getByteArray("BlockData")

        val palette: CompoundTag = nbt.getCompound("Palette")
        val tiles = nbt["BlockEntities"] as ListTag?

        // Load NBT data
        if (tiles != null) {
            for (tile in tiles) {
                if (tile is CompoundTag) {
                    if (tile.isEmpty) continue
                    val nbtMaterial: NBTMaterial = NBTMaterial.fromTag(tile)
                    val nbtBlock: NBTBlock = nbtMaterial.getNbtBlock(tile)
                    if (!nbtBlock.isEmpty) nbtBlocks[nbtBlock.offset] = nbtBlock
                }
            }
        }

        /*
         * 	Explanation:
         *    The "Palette" is setup like this
         *      "block_data": id (the ID is a Unique ID that WorldEdit gives that
         *                    corresponds to an index in the BlockDatas Array)
         *    So I loop through all the Keys in the "Palette" Compound
         *    and store the custom ID and BlockData in the palette Map
         */
        palette.allKeys.forEach { rawState ->
            val id = palette.getInt(rawState)
            val blockData: BlockData = Bukkit.createBlockData(rawState)
            blocks[id] = blockData
        }

        // Load all material types - need to do more caching here sometime
        for (blockData in blockDatas) {
            val data: BlockData = blocks[blockData.toInt()] ?: continue
            materials.add(data.material)
        }
        return this
    }

    val schematicMaterialData: Map<Material, Int>
        /**
         * Returns a material-count map of the materials present in this schematic.
         *
         * The key corresponds to the Bukkit [Material] and the value is the
         * amount present in the schematic.
         *
         * @return material-count map of materials in the schematic
         */
        get() {
            val materialValuesMap: MutableMap<Material, Int> = mutableMapOf()
            for (material in materials) {
                val count = materialValuesMap.getOrDefault(material, 0)
                materialValuesMap[material] = count + 1
            }
            return materialValuesMap
        }

    /**
     * Hacky method to avoid "final".
     */
    private class Data {
        var trackCurrentBlock: Int = 0
    }
}