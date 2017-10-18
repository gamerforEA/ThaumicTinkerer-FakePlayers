package com.gamerforea.ttinkerer;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.registry.GameData;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.util.Set;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

public final class EventConfig
{
	public static final Set<String> focusDislocationBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> blockTalismanBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> transvectorBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> animationTabletBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static final Set<String> repairerBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static boolean enableIchorPickAdvBedrockBreaking = false;
	public static boolean enableGenLegsLight = false;
	public static boolean shareBookSingleUse = true;
	public static float dislocationVisMultipier = 1;
	public static boolean robeStratosphereProjectileProtect = true;
	public static boolean disableClothRecipeStack = false;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("ThaumicTinkerer");
			String c = CATEGORY_GENERAL;
			readStringSet(cfg, "focusDislocationBlackList", c, "Чёрный список блоков для Фокуса Перемещения", focusDislocationBlackList);
			readStringSet(cfg, "blockTalismanBlackList", c, "Чёрный список блоков для Кольца черной дыры", blockTalismanBlackList);
			readStringSet(cfg, "transvectorBlackList", c, "Чёрный список блоков для Трансвекторного дислокатора", transvectorBlackList);
			readStringSet(cfg, "animationTabletBlackList", c, "Чёрный список предметов для Динамической дощечки", animationTabletBlackList);
			readStringSet(cfg, "repairerBlackList", c, "Чёрный список предметов для Таум-Восстановителя", repairerBlackList);
			enableIchorPickAdvBedrockBreaking = cfg.getBoolean("enableIchorPickAdvBedrockBreaking", c, enableIchorPickAdvBedrockBreaking, "Включить разрушение коренной породы Пробуждённой ихориевой киркой");
			enableGenLegsLight = cfg.getBoolean("enableGenLegsLight", c, enableGenLegsLight, "Включить освещение территории Пылающими шароварами");
			shareBookSingleUse = cfg.getBoolean("shareBookSingleUse", c, shareBookSingleUse, "Одноразовое использование Тома записи исследований");
			dislocationVisMultipier = cfg.getFloat("dislocationVisMultipier", c, dislocationVisMultipier, 0, Float.MAX_VALUE, "Множитель потребления Вис в Набалдашника Перемещения");
			robeStratosphereProjectileProtect = cfg.getBoolean("robeStratosphereProjectileProtect", c, robeStratosphereProjectileProtect, "Включить защиту от снарядов для Робы стратосферы");
			disableClothRecipeStack = cfg.getBoolean("disableClothRecipeStack", c, disableClothRecipeStack, "Выключить работу со стаками для Ткани поглощения заклинаний");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inBlackList(Set<String> blackList, ItemStack stack)
	{
		return stack != null && inBlackList(blackList, stack.getItem(), stack.getItemDamage());
	}

	public static final boolean inBlackList(Set<String> blackList, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inBlackList(blackList, ((ItemBlock) item).field_150939_a, meta);

		return inBlackList(blackList, getId(item), meta);
	}

	public static final boolean inBlackList(Set<String> blackList, Block block, int meta)
	{
		return inBlackList(blackList, getId(block), meta);
	}

	private static final boolean inBlackList(Set<String> blackList, String id, int meta)
	{
		return id != null && (blackList.contains(id) || blackList.contains(id + ':' + meta));
	}

	private static final void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static final Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static final Set<String> getStringSet(Configuration cfg, String name, String category, String comment, String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}

	private static final String getId(Item item)
	{
		return GameData.getItemRegistry().getNameForObject(item);
	}

	private static final String getId(Block block)
	{
		return GameData.getBlockRegistry().getNameForObject(block);
	}
}