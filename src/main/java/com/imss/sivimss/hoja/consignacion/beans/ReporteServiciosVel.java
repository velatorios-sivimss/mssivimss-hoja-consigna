package com.imss.sivimss.hoja.consignacion.beans;

import java.util.HashMap;
import java.util.Map;

import com.imss.sivimss.hoja.consignacion.model.request.ReporteServiciosVelDto;

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

}
