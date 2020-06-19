package dk.dtu.PassVault.Android.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;
import dk.dtu.PassVault.Business.Util.PasswordEvaluator;
import dk.dtu.PassVault.Business.Enum.PasswordStrength;
import dk.dtu.PassVault.R;

public class RegisterMasterActivity extends BaseActivity {

    protected static class SetupCredentialTransaction extends Database.Transaction<Boolean> {
        protected WeakReference<RegisterMasterActivity> ref;
        protected String hashedPassword;

        SetupCredentialTransaction(WeakReference<RegisterMasterActivity> ref, String hashedPassword) {
            this.hashedPassword = hashedPassword;
            this.ref = ref;
        }

        @Override
        public Boolean doRequest(Database db) {
            Credential cred = new Credential(this.hashedPassword);
            db.setCredential(cred);

            return db.hasCredential();
        }

        @Override
        public void onResult(Boolean result) {
            RegisterMasterActivity activity = this.ref.get();
            if (activity == null) return;

            activity.toastShort(
                result ? R.string.master_password_created : R.string.something_went_wrong
            );
        }

    }

    protected boolean allowNoKey() {
        return true;
    }

    protected void setMasterPassword(String password) {
        this.getCrypto().hash(password, new Crypto.CryptoResponse() {
            @Override
            public void run() {
                if (!this.isSuccessful) {
                    toastShort(R.string.error_occurred);
                    return;
                }

                Database.dispatch(
                    getApplicationContext(),
                    new SetupCredentialTransaction(
                        new WeakReference<>(RegisterMasterActivity.this),
                        this.hashedData
                    )
                );
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_register_master_new);
        PasswordEvaluator passwordEvaluator = new PasswordEvaluator();
        final String TAG = "Strength_bar";

        final ProgressBar progressBar = findViewById(R.id.strength_progressbar);
        EditText password = findViewById(R.id.reg_master_password_editText1);
        EditText password2 = findViewById(R.id.reg_master_password_editText2);
        TextView passwordStrengthView = findViewById(R.id.password_strength_textView);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    passwordEvaluator.updatePasswordStrength(
                    passwordStrengthView,
                    password,
                    progressBar,
                    getApplicationContext(),
                    getString(R.string.password_strength_indicator));
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        Button saveButton = findViewById(R.id.save_registration);
        saveButton.setOnClickListener(v -> {
            String pw = password.getText().toString();
            String pw2 = password2.getText().toString();

            if (!pw.equals(pw2)) {
                this.toastShort(R.string.passwords_not_identical);
                return;
            }

            if (pw.length() < 8) {
                this.toastShort(R.string.master_password_too_short);
                return;
            }

            this.setMasterPassword(pw);

            this.setResult(RESULT_OK);
            this.finish();
        });
    }
}


