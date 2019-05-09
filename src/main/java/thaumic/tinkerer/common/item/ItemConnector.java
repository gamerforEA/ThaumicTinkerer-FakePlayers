/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the ThaumicTinkerer Mod.
 * <p>
 * ThaumicTinkerer is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * <p>
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
 * Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 * <p>
 * File Created @ [8 Sep 2013, 19:35:02 (GMT)]
 */
package thaumic.tinkerer.common.item;

import com.gamerforea.eventhelper.util.EventUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.entities.golems.EntityGolemBase;
import thaumic.tinkerer.common.block.tile.TileGolemConnector;
import thaumic.tinkerer.common.block.tile.transvector.TileTransvector;
import thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface;
import thaumic.tinkerer.common.core.helper.ItemNBTHelper;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ItemBase;
import thaumic.tinkerer.common.registry.ThaumicTinkererArcaneRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;

public class ItemConnector extends ItemBase
{

	private static final String TAG_POS_X = "posx";
	private static final String TAG_POS_Y = "posy";
	private static final String TAG_POS_Z = "posz";
	private static final String TAG_CONNECTING_GOLEM = "ConnectingGolem";

	public ItemConnector()
	{

		this.setMaxStackSize(1);
	}

	public static boolean getConnectingGolem(ItemStack stack)
	{
		return ItemNBTHelper.getBoolean(stack, TAG_CONNECTING_GOLEM, false);
	}

	public static void setConnectingGolem(ItemStack stack, boolean connecting)
	{
		ItemNBTHelper.setBoolean(stack, TAG_CONNECTING_GOLEM, connecting);
	}

	public static void setX(ItemStack stack, int x)
	{
		ItemNBTHelper.setInt(stack, TAG_POS_X, x);
	}

	public static void setY(ItemStack stack, int y)
	{
		ItemNBTHelper.setInt(stack, TAG_POS_Y, y);
	}

	public static void setZ(ItemStack stack, int z)
	{
		ItemNBTHelper.setInt(stack, TAG_POS_Z, z);
	}

	public static int getX(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_POS_X, 0);
	}

	public static int getY(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_POS_Y, -1);
	}

	public static int getZ(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_POS_Z, 0);
	}

	@Override
	public boolean shouldDisplayInTab()
	{
		return true;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		return null;
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererArcaneRecipe(LibResearch.KEY_INTERFACE + "1", LibResearch.KEY_INTERFACE, new ItemStack(this), new AspectList().add(Aspect.ORDER, 2), " I ", " WI", "S  ", 'I', new ItemStack(Items.iron_ingot), 'W', new ItemStack(Items.stick), 'S', new ItemStack(ConfigItems.itemShard, 1, 4));
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		if (world.isRemote)
			return false;

		// TODO gamerforEA code start
		if (EventUtils.cantBreak(player, x, y, z))
			return false;
		// TODO gamerforEA code end

		TileEntity tile = world.getTileEntity(x, y, z);

		if (getY(stack) == -1)
			if (tile instanceof TileTransvector || tile instanceof TileGolemConnector)
			{
				setX(stack, x);
				setY(stack, y);
				setZ(stack, z);

				if (world.isRemote)
					player.swingItem();

				this.playSound(world, x, y, z);
				if (tile instanceof TileTransvector)
					player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.set"));
				else
					player.addChatMessage(new ChatComponentTranslation("ttmisc.golemconnector.set"));
			}
			else
				player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.notinterf"));
		else
		{
			int targetX = getX(stack);
			int targetY = getY(stack);
			int targetZ = getZ(stack);

			TileEntity tile1 = world.getTileEntity(targetX, targetY, targetZ);
			if (!(tile1 instanceof TileTransvector))
			{
				setY(stack, -1);
				player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.notpresent"));
			}
			else
			{
				TileTransvector trans = (TileTransvector) tile1;

				if (tile1 instanceof TileTransvectorInterface && tile instanceof TileTransvectorInterface)
				{
					player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.interffail"));
					return true;
				}

				if (Math.abs(targetX - x) > trans.getMaxDistance() || Math.abs(targetY - y) > trans.getMaxDistance() || Math.abs(targetZ - z) > trans.getMaxDistance())
				{
					player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.toofar"));
					return true;
				}

				trans.x = x;
				trans.y = y;
				trans.z = z;

				setY(stack, -1);

				this.playSound(world, x, y, z);
				player.addChatMessage(new ChatComponentTranslation("ttmisc.connector.complete"));
				world.markBlockForUpdate(trans.x, trans.y, trans.z);
			}
		}

		return true;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, EntityLivingBase par3EntityLivingBase)
	{
		par1ItemStack = par2EntityPlayer.getCurrentEquippedItem();
		if (par2EntityPlayer.isSneaking())
			if (par3EntityLivingBase instanceof EntityGolemBase)
			{
				if (getY(par1ItemStack) == -1)
				{
					if (par3EntityLivingBase.worldObj.isRemote)
						return false;
					par2EntityPlayer.addChatMessage(new ChatComponentTranslation("ttmisc.golemconnector.notinterf"));
					return true;
				}
				int x = getX(par1ItemStack);
				int y = getY(par1ItemStack);
				int z = getZ(par1ItemStack);
				TileEntity tile1 = par2EntityPlayer.worldObj.getTileEntity(x, y, z);
				if (!(tile1 instanceof TileGolemConnector))
				{
					setY(par1ItemStack, -1);
					if (par3EntityLivingBase.worldObj.isRemote)
						return false;
					par2EntityPlayer.addChatMessage(new ChatComponentTranslation("ttmisc.golemconnector.notpresent"));
					return false;
				}
				else
				{
					if (par3EntityLivingBase.worldObj.isRemote)
					{
						par2EntityPlayer.swingItem();
						return false;
					}
					TileGolemConnector trans = (TileGolemConnector) tile1;

					trans.ConnectGolem(par3EntityLivingBase.getUniqueID());

					setY(par1ItemStack, -1);

					this.playSound(par3EntityLivingBase.worldObj, (int) par3EntityLivingBase.posX, (int) par3EntityLivingBase.posY, (int) par3EntityLivingBase.posZ);
					par2EntityPlayer.addChatMessage(new ChatComponentTranslation("ttmisc.golemconnector.complete"));
					par2EntityPlayer.worldObj.markBlockForUpdate(trans.xCoord, trans.yCoord, trans.zCoord);
				}
				return true;
			}
		return false;
	}

	private void playSound(World world, int x, int y, int z)
	{
		if (!world.isRemote)
			world.playSoundEffect(x, y, z, "random.orb", 0.8F, 1F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D()
	{
		return true;
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.CONNECTOR;
	}
}