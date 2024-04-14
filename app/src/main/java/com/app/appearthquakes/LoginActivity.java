package com.app.appearthquakes;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView buttonRegistry;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        AppDatabase appDatabase = Room.databaseBuilder(
                getApplicationContext(),
                AppDatabase.class,
                "dbPruebas"
        ).allowMainThreadQueries().build();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegistry = findViewById(R.id.registry_btn);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                List<User> users = appDatabase.daoUser().getUsers();

                boolean found = false;
                for (User user : users) {
                    if (user.getEmal().equals(email) && user.getPassword().equals(password)) {
                        found = true;
                        break;
                    }
                }

                if (found) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    showToast("Credenciales incorrectas");
                }
            }
        });


        buttonRegistry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                builder.setTitle("Registro de Usuario");
                View view = getLayoutInflater().inflate(R.layout.user_registry_dialog, null);
                builder.setView(view);
                EditText emailEditText = view.findViewById(R.id.emailEditText);
                EditText nameEditText = view.findViewById(R.id.nombreEditText);
                EditText lastNameEditText = view.findViewById(R.id.apellidoEditText);
                EditText passwordEditText = view.findViewById(R.id.claveEditText);
                TextView registryButton = view.findViewById(R.id.registry_dialog_button);
                TextView cancelButton = view.findViewById(R.id.cancel_dialog_button);
                AlertDialog dialog = builder.create();
                dialog.show();

                registryButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String email = emailEditText.getText().toString();
                        String password = passwordEditText.getText().toString();
                        String name = nameEditText.getText().toString();
                        String lastName = lastNameEditText.getText().toString();

                        if (email.isEmpty() || password.isEmpty() || name.isEmpty() || lastName.isEmpty()) {
                            showToast("Por favor, completa todos los campos");
                        } else {
                            User user = new User(email, password, name, lastName);
                            appDatabase.daoUser().insertUser(user);
                            showToast("Usuario registrado exitosamente");
                            dialog.dismiss();
                        }
                    }
                });
                cancelButton.setOnClickListener( it -> dialog.dismiss());

            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
