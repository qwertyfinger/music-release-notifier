package com.qwertyfinger.musicreleasetracker.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import com.qwertyfinger.musicreleasetracker.R;
import com.qwertyfinger.musicreleasetracker.fragments.SettingsFragment;
import com.qwertyfinger.musicreleasetracker.util.Utils;

public class SignInDialog extends DialogPreference {

    private EditText username;
    private EditText password;
    private Context context;
    private String usernameSpace;
    private String passwordSpace;

    public SignInDialog(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        setDialogLayoutResource(R.layout.signin_dialog);
        setPositiveButtonText(context.getString(R.string.signin_dialog_ok_button));
        setNegativeButtonText(context.getString(R.string.signin_dialog_cancel_button));

        TypedArray array = this.context.obtainStyledAttributes(attrs, R.styleable.SignInDialog);
        String service = array.getString(R.styleable.SignInDialog_signInService);

        switch (service) {
            case "lastfm":
                usernameSpace = SettingsFragment.LAST_FM_USERNAME;
                passwordSpace = SettingsFragment.LAST_FM_PASSWORD;
                break;
            case "deezer":
                usernameSpace = SettingsFragment.DEEZER_USERNAME;
                passwordSpace = SettingsFragment.DEEZER_PASSWORD;
                break;
        }
        array.recycle();
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        username = (EditText) view.findViewById(R.id.username);
        password = (EditText) view.findViewById(R.id.password);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);

        username.setText(settings.getString(usernameSpace, ""));

        String passwordString = settings.getString(passwordSpace, "");
        if (!passwordString.equals(""))
            password.setText(Utils.decode(context, passwordString));
        else
            password.setText(passwordString);

        super.onBindDialogView(view);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = settings.edit();

            editor.putString(usernameSpace, username.getText().toString());
            editor.putString(passwordSpace, Utils.encode(context, password.getText().toString()));
            String debug = Utils.encode(context, password.getText().toString());
            String debus = Utils.decode(context, debug);
            editor.commit();
        }
    }
}
