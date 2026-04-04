package reel.ru.AuthService.converter;

public interface Converter<F, T> {
    T convert(F from);
}
