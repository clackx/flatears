package flynn.pro.flatears;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * Created by claqx on 24.06.16.
 */
public class CallPlayer extends Activity
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    public static final String DEFAULT_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getPath()+"/FLATEARS/";
    public static final String BASE_PATH = "content://flynn.pro.flatears/records";
    private static final String TAG = "CALLPLYR";
    private AudioPlayerControl aplayer = null;
    private MediaController controller = null;
    private ViewGroup anchor = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getIntent();
        String name = i.getData().getEncodedPath();
        String path = DEFAULT_STORAGE_LOCATION + name;

        setContentView(R.layout.player_activity);
        anchor = (ViewGroup) findViewById(R.id.playerlayout);

        TextView tvNum = (TextView) findViewById(R.id.txtNum);
        TextView tvDat = (TextView) findViewById(R.id.txtDate);
        TextView tvSrc = (TextView) findViewById(R.id.txtSource);
        TextView tvFrm = (TextView) findViewById(R.id.txtFormat);
        TextView tvDur = (TextView) findViewById(R.id.txtDetails);
        TextView tvDrc = (TextView) findViewById(R.id.txtDirect);

        String where = RecordingProvider.KEY_LINK + " = '" + name + "'";
        Cursor mCursor = getApplicationContext().getContentResolver().query(Uri.parse(BASE_PATH), null, where, null, null);

        String cDat="", cNum="", cDur = "", cLT = "", cTyp="", cSrc="", cFrm="";

        if (null == mCursor) {

            // If the Cursor is empty, the provider found no matches
        } else if (mCursor.getCount() < 1) {


        } else {
            mCursor.moveToLast();
            cDat = mCursor.getString(2)+"  "+mCursor.getString(3);
            cNum = mCursor.getString(6);
            cDur = mCursor.getString(7);
            cLT = mCursor.getString(1);
            cTyp = mCursor.getString(4);
            cSrc = mCursor.getString(8);
            cFrm = mCursor.getString(9);
        }

        if (cLT != "") {
            SimpleDateFormat sdf_date = new SimpleDateFormat("E, dd MMM HH:mm");
            Date nDate = new Date(Long.parseLong(cLT));
            cDat = sdf_date.format(nDate);
        }

        if (Objects.equals(cNum, "")) {cNum = " неизвестный";}
        if (Objects.equals(cTyp, "")) {cTyp = "   unknown"; cDur = ""+(Integer.parseInt("0"+cDur)/1000);}

        //String s= cNum+" "+cTyp+" \n"+cDat+"   ( "+cDur+" сек. )";
        //SpannableString ss1=  new SpannableString(s);
        //ss1.setSpan(new RelativeSizeSpan(1.2f), 0, 12, 0); // set size
        // set color
        if (cTyp == null) {cTyp ="";}
        switch (cTyp) {
            case "OUTGOING":
                cTyp = "ИСХОДЯЩИЙ";
                //ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Forest_Green)), 0, 12, 0);
                break;
            case "INCOMING":
                cTyp = "ВХОДЯЩИЙ";
                //ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Blue_Ribbon)), 0, 12, 0);
                break;
            case "MISSED":
                cTyp = "ПРОПУЩЕННЫЙ";
                //ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Love_Red)), 0, 12, 0);
                break;
            default:
                cTyp = "НЕОПРЕДЕЛЕННЫЙ";
                //ss1.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.Steel_Blue)), 0, 12, 0);
                break;
        }

        tvDrc.setText(cTyp);
        tvNum.setText(cNum);
        tvDat.setText(cDat);
        tvSrc.setText("Источник : " + cSrc);
        tvFrm.setText("Формат : " + cFrm);
        tvDur.setText("Продолжительность: " + cDur + "c");

        if (aplayer != null) {
            Log.i(TAG, "CallPlayer onCreate called with aplayer already allocated, destroying old one.");
            aplayer.destroy();
            aplayer = null;
        }
        if (controller != null) {
            Log.i(TAG, "CallPlayer onCreate called with controller already allocated, destroying old one.");
            controller = null;
        }

        Log.i(TAG, "CallPlayer onCreate with data: " + path);
        try {
            aplayer = new AudioPlayerControl(path, this);

            // creating the controller here fails.  Have to do it once our onCreate has finished?
            // do it in the onPrepared listener for the actual MediaPlayer
        } catch (java.io.IOException e) {
            Log.e(TAG, "CallPlayer onCreate failed while creating AudioPlayerControl", e);
            Toast t = Toast.makeText(this, "CallPlayer received error attempting to create AudioPlayerControl: " + e, Toast.LENGTH_LONG);
            t.show();
            finish();
        }
    }

    public void onDestroy() {
        Log.i(TAG, "CallPlayer onDestroy");
        if (aplayer != null) {
            aplayer.destroy();
            aplayer = null;
        }
        super.onDestroy();
    }

    private class MyMediaController extends MediaController {
        public MyMediaController(Context c, boolean ff) {
            super(c, ff);
        }

        /*
        public int mTimeout = 0;

        @Override
        public void show() {
            show(mTimeout);
        }

        @Override
        public void show(int timeout) {
            super.show(mTimeout);
        }

        @Override
        public void hide() {
            // Do not hide until a timeout is set
            if (mTimeout > 0) super.hide();
        }

        public void hideActually() {
            super.hide();
        }
        */

        // somehow, this causes the activity to be un-exitable
        //public void hide() {
        //    Log.d(TAG, "MyMediaController turning hide() into no-op");
        //}

        // but we can't do this because we don't have access to the mHandler object
        //public void show(int timeout) {
        //    super.show(timeout);
        //    // never auto disappear
        //    mHandler.removeMessages(FADE_OUT);
        //}
    }

    // MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "CallPlayer onPrepared about to construct MediaController object");
        controller = new MediaController(this, true); // enble fast forward
        //controller = new MyMediaController(this, true); // enble fast forward
        //controller = new MediaController(getApplicationContext()); // why is useing 'this' different than 'getApplicationContext()' ?

        controller.setMediaPlayer(aplayer);
        controller.setAnchorView(anchor);
        controller.setEnabled(true);
        controller.show(aplayer.getDuration()); //aplayer.getDuration());

        // controller disappears after 3 seconds no matter what... set timer to handle re-showing it?
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "CallPlayer onInfo with what " + what + " extra " + extra);
        return false;
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "CallPlayer onError with what " + what + " extra " + extra);
        Toast t = Toast.makeText(this, "CallPlayer received error (what:" + what + " extra:" + extra + ") from MediaPlayer attempting to play ", Toast.LENGTH_LONG);
        t.show();
        finish();
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "CallPlayer onCompletion, finishing activity");
        finish();
    }

/*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.v(TAG, "+++ onBackPressed start +++");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // write your code here
            Log.i(TAG, "CallPlayer onDestroy");
            if (aplayer != null) {
                aplayer.destroy();
                aplayer = null;
            }
            super.onDestroy();
        }
        return true;
    }
*/

/*
    @Override
    public void onBackPressed() {
        Log.v(TAG, "=== onBackPressed start ===");
        super.onDestroy();
        Log.v(TAG, "=== onBackPressed end ===");
    }
*/
}
