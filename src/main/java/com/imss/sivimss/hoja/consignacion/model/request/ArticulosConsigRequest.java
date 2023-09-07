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
public class ArticulosConsigRequest {

	private Integer idHojaConsig;
	private String proveedor;
	private Integer idOds;
	private String categoria;
	private String folioOde;
	private Integer idPaquete;
	private Double costoConIva;
}
