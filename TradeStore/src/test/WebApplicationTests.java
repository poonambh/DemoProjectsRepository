package com.psl.adms.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WebApplicationTests {


	@Test
	public void contextLoads() {
	}

	@Test
	public void createTradeRecordTest() {
		// given
		TardeItem item = new TardeItem();
		entityManager.persist(alex);
		entityManager.flush();

		// when
		Employee found = employeeRepository.findByName(alex.getName());

		// then
		assertThat(found.getName())
				.isEqualTo(alex.getName());
	}

}
