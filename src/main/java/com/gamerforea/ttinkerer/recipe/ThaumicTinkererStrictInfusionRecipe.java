package com.gamerforea.ttinkerer.recipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.common.config.ConfigResearch;
import thaumic.tinkerer.common.registry.ThaumicTinkererInfusionRecipe;

import java.util.ArrayList;

public final class ThaumicTinkererStrictInfusionRecipe extends ThaumicTinkererInfusionRecipe
{
	private final String name;
	private final String research;
	private final Object output;
	private final int instability;
	private final AspectList aspects;
	private final ItemStack input;
	private final ItemStack[] stuff;

	public ThaumicTinkererStrictInfusionRecipe(String name, String research, Object output, int instability, AspectList aspects, ItemStack input, ItemStack... stuff)
	{
		super(name, research, output, instability, aspects, input, stuff);
		this.name = name;
		this.research = research;
		this.output = output;
		this.instability = instability;
		this.aspects = aspects;
		this.input = input;
		this.stuff = stuff;
	}

	public ThaumicTinkererStrictInfusionRecipe(String name, Object output, int instability, AspectList aspects, ItemStack input, ItemStack... stuff)
	{
		this(name, name, output, instability, aspects, input, stuff);
	}

	@Override
	public void registerRecipe()
	{
		if (!(this.output instanceof ItemStack) && !(this.output instanceof Object[]))
			return;
		InfusionRecipe recipe = new StrictInfusionRecipe(this.research, this.output, this.instability, this.aspects, this.input, this.stuff);
		ThaumcraftApi.getCraftingRecipes().add(recipe);
		ConfigResearch.recipes.put(this.name, recipe);
	}

	private static final class StrictInfusionRecipe extends InfusionRecipe
	{
		public StrictInfusionRecipe(String research, Object output, int inst, AspectList aspects2, ItemStack input, ItemStack[] recipe)
		{
			super(research, output, inst, aspects2, input, recipe);
		}

		@Override
		public boolean matches(ArrayList<ItemStack> input, ItemStack central, World world, EntityPlayer player)
		{
			ItemStack recipeInput = this.getRecipeInput();
			if (recipeInput == null)
				return false;
			if (!this.research.isEmpty() && !ThaumcraftApiHelper.isResearchComplete(player.getCommandSenderName(), this.research))
				return false;
			if (!areStacksEquals(central, recipeInput))
				return false;

			ArrayList<ItemStack> inputList = new ArrayList<>(input);
			for (ItemStack comp : this.getComponents())
			{
				boolean itemRemoved = false;
				for (int i = 0; i < inputList.size(); ++i)
				{
					if (areStacksEquals(inputList.get(i), comp))
					{
						inputList.remove(i);
						itemRemoved = true;
						break;
					}
				}
				if (!itemRemoved)
					return false;
			}
			return inputList.isEmpty();
		}

		private static boolean areStacksEquals(ItemStack stack0, ItemStack stack1)
		{
			if (stack0 == stack1)
				return true;
			if (stack0 == null || stack1 == null)
				return false;
			return stack0.isItemEqual(stack1) && ItemStack.areItemStackTagsEqual(stack0, stack1);
		}
	}
}
