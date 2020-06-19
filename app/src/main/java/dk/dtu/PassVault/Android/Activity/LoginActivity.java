package dk.dtu.PassVault.Android.Activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.R;

public class LoginActivity extends BaseActivity {

    protected static final String SETTING_HAS_SHOWN_WELCOME_DIALOG = "hasShownWelcomeDialog";
    protected static int REQUEST_CODE_BUTTONS = 2;

    protected static class ShowApplicableButtonsTransaction extends Database.Transaction<Boolean> {

        protected WeakReference<LoginActivity> ref;

        public ShowApplicableButtonsTransaction(WeakReference<LoginActivity> ref) {
            this.ref = ref;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.hasCredential();
        }

        @Override
        public void onResult(Boolean result) {
            LoginActivity activity = this.ref.get();

            if (activity == null) {
                return;
            }

            activity.findViewById(R.id.sign_in_btn).setEnabled(result);
            activity.findViewById(R.id.register_btn).setEnabled(!result);
        }
    }

    protected static class ShowWelcomeDialogIfRelevantTransaction extends Database.Transaction<Boolean> {

        protected WeakReference<LoginActivity> ref;

        public ShowWelcomeDialogIfRelevantTransaction(WeakReference<LoginActivity> ref) {
            this.ref = ref;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.getSetting(SETTING_HAS_SHOWN_WELCOME_DIALOG) == null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onResult(Boolean result) {
            LoginActivity activity = this.ref.get();

            if (activity == null || !result) return;

            new AlertDialog.Builder(activity)
                .setTitle(R.string.welcome_dialog_title)
                .setMessage(R.string.welcome_dialog_content)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setOnDismissListener(dialog -> {
                    Database.dispatch(activity, new UpdateWelcomeDialogSettingTransaction(true));
                })
                .setIcon(R.drawable.logo_icon)
                .show();
        }
    }

    protected static class UpdateWelcomeDialogSettingTransaction extends Database.Transaction<Void> {

        protected boolean hasShown;

        public UpdateWelcomeDialogSettingTransaction(boolean hasShown) {
            this.hasShown = hasShown;
        }

        @Override
        public Void doRequest(Database db) {
            db.setSetting(SETTING_HAS_SHOWN_WELCOME_DIALOG, String.valueOf(this.hasShown));
            return null;
        }

        @Override
        public void onResult(Void result) {
            // Do nothing
        }
    }

    protected boolean allowNoKey() {
        return true;
    }

    protected void showApplicableButtons() {
        ShowApplicableButtonsTransaction transaction =
            new ShowApplicableButtonsTransaction(new WeakReference<>(this));

        Database.dispatch(getApplicationContext(), transaction);
    }

    protected void validateMasterPassword(String password) {
        this.getCrypto().checkMasterPassword(getApplicationContext(), password,
            new Crypto.MasterPasswordValidationResponse() {
                @Override
                public void run() {
                    if (this.isValid) {
                        this.crypto.setKey(password);

                        // Go to Vault
                        startActivity(new Intent(getApplicationContext(), VaultActivity.class));
                        finish();
                    } else {
                        toastShort(R.string.wrong_password_or_something_wrong);
                    }
                }
            }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signInButton = findViewById(R.id.sign_in_btn);
        Button registerButton = findViewById(R.id.register_btn);

        signInButton.setOnClickListener(v -> {
            final EditText password = (EditText) findViewById(R.id.password);
            this.validateMasterPassword(password.getText().toString());
        });

        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterMasterActivity.class);
            startActivityForResult(intent, REQUEST_CODE_BUTTONS);
        });

        this.showApplicableButtons();

        Database.dispatch(
            this,
            new ShowWelcomeDialogIfRelevantTransaction(new WeakReference<>(this))
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_BUTTONS) {
            this.showApplicableButtons();
        }
    }
}