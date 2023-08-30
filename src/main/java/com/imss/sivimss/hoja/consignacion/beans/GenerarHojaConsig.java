package com.imss.sivimss.hoja.consignacion.beans;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsignacionRequest;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.SelectQueryUtil;

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
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select( 
				"ART.DES_ARTICULO AS art",
				"ART.ID_ARTICULO AS idArticulo",
				"PROV.ID_PROVEEDOR AS idProvedor",
				"SOS.ID_ESTATUS_ORDEN_SERVICIO AS estatusOds",
				"SOS.ID_ORDEN_SERVICIO AS idOds",
				"PROV.NOM_PROVEEDOR AS proveedor",
				"DATE_FORMAT(SOS.FEC_ALTA, '"+fecFormat+"') AS fecOds",
				"SOS.CVE_FOLIO AS folioOds",
				"PAQ.DES_NOM_PAQUETE AS paquete",
				"SOE.CVE_FOLIO AS folioOde",
				"CAT.DES_CATEGORIA_ARTICULO AS categoria",
				"CON.MON_COSTO_UNITARIO AS costo")
		.from("SVT_ARTICULO ART")
		.join("SVC_CATEGORIA_ARTICULO CAT ", "ART.ID_CATEGORIA_ARTICULO = CAT.ID_CATEGORIA_ARTICULO ")
		.join("SVT_CONTRATO_ARTICULOS CON", "ART.ID_ARTICULO = CON.ID_ARTICULO")
		.join("SVT_INVENTARIO_ARTICULO INV", "ART.ID_ARTICULO = INV.ID_ARTICULO")
		.join("SVT_ORDEN_ENTRADA SOE", "INV.ID_ODE = SOE.ID_ODE")
		.join("SVC_DETALLE_CARAC_PAQ DET", "ART.ID_ARTICULO = DET.ID_ARTICULO")
		.join("SVT_PROVEEDOR PROV", "DET.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join("SVC_CARACTERISTICAS_PAQUETE CAR", "DET.ID_CARAC_PAQUETE = CAR.ID_CARAC_PAQUETE")
		.join("SVT_PAQUETE PAQ ", "CAR.ID_PAQUETE = PAQ.ID_PAQUETE")
		.join("SVC_ORDEN_SERVICIO SOS", "CAR.ID_ORDEN_SERVICIO = SOS.ID_ORDEN_SERVICIO")
		.join("SVC_VELATORIO SV", "SOS.ID_VELATORIO = SV.ID_VELATORIO")
		.join("SVT_PAGO_BITACORA PAG", "SOS.ID_ORDEN_SERVICIO = PAG.ID_REGISTRO");
		queryUtil.where("INV.ID_TIPO_ASIGNACION_ART = 1").and("(SOS.ID_ESTATUS_ORDEN_SERVICIO = 4 OR SOS.ID_ESTATUS_ORDEN_SERVICIO = 6)")
		.and("AND PAG.CVE_ESTATUS_PAGO = 5");
		if(filtros.getIdDelegacion()!=null) {
			queryUtil.where("SV.ID_DELEGACION = "+ filtros.getIdDelegacion() + "");
		}
		if(filtros.getIdVelatorio()!=null){
			queryUtil.where("FORM.ID_VELATORIO = " + filtros.getIdVelatorio() + "");	
		}
		if(filtros.getProveedor()!=null){
			queryUtil.where("PROV.ID_PROVEEDOR = " + filtros.getProveedor()+ "");	
		}
		if(filtros.getFecInicio()!=null) {
			queryUtil.where("SOS.FEC_ALTA BETWEEN '" + fecInicio+"' 00:00:01" ).and("'"+fecFin+"' 23:59:59");	
		}
		queryUtil.groupBy("FORM.ID_FORMATO_ACTIVIDAD ORDER BY FORM.FEC_ELABORACION ASC");
		String query = obtieneQuery(queryUtil);
		log.info("actividades promotores "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
		return request;
	}
	
	private String setValor(String valor) {
        if (valor==null || valor.equals("")) {
            return "NULL";
        }else {
            return "'"+valor+"'";
        }
    }
	
	private static String encodedQuery(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes(StandardCharsets.UTF_8));
    }
	
	private static String obtieneQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
	}


}
