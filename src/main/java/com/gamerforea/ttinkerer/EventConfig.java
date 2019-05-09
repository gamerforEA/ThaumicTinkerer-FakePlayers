package com.gamerforea.ttinkerer;

import com.gamerforea.eventhelper.config.*;
import net.minecraftforge.common.config.Configuration;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

@Config(name = "ThaumicTinkerer")
public final class EventConfig
{
	private static final String CATEGORY_BLACKLISTS = "blacklists";
	private static final String CATEGORY_BLACKLISTS_TRANSVECTOR = CATEGORY_BLACKLISTS + Configuration.CATEGORY_SPLITTER + "transvector";
	private static final String CATEGORY_OTHER = "other";
	private static final String CATEGORY_ARMOR = "armor";

	@ConfigItemBlockList(name = "focusDislocation",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Фокуса Перемещения",
						 oldName = "focusDislocationBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList focusDislocationBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "blockTalisman",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список блоков для Кольца черной дыры",
						 oldName = "blockTalismanBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList blockTalismanBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "animationTablet",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Динамической дощечки",
						 oldName = "animationTabletBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList animationTabletBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "repairer",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Таум-Восстановителя",
						 oldName = "repairerBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList repairerBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "placementMirror",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Терраформингового стекла",
						 oldName = "placementMirrorBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList placementMirrorBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "ichorPouch",
						 category = CATEGORY_BLACKLISTS,
						 comment = "Чёрный список предметов для Бездонной сумки",
						 oldName = "ichorPouchBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList ichorPouchBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "blackList",
						 category = CATEGORY_BLACKLISTS_TRANSVECTOR,
						 comment = "Чёрный список блоков для Трансвекторного дислокатора",
						 oldName = "transvectorBlackList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList transvectorBlackList = new ItemBlockList();

	@ConfigItemBlockList(name = "whiteList",
						 category = CATEGORY_BLACKLISTS_TRANSVECTOR,
						 comment = "Белый список блоков для Трансвекторного дислокатора",
						 oldName = "transvectorWhiteList",
						 oldCategory = CATEGORY_GENERAL)
	public static final ItemBlockList transvectorWhiteList = new ItemBlockList();

	@ConfigBoolean(name = "enableBlackList",
				   category = CATEGORY_BLACKLISTS_TRANSVECTOR,
				   comment = "Включить чёрный список блоков для Трансвекторного дислокатора",
				   oldName = "transvectorEnableBlackList",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean transvectorEnableBlackList = true;

	@ConfigBoolean(name = "enableWhiteList",
				   category = CATEGORY_BLACKLISTS_TRANSVECTOR,
				   comment = "Включить белый список блоков для Трансвекторного дислокатора",
				   oldName = "transvectorEnableWhiteList",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean transvectorEnableWhiteList = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Включить разрушение коренной породы Пробуждённой ихориевой киркой",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean enableIchorPickAdvBedrockBreaking = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Включить разрушение всех (почти) блоков Пробуждённой ихориевой киркой",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean enableIchorPickAdvAllBreaking = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Включить освещение территории Пылающими шароварами",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean enableGenLegsLight = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Одноразовое использование Тома записи исследований",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean shareBookSingleUse = true;

	@ConfigInt(category = CATEGORY_OTHER,
			   comment = "Максимальное количество созданий Тома записи исследований (0 - без ограничений)",
			   oldCategory = CATEGORY_GENERAL,
			   min = 0)
	public static int shareBookCreateLimit = 0;

	@ConfigFloat(category = CATEGORY_OTHER,
				 comment = "Множитель потребления Вис в Набалдашника Перемещения",
				 oldCategory = CATEGORY_GENERAL,
				 min = 0)
	public static float dislocationVisMultipier = 1;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Включить защиту от снарядов для Робы стратосферы",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean robeStratosphereProjectileProtect = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Выключить работу со стаками для Ткани поглощения заклинаний",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean disableClothRecipeStack = false;

	@ConfigInt(category = CATEGORY_OTHER,
			   comment = "Максимальное количество блоков, разрушаемое Пробуждённым ихориевым топором",
			   oldCategory = CATEGORY_GENERAL,
			   min = 1)
	public static int ichorAxeMaxBlocks = 1000;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запретить дроп аспектов с мобов, призванных Пьедесталом некроманта",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean summonerDenyDropAspect = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Включить Набалдашник Перемещения",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean enableFocusDislocation = true;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет для Трансвекторного дислокатора на обмен одинаковых блоков",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean transvectorDenySameBlock = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет для Трансвекторного дислокатора на перемещение контейнеров",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean transvectorDenyInventory = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запрет ставить больше 1 Пьедестала некроманта на чанк",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean summonerOnlyOnePerChunk = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "При использовании Набалдашника Перемещения обновлять соседние блоки (фикс дюпа)",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean focusDislocationNotifyNeighbors = false;

	@ConfigBoolean(category = CATEGORY_OTHER,
				   comment = "Запретить переносить блоки-контейнеры Набалдашником Перемещения",
				   oldCategory = CATEGORY_GENERAL)
	public static boolean focusDislocationDenyInventory = false;

	@ConfigBoolean(category = CATEGORY_OTHER, comment = "Фикс крафта Наполненных семян", oldCategory = CATEGORY_GENERAL)
	public static boolean fixInfusedSeedsCraft = false;

	@ConfigInt(category = CATEGORY_ARMOR,
			   comment = "Количество очков защиты Ихориевой брони",
			   oldCategory = CATEGORY_GENERAL,
			   min = 1)
	public static int ichorArmor1 = 3;

	@ConfigInt(category = CATEGORY_ARMOR,
			   comment = "Количество очков защиты Ихориевой брони",
			   oldCategory = CATEGORY_GENERAL,
			   min = 1)
	public static int ichorArmor2 = 8;

	@ConfigInt(category = CATEGORY_ARMOR,
			   comment = "Количество очков защиты Ихориевой брони",
			   oldCategory = CATEGORY_GENERAL,
			   min = 1)
	public static int ichorArmor3 = 6;

	@ConfigInt(category = CATEGORY_ARMOR,
			   comment = "Количество очков защиты Ихориевой брони",
			   oldCategory = CATEGORY_GENERAL,
			   min = 1)
	public static int ichorArmor4 = 3;

	public static void init()
	{
		ConfigUtils.readConfig(EventConfig.class);
	}

	static
	{
		init();
	}
}