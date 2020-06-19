package dk.dtu.PassVault.Android.Activity.Abstract;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import dk.dtu.PassVault.Android.Activity.LoginActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected boolean allowNoKey() {
        return false;
    }

    protected Crypto getCrypto() {
        return Crypto.getInstance();
    }

    protected void toastLong(int message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void toastShort(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        ActionBar ab = getSupportActionBar();
        if(ab != null) {
            ab.hide();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Crypto crypto = this.getCrypto();
        boolean allowNoKey = this.allowNoKey();

        if(!allowNoKey && !crypto.hasKey()) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.needs_relogin,
                    Toast.LENGTH_LONG
            ).show();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);

            finishAndRemoveTask();

            return;
        }

        if(!crypto.init(allowNoKey)) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.cipher_setup_error,
                    Toast.LENGTH_LONG
            ).show();

            finishAndRemoveTask();
        }
    }

}
