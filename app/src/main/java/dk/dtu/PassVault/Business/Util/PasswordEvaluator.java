package dk.dtu.PassVault.Business.Util;

import dk.dtu.PassVault.Business.Enum.PasswordStrength;

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

    private boolean hasLower;
    private boolean hasUpper;
    private boolean hasNumber;
    private boolean hasSpecial;

    private PasswordStrength passwordStrength;

    private String password = "";

    public PasswordEvaluator(){
    }

    public PasswordStrength getPasswordStrength(String password) {
        this.password=password;
        setPasswordStrength(password);
        return passwordStrength;
    }

    private void setPasswordStrength(String password) {
        int numOfDifferentChars = getNumOfDifferentChars();

        // Very strong password if length is greater than 15 and all types are selected
        if (numOfDifferentChars >= 4 && password.length() >= 15) {
            passwordStrength = PasswordStrength.VERY_STRONG;
        } else if (numOfDifferentChars >= 3 && password.length() >= 12) { // Strong password if length is greater 12 and a least 3 types are selected
            passwordStrength = PasswordStrength.STRONG;
        } else { // Weak password if length is
            passwordStrength = PasswordStrength.WEAK;
        }
    }


    private int getNumOfDifferentChars() {

        for (int i = 0; i < this.password.length(); i++) {
            if (!hasLower && LOWER_CASE_ASCII_LOW <= this.password.charAt(i) && this.password.charAt(i) <= LOWER_CASE_ASCII_HIGH) {
                hasLower = true;
            }
            if (!hasUpper && UPPER_CASE_ASCII_LOW <= this.password.charAt(i) && this.password.charAt(i) <= UPPER_CASE_ASCII_HIGH
            ) {
                hasUpper = true;
            }
            if (!hasNumber && NUMBERS_ASCII_LOW <= this.password.charAt(i) && this.password.charAt(i) <= NUMBERS_ASCII_HIGH) {
                hasNumber = true;
            }
            if (!hasSpecial && SPECIAL_ASCII_LOW <= this.password.charAt(i) && this.password.charAt(i) <= SPECIAL_ASCII_HIGH) {
                hasSpecial = true;
            }
        }

        int i = 0;
        if (hasLower) {
            i++;
        }
        if (hasUpper) {
            i++;
        }
        if (hasNumber) {
            i++;
        }
        if (hasSpecial) {
            i++;
        }
        return i;
    }
}
