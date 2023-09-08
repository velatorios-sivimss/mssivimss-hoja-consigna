package com.imss.sivimss.hoja.consignacion.beans;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.imss.sivimss.hoja.consignacion.model.request.ArticulosConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.GenerarHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.ReporteDto;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.QueryHelper;
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
	
	private Integer idVelatorio;
	private Integer idProveedor;
	private String fecInicio;
	private String fecFin;
	private Integer idUsuario;
	
	public GenerarHojaConsig(GenerarHojaConsigRequest generarHoja) {
		this.idVelatorio = generarHoja.getIdVelatorio();
		this.idProveedor = generarHoja.getIdProveedor();
	}
	
	//tablas
	private static final String SVT_PAQUETE = "SVT_PAQUETE PAQ";
	private static final String SVC_VELATORIO = "SVC_VELATORIO SV";
	private static final String SVT_PROVEEDOR = "SVT_PROVEEDOR PROV";
	private static final String SVT_HOJA_CONSIGNACION = "SVT_HOJA_CONSIGNACION HOJ";
	
     //ALIAS
	private static final String PROVEEDOR = " AS proveedor";
	
	
	public DatosRequest buscarArtConsig(DatosRequest request, FiltrosHojaConsigRequest filtros,
			String fecFormat) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select( "ART.ID_ARTICULO AS idArticulo",
				"ART.DES_ARTICULO AS art",
				"CAR.ID_PAQUETE AS idPaquete",
				"ART.ID_ARTICULO AS idArticulo",
				"PROV.ID_PROVEEDOR AS idProvedor",
				"SOS.ID_ESTATUS_ORDEN_SERVICIO AS estatusOds",
				"SOS.ID_ORDEN_SERVICIO AS idOds",
				"PROV.NOM_PROVEEDOR " +PROVEEDOR,
				"DATE_FORMAT(SOS.FEC_ALTA, '"+fecFormat+"') AS fecOds",
				"SOS.CVE_FOLIO AS folioOds",
				"PAQ.DES_NOM_PAQUETE AS paquete",
				"SOE.CVE_FOLIO AS folioOde",
				"CAT.DES_CATEGORIA_ARTICULO AS categoria",
				"CON.MON_COSTO_UNITARIO AS costoUnitario",
				"CON.MON_COSTO_UNITARIO+(CON.MON_COSTO_UNITARIO*0.16) AS costoConIva")
		.from("SVC_ORDEN_SERVICIO SOS")
		.join("SVC_CARACTERISTICAS_PAQUETE CAR", "SOS.ID_ORDEN_SERVICIO = CAR.ID_ORDEN_SERVICIO")
		.join(SVT_PAQUETE, "CAR.ID_PAQUETE = PAQ.ID_PAQUETE ")
		.join("SVC_DETALLE_CARAC_PAQ DET", "CAR.ID_CARAC_PAQUETE = DET.ID_CARAC_PAQUETE")
		.join("SVT_ARTICULO ART", "DET.ID_ARTICULO = ART.ID_ARTICULO")
		.join("SVC_CATEGORIA_ARTICULO CAT", "ART.ID_CATEGORIA_ARTICULO = CAT.ID_CATEGORIA_ARTICULO ")
		.join("SVT_CONTRATO_ARTICULOS CON"," ART.ID_ARTICULO = CON.ID_ARTICULO ")
		.join("SVT_INVENTARIO_ARTICULO INV", "ART.ID_ARTICULO = INV.ID_ARTICULO")
		.join("SVT_ORDEN_ENTRADA SOE", "INV.ID_ODE = SOE.ID_ODE")
		.join(SVT_PROVEEDOR, "DET.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join(SVC_VELATORIO, "SOS.ID_VELATORIO = SV.ID_VELATORIO")
		.join("SVT_PAGO_BITACORA PAG", "SOS.ID_ORDEN_SERVICIO = PAG.ID_REGISTRO")
		.leftJoin("SVT_ART_HOJA_CONSIGNACION HOJ", "SOS.ID_ORDEN_SERVICIO = HOJ.ID_ORDEN_SERVICIO");
		queryUtil.where("HOJ.ID_ORDEN_SERVICIO IS NULL").and("INV.ID_TIPO_ASIGNACION_ART = 1").and("(SOS.ID_ESTATUS_ORDEN_SERVICIO = 4 OR SOS.ID_ESTATUS_ORDEN_SERVICIO = 6)")
		.and("PAG.CVE_ESTATUS_PAGO = 5");
		if(filtros.getIdDelegacion()!=null) {
			queryUtil.where("SV.ID_DELEGACION = "+ filtros.getIdDelegacion() + "");
		}
		if(filtros.getIdVelatorio()!=null){
			queryUtil.where("SOS.ID_VELATORIO = " + filtros.getIdVelatorio() + "");	
		}
		if(filtros.getIdProveedor()!=null){
			queryUtil.where("DET.ID_PROVEEDOR = " + filtros.getIdProveedor()+ "");	
		}
		if(filtros.getFecInicio()!=null) {
			queryUtil.where("SOS.FEC_ALTA >= :fecInicio")
			.setParameter("fecInicio", fecInicio);
		}
		if(filtros.getFecFin()!=null) {
			queryUtil.where("SOS.FEC_ALTA <= :fecFin")
			.setParameter("fecFin", fecFin);
		}
		String query = obtieneQuery(queryUtil);
		log.info("buscar articulos "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
		return request;
	}
	
	

	public DatosRequest datosHojaConsig(DatosRequest request, FiltrosHojaConsigRequest filtros) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("ART.ID_ARTICULO AS id",
				 "COUNT(*) AS totalArt",
				"SUM(CON.MON_COSTO_UNITARIO+(CON.MON_COSTO_UNITARIO*0.16)) AS totalCosto")
		.from("SVC_ORDEN_SERVICIO SOS")
		.join("SVC_CARACTERISTICAS_PAQUETE CAR", "SOS.ID_ORDEN_SERVICIO = CAR.ID_ORDEN_SERVICIO")
		.join(SVT_PAQUETE, "CAR.ID_PAQUETE = PAQ.ID_PAQUETE ")
		.join("SVC_DETALLE_CARAC_PAQ DET", "CAR.ID_CARAC_PAQUETE = DET.ID_CARAC_PAQUETE")
		.join("SVT_ARTICULO ART", "DET.ID_ARTICULO = ART.ID_ARTICULO")
		.join("SVC_CATEGORIA_ARTICULO CAT", "ART.ID_CATEGORIA_ARTICULO = CAT.ID_CATEGORIA_ARTICULO ")
		.join("SVT_CONTRATO_ARTICULOS CON"," ART.ID_ARTICULO = CON.ID_ARTICULO ")
		.join("SVT_INVENTARIO_ARTICULO INV", "ART.ID_ARTICULO = INV.ID_ARTICULO")
		.join("SVT_ORDEN_ENTRADA SOE", "INV.ID_ODE = SOE.ID_ODE")
		.join(SVT_PROVEEDOR, "DET.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join(SVC_VELATORIO, "SOS.ID_VELATORIO = SV.ID_VELATORIO")
		.join("SVT_PAGO_BITACORA PAG", "SOS.ID_ORDEN_SERVICIO = PAG.ID_REGISTRO")
		.leftJoin("SVT_ART_HOJA_CONSIGNACION HOJ", "SOS.ID_ORDEN_SERVICIO = HOJ.ID_ORDEN_SERVICIO");
		queryUtil.where("HOJ.ID_ORDEN_SERVICIO IS NULL").and("INV.ID_TIPO_ASIGNACION_ART = 1").and("(SOS.ID_ESTATUS_ORDEN_SERVICIO = 4 OR SOS.ID_ESTATUS_ORDEN_SERVICIO = 6)")
		.and("PAG.CVE_ESTATUS_PAGO = 5");
		if(filtros.getIdDelegacion()!=null) {
			queryUtil.where("SV.ID_DELEGACION = "+ filtros.getIdDelegacion() + "");
		}
		if(filtros.getIdVelatorio()!=null){
			queryUtil.where("SOS.ID_VELATORIO = " + filtros.getIdVelatorio() + "");	
		}
		if(filtros.getIdProveedor()!=null){
			queryUtil.where("DET.ID_PROVEEDOR = " + filtros.getIdProveedor()+ "");	
		}
		if(filtros.getFecInicio()!=null) {
			queryUtil.where("SOS.FEC_ALTA >= :fecInicio")
			.setParameter("fecInicio", fecInicio);
		}
		if(filtros.getFecFin()!=null) {
			queryUtil.where("SOS.FEC_ALTA <= :fecFin")
			.setParameter("fecFin", fecFin);
		}
		String query = obtieneQuery(queryUtil);
		log.info("datos "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
		return request;
	}
	
	public DatosRequest buscarHojaConsig(DatosRequest request, FiltrosHojaConsigRequest filtros,
			String fecFormat) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("HOJ.ID_HOJA_CONSIGNACION AS idHojaConsig",
				"HOJ.DES_FOLIO AS folio",
				"DATE_FORMAT(HOJ.FEC_ELABORACION, '"+fecFormat+"') fecElaboracion",
				"PROV.NOM_PROVEEDOR " +PROVEEDOR)
		.from(SVT_HOJA_CONSIGNACION)
		.join(SVT_PROVEEDOR, "HOJ.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join(SVC_VELATORIO, "HOJ.ID_VELATORIO = SV.ID_VELATORIO");
			queryUtil.where("HOJ.IND_ACTIVO = 1");
			if(filtros.getIdVelatorio()!=null) {
				queryUtil.where("HOJ.ID_VELATORIO ="+filtros.getIdVelatorio());
			}
			if(filtros.getIdDelegacion()!=null) {
				queryUtil.where("SV.ID_DELEGACION ="+filtros.getIdDelegacion());
			}
			if(filtros.getIdProveedor()!=null) {
				queryUtil.where("HOJ.ID_PROVEEDOR ="+filtros.getIdProveedor());
			}
			if(filtros.getFolio()!=null) {
				queryUtil.where("HOJ.DES_FOLIO ="+filtros.getFolio());
			}
			if(filtros.getFecInicio()!=null) {
				queryUtil.where("HOJ.FEC_ELABORACION BETWEEN '" + fecInicio+ "'").and("'"+fecFin+"'");
			}
		String query = obtieneQuery(queryUtil);
		log.info("buscar hoja consig "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
	    parametros.put("pagina",filtros.getPagina());
        parametros.put("tamanio",filtros.getTamanio());
	    request.setDatos(parametros);
		return request;
	}
	
	
	public DatosRequest verDetalleArticulos(DatosRequest request, String fecFormat, String palabra) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("ART.ID_ORDEN_SERVICIO AS idOds",
				"ART.ID_PAQUETE AS idPaquete",
				"PROV.NOM_PROVEEDOR"+PROVEEDOR,
				"ART.REF_CATEGORIA_ART AS categoria",
				"ART.CVE_FOLIO_ODE AS folioOde",
				"PAQ.DES_NOM_PAQUETE AS paquete",
				"ART.IMP_COSTO_UNITARIO_ART AS costoUnitario",
				"ART.IMP_COSTO_UNITARIO_ART+(ART.IMP_COSTO_UNITARIO_ART*0.16) AS costoConIva",
				"DATE_FORMAT(ODS.FEC_ALTA, '"+fecFormat+"') AS fecOds",
				"ODS.CVE_FOLIO AS folioOds")
		.from("SVT_ART_HOJA_CONSIGNACION ART")
		.join(SVT_HOJA_CONSIGNACION, "ART.ID_HOJA_CONSIGNACION = HOJ.ID_HOJA_CONSIGNACION")
		.join("SVT_PROVEEDOR PROV ", "HOJ.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join("SVC_ORDEN_SERVICIO ODS", "ART.ID_ORDEN_SERVICIO = ODS.ID_ORDEN_SERVICIO")
		.join(SVT_PAQUETE, "ART.ID_PAQUETE = PAQ.ID_PAQUETE");
		queryUtil.where("HOJ.IND_ACTIVO=1").and("ART.IND_ACTIVO=1").and
		("ART.ID_HOJA_CONSIGNACION = " +Integer.parseInt(palabra));
		String query = obtieneQuery(queryUtil);
		log.info("formato "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
       request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
	    return request;
	}
	


	public DatosRequest detalleHojaConsig(DatosRequest request, String fecFormat, String palabra) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("COUNT(*) totalArt",
				"SUM(ART.IMP_COSTO_UNITARIO_ART) AS totalCosto",
				"HOJ.DES_FOLIO AS folio",
				"DATE_FORMAT(HOJ.FEC_ELABORACION, '"+fecFormat+"') AS fecElaboracion",
				"HOJ.TIM_HORA_ELABORACION AS hrElaboracion",
				"SV.DES_VELATORIO AS velatorio",
				"SD.DES_DELEGACION AS delegacion")
		.from("SVT_ART_HOJA_CONSIGNACION ART")
		.join(SVT_HOJA_CONSIGNACION, "ART.ID_HOJA_CONSIGNACION = HOJ.ID_HOJA_CONSIGNACION")
		.join(SVC_VELATORIO, "HOJ.ID_VELATORIO = SV.ID_VELATORIO")
		.join("SVC_DELEGACION SD", "SV.ID_DELEGACION = SD.ID_DELEGACION");
		queryUtil.where("HOJ.IND_ACTIVO=1").and("ART.IND_ACTIVO=1").and
		("ART.ID_HOJA_CONSIGNACION = " +Integer.parseInt(palabra));
		String query = obtieneQuery(queryUtil);
		log.info("formato "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
       request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
	    return request;
	}
	
	public DatosRequest generarHojaConsig() {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_HOJA_CONSIGNACION");
		q.agregarParametroValues("DES_FOLIO", "(SELECT CONCAT(LPAD(COUNT(HOJ.ID_HOJA_CONSIGNACION)+1, 4,'0'),'-',(SELECT LPAD(SV.ID_VELATORIO, 2, '0') "
				+ "FROM SVC_VELATORIO SV WHERE ID_VELATORIO = "+this.idVelatorio+")) "
				+ "FROM SVT_HOJA_CONSIGNACION HOJ WHERE HOJ.IND_ACTIVO=1)");
		q.agregarParametroValues("ID_VELATORIO", ""+this.idVelatorio+"");
		q.agregarParametroValues("ID_PROVEEDOR", ""+this.getIdProveedor()+"");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "0");
	    q.agregarParametroValues("ID_USUARIO_ALTA", "" +idUsuario+ "");
		q.agregarParametroValues("FEC_ALTA", "" +AppConstantes.CURRENT_TIMESTAMP +"");
		String query = q.obtenerQueryInsertar();// + "$$" + insertarArticulos(hojaRequest.getArtConsig());
		/*StringBuilder queries= new StringBuilder();
		queries.append(query);
				//for(int i=0; i<hojaRequest.getArtConsig().size(); i++) {
					for(ArticulosConsigRequest articulos : hojaRequest.getArtConsig()) {
				  //      ArticulosConsigRequest articulos = hojaRequest.getArtConsig().get(i);
						queries.append("$$" + insertarArticulosConsig(articulos));
			}
			log.info("hoja consig " +query);
		
				  parametro.put("separador","$$");
			      parametro.put("replace","idTabla");*/
			      	String encoded = encodedQuery(query);
				  parametro.put(AppConstantes.QUERY, encoded);
		        request.setDatos(parametro);
		return request;
	}
	
	public DatosRequest insertarArticulos(GenerarHojaConsigRequest hojaRequest, Integer idHojaConsig) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("UPDATE SVT_HOJA_CONSIGNACION");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "1");
		q.addWhere("ID_HOJA_CONSIGNACION = "+idHojaConsig);
		String query = q.obtenerQueryActualizar();
		StringBuilder queries= new StringBuilder();
		queries.append(query);
				//for(int i=0; i<hojaRequest.getArtConsig().size(); i++) {
					for(ArticulosConsigRequest articulos : hojaRequest.getArtConsig()) {
				  //      ArticulosConsigRequest articulos = hojaRequest.getArtConsig().get(i);
						queries.append("$$" + insertarArticulosConsig(articulos, idHojaConsig));
			}
			log.info("articulos " +queries);
			String encoded = encodedQuery(queries.toString());
			  parametro.put(AppConstantes.QUERY, encoded);
				  parametro.put("separador","$$");
		        request.setDatos(parametro);
		return request;
	}
	
	
	
	private String insertarArticulosConsig(ArticulosConsigRequest articulos, Integer idHojaConsig) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_ART_HOJA_CONSIGNACION");
		q.agregarParametroValues("ID_HOJA_CONSIGNACION", ""+idHojaConsig+"");
		q.agregarParametroValues("ID_ARTICULO", ""+articulos.getIdArticulo()+"");
		q.agregarParametroValues("ID_ORDEN_SERVICIO", ""+articulos.getIdOds()+"");
		q.agregarParametroValues("ID_PAQUETE", ""+articulos.getIdPaquete()+"");
		q.agregarParametroValues("REF_CATEGORIA_ART", "'"+articulos.getCategoria()+"'");
		q.agregarParametroValues("CVE_FOLIO_ODE", "'"+articulos.getFolioOde()+"'");
		q.agregarParametroValues("IMP_COSTO_UNITARIO_ART", ""+articulos.getCostoConIva()+"");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "1");
		q.agregarParametroValues("FEC_ALTA", "" +AppConstantes.CURRENT_TIMESTAMP +"");
		String query = q.obtenerQueryInsertar();
		log.info("insertar articulo -> "+query);
		  String encoded = encodedQuery(query);
		  parametro.put(AppConstantes.QUERY, encoded);
        request.setDatos(parametro);
        return query;
	}
	
	public DatosRequest catalogoProveedores(DatosRequest request, FiltrosHojaConsigRequest filtros) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("PROV.ID_PROVEEDOR AS idProveedor",
				"PROV.NOM_PROVEEDOR AS proveedor")
		.from(SVT_PROVEEDOR)
		.join("SVT_CONTRATO SC", "PROV.ID_PROVEEDOR = SC.ID_PROVEEDOR")
		.join(SVC_VELATORIO, "SC.ID_VELATORIO = SV.ID_VELATORIO");
			queryUtil.where("PROV.ID_TIPO_PROVEEDOR = 2").and("(SC.IND_ACTIVO = 1 OR SC.FEC_FIN_VIG <= CURDATE())");
			if(filtros.getIdVelatorio()!=null) {
				queryUtil.where("SC.ID_VELATORIO ="+filtros.getIdVelatorio());
			}
			if(filtros.getIdDelegacion()!=null) {
				queryUtil.where("SV.ID_DELEGACION ="+filtros.getIdDelegacion());
			}
		String query = obtieneQuery(queryUtil);
		log.info("catalogo prov "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
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

	public Map<String, Object> reporteHojaConsig(Integer idHojaConsig, String anexo24) {
		Map<String, Object> envioDatos = new HashMap<>();
		envioDatos.put("idHojaConsig", idHojaConsig);
		envioDatos.put("version", "1.0.0");
		envioDatos.put("rutaNombreReporte", anexo24);
		envioDatos.put("tipoReporte", "pdf");
		return envioDatos;
	}
	
	public Map<String, Object> reporteConsultaHojaConsig(ReporteDto reporte, String reporteHojaConsig) {
		Map<String, Object> envioDatos = new HashMap<>();
		StringBuilder condition= new StringBuilder();
		if(reporte.getIdDelegacion()!=null) {
			condition.append(" AND SV.ID_DELEGACION = "+reporte.getIdDelegacion()+"");
		}
		if(reporte.getIdVelatorio()!=null) {
			condition.append(" AND HOJ.ID_VELATORIO = "+reporte.getIdVelatorio()+"");
		}
		if(reporte.getIdProveedor()!=null) {
			condition.append(" AND HOJ.ID_PROVEEDOR = "+reporte.getIdProveedor()+"");
		}
		if(reporte.getFolio()!=null) {
			condition.append(" AND HOJ.DES_FOLIO = '"+reporte.getFolio()+"'");
		}
		if(reporte.getFecInicio()!=null) {
			condition.append(" AND HOJ.FEC_ELABORACION BETWEEN '"+reporte.getFecInicio()+"' AND '"+reporte.getFecFin()+"'");
		}
		log.info("reporte -> "+condition.toString());
		envioDatos.put("condition", condition.toString());
		envioDatos.put("rutaNombreReporte", reporteHojaConsig);
		envioDatos.put("tipoReporte", reporte.getTipoReporte());
		if(reporte.getTipoReporte().equals("xls")) { 
			envioDatos.put("IS_IGNORE_PAGINATION", true); 
			}
		return envioDatos;
	}

	
	private static String encodedQuery(String query) {
        return DatatypeConverter.printBase64Binary(query.getBytes(StandardCharsets.UTF_8));
    }
	
	private static String obtieneQuery(SelectQueryUtil queryUtil) {
        return queryUtil.build();
	}

}
