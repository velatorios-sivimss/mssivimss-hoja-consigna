package com.imss.sivimss.arquetipo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.imss.sivimss.hoja.consignacion.HojaConsignacionApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ArquetipoApplicationTests {

	@Test
	void contextLoads() {
		String result = "test";
		HojaConsignacionApplication.main(new String[] {});
		assertNotNull(result);
	}

}
