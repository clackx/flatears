package flynn.pro.flatears;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    private Handler thandler = new Handler(Looper.getMainLooper());

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
                Log.d("TIMERHNDLR", "Процедура выгрузки инициирована по таймеру");

                Context c = getActivity().getBaseContext();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
                String serverip = prefs.getString(PrefsFragment.PREF_SERVER_IP, "1");
                FTPUploader._uploadall(serverip);
            }

            //DetectConnection.checkInternetConnection(context);
            // :: Pause ten second to next connection check
            thandler.postDelayed(this, 60000);
        }
    };

}
