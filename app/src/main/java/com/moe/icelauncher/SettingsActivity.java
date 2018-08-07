package com.moe.icelauncher;
import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.preference.PreferenceScreen;
import android.app.AlertDialog;
import android.content.DialogInterface;
import com.moe.icelauncher.util.DeviceManager;
import android.content.Intent;
import android.net.Uri;

public class SettingsActivity extends Activity
{
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new LauncherSettingsFragment())
			.commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class LauncherSettingsFragment extends PreferenceFragment
	implements Preference.OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
			getPreferenceManager().setSharedPreferencesName(LauncherSettings.Settings.TABLE_NAME);
			addPreferencesFromResource(R.xml.launcher_preferences);
//			 SwitchPreference pref = (SwitchPreference) findPreference(
//				Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
//            pref.setPersistent(false);
//            Bundle extras = new Bundle();
//            extras.putBoolean(LauncherSettings.Settings.EXTRA_DEFAULT_VALUE, false);
//            Bundle value = getActivity().getContentResolver().call(
//				LauncherSettings.Settings.CONTENT_URI,
//				LauncherSettings.Settings.METHOD_GET,
//				Utilities.ALLOW_ROTATION_PREFERENCE_KEY, extras);
//            pref.setChecked(value.getBoolean(LauncherSettings.Settings.EXTRA_VALUE));
//
//            pref.setOnPreferenceChangeListener(this);
//			//findPreference(Utilities.UNINSTALL).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Bundle extras = new Bundle();
            extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, (Boolean) newValue);
            getActivity().getContentResolver().call(
				LauncherSettings.Settings.CONTENT_URI,
				LauncherSettings.Settings.METHOD_SET,
				preference.getKey(), extras);
            return true;
        }

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
		{
			switch(preference.getKey()){
				case Utilities.SharedPreferences.UNINSTALL:
					AlertDialog dialog=new AlertDialog.Builder(getActivity(),R.style.Dialog).setTitle(R.string.how_to_do).setPositiveButton(android.R.string.cancel, null).setNegativeButton(R.string.uninstall, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								DeviceManager.getInstance(getActivity()).removeDeviceOwner();
								Intent uninstall=new Intent(Intent.ACTION_UNINSTALL_PACKAGE);
								uninstall.setData(Uri.fromParts("package",getActivity().getPackageName(),null));
								startActivity(uninstall);
							}
						}).setNeutralButton(R.string.clear_device_owner, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface p1, int p2)
							{
								DeviceManager.getInstance(getActivity()).removeDeviceOwner();
							}
							}).create();
					dialog.show();
					return true;
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
			
		}
		
    }
}
