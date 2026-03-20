package reel.ru.AuthService.service.security.otp;

import lombok.NonNull;

public class OtpKeyFormatter {
    public static String format(@NonNull String id, @NonNull OtpType format, @NonNull OtpPurposeType purpose) {
        return String.format("otp:%s:%s:%s", format.name().toLowerCase(), id, purpose.name().toLowerCase());
    }

    public static String formatForAttempts(@NonNull String id, @NonNull OtpType format, @NonNull OtpPurposeType purpose) {
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
