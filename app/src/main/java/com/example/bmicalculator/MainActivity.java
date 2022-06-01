package com.example.bmicalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.text.DecimalFormat;

public class MainActivity extends DBActivity {
    EditText editWeight,editHeight;
    Button btnCalculate,btnToData;
    TextView txtBMI;
    double result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editHeight = findViewById(R.id.editHeight);
        editWeight = findViewById(R.id.editWeight);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnToData = findViewById(R.id.btnToData);
        txtBMI = findViewById(R.id.txtBMICalculate);

        try {
            initDB();
        } catch (Exception e) {
            e.printStackTrace();
        }

        btnToData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, DataHistory.class));
            }
        });

        btnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(editHeight.getText().toString())||TextUtils.isEmpty(editWeight.getText().toString()))
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Empty input", Toast.LENGTH_SHORT);
                    toast.show();
                }
                else {
                    result = Double.parseDouble(editWeight.getText().toString()) / ((Double.parseDouble(editHeight.getText().toString()) * 0.01) * (Double.parseDouble(editHeight.getText().toString()) * 0.01));
                    String textResult = String.valueOf(result);
                    textResult = String.format("%.2f", result);
                    txtBMI.setText(textResult);

                    try {
                        ExecSQL(
                                "INSERT INTO BMI(Weight, Height, BodyMass) " +
                                        "VALUES(?, ?, ?) ",
                                new Object[]{
                                        editWeight.getText().toString(),
                                        editHeight.getText().toString(),
                                        txtBMI.getText().toString(),
                                },
                                () -> Toast.makeText(getApplicationContext(),
                                        "Record Inserted", Toast.LENGTH_LONG).show()

                        );
                        //Synchron
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final StringBuilder jsonObject = new StringBuilder();
                                jsonObject.append("{");
                                jsonObject.append("'username': '" + txtBMI.getText().toString() + "', ");
                                jsonObject.append("'weight': '" + editWeight.getText().toString() + "', ");
                                jsonObject.append("'height': '" + editHeight.getText().toString() + "', ");
                                jsonObject.append("'BodyMass': '" + txtBMI.getText().toString() + "' ");
                                jsonObject.append("}");

                                final StringBuilder result = new StringBuilder();

                                try {
                                    result.append(postData("SaveToFile",
                                            txtBMI.getText().toString(),
                                            jsonObject.toString()
                                    ));

                                    JSONObject jo = (JSONObject) new JSONTokener(result.toString()).nextValue();
                                    final String message = jo.getString("message");

                                    if (message == null) {
                                        throw new Exception("SERVER FAULT: " + result.toString());
                                    }

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        }
                                    });

                                } catch (final Exception e) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),
                                                    "Exception: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }

                            }
                        });
                        t.start();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(),
                                "Insert Failed: " + e.getLocalizedMessage()
                                , Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}