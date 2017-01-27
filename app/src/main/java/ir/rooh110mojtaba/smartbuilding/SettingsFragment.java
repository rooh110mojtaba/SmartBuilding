package ir.rooh110mojtaba.smartbuilding;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Mojtaba on 1/22/2017.
 */
public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
