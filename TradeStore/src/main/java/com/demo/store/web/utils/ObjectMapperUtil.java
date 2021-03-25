package com.demo.store.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperUtil {

	public static Object map(Object sourceObject, Class<?> dtoClass) {
		 ObjectMapper mapper = new ObjectMapper();
		 Object target = mapper.convertValue(sourceObject, dtoClass);
		return target;
	}
}
