package com.imss.sivimss.hoja.consignacion.model.request;

import java.util.List;

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
public class GenerarHojaConsigRequest {
	
	private Integer idVelatorio;
	private Integer idProveedor;
	private List<ArticulosConsigRequest> artConsig;

}
