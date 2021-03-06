package jcg.mini_dji_go_app;

import java.util.UUID;

public final class BluetoothConstants {

    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public final static String KEY = "BLcbyvFdRafKfUPOAQWCc5RveU8lpZ08";
    public final static Integer REQUEST_ENABLE_BT = 1;
    public final static String DEFAULT_DEVICE_NAME = "Dloiseau";

    //public final static Integer FRAME_COMPRESS_QUALITY = 20; // [0-100] % - [ QUALITY*X ]
    //public final static Integer FRAME_COMPRESS_SIZE = 2; // [0-???] - [ SIZE/X ] - [Multiple of 2]


    // ------------------- CODIGO PROVISIONAL -------------------
    public final static Integer A1 = 20;
    public final static Integer A2 = 40;
    public final static Integer A3 = 60;
    public final static Integer B1 = 4;
    public final static Integer B2 = 2;
    public final static Integer B3 = 0;

    public final static Integer[] A = {A1,A2,A3,A1,A2,A3,A1,A2};
    public final static Integer[] B = {B1,B1,B1,B2,B2,B2,B3,B3};

}
