package com.psl.adms.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import com.psl.adms.commons.dto.SecurityAttributeDto;
import com.psl.adms.commons.enums.DocumentStatus;
import com.psl.adms.core.service.keycloak.KeyCloakService;

public class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	@Autowired
	private KeyCloakService keyClockService;

	private static final String SORT_COL = "sortCol";
	private static final String ASC = "asc";
	private static final String SORT_ORDER = "sortOrder";
	private static final String LIMIT = "limit";
	private static final String OFFSET = "offset";
	
	private static final int MAX_LIMIT = 100;
	private static final int DEFAULT_LIMIT = 10;
	
	protected HttpStatus checkAccess(HttpServletRequest httpServletRequest,
										HttpMethod httpMethod, String token){

		String contextPath = httpServletRequest.getContextPath();
		String resource = httpServletRequest.getRequestURI().toString();
		resource = resource.substring(contextPath.length()+1);
		if(resource.indexOf("/") > 0) 
			resource = resource.substring(0, resource.indexOf("/"));
		SecurityAttributeDto attributeDto = new SecurityAttributeDto(httpMethod,
				resource);
		logger.debug("resource="+resource);
		HttpStatus httpStatus = keyClockService.checkAccess(attributeDto, token);
		logger.debug("Status="+httpStatus);
		if(HttpStatus.FORBIDDEN.equals(httpStatus)) {
			throw new AccessDeniedException("Access to resource "+ resource 
					+ " not allowed for current user");
		} else if (HttpStatus.INTERNAL_SERVER_ERROR.equals(httpStatus)) {
			throw new RuntimeException("Internal error ") ;
			
		}
		return httpStatus;
	}
	
	protected String getGroupIdFromUserId(String userId) {
		return keyClockService.getGroupIdFromUserId(userId);
	}
	
	protected Pageable createPageableObject(Map<String, String> queryMap, String defaultSortCol) {
		int offset = StringUtils.isEmpty(queryMap.get(OFFSET)) ? 0 : Integer.parseInt(queryMap.get(OFFSET));
		int limit = StringUtils.isEmpty(queryMap.get(LIMIT)) ? DEFAULT_LIMIT : Integer.parseInt(queryMap.get(LIMIT));
		
		offset = offset < 0 ? 0 : offset;
		limit = limit <= 0 ? DEFAULT_LIMIT : limit > MAX_LIMIT ? MAX_LIMIT : limit;

		Sort sort = new Sort(Sort.Direction.DESC, defaultSortCol);

		if (!StringUtils.isEmpty(queryMap.get(SORT_COL))) {
			String sortCol = queryMap.get(SORT_COL).toString();

			String sortOrder = StringUtils.isEmpty(queryMap.get(SORT_ORDER)) ? null
					: queryMap.get(SORT_ORDER).toString();
			if (StringUtils.isEmpty(sortOrder) || sortOrder.equalsIgnoreCase(ASC)) {
				sort = new Sort(Sort.Direction.ASC, sortCol);
			} else {
				sort = new Sort(Sort.Direction.DESC, sortCol);
			}
		}

		Pageable pageable = PageRequest.of(offset, limit, sort);
		return pageable;
	}

	
	protected Pageable createPageableObjectForSearch(Map<String, String> queryMap, String defaultSortCol) {
		
		int offset = StringUtils.isEmpty(queryMap.get(OFFSET)) ? 0 : Integer.parseInt(queryMap.get(OFFSET));
		int limit = StringUtils.isEmpty(queryMap.get(LIMIT)) ? DEFAULT_LIMIT : Integer.parseInt(queryMap.get(LIMIT));
		
		offset = offset < 0 ? 0 : offset;
		limit = limit <= 0 ? DEFAULT_LIMIT : limit > MAX_LIMIT ? MAX_LIMIT : limit;

		Sort sort = new Sort(Sort.Direction.DESC, defaultSortCol);

		if (!StringUtils.isEmpty(queryMap.get(SORT_COL))) {
			String sortCol = queryMap.get(SORT_COL).toString();

			String sortOrder = StringUtils.isEmpty(queryMap.get(SORT_ORDER)) ? null
					: queryMap.get(SORT_ORDER).toString();
			if (StringUtils.isEmpty(sortOrder) || sortOrder.equalsIgnoreCase(ASC)) {
				sort = new Sort(Sort.Direction.ASC, sortCol);
			} else {
				sort = new Sort(Sort.Direction.DESC, sortCol);
			}
		}

		Pageable pageable = PageRequest.of(offset*limit, limit, sort);
		return pageable;
		
	}
	
	
	protected List<String> getStatusList(Map<String, String> queryMap) {
		String status = StringUtils.isEmpty(queryMap.get("status")) ? "" : queryMap.get("status");

		List<String> statusList = new ArrayList<String>();
		if (!StringUtils.isEmpty(status)) {
			statusList = Arrays.asList(status.trim().split(","));

			statusList = statusList.stream().map(value -> DocumentStatus.getValidDocumentStatus(value))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		return statusList;
	}
	
	protected String getKeycloakUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return ((SimpleKeycloakAccount)authentication.getDetails())
				.getKeycloakSecurityContext().getToken().getSubject();
	}
}
