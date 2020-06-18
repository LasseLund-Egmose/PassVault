package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
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
    FloatingActionButton fab_settings;
    ExtendedFloatingActionButton fab_modifyMasterPass, fab_deleteAccount;
    Animation fabOpen, fabClose, fabRotateRight, fabRotateLeft;
    boolean isOpen = false;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vault);
        getSupportActionBar().hide();

        fab_settings = (FloatingActionButton) findViewById(R.id.settingsBtn);
        fab_modifyMasterPass = (ExtendedFloatingActionButton) findViewById(R.id.modify_master_pass_Btn);
        fab_deleteAccount = (ExtendedFloatingActionButton) findViewById(R.id.deleteAccountBtn);

        fabOpen = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_settings_open);
        fabClose = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_settings_close);
        fabRotateRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_settings_rotate);
        fabRotateLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_settings_rotate_back);


        FloatingActionButton addButton = findViewById(R.id.addBtn);
        addButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditOrCreateVaultItemActivity.class);
            startActivityForResult(intent, ADD_PROFILE_CODE);
        });

        fab_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpen) {
                    fab_modifyMasterPass.startAnimation(fabClose);
                    fab_deleteAccount.startAnimation(fabClose);
                    fab_settings.startAnimation(fabRotateLeft);
                    fab_modifyMasterPass.setClickable(false);
                    fab_deleteAccount.setClickable(false);
                    isOpen = false;

                } else {
                    fab_modifyMasterPass.startAnimation(fabOpen);
                    fab_deleteAccount.startAnimation(fabOpen);
                    fab_settings.startAnimation(fabRotateRight);
                    fab_modifyMasterPass.setClickable(true);
                    fab_deleteAccount.setClickable(true);
                    isOpen = true;

                }
            }
        });


        fab_modifyMasterPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogModifyPass();
            }
        });

        fab_deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogDeleteAccount();
            }
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

    public void openDialogModifyPass() {
        DialogModifyMasterPass dialog = new DialogModifyMasterPass();
        dialog.show(getSupportFragmentManager(), "modify pass dialog");
    }

    public void openDialogDeleteAccount() {
        DialogDeleteAccount dialog = new DialogDeleteAccount();
        dialog.show(getSupportFragmentManager(), "delete account");
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
