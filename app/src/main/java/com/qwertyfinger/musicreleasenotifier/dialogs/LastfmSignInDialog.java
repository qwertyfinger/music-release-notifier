package com.qwertyfinger.musicreleasetracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.qwertyfinger.musicreleasetracker.App;
import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.events.lastfm.InvalidThresholdEvent;
import com.qwertyfinger.musicreleasetracker.events.lastfm.InvalidUsernameEvent;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.misc.Constants;
import com.qwertyfinger.musicreleasetracker.misc.Utils;

import java.lang.reflect.Field;

import de.greenrobot.event.EventBus;
import de.umass.lastfm.Caller;
import de.umass.lastfm.User;

public class LastfmSignInDialog extends DialogPreference {

    private EditText username;
    private TextView wrongData;
    private EditText threshold;

    public LastfmSignInDialog(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.lastfm_dialog);
    }

    @Override
    protected void onPrepareDialogBuilder (final AlertDialog.Builder builder) {
        DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (Utils.isConnected(getContext())) {
                    if (!username.getText().toString().equals("")) {
                        checkData();
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                            field.setAccessible(true);
                            field.set(dialog, false);
                        } catch (Exception e) {}
                    }
                    else {
                        SharedPreferences.Editor editor = getEditor();
                        editor.remove(SettingsFragment.LAST_FM);
                        editor.commit();
                    }
                }

                else
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
        builder.setMessage(R.string.lastfm_dialog_message);
        builder.setPositiveButton(R.string.ok_button, okListener);
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        EventBus.getDefault().register(this);

        username = (EditText) view.findViewById(R.id.username);
        wrongData = (TextView) view.findViewById(R.id.wrongData);
        threshold = (EditText) view.findViewById(R.id.threshold);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

        Integer thresholdInt = settings.getInt(SettingsFragment.LAST_FM_THRESHOLD, 0);
        if (thresholdInt == 0)
            threshold.setText("");
        else
            threshold.setText(thresholdInt.toString());
        username.setText(settings.getString(SettingsFragment.LAST_FM, ""));

        super.onBindDialogView(view);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        EventBus.getDefault().unregister(this);
    }

    private void checkData() {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());
        Caller.getInstance().setUserAgent(Constants.LASTFM_USER_AGENT);
        Caller.getInstance().setCache(null);

        App.getInstance().getJobManager().addJob(new Job(new Params(Constants.JOB_PRIORITY_CRITICAL)) {
            @Override
            public void onAdded() {

            }

            @Override
            public void onRun() throws Throwable {
                User user = User.getInfo(username.getText().toString(), Constants.LASTFM_API_KEY);

                int thresholdInt;
                if (threshold.getText().toString().equals(""))
                    thresholdInt = 0;
                else
                    thresholdInt = Integer.parseInt(threshold.getText().toString());

                Dialog dialog = getDialog();
                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                if (user != null) {
                    SharedPreferences.Editor editor = getEditor();
                    editor.putString(SettingsFragment.LAST_FM, username.getText().toString());
                    editor.putInt(SettingsFragment.LAST_FM_THRESHOLD, thresholdInt);
                    editor.commit();

                    field.setAccessible(true);
                    field.set(dialog, true);
                    dialog.dismiss();
                } else {
                    if (user == null) {
                        EventBus.getDefault().post(new InvalidUsernameEvent());
                        field.setAccessible(true);
                        field.set(dialog, true);
                    } else {
                        EventBus.getDefault().post(new InvalidThresholdEvent());
                        field.setAccessible(true);
                        field.set(dialog, true);
                    }
                }
            }

            @Override
            protected void onCancel() {

            }

            @Override
            protected boolean shouldReRunOnThrowable(Throwable throwable) {
                return false;
            }
        });
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(InvalidUsernameEvent event) {
        wrongData.setVisibility(View.VISIBLE);
        wrongData.setText(R.string.lastfm_wrong_username);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(InvalidThresholdEvent event) {
        wrongData.setVisibility(View.VISIBLE);
        wrongData.setText(R.string.lastfm_invalid_threshold);
    }
}
