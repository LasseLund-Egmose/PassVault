package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
        signInButton.setOnClickListener(v -> {
            EditText password = (EditText) findViewById(R.id.password);
            this.getCrypto().setKey(password.getText().toString());
            Log.i("Main", "Master password: " + this.getCrypto().getKey());

            Intent intent = new Intent(getApplicationContext(),WalletActivity.class);
            startActivity(intent);
        });
    }
}