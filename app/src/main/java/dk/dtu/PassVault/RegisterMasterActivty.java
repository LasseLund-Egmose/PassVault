package dk.dtu.PassVault;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.lang.ref.WeakReference;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;



public class RegisterMasterActivty extends BaseActivity {
    private static final String TAG = "Log_Pass";
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.i(TAG,"RegisterMaster created");
            setContentView(R.layout.activity_register_master);
            getSupportActionBar().hide();

            //Bare brugt til at illustrere hvordan strength indicator virker.
            passwordStrengthCheck();

            Button saveButton = (Button) findViewById(R.id.save_registration);
            saveButton.setOnClickListener(v -> {
                EditText password = (EditText) findViewById(R.id.reg_master_password_editText1);

                this.getCrypto().hash(password.getText().toString(), new Crypto.CryptoResponse() {
                    @Override
                    public void run() {
                        if(!this.isSuccessful) {
                            Toast.makeText(getApplicationContext(), "An error occurred!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        Database.dispatch(
                                getApplicationContext(),
                                new LoginActivity.SetupCredentialTransaction(
                                        new WeakReference<>(getApplicationContext()),
                                        this.hashedData
                                )
                        );
                    }
                });
            });
        }

//ADD LISTENER!!
        public void passwordStrengthCheck(){

            //changes text of strength indicator
            TextView tv = findViewById(R.id.password_strength_textView);
            tv.setText(getString(R.string.password_strength_indicator) + " Weak");


            //changes the stength indicator progressbar
            ProgressBar pb = (ProgressBar) findViewById(R.id.strength_progressbar);
            pb.setProgress(66);
            pb.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
        }
    }

