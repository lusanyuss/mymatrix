package sample.tencent.matrix.hooks;


public class JNIObj {

    private static final String TAG = "Matrix.JNIObj";

    static {
        init();
    }

    private static void init() {
        try {
            System.loadLibrary("native-lib");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public native void reallocTest();

    public native void doMmap();

    public native static void mallocTest();

    public native static void threadTest();
}
