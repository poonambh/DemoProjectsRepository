package com.psl.adms.web.controller.annotationreviewer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.psl.adms.commons.dto.AutoAnnotationAssignmentDto;
import com.psl.adms.commons.dto.AutoAnnotationReviewerAssignmentDto;
import com.psl.adms.commons.dto.BatchDocIdDto;
import com.psl.adms.commons.dto.BatchDto;
import com.psl.adms.commons.dto.DocVendorDto;
import com.psl.adms.commons.dto.GridDto;
import com.psl.adms.commons.dto.SearchFilterExpressionDto;
import com.psl.adms.commons.entity.AnnotationAssignment;
import com.psl.adms.commons.entity.AnnotationReviewerAssignment;
import com.psl.adms.commons.entity.AutoAnnotationReviewerAssignment;
import com.psl.adms.commons.entity.Batch;
import com.psl.adms.commons.enums.SecurityUserRole;
import com.psl.adms.commons.exceptions.BadRequestException;
import com.psl.adms.commons.utils.DateMapperUtil;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.annotationreviewerassignment.AnnotationReviewerAssignmentService;
import com.psl.adms.core.service.batch.BatchService;
import com.psl.adms.web.controller.BaseController;

@RestController
@RequestMapping("/annotation-review-vendor")
public class AnnotationReviewerController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(AnnotationReviewerController.class);

	private static final String GROUP_ID = "groupId";
	private static final String ASSIGNMENT_DATE = "assignmentDate";

	@Autowired
	private AnnotationReviewerAssignmentService reviewerAssignService;

	@PostMapping
	@RequestMapping("/assign")
	public void assignVendor(@RequestBody BatchDocIdDto batchDocIdDto, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		String userId = getKeycloakUserId();
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		reviewerAssignService.assignDocsToReviewerVendor(batchDocIdDto, userId);
	}

	@GetMapping
	@RequestMapping("/allassignments")
	public GridDto fetchAllReviewersAndDocumentCount(Map<String, String> queryMap,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest, HttpMethod httpMethod)
			throws IllegalAccessException {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		@SuppressWarnings("unchecked")
		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder
				.getContext().getAuthentication().getAuthorities();

		String userId = getKeycloakUserId();

		GridDto documentGridDto = new GridDto();
		List<DocVendorDto> vendorDocDtoList = null;
		Long totalCount = 0L;

		if (CollectionUtils.isEmpty(authorities))
			throw new IllegalAccessException(
					"Spring security permission error ,No Permissions found for logged in User");// log or exception;

		String groupId = getGroupIdFromUserId(userId);

		List<String> roleList = getUserRoleList(authorities);

		Pageable pageable = createPageableObject(queryMap, AnnotationReviewerAssignment.GROUP_ID_SORT_COLUMN);

		if (roleList.contains(SecurityUserRole.ROLE_REVIEWER.toString())) {

			totalCount = reviewerAssignService.getTotalCountOfReviewersAndDocCount(groupId);
			vendorDocDtoList = reviewerAssignService.fetchReviewerAndDocumentCount(pageable, groupId);

		} else {
			if (roleList.contains(SecurityUserRole.ROLE_SITEPM.toString())) {

				totalCount = reviewerAssignService.getTotalCountOfAllReviewersAndDocCount();
				vendorDocDtoList = reviewerAssignService.fetchAllReviewersAndDocumentCount(pageable);

			}
		}

		if (CollectionUtils.isEmpty(vendorDocDtoList))
			throw new IllegalAccessException("No Data Found for userId : " + userId);// log or exception;

		documentGridDto.setTotalCount(totalCount);
		documentGridDto.setData(vendorDocDtoList);
		return documentGridDto;
	}

	@GetMapping
	@RequestMapping("/assigneddates/{groupId}")
	public GridDto fetchAssignmentDates(@PathVariable("groupId") String groupId,
			@RequestParam Map<String, String> queryMap, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		GridDto dto = new GridDto();

		Pageable pageable = createPageableObject(queryMap, AnnotationReviewerAssignment.ASSIGNMENT_DATE_SORT_COLUMN);

		long totalCount = reviewerAssignService.getTotalCountOfAssignmentDate(groupId);

		List<Date> assignmentDateList = reviewerAssignService.fetchAssignmentDates(groupId, pageable);

		dto.setTotalCount(totalCount);
		dto.setData(assignmentDateList);

		return dto;

	}

	@GetMapping
	@RequestMapping("/assigneddocuments")
	public GridDto fetchAssignedDocuments(@RequestParam Map<String, String> queryMap,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		GridDto dto = new GridDto();

		String groupId = StringUtils.isEmpty(queryMap.get(GROUP_ID)) ? null : queryMap.get(GROUP_ID);
		String assignmentDate = StringUtils.isEmpty(queryMap.get(ASSIGNMENT_DATE)) ? null
				: queryMap.get(ASSIGNMENT_DATE);

		String formattedAssignmentDate = convertDate(assignmentDate);
		
		Pageable pageable = createPageableObject(queryMap, AutoAnnotationReviewerAssignment.GROUP_ID_SORT_COLUMN);

		Long totalCount = reviewerAssignService.getTotalCountOfAssignedDocuments(groupId, formattedAssignmentDate);

		List<AutoAnnotationReviewerAssignmentDto> listOfDocuments = reviewerAssignService
				.fetchReviewVendorAssignedDocuments(groupId, formattedAssignmentDate, pageable);

		dto.setTotalCount(totalCount);
		dto.setData(listOfDocuments);

		return dto;

	}
	
	@PostMapping(value = "/search")
	public GridDto search(@RequestParam Map<String, String> queryMap,
			@RequestBody List<SearchFilterExpressionDto> searchFilterExpressionDtoList ,@RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		
		GridDto batchGridDto = new GridDto();
		
		if(queryMap == null) {
			throw new BadRequestException("query parameters are found null");
		}
		
		/*if(CollectionUtils.isEmpty(searchFilterExpressionDtoList)) {
			throw new BadRequestException("payload is found null");
		}*/
		
		Pageable pageable = createPageableObjectForSearch(queryMap, Batch.SORTCOL_MODIFIED_AT);
		

		long totalCount = reviewerAssignService.searchAssignedBatchCount(searchFilterExpressionDtoList);
		
		if (totalCount <= 0) {
			return batchGridDto;
		}

		List<Batch> batchList = reviewerAssignService.searchAssignedBatches(searchFilterExpressionDtoList,pageable,totalCount);

		List<BatchDto> batchDtoList = new ArrayList<>();
		
		for (Batch batch : batchList) {
			BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);
			batchDtoList.add(batchDto);
		}

		batchGridDto.setData(batchDtoList);
		batchGridDto.setTotalCount(totalCount);

		return batchGridDto;
		
		
	}

	private String convertDate(String assignmentDate) {

		DateFormat outputFormat = new SimpleDateFormat(DateMapperUtil.CONVERTED_TIMESTAMP_FORMAT);
		DateFormat inputFormat = new SimpleDateFormat(DateMapperUtil.ISO8601_TIMESTAMP_FORMAT);
		String convertedAssignmentDate = "";
		try {
			Date date = inputFormat.parse(assignmentDate);
			convertedAssignmentDate = outputFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return convertedAssignmentDate;
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
