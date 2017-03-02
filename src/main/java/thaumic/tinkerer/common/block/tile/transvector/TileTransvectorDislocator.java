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
 * File Created @ [Nov 24, 2013, 6:48:04 PM (GMT)]
 */
package thaumic.tinkerer.common.block.tile.transvector;

import java.util.List;

import com.gamerforea.ttinkerer.EventConfig;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.util.ForgeDirection;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.codechicken.lib.vec.Vector3;
import thaumcraft.common.config.ConfigBlocks;
import thaumic.tinkerer.common.ThaumicTinkerer;
import thaumic.tinkerer.common.block.transvector.BlockTransvectorDislocator;
import thaumic.tinkerer.common.lib.LibFeatures;

public class TileTransvectorDislocator extends TileTransvector
{
	private static final String TAG_ORIENTATION = "orientation";
	public int orientation;
	private int cooldown = 0;
	private boolean pulseStored = false;

	@Override
	public void updateEntity()
	{
		super.updateEntity();

		this.cooldown = Math.max(0, this.cooldown - 1);
		if (this.cooldown == 0 && this.pulseStored)
		{
			this.pulseStored = false;
			this.receiveRedstonePulse();
		}
	}

	public void receiveRedstonePulse()
	{
		this.getTile(); // sanity check

		if (this.y < 0)
			return;

		if (this.cooldown > 0)
		{
			this.pulseStored = true;
			return;
		}

		ChunkCoordinates endCoords = new ChunkCoordinates(this.x, this.y, this.z);
		ChunkCoordinates targetCoords = this.getBlockTarget();
		List<Entity> entitiesAtEnd = this.getEntitiesAtPoint(endCoords);
		List<Entity> entitiesAtTarget = this.getEntitiesAtPoint(targetCoords);

		Vector3 targetToEnd = this.asVector(targetCoords, endCoords);
		Vector3 endToTarget = this.asVector(endCoords, targetCoords);
		if (this.worldObj.blockExists(this.x, this.y, this.z))
		{
			BlockData endData = new BlockData(endCoords);
			BlockData targetData = new BlockData(targetCoords);

			if (this.checkBlock(targetCoords) && this.checkBlock(endCoords))
			{
				endData.clearTileEntityAt();
				targetData.clearTileEntityAt();

				endData.setTo(targetCoords);
				targetData.setTo(endCoords);

				endData.notify(targetCoords);
				targetData.notify(endCoords);
			}
		}

		for (Entity entity : entitiesAtEnd)
			this.moveEntity(entity, endToTarget);
		for (Entity entity : entitiesAtTarget)
			this.moveEntity(entity, targetToEnd);

		this.cooldown = 10;
	}

	private boolean checkBlock(ChunkCoordinates coords)
	{
		Block block = this.worldObj.getBlock(coords.posX, coords.posY, coords.posZ);
		int meta = this.worldObj.getBlockMetadata(coords.posX, coords.posY, coords.posZ);

		// TODO gamerforEA code start
		if (EventConfig.inBlackList(EventConfig.transvectorBlackList, block, meta))
			return false;

		if (this.fake.cantBreak(coords.posX, coords.posY, coords.posZ))
			return false;
		// TODO gamerforEA code end

		return !(block == ConfigBlocks.blockAiry && meta == 0) && !ThaumcraftApi.portableHoleBlackList.contains(block) && block != null && block.getBlockHardness(this.worldObj, coords.posX, coords.posY, coords.posZ) != -1F || block != Blocks.air;
	}

	private List<Entity> getEntitiesAtPoint(ChunkCoordinates coords)
	{
		return this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(coords.posX, coords.posY, coords.posZ, coords.posX + 1, coords.posY + 1, coords.posZ + 1));
	}

	private Vector3 asVector(ChunkCoordinates source, ChunkCoordinates target)
	{
		return new Vector3(target.posX, target.posY, target.posZ).subtract(new Vector3(source.posX, source.posY, source.posZ));
	}

	private void moveEntity(Entity entity, Vector3 vec)
	{
		if (entity instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP) entity;
			player.playerNetServerHandler.setPlayerLocation(entity.posX + vec.x, entity.posY + vec.y, entity.posZ + vec.z, player.rotationYaw, player.rotationPitch);
		}
		else
			entity.setPosition(entity.posX + vec.x, entity.posY + vec.y, entity.posZ + vec.z);
	}

	public ChunkCoordinates getBlockTarget()
	{
		ForgeDirection dir = ForgeDirection.getOrientation(this.orientation);
		return new ChunkCoordinates(this.xCoord + dir.offsetX, this.yCoord + dir.offsetY, this.zCoord + dir.offsetZ);
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp)
	{
		super.readCustomNBT(cmp);

		this.orientation = cmp.getInteger(TAG_ORIENTATION);
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp)
	{
		super.writeCustomNBT(cmp);

		cmp.setInteger(TAG_ORIENTATION, this.orientation);
	}

	@Override
	public int getMaxDistance()
	{
		return LibFeatures.DISLOCATOR_DISTANCE;
	}

	@Override
	boolean tileRequiredAtLink()
	{
		return false;
	}

	class BlockData
	{

		Block block;
		int meta;
		NBTTagCompound tile;

		ChunkCoordinates coords;

		public BlockData(Block block, int meta, TileEntity tile, ChunkCoordinates coords)
		{
			this.block = block;
			this.meta = meta;

			if (tile != null)
			{
				NBTTagCompound cmp = new NBTTagCompound();
				tile.writeToNBT(cmp);
				this.tile = cmp;
			}
			;

			this.coords = coords;
		}

		public BlockData(ChunkCoordinates coords)
		{
			this(TileTransvectorDislocator.this.worldObj.getBlock(coords.posX, coords.posY, coords.posZ), TileTransvectorDislocator.this.worldObj.getBlockMetadata(coords.posX, coords.posY, coords.posZ), TileTransvectorDislocator.this.worldObj.getTileEntity(coords.posX, coords.posY, coords.posZ), coords);
		}

		public void clearTileEntityAt()
		{
			if (this.block != null)
			{
				TileEntity tileToSet = this.block.createTileEntity(TileTransvectorDislocator.this.worldObj, this.meta);
				TileTransvectorDislocator.this.worldObj.setTileEntity(this.coords.posX, this.coords.posY, this.coords.posZ, tileToSet);
			}
		}

		public void setTo(ChunkCoordinates coords)
		{
			TileTransvectorDislocator.this.worldObj.setBlock(coords.posX, coords.posY, coords.posZ, this.block, this.meta, 2);

			TileEntity tile = this.tile == null ? null : TileEntity.createAndLoadEntity(this.tile);

			TileTransvectorDislocator.this.worldObj.setTileEntity(coords.posX, coords.posY, coords.posZ, tile);

			if (tile != null)
			{
				tile.xCoord = coords.posX;
				tile.yCoord = coords.posY;
				tile.zCoord = coords.posZ;
				tile.updateContainingBlockInfo();
			}

			//if (block != null)
			//	block.onNeighborBlockChange(worldObj, coords.posX, coords.posY, coords.posZ, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockTransvectorDislocator.class));

			TileTransvectorDislocator.this.worldObj.setBlockMetadataWithNotify(coords.posX, coords.posY, coords.posZ, this.meta, 2);

		}

		public void notify(ChunkCoordinates coords)
		{

			if (this.block != null)
				this.block.onNeighborBlockChange(TileTransvectorDislocator.this.worldObj, coords.posX, coords.posY, coords.posZ, ThaumicTinkerer.registry.getFirstBlockFromClass(BlockTransvectorDislocator.class));

		}
	}

}
