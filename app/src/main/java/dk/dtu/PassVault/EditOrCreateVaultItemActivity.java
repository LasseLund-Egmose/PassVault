package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;


public class EditOrCreateVaultItemActivity extends BaseActivity implements PlatformDialog.Listener {

    protected EditText title, platform, username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add_new);

        this.platform = findViewById(R.id.platform);
        this.title = findViewById(R.id.title);
        this.username = findViewById(R.id.username);
        this.password = findViewById(R.id.password);

        this.platform.setOnFocusChangeListener((v, isFocused) -> {
            if(!isFocused) return;

            DialogFragment dialog = new PlatformDialog();
            dialog.show(getSupportFragmentManager(), "PlatformDialogFragment");
        });

        Button genBtn = findViewById(R.id.generate_password);
        genBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PasswordGeneratorActivity.class);
            startActivityForResult(intent, 0);
        });

        Button createButton = findViewById(R.id.button5);
        createButton.setOnClickListener(v -> createClicked());
    }

    private void createClicked() {
        String title = this.title.getText().toString();
        String platform = this.platform.getText().toString();
        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        Intent i = new Intent(getApplicationContext(), VaultActivity.class);
        i.putExtra("URI", platform);
        i.putExtra("displayName", title);
        i.putExtra("username", username);
        i.putExtra("password", password);

        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            password.setText(intent.getData().toString());
        }
    }

    @Override
    public void onDialogAddClick(DialogFragment dialog, String result) {
        Log.i("Dialog", result);

        platform.setText(result);
        platform.clearFocus();

    }

    @Override
    public void onDialogCancelClick(DialogFragment dialog) {
        Log.i("Dialog", "Negative");
        platform.clearFocus();
    }
}
