package com.example.calories;

import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    private Interpreter tflite;
    private final String MODEL_PATH = "calories_model.tflite";

    private Button predictButton;
    private EditText genderEditText;
    private EditText ageEditText;
    private EditText heightEditText;
    private EditText weightEditText;
    private EditText durationEditText;
    private EditText heartRateEditText;
    private EditText bodyTempEditText;
    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize views
        predictButton = findViewById(R.id.predict_button);
        genderEditText = findViewById(R.id.etGender);
        ageEditText = findViewById(R.id.etAge);
        heightEditText = findViewById(R.id.etHeight);
        weightEditText = findViewById(R.id.etWeight);
        durationEditText = findViewById(R.id.etDuration);
        heartRateEditText = findViewById(R.id.etHeartRate);
        bodyTempEditText = findViewById(R.id.etBodyTemperature);
        resultTextView = findViewById(R.id.prediction_result);

        // Load the TensorFlow Lite model
        try {
            MappedByteBuffer tfliteModel = loadModelFile();
            tflite = new Interpreter(tfliteModel);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show();
        }

        // Set click listener for the predict button
        predictButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                predictCaloriesBurnt();
            }
        });
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = getAssets().openFd(MODEL_PATH);
        FileInputStream fileInputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long length = fileDescriptor.getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, length);
    }

    private void predictCaloriesBurnt() {
        // Get user inputs
        String genderStr = genderEditText.getText().toString().trim();
        String ageStr = ageEditText.getText().toString().trim();
        String heightStr = heightEditText.getText().toString().trim();
        String weightStr = weightEditText.getText().toString().trim();
        String durationStr = durationEditText.getText().toString().trim();
        String heartRateStr = heartRateEditText.getText().toString().trim();
        String bodyTempStr = bodyTempEditText.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(genderStr)
                || TextUtils.isEmpty(ageStr)
                || TextUtils.isEmpty(heightStr)
                || TextUtils.isEmpty(weightStr)
                || TextUtils.isEmpty(durationStr)
                || TextUtils.isEmpty(heartRateStr)
                || TextUtils.isEmpty(bodyTempStr)) {
            Toast.makeText(this, "Please enter all the required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int gender = Integer.parseInt(genderStr);
        float age = Float.parseFloat(ageStr);
        float height = Float.parseFloat(heightStr);
        float weight = Float.parseFloat(weightStr);
        float duration = Float.parseFloat(durationStr);
        float heartRate = Float.parseFloat(heartRateStr);
        float bodyTemp = Float.parseFloat(bodyTempStr);

        // Validate inputs
        if (gender < 0 || gender > 1) {
            Toast.makeText(this, "Invalid gender value. Please enter 0 for male or 1 for female.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (age < 15 || age > 80) {
            Toast.makeText(this, "Invalid age value. Please enter an age between 15 and 80.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (height < 140 || height > 210) {
            Toast.makeText(this, "Invalid height value. Please enter a height between 140 and 210 cm.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (weight < 30 || weight > 250) {
            Toast.makeText(this, "Invalid weight value. Please enter a weight between 30 and 250 kg.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duration < 5 || duration > 240) {
            Toast.makeText(this, "Invalid duration value. Please enter a duration between 5 and 240 mins.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (heartRate < 60 || heartRate > 220) {
            Toast.makeText(this, "Invalid heart rate value. Please enter a heart rate between 60 and 220 bpm.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (bodyTemp < 35 || bodyTemp > 41) {
            Toast.makeText(this, "Invalid body temperature value. Please enter a temperature between 35 and 41 °C.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform inference using the TensorFlow Lite model
        float[][] input = {{gender, age, height, weight, duration, heartRate, bodyTemp}};
        float[][] output = new float[1][1];
        tflite.run(input, output);

        // Display the predicted calories burnt
        float predictedCalories = output[0][0];
        String outputText = "Calories burnt: " + predictedCalories;
        resultTextView.setText(outputText);
    }
}
