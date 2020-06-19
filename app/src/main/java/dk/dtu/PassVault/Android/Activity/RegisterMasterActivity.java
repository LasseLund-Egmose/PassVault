package dk.dtu.PassVault.Android.Activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.strength_progressbar);
        EditText password = (EditText) findViewById(R.id.reg_master_password_editText1);
        EditText password2 = ((EditText) findViewById(R.id.reg_master_password_editText2));
        TextView passwordStrengthView = (TextView) findViewById(R.id.password_strength_textView);

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatePasswordStrength(passwordStrengthView, password, progressBar);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        Button saveButton = (Button) findViewById(R.id.save_registration);
        saveButton.setOnClickListener(v -> {
            String pw = password.getText().toString();
            String pw2 = password2.getText().toString();

            if (!pw.equals(pw2)) {
                this.toastShort(R.string.passwords_not_identical);
            }

            this.setMasterPassword(pw);

            this.setResult(RESULT_OK);
            this.finish();
        });
    }

    // TODO: Update this so there's no duplicate code
    public void updatePasswordStrength(TextView strengthView, EditText passwordEditText, ProgressBar progressBar) {
        PasswordEvaluator pwe = new PasswordEvaluator();
        PasswordStrength passwordStrength = pwe.getPasswordStrength(passwordEditText.getText().toString());

        if (passwordStrength.equals(PasswordStrength.WEAK)) {
            strengthView.setText(getString(R.string.password_strength_indicator) +" WEAK");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_red));
            progressBar.setProgress(33);
        } else if (passwordStrength.equals(PasswordStrength.STRONG)) {
            strengthView.setText(getString(R.string.password_strength_indicator)+" STRONG");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
            progressBar.setProgress(66);
        } else {
            strengthView.setText(getString(R.string.password_strength_indicator)+" VERY STRONG");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_green));
            progressBar.setProgress(100);
        }
    }
}


