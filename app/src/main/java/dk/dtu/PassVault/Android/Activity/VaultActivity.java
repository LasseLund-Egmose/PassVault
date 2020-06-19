package dk.dtu.PassVault.Android.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.autofill.AutofillManager;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Android.Adapter.VaultItemAdapter;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;
import dk.dtu.PassVault.Android.Dialog.SingleVaultItemDialog;
import dk.dtu.PassVault.R;

public class VaultActivity extends BaseActivity {

    public static final int ADD_PROFILE_CODE = 1;

    protected static final String SETTING_HAS_SHOWN_AUTO_FILL_DIALOG = "hasShownAutoFillDialog";

    protected static class AddVaultItemTransaction extends Database.Transaction<Void> {

        protected WeakReference<VaultActivity> activityRef;
        protected VaultItem item;

        public AddVaultItemTransaction(WeakReference<VaultActivity> activityRef, VaultItem item) {
            this.activityRef = activityRef;
            this.item = item;
        }

        @Override
        public Void doRequest(Database db) {
            db.addVaultItem(this.item);
            return null;
        }

        @Override
        public void onResult(Void result) {
            VaultActivity activity = this.activityRef.get();

            Toast.makeText(activity, "Vault item created", Toast.LENGTH_LONG).show();
            activity.refreshList();
        }
    }

    protected static class GetVaultItemsTransaction extends Database.Transaction<VaultItem[]> {

        protected WeakReference<VaultItemAdapter> vaultItemAdapterRef;
        protected WeakReference<ArrayList<VaultItem>> vaultItemsRef;

        public GetVaultItemsTransaction(WeakReference<VaultItemAdapter> vaultItemAdapterRef, WeakReference<ArrayList<VaultItem>> vaultItemsRef) {
            this.vaultItemAdapterRef = vaultItemAdapterRef;
            this.vaultItemsRef = vaultItemsRef;
        }

        @Override
        public VaultItem[] doRequest(Database db) {
            return db.getVaultItems();
        }

        @Override
        public void onResult(VaultItem[] items) {
            ArrayList<VaultItem> vaultItems = this.vaultItemsRef.get();
            vaultItems.clear();
            vaultItems.addAll(Arrays.asList(items));

            this.vaultItemAdapterRef.get().notifyDataSetChanged();
        }
    }

    protected static class UpdateAutoFillDialogSetting extends Database.Transaction<Void> {

        protected boolean hasShown;

        public UpdateAutoFillDialogSetting(boolean hasShown) {
            this.hasShown = hasShown;
        }

        @Override
        public Void doRequest(Database db) {
            db.setSetting(SETTING_HAS_SHOWN_AUTO_FILL_DIALOG, String.valueOf(this.hasShown));
            return null;
        }

        @Override
        public void onResult(Void result) {
            // Do nothing
        }
    }

    protected static class ShowAutoFillDialogIfRelevant extends Database.Transaction<Boolean> {

        protected WeakReference<VaultActivity> ref;

        public ShowAutoFillDialogIfRelevant(WeakReference<VaultActivity> ref) {
            this.ref = ref;
        }

        @Override
        public Boolean doRequest(Database db) {
            return db.getSetting(SETTING_HAS_SHOWN_AUTO_FILL_DIALOG) == null;
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onResult(Boolean result) {
            VaultActivity activity = this.ref.get();

            if (activity == null || !result) return;

            new AlertDialog.Builder(activity)
                .setTitle(R.string.autofill_title)
                .setMessage(R.string.autofill_prompt)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_REQUEST_SET_AUTOFILL_SERVICE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    intent.setData(Uri.parse("package:dk.dtu.PassVault"));
                    activity.startActivity(intent);

                    Toast.makeText(activity, R.string.select_passvault, Toast.LENGTH_LONG).show();

                    Database.dispatch(activity, new UpdateAutoFillDialogSetting(true));
                })
                .setNegativeButton(android.R.string.no, ((dialog, which) -> Database.dispatch(activity, new UpdateAutoFillDialogSetting(false))))
                .setIcon(R.drawable.logo_icon)
                .show();
        }
    }

    protected VaultItemAdapter vaultItemAdapter = null;
    protected ArrayList<VaultItem> vaultItems = new ArrayList<>();

    protected void promptAutoFill() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        AutofillManager afm = getApplicationContext().getSystemService(AutofillManager.class);
        if (afm.isAutofillSupported() && !afm.hasEnabledAutofillServices()) {
            Database.dispatch(getApplicationContext(), new ShowAutoFillDialogIfRelevant(new WeakReference<>(this)));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);

        FloatingActionButton addButton = findViewById(R.id.addBtn);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditOrCreateVaultItemActivity.class);
            startActivityForResult(intent, ADD_PROFILE_CODE);
        });


        this.vaultItemAdapter = new VaultItemAdapter(this, R.layout.vault_item_single, vaultItems);

        GridView vaultContainer = (GridView) findViewById(R.id.vault_item_container);
        vaultContainer.setAdapter(this.vaultItemAdapter);
        vaultContainer.setOnItemClickListener((parent, view, position, id) -> {
            VaultItem item = this.vaultItemAdapter.getItem(position);

            DialogFragment dialog = new SingleVaultItemDialog(item);
            dialog.show(getSupportFragmentManager(), "SingleVaultItemDialog");
        });

        this.refreshList();
        this.promptAutoFill();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.refreshList();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK && requestCode == ADD_PROFILE_CODE) {
            Bundle extras = intent.getExtras();

            if (extras == null) {
                return;
            }

            final String URI = extras.getString("URI");
            final String displayName = extras.getString("displayName");
            final String userName = extras.getString("username");
            final String password = extras.getString("password");

            if (URI == null || displayName == null || userName == null || password == null) {
                return;
            }

            WeakReference<VaultActivity> thisRef = new WeakReference<>(this);

            this.getCrypto().encrypt(password, new Crypto.CryptoResponse() {
                @Override
                public void run() {
                    if (!this.isSuccessful) {
                        toastShort(R.string.error_occurred);
                        return;
                    }

                    VaultItem item = new VaultItem(URI, displayName, userName, this.encryptedData);
                    Database.dispatch(getApplicationContext(), new AddVaultItemTransaction(thisRef, item));
                }
            });
        }
    }

    public void refreshList() {
        if (this.vaultItemAdapter == null) {
            return;
        }

        Database.dispatch(
            getApplicationContext(),
            new GetVaultItemsTransaction(new WeakReference<>(this.vaultItemAdapter), new WeakReference<>(vaultItems))
        );
    }
}
