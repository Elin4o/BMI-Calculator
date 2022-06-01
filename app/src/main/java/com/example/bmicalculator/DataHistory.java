package com.example.bmicalculator;

import androidx.annotation.CallSuper;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class DataHistory extends DBActivity {
    protected ListView simpleList;
    protected Button btnBack,btnDelete;

    protected void FillListView() throws Exception{
        final ArrayList<String> listResults=
                new ArrayList<>();
        SelectSQL(
                "SELECT * FROM BMI ORDER BY Weight",
                null,
                (ID, Weight,Height,BodyMass)-> listResults.add(ID+"   |   \t"+Weight+"     |  \t"+Height+"   | \t"+BodyMass+"\n")
        );
        simpleList.clearChoices();
        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(
                getApplicationContext(),
                R.layout.activity_listview,
                R.id.textView,
                listResults

        );
        simpleList.setAdapter(arrayAdapter);

    }

    private void BackToMain()
    {
        finishActivity(200);
        Intent i = new Intent(DataHistory.this, MainActivity.class);
        startActivity(i);
    }

    @Override
    @CallSuper
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        try
        {
            FillListView();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_history);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        simpleList=findViewById(R.id.simpleList);


        btnBack.setOnClickListener(v -> startActivity(new Intent(DataHistory.this, MainActivity.class)));

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            initDB();
            FillListView();
            AsyncDataGetBMI("GetListOfProjects", "user", "{}",
                    new FillListViewElems() {
                        @Override
                        public void FillListViewWithElements(ArrayList<String> elems) {
                            ArrayAdapter<String> arrayAdapter=new ArrayAdapter<>(
                                    getApplicationContext(),
                                    R.layout.activity_listview,
                                    R.id.textView,
                                    elems

                            );
                            //simpleList.clearChoices();
                            simpleList.setAdapter(arrayAdapter);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
        btnDelete.setOnClickListener(view ->
        {
            try
            {
                ExecSQL("DELETE FROM BMI ",
                        new Object[]{},
                        ()-> Toast.makeText(getApplicationContext(),
                                "Delete Successful", Toast.LENGTH_LONG).show()
                );
            }
            catch (Exception exception)
            {
                Toast.makeText(getApplicationContext(),
                        "Delete Error: "+exception.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
            finally
            {
                BackToMain();
            }
        });
    }
}