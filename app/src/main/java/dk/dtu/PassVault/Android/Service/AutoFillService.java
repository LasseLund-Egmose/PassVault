package dk.dtu.PassVault.Android.Service;

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
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import dk.dtu.PassVault.Android.Activity.AutoFillDialogActivity;
import dk.dtu.PassVault.Business.Struct.AutoFillFieldSet;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AutoFillService extends android.service.autofill.AutofillService {

    public static final String LOG_TAG = "AutoFillDebugging";

    public abstract static class AutoFillCommunicator extends BroadcastReceiver {

        public static final String ACTION_RECEIVED_MASTER_PASSWORD = "dk.dtu.PassVault.RECEIVED_MASTER_PASSWORD";
        public static final String ACTION_MASTER_PASSWORD_VALIDATION_RESPONSE = "dk.dtu.PassVault.MASTER_PASSWORD_VALIDATION_RESPONSE";

    }

    protected static class ValidateMasterPasswordAndDecryptVaultItem extends Database.Transaction<Boolean> {

        protected FillCallback callback;
        protected Crypto crypto;
        protected AutoFillFieldSet fields;
        protected String hashedMasterPassword;
        protected String packageName;
        protected WeakReference<AutoFillService> serviceRef;
        protected VaultItem vaultItem;

        public ValidateMasterPasswordAndDecryptVaultItem(
                FillCallback callback,
                Crypto crypto,
                AutoFillFieldSet fields,
                String hashedMasterPassword,
                String packageName,
                WeakReference<AutoFillService> serviceRef,
                VaultItem vaultItem
        ) {
            this.callback = callback;
            this.crypto = crypto;
            this.fields = fields;
            this.hashedMasterPassword = hashedMasterPassword;
            this.packageName = packageName;
            this.serviceRef = serviceRef;
            this.vaultItem = vaultItem;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.getCredential().match(this.hashedMasterPassword);
        }

        protected void buildAutoFillResponse(String decryptedPassword) {
            RemoteViews presentation = new RemoteViews(packageName, android.R.layout.simple_list_item_1);
            presentation.setTextViewText(android.R.id.text1, "PassVault");

            Dataset.Builder dataSetBuilder = new Dataset.Builder();

            Log.i(LOG_TAG, "Build response: " + this.fields);
            if(this.fields.username != null) {
                dataSetBuilder.setValue(
                    this.fields.username,
                    AutofillValue.forText(vaultItem.username),
                    presentation
                );
            }

            if(this.fields.password != null) {
                dataSetBuilder.setValue(
                    this.fields.password,
                    AutofillValue.forText(decryptedPassword),
                    presentation
                );
            }

            if(callback != null) {
                FillResponse response = new FillResponse.Builder()
                        .addDataset(dataSetBuilder.build())
                        .build();

                callback.onSuccess(response); // Send response
            } else {
                Log.i(LOG_TAG, "Callback is null");
            }
        }

        @Override
        public void onResult(Boolean result) {
            AutoFillService service = this.serviceRef.get();
            if(service == null) {
                return;
            }

            Intent intent = new Intent();
            intent.setAction(AutoFillCommunicator.ACTION_MASTER_PASSWORD_VALIDATION_RESPONSE);
            intent.putExtra("success", result);
            service.sendBroadcast(intent);

            service.setRequestLock(false);

            if(!result) {
                return;
            }

            Log.i(LOG_TAG, "Encrypted password: " + Arrays.toString(this.vaultItem.password));

            this.crypto.decrypt(this.vaultItem.password, new Crypto.CryptoResponse() {
                @Override
                public void run() {
                    if(!this.isSuccessful) {
                        return;
                    }

                    Log.i(LOG_TAG, "Decrypted password: " + this.decryptedData);
                    Log.i(LOG_TAG, "Callback: " + callback);

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

    protected boolean requestLock = false;
    protected AutoFillCommunicator receiver = null;

    protected AutoFillFieldSet fields = new AutoFillFieldSet();

    protected void setupPrompt(FillCallback callback, VaultItem item) {
        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
            this.receiver = null;
        }

        String selfPkgName = this.getPackageName();

        this.receiver = new AutoFillCommunicator() {

            protected void dispatchVaultItemDecryption(Crypto crypto, String hashedMasterPassword) {
                ValidateMasterPasswordAndDecryptVaultItem transaction = new ValidateMasterPasswordAndDecryptVaultItem(
                        callback, crypto, fields, hashedMasterPassword, selfPkgName,
                        new WeakReference<>(AutoFillService.this), item
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

                        Log.i(LOG_TAG, "Hashed password: " + this.hashedData);
                        Log.i(LOG_TAG, "WeakRefCallback: " + callback);

                        dispatchVaultItemDecryption(this.crypto, this.hashedData);
                    }
                });
            }

            @Override
            public void onReceive(Context context, Intent intent) {
                if(!AutoFillCommunicator.ACTION_RECEIVED_MASTER_PASSWORD.equals(intent.getAction())) {
                    return;
                }

                Log.i(LOG_TAG, "Received broadcast" + intent.getExtras());

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

    protected void traverseStructure(AssistStructure structure) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            traverseNode(viewNode);
        }
    }

    protected void traverseNode(AssistStructure.ViewNode viewNode) {
        String className = viewNode.getClassName();
        if(className != null && className.equals("android.widget.EditText")) {
            boolean noUsername = this.fields.username == null;
            boolean noPassword = this.fields.password == null;

            if(noPassword && (viewNode.getText() + viewNode.getHint()).toLowerCase().contains("password")) {
                this.fields.password = viewNode.getAutofillId();
            } else if(noUsername) {
                this.fields.username = viewNode.getAutofillId();
            } else if(noPassword) {
                this.fields.password = viewNode.getAutofillId();
            }
        }

        for(int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            traverseNode(childNode);
        }
    }

    protected void setRequestLock(boolean to) {
        this.requestLock = to;
    }

    @Override
    public void onFillRequest(@NonNull FillRequest request, @NonNull CancellationSignal signal, @NonNull FillCallback callback) {
        Log.i(LOG_TAG, "Autofill request!");

        if(this.requestLock) {
            return;
        }

        this.requestLock = true;

        List<FillContext> fillContexts = request.getFillContexts();
        AssistStructure structure = fillContexts.get(fillContexts.size() - 1).getStructure();

        this.traverseStructure(structure);

        Log.i(LOG_TAG, this.fields.toString());

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