package com.imss.sivimss.hoja.consignacion.model.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class HojaConsigResponse {
	
	private Double totalCosto;
	private Integer totalArt;
	private List<ArticulosConsigResponse> artResponse;

}