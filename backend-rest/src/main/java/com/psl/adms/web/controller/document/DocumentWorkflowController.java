package com.psl.adms.web.controller.document;

import com.psl.adms.commons.dto.DocumentDeduplicationDto;
import com.psl.adms.commons.dto.DocumentMessageDto;
import com.psl.adms.commons.dto.DocumentProcessResponseDto;
import com.psl.adms.commons.entity.Document;
import com.psl.adms.commons.exceptions.BadRequestException;
import com.psl.adms.commons.exceptions.InvalidTransitionException;
import com.psl.adms.commons.exceptions.ResourceNotFoundException;
import com.psl.adms.core.service.document.DocumentDeduplicationService;
import com.psl.adms.core.service.document.DocumentService;
import com.psl.adms.web.controller.BaseController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RequestMapping("/workflow")
@RestController
public class DocumentWorkflowController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentWorkflowController.class);

    @Autowired
    private DocumentDeduplicationService documentDeduplicationService;

    @Autowired
    private DocumentService documentService;

    @PostMapping(value = "/dedup")
    public ResponseEntity submitForDedup(@RequestBody @Valid DocumentDeduplicationDto documentDeduplicationDto) {
        documentDeduplicationService.submitForDedup(documentDeduplicationDto);
        return ResponseEntity.ok().body(HttpStatus.OK);
    }

    /**
     * Initiates the document processing by initiating the OCR
     * @param documentMessageDto
     */
    @PostMapping(value = "/submitocrrequest")
    public ResponseEntity startOcrRequest(@RequestHeader("Authorization") String token,
                                          @RequestBody DocumentMessageDto documentMessageDto,
                                          HttpServletRequest httpServletRequest,
                                          HttpMethod httpMethod){
        try {
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            documentService.submitOCRRequest(documentMessageDto);
            return ResponseEntity.ok().body(HttpStatus.OK);
        }catch(Throwable t){
            return handleException(t);
        }
    }

    @PostMapping(value = "/cacheocrpage")
    public ResponseEntity cacheOcrPageRequest(@RequestHeader("Authorization") String token,
                                          @RequestBody DocumentMessageDto documentMessageDto,
                                          HttpServletRequest httpServletRequest,
                                          HttpMethod httpMethod){
        try {
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            documentService.cacheOcrPage(documentMessageDto);
            return ResponseEntity.ok().body(HttpStatus.OK);
        }catch(Throwable t){
            return handleException(t);
        }
    }

    @PostMapping(value = "/submitnerebookrequest")
    public ResponseEntity submitNEREbookRequest(@RequestHeader("Authorization") String token,
                                           @RequestBody DocumentMessageDto documentMessageDto,
                                           HttpServletRequest httpServletRequest,
                                           HttpMethod httpMethod){
        documentService.publishNerEbookRequest(documentMessageDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/submitnerrequest")
    public ResponseEntity submitNERRequest(@RequestHeader("Authorization") String token,
                                           @RequestBody DocumentMessageDto documentMessageDto,
                                           HttpServletRequest httpServletRequest,
                                           HttpMethod httpMethod){
        try {
            logger.info("processing NER Request");
            String userId = getKeycloakUserId();
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            documentService.submitNERRequest(documentMessageDto);
            return ResponseEntity.ok().build();
        }catch(Throwable t){
            return handleException(t);
        }
    }

    @PostMapping(value = "/submitebookrequest")
    public ResponseEntity submitEbookRequest(@RequestHeader("Authorization") String token,
                                             @RequestBody DocumentMessageDto documentMessageDto,
                                             HttpServletRequest httpServletRequest,
                                             HttpMethod httpMethod){
        try {
            logger.info("processing Ebook Request, docId="+documentMessageDto.getDocId());
            String userId = getKeycloakUserId();
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            documentService.submitEbookRequest(documentMessageDto);
            return ResponseEntity.ok().build();
        }catch(Throwable t){
            return handleException(t);
        }
    }

    @PostMapping(value = "/submitftsrequest")
    public ResponseEntity submitFTSRequest(@RequestHeader("Authorization") String token,
                                           @RequestBody DocumentMessageDto documentMessageDto,
                                           HttpServletRequest httpServletRequest,
                                           HttpMethod httpMethod){
        try {
            logger.info("processing FTS Request docId="+documentMessageDto.getDocId());
            String userId = getKeycloakUserId();
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            documentService.submitFTSRequest(documentMessageDto);
            return ResponseEntity.ok().build();
        }catch(Throwable t){
            return handleException(t);
        }
    }

    @PostMapping(value = "/submitupdaterequest")
    public ResponseEntity submitDocStatusUpdateRequest(@RequestHeader("Authorization") String token,
                                           @RequestBody DocumentProcessResponseDto documentProcessResponseDto,
                                           HttpServletRequest httpServletRequest,
                                           HttpMethod httpMethod){
        try {
            logger.info("processing Doc status update request docId="+documentProcessResponseDto.getDocId());
            String userId = getKeycloakUserId();
//			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);
            Document document = documentService.fetchDocumentById(documentProcessResponseDto.getDocId());
            document.setStatus(documentProcessResponseDto.getDocumentStatus());
            documentService.updateDocument(document);
            return ResponseEntity.ok().build();
        }catch(Throwable t){
            return handleException(t);
        }
    }

    private ResponseEntity handleException(Throwable t){
        String message = StringUtils.isEmpty(t.getMessage())? t.getCause().getMessage():t.getMessage();
        logger.error(message);
        if(t instanceof AccessDeniedException ) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
        }else if(t instanceof BadRequestException || t instanceof InvalidTransitionException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }else if (t instanceof ResourceNotFoundException){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(message);
    }
}
