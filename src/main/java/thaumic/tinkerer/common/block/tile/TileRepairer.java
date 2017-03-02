/**
 x * This class was created by <Vazkii>. It's distributed as
 * part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
 * Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [Nov 30, 2013, 5:39:09 PM (GMT)]
 */
package thaumic.tinkerer.common.block.tile;

import java.util.HashMap;
import java.util.Map;

import com.gamerforea.ttinkerer.EventConfig;

import appeng.api.movable.IMovableTile;
import cpw.mods.fml.common.Loader;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.aspects.IEssentiaTransport;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.compat.TinkersConstructCompat;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.lib.LibBlockNames;

/*import thaumic.tinkerer.common.compat.TinkersConstructCompat;*/

public class TileRepairer extends TileEntity implements ISidedInventory, IAspectContainer, IEssentiaTransport, IMovableTile
{

	private static final Map<Aspect, Integer> repairValues = new HashMap();
	static
	{
		repairValues.put(Aspect.TOOL, 8);
		repairValues.put(Aspect.CRAFT, 5);
		repairValues.put(Aspect.ORDER, 3);
	}
	public int ticksExisted = 0;
	public boolean tookLastTick = true;
	int dmgLastTick = 0;
	ItemStack[] inventorySlots = new ItemStack[1];

	@Override
	public void updateEntity()
	{
		if (++this.ticksExisted % 10 == 0)
		{
			// TODO gamerforEA code start
			if (EventConfig.inBlackList(EventConfig.repairerBlackList, this.inventorySlots[0]))
				return;
			// TODO gamerforEA code end

			if (Loader.isModLoaded("TConstruct") && ConfigHandler.repairTConTools)
				if (this.inventorySlots[0] != null)
					if (TinkersConstructCompat.isTConstructTool(this.inventorySlots[0]))
					{
						int dmg = TinkersConstructCompat.getDamage(this.inventorySlots[0]);
						if (dmg > 0)
						{
							int essentia = this.drawEssentia();
							TinkersConstructCompat.fixDamage(this.inventorySlots[0], essentia);
							this.markDirty();
							if (this.dmgLastTick != 0 && this.dmgLastTick != dmg)
							{
								ThaumicTinkerer.tcProxy.sparkle((float) (this.xCoord + 0.25 + Math.random() / 2F), (float) (this.yCoord + 1 + Math.random() / 2F), (float) (this.zCoord + 0.25 + Math.random() / 2F), 0);
								this.tookLastTick = true;
							}
							else
								this.tookLastTick = false;
						}
						else
							this.tookLastTick = false;
						this.dmgLastTick = this.inventorySlots[0] == null ? 0 : TinkersConstructCompat.getDamage(this.inventorySlots[0]);
						return;
					}
			if (this.inventorySlots[0] != null && this.inventorySlots[0].getItemDamage() > 0)
			{
				int essentia = this.drawEssentia();
				int dmg = this.inventorySlots[0].getItemDamage();
				this.inventorySlots[0].setItemDamage(Math.max(0, dmg - essentia));
				this.markDirty();

				if (this.dmgLastTick != 0 && this.dmgLastTick != dmg)
				{
					ThaumicTinkerer.tcProxy.sparkle((float) (this.xCoord + 0.25 + Math.random() / 2F), (float) (this.yCoord + 1 + Math.random() / 2F), (float) (this.zCoord + 0.25 + Math.random() / 2F), 0);
					this.tookLastTick = true;
				}
				else
					this.tookLastTick = false;
			}
			else
				this.tookLastTick = false;

			this.dmgLastTick = this.inventorySlots[0] == null ? 0 : this.inventorySlots[0].getItemDamage();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);

		this.readCustomNBT(par1NBTTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);

		this.writeCustomNBT(par1NBTTagCompound);
	}

	public void readCustomNBT(NBTTagCompound par1NBTTagCompound)
	{
		NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		this.inventorySlots = new ItemStack[1];

		if (nbttaglist.tagCount() > 0)
		{
			NBTTagCompound tagList = nbttaglist.getCompoundTagAt(0);
			this.inventorySlots[0] = ItemStack.loadItemStackFromNBT(tagList);
		}
	}

