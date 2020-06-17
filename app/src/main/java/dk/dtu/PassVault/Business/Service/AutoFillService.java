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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dk.dtu.PassVault.AutoFillDialogActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillService extends android.service.autofill.AutofillService {

    public abstract static class AutoFillPermissionGrantedReceiver extends BroadcastReceiver {

        public static final String ACTION = "dk.dtu.PassVault.AUTO_FILL_PERMISSION_GRANTED";

    }

    protected static class ValidateMasterPasswordAndGetVaultItem extends Database.Transaction<VaultItem> {

        protected FillCallback callback;
        protected Crypto crypto;
        protected String hashedMasterPassword;
        protected String packageName;
        protected ArrayList<AutofillId> passwordFields;
        protected String URI;

        public ValidateMasterPasswordAndGetVaultItem(
                FillCallback callback,
                Crypto crypto,
                String hashedMasterPassword,
                String packageName,
                ArrayList<AutofillId> passwordFields,
                String URI
        ) {
            this.callback = callback;
            this.crypto = crypto;
            this.hashedMasterPassword = hashedMasterPassword;
            this.packageName = packageName;
            this.passwordFields = passwordFields;
            this.URI = URI;
        }

        @Override
        public VaultItem doRequest(Database db) {
            if(!db.getCredential().match(this.hashedMasterPassword)) {
                return null;
            }

            return db.getVaultItemByURI(this.URI);
        }

        @Override
        public void onResult(VaultItem result) {
            Log.i("Autofill", "Encrypted password: " + Arrays.toString(result.password));

            this.crypto.decrypt(result.password, new Crypto.CryptoResponse() {
                @Override
                public void run() {
                    if(!this.isSuccessful) {
                        return;
                    }

                    Log.i("Autofill", "Decrypted password: " + this.decryptedData);
                    Log.i("Autofill", "Password fields: " + passwordFields);
                    Log.i("Autofill", "Callback: " + callback);

                    RemoteViews passwordPresentation = new RemoteViews(packageName, android.R.layout.simple_list_item_1);
                    passwordPresentation.setTextViewText(android.R.id.text1, "PassVault password");

                    AutofillId passwordID = passwordFields.get(0);

                    FillResponse fillResponse = new FillResponse.Builder()
                        .addDataset(new Dataset.Builder()
                            .setValue(passwordID,
                                AutofillValue.forText(this.decryptedData), passwordPresentation)
                            .build())
                        .build();

                    if(callback != null) {
                        callback.onSuccess(fillResponse);
                    } else {
                        Log.i("Autofill", "Callback is null");
                    }
                }
            });
        }
    }

    protected boolean hasReceivedRequest = false;
    protected AutoFillPermissionGrantedReceiver receiver = null;
    protected ArrayList<AutofillId> passwordFields = new ArrayList<>();

    protected AutofillId identifyPasswordField(AssistStructure.ViewNode root) {
        if(root.getClassName() != null && root.getClassName().equals("android.widget.EditText")) {
            // TODO: Identify through root.getText() and root.getHint() whether or not this is a password field
            return root.getAutofillId();
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
    public void onFillRequest(FillRequest request, CancellationSignal signal, FillCallback callback) {
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

        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
            this.receiver = null;
        }

        String pkgName = this.getPackageName();

        this.receiver = new AutoFillPermissionGrantedReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
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

                crypto.hash(password, new Crypto.CryptoResponse() {
                    @Override
                    public void run() {
                        if (!this.isSuccessful) {
                            return;
                        }

                        Log.i("Autofill", "Hashed password: " + this.hashedData);
                        Log.i("Autofill", "WeakRefCallback: " + callback);

                        Database.dispatch(
                            getApplicationContext(),
                            new ValidateMasterPasswordAndGetVaultItem(
                                callback,
                                this.crypto,
                                this.hashedData,
                                pkgName,
                                passwordFields,
                                "app://com.google.chrome"
                            )
                        );
                    }
                });
            }
        };

        registerReceiver(this.receiver, new IntentFilter(AutoFillPermissionGrantedReceiver.ACTION));

        Context context = this.getApplicationContext();
        Intent intent = new Intent(context, AutoFillDialogActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
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