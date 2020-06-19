package dk.dtu.PassVault.Android.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.R;

public class LoginActivity extends BaseActivity {

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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_BUTTONS) {
            this.showApplicableButtons();
        }
    }
}