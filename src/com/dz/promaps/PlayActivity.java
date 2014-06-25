package com.dz.promaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.android.gms.ads.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dz on 25.04.14.
 */
public class PlayActivity extends Activity{

    private final String[] dataLitrs = {"<5 литров", "<10 литров", "<15 литров", "<20 литров", "<25 литров"};
    private final String[] dataAI = {"AI95", "AI92"};
    private Double MAX_DISTANCE;
    private Double RESIDUE=5.0;
    private String MARKA="AI95";

    private AdView adView;//создание баннера рекламы

    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    TextView score_text;
    Spinner spinner;
    Spinner spinner1;
    EditText expense;
    Button button_find;

//инициализация для микрофона
    protected static final int RESULT_SPEECH = 1;

    private ImageButton btnSpeak;
    private TextView txtText;
    private String phrase;

    Map<String, Integer> RESULT_FROM_SPEECH = new HashMap<String, Integer>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_xml);

    init();
    initFirstAdapter();
    initSecondAdapter();



    }

    protected void onStart(){
        super.onStart();
        Log.i("PRINT", "ONStART!");
    }
    //Проверяем подключение к интернету
    public boolean isOnline() {
        ConnectivityManager c = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = c.getActiveNetworkInfo();
        if (nInfo != null && nInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }


    //инициализируем адаптер с выбором остатка топлива
    private void initFirstAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataLitrs);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Title");
        // выделяем элемент
        spinner.setSelection(0);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // выбираем позицию нажатого элемента и меняем остаток топлива
                switch (position){
                    case 0: RESIDUE = 5.0;  break;
                    case 1: RESIDUE = 10.0;  break;
                    case 2: RESIDUE = 15.0; break;
                    case 3: RESIDUE = 20.0; break;
                    case 4: RESIDUE = 25.0; break;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {//если ничего не выбрано
            }
        });
    }

    private void init(){//инициализируем все поля

        score_text = (TextView) findViewById(R.id.expense);
        spinner = (Spinner) findViewById(R.id.spinner);
        spinner1 = (Spinner) findViewById(R.id.spinnerAI);

        expense = (EditText) findViewById(R.id.expense);//инициализируем средний расход на 100 км

        preferences = getSharedPreferences("mySettings",Context.MODE_PRIVATE);
        editor = preferences.edit();

        String count_fuel = preferences.getString("expense","");
        if(!count_fuel.equalsIgnoreCase(""))
        {
            expense.setText(count_fuel.toString());
        }
        else {
            expense.setText("10");
        }

        expense.setInputType(InputType.TYPE_CLASS_NUMBER);

        button_find = (Button) findViewById(R.id.buttong_find);
        button_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(expense.getText().length() != 0 && Double.valueOf(expense.getText().toString()) != 0){

                //передача данных пользователя в maps
                MAX_DISTANCE = RESIDUE*100.0/Double.valueOf(expense.getText().toString());

                Intent intent = new Intent(PlayActivity.this, MyActivity.class);
                intent.putExtra("distance",MAX_DISTANCE.toString());
                intent.putExtra("marka",MARKA);

                    if(isOnline()) startActivity(intent);
                        else Toast.makeText(getApplicationContext(),
                            "Проверьте подключение интернета", Toast.LENGTH_LONG).show();

                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Введите средний расход топлива", Toast.LENGTH_LONG).show();
                }

                editor.putString("expense", expense.getText().toString());
                editor.commit();

            }
        });


        txtText = (TextView) findViewById(R.id.text);

        btnSpeak = (ImageButton) findViewById(R.id.btn_speak);
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(
                        RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    String phrase = "";
                    txtText.setText(phrase);
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }
            }
        });
    }


    //инициализируем адаптер с выбором марки бензина
    private void initSecondAdapter(){

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, dataAI);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        spinner1.setAdapter(adapter);
        // заголовок
        spinner1.setPrompt("Title");

        // выделяем элемент
        spinner1.setSelection(0);


        // устанавливаем обработчик нажатия
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // меняем марку бензина в зависимости от позиции в адаптере
                if(position == 1) MARKA = "AI92";
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    //Запись с микрофона
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SPEECH: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    phrase = text.get(0);
                    GetInfoText(phrase);
                    txtText.setText(phrase);

                    for(Map.Entry entry : RESULT_FROM_SPEECH.entrySet())
                        System.out.println("KEY == " + entry.getKey() + " VALUE == " + entry.getValue());
                }
                break;
            }
        }
    }

    private void GetInfoText(String phrase){

        String s[] = phrase.split(" ");

        for (int i = 0; i < s.length; i++){

            if(s[i].equals("95"))
            {
                RESULT_FROM_SPEECH.put("бензин",95);
                spinner.setSelection(0);
            }

            if(s[i].equals("92"))
            {
                RESULT_FROM_SPEECH.put("бензин",92);
                spinner1.setSelection(1);
            }

            if(s[i].equals("остаток") && Character.isDigit(s[i+1].charAt(0)) && i+1 <= s.length) {
                if(Integer.parseInt(s[i+1]) > 0  && Integer.parseInt(s[i+1]) <= 50)
                {
                    RESULT_FROM_SPEECH.put("остаток", Integer.parseInt(s[i+1]));

                    if(Integer.parseInt(s[i+1]) <= 25)
                        spinner.setSelection(4);
                    else
                        spinner.setSelection(4);

                    if(Integer.parseInt(s[i+1]) <= 20)
                         spinner.setSelection(3);

                    if(Integer.parseInt(s[i+1]) <= 15)
                        spinner.setSelection(2);

                    if(Integer.parseInt(s[i+1]) <= 10)
                        spinner.setSelection(1);

                    if(Integer.parseInt(s[i+1]) <= 5)
                        spinner.setSelection(0);
                }
            }

            if(s[i].equals("расход") && Character.isDigit(s[i+1].charAt(0)) && i+1 <= s.length) {
                if(Integer.parseInt(s[i+1]) > 0  && Integer.parseInt(s[i+1]) <= 50)
                {
                    RESULT_FROM_SPEECH.put("расход", Integer.parseInt(s[i+1]));
                    expense.setText(s[i+1]);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return super.onCreateOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret;

        if (item.getItemId() == R.id.action_estimate) {
            // Handle Settings
            ret = true;

            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.dz.promaps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);

        } else {
            ret = super.onOptionsItemSelected(item);
        }
        if (item.getItemId() == R.id.action_question) {
            // Handle Settings
            ret = true;

//Окно информации
            String message = "В приложении доступно голосовое управление,\n" +
                    "для ввода команды нужно:\n\n" +
                    "1.Нажать микрофон внизу экрана\n"+
                    "2.Произнести фразу в виде:\n" +
                    "\"бензин [марка] остаток [кол-во] литров расход [кол-во] литров на 100 км\"";

            final AlertDialog.Builder infodialog = new AlertDialog.Builder(this);
            infodialog.setTitle("Голосовой ввод");
            infodialog.setMessage(message);
            infodialog.create();
            infodialog.show();

        } else {
            ret = super.onOptionsItemSelected(item);
        }
        return ret;
    }
}