	public void writeCustomNBT(NBTTagCompound par1NBTTagCompound)
	{
		NBTTagList nbttaglist = new NBTTagList();
		if (this.inventorySlots[0] != null)
		{
			NBTTagCompound tagList = new NBTTagCompound();
			tagList.setByte("Slot", (byte) 0);
			this.inventorySlots[0].writeToNBT(tagList);
			nbttaglist.appendTag(tagList);
		}
		par1NBTTagCompound.setTag("Items", nbttaglist);
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventorySlots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventorySlots[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.inventorySlots[i] != null)
		{
			ItemStack stackAt;

			if (this.inventorySlots[i].stackSize <= j)
			{
				stackAt = this.inventorySlots[i];
				this.inventorySlots[i] = null;
				return stackAt;
			}
			else
			{
				stackAt = this.inventorySlots[i].splitStack(j);

				if (this.inventorySlots[i].stackSize == 0)
					this.inventorySlots[i] = null;

				return stackAt;
			}
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return this.getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventorySlots[i] = itemstack;
	}

	@Override
	public String getInventoryName()
	{
		return LibBlockNames.REPAIRER;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64;
	}

	@Override
	public void openInventory()
	{

	}

	@Override
	public void closeInventory()
	{

	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeCustomNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, -999, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet)
	{
		super.onDataPacket(manager, packet);
		this.readCustomNBT(packet.func_148857_g());
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[] { 0 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		if (Loader.isModLoaded("TConstruct") && ConfigHandler.repairTConTools)
			if (TinkersConstructCompat.isTConstructTool(itemstack))
				return itemstack != null;
		return itemstack != null && itemstack.getItem().isRepairable();
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return true;
	}

	int drawEssentia()
	{
		ForgeDirection orientation = this.getOrientation();
		TileEntity te = ThaumcraftApiHelper.getConnectableTile(this.worldObj, this.xCoord, this.yCoord, this.zCoord, orientation);
		if (te != null)
		{
			IEssentiaTransport ic = (IEssentiaTransport) te;
			if (!ic.canOutputTo(orientation.getOpposite()))
				return 0;

			for (Aspect aspect : repairValues.keySet())
				if (ic.getSuctionType(orientation.getOpposite()) == aspect && ic.getSuctionAmount(orientation.getOpposite()) < this.getSuctionAmount(orientation) && ic.takeEssentia(aspect, 1, orientation.getOpposite()) == 1)
					return repairValues.get(aspect);
		}
		return 0;
	}

	ForgeDirection getOrientation()
	{
		return ForgeDirection.getOrientation(this.getBlockMetadata());
	}

	@Override
	public AspectList getAspects()
	{
		ItemStack stack = this.inventorySlots[0];
		if (stack == null)
			return null;
		if (Loader.isModLoaded("TConstruct") && ConfigHandler.repairTConTools)
			if (TinkersConstructCompat.isTConstructTool(stack))
				return new AspectList().add(Aspect.ENTROPY, TinkersConstructCompat.getDamage(stack));
		return new AspectList().add(Aspect.ENTROPY, stack.getItemDamage());
	}

	@Override
	public void setAspects(AspectList paramAspectList)
	{
	}

	@Override
	public boolean doesContainerAccept(Aspect paramAspect)
	{

		return false;
	}

	@Override
	public int addToContainer(Aspect paramAspect, int paramInt)
	{
		return 0;
	}

	@Override
	public boolean takeFromContainer(Aspect paramAspect, int paramInt)
	{
		return false;
	}

	@Override
	public boolean takeFromContainer(AspectList paramAspectList)
	{
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect paramAspect, int paramInt)
	{
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList paramAspectList)
	{
		return false;
	}

	@Override
	public int containerContains(Aspect paramAspect)
	{
		return 0;
	}

	@Override
	public boolean isConnectable(ForgeDirection paramForgeDirection)
	{
		return paramForgeDirection == this.getOrientation();
	}

	@Override
	public boolean canInputFrom(ForgeDirection paramForgeDirection)
	{
		return false;
	}

	@Override
	public boolean canOutputTo(ForgeDirection paramForgeDirection)
	{
		return this.isConnectable(paramForgeDirection);
	}

	@Override
	public void setSuction(Aspect paramAspect, int paramInt)
	{
	}

	@Override
	public int takeEssentia(Aspect paramAspect, int paramInt, ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public int getMinimumSuction()
	{
		return 0;
	}

	@Override
	public boolean renderExtendedTube()
	{
		return false;
	}

	@Override
	public int addEssentia(Aspect arg0, int arg1, ForgeDirection direction)
	{
		return 0;
	}

	@Override
	public int getEssentiaAmount(ForgeDirection arg0)
	{
		return 0;
	}

	@Override
	public Aspect getEssentiaType(ForgeDirection arg0)
	{
		return null;
	}

	@Override
	public int getSuctionAmount(ForgeDirection arg0)
	{
		return arg0 == this.getOrientation() ? 128 : 0;
	}

	@Override
	public Aspect getSuctionType(ForgeDirection arg0)
	{
		return arg0 == this.getOrientation() ? Aspect.TOOL : null;
	}

	@Override
	public boolean prepareToMove()
	{
		return true;
	}

	@Override
	public void doneMoving()
	{

	}

}