package mrriegel.storagenetwork.items;

import java.util.List;

import mrriegel.storagenetwork.CreativeTab;
import mrriegel.storagenetwork.StorageNetwork;
import mrriegel.storagenetwork.config.ConfigHandler;
import mrriegel.storagenetwork.handler.GuiHandler;
import mrriegel.storagenetwork.helper.NBTHelper;
import mrriegel.storagenetwork.tile.TileMaster;
import mrriegel.storagenetwork.tile.TileRequest.Sort;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRemote extends Item {

	public ItemRemote() {
		super();
		this.setCreativeTab(CreativeTab.tab1);
		this.setHasSubtypes(true);
		this.setUnlocalizedName(StorageNetwork.MODID + ":remote");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for (int i = 0; i < 2; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "_" + stack.getItemDamage();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		tooltip.add(StatCollector.translateToLocal("tooltip.storagenetwork.remote_" + stack.getItemDamage()));
		if (stack.hasTagCompound() && NBTHelper.getBoolean(stack, "bound")) {
			tooltip.add("Dimension: " + NBTHelper.getInteger(stack, "dim") + ", x: " + NBTHelper.getInteger(stack, "x") + ", y: " + NBTHelper.getInteger(stack, "y") + ", z: " + NBTHelper.getInteger(stack, "z"));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn) {
		if (worldIn.isRemote)
			return super.onItemRightClick(itemStackIn, worldIn, playerIn);
		int x = NBTHelper.getInteger(itemStackIn, "x");
		int y = NBTHelper.getInteger(itemStackIn, "y");
		int z = NBTHelper.getInteger(itemStackIn, "z");
		World world = MinecraftServer.getServer().worldServerForDimension(NBTHelper.getInteger(itemStackIn, "dim"));
		if (NBTHelper.getBoolean(itemStackIn, "bound") && world.getTileEntity(new BlockPos(x, y, z)) instanceof TileMaster) {
			if ((itemStackIn.getItemDamage() == 0 && NBTHelper.getInteger(itemStackIn, "dim") == worldIn.provider.getDimensionId() && playerIn.getDistance(x, y, z) <= ConfigHandler.rangeWirelessAccessor) || itemStackIn.getItemDamage() == 1) {
				if (world.getChunkFromBlockCoords(new BlockPos(x, y, z)).isLoaded()) {
					if (NBTHelper.getString(itemStackIn, "sort") == null)
						NBTHelper.setString(playerIn.getHeldItem(), "sort", Sort.NAME.toString());
					playerIn.openGui(StorageNetwork.instance, getGui(), world, x, y, z);
				} else
					playerIn.addChatMessage(new ChatComponentText("Cable Master not loaded."));
			} else if (itemStackIn.getItemDamage() == 0 && (NBTHelper.getInteger(itemStackIn, "dim") == worldIn.provider.getDimensionId() || playerIn.getDistance(x, y, z) > 32))
				if (!worldIn.isRemote)
					playerIn.addChatMessage(new ChatComponentText("Out of Range"));
		}
		return super.onItemRightClick(itemStackIn, worldIn, playerIn);
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (worldIn.getTileEntity(pos) instanceof TileMaster) {
			NBTHelper.setInteger(stack, "x", pos.getX());
			NBTHelper.setInteger(stack, "y", pos.getY());
			NBTHelper.setInteger(stack, "z", pos.getZ());
			NBTHelper.setBoolean(stack, "bound", true);
			NBTHelper.setInteger(stack, "dim", worldIn.provider.getDimensionId());
			NBTHelper.setString(stack, "sort", Sort.NAME.toString());
			return true;
		}
		return super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
	}

	protected int getGui() {
		return GuiHandler.REMOTE;
	}

	public static TileMaster getTile(ItemStack stack) {
		if (stack == null)
			return null;
		TileEntity t = MinecraftServer.getServer().worldServerForDimension(NBTHelper.getInteger(stack, "dim")).getTileEntity(new BlockPos(NBTHelper.getInteger(stack, "x"), NBTHelper.getInteger(stack, "y"), NBTHelper.getInteger(stack, "z")));
		return t instanceof TileMaster ? (TileMaster) t : null;

	}

	public static void copyTag(ItemStack from, ItemStack to) {
		NBTHelper.setInteger(to, "x", NBTHelper.getInteger(from, "x"));
		NBTHelper.setInteger(to, "y", NBTHelper.getInteger(from, "y"));
		NBTHelper.setInteger(to, "z", NBTHelper.getInteger(from, "z"));
		NBTHelper.setBoolean(to, "bound", NBTHelper.getBoolean(from, "bound"));
		NBTHelper.setInteger(to, "dim", NBTHelper.getInteger(from, "dim"));
		NBTHelper.setString(to, "sort", NBTHelper.getString(from, "sort"));
	}

}
