package reel.ru.AuthService.model.security.otp;

public class OtpKeyFormatter {
    public static String format(String id, OtpType format, OtpPurposeType purpose) {
        return String.format("otp:%s:%s:%s", format.name().toLowerCase(), id, purpose.name().toLowerCase());
    }

    public static String formatForAttempts(String id, OtpType format, OtpPurposeType purpose) {
        return String.format("otp:%s:%s:%s:attempts", format.name().toLowerCase(), id, purpose.name().toLowerCase());
    }

    public enum OtpType {
        EMAIL,
        PHONE
    }

    public enum OtpPurposeType {
        SIGN_IN
    }
}
