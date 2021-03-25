package com.psl.adms.web.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.psl.adms.commons.enums.SchedulerTasks;
import com.psl.adms.core.service.batchcopy.BatchCopyFileService;

import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;


@Component
@PropertySource(value ="application-${spring.profiles.active}.properties", ignoreResourceNotFound = true)
public class UiAccelerationScheduledTasks {

	
	private static final Logger logger = LoggerFactory.getLogger(UiAccelerationScheduledTasks.class);
	
	@Autowired
	private BatchCopyFileService batchCopyFileService;
	
	@Scheduled(cron = "${document.ui.acceleration.file.copy.cron}")
	@SchedulerLock(name = SchedulerTasks.DOCUMENT_UI_ACCELERATION_FILECOPY_SCHEDULER) 
	public void scheduleBatchCopyFromNfsToApache() {
		
		logger.info("DOCUMENT_UI_ACCELERATION_FILECOPY_SCHEDULER start ");
		LockAssert.assertLocked();
		batchCopyFileService.copyFileFromNfsToApache();
		
	}
	
	
}
