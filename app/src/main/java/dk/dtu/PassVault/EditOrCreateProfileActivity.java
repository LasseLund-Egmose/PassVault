package dk.dtu.PassVault;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;


public class EditOrCreateProfileActivity extends BaseActivity {

    private EditText mEditText, mEditText1, mEditText2, mEditText3;
    public static int RESULT_LOAD_IMAGE = 1;

    private EditText mEditText, mEditText1, mEditText2, mEditText3;
    public static int RESULT_LOAD_IMAGE = 1;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_add);

        mEditText  = (EditText) findViewById(R.id.editView);
        mEditText1 = (EditText) findViewById(R.id.editView1);
        mEditText2 = (EditText) findViewById(R.id.editView2);
        mEditText3 = (EditText) findViewById(R.id.editView3);




        Button createButton = findViewById(R.id.button5);
        createButton.setOnClickListener(new OnClickListener() {

        @Override
            public void onClick(View v) {
                createClicked();
            }
        });

                Button genBtn = (Button) findViewById(R.id.generate_password);
        
        genBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),PasswordGeneratorActivity.class);
            startActivity(intent);
        });

        ImageButton editLogo = (ImageButton)findViewById(R.id.button2);
        editLogo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

            }




        });


        }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private void createClicked(){

        String title = mEditText1.getText().toString();
        String url = mEditText2.getText().toString();
        String username = mEditText.getText().toString();
        String password = mEditText3.getText().toString();

        Intent i = new Intent(getApplicationContext(), WalletActivity.class);
        i.putExtra("title", title);
        i.putExtra("url",url);
        i.putExtra("username",username);
        i.putExtra("password", password);

        setResult(RESULT_OK,i);
        finish();

    }
}
