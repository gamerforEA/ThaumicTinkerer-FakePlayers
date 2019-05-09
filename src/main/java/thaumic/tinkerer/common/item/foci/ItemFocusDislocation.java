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
 * File Created @ [9 Sep 2013, 22:19:25 (GMT)]
 */
package thaumic.tinkerer.common.item.foci;

import com.gamerforea.eventhelper.util.EventUtils;
import com.gamerforea.ttinkerer.EventConfig;
import com.gamerforea.ttinkerer.ModUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.core.helper.ItemNBTHelper;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.ResearchHelper;
import thaumic.tinkerer.common.research.TTResearchItem;

import java.util.ArrayList;

public class ItemFocusDislocation extends ItemModFocus
{
	private static final String TAG_AVAILABLE = "available";
	private static final String TAG_TILE_CMP = "tileCmp";
	@Deprecated
	private static final String TAG_BLOCK_ID = "blockID";
	private static final String TAG_BLOCK_NAME = "blockName";
	private static final String TAG_BLOCK_META = "blockMeta";

	// TODO gamerforEA add multiplier
	private static final AspectList visUsage = new AspectList().add(Aspect.ENTROPY, (int) (500 * EventConfig.dislocationVisMultipier)).add(Aspect.ORDER, (int) (500 * EventConfig.dislocationVisMultipier)).add(Aspect.EARTH, (int) (100 * EventConfig.dislocationVisMultipier));

	// TODO gamerforEA add multiplier
	private static final AspectList visUsageTile = new AspectList().add(Aspect.ENTROPY, (int) (2500 * EventConfig.dislocationVisMultipier)).add(Aspect.ORDER, (int) (2500 * EventConfig.dislocationVisMultipier)).add(Aspect.EARTH, (int) (500 * EventConfig.dislocationVisMultipier));

	// TODO gamerforEA add multiplier
	private static final AspectList visUsageSpawner = new AspectList().add(Aspect.ENTROPY, (int) (10000 * EventConfig.dislocationVisMultipier)).add(Aspect.ORDER, (int) (10000 * EventConfig.dislocationVisMultipier)).add(Aspect.EARTH, (int) (5000 * EventConfig.dislocationVisMultipier));

	private static ArrayList<Block> blacklist = new ArrayList<>();
	private IIcon ornament;

	private static AspectList getCost(TileEntity tile)
	{
		return tile == null ? visUsage : tile instanceof TileEntityMobSpawner ? visUsageSpawner : visUsageTile;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		super.registerIcons(par1IconRegister);
		this.ornament = IconHelper.forItem(par1IconRegister, this, "Orn");
	}

