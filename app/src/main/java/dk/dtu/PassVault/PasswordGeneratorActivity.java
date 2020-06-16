package dk.dtu.PassVault;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import static android.widget.Toast.LENGTH_LONG;


public class PasswordGeneratorActivity extends BaseActivity {

    private final static String TAG = "pgActivity";
    private boolean passwordGenerated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_generator);

        final PasswordGenerator passwordGenerator = new PasswordGenerator();
        final TextView generatedPassword = (TextView) findViewById(R.id.generatedPassword);
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        Button generateButton = (Button) findViewById(R.id.generateButton);
        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordGenerator.canGenerate()) {
                    generatedPassword.setText(passwordGenerator.generateNewPassword());
                    passwordGenerated = true;
                } else {
                    Toast.makeText(PasswordGeneratorActivity.this, "Please turn on some of the settings", LENGTH_LONG).show();
                }
            }
        });

        Button copyButton = (Button) findViewById(R.id.copyButton);
        copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passwordGenerated) {
                    ClipData clipData = ClipData.newPlainText("Generated password", generatedPassword.getText());
                    Toast.makeText(PasswordGeneratorActivity.this, "Generated password copied to clipboard", LENGTH_LONG).show();
                } else {
                    Toast.makeText(PasswordGeneratorActivity.this, "Please generate a password", LENGTH_LONG).show();
                }
            }
        });

        SeekBar lengthBar = (SeekBar) findViewById(R.id.lengthBar);
        lengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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
            }
        });


    }


}