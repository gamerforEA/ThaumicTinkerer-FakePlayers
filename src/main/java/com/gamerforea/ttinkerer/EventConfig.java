package com.gamerforea.ttinkerer;

import java.util.Set;

import com.gamerforea.eventhelper.util.FastUtils;
import com.google.common.collect.Sets;

import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static Set<String> focusDislocationBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");

	static
	{
		try
		{
			Configuration config = FastUtils.getConfig("ThaumicTinkerer");

			focusDislocationBlackList = Sets.newHashSet(config.getStringList("focusDislocationBlackList", Configuration.CATEGORY_GENERAL, focusDislocationBlackList.toArray(new String[focusDislocationBlackList.size()]), "Чёрный список блоков для Фокуса Перемещения"));

			config.save();
		}
		catch (Throwable throwable)
		{
			System.err.println("Failed load config. Use default values.");
			throwable.printStackTrace();
		}
	}
}