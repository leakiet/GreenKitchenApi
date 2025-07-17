package com.greenkitchen.portal.utils;

import java.security.Key;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.greenkitchen.portal.security.MyUserDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
	private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

	@Value("${app.accessTokenSecret}")
	private String accessTokenSecret;

	@Value("${app.accessTokenExpirationMs}")
	private int accessTokenExpirationMs;

	@Value("${app.refreshTokenSecret}")
	private String refreshTokenSecret;

	@Value("${app.refreshTokenExpirationMs}")
	private int refreshTokenExpirationMs;

	public String generateJwtToken(Authentication authentication) {
		return generateToken(authentication, "access", accessTokenSecret, accessTokenExpirationMs);
	}

	public String generateRefreshToken(Authentication authentication) {
		return generateToken(authentication, "refresh", refreshTokenSecret, refreshTokenExpirationMs);
	}

	// Method chung để generate token
	private String generateToken(Authentication authentication, String tokenType, String secret, int expirationMs) {
		MyUserDetails userPrincipal = (MyUserDetails) authentication.getPrincipal();

		return Jwts.builder()
				.setSubject(userPrincipal.getUsername())
				.setIssuedAt(new Date())
				.setIssuer("The Green Kitchen")
				.claim("type", tokenType)
				.claim("roles", userPrincipal.getRoles())
				.setExpiration(new Date((new Date()).getTime() + expirationMs))
				.signWith(getKey(secret), SignatureAlgorithm.HS256)
				.compact();
	}

	private Key getKey(String secret) {
		return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey(accessTokenSecret)).build().parseClaimsJws(token).getBody().getSubject();
	}
	
	public Claims getClaimsFromJwtToken(String token) {
		return Jwts.parserBuilder().setSigningKey(getKey(accessTokenSecret)).build().parseClaimsJws(token).getBody();
	}

	public boolean validateJwtToken(String authToken) {
		return validateToken(authToken, accessTokenSecret, "access");
	}

	public boolean validateRefreshToken(String refreshToken) {
		return validateToken(refreshToken, refreshTokenSecret, "refresh");
	}

	// Method chung để validate token
	private boolean validateToken(String token, String secret, String expectedType) {
		try {
			Claims claims = Jwts.parserBuilder().setSigningKey(getKey(secret)).build().parseClaimsJws(token).getBody();
			String tokenType = (String) claims.get("type");
			
			// Nếu expectedType là null thì không cần check type (backward compatibility)
			if (expectedType != null && !expectedType.equals(tokenType)) {
				logger.error("Invalid token type. Expected: {}, Found: {}", expectedType, tokenType);
				return false;
			}
			
			return true;
		} catch (MalformedJwtException e) {
			logger.error("Invalid {} token: {}", expectedType, e.getMessage());
		} catch (ExpiredJwtException e) {
			logger.error("{} token is expired: {}", expectedType, e.getMessage());
		} catch (UnsupportedJwtException e) {
			logger.error("{} token is unsupported: {}", expectedType, e.getMessage());
		} catch (IllegalArgumentException e) {
			logger.error("{} token claims string is empty: {}", expectedType, e.getMessage());
		}
		return false;
	}

	public String getUserNameFromRefreshToken(String refreshToken) {
		return Jwts.parserBuilder().setSigningKey(getKey(refreshTokenSecret)).build().parseClaimsJws(refreshToken).getBody().getSubject();
	}
}