package com.psl.adms.web.controller.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.psl.adms.commons.context.TenantContext;
import com.psl.adms.commons.dto.FilterStaticContentDto;
import com.psl.adms.commons.dto.SearchFilterExpressionDto;
import com.psl.adms.commons.enums.SecurityUserRole;
import com.psl.adms.core.service.filter.StaticContentService;

@RestController
@RequestMapping("/filter")
public class StaticContentController {

	private static final Logger logger = LoggerFactory.getLogger(StaticContentController.class);

	@Autowired
	public StaticContentService staticContentService;

	@RequestMapping("/staticdata")
	public FilterStaticContentDto fetchFilterStaticContents() throws IllegalAccessException {
		
		FilterStaticContentDto filterStaticContentDto = new FilterStaticContentDto();
		@SuppressWarnings("unchecked")
		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		if (CollectionUtils.isEmpty(authorities))
			throw new IllegalAccessException(
					"Spring security permission error ,No Permissions found for logged in User");

		List<String> roleList = getUserRoleList(authorities);

		Long tenantId = TenantContext.getCurrentTenant();

		if (roleList.contains(SecurityUserRole.ROLE_SITEPM.toString())) {
			filterStaticContentDto = staticContentService.fetchFilterStaticContents(tenantId,SecurityUserRole.ROLE_SITEPM);
		}
		else if (roleList.contains(SecurityUserRole.ROLE_SCANVENDOR.toString())) {
			filterStaticContentDto = staticContentService.fetchFilterStaticContents(tenantId,SecurityUserRole.ROLE_SCANVENDOR);
		}
		else if (roleList.contains(SecurityUserRole.ROLE_ANNOTATOR.toString())) {
			filterStaticContentDto =  staticContentService.fetchFilterStaticContents(tenantId,SecurityUserRole.ROLE_ANNOTATOR);
		}
		else if (roleList.contains(SecurityUserRole.ROLE_REVIEWER.toString())) {
			filterStaticContentDto =  staticContentService.fetchFilterStaticContents(tenantId,SecurityUserRole.ROLE_REVIEWER);
		}
		
		return filterStaticContentDto;
		
	}

	private List<String> getUserRoleList(Collection<SimpleGrantedAuthority> authorities) {
		Iterator itr = authorities.iterator();
		List<String> roleList = new ArrayList<>();
		while (itr.hasNext()) {
			SimpleGrantedAuthority authority = (SimpleGrantedAuthority) itr.next();
			roleList.add(authority.getAuthority());
		}
		return roleList;

	}

}
