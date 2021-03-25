package com.psl.adms.web.controller.document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Tuple;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
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
import com.psl.adms.commons.dto.DocPrimaryKeyDescDto;
import com.psl.adms.commons.dto.DocumentDto;
import com.psl.adms.commons.dto.GridDto;
import com.psl.adms.commons.dto.SearchFilterExpressionDto;
import com.psl.adms.commons.dto.StatusDescDto;
import com.psl.adms.commons.entity.Batch;
import com.psl.adms.commons.entity.DocMetadataDesc;
import com.psl.adms.commons.entity.DocPrimaryKeyDesc;
import com.psl.adms.commons.entity.DocRejectionDesc;
import com.psl.adms.commons.entity.Document;
import com.psl.adms.commons.enums.DocumentStatus;
import com.psl.adms.commons.exceptions.BadRequestException;
import com.psl.adms.commons.exceptions.InvalidTransitionException;
import com.psl.adms.commons.exceptions.ResourceNotFoundException;
import com.psl.adms.commons.utils.DateMapperUtil;
import com.psl.adms.commons.utils.IdGeneratorUtil;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.dao.document.DocumentFilterRpository;
import com.psl.adms.core.service.document.DocRejectionDescService;
import com.psl.adms.core.service.document.DocumentService;
import com.psl.adms.web.controller.BaseController;

@RequestMapping("/documents")
@RestController
public class DocumentController extends BaseController {
	private static final String URL_SEPERATOR = "/";
	private static final String DOCUMENT_ROOT_FOLDER_PATH = "docRoot";
	public static final String DOC_ID_NOT_IDENTIFIABLE = "Not Identifiable";

