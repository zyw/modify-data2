package cn.demo.modifydata.util;

public enum EnumStopFlag {
    /**
     * 正常情况
     */
    NORMAL,
    /**
     * Rb,Rf,Rz列超线
     */
    RB_RF_RZ,
    /**
     * Rb,Rf,Rz为负数时
     */
    RB_RF_RZ_FS,
    /**
     * HD列超线
     */
    HD
}
