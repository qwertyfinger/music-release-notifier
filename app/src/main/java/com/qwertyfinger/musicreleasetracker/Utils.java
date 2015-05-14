package com.qwertyfinger.musicreleasetracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;

public class Utils {

    /**
     * Show toast for time, less than default
     * @param context activity's context
     * @param length {@code Toast.LENGH_SHORT} or {@code Toast.LENGTH_LONG}
     * @param handlerDuration needed duration of toast, less than default
     * @param text toast's message
     */
    public static void makeToast(Context context, int length, long handlerDuration, CharSequence text){
        final Toast toast = Toast.makeText(context, text, length);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, handlerDuration);
    }

    public static void makeToastNonUI(final Context context, final CharSequence text, final int length) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, text, length).show();
            }
        });
    }

    public static void makeExtStorToast(final Context context) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, context.getString(R.string.external_storage_warning), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void makeInternetToast(final Context context) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(new Runnable() {
            public void run() {
                Toast.makeText(context, context.getString(R.string.internet_needed_warning), Toast.LENGTH_SHORT).show();
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

    public static boolean isConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.WIFI_ONLY, true);
    }

    /**
     * Deal with artists who have similar titles, whose best match in Musicbrainz
     * search represents not the most popular artist for this title.
     * @param artistTitle artist title
     * @return corrected mbid if set, else {@code null}
     */
    public static String correctArtistMbid(String artistTitle) {
        String lowerCasedName = artistTitle.toLowerCase();
        switch (lowerCasedName) {
            case "muse":
                return "9c9f1380-2516-4fc9-a3e6-f9f61941d090";
            case "placebo":
                return "847e8284-8582-4b0e-9c26-b042a4f49e57";
            case "veto":
                return "96cf8760-18ee-443e-b950-4a219e55e443";
            default:
                return null;
        }
    }

    /**
     * Deal with case problem for months in header in Ukrainian and Russian languages.
     * @param ukOrRu {@code true} if locale is Ukrainian, {@code false} if Russian
     * @param month number of month, as specified by Java Calendar
     * @return correct name of month for header
     */
    public static String convertMonth(boolean ukOrRu, int month) {
        if (ukOrRu) {
            switch (month) {
                case 0:
                    return "ѳ����";
                case 1:
                    return "�����";
                case 2:
                    return "��������";
                case 3:
                    return "������";
                case 4:
                    return "�������";
                case 5:
                    return "�������";
                case 6:
                    return "������";
                case 7:
                    return "�������";
                case 8:
                    return "��������";
                case 9:
                    return "�������";
                case 10:
                    return "��������";
                case 11:
                    return "�������";
                default:
                    return "�� ���������";
            }
        } else {
            switch (month) {
                case 0:
                    return "������";
                case 1:
                    return "�������";
                case 2:
                    return "����";
                case 3:
                    return "������";
                case 4:
                    return "���";
                case 5:
                    return "����";
                case 6:
                    return "����";
                case 7:
                    return "������";
                case 8:
                    return "��������";
                case 9:
                    return "�������";
                case 10:
                    return "������";
                case 11:
                    return "�������";
                default:
                    return "�� ����������";
            }
        }
    }

//    encryption methods, unused for now
    /*public static void generateKey(Context context) throws NoSuchAlgorithmException {
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
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }*/
}
