package dk.dtu.PassVault;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.lang.ref.WeakReference;

import dk.dtu.PassVault.Business.Crypto.Crypto;
import dk.dtu.PassVault.Business.Service.AutoFillService;

public class AutoFillDialogActivity extends BaseActivity {

    protected AutoFillService.AutoFillCommunicator receiver = null;

    @Override
    protected boolean allowNoKey() {
        return true;
    }

    protected void setupReceiver() {
        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
        }

        this.receiver = new AutoFillService.AutoFillCommunicator() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!AutoFillService.AutoFillCommunicator.ACTION_MASTER_PASSWORD_VALIDATION_RESPONSE.equals(intent.getAction())) {
                    return;
                }

                Log.i("Dialog", "Received validation response");
            }
        };
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);

        this.setContentView(R.layout.autofill_dialog);

        EditText masterPassword = this.findViewById(R.id.autoFillInput);
        Button btn = this.findViewById(R.id.autoFillBtn);



        btn.setOnClickListener(v -> {
            this.setupReceiver();

            Intent intent = new Intent();
            intent.setAction(AutoFillService.AutoFillCommunicator.ACTION_RECEIVED_MASTER_PASSWORD);
            intent.putExtra("password", masterPassword.getText().toString());
            sendBroadcast(intent);
        });
    }

    @Override
    protected void onDestroy() {
        if (this.receiver != null) {
            unregisterReceiver(this.receiver);
        }

        super.onDestroy();
    }
}