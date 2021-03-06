package org.bukkit.craftbukkit.entity;

import net.minecraft.server.EntityEnderman;

import net.minecraft.server.IBlockData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.material.MaterialData;

public class CraftEnderman extends CraftMonster implements Enderman {
    public CraftEnderman(CraftServer server, EntityEnderman entity) {
        super(server, entity);
    }

    @Override public boolean teleportRandomly() { return getHandle().teleportRandomly(); } // Paper
    public MaterialData getCarriedMaterial() {
        IBlockData blockData = getHandle().getCarried();
        return (blockData == null) ? Material.AIR.getNewData((byte) 0) : CraftMagicNumbers.getMaterial(blockData.getBlock()).getNewData((byte) blockData.getBlock().toLegacyData(blockData));
    }

    public void setCarriedMaterial(MaterialData data) {
        getHandle().setCarried(CraftMagicNumbers.getBlock(data.getItemTypeId()).fromLegacyData(data.getData()));
    }

    @Override
    public EntityEnderman getHandle() {
        return (EntityEnderman) entity;
    }

    @Override
    public String toString() {
        return "CraftEnderman";
    }

    public EntityType getType() {
        return EntityType.ENDERMAN;
    }
}
