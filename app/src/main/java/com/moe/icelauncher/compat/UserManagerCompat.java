package com.moe.icelauncher.compat;
import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import android.content.pm.PackageManager;
import android.util.ArrayMap;
import java.util.Collections;
import java.util.List;
import com.moe.icelauncher.util.LongArrayMap;
import java.util.ArrayList;

public class UserManagerCompat
{
	protected final UserManager mUserManager;
    private final PackageManager mPm;
    private final Context mContext;
	private static final String USER_CREATION_TIME_KEY = "user_creation_time_";
	
    protected LongArrayMap<UserHandle> mUsers;
    // Create a separate reverse map as LongArrayMap.indexOfValue checks if objects are same
    // and not {@link Object#equals}
    protected ArrayMap<UserHandle, Long> mUserToSerialMap;

	private static UserManagerCompat mUserManagerCompat; 
	private UserManagerCompat(Context context){
		this.mContext=context;
		mPm=context.getPackageManager();
		mUserManager=context.getSystemService(UserManager.class);
	}
	public static UserManagerCompat getInstance(Context context){
		synchronized(UserManagerCompat.class){
			if(mUserManagerCompat==null)
				mUserManagerCompat=new UserManagerCompat(context);
		}
		return mUserManagerCompat;
	}
	
   
    
    public long getSerialNumberForUser(UserHandle user) {
        synchronized (this) {
            if (mUserToSerialMap != null) {
                Long serial = mUserToSerialMap.get(user);
                return serial == null ? 0 : serial;
            }
        }
        return mUserManager.getSerialNumberForUser(user);
    }

    
    public UserHandle getUserForSerialNumber(long serialNumber) {
        synchronized (this) {
            if (mUsers != null) {
                return mUsers.get(serialNumber);
            }
        }
        return mUserManager.getUserForSerialNumber(serialNumber);
    }

    
    public boolean isQuietModeEnabled(UserHandle user) {
        return false;
    }

    
    public boolean isUserUnlocked(UserHandle user) {
        return true;
    }

    
    public boolean isDemoUser() {
        return false;
    }

    
    public void enableAndResetCache() {
        synchronized (this) {
            mUsers = new LongArrayMap<>();
            mUserToSerialMap = new ArrayMap<>();
            List<UserHandle> users = mUserManager.getUserProfiles();
            if (users != null) {
                for (UserHandle user : users) {
                    long serial = mUserManager.getSerialNumberForUser(user);
                    mUsers.put(serial, user);
                    mUserToSerialMap.put(user, serial);
                }
            }
        }
    }

    
    public List<UserHandle> getUserProfiles() {
        synchronized (this) {
            if (mUsers != null) {
                return new ArrayList<>(mUserToSerialMap.keySet());
            }
        }

        List<UserHandle> users = mUserManager.getUserProfiles();
        return users == null ? Collections.<UserHandle>emptyList() : users;
    }

    
    public CharSequence getBadgedLabelForUser(CharSequence label, UserHandle user) {
        if (user == null) {
            return label;
        }
        return mPm.getUserBadgedLabel(label, user);
    }

 /*   @Override
    public long getUserCreationTime(UserHandle user) {
        SharedPreferences prefs = ManagedProfileHeuristic.prefs(mContext);
        String key = USER_CREATION_TIME_KEY + getSerialNumberForUser(user);
        if (!prefs.contains(key)) {
            prefs.edit().putLong(key, System.currentTimeMillis()).apply();
        }
        return prefs.getLong(key, 0);
    }*/
	}
