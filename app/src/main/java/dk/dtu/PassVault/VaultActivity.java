package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

import dk.dtu.PassVault.Business.Adapter.VaultItemAdapter;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

public class VaultActivity extends BaseActivity {

    public static final int ADD_PROFILE_CODE = 1;
    public static ArrayList<Button> profileButtons = new ArrayList<Button>(0);

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

    protected VaultItemAdapter vaultItemAdapter = null;
    protected ArrayList<VaultItem> vaultItems = new ArrayList<>();

    protected void refreshList() {
        if(this.vaultItemAdapter == null) {
            return;
        }

        Database.dispatch(
            getApplicationContext(),
            new GetVaultItemsTransaction(new WeakReference<>(this.vaultItemAdapter), new WeakReference<>(vaultItems))
        );
    }

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        getSupportActionBar().hide();


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
                        Toast.makeText(getApplicationContext(), "An error occurred!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    VaultItem item = new VaultItem(URI, displayName, userName, this.encryptedData);
                    Database.dispatch(getApplicationContext(), new AddVaultItemTransaction(thisRef, item));
                }
            });
        }
    }
}
