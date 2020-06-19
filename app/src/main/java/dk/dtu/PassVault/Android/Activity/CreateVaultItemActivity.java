package dk.dtu.PassVault.Android.Activity;

import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import dk.dtu.PassVault.Android.Activity.Abstract.BaseActivity;
import dk.dtu.PassVault.Business.Util.IconExtractor;
import dk.dtu.PassVault.Android.Dialog.PlatformDialog;
import dk.dtu.PassVault.R;


public class CreateVaultItemActivity extends BaseActivity implements PlatformDialog.Listener {

    protected EditText title, platform, username, password;
    private final static int REQUEST_CODE_PASSWORD = 0;
    protected ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new);

        this.platform = findViewById(R.id.platform);
        this.title = findViewById(R.id.title);
        this.username = findViewById(R.id.username);
        this.password = findViewById(R.id.password);
        this.icon = findViewById(R.id.app_icon);

        this.platform.setOnFocusChangeListener((v, isFocused) -> {
            if(!isFocused) return;

            DialogFragment dialog = new PlatformDialog();
            dialog.show(getSupportFragmentManager(), "PlatformDialogFragment");

            dialog.onCancel(new DialogInterface() {
                @Override
                public void cancel() {
                    platform.clearFocus();
                }

                @Override
                public void dismiss() {
                    platform.clearFocus();
                }
            });
        });

        Button genBtn = findViewById(R.id.generate_password);
        genBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PasswordGeneratorActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PASSWORD);
        });

        Button createButton = findViewById(R.id.button5);
        createButton.setOnClickListener(v -> createClicked());
    }

    private void createClicked() {
        String title = this.title.getText().toString();
        String platform = this.platform.getText().toString();
        String username = this.username.getText().toString();
        String password = this.password.getText().toString();

        if(platform.isEmpty()) {
            this.toastShort(R.string.must_specify_platform);
            return;
        }

        if(title.isEmpty()) {
            this.toastShort(R.string.must_specify_title);
            return;
        }

        if(username.isEmpty()) {
            this.toastShort(R.string.must_specify_username);
            return;
        }

        if(password.isEmpty()) {
            this.toastShort(R.string.must_specify_password);
            return;
        }

        Intent i = new Intent(getApplicationContext(), VaultActivity.class);
        i.putExtra("URI", platform);
        i.putExtra("displayName", title);
        i.putExtra("username", username);
        i.putExtra("password", password);

        this.setResult(RESULT_OK, i);
        this.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(intent == null) {
            return;
        }

        Uri uri = intent.getData();
        if (requestCode == REQUEST_CODE_PASSWORD && resultCode == RESULT_OK && uri != null) {
            password.setText(uri.toString());
        }
    }

    @Override
    public void onDialogAddClick(DialogFragment dialog, String result) {
        Drawable iconSrc = IconExtractor.extractIcon(getApplicationContext(), result);
        this.icon.setImageDrawable(iconSrc);

        this.platform.setText(result);
        this.platform.clearFocus();
    }

    @Override
    public void onDialogCancelClick(DialogFragment dialog) {
        this.platform.clearFocus();
    }

    @Override
    public void onDialogTouchOutsideClick(DialogFragment dialog) {
        this.platform.clearFocus();
    }

}
