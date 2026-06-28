package io.github.crucible.bootstrap;

import cpw.mods.fml.common.launcher.FMLTweaker;
import io.github.crucible.CrucibleConfigs;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;

public class CrucibleCoremodHook {
    // Too lazy for a coremod
    public static void coremodHandleLaunch(File mcDir, LaunchClassLoader classLoader, FMLTweaker tweaker) {
        classLoader.addClassLoaderExclusion("io.github.crucible.bootstrap.");
        try {
            // Ensure our config is loaded way before everything that may need it
            Class.forName("io.github.crucible.CrucibleConfigs", true, classLoader);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        classLoader.registerTransformer("io.github.crucible.patches.RecurrentComplexTransformer");
        classLoader.registerTransformer("io.github.crucible.patches.StreamsTransformer");
        classLoader.registerTransformer("thermos.ThermosClassTransformer");

        // Force-recompute StackMapTables (kept LAST in the transformer chain) so classes left with stale
        // frames by COMPUTE_MAXS-only coremod/Mixin passes pass the split bytecode verifier. RFB's
        // rfb-asm-safety plugin only makes getCommonSuperClass safe for transformers that already
        // recompute; it does NOT force a recompute on e.g. Cauldron's patched ChunkProviderServer
        // .func_73153_a after a SpongePowered Mixin rewrites it with COMPUTE_MAXS only. Reflectively
        // loading that class (Dynmap's field scan) then throws
        // "VerifyError: Expecting a stackmap frame at branch target N". Measured on the lwjgl3ify server
        // path: the full pack fails this way on every modern JDK tested (21-25); Java 8 is unaffected
        // because its verifier fails over to the old type-inference verifier for these class files.
        if (CrucibleConfigs.configs.crucible_asm_frameSafety) {
            classLoader.registerTransformer("io.github.crucible.asm.AsmFrameSafetyTransformer");
        }
    }
}
