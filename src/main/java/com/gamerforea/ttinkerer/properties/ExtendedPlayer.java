package com.gamerforea.ttinkerer.properties;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.util.Constants;
import thaumic.tinkerer.common.lib.LibMisc;

public final class ExtendedPlayer implements IExtendedEntityProperties
{
	private static final String PROP_NAME = LibMisc.MOD_ID + "_Extended";
	private int shareBooksCounter;

	@Override
	public void saveNBTData(NBTTagCompound compound)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("ShareBooksCounter", this.shareBooksCounter);
		compound.setTag(PROP_NAME, nbt);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound)
	{
		if (compound.hasKey(PROP_NAME, Constants.NBT.TAG_COMPOUND))
		{
			NBTTagCompound nbt = compound.getCompoundTag(PROP_NAME);
			this.shareBooksCounter = nbt.getInteger("ShareBooksCounter");
		}
	}

	@Override
	public void init(Entity entity, World world)
	{
	}

	public int getShareBooksCounter()
	{
		return this.shareBooksCounter;
	}

	public void setShareBooksCounter(int counter)
	{
		this.shareBooksCounter = counter;
	}

	public void cloneFrom(ExtendedPlayer other)
	{
		this.shareBooksCounter = other.shareBooksCounter;
	}

	public static ExtendedPlayer init(EntityPlayer player)
	{
		IExtendedEntityProperties properties = player.getExtendedProperties(PROP_NAME);
		if (properties == null)
		{
			properties = new ExtendedPlayer();
			player.registerExtendedProperties(PROP_NAME, properties);
		}
		return (ExtendedPlayer) properties;
	}

	public static ExtendedPlayer get(EntityPlayer player)
	{
		return (ExtendedPlayer) Preconditions.checkNotNull(player.getExtendedProperties(PROP_NAME), "Property " + PROP_NAME + " not found for player " + player.getCommandSenderName());
	}
}
