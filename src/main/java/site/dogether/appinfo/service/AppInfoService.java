package site.dogether.appinfo.service;

import org.springframework.stereotype.Service;
import site.dogether.appinfo.exception.InvalidAppVersionException;

@Service
public class AppInfoService {

    private static final int MINIMUM_REQUIRED_APP_MAJOR_VERSION = 1;
    private static final int MINIMUM_REQUIRED_APP_MINOR_VERSION = 0;
    private static final int APP_MAJOR_VERSION_INDEX = 0;
    private static final int APP_MINOR_VERSION_INDEX = 1;

    public boolean forceUpdateCheck(final String appVersion) {
        final int[] appVersionParts = parseVersionParts(appVersion);
        final int appMajorVersion = appVersionParts[APP_MAJOR_VERSION_INDEX];
        final int appMinorVersion = appVersionParts[APP_MINOR_VERSION_INDEX];

        return MINIMUM_REQUIRED_APP_MAJOR_VERSION > appMajorVersion || MINIMUM_REQUIRED_APP_MINOR_VERSION > appMinorVersion;
    }

    private int[] parseVersionParts(final String appVersion) {
        final String[] appVersionPartsString = appVersion.split("\\.");
        if (appVersionPartsString.length != 3) {
            throw new InvalidAppVersionException(String.format("유효하지 않은 App Version 형식입니다. - (%s)", appVersion));
        }

        final int[] appVersionParts = new int[3];
        for (int i = 0; i < appVersionPartsString.length; i++) {
            appVersionParts[i] = convertAppVersionPartStringToNumber(appVersionPartsString[i]);
        }

        return appVersionParts;
    }

    private int convertAppVersionPartStringToNumber(final String appVersionPartString) {
        try {
            return Integer.parseInt(appVersionPartString);
        } catch (final NumberFormatException e) {
            throw new InvalidAppVersionException(String.format("유효하지 않은 App Version 형식입니다. - (%s)", appVersionPartString));
        }
    }
}
