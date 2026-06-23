package io.github.crucible.bootstrap.rfb;

import com.gtnewhorizons.retrofuturabootstrap.api.PluginContext;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbPlugin;
import io.github.crucible.bootstrap.Lwjgl3ifyIntegration;
import org.jetbrains.annotations.NotNull;

public class CrucibleRfbPlugin implements RfbPlugin {
    @Override
    public void onConstruction(@NotNull PluginContext ctx) {
        Lwjgl3ifyIntegration.registerExtensibleEnums();
    }
}
