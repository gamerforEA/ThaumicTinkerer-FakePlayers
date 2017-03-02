package thaumic.tinkerer.common.block.tile;

import java.util.ArrayList;
import java.util.Arrays;

import com.gamerforea.ttinkerer.NoDupeProperties;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.tileentity.TileEntity;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.tiles.TilePedestal;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.core.helper.EnumMobAspect;
import thaumic.tinkerer.common.item.ItemMobAspect;

public class TileSummon extends TileEntity
{
	@Override
	public void updateEntity()
	{
		if (this.worldObj.getTotalWorldTime() % 300 == 0)
		{
			if (this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord))
				return;
			for (int radius = 1; radius < 6; radius++)
			{
				ArrayList<TileEntity> pedestals = new ArrayList<TileEntity>();
				for (int x = this.xCoord - radius; x < this.xCoord + radius; x++)
					for (int z = this.zCoord - radius; z < this.zCoord + radius; z++)
					{
						TileEntity tile = this.worldObj.getTileEntity(x, this.yCoord, z);
						if (tile instanceof TilePedestal && ((TilePedestal) tile).getStackInSlot(0) != null && ((TilePedestal) tile).getStackInSlot(0).getItem() instanceof ItemMobAspect)
							pedestals.add(tile);
					}

				for (int i = 0; i < pedestals.size(); i++)
					for (int j = 0; j < pedestals.size(); j++)
						for (int k = 0; k < pedestals.size(); k++)
						{
							TilePedestal ped1 = (TilePedestal) pedestals.get(i);
							TilePedestal ped2 = (TilePedestal) pedestals.get(j);
							TilePedestal ped3 = (TilePedestal) pedestals.get(k);

							if (ped1 != ped2 && ped2 != ped3 && ped1 != ped3)
							{
								ArrayList<Aspect> aspects = new ArrayList<Aspect>();
								aspects.add(ItemMobAspect.getAspect(ped1.getStackInSlot(0)));

								aspects.add(ItemMobAspect.getAspect(ped2.getStackInSlot(0)));

								aspects.add(ItemMobAspect.getAspect(ped3.getStackInSlot(0)));

								for (EnumMobAspect recipe : EnumMobAspect.values())
									if (Arrays.asList(recipe.aspects).containsAll(aspects) && aspects.containsAll(Arrays.asList(recipe.aspects)))
									{

										boolean isInfused = ItemMobAspect.isInfused(ped1.getStackInSlot(0)) && ItemMobAspect.isInfused(ped2.getStackInSlot(0)) && ItemMobAspect.isInfused(ped3.getStackInSlot(0));

										if (isInfused && this.worldObj.getTotalWorldTime() % 1200 != 0)
											return;

										if (!isInfused)
										{
											ped1.setInventorySlotContents(0, null);
											ped2.setInventorySlotContents(0, null);
											ped3.setInventorySlotContents(0, null);
										}

										if (!isInfused || ItemMobAspect.lastUsedTabletMatches(ped1.getStackInSlot(0), this) && ItemMobAspect.lastUsedTabletMatches(ped2.getStackInSlot(0), this) && ItemMobAspect.lastUsedTabletMatches(ped3.getStackInSlot(0), this))
										{

											if (!this.worldObj.isRemote)
											{
												Entity spawn = EntityList.createEntityByName(recipe.toString(), this.worldObj);

												// TODO gamerforEA code start
												if (spawn != null)
												// TODO gamerforEA code end
												{
													// TODO gamerforEA code start
													if (isInfused)
														NoDupeProperties.denyDropAspect(spawn);
													// TODO gamerforEA code end

													spawn.setLocationAndAngles(this.xCoord + .5, this.yCoord + 1, this.zCoord + .5, 0, 0);
													if (spawn instanceof EntitySkeleton && this.worldObj.provider.isHellWorld)
														((EntitySkeleton) spawn).setSkeletonType(1);
													this.worldObj.spawnEntityInWorld(spawn);
													((EntityLiving) spawn).onSpawnWithEgg(null);
													((EntityLiving) spawn).playLivingSound();
												}
											}

											if (this.worldObj.isRemote)
											{
												ThaumicTinkerer.tcProxy.essentiaTrailFx(this.worldObj, ped1.xCoord, ped1.yCoord, ped1.zCoord, this.xCoord, this.yCoord, this.zCoord, 20, aspects.get(0).getColor(), 20);
												ThaumicTinkerer.tcProxy.essentiaTrailFx(this.worldObj, ped2.xCoord, ped2.yCoord, ped2.zCoord, this.xCoord, this.yCoord, this.zCoord, 20, aspects.get(1).getColor(), 20);
												ThaumicTinkerer.tcProxy.essentiaTrailFx(this.worldObj, ped3.xCoord, ped3.yCoord, ped3.zCoord, this.xCoord, this.yCoord, this.zCoord, 20, aspects.get(2).getColor(), 20);
											}
										}
										if (isInfused)
										{
											ItemMobAspect.markLastUsedTablet(ped1.getStackInSlot(0), this);

											ItemMobAspect.markLastUsedTablet(ped2.getStackInSlot(0), this);

											ItemMobAspect.markLastUsedTablet(ped3.getStackInSlot(0), this);
										}

										return;

									}

							}

						}
			}
		}
	}

}
