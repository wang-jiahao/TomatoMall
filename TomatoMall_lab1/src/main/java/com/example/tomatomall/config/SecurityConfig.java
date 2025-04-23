package com.example.tomatomall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Autowired
    private NativeWebRequest nativeWebRequest;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5173")); // 前端开发服务器地址
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE","PATCH" ,"OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()

                .cors().configurationSource(corsConfigurationSource())//启用CORS配置
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/orders/notify", "/api/orders/returnUrl").permitAll() // 放行回调接口
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll() // 允许所有OPTIONS请求
                .antMatchers(HttpMethod.PATCH,"/api/products/stockpile/**").hasAuthority("admin")
                .antMatchers(HttpMethod.GET,"/api/products/stockpile/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .antMatchers(HttpMethod.POST, "/api/accounts").permitAll()  // 允许注册
                .antMatchers(HttpMethod.POST, "/api/accounts/login").permitAll() // 明确允许登录
                .antMatchers(HttpMethod.GET, "/api/accounts/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/accounts").authenticated()

                .antMatchers(HttpMethod.POST, "/api/cart/checkout").authenticated()
                .antMatchers(HttpMethod.POST, "/api/orders/*/pay").authenticated()
                // 允许匿名访问商品列表和详情

                // 管理员权限控制

                .antMatchers(HttpMethod.POST, "/api/products").hasAuthority("admin")
                .antMatchers(HttpMethod.PUT, "/api/products").hasAuthority("admin")
                .antMatchers(HttpMethod.DELETE, "/api/products/**").hasAuthority("admin")

                .antMatchers(HttpMethod.POST, "/api/cart").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/cart/**").authenticated()
                .antMatchers(HttpMethod.PATCH, "/api/cart/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/cart").authenticated()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        ;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
