package com.imss.sivimss.hoja.consignacion.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ArticulosConsigResponse {
	
	private Integer idArticulo;
	private Integer idOds;
	private Integer idPaquete;
	private String folioOds;
	private String fecOds;
	private String folioOde;
	private Double costoUnitario;
	private Double costoConIva;
	private String categoria;
	private String proveedor;
	private String paquete;

}
