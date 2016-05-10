package com.gamerforea.ttinkerer;

import static net.minecraftforge.common.config.Configuration.CATEGORY_GENERAL;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static Set<String> focusDislocationBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static Set<String> blockTalismanBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static Set<String> transvectorBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");
	public static boolean enableIchorPickAdvBedrockBreaking = false;

	static
	{
		try
		{
			Configuration cfg = FastUtils.getConfig("ThaumicTinkerer");
			readStringSet(cfg, "focusDislocationBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Фокуса Перемещения", focusDislocationBlackList);
			readStringSet(cfg, "blockTalismanBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Кольца черной дыры", blockTalismanBlackList);
			readStringSet(cfg, "transvectorBlackList", CATEGORY_GENERAL, "Чёрный список блоков для Трансвекторного дислокатора", transvectorBlackList);
			enableIchorPickAdvBedrockBreaking = cfg.getBoolean("enableIchorPickAdvBedrockBreaking", CATEGORY_GENERAL, enableIchorPickAdvBedrockBreaking, "Включить разрушение коренной породы Пробуждённой ихориевой киркой");
			cfg.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}

	public static final boolean inBlackList(Set<String> blackList, Block block, int meta)
	{
		UniqueIdentifier blockId = GameRegistry.findUniqueIdentifierFor(block);

		if (blockId != null)
		{
			String name = blockId.modId + ":" + blockId.name;
			if (blackList.contains(name) || blackList.contains(name + ":" + meta))
				return true;
		}

		return false;
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
}