package com.example.bmicalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;



import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBActivity extends RESTActivity {
    protected interface OnQuerySuccess{
        public void OnSuccess();
    }
    protected interface OnSelectSuccess{
        public void OnElementSelected(
                String ID, String Weight, String Height, String BodyMass
        );
    }

    protected boolean matchString(String string_, String regexp){
        final String regex = regexp;
        final String string = string_;

        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(string);

        while (matcher.find()) {
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    protected void SelectSQL(String SelectQ,
                             String[] args,
                             OnSelectSuccess success
    )
            throws Exception
    {
        SQLiteDatabase db=SQLiteDatabase
                .openOrCreateDatabase(getFilesDir().getPath()+"/BMI.db", null);
        Cursor cursor=db.rawQuery(SelectQ, args);
        while (cursor.moveToNext()){
            String ID=cursor.getString(cursor.getColumnIndex("ID"));
            String Weight=cursor.getString(cursor.getColumnIndex("Weight"));
            String Height=cursor.getString(cursor.getColumnIndex("Height"));
            String BodyMass=cursor.getString(cursor.getColumnIndex("BodyMass"));
            success.OnElementSelected(ID, Weight, Height, BodyMass);
        }
        db.close();
    }

    protected void ExecSQL(String SQL, Object[] args, OnQuerySuccess success)
            throws Exception
    {
        SQLiteDatabase db=SQLiteDatabase
                .openOrCreateDatabase(getFilesDir().getPath()+"/BMI.db", null);
        if(args!=null)
            db.execSQL(SQL, args);
        else
            db.execSQL(SQL);

        db.close();
        success.OnSuccess();
    }
    protected void initDB() throws  Exception{
        ExecSQL(
                "CREATE TABLE if not exists BMI( " +
                        "ID integer PRIMARY KEY AUTOINCREMENT, " +
                        "Weight text not null, " +
                        "Height text not null, " +
                        "BodyMass text not null " +
                        ")",
                null,
                ()-> Toast.makeText(getApplicationContext(),
                        "DB Init Successful", Toast.LENGTH_LONG).show()

        );
    }


}
