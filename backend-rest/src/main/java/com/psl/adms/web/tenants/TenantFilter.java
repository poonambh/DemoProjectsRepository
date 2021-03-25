package com.psl.adms.web.tenants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.psl.adms.commons.context.TenantContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class TenantFilter implements Filter {
	public static final String TENANT_HEADER = "X-TenantID";
	Logger logger = LogManager.getLogger(TenantFilter.class);
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
			throws IOException, ServletException {

		HttpServletResponse response = (HttpServletResponse) servletResponse;

		HttpServletRequest request = (HttpServletRequest) servletRequest;

		if(request.getRequestURL().indexOf("actuator") > 0) {
			logger.debug("Found actuator endpoint, skipping tenantid check");
			filterChain.doFilter(servletRequest, servletResponse);
			return;
		}

		String tenantHeader = request.getHeader(TENANT_HEADER);
		
		if(StringUtils.isEmpty(tenantHeader)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

			response.setContentType(MediaType.APPLICATION_JSON_VALUE);

			response.getWriter().write("{\"error\": \"No tenant supplied\"}");

			response.getWriter().flush();

			return;
		}

		TenantContext.setCurrentTenant(Long.parseLong(tenantHeader));

		filterChain.doFilter(servletRequest, servletResponse);
	}

	@Override
	public void destroy() {

	}
}
