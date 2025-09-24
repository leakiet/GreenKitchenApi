package com.greenkitchen.portal.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.greenkitchen.portal.security.AuthTokenFilter;
import com.greenkitchen.portal.security.MyUserDetailService;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Autowired
  private MyUserDetailService myDetailsService;

  @Bean
  public AuthTokenFilter authenticationJwtTokenFilter() {
    return new AuthTokenFilter();
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            // Public endpoints - không cần authentication

        		.requestMatchers("/apis/v1/ws/**").permitAll()
            .requestMatchers("/apis/v1/auth/**").permitAll()          // Login, register, refresh-token
            .requestMatchers("/apis/v1/chat/**").permitAll()          // Chat endpoints
            .requestMatchers("/apis/v1/support/**").permitAll()       // Feedback/Support endpoints
            .requestMatchers("/apis/v1/settings/**").permitAll()      // Settings endpoints

            .requestMatchers("/apis/v1/ingredients/**").permitAll() // ingredients
            // .requestMatchers(HttpMethod.GET, "/apis/v1/menu-meals/customers/**").permitAll()
            // .requestMatchers(HttpMethod.GET, "/apis/v1/menu-meals/customers").permitAll()
            .requestMatchers("/apis/v1/menu-meals/customers/**").permitAll()
            .requestMatchers("/apis/v1/menu-meal-reviews/**").permitAll()
            .requestMatchers("/apis/v1/post-categories/**").permitAll()
            .requestMatchers("/apis/v1/posts/**").permitAll()

            .requestMatchers("/apis/v1/carts/**").permitAll()
            .requestMatchers("/apis/v1/dashboard/**").permitAll()
            .requestMatchers("/apis/v1/customer-week-meal-days/**").permitAll()
            .requestMatchers("/apis/v1/menu-meal-reviews/**").permitAll() // Custom meals
            .requestMatchers("/apis/v1/paypal/**").permitAll() // PayPal endpoints
            .requestMatchers("/apis/v1/week-meals/**").permitAll() // Week meals endpoints
            .requestMatchers("/apis/v1/cart-scan/**").permitAll() // Cart scan endpoints
            .requestMatchers("/apis/v1/email-scheduler/**").permitAll() // Statistics endpoints

            // Protected endpoints - cần authentication
            .requestMatchers("/apis/v1/customers/**").authenticated()
            .requestMatchers("/apis/v1/addresses/**").permitAll()
            .requestMatchers("/apis/v1/customer-tdees/**").permitAll()
            .requestMatchers("/apis/v1/coupons/**").permitAll()
            .requestMatchers("/apis/v1/custom-meals/**").permitAll()
            .requestMatchers("/apis/v1/customer-references/**").permitAll()
            .requestMatchers("/apis/v1/orders/**").permitAll()
            .requestMatchers("/apis/v1/coupons/**").permitAll()
            .requestMatchers("/apis/v1/customer-coupons/**").permitAll()
            .requestMatchers("/apis/v1/stores/**").permitAll()

            // Admin endpoints
            .requestMatchers("/apis/v1/admin/**").hasRole("ADMIN")
             // Posts endpoints

            .requestMatchers("/apis/v1/uploads/**").permitAll()
            
            // Default: require authentication for everything else
            .anyRequest().authenticated())
        .formLogin(form -> form.disable());

    // Add JWT filter before UsernamePasswordAuthenticationFilter
    http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(myDetailsService);
    authProvider.setPasswordEncoder(passwordEncoder());
    return new ProviderManager(authProvider);
  }

}
