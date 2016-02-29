/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the ThaumicTinkerer Mod.
 *
 * ThaumicTinkerer is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 *
 * ThaumicTinkerer is a Derivative Work on Thaumcraft 4.
 * Thaumcraft 4 (c) Azanor 2012
 * (http://www.minecraftforum.net/topic/1585216-)
 *
 * File Created @ [9 Sep 2013, 15:54:36 (GMT)]
 */
package thaumic.tinkerer.common.block.tile.tablet;

import com.gamerforea.ttinkerer.ModUtils;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import thaumcraft.common.lib.FakeThaumcraftPlayer;

public class TabletFakePlayer extends FakeThaumcraftPlayer
{
	TileAnimationTablet tablet;

	public TabletFakePlayer(TileAnimationTablet tablet)
	{ //,String name) {
		// TODO gamerforEA code replace, old code: super(tablet.getWorldObj(), new GameProfile(UUID.fromString("a8f026a0-135b-11e4-9191-0800200c9a66"), "[ThaumcraftTablet]"));
		super(tablet.getWorldObj(), tablet.fake.profile == null ? ModUtils.profile : tablet.fake.profile);
		// TODO gamerforEA code end

		//super(tablet.getWorldObj(),"[ThaumcraftTablet]");
		this.tablet = tablet;
	}

	@Override
	public void setDead()
	{
		this.inventory.clearInventory(null, -1);
		super.setDead();
	}

	@Override
	public void openGui(Object mod, int modGuiId, World world, int x, int y, int z)
	{
		// NO-OP
	}

	@Override
	public void onUpdate()
	{
		this.capabilities.isCreativeMode = false;

		this.posX = this.tablet.xCoord + 0.5;
		this.posY = this.tablet.yCoord + 1.6;
		this.posZ = this.tablet.zCoord + 0.5;

		if (this.riddenByEntity != null)
			this.riddenByEntity.ridingEntity = null;
		if (this.ridingEntity != null)
			this.ridingEntity.riddenByEntity = null;
		this.riddenByEntity = null;
		this.ridingEntity = null;

		this.motionX = this.motionY = this.motionZ = 0;
		this.setHealth(20);
		this.isDead = false;

		int meta = this.tablet.getBlockMetadata() & 7;
		int rotation = meta == 2 ? 180 : meta == 3 ? 0 : meta == 4 ? 90 : -90;
		this.rotationYaw = this.rotationYawHead = rotation;
		this.rotationPitch = -15;

		for (int i = 0; i < this.inventory.getSizeInventory(); i++)
			if (i != this.inventory.currentItem)
			{
				ItemStack stack = this.inventory.getStackInSlot(i);
				if (stack != null)
				{
					this.entityDropItem(stack, 1.0f);
					this.inventory.setInventorySlotContents(i, null);
				}
			}
	}

	@Override
	public void addChatMessage(IChatComponent var1)
	{
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates()
	{
		return new ChunkCoordinates(this.tablet.xCoord, this.tablet.yCoord, this.tablet.zCoord);
	}
}