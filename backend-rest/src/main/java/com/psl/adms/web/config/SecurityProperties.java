package com.psl.adms.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Component
@Configuration
@ConfigurationProperties("rest.security")
public class SecurityProperties {

  private boolean enabled;
  private String apiMatcher;
  private Cors cors;
  private String issuerUri;

  
	public boolean isEnabled() {
		return enabled;
	}


	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
	public Cors getCors() {
		return cors;
	}
	
	
	public void setCors(Cors cors) {
		this.cors = cors;
	}
	
	
	public String getIssuerUri() {
		return issuerUri;
	}
	
	
	public void setIssuerUri(String issuerUri) {
		this.issuerUri = issuerUri;
	}


	public String getApiMatcher() {
		return apiMatcher;
	}


	public void setApiMatcher(String apiMatcher) {
		this.apiMatcher = apiMatcher;
	}


	public CorsConfiguration getCorsConfiguration() {
		CorsConfiguration corsConfiguration = new CorsConfiguration();
	    corsConfiguration.setAllowedOrigins(cors.getAllowedOrigins());
	    corsConfiguration.setAllowedMethods(cors.getAllowedMethods());
	    corsConfiguration.setAllowedHeaders(cors.getAllowedHeaders());
	    corsConfiguration.setExposedHeaders(cors.getExposedHeaders());
	    corsConfiguration.setAllowCredentials(cors.getAllowCredentials());
	    corsConfiguration.setMaxAge(cors.getMaxAge());

    return corsConfiguration;
  }

  
  public static class Cors {

    public List<String> getAllowedOrigins() {
		return allowedOrigins;
	}
	public List<String> getAllowedMethods() {
		return allowedMethods;
	}
	public List<String> getAllowedHeaders() {
		return allowedHeaders;
	}
	public List<String> getExposedHeaders() {
		return exposedHeaders;
	}
	public Boolean getAllowCredentials() {
		return allowCredentials;
	}
	
	public Long getMaxAge() {
		return maxAge;
	}

	
	public void setAllowedOrigins(List<String> allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}
	public void setAllowedMethods(List<String> allowedMethods) {
		this.allowedMethods = allowedMethods;
	}
	public void setAllowedHeaders(List<String> allowedHeaders) {
		this.allowedHeaders = allowedHeaders;
	}
	public void setExposedHeaders(List<String> exposedHeaders) {
		this.exposedHeaders = exposedHeaders;
	}
	public void setAllowCredentials(Boolean allowCredentials) {
		this.allowCredentials = allowCredentials;
	}
	public void setMaxAge(Long maxAge) {
		this.maxAge = maxAge;
	}


	private List<String> allowedOrigins;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private Boolean allowCredentials;
    private Long maxAge;
  }
  
  

}
