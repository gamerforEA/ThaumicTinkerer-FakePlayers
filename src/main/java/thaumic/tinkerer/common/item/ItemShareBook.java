package thaumic.tinkerer.common.item;

import com.gamerforea.ttinkerer.EventConfig;
import com.gamerforea.ttinkerer.properties.ExtendedPlayer;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchPage;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.research.ResearchManager;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.core.helper.ItemNBTHelper;
import thaumic.tinkerer.common.lib.LibItemNames;
import thaumic.tinkerer.common.lib.LibResearch;
import thaumic.tinkerer.common.registry.ItemBase;
import thaumic.tinkerer.common.registry.ThaumicTinkererCraftingBenchRecipe;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;
import thaumic.tinkerer.common.research.ResearchHelper;
import thaumic.tinkerer.common.research.TTResearchItem;

import java.util.ArrayList;
import java.util.List;

public class ItemShareBook extends ItemBase
{

	private static final String TAG_PLAYER = "player";
	private static final String NON_ASIGNED = "[none]";

	public ItemShareBook()
	{
		this.setMaxStackSize(1);
	}

	@Override
	public boolean shouldDisplayInTab()
	{
		return true;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		TTResearchItem research = (TTResearchItem) new TTResearchItem(LibResearch.KEY_SHARE_TOME, new AspectList(), 0, -1, 0, new ItemStack(this)).setStub().setAutoUnlock().setRound();
		if (ConfigHandler.enableSurvivalShareTome)
			research.setPages(new ResearchPage("0"), ResearchHelper.recipePage(LibResearch.KEY_SHARE_TOME));
		else
			research.setPages(new ResearchPage("0"));
		return research;
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		if (ConfigHandler.enableSurvivalShareTome)
			return new ThaumicTinkererCraftingBenchRecipe(LibResearch.KEY_SHARE_TOME, new ItemStack(this), " S ", "PTP", " P ", 'S', new ItemStack(ConfigItems.itemInkwell), 'T', new ItemStack(ConfigItems.itemThaumonomicon), 'P', new ItemStack(Items.paper));
		return null;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		String name = this.getPlayerName(stack);
		if (name.endsWith(NON_ASIGNED))
		{
			// TODO gamerforEA code start
			if (EventConfig.shareBookCreateLimit > 0)
			{
				ExtendedPlayer extendedPlayer = ExtendedPlayer.get(player);
				int counter = extendedPlayer.getShareBooksCounter();
				if (counter >= EventConfig.shareBookCreateLimit)
				{
					player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "Достигнут лимит на запись изучений!"));
					return stack;
				}
				extendedPlayer.setShareBooksCounter(counter + 1);
			}
			// TODO gamerforEA code end

			this.setPlayerName(stack, player.getGameProfile().getName());
			this.setPlayerResearch(stack, player.getGameProfile().getName());
			if (!world.isRemote)
				player.addChatMessage(new ChatComponentTranslation("ttmisc.shareTome.write"));
		}
		else
			sync:
					{
						List<String> researchesDone = ResearchManager.getResearchForPlayer(name);

						if (researchesDone == null)
							if (world.isRemote)
								researchesDone = this.getPlayerResearch(stack);
							else
							{
								player.addChatMessage(new ChatComponentTranslation("ttmisc.shareTome.sync"));
								break sync;

							}

						for (String key : researchesDone)
						{
							ThaumicTinkerer.tcProxy.getResearchManager().completeResearch(player, key);
						}

						if (!world.isRemote)
							player.addChatMessage(new ChatComponentTranslation("ttmisc.shareTome.sync"));

						// TODO gamerforEA code start
						if (EventConfig.shareBookSingleUse)
							stack.stackSize--;
						// TODO gamerforEA code end
					}

		return stack;
	}

	private List<String> getPlayerResearch(ItemStack par1ItemStack)
	{
		List<String> retVals = new ArrayList<>();
		NBTTagCompound cmp = ItemNBTHelper.getNBT(par1ItemStack);
		if (!cmp.hasKey("research"))
			return retVals;
		NBTTagList list = cmp.getTagList("research", Constants.NBT.TAG_STRING);
		for (int i = 0; i < list.tagCount(); i++)
		{
			retVals.add(list.getStringTagAt(i));
		}
		return retVals;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return EnumRarity.epic;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		String name = this.getPlayerName(par1ItemStack);
		par3List.add(name.equals(NON_ASIGNED) ? StatCollector.translateToLocal("ttmisc.shareTome.noAssign") : String.format(StatCollector.translateToLocal("ttmisc.shareTome.playerName"), name));
	}

	@Override
	public boolean getShareTag()
	{
		return true;
	}

	private String getPlayerName(ItemStack stack)
	{
		return ItemNBTHelper.getString(stack, TAG_PLAYER, NON_ASIGNED);
	}

	private void setPlayerName(ItemStack stack, String playerName)
	{
		ItemNBTHelper.setString(stack, TAG_PLAYER, playerName);
	}

	private void setPlayerResearch(ItemStack stack, String playername)
	{
		List<String> researchesDone = ResearchManager.getResearchForPlayer(playername);
		NBTTagCompound cmp = ItemNBTHelper.getNBT(stack);
		NBTTagList list = new NBTTagList();
		for (String tag : researchesDone)
		{
			list.appendTag(new NBTTagString(tag));
		}
		cmp.setTag("research", list);

	}

	@Override
	public String getItemName()
	{
		return LibItemNames.SHARE_BOOK;
	}

}