	private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);

	@Autowired
	private DocumentService documentService;

	@Autowired
	private DocRejectionDescService docRejectionDescService;
	
	@Autowired
	private DocumentFilterRpository documentFilterRepository;



	@PostMapping
	public ResponseEntity<Object> createDocument(@RequestHeader("Authorization") String token,
							   @RequestBody @Valid DocumentDto documentDto,
							   HttpServletRequest httpServletRequest,
							   HttpMethod httpMethod) {

		try {
			String userId = getKeycloakUserId();
			checkAccess(httpServletRequest, httpMethod, token);
			
			Date date = new Date();

			Document document = (Document) ObjectMapperUtil.map(documentDto, Document.class);
			document.setInternalDocId(IdGeneratorUtil.generateInternalDocId(date));
			document.setTenantId(TenantContext.getCurrentTenant());
			document.setCreatedBy(userId);
			document.setModifiedBy(userId);
			document.setCreatedAt(date);
			document.setModifiedAt(date);

			List<DocPrimaryKeyDesc> docPrimaryKeyList = initDocPrimaryKeyDescList(documentDto, date, document, userId);

			DocMetadataDesc docMetadata = initDocMetadataDesc(documentDto, date, document, userId);

			validateDocument(document, docPrimaryKeyList);

			documentService.createDocument(document, docPrimaryKeyList, docMetadata);
			return ResponseEntity.status(HttpStatus.OK).build();
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			if (t instanceof BadRequestException) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
			}else if (t instanceof ResourceNotFoundException){
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(t.getMessage());
			}

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}
	
	private DocMetadataDesc initDocMetadataDesc(DocumentDto documentDto, Date date, Document document, String userId) {
		List<Object> docMetadata = documentDto.getDocMetadataList();
		if (CollectionUtils.isEmpty(docMetadata)) {
			return null;
		}
		
		DocMetadataDesc docMetadataDesc = new DocMetadataDesc();
		docMetadataDesc.setMetadata(docMetadata);
		docMetadataDesc.setTenantId(TenantContext.getCurrentTenant());
		docMetadataDesc.setCreatedBy(userId);
		docMetadataDesc.setModifiedBy(userId);
		docMetadataDesc.setCreatedAt(date);
		docMetadataDesc.setModifiedAt(date);

		return docMetadataDesc;
	}

	private void validateDocument(Document document, List<DocPrimaryKeyDesc> docPrimaryKeyList) {
		if (document.getRejectionId() == null || document.getRejectionId().longValue() <= 0) {
			if (document.getNumPages() <= 0) {
				throw new BadRequestException(
						"Number of pages must be greater than or equal to 1 instead of : " + document.getNumPages());
			}
			if (document.getNumPages() < document.getTotalBlankPages()) {
				throw new BadRequestException("Total number of pages " + document.getNumPages()
						+ " is less than total blank pages " + document.getTotalBlankPages());
			}
			document.setStatus(DocumentStatus.ISSUED.getstatus());
			document.setRejectionId(null);
		} else {
			document.setStatus(DocumentStatus.REJECTED.getstatus());
			document.setIsDeviation(false);
		}

		String docId = DOC_ID_NOT_IDENTIFIABLE;
		String folderPathName = null;
		if (document.getTypeId() != null && document.getTypeId().longValue() > 0) {
			if (CollectionUtils.isEmpty(docPrimaryKeyList)) {
				throw new BadRequestException("Primary key metadata not available for document");
			}
			docId = IdGeneratorUtil.generateDocId(docPrimaryKeyList);

			//append typeId with metadata for creating docId

			docId = document.getTypeId() + IdGeneratorUtil.UNDERSCORE + docId;
			folderPathName = generateDocFolderPath(TenantContext.getCurrentTenant());
		} else {
			document.setTypeId(null);
		}

		document.setDocId(docId);
		document.setFolderHandlePath(folderPathName);
	}

	private String generateDocFolderPath(Long tenantId) {
		StringBuilder str = new StringBuilder(URL_SEPERATOR);
		str.append(tenantId);
		str.append(URL_SEPERATOR);
		str.append(DOCUMENT_ROOT_FOLDER_PATH);
		return str.toString();
	}


	private List<DocPrimaryKeyDesc> initDocPrimaryKeyDescList(DocumentDto documentDto, Date date, Document document, String userId) {
		List<DocPrimaryKeyDescDto> docPrimaryKeyDescDtoList = documentDto.getDocPrimaryKeyList();
		if (CollectionUtils.isEmpty(docPrimaryKeyDescDtoList)) {
			if (document.getRejectionId() == null) {
				throw new BadRequestException("Primary key details required.");
			}
			return null;
		}

		List<DocPrimaryKeyDesc> docPrimaryKeyList = new ArrayList<>();
		for (DocPrimaryKeyDescDto docPrimaryKeyDto : docPrimaryKeyDescDtoList) {

			boolean status = isValidDocPrimaryData(docPrimaryKeyDto);
			if (!status) {
				throw new BadRequestException("Failed to add Doucment, Invalid primary key value.");
			}

			DocPrimaryKeyDesc docPrimaryKeyDesc = (DocPrimaryKeyDesc) ObjectMapperUtil.map(docPrimaryKeyDto,
					DocPrimaryKeyDesc.class);

			docPrimaryKeyDesc.setTenantId(TenantContext.getCurrentTenant());
			docPrimaryKeyDesc.setCreatedBy(userId);
			docPrimaryKeyDesc.setModifiedBy(userId);
			docPrimaryKeyDesc.setCreatedAt(date);
			docPrimaryKeyDesc.setModifiedAt(date);

			docPrimaryKeyList.add(docPrimaryKeyDesc);
		}
		return docPrimaryKeyList;
	}
	
	private boolean isValidDocPrimaryData(DocPrimaryKeyDescDto docPrimaryKeyDto) {
		String dataType = docPrimaryKeyDto.getDataType();
		if (StringUtils.isEmpty(dataType)) {
			throw new BadRequestException("Primary key dataType is required.");
		}

		String strValue = docPrimaryKeyDto.getValue();
		if (StringUtils.isEmpty(strValue)) {
			return false;
		}

		switch (dataType.toLowerCase()) {
		case "string":
			return true;
		case "long":
			try {
				Long.parseLong(strValue);
			} catch (NumberFormatException e) {
				return false;
			}

			return true;
		case "date":
			return DateMapperUtil.isValidDateFormat(strValue, DateMapperUtil.DATE_FORMAT);
		default:
			break;
		}

		return false;
	}

	@GetMapping(value = "/{id}")
	public DocumentDto fetchDocumentById(@RequestHeader("Authorization") String token, @PathVariable("id") Long id,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		Document document = documentService.fetchDocumentById(id);

		DocumentDto documentDto = (DocumentDto) ObjectMapperUtil.map(document, DocumentDto.class);

		return documentDto;
	}

	@GetMapping
	public GridDto fetchAllDocuments(@RequestHeader("Authorization") String token,
			@RequestParam Map<String, String> queryMap, HttpServletRequest httpServletRequest, HttpMethod httpMethod) {

		Long batchId = StringUtils.isEmpty(queryMap.get("batchId")) ? null : Long.parseLong(queryMap.get("batchId"));
		if (batchId == null || batchId.longValue() <= 0) {
			throw new BadRequestException("BatchId is required");
		}
		List<String> statusList = getStatusList(queryMap);

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
		GridDto documentGridDto = new GridDto();
		Pageable pageable = createPageableObject(queryMap, Batch.SORTCOL_MODIFIED_AT);
		long totalCount = documentService.fetchTotalCountByFilter(batchId, statusList);
		if (totalCount <= 0) {
			return documentGridDto;
		}

		List<Document> documentList = documentService.fetchAllDocuments(batchId, pageable, statusList);

		List<DocumentDto> documentDtoList = new ArrayList<>();
		for (Document document : documentList) {
			DocumentDto documentDto = (DocumentDto) ObjectMapperUtil.map(document, DocumentDto.class);
			if(document.getDocMetadataDesc() != null) {
				documentDto.setDocMetadataList(document.getDocMetadataDesc().getMetadata());
			}
			documentDtoList.add(documentDto);
		}

		documentGridDto.setData(documentDtoList);
		documentGridDto.setTotalCount(totalCount);

		return documentGridDto;

	}

	@PutMapping(value = "/{id}")
	public ResponseEntity updateDocument(@RequestHeader("Authorization") String token,
			@RequestBody DocumentDto documentDto, @PathVariable("id") Long id, @AuthenticationPrincipal User user,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			String userId = getKeycloakUserId();
			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

			documentDto.setId(id);
			documentDto.setTenantId(TenantContext.getCurrentTenant());
			documentDto.setModifiedBy(userId);
			Document document = (Document) ObjectMapperUtil.map(documentDto, Document.class);

			documentService.updateDocument(document);
			return ResponseEntity.ok().build();
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		} catch (Throwable t) {
			logger.error(t.getMessage());
			if (t instanceof BadRequestException || t instanceof InvalidTransitionException) {
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(t.getMessage());
			}else if (t instanceof ResourceNotFoundException){
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(t.getMessage());
			}
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(t.getMessage());
		}
	}


	@GetMapping(value = "/doc-rejection-statuses")
	public List<StatusDescDto> fetchAllDocStatuses(@RequestHeader("Authorization") String token,
			HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		String userId = getKeycloakUserId();
		logger.debug("userId=" + userId);

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		List<DocRejectionDesc> docRejectionDescList = docRejectionDescService.fetchAllDocRejectionStatuses();

		List<StatusDescDto> statusDescDtoList = new ArrayList<>();
		for (DocRejectionDesc docRejectionDesc : docRejectionDescList) {
			StatusDescDto statusDescDto = (StatusDescDto) ObjectMapperUtil.map(docRejectionDesc, StatusDescDto.class);
			statusDescDtoList.add(statusDescDto);
		}

		return statusDescDtoList;
	}

	@PostMapping(value = "/search")
	public GridDto search(@RequestParam Map<String, String> queryMap,
			@RequestBody List<SearchFilterExpressionDto> searchFilterExpressionDtoList,
			@RequestHeader("Authorization") String token, HttpServletRequest httpServletRequest,
			HttpMethod httpMethod) {

		HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

		GridDto documentGridDto = new GridDto();

		if (queryMap == null) {
			throw new BadRequestException("query parameters are found null");
		}


		List<String> parameterKeys = searchFilterExpressionDtoList.stream().map(name -> name.getName())
				.collect(Collectors.toList());

		if (!parameterKeys.contains("batchId")) {
			throw new BadRequestException("batch Id is required");
		}
		// docIssue Page: batch status will always be Recieved

		Pageable pageable = createPageableObjectForSearch(queryMap, Document.SORTCOL_MODIFIED_AT);

		long totalCount = documentService.searchCount(searchFilterExpressionDtoList);

		if (totalCount <= 0) {
			return documentGridDto;
		}
		
		List<Document> documentList = documentFilterRepository.search(searchFilterExpressionDtoList, pageable, totalCount);

		List<DocumentDto> documentDtoList = new ArrayList<>();

		for (Document document : documentList) {
			DocumentDto documentDto = (DocumentDto) ObjectMapperUtil.map(document, DocumentDto.class);
			if(document.getDocMetadataDesc() != null) {
				documentDto.setDocMetadataList(document.getDocMetadataDesc().getMetadata());
			}
			documentDtoList.add(documentDto);
		}

		documentGridDto.setData(documentDtoList);
		documentGridDto.setTotalCount(totalCount);



		return documentGridDto;

	}

}
