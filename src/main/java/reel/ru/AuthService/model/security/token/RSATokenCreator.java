package reel.ru.AuthService.model.security.token;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class RSATokenCreator implements TokenCreator {
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.RS256;
    private final PrivateKey privateKey;

    public RSATokenCreator(@Value("${RSA_JWT_PRIVATE_KEY}") String base64PrivateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64PrivateKey));
        this.privateKey = keyFactory.generatePrivate(keySpec);
    }

    @Override
    public String create(String id, long expiredTimeMillis) {
        return Jwts.builder()
                .setSubject(id)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+expiredTimeMillis))
                .setIssuer("reel-server*")
                .signWith(privateKey, signatureAlgorithm)
                .compact();
    }
}
