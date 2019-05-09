package thaumic.tinkerer.common.item;

import com.gamerforea.ttinkerer.EventConfig;
import com.gamerforea.ttinkerer.recipe.ThaumicTinkererStrictInfusionRecipe;
import net.minecraft.block.BlockFarmland;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.config.ConfigItems;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.BlockInfusedFarmland;
import thaumic.tinkerer.common.block.BlockInfusedGrain;
import thaumic.tinkerer.common.block.tile.TileInfusedGrain;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ITTinkererItem;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipeMulti;
import thaumic.tinkerer.common.research.IRegisterableResearch;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pixlepix on 4/14/14.
 */
public class ItemInfusedSeeds extends ItemSeeds implements ITTinkererItem
{

	private static final String NBT_MAIN_ASPECT = "mainAspect";
	private static final String NBT_ASPEPCT_TENDENCIES = "aspectTendencies";
	private IIcon[] icons;

	public ItemInfusedSeeds()
	{
		super(ThaumicTinkerer.registry.getFirstBlockFromClass(BlockInfusedGrain.class), Blocks.farmland);
	}

	public static Aspect getAspect(ItemStack stack)
	{
		AspectList aspectList = new AspectList();
		if (stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		aspectList.readFromNBT(stack.getTagCompound().getCompoundTag(NBT_MAIN_ASPECT));

		return aspectList.size() == 0 ? null : aspectList.getAspects()[0];
	}

	public static void setAspect(ItemStack stack, Aspect aspect)
	{
		if (stack.stackTagCompound == null)
			stack.setTagCompound(new NBTTagCompound());
		AspectList aspectList = new AspectList().add(aspect, 1);
		NBTTagCompound nbt = new NBTTagCompound();
		aspectList.writeToNBT(nbt);
		stack.stackTagCompound.setTag(NBT_MAIN_ASPECT, nbt);
	}

	public static AspectList getAspectTendencies(ItemStack stack)
	{
		AspectList aspectList = new AspectList();
		if (stack.getTagCompound() == null)
			stack.setTagCompound(new NBTTagCompound());
		aspectList.readFromNBT(stack.getTagCompound().getCompoundTag(NBT_ASPEPCT_TENDENCIES));

		return aspectList;
	}

	public static void setAspectTendencies(ItemStack stack, AspectList aspectList)
	{
		if (stack.stackTagCompound == null)
			stack.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = new NBTTagCompound();
		aspectList.writeToNBT(nbt);
		stack.stackTagCompound.setTag(NBT_ASPEPCT_TENDENCIES, nbt);
	}

	public static ItemStack getStackFromAspect(Aspect a)
	{
		ItemStack stack = new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemInfusedSeeds.class));
		setAspect(stack, a);
		return stack;
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		super.addInformation(par1ItemStack, par2EntityPlayer, par3List, par4);
		par3List.add(getAspect(par1ItemStack).getName());
		AspectList aspectList = getAspectTendencies(par1ItemStack);
		if (aspectList != null && aspectList.getAspects()[0] != null)
			for (Aspect a : aspectList.getAspects())
			{
				par3List.add(a.getName() + ": " + aspectList.getAmount(a));
			}
	}

	@Override
	public boolean getHasSubtypes()
	{
		return true;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List l)
	{
		for (Aspect primal : Aspect.getPrimalAspects())
		{
			ItemStack itemStack = new ItemStack(item, 1);
			setAspect(itemStack, primal);
			setAspectTendencies(itemStack, new AspectList());
			l.add(itemStack);
		}
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.icons = new IIcon[7];
		this.icons[0] = IconHelper.forName(par1IconRegister, "seed_aer");
		this.icons[1] = IconHelper.forName(par1IconRegister, "seed_ignis");
		this.icons[2] = IconHelper.forName(par1IconRegister, "seed_aqua");
		this.icons[3] = IconHelper.forName(par1IconRegister, "seed_terra");

		this.icons[4] = IconHelper.forName(par1IconRegister, "seed_ordo");
		this.icons[5] = IconHelper.forName(par1IconRegister, "seed_perditio");

		this.icons[6] = IconHelper.forName(par1IconRegister, "seed_generic");
	}

	@Override
	public IIcon getIconIndex(ItemStack stack)
	{
		return getAspect(stack) == null ? this.icons[0] : this.icons[BlockInfusedGrain.getNumberFromAspectForTexture(getAspect(stack))];
	}

	@Override
	public ArrayList<Object> getSpecialParameters()
	{
		return null;
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.INFUSED_SEEDS;
	}

	@Override
	public boolean shouldRegister()
	{
		return true;
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
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return EnumRarity.rare;
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		// TODO gamerforEA code start
		if (EventConfig.fixInfusedSeedsCraft)
		{
			return new ThaumicTinkererRecipeMulti(new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 0, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.AIR), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0)),

												  new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 1, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.FIRE), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1)),

												  new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 2, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.EARTH), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3)),

												  new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 3, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.WATER), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2)),

												  new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 4, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.ORDER), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4)),

												  new ThaumicTinkererStrictInfusionRecipe(LibResearch.KEY_POTIONS + 5, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.ENTROPY), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5)));
		}
		// TODO gamerforEA code end

		return new ThaumicTinkererRecipeMulti(new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 0, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.AIR), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0), new ItemStack(ConfigItems.itemShard, 1, 0)),

											  new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 1, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.FIRE), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1), new ItemStack(ConfigItems.itemShard, 1, 1)),

											  new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 2, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.EARTH), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3), new ItemStack(ConfigItems.itemShard, 1, 3)),

											  new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 3, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.WATER), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2), new ItemStack(ConfigItems.itemShard, 1, 2)),

											  new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 4, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.ORDER), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4), new ItemStack(ConfigItems.itemShard, 1, 4)),

											  new ThaumicTinkererInfusionRecipe(LibResearch.KEY_POTIONS + 5, LibResearch.KEY_POTIONS, getStackFromAspect(Aspect.ENTROPY), 5, new AspectList().add(Aspect.CROP, 32).add(Aspect.HARVEST, 32), new ItemStack(Items.wheat_seeds), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5), new ItemStack(ConfigItems.itemShard, 1, 5)));
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World world, int x, int y, int z, int par7, float par8, float par9, float par10)
	{
		if (par7 != 1)
			return false;
		if (world.getBlock(x, y, z) instanceof BlockFarmland && par2EntityPlayer.canPlayerEdit(x, y, z, par7, par1ItemStack) && par2EntityPlayer.canPlayerEdit(x, y + 1, z, par7, par1ItemStack))
		{

			world.setBlock(x, y, z, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockInfusedFarmland.class));
			world.setBlock(x, y + 1, z, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockInfusedGrain.class));
			BlockInfusedGrain.setAspect(world, x, y + 1, z, getAspect(par1ItemStack));
			((TileInfusedGrain) world.getTileEntity(x, y + 1, z)).primalTendencies = getAspectTendencies(par1ItemStack);
			par1ItemStack.stackSize--;
			return true;
		}
		return false;
	}

}