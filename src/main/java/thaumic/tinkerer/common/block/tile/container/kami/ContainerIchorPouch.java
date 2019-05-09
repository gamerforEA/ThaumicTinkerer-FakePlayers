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
 * File Created @ [Dec 29, 2013, 10:37:08 PM (GMT)]
 */
package thaumic.tinkerer.common.block.tile.container.kami;

import com.gamerforea.eventhelper.util.ItemInventoryValidator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.common.container.InventoryFocusPouch;
import thaumcraft.common.items.wands.ItemFocusPouch;
import thaumic.tinkerer.common.block.tile.container.ContainerPlayerInv;
import thaumic.tinkerer.common.block.tile.container.slot.kami.SlotNoPouches;
import thaumic.tinkerer.common.item.kami.ItemIchorPouch;

public class ContainerIchorPouch extends ContainerPlayerInv
{
	public IInventory inv = new InventoryIchorPouch(this);
	EntityPlayer player;
	ItemStack pouch;
	int blockSlot;

	// TODO gamerforEA code start
	public final ItemInventoryValidator validator;
	// TODO gamerforEA code end

	public ContainerIchorPouch(EntityPlayer player)
	{
		super(player.inventory);

		this.player = player;
		this.pouch = player.getCurrentEquippedItem();
		this.blockSlot = player.inventory.currentItem + 27 + 13 * 9;

		// TODO gamerforEA code start
		this.validator = new ItemInventoryValidator(this.pouch, ItemIchorPouch.class::isInstance);
		this.validator.setSlotIndex(player.inventory.currentItem, true);
		// TODO gamerforEA code end

		for (int y = 0; y < 9; y++)
		{
			for (int x = 0; x < 13; x++)
			{
				this.addSlotToContainer(new SlotNoPouches(this.inv, y * 13 + x, 12 + x * 18, 8 + y * 18));
			}
		}
		this.initPlayerInv();

		// TODO gamerforEA code start
		for (Slot slot : (Iterable<? extends Slot>) this.inventorySlots)
		{
			if (this.validator.tryGetSlotNumberFromPlayerSlot(slot))
				break;
		}
		// TODO gamerforEA code end

		if (!player.worldObj.isRemote)
			try
			{
				((InventoryIchorPouch) this.inv).stackList = ((ItemFocusPouch) this.pouch.getItem()).getInventory(this.pouch);
			}
			catch (Exception e)
			{
			}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		if (slot == this.blockSlot)
			return null;

		// TODO gamerforEA code start
		if (this.validator.getSlotNumber() == slot)
			return null;
		// TODO gamerforEA code end

		ItemStack stack = null;
		Slot slotObject = (Slot) this.inventorySlots.get(slot);
		if (slotObject != null && slotObject.getHasStack())
		{
			// TODO gamerforEA code start
			if (!this.canInteractWith(player))
				return null;
			// TODO gamerforEA code end

			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();
			if (slot < 13 * 9)
			{
				if (!this.inv.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 13 * 9, this.inventorySlots.size(), true))
					return null;
			}
			else if (!this.inv.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 0, 13 * 9, false))
				return null;
			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();
		}

		return stack;
	}

	@Override
	public ItemStack slotClick(int slot, int button, int buttonType, EntityPlayer player)
	{
		if (slot == this.blockSlot)
			return null;

		// TODO gamerforEA code start
		if (!this.validator.canSlotClick(slot, button, buttonType, player))
			return null;
		// TODO gamerforEA code end

		return super.slotClick(slot, button, buttonType, player);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.player.worldObj.isRemote)
		{
			((ItemFocusPouch) this.pouch.getItem()).setInventory(this.pouch, ((InventoryIchorPouch) this.inv).stackList);

			// TODO gamerforEA add condition [2]
			if (this.player == null || !this.canInteractWith(this.player))
				return;
			if (this.player.getHeldItem() != null && this.player.getHeldItem().isItemEqual(this.pouch))
				this.player.setCurrentItemOrArmor(0, this.pouch);

			this.player.inventory.markDirty();
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		// TODO gamerforEA code replace, old code:
		// return true;
		return this.validator.canInteractWith(player);
		// TODO gamerforEA code end
	}

	@Override
	public int getInvXStart()
	{
		return 48;
	}

	@Override
	public int getInvYStart()
	{
		return 177;
	}

	// TODO gamerforEA code start
	@Override
	public void onCraftMatrixChanged(IInventory inventory)
	{
		((ItemFocusPouch) this.pouch.getItem()).setInventory(this.pouch, ((InventoryIchorPouch) this.inv).stackList);
		if (this.player != null && this.canInteractWith(this.player) && this.player.getHeldItem() != null && this.player.getHeldItem().isItemEqual(this.pouch))
			this.player.setCurrentItemOrArmor(0, this.pouch);
		super.onCraftMatrixChanged(inventory);
	}
	// TODO gamerforEA code end

	private static class InventoryIchorPouch extends InventoryFocusPouch
	{
		public InventoryIchorPouch(Container par1Container)
		{
			super(par1Container);
			this.stackList = new ItemStack[13 * 9];
		}

		@Override
		public int getInventoryStackLimit()
		{
			return 64;
		}

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return itemstack != null && !(itemstack.getItem() instanceof ItemFocusPouch);
		}
	}
}
