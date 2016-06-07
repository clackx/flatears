package flynn.pro.flatears;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceFragmentCompat;


/**
 * A simple {@link Fragment} subclass.
 */
 public class SettingsFragment extends PreferenceFragmentCompat {

    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}
