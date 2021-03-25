package com.psl.adms.web.config;

import com.psl.adms.web.tenants.TenantFilter;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.client.KeycloakClientRequestFactory;
import org.keycloak.adapters.springsecurity.client.KeycloakRestTemplate;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.switchuser.SwitchUserFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;


@Configuration
@ComponentScan(
    basePackageClasses = KeycloakSecurityComponents.class,
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.keycloak.adapters.springsecurity.management.HttpSessionManager"))
@EnableWebSecurity
public class KeycloakSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

    @Autowired
    public KeycloakClientRequestFactory keycloakClientRequestFactory;

    @Autowired
    public void configureGlobal(
            AuthenticationManagerBuilder auth) throws Exception {

        KeycloakAuthenticationProvider keycloakAuthenticationProvider
                = keycloakAuthenticationProvider();
        keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(
                new SimpleAuthorityMapper());
        auth.authenticationProvider(keycloakAuthenticationProvider);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public KeycloakRestTemplate keycloakRestTemplate() {
        return new KeycloakRestTemplate(keycloakClientRequestFactory);
    }

    @Bean
    @Primary
    public KeycloakConfigResolver keycloakConfigResolver(KeycloakSpringBootProperties properties) {
        return new CustomKeycloakSpringBootConfigResolver(properties);
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(
                new SessionRegistryImpl());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http.csrf().disable();
        http.httpBasic().disable();
        http.cors().and().authorizeRequests()
        .antMatchers("/actuator", "/actuator/**",
        		"/info","/info/**",
        		"/health","/health/**")
        .permitAll();
        
        http.cors().and().
        authorizeRequests()
        .requestMatchers(CorsUtils:: isPreFlightRequest).permitAll()
        .anyRequest()
        .authenticated()
        .and()
        .httpBasic();

                
        http.cors().and()
        .authorizeRequests()
        .antMatchers("/documents","/documents/**",
                "/doc-rejection-statuses","/doc-rejection-statuses/**",
                "/batches","/batches/**",
                "/annotationvendor","/annotationvendor/**",
                "/pages","/pages/**",
                "/scanlog","/scanlog/**",
                "/scanvendor","/scanvendor/**","/annotation-review-vendor/**")
        .authenticated().and()
                .authorizeRequests();

        //added custom filter after all spring security filters
      http.addFilterAfter(new TenantFilter(), SwitchUserFilter.class);
    }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
      final CorsConfiguration configuration = new CorsConfiguration();
      configuration.setAllowedOrigins(Arrays.asList("*"));
      configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
      configuration.setAllowCredentials(true);
      configuration.setAllowedHeaders(Arrays.asList("Authorization", "Cache-Control", "Content-Type", "X-TenantID"));
      final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      source.registerCorsConfiguration("/**", configuration);
      return source;
  }    
}
