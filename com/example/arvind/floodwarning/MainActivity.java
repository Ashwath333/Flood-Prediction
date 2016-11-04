package com.example.arvind.floodwarning;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    DatabaseHandler db;

    public MainActivity() {
        this.db = new DatabaseHandler(this);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) C0148R.layout.activity_main);
        Spinner city = (Spinner) findViewById(C0148R.id.spinner);
        city.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList();
        categories.add("Madurai East");
        categories.add("Madurai West");
        categories.add("Madurai North");
        categories.add("Madurai South");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter(this, 17367048, categories);
        dataAdapter.setDropDownViewResource(17367049);
        city.setAdapter(dataAdapter);
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        TextView rain = (TextView) findViewById(C0148R.id.textView3);
        TextView flood = (TextView) findViewById(C0148R.id.textView4);
        int result = -1;
        if (item == "Madurai East") {
            result = this.db.selectDB("1");
            result = 1;
        } else if (item == "Madurai West") {
            result = this.db.selectDB("2");
            result = 0;
        } else if (item == "Madurai South") {
            result = this.db.selectDB("3");
            result = 0;
        } else if (item == "Madurai North") {
            result = this.db.selectDB("4");
            result = 1;
        }
        if (result == 1) {
            rain.setText("Rainfall is High");
            flood.setText("Flood is possible");
        } else if (result == 0) {
            rain.setText("Rainfall is low");
            flood.setText("No flood warning");
        } else {
            rain.setText("NA");
            flood.setText("NA");
        }
        Toast.makeText(parent.getContext(), "Selected: " + item, 1).show();
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
        TextView flood = (TextView) findViewById(C0148R.id.textView4);
        ((TextView) findViewById(C0148R.id.textView3)).setText("NA");
        flood.setText("Flood warning NA");
    }
}
