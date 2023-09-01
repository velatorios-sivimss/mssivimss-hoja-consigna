package com.imss.sivimss.hoja.consignacion.model.request;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@JsonIgnoreType(value = true)
public class ReporteDto {
	
	private Integer idVelatorio;
	private Integer idDelegacion;
	private Integer idHojaConsig;
	private Integer idproveedor;
	private String fecInicio;
	private String fecFin;
	private String tipoReporte;

}
