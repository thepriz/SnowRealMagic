package snownee.snow;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockWall;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileSnowLayer extends TileEntity
{
    private IBlockState state = Blocks.ACACIA_FENCE.getDefaultState();
    private boolean isFullHeight = false;

    public TileSnowLayer()
    {
    }

    public IBlockState getState()
    {
        return state;
    }

    public boolean isFullHeight()
    {
        return isFullHeight;
    }

    public void setState(IBlockState state)
    {
        this.state = state;
        Block block = state.getBlock();
        this.isFullHeight = block instanceof BlockWall || block instanceof BlockFence || block instanceof BlockPane;
        if (world != null && !world.isRemote)
        {
            IBlockState blockState = world.getBlockState(pos);
            world.markAndNotifyBlock(pos, null, blockState, blockState, 11);
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        Block block = null;
        if (compound.hasKey("blockId", Constants.NBT.TAG_STRING))
        {
            block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(compound.getString("blockId")));
        }
        if (block == null)
        {
            block = Blocks.AIR;
        }
        int meta = 0;
        if (compound.hasKey("blockMeta", Constants.NBT.TAG_INT))
        {
            meta = compound.getInteger("blockMeta");
        }
        state = block.getStateFromMeta(meta);
        isFullHeight = block instanceof BlockWall || block instanceof BlockFence || block instanceof BlockPane;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        compound.setString("blockId", ForgeRegistries.BLOCKS.getKey(state.getBlock()).toString());
        compound.setInteger("blockMeta", state.getBlock().getMetaFromState(state));
        return compound;
    }

    @Override
    public boolean hasFastRenderer()
    {
        return !(ModConfig.forceNormalTESR || state.getBlock() instanceof BlockBush);
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public final SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(this.pos, -1, this.writeToNBT(new NBTTagCompound()));
    }

    @Override
    public final void onDataPacket(NetworkManager manager, SPacketUpdateTileEntity packet)
    {
        this.readFromNBT(packet.getNbtCompound());
    }

    @Nonnull
    @Override
    public final NBTTagCompound getUpdateTag()
    {
        return this.writeToNBT(new NBTTagCompound());
    }

    @Override
    public final void handleUpdateTag(NBTTagCompound tag)
    {
        this.readFromNBT(tag);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return state.getBoundingBox(world, pos).offset(pos);
    }
}