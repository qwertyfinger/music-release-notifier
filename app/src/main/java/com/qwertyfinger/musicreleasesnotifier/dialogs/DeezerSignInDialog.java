package com.qwertyfinger.musicreleasesnotifier.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.Toast;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;
import com.qwertyfinger.musicreleasesnotifier.App;
import com.qwertyfinger.musicreleasesnotifier.R;
import com.qwertyfinger.musicreleasesnotifier.events.deezer.LoggedInDeezerEvent;
import com.qwertyfinger.musicreleasesnotifier.events.deezer.LoggedOutDeezerEvent;
import com.qwertyfinger.musicreleasesnotifier.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasesnotifier.misc.Utils;

import de.greenrobot.event.EventBus;

public class DeezerSignInDialog extends DialogPreference {

    private boolean signInFlag;

    public DeezerSignInDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder (final AlertDialog.Builder builder) {
        signInFlag = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(SettingsFragment.DEEZER, false);

        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Utils.isConnected(getContext())) {
                    startAuthorization();
                    dialog.dismiss();
                } else
                    Toast.makeText(getContext(), R.string.internet_needed_warning, Toast.LENGTH_SHORT).show();


            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        builder.setNegativeButton(R.string.cancel_button, cancelListener);

        if (signInFlag) {
            builder.setMessage(R.string.deezer_logout_message);
            builder.setPositiveButton(R.string.logout_button, okListener);

        }
        else {
            builder.setMessage(R.string.deezer_signin_message);
            builder.setPositiveButton(R.string.ok_button, okListener);
        }
    }

    private void startAuthorization() {
        final DeezerConnect deezerConnect = App.getInstance().getDeezerConnect();

        if (!signInFlag) {
            String[] permissions = new String[] { Permissions.BASIC_ACCESS };

            DialogListener listener = new DialogListener() {

                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, App.getInstance().getApplicationContext());

                    EventBus.getDefault().post(new LoggedInDeezerEvent());

                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(SettingsFragment.DEEZER, true);
                    editor.commit();
                }

                public void onCancel() {
                }

                public void onException(Exception e) {
                }
            };

            deezerConnect.authorize((Activity) getContext(), permissions, listener);
        }
        else {
            deezerConnect.logout(getContext());

            EventBus.getDefault().post(new LoggedOutDeezerEvent());

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(SettingsFragment.DEEZER, false);
            editor.commit();
        }
    }
}
