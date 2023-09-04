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
	
	
	
	public DatosRequest buscarArtConsig(DatosRequest request, FiltrosHojaConsigRequest filtros,
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
			queryUtil.where("PROV.ID_PROVEEDOR = " + filtros.getIdProveedor()+ "");	
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
	
	
	public DatosRequest buscarHojaConsig(DatosRequest request, FiltrosHojaConsigRequest filtros,
			String fecFormat) {
		Map<String, Object> parametros = new HashMap<>();
		SelectQueryUtil queryUtil = new SelectQueryUtil();
		queryUtil.select("HOJ.ID_HOJA_CONSIGNACION AS idHojaConsig",
				"HOJ.DES_FOLIO AS folio",
				"DATE_FORMAT(HOJ.FEC_ELABORACION, '"+fecFormat+"') fecElaboracion",
				"PROV.NOM_PROVEEDOR AS proveedor")
		.from("SVT_HOJA_CONSIGNACION HOJ")
		.join("SVT_PROVEEDOR PROV", "HOJ.ID_PROVEEDOR = PROV.ID_PROVEEDOR")
		.join("SVC_VELATORIO SV", "HOJ.ID_VELATORIO = SV.ID_VELATORIO");
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
	
	public DatosRequest generarHojaConsig(GenerarHojaConsigRequest hojaRequest) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_HOJA_CONSIGNACION");
		q.agregarParametroValues("DES_FOLIO", "(SELECT CONCAT(LPAD(COUNT(HOJ.ID_HOJA_CONSIGNACION)+1, 4,'0'),'-',(SELECT LPAD(SV.ID_VELATORIO, 2, '0') "
				+ "FROM SVC_VELATORIO SV WHERE ID_VELATORIO = "+this.idVelatorio+")) "
				+ "FROM SVT_HOJA_CONSIGNACION HOJ WHERE HOJ.IND_ACTIVO=1)");
		q.agregarParametroValues("ID_VELATORIO", ""+this.idVelatorio+"");
		q.agregarParametroValues("ID_PROVEEDOR", ""+this.getIdProveedor()+"");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "1");
	    q.agregarParametroValues("ID_USUARIO_ALTA", "" +idUsuario+ "");
		q.agregarParametroValues("FEC_ALTA", "" +AppConstantes.CURRENT_TIMESTAMP +"");
		String query = q.obtenerQueryInsertar();// + "$$" + insertarArticulos(hojaRequest.getArtConsig());
		StringBuilder queries= new StringBuilder();
		queries.append(query);
				//for(int i=0; i<hojaRequest.getArtConsig().size(); i++) {
					for(ArticulosConsigRequest articulos : hojaRequest.getArtConsig()) {
				  //      ArticulosConsigRequest articulos = hojaRequest.getArtConsig().get(i);
						queries.append("$$" + insertarArticulosConsig(articulos));
			}
			log.info("hoja consig " +query);
			String encoded = encodedQuery(queries.toString());
				  parametro.put(AppConstantes.QUERY, encoded);
				  parametro.put("separador","$$");
			      parametro.put("replace","idTabla");
		        request.setDatos(parametro);
		return request;
	}
	
	
	
	private String insertarArticulosConsig(ArticulosConsigRequest articulos) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_ART_HOJA_CONSIGNACION");
		q.agregarParametroValues("ID_HOJA_CONSIGNACION", "idTabla");
		q.agregarParametroValues("NOM_PROVEEDOR", "'"+articulos.getProveedor()+"'");
		q.agregarParametroValues("ID_ORDEN_SERVICIO", ""+articulos.getIdOds()+"");
		q.agregarParametroValues("REF_CATEGORIA_ART", "'"+articulos.getCategoria()+"'");
		q.agregarParametroValues("CVE_FOLIO_ODE", "'"+articulos.getFolioOde()+"'");
		q.agregarParametroValues("REF_NOM_PAQUETE", "'"+articulos.getPaquete()+"'");
		q.agregarParametroValues("IMP_COSTO_UNITARIO_ART", ""+articulos.getCosto()+"");
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
		queryUtil.select("PROV.ID_PROVEEDOR",
				"PROV.NOM_PROVEEDOR")
		.from("SVT_PROVEEDOR PROV")
		.join("SVT_CONTRATO SC", "PROV.ID_PROVEEDOR = SC.ID_PROVEEDOR")
		.join("SVC_VELATORIO SV", "SC.ID_VELATORIO = SV.ID_VELATORIO");
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

	public Map<String, Object> reporteHojaConsig(Integer idHojaConsig) {
		Map<String, Object> envioDatos = new HashMap<>();
		envioDatos.put("idHojaConsig", idHojaConsig);
		envioDatos.put("version", "1.0.0");
		envioDatos.put("rutaNombreReporte", "reportes/plantilla/Anexo_24_Hoja_De_Consignacion.jrxml");
		envioDatos.put("tipoReporte", "pdf");
		return envioDatos;
	}
	
	public Map<String, Object> reporteConsultaHojaConsig(ReporteDto reporte) {
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
		envioDatos.put("rutaNombreReporte", "reportes/generales/ReporteHojaConsig.jrxml");
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
