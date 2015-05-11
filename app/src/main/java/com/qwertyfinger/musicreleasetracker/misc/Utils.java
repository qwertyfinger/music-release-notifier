package com.qwertyfinger.musicreleasetracker.misc;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.widget.Toast;

import com.qwertyfinger.musicreleasetracker.R;

import org.apache.commons.codec.DecoderException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static org.apache.commons.codec.binary.Hex.decodeHex;
import static org.apache.commons.io.FileUtils.readFileToByteArray;

public class Utils {

    public static void makeToast(Context context, int toastDuration, long handlerDuration, CharSequence text){
        final Toast toast = Toast.makeText(context, text, toastDuration);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, handlerDuration);
    }

    public static void makeExtStorToast(final Context context) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, "You need to have available external storage", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static void generateKey(Context context) throws NoSuchAlgorithmException{
        SecureRandom secureRandom = new SecureRandom();
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256, secureRandom);
        SecretKey key = keyGenerator.generateKey();

        OutputStreamWriter outputStream;
        try {
            outputStream = new OutputStreamWriter(context.openFileOutput("spaceOdyssey", Context.MODE_PRIVATE));
            String hex = bytesToHex(key.getEncoded());
            outputStream.write(hex);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SecretKey loadKey(Context context) {
        byte[] encoded;
        String data = null;
        try {
            data = new String(readFileToByteArray(new File(context.getFilesDir(), "spaceOdyssey")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            encoded = decodeHex(data.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();
            return null;
        }

        return new SecretKeySpec(encoded, "AES");
    }

    public static String encode(Context context, String text) {
        byte[] encodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.ENCRYPT_MODE, loadKey(context));
            encodedBytes = c.doFinal(text.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(encodedBytes, Base64.DEFAULT);
    }

    public static String decode(Context context, String text) {
        byte[] decodedBytes = null;
        try {
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, loadKey(context));
            decodedBytes = c.doFinal(Base64.decode(text, Base64.DEFAULT));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String(decodedBytes);
    }

    final private static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     *  Deal with problem with cases in Ukrainian and Russian languages
     */
    public static String convertMonth(Context context, boolean ukOrRu, int month) {
        if (ukOrRu) {
            switch (month) {
                case 0:
                    return context.getString(R.string.january_uk);
                case 1:
                    return context.getString(R.string.february_uk);
                case 2:
                    return context.getString(R.string.march_uk);
                case 3:
                    return context.getString(R.string.april_uk);
                case 4:
                    return context.getString(R.string.may_uk);
                case 5:
                    return context.getString(R.string.june_uk);
                case 6:
                    return context.getString(R.string.july_uk);
                case 7:
                    return context.getString(R.string.august_uk);
                case 8:
                    return context.getString(R.string.september_uk);
                case 9:
                    return context.getString(R.string.october_uk);
                case 10:
                    return context.getString(R.string.november_uk);
                case 11:
                    return context.getString(R.string.december_uk);
                default:
                    return context.getString(R.string.tba_uk);
            }
        } else {
            switch (month) {
                case 0:
                    return context.getString(R.string.january_ru);
                case 1:
                    return context.getString(R.string.february_ru);
                case 2:
                    return context.getString(R.string.march_ru);
                case 3:
                    return context.getString(R.string.april_ru);
                case 4:
                    return context.getString(R.string.may_ru);
                case 5:
                    return context.getString(R.string.june_ru);
                case 6:
                    return context.getString(R.string.july_ru);
                case 7:
                    return context.getString(R.string.august_ru);
                case 8:
                    return context.getString(R.string.september_ru);
                case 9:
                    return context.getString(R.string.october_ru);
                case 10:
                    return context.getString(R.string.november_ru);
                case 11:
                    return context.getString(R.string.december_ru);
                default:
                    return context.getString(R.string.tba_ru);
            }
        }
    }
}
