package snownee.snow.block;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import snownee.kiwi.block.ModBlock;
import snownee.kiwi.util.NBTHelper;
import snownee.kiwi.util.Util;
import snownee.snow.MainModule;
import snownee.snow.SnowCommonConfig;

public class SnowFenceBlock extends FenceBlock implements ISnowVariant
{
    public static enum Mat implements IStringSerializable
    {
        WOOD(Material.WOOD), ROCK(Material.ROCK), IRON(Material.IRON), MISC(Material.CLAY);

        public final Material material;

        private Mat(Material material)
        {
            this.material = material;
        }

        @Override
        public String getName()
        {
            return toString().toLowerCase(Locale.ENGLISH);
        }
    }

    public static final BooleanProperty DOWN = SixWayBlock.DOWN;
    public static final EnumProperty<Mat> MAT = EnumProperty.create("mat", Mat.class);

    public SnowFenceBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new SnowTextureTile();
    }

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
    {
        return ModBlock.pickBlock(state, target, world, pos, player);
    }

    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public Material getMaterial(BlockState state)
    {
        return state.get(MAT).material;
    }

    public boolean canConnect(BlockState stateIn, BlockState p_220111_1_, boolean p_220111_2_, Direction p_220111_3_)
    {
        Block block = p_220111_1_.getBlock();
        boolean flag = block.isIn(BlockTags.FENCES) && p_220111_1_.getMaterial() == this.getMaterial(stateIn);
        boolean flag1 = block instanceof FenceGateBlock && FenceGateBlock.isParallel(p_220111_1_, p_220111_3_);
        return !cannotAttach(block) && p_220111_2_ || flag || flag1;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context)
    {
        World iblockreader = context.getWorld();
        BlockPos blockpos = context.getPos();
        BlockState stateIn = iblockreader.getBlockState(blockpos);
        BlockState state = super.getStateForPlacement(context).with(WATERLOGGED, false).with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, iblockreader, blockpos));
        ItemStack stack = context.getItem();
        NBTHelper data = NBTHelper.of(stack);
        String rl = data.getString("BlockEntityTag.Items.0");
        if (rl != null && ResourceLocation.func_217855_b(rl))
        {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(rl));
            if (item != null && item instanceof BlockItem)
            {
                Block block = ((BlockItem) item).getBlock();
                Material mat = block.getDefaultState().getMaterial();
                if (mat == Material.WOOD)
                {
                    state = state.with(MAT, Mat.WOOD);
                }
                else if (mat == Material.ROCK)
                {
                    state = state.with(MAT, Mat.ROCK);
                }
                else if (mat == Material.IRON)
                {
                    state = state.with(MAT, Mat.IRON);
                }
                else
                {
                    state = state.with(MAT, Mat.MISC);
                }
            }
        }
        return state;
    }

    @Override
    public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
    {
        if (facing == Direction.DOWN)
        {
            ModSnowTileBlock.updateSnowyDirt(worldIn, facingPos, facingState);
            return stateIn.with(DOWN, MainModule.BLOCK.isValidPosition(stateIn, worldIn, currentPos, true));
        }
        return facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), Boolean.valueOf(this.canConnect(stateIn, facingState, facingState.func_224755_d(worldIn, facingPos, facing.getOpposite()), facing.getOpposite()))) : stateIn;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
    {
        builder.add(NORTH, EAST, WEST, SOUTH, DOWN, MAT, WATERLOGGED);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
    {
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        String key = Util.getTextureItem(stack, "0");
        if (!key.isEmpty())
        {
            tooltip.add(new TranslationTextComponent(key).applyTextStyle(TextFormatting.GRAY));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items)
    {
        MainModule.fillTextureItems(ItemTags.FENCES, this, items);
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random)
    {
        if (!SnowCommonConfig.snowNeverMelt && worldIn.getLightFor(LightType.BLOCK, pos) > 11)
        {
            worldIn.setBlockState(pos, getRaw(state, worldIn, pos));
        }
    }
}
