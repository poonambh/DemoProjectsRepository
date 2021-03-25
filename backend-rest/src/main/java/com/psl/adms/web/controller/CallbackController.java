package com.psl.adms.web.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.psl.adms.commons.context.SystemUserContext;
import com.psl.adms.commons.context.TenantContext;
import com.psl.adms.commons.dto.DocFileBackupDto;
import com.psl.adms.commons.dto.DocRegenerateEbookDto;
import com.psl.adms.commons.dto.DocumentMessageDto;
import com.psl.adms.commons.dto.DocumentProcessResponseDto;
import com.psl.adms.commons.entity.RequestTracker;
import com.psl.adms.commons.enums.RequestTrackerTypes;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.processor.document.DocumentProcessor;
import com.psl.adms.core.service.document.DocumentService;
import com.psl.adms.core.service.requesttracker.RequestTrackerService;

@RequestMapping("/callback")
@RestController
public class CallbackController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(CallbackController.class);

	@Autowired
	private DocumentService documentService;

	@Autowired
	private DocumentProcessor documentProcessor;

	@Autowired
	private RequestTrackerService requestTrackerService;

	/**
	 * Initiates NER and Ebook generation if OCR was done successfully
	 * 
	 * @param processResponseDto
	 */
	// TODO:fix me
	@PostMapping(value = "/ocr/response")
	public ResponseEntity processOCRResponse(@RequestHeader("Authorization") String token,
			@RequestBody DocumentProcessResponseDto processResponseDto, @AuthenticationPrincipal User user,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			String userId = SystemUserContext.getCurrentSystemUser();
			logger.info("Processing OCR Response by={}, for doc id : {}", userId, processResponseDto.getDocId());
			addResponseEntryInRequestTracker(processResponseDto, userId, RequestTrackerTypes.OCR_RES.getValue());
			documentProcessor.processOCRResponse(processResponseDto, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			// TODO:add exception handling, retry
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	// TODO:fix me
	@PostMapping(value = "/ner/response")
	public ResponseEntity processNERResponse(@RequestHeader("Authorization") String token,
			@RequestBody DocumentProcessResponseDto processResponseDto, @AuthenticationPrincipal User user,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			String userId = SystemUserContext.getCurrentSystemUser();
			logger.info("processing NER Response by ={}, for doc id : {}", userId, processResponseDto.getDocId());
			addResponseEntryInRequestTracker(processResponseDto, userId, RequestTrackerTypes.NER_RES.getValue());
			documentProcessor.processNerResponse(processResponseDto, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			// TODO:add exception handling
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	// TODO:fix me
	@PostMapping(value = "/ebook/response")
	public ResponseEntity processEbookResponse(@RequestHeader("Authorization") String token,
			@RequestBody DocumentProcessResponseDto processResponseDto, @AuthenticationPrincipal User user,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			String userId = SystemUserContext.getCurrentSystemUser();
			logger.info("processing Ebook Responseby={}, for doc id : {}", userId, processResponseDto.getDocId());
			addResponseEntryInRequestTracker(processResponseDto, userId, RequestTrackerTypes.EBOOK_RES.getValue());
			documentProcessor.processEbookResponse(processResponseDto, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			// TODO:add exception handling
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	@PostMapping("/single-page-ocr/response")
	public ResponseEntity docSinglePageOcrValidation(@RequestBody DocumentProcessResponseDto documentProcessResponseDto,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {
		try {
			logger.debug("processing docSinglePageOcrValidation value : {}", documentProcessResponseDto.getDocId());
			String userId = SystemUserContext.getCurrentSystemUser();
			addResponseEntryInRequestTracker(documentProcessResponseDto, userId,
					RequestTrackerTypes.SINGLE_PAGE_OCR_RES.getValue());

			DocumentMessageDto documentMessageDto = documentProcessor
					.processSinglePageOCRResponse(documentProcessResponseDto, userId);
			return ResponseEntity.ok().body(HttpStatus.OK);
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Exception in docSinglePageOcrValidation for docId : " + documentProcessResponseDto.getDocId()
							+ " with exception : " + t.getMessage());
		}
	}

	@PostMapping(value = "/status/response")
	public ResponseEntity processStatusUpdate(@RequestHeader("Authorization") String token,
			@RequestBody DocumentProcessResponseDto processResponseDto, @AuthenticationPrincipal User user,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			String userId = SystemUserContext.getCurrentSystemUser();
			logger.info("Processing status Update by={}, for doc id : {}" + userId, processResponseDto.getDocId());
			addResponseEntryInRequestTracker(processResponseDto, userId,
					RequestTrackerTypes.STATUS_UPDATE_RES.getValue());
			documentProcessor.processDocumentStatusUpdate(processResponseDto, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			// TODO:add exception handling, retry
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	@PostMapping(value = "/document-file-backup/response")
	public ResponseEntity<Object> processDocumentFileBackupResponse(
			@RequestBody List<DocFileBackupDto> docFileBackupDtoList, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {

			logger.info("processDocumentFileBackupResponse docFileBackupDtoList size : {}",
					docFileBackupDtoList.size());

			String userId = SystemUserContext.getCurrentSystemUser();
			documentService.processDocumentFileBackupResponse(docFileBackupDtoList, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	@PostMapping(value = "/regenerate-ebook/request")
	public ResponseEntity<Object> processRegenerateEbookRequest(
			@RequestBody DocRegenerateEbookDto docRegenerateEbookDto, @RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {

			logger.info("processRegenerateEbookRequest docId : {}", docRegenerateEbookDto.getDocumentId());

			String userId = SystemUserContext.getCurrentSystemUser();
			documentService.processRegenerateEbookRequest(docRegenerateEbookDto, userId);
			return ResponseEntity.status(HttpStatus.OK).body(HttpStatus.OK.value());
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}

	private void addResponseEntryInRequestTracker(DocumentProcessResponseDto documentProcessResponseDto, String userId,
			String requestTrackerType) {
		try {
			Date date = new Date();
			RequestTracker requestTracker = new RequestTracker();
			requestTracker.setTenantId(TenantContext.getCurrentTenant());
			requestTracker.setDocumentId(documentProcessResponseDto.getDocId());
			requestTracker.setExecutionId(documentProcessResponseDto.getExecutionId());
			requestTracker.setType(requestTrackerType);
			requestTracker.setCreatedAt(date);
			requestTracker.setModifiedAt(date);
			requestTracker.setCreatedBy(userId);
			requestTracker.setModifiedBy(userId);

			String responseAsJson = ObjectMapperUtil.toJsonString(documentProcessResponseDto);
			requestTracker.setRequestResponseJson(responseAsJson);

			requestTrackerService.addRequestTrackerDetails(requestTracker);
		} catch (JsonProcessingException e) {
			logger.error("Exception in 'Callback controller' addRequestEntryInRequestTracker for docId ='{}', and exception is : {}", documentProcessResponseDto.getDocId(), e);
		}
	}
}
