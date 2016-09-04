package thaumic.tinkerer.common.item.kami;

import java.util.ArrayList;
import java.util.List;

import com.gamerforea.eventhelper.util.EventUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumic.tinkerer.client.core.handler.kami.ToolModeHUDHandler;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.core.helper.ItemNBTHelper;
import thaumic.tinkerer.common.core.proxy.TTCommonProxy;
import thaumic.tinkerer.common.item.kami.tool.ToolHandler;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ItemKamiBase;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.KamiResearchItem;
import thaumic.tinkerer.common.research.ResearchHelper;

public class ItemPlacementMirror extends ItemKamiBase
{

	@Deprecated
	private static final String TAG_BLOCK_ID = "blockID";

	private static final String TAG_BLOCK_NAME = "blockName";
	private static final String TAG_BLOCK_META = "blockMeta";
	private static final String TAG_SIZE = "size";

	IIcon[] icons = new IIcon[2];

	public ItemPlacementMirror()
	{
		super();
		this.setMaxStackSize(1);
	}

	public static boolean hasBlocks(ItemStack stack, EntityPlayer player, ChunkCoordinates[] blocks)
	{
		if (player.capabilities.isCreativeMode)
			return true;

		int required = blocks.length;
		int current = 0;
		ItemStack reqStack = new ItemStack(getBlock(stack), 1, getBlockMeta(stack));
		List<ItemStack> talismansToCheck = new ArrayList();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (stackInSlot != null && stackInSlot.getItem() == reqStack.getItem() && stackInSlot.getItemDamage() == reqStack.getItemDamage())
			{
				current += stackInSlot.stackSize;
				if (current >= required)
					return true;
			}
			if (stackInSlot != null && stackInSlot.getItem() == ThaumicTinkerer.registry.getFirstItemFromClass(ItemBlockTalisman.class))
				talismansToCheck.add(stackInSlot);
		}

		for (ItemStack talisman : talismansToCheck)
		{
			Block block = ItemBlockTalisman.getBlock(talisman);
			int meta = ItemBlockTalisman.getBlockMeta(talisman);

			if (Item.getItemFromBlock(block) == reqStack.getItem() && meta == reqStack.getItemDamage())
			{
				current += ItemBlockTalisman.getBlockCount(talisman);

				if (current >= required)
					return true;
			}
		}

