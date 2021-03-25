package com.psl.adms.web.controller.document;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.psl.adms.commons.dto.StatusDescDto;
import com.psl.adms.commons.entity.DocRejectionDesc;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.document.DocRejectionDescService;
import com.psl.adms.web.controller.BaseController;

@RequestMapping("/doc-rejection-statuses")
@RestController
public class DocRejectionDescController  extends BaseController{

    Logger logger = LoggerFactory.getLogger(DocRejectionDescController.class);

	@Autowired
	private DocRejectionDescService docRejectionDescService;

	@GetMapping
    public List<StatusDescDto> fetchAllDocStatuses(@RequestHeader("Authorization") String token,
                                                   HttpServletRequest httpServletRequest,
                                                   HttpMethod httpMethod) {
        
        HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

        List<DocRejectionDesc> docRejectionDescList = docRejectionDescService.fetchAllDocRejectionStatuses();
        
        List<StatusDescDto> statusDescDtoList = new ArrayList<>();
        for(DocRejectionDesc docRejectionDesc : docRejectionDescList) {
        	StatusDescDto statusDescDto = (StatusDescDto) ObjectMapperUtil.map(docRejectionDesc, StatusDescDto.class);
        	statusDescDtoList.add(statusDescDto);
        }
        
        return statusDescDtoList;
    }

}
