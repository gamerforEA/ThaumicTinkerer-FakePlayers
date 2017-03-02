package thaumic.tinkerer.common.block.mobilizer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigBlocks;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.block.BlockMod;
import thaumic.tinkerer.common.block.tile.TileEntityMobilizer;
import thaumic.tinkerer.common.lib.LibBlockNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.ResearchHelper;
import thaumic.tinkerer.common.research.TTResearchItem;

public class BlockMobilizer extends BlockMod
{
	@SideOnly(Side.CLIENT)
	private IIcon iconTop;
	@SideOnly(Side.CLIENT)
	private IIcon iconBottom;
	@SideOnly(Side.CLIENT)
	private IIcon iconSide;

	public BlockMobilizer()
	{
		super(Material.iron);
	}

	// TODO gamerforEA code start
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase entity, ItemStack stack)
	{
		super.onBlockPlacedBy(world, x, y, z, entity, stack);

		TileEntity tile;
		if (entity instanceof EntityPlayer && (tile = world.getTileEntity(x, y, z)) instanceof TileEntityMobilizer)
			((TileEntityMobilizer) tile).fake.setProfile(((EntityPlayer) entity).getGameProfile());
	}
	// TODO gamerforEA code end

	@Override
	public void onBlockPreDestroy(World par1World, int par2, int par3, int par4, int par5)
	{
		TileEntity tile = par1World.getTileEntity(par2, par3, par4);
		if (tile != null && tile instanceof TileEntityMobilizer)
			((TileEntityMobilizer) tile).dead = true;
		super.onBlockPreDestroy(par1World, par2, par3, par4, par5);
	}

	@Override
	public boolean hasTileEntity(int metadata)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileEntityMobilizer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.iconBottom = IconHelper.forBlock(iconRegister, this, 0);
		this.iconTop = IconHelper.forBlock(iconRegister, this, 1);
		this.iconSide = IconHelper.forBlock(iconRegister, this, 2);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int meta)
	{
		return par1 == ForgeDirection.UP.ordinal() ? this.iconTop : par1 == ForgeDirection.DOWN.ordinal() ? this.iconBottom : this.iconSide;
	}

	@Override
	public String getBlockName()
	{
		return LibBlockNames.MOBILIZER;
	}

	@Override
	public Class<? extends ItemBlock> getItemBlock()
	{
		return null;
	}

	@Override
	public Class<? extends TileEntity> getTileEntity()
	{
		return TileEntityMobilizer.class;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		return (IRegisterableResearch) new TTResearchItem(LibResearch.KEY_MOBILIZER, new AspectList().add(Aspect.MOTION, 2).add(Aspect.ORDER, 2), -7, 5, 3, new ItemStack(this)).setParents(LibResearch.KEY_MAGNETS).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_MOBILIZER), ResearchHelper.arcaneRecipePage(LibResearch.KEY_RELAY)).setSecondary();
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_MOBILIZER, new ItemStack(this), 4, new AspectList().add(Aspect.MOTION, 15).add(Aspect.ORDER, 20).add(Aspect.MAGIC, 15), new ItemStack(ConfigBlocks.blockLifter), new ItemStack(Items.iron_ingot), new ItemStack(Items.feather), new ItemStack(Items.iron_ingot), new ItemStack(ConfigBlocks.blockCosmeticSolid, 1, 1));

	}
}
