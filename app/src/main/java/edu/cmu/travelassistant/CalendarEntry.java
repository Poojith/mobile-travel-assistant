package edu.cmu.travelassistant;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by TortugaDeVaio on 10-May-17.
 */

public class CalendarEntry extends Activity {

    public CalendarEntry() {
        Log.e("aaa", "aaaa");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("aaa", "aaaa");
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null)
            {
               ; //Cry about not being clicked on
            }
            else if (extras.getBoolean("NotiClick"))
            {
                ;//Do your stuff here mate :)
            }

        }
        super.onCreate(savedInstanceState);
        Calendar cal = Calendar.getInstance();
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra("beginTime", cal.getTimeInMillis());
        intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
        intent.putExtra("title", "To catch 61D from XYZ, you must start moving now");
        startActivity(intent);
    }
}
