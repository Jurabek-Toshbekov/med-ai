package uz.sdg.sos.security;

import com.nimbusds.jwt.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    //    @Value("${application.security.jwt.secret-key}")
    private static String secretKey = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    @Value("${application.security.jwt.expiration}")
    private Long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private Long refreshExpiration;

    // 1
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    //2
    private <T> T extractClaims(String token, Function<Claims, T> claimsResolve) {
        final Claims claims = exrtractAllClaims(token);
        return claimsResolve.apply(claims);
    }

    //3
    private Claims exrtractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //4
    private static Key getSignInKey() {
        byte[] keyByte = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyByte);
    }

    //5
    public String generateToken(
            Map<String, Object> extractClaims,
            UserDetails userDetails
    ) {
        return buildToken(extractClaims, userDetails, jwtExpiration);
    }

    public String refreshToken(Map<String, Object> extractClaims,
                               UserDetails userDetails) {
        return buildToken(extractClaims, userDetails, refreshExpiration);
    }

    public String refreshToken(UserDetails userDetails) {
        return refreshToken(new HashMap<>(), userDetails);
    }

    private String buildToken(Map<String, Object> extractClaims, UserDetails userDetails, Long jwtExpiration) {
        return Jwts
                .builder()
                .setClaims(extractClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //6
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    //7
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    //8
    private boolean isTokenExpired(String token) {
        return expirationToken(token).before(new Date());
    }

    //9
    private Date expirationToken(String token) {
        return extractClaims(token, Claims::getExpiration);
    }


    // 10 new 20.07.2024
    // New method to extract username from an expired token
    public String getTokenUserName(String token) {
        String userName = null;
        try {
            // Tokenni payload qismini oling va dekodlash
            Claims claims = decodeToken(token);
            userName = claims.getSubject();
        } catch (ExpiredJwtException exception) {
            // Invalid token
            Claims claims = exception.getClaims(); // Muddati tugagan tokenning claims'lari
            userName = claims.getSubject(); // Claims orqali ma'lumot olish
        }
        return userName;
    }

    private static Claims decodeToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

}
