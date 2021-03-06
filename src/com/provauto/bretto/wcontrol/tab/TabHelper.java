// Copyright 2012 Google Inc. All Rights Reserved.

package com.provauto.bretto.wcontrol.tab;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Convenience helper to build a set of tabs for a {@link TabCompatActivity}. To use this class,
 * extend {@link TabCompatActivity} and:
 *
 * Call {@link TabCompatActivity#getTabHelper()}, returning a {@link TabHelper}.
 *
 * Create a {@link CompatTabListener}.
 *
 * Call {@link TabHelper#newTab(String)} to create each tab.
 *
 * Call CompatTab.setText().setIcon().setTabListener() to set up your tabs.
 *
 * Call {@link TabHelper#addTab(CompatTab)} for each tab, and you're done.
 */
public abstract class TabHelper {

    protected FragmentActivity mActivity;

    protected TabHelper(FragmentActivity activity) {
        mActivity = activity;
    }

    /**
     * Factory method for creating TabHelper objects for a given activity. Depending on which device
     * the app is running, either a basic helper or Honeycomb-specific helper will be returned.
     * Don't call this yourself; the TabCompatActivity instantiates one. Instead call
     * TabCompatActivity.getTabHelper().
     */
    public static TabHelper createInstance(FragmentActivity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new TabHelperHoneycomb(activity);
        } else {
            return new TabHelperEclair(activity);
        }
    }

    /**
     * Create a new tab.
     *
     * @param tag A unique tag to associate with the tab and associated fragment
     * @return CompatTab for the appropriate android version
     */
    public CompatTab newTab(String tag) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new CompatTabHoneycomb(mActivity, tag);
        } else {
            return new CompatTabEclair(mActivity, tag);
        }
    }

    public abstract void addTab(CompatTab tab);

    protected abstract void onSaveInstanceState(Bundle outState);

    protected abstract void onRestoreInstanceState(Bundle savedInstanceState);

    protected abstract void setUp();
}
