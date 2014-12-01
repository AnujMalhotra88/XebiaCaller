package in.xebia.xebiacaller.util;

import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by anujmalhotra on 28/11/14.
 */
public class Functions {

    public static int convertDpToPixel(float dp, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (dp * (metrics.densityDpi / 160f));
    }

    public static byte[] getImage(String imageName) throws IOException {
        if (imageName != null && !imageName.equals("")) {
            File file = new File(Environment.getExternalStorageDirectory() + "/xebia/" + imageName);
            if (file.exists() && file.isFile()) {
                BufferedInputStream buf = null;
                try {
                    byte[] bytes = new byte[(int) file.length()];
                    buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    return bytes;
                } finally {
                    if (buf != null)
                        buf.close();
                }
            } else {
                return null;
            }
        } else
            return null;
    }
}
