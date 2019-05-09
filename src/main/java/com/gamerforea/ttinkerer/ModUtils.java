package com.gamerforea.ttinkerer;

import com.gamerforea.eventhelper.nexus.ModNexus;
import com.gamerforea.eventhelper.nexus.ModNexusFactory;
import com.gamerforea.eventhelper.nexus.NexusUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.item.ItemStack;
import thaumcraft.common.blocks.BlockArcaneDoor;

@ModNexus(name = "ThaumicTinkerer", uuid = "87dc1bfd-b4ec-4b5f-a720-186891ee4515")
public final class ModUtils
{
	public static final ModNexusFactory NEXUS_FACTORY = NexusUtils.getFactory();

	public static void init()
	{
		EventConfig.init();
		FixEventHandler.init();
	}

	public static boolean isItemEqual(ItemStack stack1, ItemStack stack2)
	{
		return stack1 == stack2 || stack1 != null && stack2 != null && stack1.isItemEqual(stack2) && ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

	public static boolean isDoorBlock(Block block)
	{
		return block instanceof BlockDoor || block instanceof BlockArcaneDoor;
	}
}