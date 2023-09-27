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
	
	private String totalCosto;
	private Integer totalArt;
	private String folio;
	private String velatorio;
	private String delegacion;
	private String fecElaboracion;
	private String hrElaboracion;
	private List<ArticulosConsigResponse> artResponse;

}
