package com.psl.adms.web.controller.document;

import com.psl.adms.commons.dto.DocumentDto;
import com.psl.adms.commons.dto.DocumentRetryInfoDto;
import com.psl.adms.commons.entity.Document;
import com.psl.adms.commons.entity.DocumentRetryInfo;
import com.psl.adms.commons.exceptions.BadRequestException;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.document.DocumentRetryInfoService;
import com.psl.adms.web.controller.BaseController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RequestMapping("/doc-retry")
@RestController
public class DocumentRetryController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentRetryController.class);

    @Autowired
    private DocumentRetryInfoService documentRetryInfoService;

    @GetMapping(value = "/retryinfo")
    public DocumentRetryInfoDto getRetryInfo(@RequestHeader("Authorization") String token, @RequestParam
            Map<String, String> queryMap, HttpServletRequest httpServletRequest, HttpMethod httpMethod){

        Long docId = StringUtils.isEmpty(queryMap.get("document_id")) ? null : Long.parseLong(queryMap.get("document_id"));
        if (docId == null || docId.longValue() <= 0) {
            throw new BadRequestException("document_id is required");
        }

        String retryType = StringUtils.isEmpty(queryMap.get("retryType")) ? null : queryMap.get("retryType");
        if (StringUtils.isEmpty(retryType)) {
            throw new BadRequestException("retryType is required");
        }

        DocumentRetryInfo documentRetryInfo =  documentRetryInfoService.findByDocumentIdAndRetryType(docId, retryType);
        DocumentRetryInfoDto documentRetryInfoDto = (DocumentRetryInfoDto) ObjectMapperUtil.map(
                documentRetryInfo, DocumentRetryInfoDto.class);

        return documentRetryInfoDto;
    }

    @PostMapping
    public ResponseEntity create(@RequestHeader("Authorization") String token,
                                 @RequestBody @Valid DocumentRetryInfoDto documentRetryInfoDto,
                                 HttpServletRequest httpServletRequest,
                                 HttpMethod httpMethod){
        DocumentRetryInfo documentRetryInfo = (DocumentRetryInfo) ObjectMapperUtil.map(
                documentRetryInfoDto, DocumentRetryInfo.class);
        documentRetryInfoService.create(documentRetryInfo);
        return ResponseEntity.ok(HttpStatus.CREATED);
    }

    @PostMapping(value = "/increment")
    public ResponseEntity increment(@RequestHeader("Authorization") String token,
                                 @RequestBody @Valid DocumentRetryInfoDto documentRetryInfoDto,
                                 HttpServletRequest httpServletRequest,
                                 HttpMethod httpMethod){
        DocumentRetryInfo documentRetryInfo = (DocumentRetryInfo) ObjectMapperUtil.map(
                documentRetryInfoDto, DocumentRetryInfo.class);
        documentRetryInfoService.incrementRetryCount(documentRetryInfoDto.getDocumentId(),
                documentRetryInfoDto.getRetryType());
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
