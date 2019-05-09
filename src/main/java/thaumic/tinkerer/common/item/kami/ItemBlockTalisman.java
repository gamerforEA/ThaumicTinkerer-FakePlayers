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
 * File Created @ [Dec 30, 2013, 12:46:22 AM (GMT)]
 */
package thaumic.tinkerer.common.item.kami;

import baubles.api.BaubleType;
import baubles.api.IBauble;
import com.gamerforea.ttinkerer.EventConfig;
import com.gamerforea.ttinkerer.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.config.ConfigItems;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.tile.transvector.TileTransvectorInterface;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.core.helper.ItemNBTHelper;
import thaumic.tinkerer.common.core.proxy.TTCommonProxy;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ItemKamiBase;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.KamiResearchItem;
import thaumic.tinkerer.common.research.ResearchHelper;

import java.util.Arrays;
import java.util.List;

public class ItemBlockTalisman extends ItemKamiBase implements IBauble
{
	@Deprecated
	private static final String TAG_BLOCK_ID = "blockID";
	private static final String TAG_BLOCK_NAME = "blockName";
	private static final String TAG_BLOCK_META = "blockMeta";
	private static final String TAG_BLOCK_COUNT = "blockCount";

	IIcon enabledIcon;

	public ItemBlockTalisman()
	{
		this.setMaxStackSize(1);
		this.setHasSubtypes(true);
	}

	private static void setCount(ItemStack stack, int count)
	{
		ItemNBTHelper.setInt(stack, TAG_BLOCK_COUNT, count);
	}

	public static int remove(ItemStack stack, int count)
	{
		int current = getBlockCount(stack);
		setCount(stack, Math.max(current - count, 0));

		return Math.min(current, count);
	}

