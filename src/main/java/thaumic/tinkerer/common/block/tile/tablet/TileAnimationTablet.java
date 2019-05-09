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
 * File Created @ [9 Sep 2013, 15:51:34 (GMT)]
 */
package thaumic.tinkerer.common.block.tile.tablet;

import appeng.api.movable.IMovableTile;
import com.gamerforea.eventhelper.util.EventUtils;
import com.gamerforea.ttinkerer.EventConfig;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.eventhandler.Event;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.BlockAnimationTablet;
import thaumic.tinkerer.common.lib.LibBlockNames;

import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({ @Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent",
											  modid = "OpenComputers"), @Optional.Interface(iface = "dan200.computercraft.api.peripheral.IPeripheral",
																							modid = "ComputerCraft") })
public class TileAnimationTablet extends TileEntity implements IInventory, IMovableTile, IPeripheral, SimpleComponent
{
	private static final String TAG_LEFT_CLICK = "leftClick";
	private static final String TAG_REDSTONE = "redstone";
	private static final String TAG_PROGRESS = "progress";
	private static final String TAG_MOD = "mod";
	private static final String TAG_OWNER = "owner";

	private static final int[][] LOC_INCREASES = { { 0, -1 }, { 0, +1 }, { -1, 0 }, { +1, 0 } };

