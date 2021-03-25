package com.psl.adms.web.scheduler;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.psl.adms.commons.context.SystemUserContext;
import com.psl.adms.commons.context.TenantContext;
import com.psl.adms.commons.dto.CustomerDto;
import com.psl.adms.commons.dto.DocFileBackupDto;
import com.psl.adms.commons.entity.DocFileBackup;
import com.psl.adms.commons.entity.Document;
import com.psl.adms.commons.enums.DocFileBackupStatus;
import com.psl.adms.commons.enums.DocFileBackupType;
import com.psl.adms.commons.enums.DocumentStatus;
import com.psl.adms.commons.enums.SchedulerTasks;
import com.psl.adms.commons.enums.ServiceDiscoveryMetadata;
import com.psl.adms.commons.enums.ServiceName;
import com.psl.adms.commons.exceptions.DedupValidationFailureException;
import com.psl.adms.commons.utils.ObjectMapperUtil;
import com.psl.adms.core.service.document.DocFileBackupService;
import com.psl.adms.core.service.document.DocumentService;
import com.psl.adms.core.service.servicediscovery.ServiceDiscoveryService;
import com.psl.adms.core.service.servicediscovery.StorageService;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Component
@PropertySource(value = "application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
public class DocFileBackupScheduledTasks {
	private static final Logger logger = LoggerFactory.getLogger(DocFileBackupScheduledTasks.class);

	@Autowired
	private DocumentService documentService;
	
	@Autowired
	private StorageService storageService;
	
	@Autowired
	private DocFileBackupService docFileBackupService;
	
	@Autowired
	private ServiceDiscoveryService serviceDiscoveryService;
	
	@Value("${document.backup.path}")
	private String documentBackupPath;
	
	@Value("${document.backup.sizelimit}")
	private int documentBackupSizeLimit;
	
	@Value("${keycloak.realm}")
	private String realmName;

	@Scheduled(cron = "${document.backupscheduler.cron}")
	@SchedulerLock(name = SchedulerTasks.DOCUMENT_FILE_BACKUP_SCHEDULER)
	public void documentBackupScheduledTask() throws InterruptedException {
		
		try {
			logger.info("documentBackupScheduledTask start ");
			LockAssert.assertLocked();

			Date currentDate = new Date();
			String userId = SystemUserContext.getCurrentSystemUser();
			
			CustomerDto customerDto = serviceDiscoveryService.fetchCustomerByRealmName(realmName, ServiceDiscoveryMetadata.CUSTOMER_DETAILS_BY_REALM_NAME);
			if (customerDto != null) {
				TenantContext.setCurrentTenant(customerDto.getId());
			}
			
			List<DocFileBackupDto> docFileBackupDtoList = new ArrayList<>();
			
			//fetch all OCRed done documents
			List<Document> ocredDocumentList = documentService.fetchBackupPendingDocumentDetailsByStatuses(
					DocumentStatus.getOCRedSuccessToReviewPendingStatuses(), DocFileBackupStatus.getDoneOrInProgressStatuses(), Document.EBOOK_SUCCESS_STATUS);
			
			for (Document document : ocredDocumentList) {
				logger.debug("documentBackupScheduledTask OCRed docId : {}", document.getId());
				
				DocFileBackupDto ocredDocFileBackupDto = setDocFileBackupDto(currentDate, userId, document,
						DocFileBackupType.OCR_DONE.getType());
				
				docFileBackupDtoList.add(ocredDocFileBackupDto);
			}
			
			//fetch all Review done done documents
			List<Document> reviewDoneDocumentList = documentService.fetchBackupPendingDocumentDetailsByStatuses(
					DocumentStatus.getReviewDoneStatuses(), DocFileBackupStatus.getAllFileBackupDoneOrInProgressStatuses(), Document.EBOOK_DEFAULT_STATUS);
			
			for (Document document : reviewDoneDocumentList) {
				logger.debug("documentBackupScheduledTask Reviewed done docId : {}", document.getId());

				DocFileBackupDto reviewDoneDocFileBackupDto = setDocFileBackupDto(currentDate, userId, document,
						DocFileBackupType.REVIEW_DONE.getType());
				
				docFileBackupDtoList.add(reviewDoneDocFileBackupDto);
			}
			
			if(CollectionUtils.isEmpty(docFileBackupDtoList)) {
				logger.info("documentBackupScheduledTask no document found for file backup.");
				logger.info("documentBackupScheduledTask end");
				return;
			}

			logger.debug("documentBackupScheduledTask send for copy file on backup path from S2 with docFileBackupDtoList size : {}", docFileBackupDtoList.size());
			
			backupDocFileUsingS2Api(docFileBackupDtoList);
			
		} catch (Exception e) {
			logger.error("Exception in documentBackupScheduledTask : {}", e);
			throw new DedupValidationFailureException("Exception in documentBackupScheduledTask with exception : " + e);
		}
		
		logger.info("documentBackupScheduledTask end");
	}

	private void backupDocFileUsingS2Api(List<DocFileBackupDto> docFileBackupDtoList) throws InterruptedException {
		List<List<DocFileBackupDto>> docFileBackupDtoSplitList = new ArrayList<>();
		docFileBackupDtoSplitList.add(docFileBackupDtoList);
		if(docFileBackupDtoList.size() > documentBackupSizeLimit) {
			docFileBackupDtoSplitList = ListUtils.partition(docFileBackupDtoList, documentBackupSizeLimit);
		}
		
		for (List<DocFileBackupDto> list : docFileBackupDtoSplitList) {
			String message = storageService.copyDocFilesOnBackupPathS2API(list, TenantContext.getCurrentTenant());
			if(!StringUtils.isEmpty(message)) {
				logger.debug("documentBackupScheduledTask update/add doc file backup status for size : {}", list.size());
				addOrUpateDocFileBackupStatus(list);
			}
			
			if(docFileBackupDtoSplitList.size() > 1) {
				//to delay the next API call and database update
				Thread.sleep(5000);
			}
		}
	}

	private void addOrUpateDocFileBackupStatus(List<DocFileBackupDto> list) {
		List<DocFileBackup> addOrUpdateList = new ArrayList<>();
		for (DocFileBackupDto docFileBackupDto : list) {
			DocFileBackup docFileBackup = new DocFileBackup();
			docFileBackup = (DocFileBackup) ObjectMapperUtil.map(docFileBackupDto, DocFileBackup.class);
			
			addOrUpdateList.add(docFileBackup);
		}
		docFileBackupService.addBulkDocFileBackupDetails(addOrUpdateList);
	}

	private DocFileBackupDto setDocFileBackupDto(Date currentDate, String userId, Document document, String type) {
		DocFileBackupDto docFileBackupDto = new DocFileBackupDto();

		if(document.getDocFileBackup() == null) {
			docFileBackupDto.setTenantId(document.getTenantId());
			docFileBackupDto.setDocumentId(document.getId());
			docFileBackupDto.setStatus(DocFileBackupStatus.FILE_TRANSFER_IN_PROGRESS.getstatus());
			docFileBackupDto.setCreatedAt(currentDate);
			docFileBackupDto.setCreatedBy(userId);
			docFileBackupDto.setModifiedAt(currentDate);
			docFileBackupDto.setModifiedBy(userId);
		}else {
			docFileBackupDto = (DocFileBackupDto) ObjectMapperUtil.map(document.getDocFileBackup(), DocFileBackupDto.class);
			docFileBackupDto.setStatus(DocFileBackupStatus.FILE_TRANSFER_IN_PROGRESS.getstatus());
			docFileBackupDto.setModifiedAt(currentDate);
			docFileBackupDto.setModifiedBy(userId);
		}
		
		docFileBackupDto.setType(type);
		
		String sourcePath = document.getFolderHandlePath();
		if (DocFileBackupType.OCR_DONE.getType().equalsIgnoreCase(docFileBackupDto.getType())) {
			sourcePath = sourcePath.concat("/").concat(ServiceName.DMS.getServiceName());
		}
		String destinationPath = documentBackupPath.concat(sourcePath);
		
		docFileBackupDto.setSourcePath(sourcePath);
		docFileBackupDto.setDestinationPath(destinationPath);

		return docFileBackupDto;
	}

}