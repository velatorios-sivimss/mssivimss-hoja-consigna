package com.imss.sivimss.hoja.consignacion.beans;


import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsignacionRequest;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class GenerarHojaConsig {
	
	private String fecInicio;
	private String fecFin;
	
	
	
	public DatosRequest buscarArtConsig(DatosRequest request, FiltrosHojaConsignacionRequest filtros,
			String fecFormat) {
		// TODO Auto-generated method stub
		return null;
	}
	

}
