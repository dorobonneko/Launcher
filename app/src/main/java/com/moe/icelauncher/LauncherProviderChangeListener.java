package com.moe.icelauncher;

/**
 * This class is a listener for {@link LauncherProvider} changes. It gets notified in the
 * sendNotify method. This listener is needed because by default the Launcher suppresses
 * standard data change callbacks.
 */
public interface LauncherProviderChangeListener {

    public void onLauncherProviderChange(String packageName);

    public void onSettingsChanged(String settings, boolean value);

    public void onAppWidgetHostReset();
}
