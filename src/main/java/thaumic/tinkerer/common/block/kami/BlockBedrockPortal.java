package thaumic.tinkerer.common.block.kami;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Facing;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import thaumic.tinkerer.client.core.helper.IconHelper;
import thaumic.tinkerer.common.block.BlockMod;
import thaumic.tinkerer.common.block.tile.kami.TileBedrockPortal;
import thaumic.tinkerer.common.core.handler.ConfigHandler;
import thaumic.tinkerer.common.core.handler.ModCreativeTab;
import thaumic.tinkerer.common.dim.TeleporterBedrock;
import thaumic.tinkerer.common.dim.WorldProviderBedrock;
import thaumic.tinkerer.common.lib.LibBlockNames;
import thaumic.tinkerer.common.registry.ThaumicTinkererRecipe;
import thaumic.tinkerer.common.research.IRegisterableResearch;

import java.util.Random;

public class BlockBedrockPortal extends BlockMod
{

	@SideOnly(Side.CLIENT)
	private IIcon icon;

	public BlockBedrockPortal()
	{
		super(Material.portal);
		this.setStepSound(Block.soundTypeStone);
		this.setResistance(6000000.0F);
		this.disableStats();
		this.setCreativeTab(ModCreativeTab.INSTANCE);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p_149727_5_, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
	{
		world.setBlock(x, y, z, Blocks.bedrock);
		return super.onBlockActivated(world, x, y, z, p_149727_5_, p_149727_6_, p_149727_7_, p_149727_8_, p_149727_9_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister)
	{

		this.icon = IconHelper.forName(iconRegister, "portal");

	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int meta)
	{
		return this.icon;
	}

	@Override
	public float getBlockHardness(World par1World, int par2, int par3, int par4)
	{
		return -1;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public boolean canHarvestBlock(EntityPlayer player, int meta)
	{
		return false;
	}

	@Override
	public TileEntity createTileEntity(World world, int metadata)
	{
		return new TileBedrockPortal();
	}

	@Override
	public boolean canEntityDestroy(IBlockAccess world, int x, int y, int z, Entity entity)
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5)
	{
		if (par5 != 1 && par5 != 0 && !super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5))
			return false;
		else
		{
			int i1 = par2 + Facing.offsetsXForSide[Facing.oppositeSide[par5]];
			int j1 = par3 + Facing.offsetsYForSide[Facing.oppositeSide[par5]];
			int k1 = par4 + Facing.offsetsZForSide[Facing.oppositeSide[par5]];
			boolean flag = (par1IBlockAccess.getBlockMetadata(i1, j1, k1) & 8) != 0;
			return flag ? par5 == 0 || (par5 == 1 && super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5) ? true : true) : par5 == 1 || (par5 == 0 && super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5) ? true : true);
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity entity)
	{
		super.onEntityCollidedWithBlock(par1World, par2, par3, par4, entity);

		if (entity.worldObj.provider.isSurfaceWorld())
		{
			if (entity instanceof EntityPlayer && !par1World.isRemote)
			{

				FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().transferPlayerToDimension((EntityPlayerMP) entity, ConfigHandler.bedrockDimensionID, new TeleporterBedrock((WorldServer) par1World));
				if (entity.worldObj.getBlock(par2, 250, par4) == Blocks.bedrock)
					entity.worldObj.setBlock(par2, 250, par4, Blocks.air);
				if (entity.worldObj.getBlock(par2, 251, par4) == Blocks.bedrock)
					entity.worldObj.setBlock(par2, 251, par4, Blocks.air);
				if (entity.worldObj.getBlock(par2, 252, par4) == Blocks.bedrock)
					entity.worldObj.setBlock(par2, 252, par4, Blocks.air);
				if (entity.worldObj.getBlock(par2, 253, par4) == Blocks.bedrock)
					entity.worldObj.setBlock(par2, 253, par4, Blocks.air);
				if (entity.worldObj.getBlock(par2, 254, par4) == Blocks.bedrock)
					entity.worldObj.setBlock(par2, 254, par4, this);
				((EntityPlayerMP) entity).playerNetServerHandler.setPlayerLocation(par2 + 0.5, 251, par4 + 0.5, 0, 0);
			}
		}
		else if (entity.worldObj.provider instanceof WorldProviderBedrock)
			if (entity instanceof EntityPlayer && !par1World.isRemote)
			{
				EntityPlayerMP player = (EntityPlayerMP) entity;
				FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().transferPlayerToDimension(player, 0, new TeleporterBedrock((WorldServer) par1World));

				Random rand = new Random();

				int x = (int) player.posX + rand.nextInt(100);
				int z = (int) player.posZ + rand.nextInt(100);

				x -= 50;
				z -= 50;

				int y = 120;

				/* TODO gamerforEA code replace, old code:
				while (player.worldObj.getBlock(x, y, z) == Blocks.air || player.worldObj.getBlock(x, y, z).isAir(par1World, x, y, z))
					y--; */
				for (; y >= 0; y--)
				{
					Block block = player.worldObj.getBlock(x, y, z);
					if (!block.isAir(player.worldObj, x, y, z))
						break;
				}
				// TODO gamerforEA code end

				player.playerNetServerHandler.setPlayerLocation(x + 0.5, y + 3, z + 0.5, 0, 0);
			}

	}

