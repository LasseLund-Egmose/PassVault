package dk.dtu.PassVault.Business.Service;

import android.app.assist.AssistStructure;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveRequest;
import android.util.Log;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.dtu.PassVault.AutoFillDialogActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillService extends android.service.autofill.AutofillService {

    public abstract static class AutoFillCommunicator extends BroadcastReceiver {

        public static final String ACTION_RECEIVED_MASTER_PASSWORD = "dk.dtu.PassVault.RECEIVED_MASTER_PASSWORD";
        public static final String ACTION_MASTER_PASSWORD_VALIDATION_RESPONSE = "dk.dtu.PassVault.MASTER_PASSWORD_VALIDATION_RESPONSE";

    }

    protected static class ValidateMasterPasswordAndDecryptVaultItem extends Database.Transaction<Boolean> {

        protected FillCallback callback;
        protected Crypto crypto;
        protected String hashedMasterPassword;
        protected String packageName;
        protected ArrayList<AutofillId> passwordFields;
        protected VaultItem vaultItem;

        public ValidateMasterPasswordAndDecryptVaultItem(
                FillCallback callback,
                Crypto crypto,
                String hashedMasterPassword,
                String packageName,
                ArrayList<AutofillId> passwordFields,
                VaultItem vaultItem
        ) {
            this.callback = callback;
            this.crypto = crypto;
            this.hashedMasterPassword = hashedMasterPassword;
            this.packageName = packageName;
            this.passwordFields = passwordFields;
            this.vaultItem = vaultItem;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.getCredential().match(this.hashedMasterPassword);
        }

        protected void buildAutoFillResponse(String decryptedPassword) {
            RemoteViews passwordPresentation = new RemoteViews(packageName, android.R.layout.simple_list_item_1);
            passwordPresentation.setTextViewText(android.R.id.text1, "PassVault password");

            AutofillId passwordID = passwordFields.get(0);

            FillResponse fillResponse = new FillResponse.Builder()
                .addDataset(new Dataset.Builder()
                    .setValue(passwordID,
                        AutofillValue.forText(decryptedPassword), passwordPresentation)
                    .build())
                .build();

            if(callback != null) {
                callback.onSuccess(fillResponse); // Send response
            } else {
                Log.i("Autofill", "Callback is null");
            }
        }

        @Override
        public void onResult(Boolean result) {
            // TODO: Send validation response broadcast to dialog receiver

            if(!result) {
                return;
            }

            Log.i("Autofill", "Encrypted password: " + Arrays.toString(this.vaultItem.password));

            this.crypto.decrypt(this.vaultItem.password, new Crypto.CryptoResponse() {
                @Override
                public void run() {
                    if(!this.isSuccessful) {
                        return;
                    }

                    Log.i("Autofill", "Decrypted password: " + this.decryptedData);
                    Log.i("Autofill", "Password fields: " + passwordFields);
                    Log.i("Autofill", "Callback: " + callback);

                    buildAutoFillResponse(this.decryptedData);
                }
            });
        }
    }

    protected static class GetVaultItemByURI extends Database.Transaction<VaultItem> {

        protected WeakReference<AutoFillService> ref;
        protected FillCallback callback;
        protected String URI;

        public GetVaultItemByURI(WeakReference<AutoFillService> ref, FillCallback callback, String URI) {
            this.callback = callback;
            this.ref = ref;
            this.URI = URI;
        }

        @Override
        public VaultItem doRequest(Database db) {
            return db.getVaultItemByURI(this.URI);
        }

        @Override
        public void onResult(VaultItem result) {
            AutoFillService service = this.ref.get();
            if(result != null && service != null) {
                service.setupPrompt(this.callback, result);
            }
        }
    }

    protected boolean hasReceivedRequest = false;
    protected AutoFillCommunicator receiver = null;
    protected ArrayList<AutofillId> passwordFields = new ArrayList<>();

    protected void setupPrompt(FillCallback callback, VaultItem item) {
        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
            this.receiver = null;
        }

        String selfPkgName = this.getPackageName();

        this.receiver = new AutoFillCommunicator() {

            protected void dispatchVaultItemDecryption(Crypto crypto, String hashedMasterPassword) {
                ValidateMasterPasswordAndDecryptVaultItem transaction = new ValidateMasterPasswordAndDecryptVaultItem(
                        callback, crypto, hashedMasterPassword, selfPkgName, passwordFields, item
                );

                Database.dispatch(getApplicationContext(), transaction);
            }

            protected void hashPassword(Crypto crypto, String password) {
                crypto.hash(password, new Crypto.CryptoResponse() {
                    @Override
                    public void run() {
                        if (!this.isSuccessful) {
                            return;
                        }

                        Log.i("Autofill", "Hashed password: " + this.hashedData);
                        Log.i("Autofill", "WeakRefCallback: " + callback);

                        dispatchVaultItemDecryption(this.crypto, this.hashedData);
                    }
                });
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                if(!AutoFillCommunicator.ACTION_RECEIVED_MASTER_PASSWORD.equals(intent.getAction())) {
                    return;
                }

                Log.i("Autofill", "Received broadcast" + intent.getExtras());

                Bundle extras = intent.getExtras();
                if(extras == null) {
                    return;
                }

                String password = extras.getString("password");
                if(password == null) {
                    return;
                }

                Crypto crypto = Crypto.getInstance();

                // A new instance is initialized as we cannot expect any old instances to have been persisted
                crypto.setKey(password);
                crypto.init(false);

                this.hashPassword(crypto, password);
            }
        };

        registerReceiver(this.receiver, new IntentFilter(AutoFillCommunicator.ACTION_RECEIVED_MASTER_PASSWORD));

        this.openDialog();
    }

    protected void openDialog() {
        Context context = this.getApplicationContext();
        Intent intent = new Intent(context, AutoFillDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    protected AutofillId identifyPasswordField(AssistStructure.ViewNode root) {
        if(root.getClassName() != null && root.getClassName().equals("android.widget.EditText")) {
            String combinedFields = root.getText().toString() + root.getHint();

            if(combinedFields.toLowerCase().contains("password")) {
                return root.getAutofillId();
            }
        }

        for(int i = 0; i < root.getChildCount(); i++) {
            AutofillId fieldID = identifyPasswordField(root.getChildAt(i));
            if(fieldID != null) {
                return fieldID;
            }
        }

        return null;
    }

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal signal, @NonNull FillCallback callback) {
        Log.i("Autofill", "Autofill request!");

        if(this.hasReceivedRequest) {
            return;
        }

        this.hasReceivedRequest = true;

        List<FillContext> fillContexts = request.getFillContexts();
        AssistStructure structure = fillContexts.get(fillContexts.size() - 1).getStructure();

        this.passwordFields.clear();
        for(int i = 0; i < structure.getWindowNodeCount(); i++) {
            AssistStructure.WindowNode node = structure.getWindowNodeAt(i);
            Log.i("Autofill", "ID: " + node.getDisplayId() + ", Name: " + node.getTitle());

            AutofillId fieldID = this.identifyPasswordField(node.getRootViewNode());
            if(fieldID != null) {
                this.passwordFields.add(fieldID);
            }
        }

        String pkgName = structure.getActivityComponent().getPackageName();
        String URI = "app://" + pkgName;

        Database.dispatch(
                getApplicationContext(),
                new GetVaultItemByURI(new WeakReference<>(this), callback, URI)
        );

    }

    @Override
    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
        // Not applicable
    }

    @Override
    public void onDestroy() {
        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
        }

        super.onDestroy();
    }
}