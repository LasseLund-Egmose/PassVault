package dk.dtu.PassVault.Business.Util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import dk.dtu.PassVault.Business.Enum.PasswordStrength;
import dk.dtu.PassVault.R;

public class PasswordEvaluator {

    /*
     * Ascii values for lower case letters a-z is 97-122
     * Ascii values for upper case letters a-z is 65-90
     * Ascii values for numbers 0-9 is 48-57
     * Ascii values for specialChars ! " # $ % & ' ( ) * + , - . / is 33-47
     * */

    private final int LOWER_CASE_ASCII_LOW = 97;
    private final int LOWER_CASE_ASCII_HIGH = 122;
    private final int UPPER_CASE_ASCII_LOW = 65;
    private final int UPPER_CASE_ASCII_HIGH = 90;
    private final int NUMBERS_ASCII_LOW = 48;
    private final int NUMBERS_ASCII_HIGH = 57;
    private final int SPECIAL_ASCII_LOW = 33;
    private final int SPECIAL_ASCII_HIGH = 47;

    private PasswordStrength passwordStrength;

    protected void evalPasswordStrength(int passwordScore) {
        // Very strong password if length is greater than 15 and all types are selected
        if (passwordScore > 35) {
            passwordStrength = PasswordStrength.VERY_STRONG;
        } else if (passwordScore > 28) { // Strong password if length is greater 12 and a least 3 types are selected
            passwordStrength = PasswordStrength.STRONG;
        } else { // Weak password if length is
            passwordStrength = PasswordStrength.WEAK;
        }
    }

    public void updatePasswordStrength(TextView strengthView, EditText passwordEditText, ProgressBar progressBar, Context context, String baseMsg) {
        String password = passwordEditText.getText().toString();

        int passwordScore = this.passwordScore(password);
        this.evalPasswordStrength(passwordScore);

        strengthView.setText(baseMsg);
        if(this.passwordStrength.equals(PasswordStrength.WEAK)) {
            strengthView.append(" "+context.getString(R.string.strength_weak));
        }else if(this.passwordStrength.equals(PasswordStrength.STRONG)){
            strengthView.append(" "+context.getString(R.string.strength_strong));
        }else if (this.passwordStrength.equals(PasswordStrength.VERY_STRONG)){
            strengthView.append(" "+context.getString(R.string.strength_very_strong));
        }

        this.updateProgressBar(progressBar, context);
    }

    public void updatePasswordStrength(TextView strengthView, ProgressBar progressBar, Context context, PasswordGenerator passwordGenerator) {
        int passwordScore = this.passwordScore(
                passwordGenerator.getLength(),
                passwordGenerator.isLowerCaseLetters(),
                passwordGenerator.isUpperCaseLetters(),
                passwordGenerator.isNumbers(),
                passwordGenerator.isSpecialChars()
        );
        this.evalPasswordStrength(passwordScore);

        if(this.passwordStrength.equals(PasswordStrength.WEAK)) {
            strengthView.append(" "+context.getString(R.string.strength_weak));
        }else if(this.passwordStrength.equals(PasswordStrength.STRONG)){
            strengthView.append(" "+context.getString(R.string.strength_strong));
        }else if (this.passwordStrength.equals(PasswordStrength.VERY_STRONG)){
            strengthView.append(" "+context.getString(R.string.strength_very_strong));
        }

        this.updateProgressBar(progressBar, context);
    }

    protected void updateProgressBar(ProgressBar progressBar, Context context) {
        int progress;
        Drawable progressBg;

        if (this.passwordStrength.equals(PasswordStrength.WEAK)) {
            progressBg = context.getDrawable(R.drawable.pb_drawable_red);
            progress = 33;
        } else if (this.passwordStrength.equals(PasswordStrength.STRONG)) {
            progressBg = context.getDrawable(R.drawable.pb_drawable_yellow);
            progress = 66;
        } else {
            progressBg = context.getDrawable(R.drawable.pb_drawable_green);
            progress = 100;
        }

        progressBar.setProgress(progress);
        progressBar.setProgressDrawable(progressBg);
    }

    public int passwordScore(String password) {
        boolean[] diversity = this.evaluateCharacterDiversity(password);

        return passwordScore(password.length(), diversity[0], diversity[1], diversity[2], diversity[3]);
    }

    public int passwordScore(int passwordLength, boolean containsLower, boolean containsUpper, boolean containsNums, boolean containsSpecial) {
        int score = passwordLength;

        if (containsLower) { score += 5; }
        if (containsUpper) { score += 5; }
        if (containsNums) { score += 5; }
        if (containsSpecial) { score += 5; }

        return score;
    }

    public boolean[] evaluateCharacterDiversity(String password) {
        boolean hasLower = false, hasUpper = false, hasNumber = false, hasSpecial = false;

        for (int i = 0; i < password.length(); i++) {
            if (!hasLower && LOWER_CASE_ASCII_LOW <= password.charAt(i) && password.charAt(i) <= LOWER_CASE_ASCII_HIGH) {
                hasLower = true;
            }
            if (!hasUpper && UPPER_CASE_ASCII_LOW <= password.charAt(i) && password.charAt(i) <= UPPER_CASE_ASCII_HIGH
            ) {
                hasUpper = true;
            }
            if (!hasNumber && NUMBERS_ASCII_LOW <= password.charAt(i) && password.charAt(i) <= NUMBERS_ASCII_HIGH) {
                hasNumber = true;
            }
            if (!hasSpecial && SPECIAL_ASCII_LOW <= password.charAt(i) && password.charAt(i) <= SPECIAL_ASCII_HIGH) {
                hasSpecial = true;
            }
        }

        return new boolean[]{ hasLower, hasUpper, hasNumber, hasSpecial };
    }
}
