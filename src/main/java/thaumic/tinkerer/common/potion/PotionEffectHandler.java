package thaumic.tinkerer.common.potion;

import com.gamerforea.eventhelper.util.EventUtils;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.BlockForcefield;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by pixlepix on 4/19/14.
 */
public class PotionEffectHandler
{

	public static HashMap<Entity, Long> airPotionHit = new HashMap<>();
	public static HashMap<Entity, Long> firePotionHit = new HashMap<>();

	@SubscribeEvent
	public void onLivingHurt(LivingAttackEvent event)
	{
		if (event.source.getSourceOfDamage() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.source.getSourceOfDamage();

			/* TODO gamerforEA code replace, old code:
			if (player.isPotionActive(ModPotions.potionAir) && !player.worldObj.isRemote)
				airPotionHit.put(event.entity, event.entity.worldObj.getTotalWorldTime());
			if (player.isPotionActive(ModPotions.potionFire) && !player.worldObj.isRemote)
				firePotionHit.put(event.entity, event.entity.worldObj.getTotalWorldTime()); */
			if (!player.worldObj.isRemote)
			{
				boolean air = player.isPotionActive(ModPotions.potionAir);
				boolean fire = player.isPotionActive(ModPotions.potionFire);
				if ((air || fire) && EventUtils.cantDamage(player, event.entity))
					return;
				if (air)
					airPotionHit.put(event.entity, event.entity.worldObj.getTotalWorldTime());
				if (fire)
					firePotionHit.put(event.entity, event.entity.worldObj.getTotalWorldTime());
			}
			// TODO gamerforEA code end

			if (player.isPotionActive(ModPotions.potionEarth) && !player.worldObj.isRemote)
			{
				boolean xAxis = Math.abs(event.entity.posZ - player.posZ) < Math.abs(event.entity.posX - player.posX);
				int centerX = (int) ((event.entity.posX + player.posX) / 2);

				int centerY = (int) (player.posY + 2);
				int centerZ = (int) ((event.entity.posZ + player.posZ) / 2);

				for (int i = -2; i < 3; i++)
				{
					for (int j = -2; j < 3; j++)
					{
						int y = centerY + i;
						if (xAxis)
						{
							int z = centerZ + j;
							if (player.worldObj.isAirBlock(centerX, y, z))
							{
								// TODO gamerforEA code start
								if (EventUtils.cantBreak(player, centerX, y, z))
									continue;
								// TODO gamerforEA code end

								player.worldObj.setBlock(centerX, y, z, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockForcefield.class));
								ThaumicTinkerer.tcProxy.blockSparkle(player.worldObj, centerX, y, z, 100, 100);
							}
						}
						else
						{
							int x = centerX + j;
							if (player.worldObj.isAirBlock(x, y, centerZ))
							{
								// TODO gamerforEA code start
								if (EventUtils.cantBreak(player, x, y, centerZ))
									continue;
								// TODO gamerforEA code end

								player.worldObj.setBlock(x, y, centerZ, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockForcefield.class));
								ThaumicTinkerer.tcProxy.blockSparkle(player.worldObj, x, y, centerZ, 100, 100);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerTick(TickEvent.PlayerTickEvent event)
	{
		// TODO gamerforEA code start
		if (event.phase != TickEvent.Phase.END)
			return;
		// TODO gamerforEA code end

		EntityPlayer player = event.player;
		if (player.isPotionActive(ModPotions.potionWater))
			for (int x = (int) (player.posX - 2); x < player.posX + 2; x++)
			{
				for (int y = (int) (player.posY - 2); y < player.posY + 2; y++)
				{
					for (int z = (int) (player.posZ - 2); z < player.posZ + 2; z++)
					{
						Block block = player.worldObj.getBlock(x, y, z);
						if (block == Blocks.lava || block == Blocks.flowing_lava)
						{
							// TODO gamerforEA code start
							if (EventUtils.cantBreak(player, x, y, z))
								continue;
							// TODO gamerforEA code end

							player.worldObj.setBlock(x, y, z, Blocks.obsidian);
							ThaumicTinkerer.tcProxy.burst(player.worldObj, x + .5, y + .5, z + .5, 1.2F);
						}
					}
				}
			}
	}

	@SubscribeEvent
	public void onTick(TickEvent.ServerTickEvent event)
	{
		// TODO gamerforEA code start
		if (event.phase != TickEvent.Phase.END)
			return;
		// TODO gamerforEA code end

		Iterator<Map.Entry<Entity, Long>> iter = airPotionHit.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<Entity, Long> entry = iter.next();
			Entity target = entry.getKey();
			if (target.isEntityAlive())
			{
				if (target.worldObj.getTotalWorldTime() % 5 == 0)
				{
					Random rand = new Random();

					// TODO gamerforEA code replace (fix), old code: target.setVelocity(rand.nextFloat() - .5, rand.nextFloat(), rand.nextFloat() - .5);
					target.motionX = rand.nextFloat() - .5;
					target.motionY = rand.nextFloat();
					target.motionZ = rand.nextFloat() - .5;
					// TODO gamerforEA code end

					ThaumicTinkerer.tcProxy.burst(target.worldObj, target.posX, target.posY, target.posZ, .5F);
				}
			}
			// TODO gamerforEA code start
			else
			{
				iter.remove();
				continue;
			}
			// TODO gamerforEA code end

			if (target.worldObj.getTotalWorldTime() > entry.getValue() + 20)
				iter.remove();
		}

		//Fire Potion
		iter = firePotionHit.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry<Entity, Long> entry = iter.next();
			Entity target = entry.getKey();
			if (target.isEntityAlive())
			{
				if (target.worldObj.getTotalWorldTime() % 5 == 0)
				{
					Random rand = new Random();
					target.setFire(6);

					for (int i = 0; i < 30; i++)
					{
						/* TODO gamerforEA code replace, old code:
						double theta = rand.nextFloat() * 2 * Math.PI;
						double phi = rand.nextFloat() * 2 * Math.PI;
						double r = 2.5;
						double x = r * Math.sin(theta) * Math.cos(phi);
						double y = r * Math.sin(theta) * Math.sin(phi);
						double z = r * Math.cos(theta); */
						float theta = rand.nextFloat() * 2 * (float) Math.PI;
						float phi = rand.nextFloat() * 2 * (float) Math.PI;
						float r = 2.5F;
						float sinTheta = MathHelper.sin(theta);
						float x = r * sinTheta * MathHelper.cos(phi);
						float y = r * sinTheta * MathHelper.sin(phi);
						float z = r * MathHelper.cos(theta);
						// TODO gamerforEA code end

						ThaumicTinkerer.tcProxy.wispFX2(target.worldObj, target.posX + x, target.posY + y + 1, target.posZ + z, .1F, 4, true, true, 1F);
					}

				}
			}
			// TODO gamerforEA code start
			else
			{
				iter.remove();
				continue;
			}
			// TODO gamerforEA code end

			if (target.worldObj.getTotalWorldTime() > entry.getValue() + 6000)
				iter.remove();
		}
	}

}
