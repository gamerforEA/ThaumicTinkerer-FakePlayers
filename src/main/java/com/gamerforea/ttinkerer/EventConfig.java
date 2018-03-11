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
	private static final String[] DEFAULT_BLOCKS = { "minecraft:bedrock", "modid:blockname:meta" };
	public static final Set<String> focusDislocationBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> blockTalismanBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> transvectorBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> animationTabletBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> repairerBlackList = Sets.newHashSet(DEFAULT_BLOCKS);
	public static final Set<String> placementMirrorBlackList = Sets.newHashSet(DEFAULT_BLOCKS);

	public static boolean enableIchorPickAdvBedrockBreaking = false;
	public static boolean enableIchorPickAdvAllBreaking = false;
	public static boolean enableGenLegsLight = false;
	public static boolean shareBookSingleUse = true;
	public static float dislocationVisMultipier = 1;
	public static boolean robeStratosphereProjectileProtect = true;
	public static boolean disableClothRecipeStack = false;
	public static int ichorAxeMaxBlocks = 1000;
	public static boolean summonerDenyDropAspect = true;
	public static boolean enableFocusDislocation = true;

	public static int ichorArmor1 = 3;
	public static int ichorArmor2 = 8;
	public static int ichorArmor3 = 6;
	public static int ichorArmor4 = 3;

	public static boolean transvectorDenySameBlock = false;
	public static boolean transvectorDenyInventory = false;

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
			readStringSet(cfg, "placementMirrorBlackList", c, "Чёрный список предметов для Терраформингового стекла", placementMirrorBlackList);

			enableIchorPickAdvBedrockBreaking = cfg.getBoolean("enableIchorPickAdvBedrockBreaking", c, enableIchorPickAdvBedrockBreaking, "Включить разрушение коренной породы Пробуждённой ихориевой киркой");
			enableIchorPickAdvAllBreaking = cfg.getBoolean("enableIchorPickAdvAllBreaking", c, enableIchorPickAdvAllBreaking, "Включить разрушение всех (почти) блоков Пробуждённой ихориевой киркой");
			enableGenLegsLight = cfg.getBoolean("enableGenLegsLight", c, enableGenLegsLight, "Включить освещение территории Пылающими шароварами");
			shareBookSingleUse = cfg.getBoolean("shareBookSingleUse", c, shareBookSingleUse, "Одноразовое использование Тома записи исследований");
			dislocationVisMultipier = cfg.getFloat("dislocationVisMultipier", c, dislocationVisMultipier, 0, Float.MAX_VALUE, "Множитель потребления Вис в Набалдашника Перемещения");
			robeStratosphereProjectileProtect = cfg.getBoolean("robeStratosphereProjectileProtect", c, robeStratosphereProjectileProtect, "Включить защиту от снарядов для Робы стратосферы");
			disableClothRecipeStack = cfg.getBoolean("disableClothRecipeStack", c, disableClothRecipeStack, "Выключить работу со стаками для Ткани поглощения заклинаний");
			ichorAxeMaxBlocks = cfg.getInt("ichorAxeMaxBlocks", c, ichorAxeMaxBlocks, 1, Integer.MAX_VALUE, "Максимальное количество блоков, разрушаемое Пробуждённым ихориевым топором");
			summonerDenyDropAspect = cfg.getBoolean("summonerDenyDropAspect", c, summonerDenyDropAspect, "Запретить дроп аспектов с мобов, призванных Пьедесталом некроманта");
			enableFocusDislocation = cfg.getBoolean("enableFocusDislocation", c, enableFocusDislocation, "Включить Набалдашник Перемещения");

			ichorArmor1 = cfg.getInt("ichorArmor1", c, ichorArmor1, 1, Integer.MAX_VALUE, "Количество очков защиты Ихориевой брони");
			ichorArmor2 = cfg.getInt("ichorArmor2", c, ichorArmor2, 1, Integer.MAX_VALUE, "Количество очков защиты Ихориевой брони");
			ichorArmor3 = cfg.getInt("ichorArmor3", c, ichorArmor3, 1, Integer.MAX_VALUE, "Количество очков защиты Ихориевой брони");
			ichorArmor4 = cfg.getInt("ichorArmor4", c, ichorArmor4, 1, Integer.MAX_VALUE, "Количество очков защиты Ихориевой брони");

			transvectorDenySameBlock = cfg.getBoolean("transvectorDenySameBlock", c, transvectorDenySameBlock, "Запрет для Трансвекторного дислокатора на обмен одинаковых блоков");
			transvectorDenyInventory = cfg.getBoolean("transvectorDenyInventory", c, transvectorDenyInventory, "Запрет для Трансвекторного дислокатора на перемещение контейнеров");

			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static boolean inList(Set<String> list, ItemStack stack)
	{
		return stack != null && inList(list, stack.getItem(), stack.getItemDamage());
	}

	public static boolean inList(Set<String> list, Item item, int meta)
	{
		if (item instanceof ItemBlock)
			return inList(list, ((ItemBlock) item).field_150939_a, meta);

		return inList(list, getId(item), meta);
	}

	public static boolean inList(Set<String> list, Block block, int meta)
	{
		return inList(list, getId(block), meta);
	}

	private static boolean inList(Set<String> blackList, String id, int meta)
	{
		return id != null && (blackList.contains(id) || blackList.contains(id + ':' + meta));
	}

	private static void readStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		Set<String> temp = getStringSet(cfg, name, category, comment, def);
		def.clear();
		def.addAll(temp);
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, Set<String> def)
	{
		return getStringSet(cfg, name, category, comment, def.toArray(new String[def.size()]));
	}

	private static Set<String> getStringSet(Configuration cfg, String name, String category, String comment, String... def)
	{
		return Sets.newHashSet(cfg.getStringList(name, category, def, comment));
	}

	private static String getId(Item item)
	{
		return GameData.getItemRegistry().getNameForObject(item);
	}

	private static String getId(Block block)
	{
		return GameData.getBlockRegistry().getNameForObject(block);
	}
}