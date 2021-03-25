package com.psl.adms.web.controller.document;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.psl.adms.core.service.document.DocumentFileUploadService;
import com.psl.adms.web.controller.BaseController;

@RestController
@RequestMapping("/documents")
@PropertySource(value = "application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
public class DocumentFileUploadController extends BaseController {
	private static Logger logger = LoggerFactory.getLogger(DocumentFileUploadController.class);
	
	@Autowired
	private DocumentFileUploadService documentFileUploadService;

	@Value("${document.newfiles.path}")
	private String documentNewFilesPath;
	
	@Value("${document.backup.path}")
	private String documentBackupPath;

	@PostMapping(value="/{internalDocId}/file-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Object> uploadDocumentFiles(@RequestParam("file") MultipartFile file,
													@PathVariable("internalDocId") String internalDocId,
													@RequestHeader("Authorization") String token, 
													HttpServletRequest httpServletRequest, HttpMethod httpMethod) {
		try {
			logger.debug("in uploadDocumentFiles : " + internalDocId);
			
			documentFileUploadService.processForRegenerateEbook(file, internalDocId, documentNewFilesPath, documentBackupPath);
		} catch (AccessDeniedException e) {
			logger.error(e.getMessage());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
		}

		return new ResponseEntity<Object>("uploadDocumentFiles : file Uploaded successfully", HttpStatus.OK);
	}

}
