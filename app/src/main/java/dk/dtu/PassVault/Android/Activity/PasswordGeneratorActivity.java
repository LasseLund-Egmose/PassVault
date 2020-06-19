package dk.dtu.PassVault.Android.Activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Business.Util.PasswordGenerator;
import dk.dtu.PassVault.Business.Enum.PasswordStrength;
import dk.dtu.PassVault.R;

public class PasswordGeneratorActivity extends BaseActivity {

    protected ClipboardManager clipboardManager;
    protected PasswordGenerator passwordGenerator;
    protected TextView generatedPassword;
    protected ProgressBar progressBar;
    protected TextView passwordStrength;
    protected TextView passwordLength;
    protected SeekBar lengthBar;

    protected boolean passwordGenerated = false;

    protected String getStringWithSpace(int id) {
        return " " + this.getResources().getString(id);
    }

    protected void setupButtons() {
        Button generateButton = (Button) findViewById(R.id.generateButton);
        generateButton.setOnClickListener(v -> {
            if (this.passwordGenerator.canGenerate()) {
                generatedPassword.setText(passwordGenerator.getNewPassword());
                passwordGenerated = true;
            } else {
                this.toastShort(R.string.turn_on_settings);
            }
        });

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(v -> {
            if (this.passwordGenerated) {
                Intent result = new Intent();
                result.setData(Uri.parse(generatedPassword.getText().toString()));

                this.setResult(RESULT_OK, result);
                this.toastShort(R.string.generated_successfully);
            }
            else {
                this.toastShort(R.string.no_password_generated);
            }

            finish();
        });

        Button copyButton = (Button) findViewById(R.id.copyButton);
        copyButton.setOnClickListener(v -> {
            if (this.passwordGenerated) {
                this.clipboardManager.setPrimaryClip(
                    ClipData.newPlainText("Generated password", generatedPassword.getText())
                );
            }

            this.toastShort(
                this.passwordGenerated
                    ? R.string.pass_copied_to_clipboard
                    : R.string.please_generate_password
            );
        });
    }

    protected void setupLengthBar() {
        this.lengthBar.setMax(passwordGenerator.getPASSWORD_LENGTH_MAX());
        this.lengthBar.setProgress(passwordGenerator.getLength());
        this.lengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                passwordGenerator.setLength(progress);

                String spacedLength = " " + progress;
                passwordLength.setText(spacedLength);

                updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
    }

    protected void setupSwitches() {
        Switch lowerCasesSwitch = (Switch) findViewById(R.id.lowercasesSwitch);
        lowerCasesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.passwordGenerator.setLowerCaseLetters(isChecked);
            this.updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
        });

        Switch upperCasesSwitch = (Switch) findViewById(R.id.uppercasesSwitch);
        upperCasesSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.passwordGenerator.setUpperCaseLetters(isChecked);
            this.updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
        });

        Switch numbersSwitch = (Switch) findViewById(R.id.numbersSwitch);
        numbersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.passwordGenerator.setNumbers(isChecked);
            this.updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
        });

        Switch specialSwitch = (Switch) findViewById(R.id.specialSwitch);
        specialSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.passwordGenerator.setSpecialChars(isChecked);
            this.updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);
        });
    }

    protected void updatePasswordStrength(TextView passwordStrength, PasswordGenerator passwordGenerator, ProgressBar progressBar) {
        PasswordStrength strength = passwordGenerator.getPasswordStrength();

        if (strength.equals(PasswordStrength.WEAK)) {
            passwordStrength.setText(this.getStringWithSpace(R.string.strength_weak));
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_red));
            progressBar.setProgress(33);
        } else if (strength.equals(PasswordStrength.STRONG)) {
            passwordStrength.setText(this.getStringWithSpace(R.string.strength_strong));
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));
            progressBar.setProgress(66);
        } else {
            passwordStrength.setText(this.getStringWithSpace(R.string.strength_very_strong));
            progressBar.setProgressDrawable(getDrawable(R.drawable.pb_drawable_green));
            progressBar.setProgress(100);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_generate);

        this.clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        this.passwordGenerator = new PasswordGenerator();
        this.generatedPassword = this.findViewById(R.id.generatedPassword);
        this.clipboardManager = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        this.progressBar = this.findViewById(R.id.strength_progressbar);
        this.passwordStrength = this.findViewById(R.id.passwordStrengthVar);
        this.passwordLength = this.findViewById(R.id.passwordLengthNum);
        this.lengthBar = this.findViewById(R.id.lengthBar);

        this.updatePasswordStrength(passwordStrength, passwordGenerator, progressBar);

        String passLength = " " + passwordGenerator.getLength();
        this.passwordLength.setText(passLength);

        this.setupLengthBar();
        this.setupSwitches();
        this.setupButtons();

    }


}