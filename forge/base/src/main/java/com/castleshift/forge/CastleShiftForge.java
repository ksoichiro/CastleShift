package com.castleshift.forge;

import com.castleshift.CastleShift;
import net.minecraftforge.fml.common.Mod;

@Mod(CastleShift.MOD_ID)
public class CastleShiftForge {
    public CastleShiftForge() {
        CastleShift.init();
    }
}
