package com.cs407.seesafe;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class TermsAndConditionsActivity extends AppCompatActivity {

    private Button proceedButton;
    private TextToSpeech tts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_and_conditions);

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.speak("By using this feature, you agree to the following terms: " +
                        "1. This service is exclusively for individuals with visual impairments. " +
                        "2. Be respectful to the volunteers offering help. " +
                        "3. Do not use inappropriate language or behavior during the call. " +
                        "4. Misuse of this feature may result in the termination of access. " +
                        "5. The service is provided as a free offering by volunteers. " +
                        "6. This feature should not be used for emergencies. " +
                        "7. Ensure a stable internet connection for effective communication. " +
                        "8. Volunteers are not responsible for solving complex problems. " +
                        "9. Do not share personal or sensitive information during the call. " +
                        "10. Calls may be monitored or reviewed for quality assurance. " +
                        "11. SeeSafe reserves the right of final interpretation.", TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });

        proceedButton = findViewById(R.id.proceedButton);

        // Handle Proceed button click
        proceedButton.setOnClickListener(v -> {
            if (tts.isSpeaking()) {
                tts.stop(); // Stops the current speech
            }
            // Navigate to the Blind User Activity
            Intent intent = new Intent(TermsAndConditionsActivity.this, BlindUserActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
