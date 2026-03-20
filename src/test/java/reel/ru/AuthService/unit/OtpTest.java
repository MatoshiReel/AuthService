package reel.ru.AuthService.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reel.ru.AuthService.service.security.otp.OtpGenerator;
import reel.ru.AuthService.service.security.otp.OtpKeyFormatter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OtpTest {
    @Test
    @DisplayName("Generate OTP-code with 0 size parameter.")
    public void otpGeneratorWithNullParameter() {
        assertThat(OtpGenerator.generate(0), equalTo(""));
    }

    @Test
    @DisplayName("Generate OTP-code with negative size parameter.")
    public void otpGeneratorWithNegativeNumber() {
        assertThat(OtpGenerator.generate(-5), equalTo(""));
    }

    @Test
    @DisplayName("Generate OTP-code with positive size parameter.")
    public void otpGeneratorWithPositiveNumber() {
        assertThat(OtpGenerator.generate(5).length(), equalTo(5));
    }

    @Test
    @DisplayName("Format OTP key for redis with null parameters.")
    public void otpKeyFormatterWithNullParameters() {
        assertThrows(NullPointerException.class, () -> {OtpKeyFormatter.format(null, null, null);});
    }
}