	private static final ForgeDirection[] SIDES = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST };

	private static final int SWING_SPEED = 3;
	private static final int MAX_DEGREE = 45;
	public double ticksExisted = 0;
	public boolean leftClick = true;
	public boolean redstone = false;
	public int swingProgress = 0;
	List<Entity> detectedEntities = new ArrayList();
	ItemStack[] inventorySlots = new ItemStack[1];
	TabletFakePlayer player;
	private int swingMod = 0;
	private boolean isBreaking = false;
	private int initialDamage = 0;
	private int curblockDamage = 0;
	private int durabilityRemainingOnBlock;

	@Override
	public void updateEntity()
	{
		//player = new TabletFakePlayer(this);//,Owner);
		this.player.onUpdate();
		this.player.inventory.clearInventory(null, -1);
		this.ticksExisted++;

		ItemStack stack = this.getStackInSlot(0);

		if (stack != null)
		{
			if (this.swingProgress >= MAX_DEGREE)
				this.swingHit();

			this.swingMod = this.swingProgress <= 0 ? 0 : this.swingProgress >= MAX_DEGREE ? -SWING_SPEED : this.swingMod;
			this.swingProgress += this.swingMod;
			if (this.swingProgress < 0)
				this.swingProgress = 0;
		}
		else
		{
			this.swingMod = 0;
			this.swingProgress = 0;

			if (this.isBreaking)
				this.stopBreaking();
		}

		boolean detect = this.detect();
		if (!detect)
			this.stopBreaking();

		if (detect && this.isBreaking)
			this.continueBreaking();

		if ((!this.redstone || this.isBreaking) && detect && this.swingProgress == 0)
		{
			this.initiateSwing();
			this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockAnimationTablet.class), 0, 0);
		}
	}

	public void initiateSwing()
	{
		this.swingMod = SWING_SPEED;
		this.swingProgress = 1;
	}

	public void swingHit()
	{
		ChunkCoordinates coords = this.getTargetLoc();
		ItemStack stack = this.getStackInSlot(0);
		Item item = stack.getItem();

		// TODO gamerforEA code start
		if (EventConfig.animationTabletBlackList.contains(item, stack.getItemDamage()))
			return;
		// TODO gamerforEA code end

		Block block = this.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);

		this.player.setCurrentItemOrArmor(0, stack);
		//EntityPlayer realPlayer=MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(Owner);
		//NBTTagCompound data=realPlayer.getEntityData().getCompoundTag("PlayerPersisted");
		//player.getEntityData().setCompoundTag("PlayerPersisted",data);
		//NBTTagCompound cmp=player.getEntityData().getCompoundTag("PlayerPersisted");
		//System.out.println(cmp.getCompoundTag("TCResearch").getTagList("TCResearchList").tagCount());

		boolean done = false;

		if (this.leftClick)
		{
			Entity entity = this.detectedEntities.isEmpty() ? null : this.detectedEntities.get(this.worldObj.rand.nextInt(this.detectedEntities.size()));
			if (entity != null)
			{
				// TODO gamerforEA code start
				if (!EventUtils.cantDamage(this.player, entity))
				{
					// TODO gamerforEA code end
					this.player.getAttributeMap().applyAttributeModifiers(stack.getAttributeModifiers()); // Set attack strenght
					this.player.attackTargetEntityWithCurrentItem(entity);
					done = true;
				}
			}
			else if (!this.isBreaking)
				if (block != Blocks.air && !block.isAir(this.worldObj, coords.posX, coords.posY, coords.posZ) && block.getBlockHardness(this.worldObj, coords.posX, coords.posY, coords.posZ) >= 0)
				{
					this.isBreaking = true;
					this.startBreaking(block, this.worldObj.getBlockMetadata(coords.posX, coords.posY, coords.posZ));
					done = true;
				}
		}
		else
		{
			int side = SIDES[(this.getBlockMetadata() & 7) - 2].getOpposite().ordinal();

			if (!(block != Blocks.air && !block.isAir(this.worldObj, coords.posX, coords.posY, coords.posZ)))
			{
				coords.posY -= 1;
				side = ForgeDirection.UP.ordinal();
				block = this.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
			}

			// TODO gamerforEA code start
			if (!EventUtils.cantBreak(this.player, coords.posX, coords.posY, coords.posZ))
				// TODO gamerforEA code end
				try
				{
					ForgeEventFactory.onPlayerInteract(this.player, Action.RIGHT_CLICK_AIR, coords.posX, coords.posY, coords.posZ, side, this.worldObj);
					Entity entity = this.detectedEntities.isEmpty() ? null : this.detectedEntities.get(this.worldObj.rand.nextInt(this.detectedEntities.size()));
					done = entity instanceof EntityLiving && (item.itemInteractionForEntity(stack, this.player, (EntityLivingBase) entity) || !(entity instanceof EntityAnimal) || ((EntityAnimal) entity).interact(this.player));

					if (!done)
					{
						// TODO gamerforEA code start
						done =
								// TODO gamerforEA code end
								item.onItemUseFirst(stack, this.player, this.worldObj, coords.posX, coords.posY, coords.posZ, side, 0F, 0F, 0F);
					}

					if (!done)
						done = block != null && block.onBlockActivated(this.worldObj, coords.posX, coords.posY, coords.posZ, this.player, side, 0F, 0F, 0F);
					if (!done)
						done = item.onItemUse(stack, this.player, this.worldObj, coords.posX, coords.posY, coords.posZ, side, 0F, 0F, 0F);
					if (!done)
					{
						item.onItemRightClick(stack, this.worldObj, this.player);
						done = true;
					}

				}
				catch (Throwable e)
				{
					e.printStackTrace();
					@SuppressWarnings("unchecked")
					List<? extends EntityPlayer> list = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.xCoord - 8, this.yCoord - 8, this.zCoord - 8, this.xCoord + 8, this.yCoord + 8, this.zCoord + 8));
					for (EntityPlayer player : list)
					{
						player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "Something went wrong with a Tool Dynamism Tablet! Check your FML log."));
						player.addChatComponentMessage(new ChatComponentText(EnumChatFormatting.RED + "" + EnumChatFormatting.ITALIC + e.getMessage()));
					}
				}
		}

		if (done)
		{
			stack = this.player.getCurrentEquippedItem();
			if (stack == null || stack.stackSize <= 0)
				this.setInventorySlotContents(0, null);
			else if (stack != this.getStackInSlot(0))
				this.setInventorySlotContents(0, stack);
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		}
		this.markDirty();
	}

	// Copied from ItemInWorldManager, seems to do the trick.
	private void stopBreaking()
	{
		this.isBreaking = false;
		ChunkCoordinates coords = this.getTargetLoc();
		this.worldObj.destroyBlockInWorldPartially(this.player.getEntityId(), coords.posX, coords.posY, coords.posZ, -1);
	}

	// Copied from ItemInWorldManager, seems to do the trick.
	private void startBreaking(Block block, int meta)
	{
		int side = SIDES[(this.getBlockMetadata() & 7) - 2].getOpposite().ordinal();
		ChunkCoordinates coords = this.getTargetLoc();

		// TODO gamerforEA code start
		if (EventUtils.cantBreak(this.player, coords.posX, coords.posY, coords.posZ))
		{
			this.stopBreaking();
			return;
		}
		// TODO gamerforEA code end

		PlayerInteractEvent event = ForgeEventFactory.onPlayerInteract(this.player, Action.LEFT_CLICK_BLOCK, coords.posX, coords.posY, coords.posZ, side, this.worldObj);
		if (event.isCanceled())
		{
			this.stopBreaking();
			return;
		}

		this.initialDamage = this.curblockDamage;
		float var5 = 1F;

		if (block != null)
		{
			if (event.useBlock != Event.Result.DENY)
				block.onBlockClicked(this.worldObj, coords.posX, coords.posY, coords.posZ, this.player);
			var5 = block.getPlayerRelativeBlockHardness(this.player, this.worldObj, coords.posX, coords.posY, coords.posZ);
		}

		if (event.useItem == Event.Result.DENY)
		{
			this.stopBreaking();
			return;
		}

		if (var5 >= 1F)
		{
			this.tryHarvestBlock(coords.posX, coords.posY, coords.posZ);
			this.stopBreaking();
		}
		else
		{
			int var7 = (int) (var5 * 10);
			this.worldObj.destroyBlockInWorldPartially(this.player.getEntityId(), coords.posX, coords.posY, coords.posZ, var7);
			this.durabilityRemainingOnBlock = var7;
		}
	}

	// Copied from ItemInWorldManager, seems to do the trick.
	private void continueBreaking()
	{
		++this.curblockDamage;
		int var1;
		float var4;
		int var5;
		ChunkCoordinates coords = this.getTargetLoc();

		var1 = this.curblockDamage - this.initialDamage;
		Block block = this.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);

		if (block == Blocks.air)
			this.stopBreaking();
		else
		{
			// TODO gamerforEA code start
			if (EventUtils.cantBreak(this.player, coords.posX, coords.posY, coords.posZ))
			{
				this.stopBreaking();
				return;
			}
			// TODO gamerforEA code end

			var4 = block.getPlayerRelativeBlockHardness(this.player, this.worldObj, coords.posX, coords.posY, coords.posZ) * var1;
			var5 = (int) (var4 * 10);

			if (var5 != this.durabilityRemainingOnBlock)
			{
				this.worldObj.destroyBlockInWorldPartially(this.player.getEntityId(), coords.posX, coords.posY, coords.posZ, var5);
				this.durabilityRemainingOnBlock = var5;
			}

			if (var4 >= 1F)
			{
				this.tryHarvestBlock(coords.posX, coords.posY, coords.posZ);
				this.stopBreaking();
			}
		}
	}

	// Copied from ItemInWorldManager, seems to do the trick.
	public boolean tryHarvestBlock(int par1, int par2, int par3)
	{
		ItemStack stack = this.getStackInSlot(0);
		if (stack != null && stack.getItem().onBlockStartBreak(stack, par1, par2, par3, this.player))
			return false;

		Block block = this.worldObj.getBlock(par1, par2, par3);
		int var5 = this.worldObj.getBlockMetadata(par1, par2, par3);
		//worldObj.playAuxSFXAtEntity(player, 2001, par1, par2, par3, var4 + (var5 << 12));
		boolean var6;

		boolean var8 = false;
		if (block != null)
			var8 = block.canHarvestBlock(this.player, var5);

		this.worldObj.loadedEntityList.size();
		if (stack != null)
			stack.getItem().onBlockDestroyed(stack, this.worldObj, block, par1, par2, par3, this.player);

		var6 = this.removeBlock(par1, par2, par3);
		if (var6 && var8)
			block.harvestBlock(this.worldObj, this.player, par1, par2, par3, var5);

		return var6;
	}

	// Copied from ItemInWorldManager, seems to do the trick.
	private boolean removeBlock(int par1, int par2, int par3)
	{
		Block var4 = this.worldObj.getBlock(par1, par2, par3);
		int var5 = this.worldObj.getBlockMetadata(par1, par2, par3);

		if (var4 != null)
			var4.onBlockHarvested(this.worldObj, par1, par2, par3, var5, this.player);

		boolean var6 = var4 != null && var4.removedByPlayer(this.worldObj, this.player, par1, par2, par3);

		if (var4 != null && var6)
			var4.onBlockDestroyedByPlayer(this.worldObj, par1, par2, par3, var5);

		return var6;
	}

	public boolean detect()
	{
		ChunkCoordinates coords = this.getTargetLoc();
		this.findEntities(coords);
		return !this.worldObj.isAirBlock(coords.posX, coords.posY, coords.posZ) || !this.detectedEntities.isEmpty();
	}

	public void findEntities(ChunkCoordinates coords)
	{
		AxisAlignedBB boundingBox = AxisAlignedBB.getBoundingBox(coords.posX, coords.posY, coords.posZ, coords.posX + 1, coords.posY + 1, coords.posZ + 1);
		this.detectedEntities = this.worldObj.getEntitiesWithinAABB(Entity.class, boundingBox);
	}

	@Override
	public void validate()
	{
		super.validate();
		this.player = new TabletFakePlayer(this);
	}

	public ChunkCoordinates getTargetLoc()
	{
		ChunkCoordinates coords = new ChunkCoordinates(this.xCoord, this.yCoord, this.zCoord);

		int meta = this.getBlockMetadata();
		if (meta == 0)
		{
			ThaumicTinkerer.log.error("Metadata of a Tool Dynamism tablet is in an invalid state. This is a critical error.");
			return coords;
		}
		int[] increase = LOC_INCREASES[(meta & 7) - 2];
		coords.posX += increase[0];
		coords.posZ += increase[1];

		return coords;
	}

	public boolean getIsBreaking()
	{
		return this.isBreaking;
	}

	@Override
	public boolean receiveClientEvent(int par1, int par2)
	{
		if (par1 == 0)
		{
			this.initiateSwing();
			return true;
		}

		return this.tileEntityInvalid;
	}

	@Override
	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readFromNBT(par1NBTTagCompound);

		this.swingProgress = par1NBTTagCompound.getInteger(TAG_PROGRESS);

		//if(par1NBTTagCompound.hasKey(TAG_OWNER))
		//    Owner=par1NBTTagCompound.getString(TAG_OWNER);
		//else
		//    Owner="";
		this.readCustomNBT(par1NBTTagCompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeToNBT(par1NBTTagCompound);

		par1NBTTagCompound.setInteger(TAG_PROGRESS, this.swingProgress);
		par1NBTTagCompound.setInteger(TAG_MOD, this.swingMod);
		//par1NBTTagCompound.setString(TAG_OWNER,Owner);
		this.writeCustomNBT(par1NBTTagCompound);
	}

	public void readCustomNBT(NBTTagCompound par1NBTTagCompound)
	{
		this.leftClick = par1NBTTagCompound.getBoolean(TAG_LEFT_CLICK);
		this.redstone = par1NBTTagCompound.getBoolean(TAG_REDSTONE);
		//if(par1NBTTagCompound.hasKey("isBreaking"))
		//   isBreaking = par1NBTTagCompound.getBoolean("isBreaking");
		//if(par1NBTTagCompound.hasKey("initialDamage"))
		//    initialDamage = par1NBTTagCompound.getInteger("initialDamage");
		//if(par1NBTTagCompound.hasKey("curblockDamage"))
		//curblockDamage = par1NBTTagCompound.getInteger("curblockDamage");
		//if(par1NBTTagCompound.hasKey("durabilityRemainingOnBlock"))
		//    durabilityRemainingOnBlock=par1NBTTagCompound.getInteger("durabilityRemainingOnBlock");
		NBTTagList var2 = par1NBTTagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		this.inventorySlots = new ItemStack[this.getSizeInventory()];
		for (int var3 = 0; var3 < var2.tagCount(); ++var3)
		{
			NBTTagCompound var4 = var2.getCompoundTagAt(var3);
			byte var5 = var4.getByte("Slot");
			if (var5 >= 0 && var5 < this.inventorySlots.length)
				this.inventorySlots[var5] = ItemStack.loadItemStackFromNBT(var4);
		}
	}

	public void writeCustomNBT(NBTTagCompound par1NBTTagCompound)
	{
		par1NBTTagCompound.setBoolean(TAG_LEFT_CLICK, this.leftClick);
		par1NBTTagCompound.setBoolean(TAG_REDSTONE, this.redstone);
		//par1NBTTagCompound.setBoolean("isBreaking",isBreaking);
		//par1NBTTagCompound.setInteger("initialDamage", initialDamage);
		//par1NBTTagCompound.setInteger("curblockDamage",curblockDamage);
		//par1NBTTagCompound.setInteger("durabilityRemainingOnBlock",durabilityRemainingOnBlock);
		NBTTagList var2 = new NBTTagList();
		for (int var3 = 0; var3 < this.inventorySlots.length; ++var3)
		{
			if (this.inventorySlots[var3] != null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
				this.inventorySlots[var3].writeToNBT(var4);
				var2.appendTag(var4);
			}
		}
		par1NBTTagCompound.setTag("Items", var2);
	}

	@Override
	public int getSizeInventory()
	{
		return this.inventorySlots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return this.inventorySlots[i];
	}

	@Override
	public ItemStack decrStackSize(int par1, int par2)
	{
		if (this.inventorySlots[par1] != null)
		{
			ItemStack stackAt;

			if (this.inventorySlots[par1].stackSize <= par2)
			{
				stackAt = this.inventorySlots[par1];
				this.inventorySlots[par1] = null;

				if (!this.worldObj.isRemote)
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

				return stackAt;
			}
			else
			{
				stackAt = this.inventorySlots[par1].splitStack(par2);

				if (this.inventorySlots[par1].stackSize == 0)
					this.inventorySlots[par1] = null;

				if (!this.worldObj.isRemote)
					this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);

				return stackAt;
			}
		}

		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return this.getStackInSlot(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.inventorySlots[i] = itemstack;

		if (!this.worldObj.isRemote)
			this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public String getInventoryName()
	{
		return LibBlockNames.ANIMATION_TABLET;
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) == this && entityplayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64;
	}

	@Override
	public void openInventory()
	{

	}

	@Override
	public void closeInventory()
	{

	}

	@Override
	public S35PacketUpdateTileEntity getDescriptionPacket()
	{
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		this.writeCustomNBT(nbttagcompound);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, -999, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet)
	{
		super.onDataPacket(manager, packet);
		this.readCustomNBT(packet.func_148857_g());
	}

	@Override
	public String getType()
	{
		return "tt_animationTablet";
	}

	@Override
	public String[] getMethodNames()
	{
		return new String[] { "getRedstone", "setRedstone", "getLeftClick", "setLeftClick", "getRotation", "setRotation", "hasItem", "trigger" };
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
			throws LuaException
	{
		switch (method)
		{
			case 0:
				return new Object[] { this.redstone };
			case 1:
				return this.setRedstoneImplementation((Boolean) arguments[0]);
			case 2:
				return new Object[] { this.leftClick };
			case 3:
				return this.setLeftClickImplementation((Boolean) arguments[0]);
			case 4:
				return new Object[] { this.getBlockMetadata() - 2 };
			case 5:
				return this.setRotationImplementation((Double) arguments[0]);
			case 6:
				return new Object[] { this.getStackInSlot(0) != null };
			case 7:
				return this.triggerImplementation();
		}
		return null;
	}

	private Object[] triggerImplementation()
	{
		if (this.swingProgress != 0)
			return new Object[] { false };

		this.findEntities(this.getTargetLoc());
		this.initiateSwing();
		this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockAnimationTablet.class), 0, 0);

		return new Object[] { true };
	}

	@Optional.Method(modid = "ComputerCraft")
	private Object[] setRotationImplementation(Double argument) throws LuaException
	{
		int rotation = (int) argument.doubleValue();

		if (rotation > 3)
			throw new LuaException("Invalid value: " + rotation + ".");

		this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, rotation + 2, 1 | 2);
		return null;
	}

	private Object[] setLeftClickImplementation(Boolean argument)
	{
		this.leftClick = argument;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		return null;
	}

	private Object[] setRedstoneImplementation(Boolean argument)
	{
		this.redstone = argument;
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
		return null;
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer)
	{
		// NO-OP
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer)
	{
		// NO-OP
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public boolean equals(IPeripheral other)
	{
		return this.equals((Object) other);
	}

	@Override
	public boolean prepareToMove()
	{
		this.stopBreaking();
		return true;
	}

	@Override
	public void doneMoving()
	{

	}

	@Override
	public String getComponentName()
	{
		return this.getType();
	}

	@Callback(doc = "function():boolean -- Returns Whether tablet is redstone activated")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getRedstone(Context context, Arguments args)
	{
		return new Object[] { this.redstone };
	}

	@Callback(doc = "function(boolean):Nil -- Sets Whether tablet is redstone activated")
	@Optional.Method(modid = "OpenComputers")
	public Object[] setRedstone(Context context, Arguments args)
	{
		this.setRedstoneImplementation(args.checkBoolean(0));
		return new Object[] { this.redstone };
	}

	@Callback(doc = "function():boolean -- Returns Whether tablet Left clicks")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getLeftClick(Context context, Arguments args)
	{
		return new Object[] { this.leftClick };
	}

	@Callback(doc = "function(boolean):Nil -- Sets Whether tablet Left Clicks")
	@Optional.Method(modid = "OpenComputers")
	public Object[] setLeftClick(Context context, Arguments args)
	{
		this.setLeftClickImplementation(args.checkBoolean(0));
		return new Object[] { this.leftClick };
	}

	// TODO {"hasItem", "trigger" };
	@Callback(doc = "function():number -- Returns tablet Rotation")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getRotation(Context context, Arguments args)
	{
		return new Object[] { this.getBlockMetadata() - 2 };
	}

	@Callback(doc = "function(number):Nil -- Sets tablet rotation")
	@Optional.Method(modid = "OpenComputers")
	public Object[] setRotation(Context context, Arguments args) throws Exception
	{
		this.setRotationImplementation((double) args.checkInteger(0));
		return new Object[] { this.getBlockMetadata() - 2 };
	}

	@Callback(doc = "function():boolean -- Returns wether tablet has an item or not")
	@Optional.Method(modid = "OpenComputers")
	public Object[] hasItem(Context context, Arguments args)
	{
		return new Object[] { this.getStackInSlot(0) != null };
	}

	@Callback(doc = "function():Nil -- Triggers tablets swing")
	@Optional.Method(modid = "OpenComputers")
	public Object[] trigger(Context context, Arguments args)
	{
		return this.triggerImplementation();
	}
}
