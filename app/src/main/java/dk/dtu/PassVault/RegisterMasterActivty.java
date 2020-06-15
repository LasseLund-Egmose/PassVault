package dk.dtu.PassVault;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;


public class RegisterMasterActivty extends AppCompatActivity{
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_register_master);
            ProgressBar pb = (ProgressBar) findViewById(R.id.progressbar);
            pb.setProgress(40);
            pb.setProgressDrawable(getDrawable(R.drawable.pb_drawable_yellow));

        }


        public void passwordStrengthCheck(){

        }
    }

