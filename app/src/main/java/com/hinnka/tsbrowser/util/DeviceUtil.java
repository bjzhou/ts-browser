package com.hinnka.tsbrowser.util;

import android.os.Build;

public class DeviceUtil {
    private static final String ROM_HUAWE = "huawei";
    private static final String ROM_HONOR = "honor";
    private static final String ROM_MIUI = "xiaomi";
    private static final String ROM_VIVO = "vivo";
    private static final String ROM_OPPO = "oppo";
    private static final String ROM_MEIZU = "meizu";
    private static final String ROM_SAMSUNG = "samsung";
    private static final String ROM_SMARTISAN = "smartisan";

    public static boolean isHuawei() {
        return isROM(ROM_HUAWE, ROM_HONOR);
    }
    public static boolean isMIUI() {
        return isROM(ROM_MIUI);
    }
    public static boolean isVivo() {
        return isROM(ROM_VIVO);
    }
    public static boolean isOppo() {
        return isROM(ROM_OPPO);
    }
    public static boolean isMeizu() {
        return isROM(ROM_MEIZU);
    }
    public static boolean isSamsung() {
        return isROM(ROM_SAMSUNG);
    }
    public static boolean isSmartisan() {
        return isROM(ROM_SMARTISAN);
    }

    private static boolean isROM(String... roms) {
        String brand = Build.BRAND; //获取主板
        String manufacturer = Build.MANUFACTURER; //获取硬件制造商
        for (String rom : roms) {
            if(rom.equalsIgnoreCase(brand) || rom.equalsIgnoreCase(manufacturer)){
                return true;
            }
        }
        return false;
    }
}
