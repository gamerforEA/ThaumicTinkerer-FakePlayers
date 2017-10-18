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
 * File Created @ [Nov 24, 2013, 6:40:49 PM (GMT)]
 */
package thaumic.tinkerer.common.block.tile.transvector;

import com.gamerforea.eventhelper.fake.FakePlayerContainer;
import com.gamerforea.eventhelper.fake.FakePlayerContainerTileEntity;
import com.gamerforea.ttinkerer.ModUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import thaumic.tinkerer.common.block.tile.TileCamo;

public abstract class TileTransvector extends TileCamo
{
	private static final String TAG_X_TARGET = "xt";
	private static final String TAG_Y_TARGET = "yt";
	private static final String TAG_Z_TARGET = "zt";
	private static final String TAG_CHEATY_MODE = "cheatyMode";

	public int x = 0, y = -1, z = 0;
	private boolean cheaty;

	// TODO gamerforEA code start
	public final FakePlayerContainer fake = new FakePlayerContainerTileEntity(ModUtils.profile, this);
	// TODO gamerforEA code end

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	@Override
	public void writeCustomNBT(NBTTagCompound cmp)
	{
		super.writeCustomNBT(cmp);

		cmp.setInteger(TAG_X_TARGET, this.x);
		cmp.setInteger(TAG_Y_TARGET, this.y);
		cmp.setInteger(TAG_Z_TARGET, this.z);
		cmp.setBoolean(TAG_CHEATY_MODE, this.cheaty);

		// TODO gamerforEA code start
		this.fake.writeToNBT(cmp);
		// TODO gamerforEA code end
	}

	@Override
	public void readCustomNBT(NBTTagCompound cmp)
	{
		super.readCustomNBT(cmp);

		this.x = cmp.getInteger(TAG_X_TARGET);
		this.y = cmp.getInteger(TAG_Y_TARGET);
		this.z = cmp.getInteger(TAG_Z_TARGET);
		this.cheaty = cmp.getBoolean(TAG_CHEATY_MODE);

		// TODO gamerforEA code start
		this.fake.readFromNBT(cmp);
		// TODO gamerforEA code end
	}

	public final TileEntity getTile()
	{
		if (!this.worldObj.blockExists(this.x, this.y, this.z))
			return null;

		TileEntity tile = this.worldObj.getTileEntity(this.x, this.y, this.z);

		if (tile == null && this.tileRequiredAtLink() || (Math.abs(this.x - this.xCoord) > this.getMaxDistance() || Math.abs(this.y - this.yCoord) > this.getMaxDistance() || Math.abs(this.z - this.zCoord) > this.getMaxDistance()) && !this.cheaty)
		{
			this.y = -1;
			return null;
		}

		return tile;
	}

	public abstract int getMaxDistance();

	boolean tileRequiredAtLink()
	{
		return !this.cheaty;
	}

}
