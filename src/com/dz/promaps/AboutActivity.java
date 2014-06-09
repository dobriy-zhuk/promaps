package com.dz.promaps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.*;

/**
 * Created by dz on 25.04.14.
 */
public class AboutActivity extends Activity{

     @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_xml);

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
                    "1.Нажать микрофон в главном меню\n"+
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
