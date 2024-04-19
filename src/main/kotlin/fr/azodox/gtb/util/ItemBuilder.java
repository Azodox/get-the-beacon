package fr.azodox.gtb.util;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.Validate;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ItemBuilder - An API class to create an
 * {@link ItemStack} with just one line of code!
 *
 * @author Acquized
 * @version 1.8.3
 * @contributor Kev575, Azodox_, Virtuaal
 */
@SuppressWarnings("unused")
public class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;
    private Material material;
    private int amount = 1;
    private BlockData data;
    private int damage = 0;
    private boolean unbreakable = false;
    private Map<Enchantment, Integer> enchantments = new HashMap<>();
    private Component displayName;
    private List<Component> lore = new ArrayList<>();
    private List<ItemFlag> flags = new ArrayList<>();
    private boolean unsafeStackSizeAllowed = false;

    /**
     * Create a new ItemBuilder with {@link Material}
     */
    public ItemBuilder(Material material) {
        if (material == null) {
            material = Material.AIR;
        }
        this.item = new ItemStack(material);
        this.material = material;
    }

    /**
     * Create a new ItemBuilder with {@link Material} and {@link Integer} for the amount of the item
     */
    public ItemBuilder(Material material, int amount) {
        if (material == null) {
            material = Material.AIR;
        }
        if (((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSizeAllowed)) {
            amount = 1;
        }
        this.amount = amount;
        this.item = new ItemStack(material, amount);
        this.material = material;
    }

    /**
     * Create a new ItemBuilder with {@link Material},
     * {@link Integer} for the amount of the item and a {@link String} for the Display name
     */
    public ItemBuilder(Material material, int amount, Component displayName) {
        if (material == null) {
            material = Material.AIR;
        }
        Validate.notNull(displayName, "The display name is null.");
        this.item = new ItemStack(material, amount);
        this.material = material;
        if (((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSizeAllowed)) {
            amount = 1;
        }
        this.amount = amount;
        this.displayName = displayName;
    }

    /**
     * Create a new ItemBuilder with a {@link Material} and a {@link Component} for the display name
     */
    public ItemBuilder(Material material, Component displayName) {
        if (material == null) {
            material = Material.AIR;
        }
        Validate.notNull(displayName, "The display name is null.");
        this.item = new ItemStack(material);
        this.material = material;
        this.displayName = displayName;
    }

    /**
     * Create a new ItemBuilder with an {@link ItemStack} as a base
     */
    public ItemBuilder(ItemStack item) {
        Validate.notNull(item, "The item is null.");
        this.item = item;
        this.material = item.getType();
        this.amount = item.getAmount();
        this.data = ((BlockDataMeta) item.getItemMeta()).getBlockData(item.getType());
        this.damage = ((Damageable) item.getItemMeta()).getDamage();
        this.enchantments = item.getEnchantments();

        if (item.hasItemMeta()) {
            this.meta = item.getItemMeta();
            this.displayName = item.getItemMeta().displayName();
            this.lore = item.getItemMeta().lore();
            flags.addAll(item.getItemMeta().getItemFlags());
        }
    }

    /**
     * Create a new ItemBuilder from a {@link FileConfiguration}
     * and a {@link String} that represents the path
     */
    public ItemBuilder(FileConfiguration cfg, String path) {
        this(cfg.getItemStack(path));
    }

    /**
     * Sets the Amount of the ItemStack
     *
     * @param amount Amount for the ItemStack
     */
    public ItemBuilder amount(int amount) {
        if (((amount > material.getMaxStackSize()) || (amount <= 0)) && (!unsafeStackSizeAllowed))
            amount = 1;
        this.amount = amount;
        return this;
    }

    /**
     * Adds an information in the persistent data container of the item
     *
     * @param key        the key of the information
     * @param value      the value of the information
     * @param javaPlugin the plugin that will be used as the nameSpace of the additional information
     * @param type       the type of the additional information
     * @return the ItemBuilder
     */
    public <T> ItemBuilder persistentInfo(String key, T value, Plugin javaPlugin, PersistentDataType<T, T> type) {
        ItemMeta itemMeta = this.item.getItemMeta();
        NamespacedKey nsk = new NamespacedKey(javaPlugin, key);
        itemMeta.getPersistentDataContainer().set(nsk, type, value);
        return this.meta(itemMeta);
    }

    /**
     * Sets the {@link BlockData} of the ItemStack
     *
     * @param data MaterialData for the ItemStack
     */
    public ItemBuilder data(BlockData data) {
        Validate.notNull(data, "The data is null.");
        this.data = data;
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     * Sets the durability of the ItemStack
     *
     * @param damage Damage for the ItemStack
     */
    public ItemBuilder durability(int damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Sets the {@link Material} of the ItemStack
     *
     * @param material Material for the ItemStack
     */
    public ItemBuilder material(Material material) {
        Validate.notNull(material, "The material is null.");
        this.material = material;
        return this;
    }

    /**
     * Sets the {@link ItemMeta} of the ItemStack
     *
     * @param meta Meta for the ItemStack
     */
    public ItemBuilder meta(ItemMeta meta) {
        Validate.notNull(meta, "The meta is null.");
        this.meta = meta;
        return this;
    }

    /**
     * Adds a {@link Enchantment} to the ItemStack
     *
     * @param enchant Enchantment for the ItemStack
     * @param level   Level of the Enchantment
     */
    public ItemBuilder enchant(Enchantment enchant, int level) {
        Validate.notNull(enchant, "The Enchantment is null.");
        enchantments.put(enchant, level);
        return this;
    }

    /**
     * Adds a list of {@link Enchantment} to the ItemStack
     *
     * @param enchantments Map containing Enchantment and Level for the ItemStack
     */
    public ItemBuilder enchant(Map<Enchantment, Integer> enchantments) {
        Validate.notNull(enchantments, "The enchantments are null.");
        this.enchantments = enchantments;
        return this;
    }

    /**
     * Sets the Color of the leather ItemStack
     *
     * @param color Color for the ItemStack
     */
    public ItemBuilder color(Color color) {
        if (this.item.getItemMeta() instanceof LeatherArmorMeta armorMeta) {
            armorMeta.setColor(color);
            this.item.setItemMeta(armorMeta);
        }
        return this;
    }

    /**
     * Sets the display name of the ItemStack
     *
     * @param displayName Display name for the ItemStack
     */
    public ItemBuilder displayName(Component displayName) {
        Validate.notNull(displayName, "The displayName is null.");
        this.displayName = displayName;
        return this;
    }

    /**
     * Adds a Line to the Lore of the ItemStack
     *
     * @param line Line of the Lore for the ItemStack
     */
    public ItemBuilder lore(Component line) {
        Validate.notNull(line, "The line is null.");
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(line);
        return this;
    }

    /**
     * Sets the Lore of the ItemStack
     *
     * @param lore List containing String as Lines for the ItemStack Lore
     */
    public ItemBuilder lore(List<Component> lore) {
        Validate.notNull(lore, "The lores are null.");
        this.lore = lore;
        return this;
    }

    /**
     * Adds one or more Lines to the Lore of the ItemStack
     *
     * @param lines One or more Strings for the ItemStack Lore
     */
    public ItemBuilder lore(Component... lines) {
        Validate.notNull(lines, "The lines are null.");
        for (Component line : lines) {
            lore(line);
        }
        return this;
    }

    /**
     * Adds a String at a specified position in the Lore of the ItemStack
     *
     * @param line  Line of the Lore for the ItemStack
     * @param index Position in the Lore for the ItemStack
     */
    public ItemBuilder lore(Component line, int index) {
        Validate.notNull(line, "The line is null.");
        lore.set(index, line);
        return this;
    }

    /**
     * Adds a {@link ItemFlag} to the ItemStack
     *
     * @param flag ItemFlag for the ItemStack
     */
    public ItemBuilder flag(ItemFlag flag) {
        Validate.notNull(flag, "The flag is null.");
        flags.add(flag);
        return this;
    }

    /**
     * Adds more than one {@link ItemFlag} to the ItemStack
     *
     * @param flags List containing all ItemFlags
     */
    public ItemBuilder flag(List<ItemFlag> flags) {
        Validate.notNull(flags, "The flags are null.");
        this.flags = flags;
        return this;
    }

    /**
     * Makes the ItemStack Glow like it had an Enchantment
     */
    public ItemBuilder glow() {
        enchant(material != Material.BOW ? Enchantment.ARROW_INFINITE : Enchantment.LUCK, 10);
        return flag(ItemFlag.HIDE_ENCHANTS);
    }


    /**
     * Allows / Disallows Stack Sizes under 1 and above 64
     *
     * @param allow Determinate if it should be allowed or not
     */
    public ItemBuilder allowUnsafeStackSize(boolean allow) {
        this.unsafeStackSizeAllowed = allow;
        return this;
    }

    /**
     * Toggles the allowed boolean for stack sizes under 1 and above 64
     */
    public ItemBuilder toggleUnsafeStackSize() {
        return allowUnsafeStackSize(!unsafeStackSizeAllowed);
    }

    /**
     * Returns the Display name
     */
    public Component getDisplayName() {
        return displayName;
    }

    /**
     * Returns the Amount
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Returns all Enchantments
     */
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    /**
     * Returns the Durability
     */
    public int getDurability() {
        return damage;
    }

    /**
     * Returns all ItemFlags
     */
    public List<ItemFlag> getFlags() {
        return flags;
    }

    /**
     * Returns the Material
     */
    public Material getMaterial() {
        return material;
    }

    /**
     * Returns the ItemMeta
     */
    public ItemMeta getMeta() {
        return meta;
    }

    /**
     * Returns the MaterialData
     */
    public BlockData getData() {
        return data;
    }

    /**
     * Returns all Lore Lines
     */
    public List<Component> getLore() {
        return lore;
    }

    /**
     * Converts the Item to a ConfigStack and writes it to path
     *
     * @param cfg  Configuration File to which it should be written
     * @param path Path to which the ConfigStack should be written
     */
    public ItemBuilder toConfig(FileConfiguration cfg, String path) {
        cfg.set(path, build());
        return this;
    }

    /**
     * Converts back the ConfigStack to a ItemBuilder
     *
     * @param cfg  Configuration File from which it should be read
     * @param path Path from which the ConfigStack should be read
     */
    public ItemBuilder fromConfig(FileConfiguration cfg, String path) {
        return new ItemBuilder(cfg, path);
    }

    /**
     * Converts the Item to a ConfigStack and writes it to path
     *
     * @param cfg     Configuration File to which it should be written
     * @param path    Path to which the ConfigStack should be written
     * @param builder Which ItemBuilder should be written
     */
    public static void toConfig(FileConfiguration cfg, String path, ItemBuilder builder) {
        cfg.set(path, builder.build());
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     *
     * @return The ItemBuilder as JSON String
     */
    public String toJson() {
        return new Gson().toJson(this);
    }

    /**
     * Converts the ItemBuilder to a JsonItemBuilder
     *
     * @param builder Which ItemBuilder should be converted
     * @return The ItemBuilder as JSON String
     */
    public static String toJson(ItemBuilder builder) {
        return new Gson().toJson(builder);
    }

    /**
     * Converts the JsonItemBuilder back to a ItemBuilder
     *
     * @param json Which JsonItemBuilder should be converted
     */
    public static ItemBuilder fromJson(String json) {
        return new Gson().fromJson(json, ItemBuilder.class);
    }

    /**
     * Applies the currently ItemBuilder to the JSONItemBuilder
     *
     * @param json      Already existing JsonItemBuilder
     * @param overwrite Should the JsonItemBuilder used now
     */
    public ItemBuilder applyJson(String json, boolean overwrite) {
        ItemBuilder b = new Gson().fromJson(json, ItemBuilder.class);
        if (overwrite)
            return b;
        if (b.displayName != null)
            displayName = b.displayName;
        if (b.data != null)
            data = b.data;
        if (b.material != null)
            material = b.material;
        if (b.lore != null)
            lore = b.lore;
        if (b.enchantments != null)
            enchantments = b.enchantments;
        if (b.item != null)
            item = b.item;
        if (b.flags != null)
            flags = b.flags;
        damage = b.damage;
        amount = b.amount;
        return this;
    }

    /**
     * Converts the ItemBuilder to a {@link ItemStack}
     */
    public ItemStack build() {
        item = item.withType(material);
        item.setAmount(amount);
        ((Damageable) item.getItemMeta()).setDamage(damage);

        if (data != null) {
            ((BlockDataMeta) meta).setBlockData(data);
        }
        if (displayName != null) {
            meta.displayName(displayName);
        }
        if (lore != null && !lore.isEmpty()) {
            meta.lore(lore);
        }
        if (!flags.isEmpty()) {
            for (ItemFlag f : flags) {
                meta.addItemFlags(f);
            }
        }
        meta.setUnbreakable(unbreakable);
        item.setItemMeta(meta);
        if (!enchantments.isEmpty()) {
            item.addUnsafeEnchantments(enchantments);
        }
        return item;
    }
}