package com.moe.icelauncher.shortcuts;
import android.content.pm.ShortcutInfo;
import android.content.Intent;
import android.annotation.TargetApi;
import android.os.Build;
import android.content.ComponentName;
import android.os.UserHandle;

@TargetApi(Build.VERSION_CODES.N)
public class ShortcutInfoCompat {
    private static final String INTENT_CATEGORY = "com.android.launcher3.DEEP_SHORTCUT";
    public static final String EXTRA_SHORTCUT_ID = "shortcut_id";

    private ShortcutInfo mShortcutInfo;

    public ShortcutInfoCompat(ShortcutInfo shortcutInfo) {
        mShortcutInfo = shortcutInfo;
    }

	public Intent getIntent()
	{
		return mShortcutInfo.getIntent();
	}

    @TargetApi(Build.VERSION_CODES.N)
    public Intent makeIntent() {
        return new Intent(Intent.ACTION_MAIN)
			.addCategory(INTENT_CATEGORY)
			.setComponent(getActivity())
			.setPackage(getPackage())
			.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
			.putExtra(EXTRA_SHORTCUT_ID, getId());
    }

    public ShortcutInfo getShortcutInfo() {
        return mShortcutInfo;
    }

    public String getPackage() {
        return mShortcutInfo.getPackage();
    }

    public String getId() {
        return mShortcutInfo.getId();
    }

    public CharSequence getShortLabel() {
        return mShortcutInfo.getShortLabel();
    }

    public CharSequence getLongLabel() {
        return mShortcutInfo.getLongLabel();
    }

    public long getLastChangedTimestamp() {
        return mShortcutInfo.getLastChangedTimestamp();
    }

    public ComponentName getActivity() {
        return mShortcutInfo.getActivity();
    }

    public UserHandle getUserHandle() {
        return mShortcutInfo.getUserHandle();
    }

    public boolean hasKeyFieldsOnly() {
        return mShortcutInfo.hasKeyFieldsOnly();
    }

    public boolean isPinned() {
        return mShortcutInfo.isPinned();
    }

    public boolean isDeclaredInManifest() {
        return mShortcutInfo.isDeclaredInManifest();
    }

    public boolean isEnabled() {
        return mShortcutInfo.isEnabled();
    }

    public boolean isDynamic() {
        return mShortcutInfo.isDynamic();
    }

    public int getRank() {
        return mShortcutInfo.getRank();
    }

    public CharSequence getDisabledMessage() {
        return mShortcutInfo.getDisabledMessage();
    }

    @Override
    public String toString() {
        return mShortcutInfo.toString();
    }
}
