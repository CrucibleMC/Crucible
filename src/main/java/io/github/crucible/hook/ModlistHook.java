package io.github.crucible.hook;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;

import cpw.mods.fml.common.eventhandler.Event.Result;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.handshake.NetworkDispatcher;
import cpw.mods.fml.relauncher.Side;
import io.github.crucible.events.CruciblePlayerLoginEvent;
import io.github.crucible.utils.CrucibleModList;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayerMP;

public class ModlistHook {
	
	public static ModlistHook instance = new ModlistHook();
	
	public static ChannelHandlerContext channelHandlerContext;
	public static HashMap<NetworkDispatcher, Map<String, String>> tempData = new HashMap<NetworkDispatcher, Map<String, String>>();
	
	/*
	 *  Hooked on FMLNetworkHandler;
	 */
	public boolean checkMods(Map<String, String> mods, Side remoteSide) {
		if(!remoteSide.equals(Side.SERVER)) {
			if(channelHandlerContext != null) {
				NetworkDispatcher networkDispatcher = channelHandlerContext.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
				tempData.put(networkDispatcher, mods);
			}
		}
		
		return true;
	}
	
	 
	/*
	 * Hooked on FMLCommonHandler;
	 */
	 public void playerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		 if(event.getResult().equals(Result.DENY)) return;
		 
		 EntityPlayerMP player = (EntityPlayerMP)event.player;
		 
		 NetworkDispatcher networkDispatcher = (NetworkDispatcher) player.playerNetServerHandler.netManager.channel().attr(NetworkDispatcher.FML_DISPATCHER).get();
		 
		 Map<String, String> data;
		 
		 if((data = tempData.getOrDefault(networkDispatcher,null)) != null){
			 CrucibleModList crucibleModList = new CrucibleModList();
			 
			 
			 if(crucibleModList.loadFromObject(data)) {
				 CruciblePlayerLoginEvent cruciblePlayerLoginEvent = new CruciblePlayerLoginEvent(player.getBukkitEntity(), crucibleModList);
				 Bukkit.getPluginManager().callEvent(cruciblePlayerLoginEvent);
				 if(cruciblePlayerLoginEvent.getResult() == CruciblePlayerLoginEvent.Result.DENY) {
					 player.getBukkitEntity().kickPlayer(cruciblePlayerLoginEvent.getResultMessage());
				 }
				 
			 }
			 
		 }
		 
	 }

}
