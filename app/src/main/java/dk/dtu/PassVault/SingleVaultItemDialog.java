package dk.dtu.PassVault;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

import static android.widget.Toast.LENGTH_LONG;

public class SingleVaultItemDialog extends DialogFragment {

    protected Context context;
    protected VaultItem item;

    public SingleVaultItemDialog(VaultItem item) {
        this.item = item;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        setRetainInstance(true);

        builder.setView(inflater.inflate(R.layout.single_vault_item_dialog, null));

        AlertDialog ad = builder.create();

        ad.setOnShowListener(dialogInterface -> {
            AlertDialog dialog = (AlertDialog) dialogInterface;

            TextView title = dialog.findViewById(R.id.viewTitle);
            TextView platform = dialog.findViewById(R.id.viewPlatform);
            TextView username = dialog.findViewById(R.id.viewUsername);
            TextView password = dialog.findViewById(R.id.viewPassword);
            Button copyPassword = (Button) dialog.findViewById(R.id.copyPassword);
            Button copyUsername = (Button) dialog.findViewById(R.id.copyUsername);

            copyPassword.setOnClickListener(view -> {
                if (!password.getText().toString().isEmpty() && password.getText() != null) {
                    ClipData clipData = ClipData.newPlainText("Password", password.getText());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getContext(), "Password copied to clipboard", LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No password to be copied", LENGTH_LONG).show();
                }

            });

            copyUsername.setOnClickListener(view -> {
                if (!username.getText().toString().isEmpty() && username.getText() != null) {
                    ClipData clipData = ClipData.newPlainText("Username", username.getText());
                    clipboardManager.setPrimaryClip(clipData);
                    Toast.makeText(getContext(), "Username copied to clipboard", LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "No username to be copied", LENGTH_LONG).show();
                }

            });

            if (title == null || platform == null || username == null || password == null) {
                return;
            }

            title.setText(this.item.displayName);
            platform.setText(this.item.URI);
            username.setText(this.item.username);

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
        });

        return ad;
    }

}