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
public class FiltrosHojaConsignacionRequest {

	private Integer idVelatorio;
	private Integer idDelegacion;
	private String proveedor;
	private String fecInicio;
	private String fecFin;
	private Integer idCatalogo;

}