	@Override
	public ItemStack onFocusRightClick(ItemStack wandStack, World world, EntityPlayer player, MovingObjectPosition mop)
	{
		if (mop == null)
			return wandStack;

		// TODO gamerforEA code start
		if (!EventConfig.enableFocusDislocation)
			return wandStack;
		// TODO gamerforEA code starend

		int x = mop.blockX;
		int y = mop.blockY;
		int z = mop.blockZ;
		int side = mop.sideHit;

		Block block = world.getBlock(x, y, z);
		int meta = world.getBlockMetadata(x, y, z);
		TileEntity tile = world.getTileEntity(x, y, z);

		// TODO gamerforEA code start
		if (ModUtils.isDoorBlock(block) || EventConfig.focusDislocationDenyInventory && tile instanceof IInventory || EventConfig.focusDislocationBlackList.contains(block, meta))
		{
			player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "Данный блок запрещено перемещать!"));
			return wandStack;
		}
		// TODO gamerforEA code end

		ItemWandCasting wand = (ItemWandCasting) wandStack.getItem();

		if (player.canPlayerEdit(x, y, z, side, wandStack))
		{
			// TODO gamerforEA code start
			if (EventUtils.cantBreak(player, x, y, z))
				return wandStack;
			// TODO gamerforEA code end

			ItemStack blockStack = this.getPickedBlock(wandStack);
			if (blockStack != null)
			{
				if (side == 0)
					--y;
				if (side == 1)
					++y;
				if (side == 2)
					--z;
				if (side == 3)
					++z;
				if (side == 4)
					--x;
				if (side == 5)
					++x;

				if (block.canPlaceBlockOnSide(world, x, y, z, side))
				{
					// TODO gamerforEA code start
					if (EventUtils.cantBreak(player, x, y, z))
						return wandStack;

					for (ForgeDirection direction : new ForgeDirection[] { ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST })
					{
						int xx = x + direction.offsetX;
						int yy = y + direction.offsetY;
						int zz = z + direction.offsetZ;
						if (world.blockExists(xx, yy, zz) && world.getBlock(xx, yy, zz) instanceof BlockChest && Blocks.chest.func_149951_m(world, xx, yy, zz) instanceof InventoryLargeChest)
							return wandStack;
					}
					// TODO gamerforEA code end

					if (!world.isRemote)
					{
						ItemBlock itemBlock = (ItemBlock) blockStack.getItem();

						// TODO gamerforEA code replace, old code:
						// world.setBlock(x, y, z, itemBlock.field_150939_a, blockStack.getItemDamage(), 1 | 2);
						if (ModUtils.isDoorBlock(itemBlock.field_150939_a))
						{
							this.clearPickedBlock(wandStack);
							return wandStack;
						}

						boolean blockPlaced = world.setBlock(x, y, z, itemBlock.field_150939_a, blockStack.getItemDamage(), 1 | 2);
						if (!blockPlaced)
							return wandStack;
						// TODO gamerforEA code end

						itemBlock.field_150939_a.onBlockPlacedBy(world, x, y, z, player, wandStack);
						NBTTagCompound tileCmp = this.getStackTileEntity(wandStack);
						if (tileCmp != null && !tileCmp.hasNoTags())
						{
							/* TODO gamerforEA code replace, old code:
							TileEntity tileCopy = TileEntity.createAndLoadEntity(tileCmp);
							tileCopy.xCoord = x;
							tileCopy.yCoord = y;
							tileCopy.zCoord = z;
							world.setTileEntity(x, y, z, tileCopy); */
							TileEntity tileCopy = createAndLoadEntity(tileCmp, x, y, z);
							if (tileCopy != null)
								world.setTileEntity(x, y, z, tileCopy);
							// TODO gamerforEA code end
						}
					}
					else
						player.swingItem();

					this.clearPickedBlock(wandStack);

					for (int i = 0; i < 8; i++)
					{
						float xx = (float) (x + Math.random());
						float yy = (float) (y + Math.random()) + 0.65F;
						float zz = (float) (z + Math.random());
						ThaumicTinkerer.tcProxy.burst(world, xx, yy, zz, 0.2F);
					}
					world.playSoundAtEntity(player, "thaumcraft:wand", 0.5F, 1F);
				}
			}
			else if (!blacklist.contains(block) && !ThaumcraftApi.portableHoleBlackList.contains(block) && block != null && block.getBlockHardness(world, x, y, z) != -1F && wand.consumeAllVis(wandStack, player, getCost(tile), true, false))
			{
				if (!world.isRemote)
				{
					/* TODO gamerforEA code replace, old code:
					world.removeTileEntity(x, y, z);
					world.setBlock(x, y, z, Blocks.air, 0, 1 | 2);
					this.storePickedBlock(wandStack, block, (short) meta, tile); */
					NBTTagCompound tileNbt = getTileNBT(tile);

					world.removeTileEntity(x, y, z);
					boolean blockRemoved = world.setBlock(x, y, z, Blocks.air, 0, 1 | 2);

					if (!blockRemoved && tileNbt != null && !tileNbt.hasNoTags())
					{
						TileEntity tileCopy = createAndLoadEntity(tileNbt, x, y, z);
						if (tileCopy != null)
							world.setTileEntity(x, y, z, tileCopy);
						return wandStack;
					}

					this.storePickedBlock(wandStack, block, (short) meta, tileNbt);

					if (tile instanceof IInventory)
					{
						IInventory inventory = (IInventory) tile;
						for (int slot = 0; slot < inventory.getSizeInventory(); slot++)
						{
							ItemStack stackInSlot = inventory.getStackInSlot(slot);
							if (stackInSlot != null)
								inventory.setInventorySlotContents(slot, null);
						}
					}

					if (EventConfig.focusDislocationNotifyNeighbors)
						world.notifyBlockChange(x, y, z, Blocks.air);
					// TODO gamerforEA code end
				}

				for (int i = 0; i < 8; i++)
				{
					float xx = (float) (x + Math.random());
					float yy = (float) (y + Math.random());
					float zz = (float) (z + Math.random());
					ThaumicTinkerer.tcProxy.burst(world, xx, yy, zz, 0.2F);
				}

				world.playSoundAtEntity(player, block.stepSound.getBreakSound(), 1F, 1F);
				world.playSoundAtEntity(player, "thaumcraft:wand", 0.5F, 1F);

				if (world.isRemote)
					player.swingItem();
			}
		}

		return wandStack;
	}

	@Override
	public String getSortingHelper(ItemStack itemstack)
	{
		return "DISLOCATION" + this.getUniqueKey(itemstack);
	}

	public String getUniqueKey(ItemStack itemstack)
	{
		ItemStack stack = this.getPickedBlock(itemstack);
		if (stack == null)
			return "";
		String name = stack.getUnlocalizedName();
		int datahash = 0;
		if (stack.getTagCompound() != null)
			datahash = stack.getTagCompound().hashCode();
		return String.format("%s-%d", name, datahash);
	}

	public ItemStack getPickedBlock(ItemStack stack)
	{
		ItemStack focus;
		if (stack.getItem() instanceof ItemWandCasting)
		{
			ItemWandCasting wand = (ItemWandCasting) stack.getItem();
			focus = wand.getFocusItem(stack);
		}
		else
			focus = stack;
		return ItemNBTHelper.getBoolean(focus, TAG_AVAILABLE, false) ? this.getPickedBlockStack(stack) : null;
	}

	public ItemStack getPickedBlockStack(ItemStack stack)
	{
		ItemStack focus;
		if (stack.getItem() instanceof ItemWandCasting)
		{
			ItemWandCasting wand = (ItemWandCasting) stack.getItem();
			focus = wand.getFocusItem(stack);
		}
		else
			focus = stack;
		String name = ItemNBTHelper.getString(focus, TAG_BLOCK_NAME, "");
		Block block = Block.getBlockFromName(name);
		if (block == Blocks.air)
		{
			int id = ItemNBTHelper.getInt(focus, TAG_BLOCK_ID, 0);
			block = Block.getBlockById(id);
		}
		int meta = ItemNBTHelper.getInt(focus, TAG_BLOCK_META, 0);
		return new ItemStack(new ItemBlock(block), 1, meta);
	}

	public NBTTagCompound getStackTileEntity(ItemStack stack)
	{
		ItemStack focus;
		if (stack.getItem() instanceof ItemWandCasting)
		{
			ItemWandCasting wand = (ItemWandCasting) stack.getItem();
			focus = wand.getFocusItem(stack);
		}
		else
			focus = stack;
		return ItemNBTHelper.getCompound(focus, TAG_TILE_CMP, true);
	}

	// TODO gamerforEA code start
	private void storePickedBlock(ItemStack stack, Block block, short meta, NBTTagCompound tileNbt)
	{
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		ItemStack focus = wand.getFocusItem(stack);
		String blockName = Block.blockRegistry.getNameForObject(block);
		ItemNBTHelper.setString(focus, TAG_BLOCK_NAME, blockName);
		ItemNBTHelper.setInt(focus, TAG_BLOCK_META, meta);
		NBTTagCompound cmp = tileNbt == null ? new NBTTagCompound() : tileNbt;
		ItemNBTHelper.setCompound(focus, TAG_TILE_CMP, cmp);
		ItemNBTHelper.setBoolean(focus, TAG_AVAILABLE, true);
		wand.setFocus(stack, focus);
	}

	private static NBTTagCompound getTileNBT(TileEntity tile)
	{
		if (tile == null)
			return null;
		NBTTagCompound nbt = new NBTTagCompound();
		tile.writeToNBT(nbt);
		return nbt;
	}

	private static TileEntity createAndLoadEntity(NBTTagCompound nbt, int x, int y, int z)
	{
		if (nbt == null || nbt.hasNoTags())
			return null;

		TileEntity tile = TileEntity.createAndLoadEntity(nbt);
		if (tile == null)
			return null;

		tile.xCoord = x;
		tile.yCoord = y;
		tile.zCoord = z;

		return tile;
	}
	// TODO gamerforEA code end

	private void storePickedBlock(ItemStack stack, Block block, short meta, TileEntity tile)
	{
		/* TODO gamerforEA code replace, old code:
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		ItemStack focus = wand.getFocusItem(stack);
		String blockName = Block.blockRegistry.getNameForObject(block);
		ItemNBTHelper.setString(focus, TAG_BLOCK_NAME, blockName);
		ItemNBTHelper.setInt(focus, TAG_BLOCK_META, meta);
		NBTTagCompound cmp = new NBTTagCompound();
		if (tile != null)
			tile.writeToNBT(cmp);
		ItemNBTHelper.setCompound(focus, TAG_TILE_CMP, cmp);
		ItemNBTHelper.setBoolean(focus, TAG_AVAILABLE, true);
		wand.setFocus(stack, focus); */
		this.storePickedBlock(stack, block, meta, getTileNBT(tile));
		// TODO gamerforEA code end
	}

	private void clearPickedBlock(ItemStack stack)
	{
		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		ItemStack focus = wand.getFocusItem(stack);
		ItemNBTHelper.setBoolean(focus, TAG_AVAILABLE, false);
		wand.setFocus(stack, focus);
	}

	@Override
	public int getFocusColor(ItemStack stack)
	{
		return 0xFFB200;
	}

	@Override
	public IIcon getOrnament(ItemStack stack)
	{
		return this.ornament;
	}

	@Override
	public AspectList getVisCost(ItemStack stack)
	{
		return visUsage;
	}

	static
	{
		blacklist.add(Blocks.piston_extension);
		blacklist.add(Blocks.piston_head);
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.FOCUS_DISLOCATION;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		if (!Config.allowMirrors)
			return null;
		return (TTResearchItem) new TTResearchItem(LibResearch.KEY_FOCUS_DISLOCATION, new AspectList().add(Aspect.ELDRITCH, 2).add(Aspect.MAGIC, 1).add(Aspect.EXCHANGE, 1), -5, -5, 2, new ItemStack(this)).setSecondary().setParents(LibResearch.KEY_FOCUS_FLIGHT).setConcealed().setPages(new ResearchPage("0"), new ResearchPage("1"), ResearchHelper.infusionPage(LibResearch.KEY_FOCUS_DISLOCATION));
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_FOCUS_DISLOCATION, new ItemStack(this), 8, new AspectList().add(Aspect.ELDRITCH, 20).add(Aspect.DARKNESS, 10).add(Aspect.VOID, 25).add(Aspect.MAGIC, 20).add(Aspect.TAINT, 5), new ItemStack(Items.ender_pearl), new ItemStack(Items.quartz), new ItemStack(Items.quartz), new ItemStack(Items.quartz), new ItemStack(Items.quartz), new ItemStack(ConfigItems.itemResource, 1, 6), new ItemStack(ConfigItems.itemResource, 1, 6), new ItemStack(ConfigItems.itemResource, 1, 6), new ItemStack(Items.diamond));
	}
}