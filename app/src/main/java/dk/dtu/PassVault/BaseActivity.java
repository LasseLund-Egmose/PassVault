package dk.dtu.PassVault;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import dk.dtu.PassVault.Business.Crypto.Crypto;

public class BaseActivity extends AppCompatActivity {

    protected boolean allowNoKey() {
        return false;
    }

    protected Crypto getCrypto() {
        return Crypto.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!this.getCrypto().init(this.allowNoKey())) {
            Toast.makeText(
                    getApplicationContext(),
                    R.string.cipher_setup_error,
                    Toast.LENGTH_LONG
            ).show();

            finishAndRemoveTask();
        }
    }

}
