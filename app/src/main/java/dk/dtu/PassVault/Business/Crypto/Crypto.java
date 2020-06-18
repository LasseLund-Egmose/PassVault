package dk.dtu.PassVault.Business.Crypto;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;

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

    protected static class DecryptionHandler extends AsyncTask<Void, Void, CryptoResponse> {

        protected Cipher cipherDecryptInstance;
        protected CryptoResponse cr;
        protected byte[] input;

        public DecryptionHandler(Cipher cipherDecryptInstance, CryptoResponse cr, byte[] input) {
            this.cipherDecryptInstance = cipherDecryptInstance;
            this.cr = cr;
            this.input = input;
        }

        @Override
        protected CryptoResponse doInBackground(Void... voids) {
            String decryptedData = null;
            boolean success = false;

            try {
                decryptedData = new String(this.cipherDecryptInstance.doFinal(this.input), StandardCharsets.UTF_8);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.cr.injectDecryptedData(decryptedData);
            this.cr.injectSuccessful(success);

            return this.cr;
        }

        @Override
        protected void onPostExecute(CryptoResponse cryptoResponse) {
            this.cr.run();
        }
    }

    protected static class EncryptionHandler extends AsyncTask<Void, Void, CryptoResponse> {

        protected Cipher cipherEncryptInstance;
        protected CryptoResponse cr;
        protected String input;

        public EncryptionHandler(Cipher cipherEncryptInstance, CryptoResponse cr, String input) {
            this.cipherEncryptInstance = cipherEncryptInstance;
            this.cr = cr;
            this.input = input;
        }

        @Override
        protected CryptoResponse doInBackground(Void... voids) {
            byte[] encryptedData = null;
            boolean success = false;

            try {
                encryptedData = this.cipherEncryptInstance.doFinal(this.input.getBytes());
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.cr.injectEncryptedData(encryptedData);
            this.cr.injectSuccessful(success);

            return this.cr;
        }

        @Override
        protected void onPostExecute(CryptoResponse cryptoResponse) {
            this.cr.run();
        }
    }

    protected static class HashingHandler extends AsyncTask<Void, Void, CryptoResponse> {

        protected CryptoResponse cr;
        protected MessageDigest SHA256Digester;
        protected byte[] input;

        public HashingHandler(MessageDigest SHA256Digester, CryptoResponse cr, byte[] input) {
            this.SHA256Digester = SHA256Digester;
            this.cr = cr;
            this.input = input;
        }

        @Override
        protected CryptoResponse doInBackground(Void... voids) {
            String hashedData = null;
            boolean success = false;

            try {
                hashedData = new String(this.SHA256Digester.digest(this.input), StandardCharsets.UTF_8);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.cr.injectHashedData(hashedData);
            this.cr.injectSuccessful(success);

            return this.cr;
        }

        @Override
        protected void onPostExecute(CryptoResponse cryptoResponse) {
            this.cr.run();
        }
    }

    protected static final String ENCRYPTION_ALGORITHM = "AES";
    protected static final String ENCRYPTION_TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    protected static final String HASHING_ALGORITHM = "SHA-256";
    protected static final byte[] IV = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    protected static Crypto instance = null;

    public static Crypto getInstance() {
        long timestamp = (new Date()).getTime();

        // Check if instance is null or has expired - expires after 5 minutes
        if(instance == null || timestamp - instance.getCreatedAt() > 30000) {
            instance = new Crypto();
        }

        instance.renew();

        return instance;
    }

    protected Cipher cipherDecryptInstance = null;
    protected Cipher cipherEncryptInstance = null;
    protected long createdAt;
    protected KeyGenerator keyGenInstance = null;
    protected Key key = null;
    protected MessageDigest SHA256Digester = null;

    public boolean hasKey() {
        return this.key != null;
    }

    public long getCreatedAt() {
        return this.createdAt;
    }

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
        cr.injectCrypto(this);
        (new DecryptionHandler(this.cipherDecryptInstance, cr, encryptedBytes)).execute();
    }

    public void encrypt(String str, CryptoResponse cr) {
        cr.injectCrypto(this);
        (new EncryptionHandler(this.cipherEncryptInstance, cr, str)).execute();
    }

    public void hash(String str, CryptoResponse cr) {
        cr.injectCrypto(this);
        (new HashingHandler(this.SHA256Digester, cr, str.getBytes())).execute();
    }

    public void renew() {
        this.createdAt = (new Date()).getTime();
    }

    protected Crypto() {
        this.renew();
    }

}
