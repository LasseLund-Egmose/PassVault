package dk.dtu.PassVault.Android.Dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Android.Activity.EditOrCreateVaultItemActivity;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Database;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;
import dk.dtu.PassVault.R;
import dk.dtu.PassVault.Android.Activity.VaultActivity;

import static android.widget.Toast.LENGTH_LONG;

public class SingleVaultItemDialog extends DialogFragment {

    protected ClipboardManager clipboardManager;
    protected Context context;
    protected VaultItem item;
    protected VaultActivity vaultActivity;

    public SingleVaultItemDialog(VaultItem item) {
        this.item = item;
    }

    protected static class DeleteVaultItemTransaction extends Database.Transaction<Void> {

        protected VaultItem vaultItem;
        protected WeakReference<SingleVaultItemDialog> ref;

        public DeleteVaultItemTransaction(VaultItem vaultItem, WeakReference<SingleVaultItemDialog> ref) {
            this.vaultItem = vaultItem;
            this.ref = ref;
        }

        @Override
        public Void doRequest(Database db) {
            db.deleteVaultItem(this.vaultItem);
            return null;
        }

        @Override
        public void onResult(Void v) {
            SingleVaultItemDialog activity = this.ref.get();

            if(activity == null) {
                return;
            }

            Toast.makeText(activity.getContext(), "Item deleted", Toast.LENGTH_LONG).show();
            ((VaultActivity)activity.getActivity()).refreshList();
            activity.dismiss();
        }
    }

    protected void setupCopy(TextView username, TextView password, Button copyUsername, Button copyPassword) {
        copyUsername.setOnClickListener(view -> {
            String usernameString = username.getText().toString();
            if (!usernameString.isEmpty()) {
                ClipData clipData = ClipData.newPlainText("Username", usernameString);
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(getContext(), R.string.username_copied, LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), R.string.no_username_to_copy, LENGTH_LONG).show();
            }

        });

        copyPassword.setOnClickListener(view -> {
            String passwordString = password.getText().toString();
            if (!passwordString.isEmpty()) {
                ClipData clipData = ClipData.newPlainText("Password", passwordString);
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(getContext(), R.string.password_copied, LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), R.string.no_password_to_copy, LENGTH_LONG).show();
            }

        });
    }

    protected void onShow(AlertDialog dialog) {
        TextView title = dialog.findViewById(R.id.viewTitle);
        TextView platform = dialog.findViewById(R.id.viewPlatform);
        TextView username = dialog.findViewById(R.id.viewUsername);
        TextView password = dialog.findViewById(R.id.viewPassword);

        Button copyUsername = dialog.findViewById(R.id.copyUsername);
        Button copyPassword = dialog.findViewById(R.id.copyPassword);
        Button deleteBtn = dialog.findViewById(R.id.delete_item_btn);

        if(
            title == null || platform == null || username == null || password == null ||
                copyUsername == null || copyPassword == null || deleteBtn == null
        ) return;

        title.setText(this.item.displayName);
        platform.setText(this.item.URI);
        username.setText(this.item.username);

        this.setupCopy(username, password, copyUsername, copyPassword);

        // Set decrypted password
        WeakReference<TextView> passwordRef = new WeakReference<>(password);
        Crypto.getInstance().decrypt(this.item.password, new Crypto.CryptoResponse() {
            @Override
            public void run() {
                TextView password = passwordRef.get();
                if (this.isSuccessful && password != null) {
                    password.setText(this.decryptedData);
                }
            }
        });

        deleteBtn.setOnClickListener(v -> {
            DeleteVaultItemTransaction transaction = new DeleteVaultItemTransaction(
                this.item,
                new WeakReference<>(this)
            );
            Database.dispatch(context, transaction);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.setRetainInstance(true);

        FragmentActivity activity = this.requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.single_vault_item_dialog, null));

        AlertDialog ad = builder.create();

        ad.setOnShowListener(dialogInterface -> {
            this.onShow((AlertDialog) dialogInterface);
        });

        return ad;
    }

}