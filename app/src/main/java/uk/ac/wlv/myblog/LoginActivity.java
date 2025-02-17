package uk.ac.wlv.myblog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import uk.ac.wlv.myblog.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    ActivityLoginBinding binding;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        databaseHelper = new DatabaseHelper(this);
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = binding.loginEmail.getText().toString();
                String password = binding.loginPassword.getText().toString();

                if (email.equals("")||password.equals("")) {
                    Toast.makeText(LoginActivity.this,
                            "All fields are mandatory", Toast.LENGTH_SHORT).show();
                } else {
                    Boolean checkCredentials = databaseHelper.checkEmailPassword(email, password);
                    if (checkCredentials == true) {
                        Toast.makeText(LoginActivity.this,
                                "Login Successfully!", Toast.LENGTH_SHORT).show();
                        binding.loginEmail.getText().clear();
                        binding.loginPassword.getText().clear();
                        int userId = databaseHelper.getUserId(email, password);

                        // Pass user ID to MainActivity
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        binding.signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

    }

}