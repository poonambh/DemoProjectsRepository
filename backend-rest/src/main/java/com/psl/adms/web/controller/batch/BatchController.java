package com.psl.adms.web.controller.batch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.xml.ws.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
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
import com.psl.adms.commons.dto.BatchDto;
import com.psl.adms.commons.dto.DocumentDto;
import com.psl.adms.commons.dto.GridDto;
import com.psl.adms.commons.dto.SearchFilterExpressionDto;
import com.psl.adms.commons.entity.Batch;
import com.psl.adms.commons.entity.Document;
import com.psl.adms.commons.enums.BatchStatus;
import com.psl.adms.commons.enums.DocumentStatus;
import com.psl.adms.commons.exceptions.BadRequestException;
import com.psl.adms.commons.utils.DateMapperUtil;
import com.psl.adms.commons.utils.IdGeneratorUtil;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.batch.BatchService;
import com.psl.adms.core.service.document.DocumentService;
import com.psl.adms.web.controller.BaseController;

@RestController
@RequestMapping("/batches")
public class BatchController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(BatchController.class);
	

	@Autowired
	private BatchService batchService;

	@Autowired
	private DocumentService documentService;

	@PostMapping
	public ResponseEntity createBatch(@Valid @RequestBody BatchDto batchDto,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {
		try {
			String userId = getKeycloakUserId();
			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
			Date date = new Date();
			Batch batch = (Batch) ObjectMapperUtil.map(batchDto, Batch.class);
			batch.setStatus(BatchStatus.RECEIVED.getstatus());
			batch.setBatchId(IdGeneratorUtil.generateId(IdGeneratorUtil.BATCH_ID_PREFIX));
			batch.setTenantId(TenantContext.getCurrentTenant());
			batch.setCreatedBy(userId);
			batch.setModifiedBy(userId);
			batch.setCreatedAt(date);
			batch.setModifiedAt(date);

			batchService.createBatch(batch);
			return ResponseEntity.ok().build();
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	@GetMapping(value = "/{id}")
	public BatchDto fetchBatchById(@PathVariable("id") Long id, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		Batch batch = batchService.fetchBatchById(id);

		BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);

		return batchDto;
	}

	@GetMapping
	public GridDto fetchAllBatch(@RequestParam Map<String, String> queryMap,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		GridDto batchGridDto = new GridDto();

		Date startDate = StringUtils.isEmpty(queryMap.get("startDate")) ? null
				: DateMapperUtil.getStartOfDateFromString(queryMap.get("startDate"));
		Date endDate = StringUtils.isEmpty(queryMap.get("endDate")) ? null
				: DateMapperUtil.getEndOfDateFromString(queryMap.get("endDate"));

		String search = StringUtils.isEmpty(queryMap.get("search")) ? "" : queryMap.get("search");

		String status = StringUtils.isEmpty(queryMap.get("status")) ? "" : queryMap.get("status");

		List<String> statusList = new ArrayList<String>();
		if (!StringUtils.isEmpty(status)) {
			statusList = Arrays.asList(status.trim().split(","));
			statusList = statusList.stream().map(value -> BatchStatus.getValidBatchStatus(value))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		Pageable pageable = createPageableObject(queryMap, Batch.SORTCOL_MODIFIED_AT);

		long totalCount = batchService.fetchTotalCountByFilter(search, startDate, endDate, statusList, pageable);
		if (totalCount <= 0) {
			return batchGridDto;
		}

		List<Batch> batchList = batchService.fetchAllBatch(search, startDate, endDate, statusList, pageable);

		List<BatchDto> batchDtoList = new ArrayList<>();
		for (Batch batch : batchList) {
			BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);
			batchDtoList.add(batchDto);
		}

		batchGridDto.setData(batchDtoList);
		batchGridDto.setTotalCount(totalCount);

		return batchGridDto;
	}

	@GetMapping(value = "/bystatus")
	public GridDto fetchAllBatchByStatus(@RequestParam Map<String, String> queryMap,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		GridDto batchGridDto = new GridDto();

		String status = StringUtils.isEmpty(queryMap.get("status")) ? "" : queryMap.get("status");

		List<String> statusList = new ArrayList<String>();
		if (!StringUtils.isEmpty(status)) {
			statusList = Arrays.asList(status.trim().split(","));
			statusList = statusList.stream().map(value -> DocumentStatus.getValidDocumentStatus(value))
					.collect(Collectors.toCollection(ArrayList::new));
		}
		Pageable pageable = createPageableObject(queryMap, Batch.SORTCOL_BATCH_ID);

		long totalCount = batchService.fetchTotalCountByBatchStatus(statusList);
		if (totalCount <= 0) {
			return batchGridDto;
		}

		List<BatchDto> batchDtoList = batchService.fetchAllBatchByStatus(statusList, pageable);

		/*
		 * List<BatchDto> batchDtoList = new ArrayList<>(); for(Batch batch : batchList)
		 * { BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);
		 * batchDtoList.add(batchDto); }
		 */

		batchGridDto.setData(batchDtoList);
		batchGridDto.setTotalCount(totalCount);

		return batchGridDto;
	}

	@GetMapping(value = "/{id}/documents")
	public List<DocumentDto> fetchAllDocumentsByBatchId(@RequestParam Map<String, String> queryMap,
			@PathVariable("id") Long id, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {

		List<String> statusList = getStatusList(queryMap);
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		queryMap.put("batchId", String.valueOf(id));

		List<Document> documentList = documentService.fetchAllDocumentsWithoutPagination(id, statusList);

		List<DocumentDto> documentDtoList = new ArrayList<>();
		for (Document document : documentList) {
			DocumentDto documentDto = (DocumentDto) ObjectMapperUtil.map(document, DocumentDto.class);
			documentDtoList.add(documentDto);
		}

		return documentDtoList;
	}

	@PutMapping(value = "/{id}")
	public ResponseEntity updateBatch(@RequestBody BatchDto batchDto, @PathVariable("id") Long id,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {
		try {
			String userId = getKeycloakUserId();
			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
			Batch batch = getBatchObjectAfterRequestValidation(batchDto, id, userId);

			batchService.updateBatch(batch);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	@GetMapping(value = "/page-count")
	public Map<String, Long> getTotalPageCountByBatchIds(@RequestParam("batchIds") List<Long> batchIds,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		if (CollectionUtils.isEmpty(batchIds)) {
			throw new BadRequestException("Batch Ids is required.");
		}

		long totalPageCount = documentService.getTotalPageCountByBatchIds(batchIds);

		Map<String, Long> resultMap = new HashMap<>();
		resultMap.put("totalPageCount", totalPageCount);

		return resultMap;
	}

	private Batch getBatchObjectAfterRequestValidation(BatchDto batchDto, Long id, String userId) {
		Batch batch = batchService.fetchBatchById(id);
		long docCount = documentService.countByBatchId(id);

		Date date = new Date();
		batch.setModifiedBy(userId);
		batch.setModifiedAt(date);

		String status = batch.getStatus();
		if (batchDto.isDirty()) {
			batch.setDirty(batchDto.isDirty());
		}

		if (batchDto.getNoOfDocuments() > 0) {
			if (!(BatchStatus.RECEIVED.getstatus().equalsIgnoreCase(batch.getStatus())
					|| BatchStatus.ISSUE_IN_PROGRESS.getstatus().equalsIgnoreCase(batch.getStatus()))) {
				throw new BadRequestException(
						"Cannot update no. of documents after batch are issued. for batchId : " + batch.getId());
			}

			if (batchDto.getNoOfDocuments() < docCount) {
				throw new BadRequestException("No. of documents :" + batchDto.getNoOfDocuments()
						+ " cannot be less that added documents. for batchId : " + batch.getId());
			} else if (batchDto.getNoOfDocuments() == docCount) {
				status = BatchStatus.ISSUED.getstatus();
			}
			batch.setNoOfDocuments(batchDto.getNoOfDocuments());
		}

		if (!StringUtils.isEmpty(batchDto.getStatus())) {
			String batchStatus = BatchStatus.getValidBatchStatus(batchDto.getStatus());
			if (StringUtils.isEmpty(batchStatus)) {
				throw new BadRequestException("Invalid batch status.");
			}

			if (batchStatus.equalsIgnoreCase(batch.getStatus())) {
				throw new BadRequestException("Batch status is already updated to " + batchStatus);
			}

			if (!BatchStatus.REJECTED.getstatus().equalsIgnoreCase(batchStatus)
					&& docCount < batch.getNoOfDocuments()) {
				throw new BadRequestException("Cannot update status before all the documents are added.");
			}

			if (BatchStatus.RETURNED.getstatus().equalsIgnoreCase(batch.getStatus())) {
				throw new BadRequestException("Cannot update status after documents are returned.");
			}

			if (validateReturnStatusUpdation(batch, batchStatus)) {
				throw new BadRequestException(
						"Batch cannot be marked as returned before rejected or scan is complete.");
			}

			status = batchStatus;
		}

		batch.setStatus(status);
		batch.setModifiedBy(userId);
		batch.setModifiedAt(date);
		return batch;
	}

	private boolean validateReturnStatusUpdation(Batch batch, String batchStatus) {
		return BatchStatus.RETURNED.getstatus().equalsIgnoreCase(batchStatus)
				&& !(BatchStatus.SCAN_COMPLETED.getstatus().equalsIgnoreCase(batch.getStatus())
						|| BatchStatus.REJECTED.getstatus().equalsIgnoreCase(batch.getStatus()));
	}

	@GetMapping(value = "/{id}/audit-details")
	public List<BatchDto> fetchBatchAuditData(@RequestParam Map<String, String> queryMap) {

		List<Batch> batchList = batchService.fetchBatchAuditData(queryMap);

		List<BatchDto> batchDtoList = new ArrayList<>();
		for (Batch batch : batchList) {
			BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);
			batchDtoList.add(batchDto);
		}

		return batchDtoList;
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
		
		
		Pageable pageable = createPageableObjectForSearch(queryMap, Batch.SORTCOL_MODIFIED_AT);
		

		long totalCount = batchService.searchCount(searchFilterExpressionDtoList);
		
		if (totalCount <= 0) {
			return batchGridDto;
		}

		List<Batch> batchList = batchService.search(searchFilterExpressionDtoList,pageable,totalCount);

		List<BatchDto> batchDtoList = new ArrayList<>();
		
		for (Batch batch : batchList) {
			BatchDto batchDto = (BatchDto) ObjectMapperUtil.map(batch, BatchDto.class);
			batchDtoList.add(batchDto);
		}

		batchGridDto.setData(batchDtoList);
		batchGridDto.setTotalCount(totalCount);

		return batchGridDto;
		
		
	}

}
