package flynn.pro.flatears;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

/**
 * Created by claqx on 24.06.16.
 */
public class CallPlayer     extends Activity
        implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener
{
    private static final String TAG = "CallRecorder";
    private AudioPlayerControl aplayer = null;
    private MediaController controller = null;
    private ViewGroup anchor = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_activity);

        anchor = (ViewGroup)findViewById(R.id.playerlayout);

        if (aplayer != null) {
            Log.i(TAG, "CallPlayer onCreate called with aplayer already allocated, destroying old one.");
            aplayer.destroy();
            aplayer = null;
        }
        if (controller != null) {
            Log.i(TAG, "CallPlayer onCreate called with controller already allocated, destroying old one.");
            controller = null;
        }

        Intent i = getIntent();
        String path = i.getData().getEncodedPath();

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

    private class MyMediaController extends MediaController
    {
        public MyMediaController(Context c, boolean ff) {
            super(c, ff);
        }

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
    public void onPrepared(MediaPlayer mp)
    {
        Log.i(TAG, "CallPlayer onPrepared about to construct MediaController object");
        controller = new MediaController(this, true); // enble fast forward
        //controller = new MyMediaController(this, true); // enble fast forward
        //controller = new MediaController(getApplicationContext()); // why is useing 'this' different than 'getApplicationContext()' ?

        controller.setMediaPlayer(aplayer);
        controller.setAnchorView(anchor);
        controller.setEnabled(true);
        controller.show(0); //aplayer.getDuration());

        // controller disappears after 3 seconds no matter what... set timer to handle re-showing it?
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra)
    {
        Log.i(TAG, "CallPlayer onInfo with what " + what + " extra " + extra);
        return false;
    }

    public boolean onError(MediaPlayer mp, int what, int extra)
    {
        Log.e(TAG, "CallPlayer onError with what " + what + " extra " + extra);
        Toast t = Toast.makeText(this, "CallPlayer received error (what:" + what + " extra:" + extra + ") from MediaPlayer attempting to play ", Toast.LENGTH_LONG);
        t.show();
        finish();
        return true;
    }

    public void onCompletion(MediaPlayer mp)
    {
        Log.i(TAG, "CallPlayer onCompletion, finishing activity");
        finish();
    }
}