		return false;
	}

	public static ChunkCoordinates[] getBlocksToPlace(ItemStack stack, EntityPlayer player)
	{
		List<ChunkCoordinates> coords = new ArrayList();
		MovingObjectPosition pos = ToolHandler.raytraceFromEntity(player.worldObj, player, true, 5);
		if (pos != null)
		{
			Block block = player.worldObj.getBlock(pos.blockX, pos.blockY, pos.blockZ);
			if (block != null && block.isReplaceable(player.worldObj, pos.blockX, pos.blockY, pos.blockZ))
				pos.blockY--;

			ForgeDirection dir = ForgeDirection.getOrientation(pos.sideHit);
			int rotation = MathHelper.floor_double(player.rotationYaw * 4F / 360F + 0.5D) & 3;
			int range = (getSize(stack) ^ 1) / 2;

			boolean topOrBottom = dir == ForgeDirection.UP || dir == ForgeDirection.DOWN;

			int xOff = !(dir == ForgeDirection.WEST || dir == ForgeDirection.EAST) ? topOrBottom ? player.rotationPitch > 75 || (rotation & 1) == 0 ? range : 0 : range : 0;
			int yOff = topOrBottom ? player.rotationPitch > 75 ? 0 : range : range;
			int zOff = !(dir == ForgeDirection.SOUTH || dir == ForgeDirection.NORTH) ? topOrBottom ? player.rotationPitch > 75 || (rotation & 1) == 1 ? range : 0 : range : 0;

			for (int x = -xOff; x < xOff + 1; x++)
				for (int y = 0; y < yOff * 2 + 1; y++)
					for (int z = -zOff; z < zOff + 1; z++)
					{
						int xp = pos.blockX + x + dir.offsetX;
						int yp = pos.blockY + y + dir.offsetY;
						int zp = pos.blockZ + z + dir.offsetZ;

						Block block1 = player.worldObj.getBlock(xp, yp, zp);
						if (block1 == null || block1.isAir(player.worldObj, xp, yp, zp) || block1.isReplaceable(player.worldObj, xp, yp, zp))
							// TODO gamerforEA code start
							if (!EventUtils.cantBreak(player, xp, yp, zp))
								// TODO gamerforEA code end
								coords.add(new ChunkCoordinates(xp, yp, zp));
					}

		}

		return coords.toArray(new ChunkCoordinates[coords.size()]);
	}

	private static void setSize(ItemStack stack, int size)
	{
		ItemNBTHelper.setInt(stack, TAG_SIZE, size | 1);
	}

	public static int getSize(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_SIZE, 3) | 1;
	}

	@Deprecated
	public static int getBlockID(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_ID, 0);
	}

	public static Block getBlock(ItemStack stack)
	{
		Block block = Block.getBlockFromName(getBlockName(stack));
		if (block == Blocks.air)
			block = Block.getBlockById(getBlockID(stack));

		return block;
	}

	public static String getBlockName(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, TAG_BLOCK_NAME, "");
	}

	public static int getBlockMeta(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, TAG_BLOCK_META, 0);
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		Block block = par3World.getBlock(par4, par5, par6);
		int meta = par3World.getBlockMetadata(par4, par5, par6);

		if (par2EntityPlayer.isSneaking())
		{
			if (block != null && block.getRenderType() == 0)
				this.setBlock(par1ItemStack, block, meta);
		}
		else
			this.placeAllBlocks(par1ItemStack, par2EntityPlayer);

		return true;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		if (par3EntityPlayer.isSneaking())
		{
			int size = getSize(par1ItemStack);
			int newSize = size == 11 ? 3 : size + 2;
			setSize(par1ItemStack, newSize);
			ToolModeHUDHandler.setTooltip(newSize + " x " + newSize);
			par2World.playSoundAtEntity(par3EntityPlayer, "random.orb", 0.3F, 0.1F);
		}

		return par1ItemStack;
	}

	public void placeAllBlocks(ItemStack stack, EntityPlayer player)
	{
		ChunkCoordinates[] blocksToPlace = getBlocksToPlace(stack, player);
		if (!hasBlocks(stack, player, blocksToPlace))
			return;

		ItemStack stackToPlace = new ItemStack(getBlock(stack), 1, getBlockMeta(stack));
		for (ChunkCoordinates coords : blocksToPlace)
			this.placeBlockAndConsume(player, stackToPlace, coords);
		player.worldObj.playSoundAtEntity(player, "thaumcraft:wand", 1F, 1F);
	}

	private void placeBlockAndConsume(EntityPlayer player, ItemStack blockToPlace, ChunkCoordinates coords)
	{
		if (blockToPlace.getItem() == null)
			return;
		player.worldObj.setBlock(coords.posX, coords.posY, coords.posZ, Block.getBlockFromItem(blockToPlace.getItem()), blockToPlace.getItemDamage(), 1 | 2);

		if (player.capabilities.isCreativeMode)
			return;

		List<ItemStack> talismansToCheck = new ArrayList();
		for (int i = 0; i < player.inventory.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = player.inventory.getStackInSlot(i);
			if (stackInSlot != null && stackInSlot.getItem() == blockToPlace.getItem() && stackInSlot.getItemDamage() == blockToPlace.getItemDamage())
			{
				stackInSlot.stackSize--;
				if (stackInSlot.stackSize == 0)
					player.inventory.setInventorySlotContents(i, null);

				return;
			}

			if (stackInSlot != null && stackInSlot.getItem() == ThaumicTinkerer.registry.getFirstItemFromClass(ItemBlockTalisman.class))
				talismansToCheck.add(stackInSlot);
		}

		for (ItemStack talisman : talismansToCheck)
		{
			Block block = ItemBlockTalisman.getBlock(talisman);
			int meta = ItemBlockTalisman.getBlockMeta(talisman);

			if (Item.getItemFromBlock(block) == blockToPlace.getItem() && meta == blockToPlace.getItemDamage())
			{
				ItemBlockTalisman.remove(talisman, 1);
				return;
			}
		}
	}

	private void setBlock(ItemStack stack, Block block, int meta)
	{
		ItemNBTHelper.setString(stack, TAG_BLOCK_NAME, Block.blockRegistry.getNameForObject(block));
		ItemNBTHelper.setInt(stack, TAG_BLOCK_META, meta);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.icons[0] = IconHelper.forItem(par1IconRegister, this, 0);
		this.icons[1] = IconHelper.forItem(par1IconRegister, this, 1);
	}

	@Override
	public IIcon getIconFromDamageForRenderPass(int par1, int par2)
	{
		return this.icons[par2 & 1];
	}

	@Override
	public boolean requiresMultipleRenderPasses()
	{
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return TTCommonProxy.kamiRarity;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		Block block = getBlock(par1ItemStack);
		int size = getSize(par1ItemStack);

		par3List.add(size + " x " + size);
		if (block != null && block != Blocks.air)
			par3List.add(StatCollector.translateToLocal(new ItemStack(block, 1, getBlockMeta(par1ItemStack)).getUnlocalizedName() + ".name"));
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.PLACEMENT_MIRROR;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		if (!ConfigHandler.enableKami)
			return null;
		return (IRegisterableResearch) new KamiResearchItem(LibResearch.KEY_PLACEMENT_MIRROR, new AspectList().add(Aspect.CRAFT, 2).add(Aspect.CRYSTAL, 1).add(Aspect.ELDRITCH, 1).add(Aspect.MIND, 1), 17, 16, 5, new ItemStack(this)).setParents(LibResearch.KEY_BLOCK_TALISMAN).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_PLACEMENT_MIRROR));

	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_PLACEMENT_MIRROR, new ItemStack(this), 12, new AspectList().add(Aspect.CRAFT, 65).add(Aspect.CRYSTAL, 32).add(Aspect.MAGIC, 50).add(Aspect.MIND, 32), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemBlockTalisman.class)), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)), new ItemStack(Blocks.dropper), new ItemStack(Items.diamond), new ItemStack(Blocks.glass), new ItemStack(Items.blaze_powder), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)));

	}
}
