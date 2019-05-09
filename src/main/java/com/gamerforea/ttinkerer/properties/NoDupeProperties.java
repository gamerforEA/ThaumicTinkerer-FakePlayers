package com.gamerforea.ttinkerer.properties;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import thaumic.tinkerer.common.lib.LibMisc;

public final class NoDupeProperties implements IExtendedEntityProperties
{
	private static final String PROP_NAME = LibMisc.MOD_ID + "_NoDupeData";
	private boolean denyDropAspect;

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setBoolean("DenyDropAspect", this.denyDropAspect);
		compound.setTag(PROP_NAME, nbt);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		if (compound.hasKey(PROP_NAME, Constants.NBT.TAG_COMPOUND))
		{
			NBTTagCompound nbt = compound.getCompoundTag(PROP_NAME);
			this.denyDropAspect = nbt.getBoolean("DenyDropAspect");
		}
	}

	@Override
	public void init(Entity entity, World world)
	{
	}

	public static boolean canDropAspect(Entity entity)
	{
		NoDupeProperties prop = (NoDupeProperties) entity.getExtendedProperties(PROP_NAME);
		return prop == null || !prop.denyDropAspect;
	}

	public static void denyDropAspect(Entity entity)
	{
		NoDupeProperties prop = (NoDupeProperties) entity.getExtendedProperties(PROP_NAME);
		if (prop == null)
			entity.registerExtendedProperties(PROP_NAME, prop = new NoDupeProperties());
		prop.denyDropAspect = true;
	}
}
