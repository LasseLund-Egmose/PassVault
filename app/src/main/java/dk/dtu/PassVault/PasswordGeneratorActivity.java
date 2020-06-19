package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;
import static dk.dtu.PassVault.R.layout.activity_password_generator;
import static dk.dtu.PassVault.R.layout.pass_generate;


public class PasswordGeneratorActivity extends BaseActivity {

    private final static String TAG = "pgActivity";
    private boolean passwordGenerated = false;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(pass_generate);
        getSupportActionBar().hide();

        final PasswordGenerator passwordGenerator = new PasswordGenerator();
        final TextView generatedPassword = (TextView) findViewById(R.id.generatedPassword);
        final ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.strength_progressbar);

        final TextView passwordStrength = (TextView) findViewById(R.id.passwordStrengthVar);
        updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);

        final TextView passwordLength = (TextView) findViewById(R.id.passwordLengthNum);
        passwordLength.setText(" " + String.valueOf(passwordGenerator.getLength()));

        Button generateButton = (Button) findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordGenerator.canGenerate()) {
                    generatedPassword.setText(passwordGenerator.getNewPassword());
                    passwordGenerated = true;
                } else {
                    Toast.makeText(PasswordGeneratorActivity.this, "Please turn on some of the settings", LENGTH_LONG).show();
                }
            }
        });

        SeekBar lengthBar = (SeekBar) findViewById(R.id.lengthBar);
        lengthBar.setMax(passwordGenerator.getPASSWORD_LENGTH_MAX());
        //lengthBar.setMin(passwordGenerator.getPASSWORD_LENGTH_MIN());
        lengthBar.setProgress(passwordGenerator.getLength());
        lengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordGenerator.setLength(progress);
                passwordLength.setText(" " + String.valueOf(progress));
                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Switch lowerCasesSwitch = (Switch) findViewById(R.id.lowercasesSwitch);
        lowerCasesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordGenerator.setLowerCaseLetters(true);
                } else {
                    passwordGenerator.setLowerCaseLetters(false);
                }
                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }
        });

        Switch upperCasesSwitch = (Switch) findViewById(R.id.uppercasesSwitch);
        upperCasesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordGenerator.setUpperCaseLetters(true);
                } else {
                    passwordGenerator.setUpperCaseLetters(false);
                }
                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }
        });

        Switch numbersSwitch = (Switch) findViewById(R.id.numbersSwitch);
        numbersSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordGenerator.setNumbers(true);
                } else {
                    passwordGenerator.setNumbers(false);
                }
                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }
        });


        Switch specialSwitch = (Switch) findViewById(R.id.specialSwitch);
        specialSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    passwordGenerator.setSpecialChars(true);
                } else {
                    passwordGenerator.setSpecialChars(false);
                }
                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }
        });


        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordGenerated) {
                    Intent result = new Intent();
                    result.setData(Uri.parse(generatedPassword.getText().toString()));
                    setResult(RESULT_OK, result);
                    Toast.makeText(PasswordGeneratorActivity.this, "Password generated successfully", LENGTH_LONG).show();
                    finish();
                }
                else {
                    Toast.makeText(PasswordGeneratorActivity.this, "No password generated", LENGTH_LONG).show();
                    finish();
                }
            }
        });

        Button copyButton = (Button) findViewById(R.id.copyButton);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordGenerated) {
                    ClipData clipData = ClipData.newPlainText("Generated password", generatedPassword.getText());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(PasswordGeneratorActivity.this, "Generated password copied to clipboard", LENGTH_LONG).show();
                } else {
                    Toast.makeText(PasswordGeneratorActivity.this, "Please generate a password", LENGTH_LONG).show();
                }
            }
        });

    }

    public void updatePasswordStrength(TextView passwordStrength, PasswordGenerator passwordGenerator, ProgressBar progressBar) {
        if (passwordGenerator.getPasswordStrength().equals(PasswordStrength.WEAK)) {
            passwordStrength.setText(" WEAK");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_red));
            progressBar.setProgress(33);
        } else if (passwordGenerator.getPasswordStrength().equals(PasswordStrength.STRONG)) {
            passwordStrength.setText(" STRONG");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
            progressBar.setProgress(66);
        } else {
            passwordStrength.setText(" VERY STRONG");
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_green));
            progressBar.setProgress(100);
        }
    }


}