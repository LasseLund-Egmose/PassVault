package dk.dtu.PassVault.Android.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.R;
import dk.dtu.PassVault.Android.Service.AutoFillService;

public class AutoFillDialogActivity extends BaseActivity {

    public static final String LOG_TAG = "AutoFillDialogDebugging";

    protected AutoFillService.AutoFillCommunicator receiver = null;

    @Override
    protected boolean allowNoKey() {
        return true;
    }

    protected void setupReceiver() {
        if(this.receiver != null) {
            unregisterReceiver(this.receiver);
        }

        final String ACTION = AutoFillService.AutoFillCommunicator.ACTION_MASTER_PASSWORD_VALIDATION_RESPONSE;

        this.receiver = new AutoFillService.AutoFillCommunicator() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(!ACTION.equals(intent.getAction())) {
                    return;
                }

                Log.i(LOG_TAG, "Received broadcast");

                Bundle extras = intent.getExtras();
                if(extras == null) {
                    return;
                }

                if(extras.getBoolean("success")) {
                    toastShort(R.string.success);
                    finishAndRemoveTask();
                } else {
                    toastShort(R.string.wrong_password);
                }
            }
        };

        registerReceiver(this.receiver, new IntentFilter(ACTION));
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(R.layout.dialog_autofill);

        EditText masterPassword = this.findViewById(R.id.autoFillInput);
        Button btn = this.findViewById(R.id.autoFillBtn);

        btn.setOnClickListener(v -> {
            Log.i(LOG_TAG, "Click");

            this.setupReceiver();

            Intent intent = new Intent();
            intent.setAction(AutoFillService.AutoFillCommunicator.ACTION_RECEIVED_MASTER_PASSWORD);
            intent.putExtra("password", masterPassword.getText().toString());
            sendBroadcast(intent);

            Log.i(LOG_TAG, "Sent broadcast");
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