package com.gamerforea.ttinkerer;

import com.gamerforea.ttinkerer.properties.ExtendedPlayer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FixEventHandler
{
	private final Map<UUID, ExtendedPlayer> savedProperties = new HashMap<>();

	public static void init()
	{
		FixEventHandler handler = new FixEventHandler();
		FMLCommonHandler.instance().bus().register(handler);
		MinecraftForge.EVENT_BUS.register(handler);
	}

	@SubscribeEvent
	public void onEntityConstructing(EntityEvent.EntityConstructing event)
	{
		if (event.entity instanceof EntityPlayer)
			ExtendedPlayer.init((EntityPlayer) event.entity);
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event)
	{
		if (event.entity instanceof EntityPlayer)
			this.savedProperties.put(event.entity.getUniqueID(), ExtendedPlayer.get((EntityPlayer) event.entity));
	}

	@SubscribeEvent
	public void onRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		ExtendedPlayer savedProperty = this.savedProperties.remove(event.player.getUniqueID());
		if (savedProperty != null)
			ExtendedPlayer.get(event.player).cloneFrom(savedProperty);
	}
}
