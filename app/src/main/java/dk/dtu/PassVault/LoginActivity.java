package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

public class LoginActivity extends BaseActivity {

    protected static class SetupCredentialTransaction extends Database.Transaction<Boolean> {

        protected String hashedPassword;

        SetupCredentialTransaction(String hashedPassword) {
            this.hashedPassword = hashedPassword;
        }

        @Override
        public Boolean doRequest(Database db) {
            Credential cred = new Credential(this.hashedPassword);
            db.setCredential(cred);

            return db.hasCredential();
        }

        @Override
        public void onResult(Boolean result) {
            // TODO: Display success
            Log.i("Main", "Success: " + result);
        }

    }

    // Allows activity to be started without a master password having been specified
    protected boolean allowNoKey() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();

        Button signInButton = findViewById(R.id.signInButton);
        Button registerButton = findViewById(R.id.registerButton);

        signInButton.setOnClickListener(v -> {
            EditText password = (EditText) findViewById(R.id.password);

            this.getCrypto().checkMasterPassword(
                getApplicationContext(),
                password.getText().toString(),
                new Crypto.MasterPasswordValidationResponse() {
                    @Override
                    public void run() {
                        Log.i("Main", "Valid: " + this.isValid);

                        if(this.isValid) {
                            // Tell crypto instance about master password
                            this.crypto.setKey(password.getText().toString());
                            Intent intent = new Intent(getApplicationContext(),WalletActivity.class);
                            startActivity(intent);

                        }

                        // TODO: Display error
                    }
                }
            );
        });

        registerButton.setOnClickListener(v -> {
            EditText password = (EditText) findViewById(R.id.password);

            this.getCrypto().hash(password.getText().toString(), new Crypto.CryptoResponse() {
                @Override
                public void run() {
                    if(!this.isSuccessful) {
                        // TODO: Display error
                        return;
                    }

                    Database.dispatch(getApplicationContext(), new SetupCredentialTransaction(this.hashedData));
                }
            });
        });
    }
}