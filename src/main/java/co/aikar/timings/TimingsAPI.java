package co.aikar.timings;

import net.minecraft.tileentity.TileEntity;

public class TimingsAPI {

    public static TimingHandler getTimingFromTileEntity(TileEntity tileEntity){
        return (TimingHandler) tileEntity.getPersonalTickTimer();
    }

}
