package com.happym.mathsquare;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

public class sharedPreferences {

    
    private static final String PREFERENCES_FILE = "MyAppPrefs";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_PROFILE_IMAGE_URI = "user_profileimage";
    private static final String KEY_FIRSTNAME = "user_firstname";
    static final String KEY_LASTNAME = "user_lastname";
    private static final String KEY_BANK_RESULT_PRIVACY = "bank_result_privacy";
    private static final String KEY_ALLOW_NOTIFICATIONSONE = "allownotifications1";
    private static final String KEY_ALLOW_VIBRATIONS = "allowvibrations";
    private static final String KEY_SAVED_SCANSOUND = "Scan_sound";
    private static final String KEY_IS_LOGGED_IN = "101";
    private static final String KEY_STUDENT_LOGIN = "202";
    private static final String KEY_SECTION = "section";
    private static final String KEY_GRADE = "grade";
    
    private static final String KEY_USER_AGREE = "status_useragreed";
    private static final String KEY_IS_SUBSCRIBED = "billing_check";
    private static final String KEY_BILLING_YEARLY = "billing_check_yearly";
    private static final String KEY_BILLING_WEEKLY = "billing_check_weekly";
    
    private static final String KEY_BG_THEME = "backgroundtheme";

    private static final String KEY_GUEST_ID = "GUEST_ID";

    private static final String KEY_GUEST_STATUS = "GUEST_STATUS";

