package com.moe.icelauncher.util;
import android.os.Looper;
import com.moe.icelauncher.model.LauncherModel;

public class Preconditions
{
	public static void assertNotNull(Object o) {
        if (o == null) {
            throw new IllegalStateException();
        }
    }

    public static void assertWorkerThread() {
        if (!isSameLooper(LauncherModel.getWorkerLooper())) {
            throw new IllegalStateException();
        }
    }

    public static void assertUIThread() {
        if (!isSameLooper(Looper.getMainLooper())) {
            throw new IllegalStateException();
        }
    }

    public static void assertNonUiThread() {
        if (isSameLooper(Looper.getMainLooper())) {
            throw new IllegalStateException();
        }
    }

    private static boolean isSameLooper(Looper looper) {
        return Looper.myLooper() == looper;
    }
}
