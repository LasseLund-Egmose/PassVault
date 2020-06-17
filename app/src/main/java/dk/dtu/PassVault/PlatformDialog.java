package dk.dtu.PassVault;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import dk.dtu.PassVault.Business.Adapter.AppListAdapter;

public class PlatformDialog extends DialogFragment {

    protected Context context;
    protected Listener listener;
    protected ArrayList<PackageInfo> packages = new ArrayList<>();

    public interface Listener {
        void onDialogAddClick(DialogFragment dialog);
        void onDialogCancelClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        this.listener = (Listener) context;
        this.packages.addAll(context.getPackageManager().getInstalledPackages(0));
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.platform_dialog, null))
            .setPositiveButton("Add", (dialog, id) -> {
                listener.onDialogAddClick(PlatformDialog.this);
            })
            .setNegativeButton("Cancel", (dialog, id) -> {
                listener.onDialogCancelClick(PlatformDialog.this);
            });

        AlertDialog ad = builder.create();

        ad.setOnShowListener(dialogInterface -> {
            AlertDialog dialog = (AlertDialog) dialogInterface;

            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(R.color.colorDTU));
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.greyed));

            Spinner appList = dialog.findViewById(R.id.appList);
            if(appList != null) {
                appList.setAdapter(new AppListAdapter(this.context, R.layout.app_list_single, this.packages));
            }
        });

        return ad;
    }

}