package dk.dtu.PassVault.Android.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.dtu.PassVault.Android.Activity.CreateVaultItemActivity;
import dk.dtu.PassVault.Android.Adapter.AppListAdapter;
import dk.dtu.PassVault.R;

public class PlatformDialog extends DialogFragment {

    protected Context context;
    protected Listener listener;
    protected ArrayList<ApplicationInfo> packages = new ArrayList<>();

    public interface Listener {
        void onDialogAddClick(DialogFragment dialog, String result);

        void onDialogCancelClick(DialogFragment dialog);

        void onDialogTouchOutsideClick(DialogFragment dialog);
    }

    protected void onAdd(AlertDialog dialog, int checkedTab, ApplicationInfo selectedApp, String website) {
        String result = "";

        if (checkedTab == R.id.app && selectedApp != null) {
            result = "app://" + selectedApp.packageName;
            dialog.dismiss();
        }

        if (checkedTab == R.id.website){
            try {
                String scheme = URI.create(website).getScheme();
                if (scheme != null && (scheme.equals("http") || scheme.equals("https"))) {
                    result = website;
                    dialog.dismiss();
                } else {
                    Toast.makeText(getContext(), R.string.invalid_url, Toast.LENGTH_SHORT).show();
                }
            } catch (IllegalArgumentException e) {
                Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }

        this.listener.onDialogAddClick(PlatformDialog.this, result);
    }

    protected void onShow(AlertDialog dialog) {
        Button addBtn = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
        Spinner appList = dialog.findViewById(R.id.appList);
        TextView webSiteText = dialog.findViewById(R.id.webSiteText);
        RadioButton radioButtonApp = dialog.findViewById(R.id.app);
        ConstraintLayout tabApp = dialog.findViewById(R.id.tabApp);
        ConstraintLayout tabWeb = dialog.findViewById(R.id.tabWeb);

        if(
            addBtn == null || radioGroup == null || appList == null || webSiteText == null ||
                radioButtonApp == null || tabApp == null || tabWeb == null
        ) return;

        addBtn.setOnClickListener(v -> {
            this.onAdd(
                dialog,
                radioGroup.getCheckedRadioButtonId(),
                (ApplicationInfo) appList.getSelectedItem(),
                webSiteText.getText().toString()
            );
        });

        dialog.setOnCancelListener(dialog1 -> listener.onDialogTouchOutsideClick(PlatformDialog.this));

        appList.setAdapter(new AppListAdapter(this.context, R.layout.single_app_list, this.packages));

        radioGroup.setOnCheckedChangeListener((radioGroup1, i) -> {
            boolean showTabApp = radioButtonApp.isChecked();
            tabApp.setVisibility(showTabApp ? View.VISIBLE : View.INVISIBLE);
            tabWeb.setVisibility(showTabApp ? View.INVISIBLE : View.VISIBLE);
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.context = context;
        this.listener = (Listener) context;

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfoList = this.context.getPackageManager().queryIntentActivities(intent, 0);

        for (ResolveInfo info : resolveInfoList) {
            if (info.activityInfo == null || info.activityInfo.applicationInfo == null) {
                continue;
            }

            ApplicationInfo appInfo = info.activityInfo.applicationInfo;

            if (appInfo.packageName != null && !appInfo.packageName.equals("dk.dtu.PassVault")) {
                this.packages.add(appInfo);
            }
        }

        PackageManager pm = this.context.getPackageManager();
        Collections.sort(this.packages, ((appInfo1, appInfo2) -> {
            String label1 = pm.getApplicationLabel(appInfo1).toString();
            String label2 = pm.getApplicationLabel(appInfo2).toString();

            return label1.compareTo(label2);
        }));
    }

    @Override
    public void onStop() {
        super.onStop();

        CreateVaultItemActivity activity = (CreateVaultItemActivity) this.getActivity();
        if (activity == null) return;

        EditText platformField = activity.findViewById(R.id.platform);
        platformField.clearFocus();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        this.setRetainInstance(true);

        FragmentActivity activity = this.requireActivity();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_platform, null))
            .setPositiveButton("Add", (dialog, id) -> {

            })
            .setNegativeButton("Cancel", (dialog, id) -> {
                listener.onDialogCancelClick(PlatformDialog.this);
            });

        AlertDialog ad = builder.create();

        ad.setOnShowListener(dialogInterface -> {
            this.onShow((AlertDialog) dialogInterface);
        });

        return ad;
    }

}