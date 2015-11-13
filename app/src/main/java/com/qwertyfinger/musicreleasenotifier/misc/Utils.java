package com.qwertyfinger.musicreleasetracker.misc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
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
        makeToastNonUI(context, context.getString(R.string.external_storage_warning), Toast.LENGTH_SHORT);
    }

    public static void makeInternetToast(final Context context) {
        makeToastNonUI(context, context.getString(R.string.internet_needed_warning), Toast.LENGTH_SHORT);
    }

    public static void makeSyncToast(final Context context) {
        makeToastNonUI(context, "Wait until sync finishes", Toast.LENGTH_SHORT);
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        //TODO: delete in release version
        if (Build.FINGERPRINT.startsWith("generic"))
            return true;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        //TODO: delete in release version
        if (Build.FINGERPRINT.startsWith("generic"))
            return true;

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isConnected(Context context) {
        //TODO: delete in release version
        if (Build.FINGERPRINT.startsWith("generic"))
            return true;

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiConnected(Context context) {
        //TODO: delete in release version
        if (Build.FINGERPRINT.startsWith("generic"))
            return true;

        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static boolean isWifiOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.WIFI_ONLY, true);
    }

    public static boolean isSyncInProgress(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(SettingsFragment.SYNC_IN_PROGRESS,
                false);
    }

    public static int generateRandom(){
        return App.random.nextInt(30)+1;
    }

    /**
     * Deal with artists who have similar titles, whose best match in Musicbrainz
     * search represents not the most popular artist for this title.
     * @param artistTitle artist title
     * @return corrected mbid if set, else {@code null}
     */
    public static String correctArtistMbid(String artistTitle) {
        switch (artistTitle.toLowerCase()) {
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
