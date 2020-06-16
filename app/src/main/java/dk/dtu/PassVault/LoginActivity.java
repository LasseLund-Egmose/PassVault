package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.util.Arrays;

import dk.dtu.PassVault.Business.Crypto.Crypto;

public class LoginActivity extends BaseActivity {

    // Allows activity to be started without a master password having been specified
    protected boolean allowNoKey() {
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signInButton = findViewById(R.id.signInButton);
        Button registerButton = findViewById(R.id.signInButton);

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
    }
}