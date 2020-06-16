package dk.dtu.PassVault.Business.Crypto;

import android.util.Log;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    protected static final String ENCRYPTION_ALGORITHM = "AES";
    protected static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5PADDING";

    protected static Crypto instance = null;

    public static Crypto getInstance() {
        if(instance == null) {
            instance = new Crypto();
        }

        return instance;
    }

    protected Crypto() {}

    protected Cipher cipherDecryptInstance = null;
    protected Cipher cipherEncryptInstance = null;
    protected KeyGenerator keyGenInstance = null;
    protected Key key = null;

    public boolean init(boolean allowNoKey) {
        if(this.keyGenInstance == null) {
            try {
                this.keyGenInstance = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
                this.keyGenInstance.init(256);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }

        Log.i("Main", "keyGenInstance");

        if(this.key == null) {
            return allowNoKey;
        }

        Log.i("Main", "key");

        if(this.cipherDecryptInstance == null) {
            try {
                this.cipherDecryptInstance = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
                this.cipherDecryptInstance.init(Cipher.DECRYPT_MODE, this.key);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                return false;
            }
        }

        Log.i("Main", "cipherDecryptInstance");

        if(this.cipherEncryptInstance == null) {
            try {
                this.cipherEncryptInstance = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
                this.cipherEncryptInstance.init(Cipher.ENCRYPT_MODE, this.key);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
                e.printStackTrace();
                return false;
            }
        }

        Log.i("Main", "cipherEncryptInstance");

        return true;
    }

    public String getKey() {
        return Arrays.toString(this.key.getEncoded());
    }

    public void setKey(String masterPassword) {
        byte[] bytes = new byte[32];
        byte[] pwBytes = masterPassword.getBytes();
        for(int i = 0; i < 32; i++) {
            if(pwBytes.length > i) {
                bytes[i] = pwBytes[i];
            } else {
                bytes[i] = (byte) '0';
            }
        }

        this.key = new SecretKeySpec(bytes, ENCRYPTION_ALGORITHM);
    }

    public String encrypt(String password) throws BadPaddingException, IllegalBlockSizeException {
        return Arrays.toString(this.cipherEncryptInstance.doFinal(password.getBytes()));
    }

}
