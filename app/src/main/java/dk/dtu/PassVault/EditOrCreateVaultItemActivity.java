package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import dk.dtu.PassVault.Business.Util.IconExtractor;


public class EditOrCreateVaultItemActivity extends BaseActivity implements PlatformDialog.Listener {

    protected EditText title, platform, username, password;
    private final static int REQUEST_CODE_PASSWORD = 0;
    protected ImageView icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
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
        if (requestCode == REQUEST_CODE_PASSWORD && resultCode == RESULT_OK) {
            password.setText(intent.getData().toString());
        }
    }

    @Override
    public void onDialogAddClick(DialogFragment dialog, String result) {
        Log.i("Dialog", result);
        this.platform.setText(result);
        Context context = getApplicationContext();
        Drawable iconSrc = IconExtractor.extractIcon(context, result);
        this.icon.setImageDrawable(iconSrc);
        this.platform.clearFocus();
    }

    @Override
    public void onDialogCancelClick(DialogFragment dialog) {
        Log.i("Dialog", "Negative");
        platform.clearFocus();


    }

    @Override
    public void onDialogTouchOutsideClick(DialogFragment dialog) {
        platform.clearFocus();
    }

}
