package reel.ru.AuthService.model.security.otp;

import java.security.SecureRandom;

public class OtpGenerator {
    public static String generate(int size) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for(int i = 0; i < size; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
