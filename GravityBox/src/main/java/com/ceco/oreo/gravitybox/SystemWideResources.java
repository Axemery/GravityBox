/*
 * Copyright (C) 2019 Peter Gregus for GravityBox Project (C3C076@xda)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ceco.oreo.gravitybox;

import android.content.Context;

import com.ceco.oreo.gravitybox.managers.TunerManager;
import com.ceco.oreo.gravitybox.tuner.TunerMainActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XSharedPreferences;

public class SystemWideResources {

    private static ResourceProxy sResourceProxy;

    public static void initResources(final XSharedPreferences prefs, final XSharedPreferences tunerPrefs) {
        sResourceProxy = new ResourceProxy();
        List<String> resourceNames;

        // Framework resources
        resourceNames = new ArrayList<>(Arrays.asList(
                "config_enableTranslucentDecor",
                "config_showNavigationBar",
                "config_unplugTurnsOnScreen",
                "config_defaultNotificationLedOff",
                "config_sip_wifi_only",
                "config_safe_media_volume_enabled",
                "reboot_to_reset_title",
                "config_allowAllRotations"
        ));
        // add overriden items from Advanced tuning if applicable
        if (tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_ENABLED, false) &&
                !tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_LOCKED, false)) {
            TunerManager.addUserItemKeysToList(TunerManager.Category.FRAMEWORK, resourceNames);
        }
        sResourceProxy.addInterceptor(new ResourceProxy.Interceptor("android", resourceNames) {
            @Override
            public boolean onIntercept(ResourceProxy.ResourceSpec resourceSpec) {
                // Advanced tuning has priority
                if (tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_ENABLED, false) &&
                        !tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_LOCKED, false)) {
                    if (TunerManager.onIntercept(resourceSpec)) {
                        return true;
                    }
                }

                switch (resourceSpec.name) {
                    case "config_allowAllRotations":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_DISPLAY_ALLOW_ALL_ROTATIONS, false)) {
                            resourceSpec.value = true;
                            return  true;
                        }
                        break;
                    case "config_enableTranslucentDecor":
                        int translucentDecor = Integer.valueOf(prefs.getString(GravityBoxSettings.PREF_KEY_TRANSLUCENT_DECOR, "0"));
                        if (translucentDecor != 0) {
                            resourceSpec.value = (translucentDecor == 1);
                            return true;
                        }
                        break;
                    case "config_showNavigationBar":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_NAVBAR_OVERRIDE, false)) {
                            resourceSpec.value = prefs.getBoolean(GravityBoxSettings.PREF_KEY_NAVBAR_ENABLE,
                                    (boolean)resourceSpec.value);
                            return true;
                        }
                        break;
                    case "config_unplugTurnsOnScreen":
                        if (!Utils.isSamsungRom()) {
                            resourceSpec.value = prefs.getBoolean(GravityBoxSettings.PREF_KEY_UNPLUG_TURNS_ON_SCREEN,
                                    (boolean)resourceSpec.value);
                            return true;
                        }
                        break;
                    case "config_safe_media_volume_enabled":
                        if (!Utils.isSamsungRom()) {
                            Utils.TriState triState = Utils.TriState.valueOf(prefs.getString(
                                    GravityBoxSettings.PREF_KEY_SAFE_MEDIA_VOLUME, "DEFAULT"));
                            if (triState != Utils.TriState.DEFAULT) {
                                resourceSpec.value = (triState == Utils.TriState.ENABLED);
                                return true;
                            }
                        }
                        break;
                    case "config_defaultNotificationLedOff":
                        int pulseNotificationDelay = prefs.getInt(
                                GravityBoxSettings.PREF_KEY_PULSE_NOTIFICATION_DELAY, -1);
                        if (pulseNotificationDelay != -1) {
                            resourceSpec.value = pulseNotificationDelay;
                            return true;
                        }
                        break;
                    case "config_sip_wifi_only":
                        resourceSpec.value = false;
                        return true;
                    case "reboot_to_reset_title":
                        resourceSpec.value = "Recovery";
                        return true;
                    case "config_screenBrightnessSettingMinimum":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_BRIGHTNESS_MASTER_SWITCH, false)) {
                            resourceSpec.value = prefs.getInt(GravityBoxSettings.PREF_KEY_BRIGHTNESS_MIN, 20);
                            return true;
                        }
                        break;
                    case "config_screenBrightnessDim":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_BRIGHTNESS_MASTER_SWITCH, false)) {
                            resourceSpec.value = prefs.getInt(GravityBoxSettings.PREF_KEY_SCREEN_DIM_LEVEL, 10);
                            return true;
                        }
                        break;
                }
                return false;
            }
        });

        // SystemUI resources
        resourceNames = new ArrayList<>(Arrays.asList(
                "rounded_corner_content_padding",
                "config_disableMenuKeyInLockScreen",
                "config_hspap_data_distinguishable",
                "config_show4GForLTE"
        ));
        if (tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_ENABLED, false) &&
                !tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_LOCKED, false)) {
            TunerManager.addUserItemKeysToList(TunerManager.Category.SYSTEMUI, resourceNames);
        }
        sResourceProxy.addInterceptor(new ResourceProxy.Interceptor(
                "com.android.systemui",
                resourceNames,
                new ArrayList<>(Arrays.asList(
                        ResourceProxy.getFakeResId("ic_audio_notification"),
                        ResourceProxy.getFakeResId("ic_audio_notification_mute"),
                        ResourceProxy.getFakeResId("ic_qs_signal_hp"),
                        ResourceProxy.getFakeResId("stat_sys_data_fully_connected_hp")))) {
            @Override
            public boolean onIntercept(ResourceProxy.ResourceSpec resourceSpec) {
                // Advanced tuning has priority
                if (tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_ENABLED, false) &&
                        !tunerPrefs.getBoolean(TunerMainActivity.PREF_KEY_LOCKED, false)) {
                    if (TunerManager.onIntercept(resourceSpec)) {
                        return true;
                    }
                }

                switch (resourceSpec.name) {
                    case "rounded_corner_content_padding":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_CORNER_PADDING_REMOVE, false)) {
                            resourceSpec.value = 0;
                            return true;
                        }
                        break;
                    case "config_disableMenuKeyInLockScreen":
                        Utils.TriState triState = Utils.TriState.valueOf(prefs.getString(
                                GravityBoxSettings.PREF_KEY_LOCKSCREEN_MENU_KEY, "DEFAULT"));
                        if (triState != Utils.TriState.DEFAULT) {
                            resourceSpec.value = (triState == Utils.TriState.DISABLED);
                            return true;
                        }
                        break;
                    case "config_hspap_data_distinguishable":
                        if (prefs.getBoolean(GravityBoxSettings.PREF_KEY_SIGNAL_CLUSTER_HPLUS, false) &&
                                Utils.isMotoXtDevice()) {
                            resourceSpec.value = true;
                            return true;
                        }
                        break;
                    case "config_show4GForLTE":
                        String lteStyle = prefs.getString(GravityBoxSettings.PREF_KEY_SIGNAL_CLUSTER_LTE_STYLE, "DEFAULT");
                        if (!lteStyle.equals("DEFAULT")) {
                            resourceSpec.value = lteStyle.equals("4G");
                            return true;
                        }
                        break;
                }
                return false;
            }

            @Override
            public Object onGetFakeResource(Context gbContext, int fakeResId) {
                if (fakeResId == ResourceProxy.getFakeResId("ic_audio_notification")) {
                    return gbContext.getDrawable(R.drawable.ic_audio_notification);
                } else if (fakeResId == ResourceProxy.getFakeResId("ic_audio_notification_mute")) {
                    return gbContext.getDrawable(R.drawable.ic_audio_notification_mute);
                } else if (fakeResId == ResourceProxy.getFakeResId("ic_qs_signal_hp")) {
                    return gbContext.getDrawable(R.drawable.ic_qs_signal_hp);
                } else if (fakeResId == ResourceProxy.getFakeResId("stat_sys_data_fully_connected_hp")) {
                    return gbContext.getDrawable(R.drawable.stat_sys_data_fully_connected_hp);
                }
                return null;
            }
        });
    }
}