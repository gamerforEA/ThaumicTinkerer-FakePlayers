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
 * File Created @ [9 Sep 2013, 01:20:26 (GMT)]
 */
package thaumic.tinkerer.common.item;

import com.gamerforea.ttinkerer.EventConfig;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import thaumic.tinkerer.api.INoRemoveEnchant;

public class SpellClothRecipe implements IRecipe
{
	Item item;

	public SpellClothRecipe(Item item)
	{
		this.item = item;
	}

	@Override
	public boolean matches(InventoryCrafting var1, World var2)
	{
		boolean foundCloth = false;
		boolean foundEnchanted = false;
		for (int i = 0; i < var1.getSizeInventory(); i++)
		{
			ItemStack stack = var1.getStackInSlot(i);
			if (stack != null)
			{
				Item item = stack.getItem();
				if (stack.isItemEnchanted() && !(item instanceof INoRemoveEnchant) && !foundEnchanted)
				{
					// TODO gamerforEA code start
					if (EventConfig.disableClothRecipeStack && stack.stackSize != 1)
						return false;
					if (!(item instanceof ItemSpellCloth) && item.hasContainerItem(stack))
						return false;
					// TODO gamerforEA code end

					foundEnchanted = true;
				}
				else if (item == this.item && !foundCloth)
					foundCloth = true;
				else
					return false; // Found an invalid item, breaking the recipe
			}
		}

		return foundCloth && foundEnchanted;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack stackToDisenchant = null;
		for (int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && stack.isItemEnchanted())
			{
				stackToDisenchant = stack.copy();

				// TODO gamerforEA code start
				stackToDisenchant.stackSize = 1;
				// TODO gamerforEA code end

				break;
			}
		}

		if (stackToDisenchant == null)
			return null;

		NBTTagCompound cmp = (NBTTagCompound) stackToDisenchant.getTagCompound().copy();
		cmp.removeTag("ench"); // Remove enchantments
		stackToDisenchant.setTagCompound(cmp);

		return stackToDisenchant;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}

	@Override
	public ItemStack getRecipeOutput()
	{
		return null;
	}
}