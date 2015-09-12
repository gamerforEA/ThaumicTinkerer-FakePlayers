package com.gamerforea.ttinkerer;

import java.io.File;
import java.util.Set;

import com.google.common.collect.Sets;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.config.Configuration;

public final class EventConfig
{
	public static Set<String> focusDislocationBlackList = Sets.newHashSet("minecraft:stone", "IC2:blockMachine:5");

	static
	{
		try
		{
			File mainDirectory = FMLCommonHandler.instance().getMinecraftServerInstance().getFile(".");
			Configuration config = new Configuration(new File(mainDirectory, "config/Events/ThaumicTinkerer.cfg"));
			config.load();

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