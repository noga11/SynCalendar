package com.example.SynCalendar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
    
    public static Bitmap getBitmapFromDrawable(int drawableId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeResource(_context.getResources(), drawableId, options);
        return ensureSoftwareBitmap(bitmap);
    }

    public static Bitmap getBitmapFromEncodedString(String encodedString) {
        if (encodedString == null) return null;
        try {
            byte[] arr = Base64.decode(encodedString, Base64.DEFAULT);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.length, options);
            return ensureSoftwareBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getEncodedString(Bitmap bitmap) {
        if (bitmap == null) return null;
        bitmap = ensureSoftwareBitmap(bitmap);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, os);
        byte[] imageArr = os.toByteArray();
        return Base64.encodeToString(imageArr, Base64.DEFAULT);
    }

    public static URL stringToURL(String picLink) {
        try {
            return new URL(picLink);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap urlToBitmap(String url) {
        try {
            URL imageUrl = new URL(url);
            URLConnection urlConnection = imageUrl.openConnection();
            InputStream inputStream = urlConnection.getInputStream();
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
            return ensureSoftwareBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap uriToBitmap(Context context, Uri uri) {
        Bitmap bitmap = null;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                bitmap = ensureSoftwareBitmap(bitmap);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    private static Bitmap ensureSoftwareBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        // If the bitmap is already a software bitmap, return it as is
        if (!bitmap.getConfig().equals(Bitmap.Config.HARDWARE)) {
            return bitmap;
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
