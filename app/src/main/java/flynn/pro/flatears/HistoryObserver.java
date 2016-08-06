package flynn.pro.flatears;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.*;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by claqx on 22.06.16.
 */
@SuppressLint("SimpleDateFormat")
public class HistoryObserver extends ContentObserver {

    Context context;
    public static final String BASE_PATH = "content://flynn.pro.flatears/records";

    public HistoryObserver(Handler handler, Context cc) {
        // TODO Auto-generated constructor stub
        super(handler);
        context = cc;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        // TODO Auto-generated method stub
        super.onChange(selfChange);

        getCalldetailsNow();
    }


    private void getCalldetailsNow() {
        // TODO Auto-generated method stub

        Cursor managedCursor = context.getContentResolver().
                query(android.provider.CallLog.Calls.CONTENT_URI, null, null, null, android.provider.CallLog.Calls.DATE + " DESC");

        int number = managedCursor.getColumnIndex(android.provider.CallLog.Calls.NUMBER);
        int duration1 = managedCursor.getColumnIndex(android.provider.CallLog.Calls.DURATION);
        int type1 = managedCursor.getColumnIndex(android.provider.CallLog.Calls.TYPE);
        int date1 = managedCursor.getColumnIndex(android.provider.CallLog.Calls.DATE);

        if (managedCursor.moveToFirst() == true) {
            String phNumber = managedCursor.getString(number);
            String callDuration = managedCursor.getString(duration1);

            String type = managedCursor.getString(type1);
            String date = managedCursor.getString(date1);

            String dir = null;
            int dircode = Integer.parseInt(type);
            switch (dircode) {
                case android.provider.CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case android.provider.CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case android.provider.CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
                default:
                    dir = "MISSED";
                    break;
            }

            if (dir != "MISSED") {
                SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM.yyyy");
                SimpleDateFormat sdf_time = new SimpleDateFormat("HH:mm");

                String dateString = sdf_date.format(new Date(Long.parseLong(date)));
                String timeString = sdf_time.format(new Date(Long.parseLong(date)));

                ContentValues cv = new ContentValues();
                cv.put(RecordingProvider.KEY_CALLTYPE, dir);
                cv.put(RecordingProvider.KEY_BNUM, phNumber);
                cv.put(RecordingProvider.KEY_DURATION, callDuration);
                cv.put(RecordingProvider.KEY_DATE, dateString);
                cv.put(RecordingProvider.KEY_TIME, timeString);

                Cursor mCursor = context.getContentResolver().query(Uri.parse(BASE_PATH), null, null, null, null);
                mCursor.moveToLast();
                int id = mCursor.getInt(mCursor.getColumnIndex("_id"));
                String where = RecordingProvider.KEY_ID + " = " + id;
                int ex = context.getContentResolver().update(RecordingProvider.CONTENT_URI, cv, where, null);

                if (ex != 0) {
                    Log.e("HISTOBSRVR", "Ошибка обновления данных таблицы записей");
                }
            }
        }
        managedCursor.close();
    }
}
