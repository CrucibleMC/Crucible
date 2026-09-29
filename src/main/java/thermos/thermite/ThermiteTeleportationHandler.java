package thermos.thermite;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLOutboundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S07PacketRespawn;
import net.minecraft.network.play.server.S1DPacketEntityEffect;
import net.minecraft.network.play.server.S1FPacketSetExperience;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.network.ForgeMessage;
import net.minecraftforge.common.network.ForgeNetworkHandler;
import org.bukkit.Location;
import org.bukkit.World.Environment;

import java.util.Iterator;

public class ThermiteTeleportationHandler {
    public static void transferEntityToWorld(Entity ent, WorldServer newWorld, double x, double y, double z, float yaw, float pitch) {

        x = MathHelper.clamp_double(x, -29999872, 29999872);
        z = MathHelper.clamp_double(z, -29999872, 29999872);

        if (ent.isEntityAlive()) {
            ent.setLocationAndAngles(x, y, z, yaw, pitch);
            newWorld.spawnEntityInWorld(ent);
            newWorld.updateEntityWithOptionalForce(ent, false);
        }

        ent.setWorld(newWorld);
    }

    /**
     * Moves a player to {@code to} in another dimension. The player is placed at the destination before the new
     * world's PlayerManager registers them, so only the chunks around {@code to} are loaded.
     */
    public static void transferPlayerToDimension(EntityPlayerMP player, int dim, ServerConfigurationManager manager, Environment environ, Location to) {

        int oldDim = player.dimension;
        WorldServer worldserver = manager.getServerInstance().worldServerForDimension(player.dimension);
        player.dimension = dim;
        WorldServer worldserver1 = manager.getServerInstance().worldServerForDimension(player.dimension);
        // Cauldron dont crash the client, let 'em know there's a new dimension in town
        if (DimensionManager.isBukkitDimension(dim)) {
            FMLEmbeddedChannel serverChannel = ForgeNetworkHandler.getServerChannel();
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
            serverChannel.attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
            serverChannel.writeOutbound(new ForgeMessage.DimensionRegisterMessage(dim, environ.getId()));
        }
        // Cauldron end
        player.playerNetServerHandler.sendPacket(new S07PacketRespawn(dim, worldserver1.difficultySetting, worldserver1.getWorldInfo()
                .getTerrainType(), player.theItemInWorldManager.getGameType()));
        player.playerNetServerHandler.sendPacket(new S1FPacketSetExperience(player.experience, player.experienceTotal, player.experienceLevel));

        worldserver.removePlayerEntityDangerously(player);
        if (player.riddenByEntity != null) {
            player.riddenByEntity.mountEntity(null);
        }
        if (player.ridingEntity != null) {
            player.mountEntity(null);
        }
        player.isDead = false;
        transferEntityToWorld(player, worldserver1, to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        manager.func_72375_a(player, worldserver);
        // teleport, not setPlayerLocation: the latter fires a second PlayerTeleportEvent on top of the caller's
        player.playerNetServerHandler.teleport(new Location(worldserver1.getWorld(), player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch));
        player.theItemInWorldManager.setWorld(worldserver1);
        manager.updateTimeAndWeatherForPlayer(player, worldserver1);
        manager.syncPlayerInventory(player);
        Iterator<PotionEffect> iterator = player.getActivePotionEffects().iterator();

        while (iterator.hasNext()) {
            PotionEffect potioneffect = iterator.next();
            player.playerNetServerHandler.sendPacket(new S1DPacketEntityEffect(player.getEntityId(), potioneffect));
        }
        FMLCommonHandler.instance().firePlayerChangedDimensionEvent(player, oldDim, dim);
    }
}
