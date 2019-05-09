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
 * File Created @ [14 Sep 2013, 01:07:25 (GMT)]
 */
package thaumic.tinkerer.common.block.tile;

import appeng.api.movable.IMovableTile;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumic.tinkerer.common.core.helper.Tuple4Int;
import thaumic.tinkerer.common.enchantment.core.EnchantmentManager;
import thaumic.tinkerer.common.lib.LibBlockNames;
import thaumic.tinkerer.common.lib.LibFeatures;

import java.util.ArrayList;
import java.util.List;

public class TileEnchanter extends TileEntity implements ISidedInventory, IMovableTile
{

	private static final String TAG_ENCHANTS = "enchantsIntArray";
	private static final String TAG_LEVELS = "levelsIntArray";
	private static final String TAG_TOTAL_ASPECTS = "totalAspects";
	private static final String TAG_CURRENT_ASPECTS = "currentAspects";
	private static final String TAG_WORKING = "working";

	public List<Integer> enchantments = new ArrayList();
	public List<Integer> levels = new ArrayList();

	public AspectList totalAspects = new AspectList();
	public AspectList currentAspects = new AspectList();

	public boolean working = false;
	ItemStack[] inventorySlots = new ItemStack[2];
	private List<Tuple4Int> pillars = new ArrayList();

	public void clearEnchants()
	{
		this.enchantments.clear();
		this.levels.clear();
	}

	public void appendEnchant(int enchant)
	{
		this.enchantments.add(enchant);
	}

	public void appendLevel(int level)
	{
		this.levels.add(level);
	}

	public void removeEnchant(int index)
	{
		this.enchantments.remove(index);
	}

	public void removeLevel(int index)
	{
		this.levels.remove(index);
	}

	public void setEnchant(int index, int enchant)
	{
		this.enchantments.set(index, enchant);
	}

	public void setLevel(int index, int level)
	{
		this.levels.set(index, level);
	}

