package com.psl.adms.web.controller.scanlog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.psl.adms.commons.exceptions.FileOperationException;
import com.psl.adms.commons.utils.DateMapperUtil;
import com.psl.adms.core.service.scanlog.ScanLogService;
import com.psl.adms.web.controller.BaseController;

@RestController
@RequestMapping("/scanlog")
public class ScanLogController  extends BaseController{

	 private static final Logger logger = LoggerFactory.getLogger(ScanLogController.class);
	 private static final String APPLICATION_RESPONSE_CONTENTTYPE = "application/csv";

	@Autowired
	private ScanLogService scanLogService;

	
	@RequestMapping(value = "downloadFile", method = RequestMethod.GET)
	public StreamingResponseBody getScanLogFile(HttpServletResponse response, @RequestParam Map<String, String> queryMap,
			@RequestHeader("Authorization") String token,
            HttpServletRequest httpServletRequest,
            HttpMethod httpMethod) throws Exception {
		try {
			HttpStatus httpStatus = checkAccess(httpServletRequest, httpMethod, token);

			String groupId = StringUtils.isEmpty(queryMap.get("groupId")) ? null : queryMap.get("groupId");
			String assignmentDate = StringUtils.isEmpty(queryMap.get("assignmentDate"))
					? null : queryMap.get("assignmentDate");
			
			String formattedAssignmentDate = convertDate(assignmentDate);

			File file = scanLogService.createScanLogCSV(groupId, formattedAssignmentDate);
			response.setContentType(APPLICATION_RESPONSE_CONTENTTYPE);
			response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
			InputStream inputStream = new FileInputStream(file);
			return outputStream -> {
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
					outputStream.write(data, 0, nRead);
				}
			};
		}
		catch(Throwable e) {
			throw new FileOperationException(e);
		}

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

}
