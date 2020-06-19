package dk.dtu.PassVault.Business.Util;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import dk.dtu.PassVault.Business.Enum.PasswordStrength;

public class PasswordGenerator {
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

    private final int PASSWORD_LENGTH_MIN = 1;
    private final int PASSWORD_LENGTH_MAX = 32;

    private int length = 16;

    private boolean lowerCaseLetters = true;
    private boolean upperCaseLetters = true;
    private boolean numbers = true;
    private boolean specialChars = true;

    private String password = "";

    // Generates a new password based on input
    private void generateNewPassword() {
        if (!canGenerate()) {
            throw new IllegalArgumentException("The password must consist of at least one type of character");
        } else {
            StringBuilder password = new StringBuilder();
            for (int i = 0; i < this.length; i++) {
                password.append(chooseRandom());
            }
            this.password = password.toString();
        }
    }

    // Choose random from list
    private char chooseRandom() {
        ArrayList<Character> chars = makeRandomCharList();
        Random r = new Random();
        int result = r.nextInt(chars.size());
        return chars.get(result);
    }

    // Makes a random list of chars, based on input
    private ArrayList<Character> makeRandomCharList() {

        ArrayList<Character> chars = new ArrayList<>();

        if (lowerCaseLetters) {
            chars.add(getRandomBetweenTwoNum(LOWER_CASE_ASCII_LOW, LOWER_CASE_ASCII_HIGH));
        }
        if (upperCaseLetters) {
            chars.add(getRandomBetweenTwoNum(UPPER_CASE_ASCII_LOW, UPPER_CASE_ASCII_HIGH));
        }
        if (numbers) {
            chars.add(getRandomBetweenTwoNum(NUMBERS_ASCII_LOW, NUMBERS_ASCII_HIGH));
        }
        if (specialChars) {
            chars.add(getRandomBetweenTwoNum(SPECIAL_ASCII_LOW, SPECIAL_ASCII_HIGH));
        }
        return chars;
    }

    // Generate a random Ascii value between to values.
    private char getRandomBetweenTwoNum(int low, int high) {
        Random r = new Random();
        int result = r.nextInt(high - low) + low;
        return (char) result;
    }

    // Check that at least one type of char is true
    public boolean canGenerate() {
        return lowerCaseLetters || upperCaseLetters || numbers || specialChars;
    }

    private boolean passwordIsValid() {
        PasswordEvaluator pwe = new PasswordEvaluator();
        boolean[] diversity = pwe.evaluateCharacterDiversity(this.password);

        Log.i("Password Eval", Arrays.toString(diversity));

        return diversity[0] == lowerCaseLetters && diversity[1] == upperCaseLetters && diversity[2] == numbers && diversity[3] == specialChars;
    }

    // Getters and setters
    public String getNewPassword() {
        generateNewPassword();
        if (passwordIsValid()) {
            return this.password;
        }
        return getNewPassword();
    }

    public void setLength(int length) throws IllegalArgumentException {
        if (PASSWORD_LENGTH_MIN <= length && length <= PASSWORD_LENGTH_MAX) {
            this.length = length;
        } else {
            throw new IllegalArgumentException("Length must be between " +
                    PASSWORD_LENGTH_MIN + " and " + PASSWORD_LENGTH_MAX + " character");
        }
    }

    public int getLength() {
        return length;
    }

    public void setLowerCaseLetters(boolean lowerCaseLetters) {
        this.lowerCaseLetters = lowerCaseLetters;
    }

    public void setUpperCaseLetters(boolean upperCaseLetters) {
        this.upperCaseLetters = upperCaseLetters;
    }

    public void setNumbers(boolean numbers) {
        this.numbers = numbers;
    }

    public void setSpecialChars(boolean specialChars) {
        this.specialChars = specialChars;
    }

    public boolean isLowerCaseLetters() {
        return lowerCaseLetters;
    }

    public boolean isUpperCaseLetters() {
        return upperCaseLetters;
    }

    public boolean isNumbers() {
        return numbers;
    }

    public boolean isSpecialChars() {
        return specialChars;
    }

    public int getPASSWORD_LENGTH_MIN() {
        return PASSWORD_LENGTH_MIN;
    }

    public int getPASSWORD_LENGTH_MAX() {
        return PASSWORD_LENGTH_MAX;
    }
}