	@Override
	public void updateEntity()
	{
		// TODO gamerforEA add condition [2]
		if (this.getStackInSlot(0) == null || this.getStackInSlot(0).isItemEnchanted())
		{
			this.enchantments.clear();
			this.levels.clear();
		}

		if (this.working)
		{
			ItemStack tool = this.getStackInSlot(0);
			if (tool == null)
			{
				this.working = false;
				return;
			}

			// TODO gamerforEA code start
			if (tool.isItemEnchanted())
			{
				this.working = false;
				return;
			}
			// TODO gamerforEA code end

			this.checkPillars();

			if (!this.working) // Pillar check
				return;

			enchantItem:
			{
				for (Aspect aspect : LibFeatures.PRIMAL_ASPECTS)
				{
					int currentAmount = this.currentAspects.getAmount(aspect);
					int totalAmount = this.totalAspects.getAmount(aspect);

					if (currentAmount < totalAmount)
						break enchantItem;
				}

				this.working = false;
				this.currentAspects = new AspectList();
				this.totalAspects = new AspectList();

				for (int i = 0; i < this.enchantments.size(); i++)
				{
					int enchant = this.enchantments.get(i);
					int level = this.levels.get(i);

					if (!this.worldObj.isRemote)
						tool.addEnchantment(Enchantment.enchantmentsList[enchant], level);
				}

				this.enchantments.clear();
				this.levels.clear();
				this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, "thaumcraft:wand", 1F, 1F);
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				return;
			}

			ItemStack wand = this.getStackInSlot(1);

			if (wand != null && wand.getItem() instanceof ItemWandCasting && !((ItemWandCasting) wand.getItem()).isStaff(wand))
			{
				ItemWandCasting wandItem = (ItemWandCasting) wand.getItem();
				AspectList wandAspects = wandItem.getAllVis(wand);

				int missing, onWand;
				List<Aspect> aspectsThatCanGet = new ArrayList();

				for (Aspect aspect : LibFeatures.PRIMAL_ASPECTS)
				{
					missing = this.totalAspects.getAmount(aspect) - this.currentAspects.getAmount(aspect);
					onWand = wandAspects.getAmount(aspect);

					if (missing > 0 && onWand >= 100)
						aspectsThatCanGet.add(aspect);
				}

				int i = aspectsThatCanGet.isEmpty() ? 0 : this.worldObj.rand.nextInt(aspectsThatCanGet.size());
				Aspect aspect = aspectsThatCanGet.isEmpty() ? null : aspectsThatCanGet.get(i);

				if (aspect != null)
				{
					this.consumeAllVisCrafting(wand, null, new AspectList().add(aspect, 1), true, wandItem);
					this.currentAspects.add(aspect, 1);
					Tuple4Int p = this.pillars.get(i);
					if (this.worldObj.rand.nextBoolean())
					{
						Thaumcraft.proxy.blockRunes(this.worldObj, p.i1, p.i4 - 0.75, p.i3, 0.3F + this.worldObj.rand.nextFloat() * 0.7F, 0.0F, 0.3F + this.worldObj.rand.nextFloat() * 0.7F, 15, this.worldObj.rand.nextFloat() / 8F);
						Thaumcraft.proxy.blockRunes(this.worldObj, this.xCoord, this.yCoord + 0.25, this.zCoord, 0.3F + this.worldObj.rand.nextFloat() * 0.7F, 0.0F, 0.3F + this.worldObj.rand.nextFloat() * 0.7F, 15, this.worldObj.rand.nextFloat() / 8F);
						if (this.worldObj.rand.nextInt(5) == 0)
							this.worldObj.playSoundEffect(p.i1, p.i2, p.i3, "thaumcraft:brain", 0.5F, 1F);
					}

				}
			}
		}
	}

	public boolean consumeAllVisCrafting(ItemStack is, EntityPlayer player, AspectList aspects, boolean doit, ItemWandCasting wandItem)
	{
		if (aspects != null && aspects.size() != 0)
		{
			AspectList aspectList = new AspectList();

			for (Aspect aspect : aspects.getAspects())
			{
				int cost = aspects.getAmount(aspect) * 100;
				aspectList.add(aspect, cost);
			}
			aspects = aspectList;
			if (aspects != null && aspects.size() != 0)
			{
				AspectList nl = new AspectList();

				for (Aspect aspect : aspects.getAspects())
				{
					int cost = aspects.getAmount(aspect);
					cost = (int) (cost * wandItem.getConsumptionModifier(is, player, aspect, true));
					nl.add(aspect, cost);
				}

				for (Aspect aspect : nl.getAspects())
				{
					if (wandItem.getVis(is, aspect) < nl.getAmount(aspect))
						return false;
				}

				if (doit && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER)
					for (Aspect aspect : nl.getAspects())
					{
						wandItem.storeVis(is, aspect, wandItem.getVis(is, aspect) - nl.getAmount(aspect));
					}

				return true;
			}
			else
				return false;
		}
		else
			return false;
	}

	@Override
	public void markDirty()
	{
		super.markDirty();
		if (!this.worldObj.isRemote && !this.working)
		{
			this.enchantments.clear();
			this.levels.clear();
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	public boolean checkPillars()
	{
		if (this.pillars.isEmpty())
		{
			if (this.assignPillars())
			{
				this.working = false;
				this.currentAspects = new AspectList();
				return false;
			}
			return true;
		}

		for (int i = 0; i < this.pillars.size(); i++)
		{
			Tuple4Int pillar = this.pillars.get(i);
			int pillarHeight = this.findPillar(pillar.i1, pillar.i2, pillar.i3);
			if (pillarHeight == -1)
			{
				this.pillars.clear();
				return this.checkPillars();
			}
			else if (pillarHeight != pillar.i4)
				pillar.i4 = pillarHeight;
		}

		return true;
	}

	public boolean assignPillars()
	{
		int y = this.yCoord;
		for (int x = this.xCoord - 4; x <= this.xCoord + 4; x++)
		{
			for (int z = this.zCoord - 4; z <= this.zCoord + 4; z++)
			{
				int height = this.findPillar(x, y, z);
				if (height != -1)
					this.pillars.add(new Tuple4Int(x, y, z, height));

				if (this.pillars.size() == 6)
					return false;
			}
		}

		this.pillars.clear();
		return true;
	}

	public int findPillar(int x, int y, int z)
	{
		int obsidianFound = 0;
		for (int i = 0; true; i++)
		{
			if (y + i >= 256)
				return -1;

			Block id = this.worldObj.getBlock(x, y + i, z);
			int meta = this.worldObj.getBlockMetadata(x, y + i, z);
			if (id == ConfigBlocks.blockCosmeticSolid && meta == 0)
			{
				++obsidianFound;
				continue;
			}
			if (id == ConfigBlocks.blockAiry && meta == 1)
			{
				if (obsidianFound >= 2 && obsidianFound < 13)
					return y + i;
				return -1;
			}

			return -1;
		}
	}

	public void updateAspectList()
	{
		this.totalAspects = new AspectList();
		for (int i = 0; i < this.enchantments.size(); i++)
		{
			int enchant = this.enchantments.get(i);
			int level = this.levels.get(i);

			AspectList aspects = EnchantmentManager.enchantmentData.get(enchant).get(level).aspects;
			for (Aspect aspect : aspects.getAspectsSorted())
			{
				this.totalAspects.add(aspect, aspects.getAmount(aspect));
			}
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
		this.working = par1NBTTagCompound.getBoolean(TAG_WORKING);
		this.currentAspects.readFromNBT(par1NBTTagCompound.getCompoundTag(TAG_CURRENT_ASPECTS));
		this.totalAspects.readFromNBT(par1NBTTagCompound.getCompoundTag(TAG_TOTAL_ASPECTS));

		this.enchantments.clear();
		for (int i : par1NBTTagCompound.getIntArray(TAG_ENCHANTS))
		{
			this.enchantments.add(i);
		}
		this.levels.clear();
		for (int i : par1NBTTagCompound.getIntArray(TAG_LEVELS))
		{
			this.levels.add(i);
		}

		NBTTagList var2 = par1NBTTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		this.inventorySlots = new ItemStack[this.getSizeInventory()];
		for (int var3 = 0; var3 < var2.tagCount(); ++var3)
		{
			NBTTagCompound var4 = var2.getCompoundTagAt(var3);
			byte var5 = var4.getByte("Slot");
			if (var5 >= 0 && var5 < this.inventorySlots.length)
				this.inventorySlots[var5] = ItemStack.loadItemStackFromNBT(var4);
		}
	}

	public void writeCustomNBT(NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setIntArray(TAG_LEVELS, ArrayUtils.toPrimitive(this.levels.toArray(new Integer[0])));

		par1NBTTagCompound.setIntArray(TAG_ENCHANTS, ArrayUtils.toPrimitive(this.enchantments.toArray(new Integer[0])));

		NBTTagCompound totalAspectsCmp = new NBTTagCompound();
		this.totalAspects.writeToNBT(totalAspectsCmp);

		NBTTagCompound currentAspectsCmp = new NBTTagCompound();
		this.currentAspects.writeToNBT(currentAspectsCmp);

		par1NBTTagCompound.setBoolean(TAG_WORKING, this.working);
		par1NBTTagCompound.setTag(TAG_TOTAL_ASPECTS, totalAspectsCmp);
		par1NBTTagCompound.setTag(TAG_CURRENT_ASPECTS, currentAspectsCmp);
		NBTTagList var2 = new NBTTagList();
		for (int var3 = 0; var3 < this.inventorySlots.length; ++var3)
		{
			if (this.inventorySlots[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
				this.inventorySlots[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}
		par1NBTTagCompound.setTag("Items", var2);
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
		return LibBlockNames.ENCHANTER;
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
		return false;
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
	{
		super.onDataPacket(net, pkt);
		this.readCustomNBT(pkt.func_148857_g());
	}

	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeCustomNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, -999, nbttagcompound);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return false;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return false;
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