	@Deprecated
	public static int getBlockID(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_ID, 0);
	}

	public static String getBlockName(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, TAG_BLOCK_NAME, "");
	}

	public static Block getBlock(ItemStack stack)
	{
		Block block = Block.getBlockFromName(getBlockName(stack));
		if (block == Blocks.air)
			block = Block.getBlockById(getBlockID(stack));
		return block;
	}

	public static int getBlockMeta(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_META, 0);
	}

	public static int getBlockCount(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_COUNT, 0);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if ((getBlock(par1ItemStack) != Blocks.air || getBlockID(par1ItemStack) != 0) && par3EntityPlayer.isSneaking())
		{
			int dmg = par1ItemStack.getItemDamage();
			par1ItemStack.setItemDamage(~dmg & 1);
			par2World.playSoundAtEntity(par3EntityPlayer, "random.orb", 0.3F, 0.1F);
		}
		return par1ItemStack;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);

		// TODO gamerforEA code start
		if (EventConfig.blockTalismanBlackList.contains(block, meta))
		{
			player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "Данное взаимодействие запрещено!"));
			return false;
		}
		// TODO gamerforEA code end

		boolean set = this.setBlock(stack, block, meta);

		if (!set)
		{
			Block bBlock = getBlock(stack);
			int bMeta = getBlockMeta(stack);
			ItemStack bStack = new ItemStack(bBlock, 1, bMeta);
			Item bItem = bStack.getItem();
			if (bItem != null)
			{
				int maxSize = bStack.getMaxStackSize();
				TileEntity tile = world.getTileEntity(x, y, z);
				if (tile instanceof IInventory)
				{
					IInventory inv = (IInventory) tile;
					int[] slots = inv instanceof ISidedInventory ? ((ISidedInventory) inv).getAccessibleSlotsFromSide(side) : TileTransvectorInterface.buildSlotsForLinearInventory(inv);
					for (int slot : slots)
					{
						ItemStack stackInSlot = inv.getStackInSlot(slot);
						if (stackInSlot == null)
						{
							ItemStack newStack = bStack.copy();
							newStack.stackSize = remove(stack, maxSize);
							if (newStack.stackSize > 0)
								if (inv.isItemValidForSlot(slot, newStack) && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(slot, newStack, side)))
								{
									inv.setInventorySlotContents(slot, newStack);
									inv.markDirty();
									set = true;
								}
						}
						else
						{
							// TODO gamerforEA code replace, old code:
							// if (stackInSlot.getItem() == bItem && stackInSlot.getItemDamage() == bMeta)
							if (ModUtils.isItemEqual(stackInSlot, bStack))
							// TODO gamerforEA code end
							{
								int missing = maxSize - stackInSlot.stackSize;
								if (inv.isItemValidForSlot(slot, stackInSlot) && (!(inv instanceof ISidedInventory) || ((ISidedInventory) inv).canInsertItem(slot, stackInSlot, side)))
								{
									stackInSlot.stackSize += remove(stack, missing);
									inv.markDirty();
									set = true;
								}
							}
						}
					}
				}
				else
				{
					int remove = remove(stack, 1);
					if (remove > 0)
					{
						bItem.onItemUse(bStack.copy(), player, world, x, y, z, side, par8, par9, par10);
						set = true;
					}
				}
			}
		}

		player.setCurrentItemOrArmor(0, stack);

		return set;
	}

	private boolean setBlock(ItemStack stack, Block block, int meta)
	{
		if (getBlock(stack) == Blocks.air || getBlockCount(stack) == 0)
		{
			ItemNBTHelper.setString(stack, TAG_BLOCK_NAME, Block.blockRegistry.getNameForObject(block));
			ItemNBTHelper.setInt(stack, TAG_BLOCK_META, meta);
			return true;
		}
		return false;
	}

	private void add(ItemStack stack, int count)
	{
		int current = getBlockCount(stack);
		setCount(stack, current + count);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = IconHelper.forItem(par1IconRegister, this, 0);
		this.enabledIcon = IconHelper.forItem(par1IconRegister, this, 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int par1)
	{
		return par1 == 1 ? this.enabledIcon : this.itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		Block block = getBlock(par1ItemStack);
		if (block != null && block != Blocks.air)
		{
			int count = getBlockCount(par1ItemStack);
			par3List.add(StatCollector.translateToLocal(new ItemStack(block, 1, getBlockMeta(par1ItemStack)).getUnlocalizedName() + ".name") + " (x" + count + ")");
		}

		if (par1ItemStack.getItemDamage() == 1)
			par3List.add(StatCollector.translateToLocal("ttmisc.active"));
		else
			par3List.add(StatCollector.translateToLocal("ttmisc.inactive"));
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return TTCommonProxy.kamiRarity;
	}

	@Override
	public BaubleType getBaubleType(ItemStack itemstack)
	{
		return BaubleType.RING;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity)
	{
		Block bBlock = getBlock(stack);
		if (!entity.worldObj.isRemote && bBlock != Blocks.air && entity instanceof EntityPlayer && stack.getItemDamage() == 1)
		{
			EntityPlayer player = (EntityPlayer) entity;
			int bMeta = getBlockMeta(stack);
			ItemStack bStack = new ItemStack(bBlock, 1, bMeta);
			Item bItem = bStack.getItem();
			if (bItem == null)
				return;
			int maxSize = bStack.getMaxStackSize();
			int highest = -1;
			boolean hasFreeSlot = false;
			int[] counts = new int[player.inventory.getSizeInventory() - player.inventory.armorInventory.length];
			Arrays.fill(counts, 0);

			for (int i = 0; i < counts.length; i++)
			{
				ItemStack stackInSlot = player.inventory.getStackInSlot(i);
				if (stackInSlot == null)
				{
					hasFreeSlot = true;
					continue;
				}

				// TODO gamerforEA code replace, old code:
				// if (bItem == stackInSlot.getItem() && stackInSlot.getItemDamage() == bMeta)
				if (ModUtils.isItemEqual(bStack, stackInSlot))
				// TODO gamerforEA code end
				{
					counts[i] = stackInSlot.stackSize;
					if (highest == -1)
						highest = i;
					else
						highest = counts[i] > counts[highest] && highest > 8 ? i : highest;
				}
			}

			if (highest == -1)
			{
				ItemStack heldItem = player.inventory.getItemStack();

				// TODO gamerforEA code replace, old code:
				// if (hasFreeSlot && (heldItem == null || bItem == heldItem.getItem() || heldItem.getItemDamage() != bMeta))
				if (hasFreeSlot && (heldItem == null || ModUtils.isItemEqual(bStack, heldItem)))
				// TODO gamerforEA code end
				{
					ItemStack newStack = bStack.copy();

					// TODO gamerforEA code replace, old code:
					// newStack.stackSize = remove(stack, 64);
					newStack.stackSize = remove(stack, maxSize);
					// TODO gamerforEA code end

					if (newStack.stackSize != 0)
						player.inventory.addItemStackToInventory(newStack);
				}
			}
			else
			{
				for (int i = 0; i < counts.length; i++)
				{
					int count = counts[i];

					if (i == highest || count == 0)
						continue;

					this.add(stack, count);
					player.inventory.setInventorySlotContents(i, null);
				}

				int countInHighest = counts[highest];
				if (countInHighest < maxSize)
				{
					int missing = maxSize - countInHighest;
					ItemStack stackInHighest = player.inventory.getStackInSlot(highest);
					stackInHighest.stackSize += remove(stack, missing);
				}
			}
		}
	}

	@Override
	public void onEquipped(ItemStack itemstack, EntityLivingBase player)
	{

	}

	@Override
	public void onUnequipped(ItemStack itemstack, EntityLivingBase player)
	{

	}

	@Override
	public boolean canEquip(ItemStack itemstack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	public boolean canUnequip(ItemStack itemstack, EntityLivingBase player)
	{
		return true;
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.BLOCK_TALISMAN;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		if (!ConfigHandler.enableKami)
			return null;
		return (IRegisterableResearch) new KamiResearchItem(LibResearch.KEY_BLOCK_TALISMAN, new AspectList().add(Aspect.VOID, 2).add(Aspect.DARKNESS, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MAGIC, 1), 14, 17, 5, new ItemStack(this)).setParents(LibResearch.KEY_ICHOR_PICK_GEM, LibResearch.KEY_ICHOR_SHOVEL_GEM).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_BLOCK_TALISMAN));

	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_BLOCK_TALISMAN, new ItemStack(this), 9, new AspectList().add(Aspect.VOID, 65).add(Aspect.DARKNESS, 32).add(Aspect.MAGIC, 50).add(Aspect.ELDRITCH, 32), new ItemStack(ConfigItems.itemFocusPortableHole), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)), new ItemStack(Blocks.ender_chest), new ItemStack(Items.diamond), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)), new ItemStack(ConfigItems.itemResource, 1, 11), new ItemStack(ConfigBlocks.blockJar, 1, 3));

	}
}
