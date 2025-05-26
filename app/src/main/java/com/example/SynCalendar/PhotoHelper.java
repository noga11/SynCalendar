package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class PhotoHelper {
    private static Context _context;
    
    public static void setContext(Context context) {
        _context = context;
    }

    private static Bitmap ensureSoftwareBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        // If the bitmap is already a software bitmap, return it as is
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!bitmap.getConfig().equals(Bitmap.Config.HARDWARE)) {
                return bitmap;
            }
        }

        // Convert hardware bitmap to software bitmap
        Bitmap softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        if (bitmap != softwareBitmap) {
            bitmap.recycle();
        }
        return softwareBitmap;
    }

    public static String bitmapToString(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        bitmap = ensureSoftwareBitmap(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    public static Bitmap stringToBitmap(String encodedString) {
        if (encodedString == null) return null;
        
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
