package org.polyfrost.forgecompat.mixin;

import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportCategory;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public class CrashReportMixin {
    @Shadow
    @Final
    private CrashReportCategory systemDetails;

    @Inject(method = "fillSystemDetails", at = @At("TAIL"))
    private void forgecompat$fillSystemDetails(CallbackInfo ci) {
        FMLCommonHandler.instance().enhanceCrashReport((CrashReport) (Object) this, systemDetails);
    }
}
