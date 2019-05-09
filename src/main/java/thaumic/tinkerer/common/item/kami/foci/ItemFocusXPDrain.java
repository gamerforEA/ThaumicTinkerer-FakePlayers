package thaumic.tinkerer.common.item.kami.foci;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.core.proxy.TTCommonProxy;
import thaumic.tinkerer.common.item.ItemXPTalisman;
import thaumic.tinkerer.common.item.kami.ItemKamiResource;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.KamiResearchItem;
import thaumic.tinkerer.common.research.ResearchHelper;

import java.awt.*;
import java.util.List;

public class ItemFocusXPDrain extends ItemModKamiFocus
{

	AspectList cost = new AspectList();
	private int lastGiven = 0;

	@Override
	public boolean isVisCostPerTick(ItemStack stack)
	{
		return true;
	}

	@Override
	public void onUsingFocusTick(ItemStack stack, EntityPlayer player, int count)
	{
		if (player.worldObj.isRemote)
			return;

		ItemWandCasting wand = (ItemWandCasting) stack.getItem();
		AspectList aspects = wand.getAllVis(stack);

		Aspect aspectToAdd = null;
		int takes = 0;

		while (aspectToAdd == null && takes < 7)
		{
			this.lastGiven = this.lastGiven == 5 ? 0 : this.lastGiven + 1;

			Aspect aspect = Aspect.getPrimalAspects().get(this.lastGiven);

			if (aspects.getAmount(aspect) < wand.getMaxVis(stack))
				aspectToAdd = aspect;

			++takes;
		}

		if (aspectToAdd != null)
		{
			int xpUse = this.getXpUse(stack);
			if (player.experienceTotal >= xpUse)
			{
				// TODO gamerforEA code replace, old code:
				// ExperienceHelper.drainPlayerXP(player, xpUse);
				int resultXp = player.experienceTotal - xpUse;
				player.experience = 0;
				player.experienceLevel = 0;
				player.experienceTotal = 0;
				if (player instanceof EntityPlayerMP)
					ReflectionHelper.setPrivateValue(EntityPlayerMP.class, (EntityPlayerMP) player, -1, "field_71144_ck", "lastExperience");
				if (resultXp > 0)
					player.addExperience(resultXp);
				// TODO gamerforEA code end

				int amount = wand.getVis(stack, aspectToAdd) + 500;

				// TODO gamerforEA code clear:
				// ThaumicTinkerer.log.info(amount);

				wand.storeVis(stack, aspectToAdd, Math.min(wand.getMaxVis(stack), amount));
			}
		}
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2)
	{
		return this.getFocusColor(par1ItemStack);
	}

	@Override
	public int getFocusColor(ItemStack stack)
	{
		EntityPlayer player = ThaumicTinkerer.proxy.getClientPlayer();
		return player == null ? 0xFFFFFF : Color.HSBtoRGB(player.ticksExisted * 2 % 360 / 360F, 1F, 1F);
	}

	int getXpUse(ItemStack stack)
	{
		return 15;
	}

	@Override
	protected void addVisCostTooltip(AspectList cost, ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		list.add(" " + EnumChatFormatting.GREEN + StatCollector.translateToLocal("ttmisc.experience") + EnumChatFormatting.WHITE + " x " + this.getXpUse(stack));
	}

	@Override
	public AspectList getVisCost(ItemStack stack)
	{
		return this.cost;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return TTCommonProxy.kamiRarity;
	}

	@Override
	public String getSortingHelper(ItemStack paramItemStack)
	{
		return "XPDRAIN";
	}

	@Override
	public String getItemName()
	{
		return LibItemNames.FOCUS_XP_DRAIN;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		if (!ConfigHandler.enableKami)
			return null;
		return (IRegisterableResearch) new KamiResearchItem(LibResearch.KEY_FOCUS_XP_DRAIN, new AspectList().add(Aspect.MIND, 2).add(Aspect.MAGIC, 1).add(Aspect.AURA, 1).add(Aspect.MAN, 1), 12, 3, 5, new ItemStack(this)).setParents(LibResearch.KEY_ICHORCLOTH_ROD).setPages(new ResearchPage("0"), ResearchHelper.infusionPage(LibResearch.KEY_FOCUS_XP_DRAIN));

	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return new ThaumicTinkererInfusionRecipe(LibResearch.KEY_FOCUS_XP_DRAIN, new ItemStack(this), 12, new AspectList().add(Aspect.MIND, 65).add(Aspect.TAINT, 16).add(Aspect.MAGIC, 50).add(Aspect.AURA, 32), new ItemStack(Items.ender_pearl), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)), new ItemStack(Items.experience_bottle), new ItemStack(Items.diamond), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemXPTalisman.class)), new ItemStack(Blocks.enchanting_table), new ItemStack(ThaumicTinkerer.registry.getFirstItemFromClass(ItemKamiResource.class)));

	}
}
