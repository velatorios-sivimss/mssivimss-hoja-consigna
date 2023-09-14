package com.imss.sivimss.hoja.consignacion.beans;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.imss.sivimss.hoja.consignacion.exception.BadRequestException;
import com.imss.sivimss.hoja.consignacion.model.request.ReporteServiciosVelDto;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.SelectQueryUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReporteServiciosVel {

	public Map<String, Object> generarReporte(ReporteServiciosVelDto reporte, String reporteServVel) {
		Map<String, Object> envioDatos = new HashMap<>();
		StringBuilder condition= new StringBuilder();
		if(reporte.getId_delegacion()!=null) {
			condition.append(" AND VEL.ID_DELEGACION = "+reporte.getId_delegacion()+"");
		}
		if(reporte.getId_velatorio()!=null) {
			condition.append(" AND ODS.ID_VELATORIO = "+reporte.getId_velatorio()+"");
		}
		if(reporte.getId_ods()!=null) {
			condition.append(" AND ODS.ID_ORDEN_SERVICIO = '"+reporte.getId_ods()+"'");
		}
		if(reporte.getFecha_inicial()!=null) {
			condition.append(" AND ODS.FEC_ALTA BETWEEN '"+reporte.getFecInicioConsulta()+ " 00:00:01' AND '"+reporte.getFecFinConsulta()+" 23:59:59'");
			envioDatos.put("fecInicio", reporte.getFecha_inicial());
			envioDatos.put("fecFin", reporte.getFecha_final());
		}
		condition.append(" ORDER BY ODS.FEC_ALTA ASC");
		log.info("reporte -> "+condition.toString());
		envioDatos.put("condition", condition.toString());
		envioDatos.put("rutaNombreReporte", reporteServVel);
		if(reporte.getId_tipo_reporte()==1) {
			envioDatos.put("tipoReporte", "pdf");
		}
		else if(reporte.getId_tipo_reporte()==2) { 
			envioDatos.put("tipoReporte", "xls");
			envioDatos.put("IS_IGNORE_PAGINATION", true); 
			}
		else {
			envioDatos.put("tipoReporte", "csv");
		}
		return envioDatos;
	}
	

	public DatosRequest catalogoFolios(DatosRequest request, ReporteServiciosVelDto filtros) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("ODS.ID_ORDEN_SERVICIO id_ods",
				"ODS.CVE_FOLIO AS folio_ods")
		.from("SVC_ORDEN_SERVICIO ODS")
		.join("SVT_PAGO_BITACORA PAG", "ODS.ID_ORDEN_SERVICIO = PAG.ID_REGISTRO")
		.join("SVC_VELATORIO VEL", "ODS.ID_VELATORIO = VEL.ID_VELATORIO")
		.join("SVC_FACTURA FAC", "PAG.ID_PAGO_BITACORA = FAC.ID_PAGO");
			queryUtil.where("ODS.ID_ESTATUS_ORDEN_SERVICIO = 4").and("PAG.CVE_ESTATUS_PAGO = 5");
			if(filtros.getId_velatorio()!=null) {
				queryUtil.where("VEL.ID_VELATORIO  ="+filtros.getId_velatorio());
			}
			if(filtros.getId_delegacion()!=null) {
				queryUtil.where("SV.ID_DELEGACION ="+filtros.getId_delegacion());
			}
			queryUtil.orderBy("ODS.FEC_ALTA ASC");
		String query = obtieneQuery(queryUtil);
		log.info("catalogo folios "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
	    request.setDatos(parametros);
		return request;
	}

	private static String encodedQuery(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes(StandardCharsets.UTF_8));
    }
	
	private static String obtieneQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
	}
	
}