	public void travelToDimension(int par1, Entity e)
	{
		if (!e.worldObj.isRemote && !e.isDead)
		{
			e.worldObj.theProfiler.startSection("changeDimension");
			MinecraftServer minecraftserver = MinecraftServer.getServer();
			int j = e.dimension;
			WorldServer worldserver = minecraftserver.worldServerForDimension(j);
			WorldServer worldserver1 = minecraftserver.worldServerForDimension(par1);
			e.dimension = par1;

			if (j == 1 && par1 == 1)
			{
				worldserver1 = minecraftserver.worldServerForDimension(0);
				e.dimension = 0;
			}

			e.worldObj.removeEntity(e);
			e.isDead = false;
			e.worldObj.theProfiler.startSection("reposition");
			minecraftserver.getConfigurationManager().transferEntityToWorld(e, j, worldserver, worldserver1, new TeleporterBedrock(worldserver));
			e.worldObj.theProfiler.endStartSection("reloading");
			Entity entity = EntityList.createEntityByName(EntityList.getEntityString(e), worldserver1);

			if (entity != null)
			{
				entity.copyDataFrom(e, true);

				if (j == 1 && par1 == 1)
				{
					ChunkCoordinates chunkcoordinates = worldserver1.getSpawnPoint();
					chunkcoordinates.posY = e.worldObj.getTopSolidOrLiquidBlock(chunkcoordinates.posX, chunkcoordinates.posZ);
					entity.setLocationAndAngles(chunkcoordinates.posX, chunkcoordinates.posY, chunkcoordinates.posZ, entity.rotationYaw, entity.rotationPitch);
				}

				worldserver1.spawnEntityInWorld(entity);
			}

			e.isDead = true;
			e.worldObj.theProfiler.endSection();
			worldserver.resetUpdateEntityTick();
			worldserver1.resetUpdateEntityTick();
			e.worldObj.theProfiler.endSection();
		}
	}

	@Override
	public String getBlockName()
	{
		return LibBlockNames.PORTAL;
	}

	@Override
	public Class<? extends ItemBlock> getItemBlock()
	{
		return null;
	}

	@Override
	public Class<? extends TileEntity> getTileEntity()
	{
		return TileBedrockPortal.class;
	}

	@Override
	public IRegisterableResearch getResearchItem()
	{
		return null;
	}

	@Override
	public ThaumicTinkererRecipe getRecipeItem()
	{
		return null;
	}
}
