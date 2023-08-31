package com.imss.sivimss.hoja.consignacion.beans;


import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import com.imss.sivimss.hoja.consignacion.model.request.ArticulosConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.GenerarHojaConsigRequest;
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
	
	private String fecInicio;
	private String fecFin;
	private Integer idUsuario;
	
	
	
	public DatosRequest buscarArtConsig(DatosRequest request, FiltrosHojaConsigRequest filtros,
			String fecFormat) {
		String hrInicio= fecInicio +" 00:00:01";
		String hrFin=fecFin+" 23:59:59";
		log.info("-->"+hrInicio);
		log.info(hrFin);
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
			queryUtil.where("SOS.FEC_ALTA BETWEEN '" + fecInicio+ "'").and("'"+fecFin+"'");
		}
		String query = obtieneQuery(queryUtil);
		log.info("hoja consignacion "+query);
		String encoded = encodedQuery(query);
	    parametros.put(AppConstantes.QUERY, encoded);
        request.getDatos().remove(AppConstantes.DATOS);
	    request.setDatos(parametros);
		return request;
	}
	
	public DatosRequest generarHojaConsig(GenerarHojaConsigRequest hojaRequest) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_HOJA_CONSIGNACION");
		//q.agregarParametroValues("DES_FOLIO", "(SELECT CONCAT(LPAD(COUNT(FORM.ID_FORMATO_ACTIVIDAD)+1, 5,'0'),'-',SV.ID_VELATORIO) FROM SVT_FORMATO_ACTIVIDAD_PROMOTORES FORM JOIN SVC_VELATORIO SV ON FORM.ID_VELATORIO = SV.ID_VELATORIO WHERE FORM.ID_VELATORIO = "+this.idVelatorio+" AND FORM.IND_ACTIVO=1)");
		q.agregarParametroValues("DES_FOLIO", "'0001-01'");
		q.agregarParametroValues("ID_VELATORIO", ""+hojaRequest.getIdVelatorio()+"");
		q.agregarParametroValues("ID_PROVEEDOR", ""+hojaRequest.getIdProveedor()+"");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "1");
	    q.agregarParametroValues("ID_USUARIO_ALTA", "" +idUsuario+ "");
		q.agregarParametroValues("FEC_ALTA", "" +AppConstantes.CURRENT_TIMESTAMP +"");
		String query = q.obtenerQueryInsertar();// + "$$" + insertarArticulos(hojaRequest.getArtConsig());
		StringBuilder queries= new StringBuilder();
		queries.append(query);
				//for(int i=0; i<hojaRequest.getArtConsig().size(); i++) {
					for(ArticulosConsigRequest articulos : hojaRequest.getArtConsig()) {
				  //      ArticulosConsigRequest articulos = hojaRequest.getArtConsig().get(i);
						queries.append("$$" + insertarActividades(articulos));
			}
			log.info("estoy hojaConsignacion " +query);
			String encoded = encodedQuery(queries.toString());
				  parametro.put(AppConstantes.QUERY, encoded);
				  parametro.put("separador","$$");
			      parametro.put("replace","idTabla");
		        request.setDatos(parametro);
		return request;
	}
	
	
	
	private String insertarActividades(ArticulosConsigRequest articulos) {
		DatosRequest request = new DatosRequest();
		Map<String, Object> parametro = new HashMap<>();
		final QueryHelper q = new QueryHelper("INSERT INTO SVT_ARTICULOS_HOJA_CONSIGNACION");
		q.agregarParametroValues("ID_HOJA_CONSIGNACION", "idTabla");
		q.agregarParametroValues("NOM_PROVEEDOR", "'"+articulos.getProveedor()+"'");
		q.agregarParametroValues("ID_ORDEN_SERVICIO", ""+articulos.getIdOds()+"");
		q.agregarParametroValues("DES_CATEGORIA_ARTICULO", "'"+articulos.getCategoria()+"'");
		q.agregarParametroValues("CVE_FOLIO_ODE", "'"+articulos.getFolioOde()+"'");
		q.agregarParametroValues("DES_NOM_PAQUETE", "'"+articulos.getPaquete()+"'");
		q.agregarParametroValues("MON_COSTO_UNITARIO_ARTICULO", ""+articulos.getCosto()+"");
		q.agregarParametroValues("" +AppConstantes.IND_ACTIVO+ "", "1");
		q.agregarParametroValues("FEC_ALTA", "" +AppConstantes.CURRENT_TIMESTAMP +"");
		String query = q.obtenerQueryInsertar();
		log.info("insertar articulo -> "+query);
		  String encoded = encodedQuery(query);
		  parametro.put(AppConstantes.QUERY, encoded);
        request.setDatos(parametro);
        return query;
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
