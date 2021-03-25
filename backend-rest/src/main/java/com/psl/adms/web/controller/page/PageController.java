package com.psl.adms.web.controller.page;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.psl.adms.commons.context.TenantContext;
import com.psl.adms.commons.dto.BulkPageDto;
import com.psl.adms.commons.dto.PageDto;
import com.psl.adms.commons.entity.Page;
import com.psl.adms.commons.enums.PageStatus;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.page.PageService;
import com.psl.adms.web.controller.BaseController;

@RestController
@RequestMapping("/pages")
public class PageController  extends BaseController{
	
    Logger logger = LoggerFactory.getLogger(PageController.class);
	
	@Autowired
	private PageService pageService;
	
	@PostMapping
    public void createPage(@RequestBody @Valid PageDto pageDto,
    		@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod)  {
		String userId = getKeycloakUserId();
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		Page page = (Page) ObjectMapperUtil.map(pageDto, Page.class);

		Date date = new Date();

		// TODO : date and user created id, need to update docId creation logic
		
		page.setTenantId(TenantContext.getCurrentTenant());
		page.setCreatedBy(userId);
		page.setModifiedBy(userId);
		page.setCreatedAt(date);
		page.setModifiedAt(date);
		page.setStatus(PageStatus.ISSUED.getstatus());
    	pageService.createPage(page);
    }
	
	@PostMapping
	@RequestMapping("/bulk")
    public void createBulkPages(@RequestParam BulkPageDto dto,
    		@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod) {
		String userId = getKeycloakUserId();
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		dto.setModifiedBy(userId);
		dto.setCreatedBy(userId);
		pageService.createPages(dto);
    }


	@GetMapping(value="/{id}")
    public PageDto fetchPageById(@PathVariable("id") Long id,
    		@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod){

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

    	Page page = pageService.fetchPageById(id);
    	PageDto pageDto = (PageDto) ObjectMapperUtil.map(page, PageDto.class);
        return pageDto;
    }

	@GetMapping
    public List<PageDto> fetchAllPages(@RequestParam Map<String, String> queryMap,
    		@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod){

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		List<Page> pageList = pageService.fetchAllPages();
        
        List<PageDto> pageDtoList = new ArrayList<>();
        for(Page page : pageList) {
        	PageDto pageDto = (PageDto) ObjectMapperUtil.map(page, PageDto.class);
        	pageDtoList.add(pageDto);
        }
        return pageDtoList;
    }

	@PutMapping(value="/{id}")
    public void updatePage(@RequestBody PageDto pageDto, @PathVariable("id") Long id,
    		@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod) {

		String userId = getKeycloakUserId();
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		pageDto.setId(id);
		pageDto.setTenantId(TenantContext.getCurrentTenant());
		pageDto.setModifiedBy(userId);
		Page page = (Page) ObjectMapperUtil.map(pageDto, Page.class);
    	pageService.updatePage(page);
    }
	
}
