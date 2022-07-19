package com.henry.common.utils;

import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.mobile.device.DeviceUtils;

import javax.servlet.http.HttpServletRequest;


public class DeviceUtil {
    public static final String PC = "pc";
    public static final String IOS = "ios";
    public static final String ANDROID = "android";

    public static String getDeviceType(HttpServletRequest request) {
        if (isMobileDevice(request)) {
            if (isIOS(request)) {
                return IOS;
            } else if (isAndroid(request)) {
                return ANDROID;
            }
        }
        return PC;
    }

    public static boolean isMobileDevice(HttpServletRequest request) {
        Device currentDevice = DeviceUtils.getCurrentDevice(request);
        if (currentDevice == null) {
            return false;
        }
        return currentDevice.isMobile() || currentDevice.isTablet();
    }

    public static boolean isIOS(HttpServletRequest request) {
        Device currentDevice = DeviceUtils.getCurrentDevice(request);
        if (currentDevice != null && isMobileDevice(request) && currentDevice.getDevicePlatform() == DevicePlatform.IOS) {
            return true;
        }
        return false;
    }

    public static boolean isAndroid(HttpServletRequest request) {
        Device currentDevice = DeviceUtils.getCurrentDevice(request);
        if (currentDevice != null && isMobileDevice(request) && currentDevice.getDevicePlatform() == DevicePlatform.ANDROID) {
            return true;
        }
        return false;
    }
}
