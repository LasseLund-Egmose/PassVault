package dk.dtu.PassVault.Business.Crypto;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.Credential;

public class Crypto {

    protected static final String ENCRYPTION_ALGORITHM = "AES";
    protected static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    protected static final String HASHING_ALGORITHM = "SHA-256";
    protected static final byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    protected static Crypto instance = null;

    public static Crypto getInstance() {
        if(instance == null) {
            instance = new Crypto();
        }

        return instance;
    }

    protected Crypto() {}



    public static abstract class CryptoResponse implements Runnable {

        protected Crypto crypto = null;
        protected byte[] encryptedData = null;
        protected String decryptedData = null;
        protected String hashedData = null;
        protected boolean isSuccessful = false;

        public void injectCrypto(Crypto crypto) {
            this.crypto = crypto;
        }

        public void injectDecryptedData(String decryptedData) {
            this.decryptedData = decryptedData;
        }

        public void injectEncryptedData(byte[] encryptedData) {
            this.encryptedData = encryptedData;
        }

        public void injectHashedData(String hashedData) {
            this.hashedData = hashedData;
        }

        public void injectSuccessful(boolean isSuccessful) {
            this.isSuccessful = isSuccessful;
        }
    }

    public static abstract class MasterPasswordValidationResponse implements Runnable {

        protected Crypto crypto = null;
        protected boolean isValid = false;

        public void injectCrypto(Crypto crypto) {
            this.crypto = crypto;
        }

        public void injectResult(boolean isValid) {
            this.isValid = isValid;
        }
    }

    protected static class MasterPasswordValidationTransaction extends Database.Transaction<Boolean> {

        protected MasterPasswordValidationResponse mPVR;
        protected String hashedPassword;
        protected boolean successfulHash;

        public MasterPasswordValidationTransaction(String hashedPassword, boolean successfulHash, MasterPasswordValidationResponse mPVR) {
            this.mPVR = mPVR;
            this.hashedPassword = hashedPassword;
            this.successfulHash = successfulHash;
        }

        @Override
        public Boolean doRequest(Database db) {
            Credential cred = db.getCredential();

            if(!this.successfulHash || cred == null) {
                return false;
            }

            return cred.match(hashedPassword);
        }

        @Override
        public void onResult(Boolean result) {
            mPVR.injectResult(result);
            mPVR.run();
        }
    }

    protected Cipher cipherDecryptInstance = null;
    protected Cipher cipherEncryptInstance = null;
    protected KeyGenerator keyGenInstance = null;
    protected Key key = null;
    protected MessageDigest SHA256Digester = null;

    public boolean init(boolean allowNoKey) {
        if(this.keyGenInstance == null) {
            try {
                this.keyGenInstance = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
                this.keyGenInstance.init(256);
            } catch (NoSuchAlgorithmException e) {
                return false;
            }
        }

        if(this.SHA256Digester == null) {
            try {
                this.SHA256Digester = MessageDigest.getInstance(HASHING_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return false;
            }
        }

        if(this.key == null) {
            return allowNoKey;
        }

        if(this.cipherDecryptInstance == null) {
            try {
                this.cipherDecryptInstance = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
                this.cipherDecryptInstance.init(Cipher.DECRYPT_MODE, this.key, new IvParameterSpec(IV));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
                return false;
            }
        }

        if(this.cipherEncryptInstance == null) {
            try {
                this.cipherEncryptInstance = Cipher.getInstance(ENCRYPTION_TRANSFORMATION);
                this.cipherEncryptInstance.init(Cipher.ENCRYPT_MODE, this.key, new IvParameterSpec(IV));
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                e.printStackTrace();
                return false;
            }
        }

        return true;
    }

    public String getKey() {
        return Arrays.toString(this.key.getEncoded());
    }

    // TODO: Cache
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
        this.init(false);
    }

    public void checkMasterPassword(Context context, String password, MasterPasswordValidationResponse mPVR) {
        mPVR.injectCrypto(this);

        this.hash(password, new CryptoResponse() {
            @Override
            public void run() {
                Database.dispatch(context, new MasterPasswordValidationTransaction(this.hashedData, this.isSuccessful, mPVR));
            }
        });
    }

    public void decrypt(byte[] encryptedBytes, CryptoResponse cr) {
        AsyncTask.execute(() -> {
            String decryptedData = null;
            boolean success = false;

            try {
                decryptedData = new String(this.cipherDecryptInstance.doFinal(encryptedBytes), StandardCharsets.UTF_8);
                success = true;
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            cr.injectCrypto(this);
            cr.injectDecryptedData(decryptedData);
            cr.injectSuccessful(success);
            cr.run();
        });
    }

    public void encrypt(String str, CryptoResponse cr) {
        AsyncTask.execute(() -> {
            byte[] encryptedData = null;
            boolean success = false;

            try {
                encryptedData = this.cipherEncryptInstance.doFinal(str.getBytes());
                success = true;
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
            }

            cr.injectCrypto(this);
            cr.injectEncryptedData(encryptedData);
            cr.injectSuccessful(success);
            cr.run();
        });
    }

    public void hash(String str, CryptoResponse cr) {
        AsyncTask.execute(() -> {
            cr.injectCrypto(this);
            cr.injectHashedData(new String(this.SHA256Digester.digest(str.getBytes()), StandardCharsets.UTF_8));
            cr.injectSuccessful(true);
            cr.run();
        });
    }

}
