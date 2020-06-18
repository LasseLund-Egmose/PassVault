package dk.dtu.PassVault;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;



public class RegisterMasterActivty extends BaseActivity {
    private static final String TAG = "Log_Pass";

    protected boolean allowNoKey() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"RegisterMaster created");
        setContentView(R.layout.activity_register_master_new);
        getSupportActionBar().hide();

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
                updatePasswordStrength(passwordStrengthView,password,progressBar);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });


        Button saveButton = (Button) findViewById(R.id.save_registration);
        saveButton.setOnClickListener(v -> {

            if(password.getText().toString().equals(password2.getText().toString())){

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

                setResult(RESULT_OK);
                finish();
            }else {
                Toast.makeText(getApplicationContext(),"Master password is not identical",Toast.LENGTH_LONG).show();
            }

        });
    }


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


