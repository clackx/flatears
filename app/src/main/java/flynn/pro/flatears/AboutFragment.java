package flynn.pro.flatears;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private Handler thandler = new Handler();

    public static AboutFragment newInstance() {
        AboutFragment fragment = new AboutFragment();
        return fragment;
    }


    public AboutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        runnable.run();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    private  Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (NetworkUtil.allowUpload) {
                Log.d("TIMERHANDLER ::", "UPLOAD ALL NOW!!");
                FTPUploader._uploadall();
            }
            // :: make some noise
            //Toast.makeText(AboutFragment.this, "Tick!!! - TUCK !!", Toast.LENGTH_LONG).show();
            //CallLog cl = CallLog.xx;
            //DetectConnection.checkInternetConnection(context);
            // :: Pause ten second to next connection check
            thandler.postDelayed(this, 60000);
        }
    };

}
