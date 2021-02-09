package jcg.mini_dji_go_app;

import java.util.UUID;

public final class BluetoothConstants {

    public static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public final static String KEY = "BLcbyvFdRafKfUPOAQWCc5RveU8lpZ08";
    public final static Integer REQUEST_ENABLE_BT = 1;
    public final static String DEFAULT_DEVICE_NAME = "Dloiseau";

    public final static Integer FRAME_COMPRESS_QUALITY = 20; // [0-100] % - [ QUALITY*X ]
    public final static Integer FRAME_COMPRESS_SIZE = 2; // [0-???] - [ SIZE/X ] - [Multiple of 2]
}
