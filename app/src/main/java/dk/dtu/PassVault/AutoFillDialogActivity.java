package dk.dtu.PassVault;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Service.AutoFillService;

public class AutoFillDialogActivity extends BaseActivity {

    @Override
    protected boolean allowNoKey() {
        return true;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(R.layout.autofill_dialog);

        EditText masterPassword = this.findViewById(R.id.autoFillInput);
        Button btn = this.findViewById(R.id.autoFillBtn);

        btn.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(AutoFillService.AutoFillPermissionGrantedReceiver.ACTION);
            intent.putExtra("password", masterPassword.getText().toString());
            sendBroadcast(intent);

            finishAndRemoveTask();
        });
    }

}