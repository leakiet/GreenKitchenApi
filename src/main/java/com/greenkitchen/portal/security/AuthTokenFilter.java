package com.greenkitchen.portal.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Order(1)
public class AuthTokenFilter extends OncePerRequestFilter {
	@Autowired
	private JwtUtils jwtUtils;

	@Autowired
	private MyUserDetailService userDetailService;

	private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
		String path = request.getServletPath();

		// Không filter những endpoint này (cho phép gọi khi token expired)
		return !path.startsWith("/apis/")
			|| path.startsWith("/apis/v1/auth/")
			|| path.startsWith("/apis/v1/ws/")
			|| path.startsWith("/apis/v1/chat/");
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		try {
			String jwt = parseJwt(request);
			System.out.println("Parsed JWT: " + (jwt != null ? "Token found (length: " + jwt.length() + ")" : "null"));
			
			if (jwt != null) {
				try {
					if (jwtUtils.validateJwtToken(jwt)) {
						String username = jwtUtils.getUserNameFromJwtToken(jwt);
						System.out.println("Valid JWT token for user: " + username);

						MyUserDetails userDetails = (MyUserDetails) userDetailService.loadUserByUsername(username);
						UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
								userDetails, null, userDetails.getAuthorities());
						authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

						SecurityContextHolder.getContext().setAuthentication(authentication);
						System.out.println("Authentication set successfully for user: " + username);
					} else {
						System.out.println("JWT validation failed for token");
					}
				} catch (ExpiredJwtException e) {
					logger.error("Access token is expired: {}", e.getMessage());
					System.out.println("Access token expired, returning 410");
					
					// Trả về HTTP 410 Gone khi token hết hạn
					response.setStatus(410); // HTTP 410 Gone
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Access token expired\",\"code\":410}");
					return; // Không tiếp tục filter chain
				} catch (MalformedJwtException e) {
					logger.error("Malformed JWT token: {}", e.getMessage());
					System.out.println("Malformed JWT token, returning 401");
					
					// Trả về HTTP 401 Unauthorized khi token không hợp lệ
					response.setStatus(401); // HTTP 401 Unauthorized
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Invalid token format\",\"code\":401}");
					return;
				} catch (UnsupportedJwtException e) {
					logger.error("Unsupported JWT token: {}", e.getMessage());
					System.out.println("Unsupported JWT token, returning 401");
					
					// Trả về HTTP 401 Unauthorized khi token không được hỗ trợ
					response.setStatus(401); // HTTP 401 Unauthorized
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Unsupported token\",\"code\":401}");
					return;
				} catch (IllegalArgumentException e) {
					logger.error("JWT token claims string is empty: {}", e.getMessage());
					System.out.println("Empty JWT token, returning 401");
					
					// Trả về HTTP 401 Unauthorized khi token rỗng
					response.setStatus(401); // HTTP 401 Unauthorized
					response.setContentType("application/json");
					response.setCharacterEncoding("UTF-8");
					response.getWriter().write("{\"error\":\"Empty token\",\"code\":401}");
					return;
				}
			} else {
				System.out.println("No JWT token provided");
			}
		} catch (Exception e) {
			logger.error("Cannot set user authentication: {}", e);
			System.out.println("Exception in JWT processing: " + e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	private String parseJwt(HttpServletRequest request) {
		// Ưu tiên lấy token từ Cookie trước (vì FE dùng HttpOnly Cookie)
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			System.out.println("Checking cookies for access_token...");
			for (Cookie cookie : cookies) {
				System.out.println("Cookie found: " + cookie.getName() + " = " + 
					(cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(cookie.getValue().length(), 20)) + "..." : "null"));
				if ("access_token".equals(cookie.getName())) {
					System.out.println("Access token found in cookie!");
					return cookie.getValue();
				}
			}
		} else {
			System.out.println("No cookies found in request");
		}

		// Fallback: tìm trong Authorization header
		String headerAuth = request.getHeader("Authorization");
		System.out.println("headerAuth:: " + headerAuth);

		if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
			System.out.println("Token found in Authorization header");
			return headerAuth.substring(7);
		}

		System.out.println("No JWT token found in request (neither cookie nor header)");
		return null;
	}
}
