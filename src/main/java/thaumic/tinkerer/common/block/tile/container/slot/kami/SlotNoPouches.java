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
 * File Created @ [Dec 29, 2013, 11:12:29 PM (GMT)]
 */
package thaumic.tinkerer.common.block.tile.container.slot.kami;

import com.gamerforea.ttinkerer.EventConfig;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import thaumcraft.common.items.wands.ItemFocusPouch;

public class SlotNoPouches extends Slot
{
	public SlotNoPouches(IInventory inventory, int slotIndex, int x, int y)
	{
		super(inventory, slotIndex, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		// TODO gamerforEA code start
		if (stack == null)
			return true;
		if (EventConfig.ichorPouchBlackList.contains(stack))
			return false;
		// TODO gamerforEA code end

		Item item = stack.getItem();
		return !(item instanceof ItemFocusPouch);
	}
}
