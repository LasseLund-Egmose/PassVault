package dk.dtu.PassVault;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import dk.dtu.PassVault.Business.Adapter.AppListAdapter;
import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Database.Entities.VaultItem;

public class SingleVaultItemDialog extends DialogFragment {

    protected Context context;
    protected VaultItem item;
    protected ArrayList<PackageInfo> packages = new ArrayList<>();

    public SingleVaultItemDialog(VaultItem item) {
        this.item = item;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        this.packages.addAll(context.getPackageManager().getInstalledPackages(0));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.single_vault_item_dialog, null));

        AlertDialog ad = builder.create();

        ad.setOnShowListener(dialogInterface -> {
            AlertDialog dialog = (AlertDialog) dialogInterface;

            TextView title = dialog.findViewById(R.id.viewTitle);
            TextView platform = dialog.findViewById(R.id.viewPlatform);
            TextView username = dialog.findViewById(R.id.viewUsername);
            TextView password = dialog.findViewById(R.id.viewPassword);

            if(title == null || platform == null || username == null || password == null) {
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
                    if(this.isSuccessful && password != null) {
                        password.setText(this.decryptedData);
                    }
                }
            });
        });

        return ad;
    }

}