    private static final String KEY_SUBSCRIBTION_TIME = "User_subscription_time";

    
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
    }
    public static void saveGrade(Context context, String emailId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_GRADE, emailId);
        editor.apply();
    }

    public static String getGrade(Context context) {
        return getSharedPreferences(context).getString(KEY_GRADE, null);
    }
    
    public static void clearGrade(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_GRADE);
        editor.apply();
    }
    
    public static void saveSection(Context context, String emailId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_SECTION, emailId);
        editor.apply();
    }

    public static String getSection(Context context) {
        return getSharedPreferences(context).getString(KEY_SECTION, null);
    }
    
    public static void clearSection(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_SECTION);
        editor.apply();
    }

    public static void saveEmail(Context context, String emailId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_EMAIL, emailId);
        editor.apply();
    }

    public static String getEmail(Context context) {
        return getSharedPreferences(context).getString(KEY_EMAIL, null);
    }
    
    public static void clearEmailId(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_EMAIL);
        editor.apply();
    }

    public static void saveFirstN(Context context, String studentId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_FIRSTNAME, studentId);
        editor.apply();
    }

    public static String getFirstN(Context context) {
        return getSharedPreferences(context).getString(KEY_FIRSTNAME, null);
    }

    public static void clearFirstName(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_FIRSTNAME);
        editor.apply();
    }
    
    public static void saveLastN(Context context, String studentId) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_LASTNAME, studentId);
        editor.apply();
    }

    public static String getLastN(Context context) {
        return getSharedPreferences(context).getString(KEY_LASTNAME, null);
    }

    public static void clearLastName(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_LASTNAME);
        editor.apply();
    }

    public static void saveProfileImageUri(Context context, String imageUri) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_PROFILE_IMAGE_URI, imageUri);
        editor.apply();
    }

    public static String getProfileImageUri(Context context) {
        return getSharedPreferences(context).getString(KEY_PROFILE_IMAGE_URI, null);
    }

    public static void clearProfileImageUri(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_PROFILE_IMAGE_URI);
        editor.apply();
    }

    public static boolean isLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }
    
    
        public static boolean StudentIsLoggedIn(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_STUDENT_LOGIN, false);
    }

    public static void StudentIsSetLoggedIn(Context context, boolean isLoggedIn) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_STUDENT_LOGIN, isLoggedIn);
        editor.apply();
    }
    

    // Methods for Bank result privacy
    public static void setBankResultPrivacy(Context context, boolean isEnabled) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_BANK_RESULT_PRIVACY, isEnabled);
        editor.apply();
    }

    public static boolean isBankResultPrivacyEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_BANK_RESULT_PRIVACY, false);
    }

    public static void clearBankResultPrivacy(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_BANK_RESULT_PRIVACY);
        editor.apply();
    }
    
    public static void setAllowNotifications(Context context, boolean isEnabled) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_ALLOW_NOTIFICATIONSONE, isEnabled);
        editor.apply();
    }

    public static boolean isAllowNotificationsEnabled(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_ALLOW_NOTIFICATIONSONE, false);
    }

    public static void clearAllowNotifications(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_ALLOW_NOTIFICATIONSONE);
        editor.apply();
    }
    
    public static void setAllowVibe(Context context, boolean isEnabled) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_ALLOW_VIBRATIONS, isEnabled);
        editor.apply();
    }

    public static boolean isAllowVibe(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_ALLOW_VIBRATIONS, false);
    }

    public static void clearAllowVib(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_ALLOW_VIBRATIONS);
        editor.apply();
    }
    
    public static String getScanSound(Context context) {
        return getSharedPreferences(context).getString(KEY_SAVED_SCANSOUND, null);
    }

    public static void clearScanSound(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_SAVED_SCANSOUND);
        editor.apply();
    }

    public static void saveScanSound(Context context, String music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_SAVED_SCANSOUND, music);
        editor.apply();
    }
    
    // THEME
    
    public static String getBGtheme(Context context) {
        return getSharedPreferences(context).getString(KEY_BG_THEME, null);
    }

    public static void clearBGtheme(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_BG_THEME);
        editor.apply();
    }

    public static void saveBGtheme(Context context, String music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(KEY_BG_THEME, music);
        editor.apply();
    }
    
    //LEGAL TERMS STATUS
    public static boolean getStatusAgree(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_IS_SUBSCRIBED, false);
    }

    public static void clearStatusAgree(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_IS_SUBSCRIBED);
        editor.apply();
    }

    public static void notifyStatusAgree(Context context, boolean music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_SUBSCRIBED, music);
        editor.apply();
    }
    
    //BILLING
    
    public static boolean getSubStatus(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_IS_SUBSCRIBED, false);
    }

    public static void clearSubStatus(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_IS_SUBSCRIBED);
        editor.apply();
    }

    public static void notifySubStatus(Context context, boolean music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_IS_SUBSCRIBED, music);
        editor.apply();
    }
    
    public static boolean getYearlyBillStatus(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_BILLING_YEARLY, false);
    }

    public static void clearYearlyBillStatus(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_BILLING_YEARLY);
        editor.apply();
    }

    public static void notifyYearlyBillStatus(Context context, boolean music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_BILLING_YEARLY, music);
        editor.apply();
    }
    
    public static boolean getWeeklyBillStatus(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_BILLING_WEEKLY, false);
    }

    public static void clearWeeklyBillStatus(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_BILLING_WEEKLY);
        editor.apply();
    }

    public static void notifyWeeklyBillStatus(Context context, boolean music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_BILLING_WEEKLY, music);
        editor.apply();
    }
    
    public static String getOrCreateGuestId(Context context) {
        SharedPreferences prefs = getSharedPreferences(context);
        String guestId = prefs.getString(KEY_GUEST_ID, null);

        if (guestId == null) {
            String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            guestId = "guest_" + deviceId;
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_GUEST_ID, guestId);
            editor.apply();
        }

        return guestId;
    }
    
    public static boolean getGuestAccountStatus(Context context) {
        return getSharedPreferences(context).getBoolean(KEY_GUEST_STATUS, false);
    }

    public static void clearGusstAccountStatus(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_GUEST_STATUS);
        editor.apply();
    }

    public static void notifyGuestAccountStatus(Context context, boolean music) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putBoolean(KEY_GUEST_STATUS, music);
        editor.apply();
    }
    
    public static long getSubscriptionTime(Context context) {
        return getSharedPreferences(context).getLong(KEY_SUBSCRIBTION_TIME, -1);
    }

    public static void clearSubscriptionTims(Context context) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.remove(KEY_SUBSCRIBTION_TIME);
        editor.apply();
    }

    public static void notifySubscriptionTime(Context context, long time) {
        SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(KEY_GUEST_STATUS, time);
        editor.apply();
    }
}
