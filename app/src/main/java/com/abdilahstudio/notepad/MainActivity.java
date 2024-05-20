package com.abdilahstudio.notepad;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "notepad_prefs";
    private static final String SWITCH_STATE_KEY = "switch_state";
    private EditText editTextNote;
    private TextView textViewNotes;
    private SharedPreferences sharedPreferences;
    private Switch switchToolbar;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    Menu menu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextNote = findViewById(R.id.editTextNote);
        textViewNotes = findViewById(R.id.textViewNotes);
        Button buttonSave = findViewById(R.id.buttonSave);

        // definisi shared preference
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // load swith status
        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);

        // kunci  jika swith aktif
        if (switchState) {
            kunci();
            Toast.makeText(this, "Kunci aktif", Toast.LENGTH_SHORT).show();
        }

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        displayNotes();

        // inisialisasi dan atur action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // mengatur judul action bar
        getSupportActionBar().setTitle("NOTEPAD");
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        // kunci jika swith aktif
        if (switchState) {
            kunci();
            Toast.makeText(this, "Kunci aktif", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem switchItem = menu.findItem(R.id.switch_item);
        switchToolbar = switchItem.getActionView().findViewById(R.id.switch_toolbar);

        boolean switchState = sharedPreferences.getBoolean(SWITCH_STATE_KEY, false);
        switchToolbar.setChecked(switchState);

        // set event swith
        switchToolbar.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(SWITCH_STATE_KEY, isChecked);
            editor.apply();

            String message = isChecked ? "Kunci aktif" : "Kunci nonaktif";
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        });
        return true;
    }

    private void saveNote(){
        String note = editTextNote.getText().toString().trim();

        // simpan catatan ke sharedpreference
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("note", note);
        editor.apply();

        editTextNote.setText("");
        displayNotes();
    }

    private void displayNotes(){
        String savedNote = sharedPreferences.getString("note", "");
        textViewNotes.setText("Catatan :\n" + savedNote);
    }

    private void kunci(){
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    // Handle user cancellation by closing the app
                    finish();
                } else {
                    // Handle other errors
                    Toast.makeText(getApplicationContext(), "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                bukaKunci();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(MainActivity.this, "Gagal membuka kunci", Toast.LENGTH_SHORT).show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Buka Aplikasi")
                .setSubtitle("Gunakan kunci keamanan pada ponsel anda")
                .setNegativeButtonText("Batal")
                .build();

        // menampilkan promt saat aplikasi dibuka
        biometricPrompt.authenticate(promptInfo);
    }

    private void bukaKunci() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Aplikasi terbuka!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}