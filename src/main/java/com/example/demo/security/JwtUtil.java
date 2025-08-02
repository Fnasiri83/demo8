
package com.example.demo.security;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

//    @Value("${jwt.secret}")
    private String secret="ABCqwertyuiop1234567890wqqABCqwertyuiop1234567890wqqABCqwertyuiop1234567890wqqABCqwertyuiop1234567890wqq";

    @Value("${jwt.expiration}")
    private Long expiration;
    public String extractUsername(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // تولید توکن
    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    // استخراج نام کاربری از توکن
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // استخراج نقش از توکن
    public String getRoleFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("role", String.class));
    }

    // استخراج تاریخ انقضا از توکن
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    // متد عمومی برای استخراج claims
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    // استخراج تمام claims از توکن
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    // بررسی انقضای توکن
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    // اعتبارسنجی توکن
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
}















//package com.example.demo.security;
//
//import io.jsonwebtoken.*;
//        import io.jsonwebtoken.security.Keys; // اضافه کردن import برای استفاده از Keys
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//public class JwtUtil {
//
//    @Value("${jwt.secret}")
//    private String secret;  // هنوز از این برای پیکربندی استفاده می‌کنیم، ولی به‌جای آن SecretKey می‌سازیم
//
//    @Value("${jwt.expiration}")
//    private Long expiration;
//
//    private SecretKey secretKey; // کلید امن برای HS512
//
//    // سازنده برای ایجاد کلید امن
//    public JwtUtil() {
//        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes()); // تبدیل رشته secret به SecretKey مناسب
//    }
//
//    public String extractUsername(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    // تولید توکن
//    public String generateToken(String username, String role) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("role", role);
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(username)
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
//                .signWith(secretKey) // استفاده از secretKey به جای secret
//                .compact();
//    }
//
//    // استخراج نام کاربری از توکن
//    public String getUsernameFromToken(String token) {
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    // استخراج نقش از توکن
//    public String getRoleFromToken(String token) {
//        return getClaimFromToken(token, claims -> claims.get("role", String.class));
//    }
//
//    // استخراج تاریخ انقضا از توکن
//    public Date getExpirationDateFromToken(String token) {
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//    // متد عمومی برای استخراج claims
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // استخراج تمام claims از توکن
//    private Claims getAllClaimsFromToken(String token) {
//        return Jwts.parser()
//                .setSigningKey(secretKey)  // استفاده از secretKey برای بررسی توکن
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // بررسی انقضای توکن
//    private Boolean isTokenExpired(String token) {
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }
//
//    // اعتبارسنجی توکن
//    public Boolean validateToken(String token, String username) {
//        final String tokenUsername = getUsernameFromToken(token);
//        return (tokenUsername.equals(username) && !isTokenExpired(token));
//    }
//}



//package com.example.demo.security;
//
//import io.jsonwebtoken.*;
//import org.springframework.stereotype.Component;
//
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.function.Function;
//
//@Component
//public class JwtUtil {
//
//    // کلید رمزنگاری (در حالت واقعی بهتره از config یا env بخونیم)
//    private final String SECRET_KEY = "my_secret_key";
//
//    // استخراج username (یا هر subject دیگر) از توکن
//    public String extractUsername(String token) {
//        return extractClaim(token, Claims::getSubject);
//    }
//
//    // استخراج expiration (تاریخ انقضا)
//    public Date extractExpiration(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    // استخراج claim خاص
//    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
//        final Claims claims = extractAllClaims(token);
//        return claimsResolver.apply(claims);
//    }
//
//    // خواندن claims
//    private Claims extractAllClaims(String token) {
//        return Jwts.parser()
//                .setSigningKey(SECRET_KEY)
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    // بررسی انقضای توکن
//    private Boolean isTokenExpired(String token) {
//        return extractExpiration(token).before(new Date());
//    }
//
//    // تولید توکن با subject (مثلاً username)
//    public String generateToken(String subject) {
//        return Jwts.builder()
//                .setSubject(subject)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 5)) // ۵ ساعت اعتبار
//                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
//                .compact();
//    }
//    public String generateToken(String username, String role) {
//        Map<String, Object> claims = new HashMap<>();
//        claims.put("role", role); // ذخیره نقش در claims
//
//        return Jwts.builder()
//                .setClaims(claims)
//                .setSubject(username)
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
//               .signWith(SignatureAlgorithm.HS512, secret)
//                .compact();
//    }
//}
