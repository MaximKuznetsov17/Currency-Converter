package com.example.malsi.firstapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private String inputCurrency = "USD";
    private String outputCurrency = "RUB";
    private List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uploadItems();
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("inputCurrency", inputCurrency);
        outState.putString("outputCurrency", outputCurrency);
        outState.putStringArrayList("items", (ArrayList<String>) items);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        inputCurrency = savedInstanceState.getString("inputCurrency");
        outputCurrency = savedInstanceState.getString("outputCurrency");

        items.clear();
        items.addAll(savedInstanceState.getStringArrayList("currencies"));
    }

    private void uploadItems() {
        if (items.isEmpty()) {
            GetItemsTask getItemsTask = new GetItemsTask();
            getItemsTask.execute();
        }
    }

    private void fillSpinners() {
        Spinner inputSpinner = findViewById(R.id.inputCurrency);
        Spinner outputSpinner = findViewById(R.id.outputCurrency);

        ArrayAdapter adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout
                .simple_spinner_dropdown_item);
        inputSpinner.setAdapter(adapter);
        inputSpinner.setSelection(adapter.getPosition(inputCurrency));
        outputSpinner.setAdapter(adapter);
        outputSpinner.setSelection(adapter.getPosition(outputCurrency));
    }

    public void conversion(View view) {
        ConversionTask conversionTask = new ConversionTask();
        conversionTask.execute();
    }

    public class GetItemsTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String response) {
            if (response == null) {
                Toast.makeText(getApplicationContext(),
                        "Неполадки с интернет-соединением!", Toast.LENGTH_SHORT).show();
            } else {
                List<String> values = new ArrayList<>();
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    object = object.getJSONObject("results");

                    for (Iterator<?> iterator = object.keys(); iterator.hasNext(); ) {
                        String key = (String) iterator.next();
                        values.add(key);
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(),
                            "Невозможно распарсить формат JSON", Toast.LENGTH_LONG).show();
                }
                items.clear();
                items.addAll(values);
            }
            fillSpinners();
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL("https://free.currencyconverterapi.com/api/v6/currencies");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    return bufferedReader.readLine();
                }
                catch (Exception e) {
                    throw e;
                }
                finally {
                    urlConnection.disconnect();
                }
            }
            catch (Exception e) {
                return null;
            }
        }
    }


    public class ConversionTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response == null) {
                Toast.makeText(getApplicationContext(),
                        "Неполадки с интернет-соединением!", Toast.LENGTH_SHORT).show();
            } else {
                Float value;
                try {
                    JSONObject object = (JSONObject) new JSONTokener(response).nextValue();
                    Spinner inputCur = findViewById(R.id.inputCurrency);
                    String currencyFrom = inputCur.getSelectedItem().toString();
                    inputCurrency = currencyFrom;

                    Spinner outputCur = findViewById(R.id.outputCurrency);
                    String currencyTo = outputCur.getSelectedItem().toString();
                    outputCurrency = currencyTo;

                    value = Float.valueOf(object.getString(currencyFrom + "_" + currencyTo));
                    EditText editText = findViewById(R.id.inputMoney);
                    Float inputMoney = Float.valueOf(editText.getText().toString());
                    value *= inputMoney;
                } catch (JSONException e) {
                    value = new Float(0);
                } catch (NumberFormatException e) {
                    value = new Float(0);
                    Toast.makeText(getApplicationContext(), "Проверьте поле ввода!", Toast.LENGTH_SHORT).show();
                }
                TextView resultTextView = findViewById(R.id.outputMoney);
                resultTextView.setText(value.toString());
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            uploadItems();
            try {
                Spinner inputCurrency = findViewById(R.id.inputCurrency);
                String currencyFrom = inputCurrency.getSelectedItem().toString();
                Spinner outputCurrency = findViewById(R.id.outputCurrency);
                String currencyTo = outputCurrency.getSelectedItem().toString();

                URL url = new URL("https://free.currencyconverterapi.com/api/v6/convert?q=" + currencyFrom + "_" + currencyTo + "&compact=ultra");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                return bufferedReader.readLine();
            }
            catch (Exception e) {
                return null;
            }
        }
    }
}
