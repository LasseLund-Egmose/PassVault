package dk.dtu.PassVault;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class DialogModifyMasterPass extends AppCompatDialogFragment {

    private EditText mCurrentPassword, mNewPassword, mNewPasswordAgain;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_modify_master_password, null);
        builder.setView(view)
        .setPositiveButton("Modify", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        mCurrentPassword = view.findViewById(R.id.current_master_pass_editView);
        mNewPassword = view.findViewById(R.id.new_master_pass_editView);
        mNewPasswordAgain = view.findViewById(R.id.new_master_pass_again_editView);

        return builder.create();
    }
}
