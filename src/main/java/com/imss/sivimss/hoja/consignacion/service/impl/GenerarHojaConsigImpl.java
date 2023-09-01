package com.imss.sivimss.hoja.consignacion.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.imss.sivimss.hoja.consignacion.beans.GenerarHojaConsig;
import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.GenerarHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.ReporteDto;
import com.imss.sivimss.hoja.consignacion.model.request.UsuarioDto;
import com.imss.sivimss.hoja.consignacion.service.GenerarHojaConsigService;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.LogUtil;
import com.imss.sivimss.hoja.consignacion.util.MensajeResponseUtil;
import com.imss.sivimss.hoja.consignacion.util.ProviderServiceRestTemplate;
import com.imss.sivimss.hoja.consignacion.util.Response;

import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;


@Slf4j
@Service
public class GenerarHojaConsigImpl implements GenerarHojaConsigService{
	
	@Autowired
	private LogUtil logUtil;

	@Value("${endpoints.rutas.dominio-consulta}")
	private String urlConsulta;
	@Value("${endpoints.rutas.dominio-consulta-paginado}")
	private String urlPaginado;
	@Value("${endpoints.rutas.dominio-crear}")
	private String urlCrear;
	@Value("${endpoints.rutas.dominio-crear-multiple}")
	private String urlCrearMultiple;
	@Value("${endpoints.rutas.dominio-insertar-multiple}")
	private String urlInsertarMultiple;
	@Value("${endpoints.rutas.dominio-actualizar}")
	private String urlActualizar;
	@Value("${endpoints.ms-reportes}")
	private String urlReportes;
	@Value("${formato-fecha}")
	private String fecFormat;
	
	private static final String BAJA = "baja";
	private static final String ALTA = "alta";
	private static final String MODIFICACION = "modificacion";
	private static final String CONSULTA = "consulta";
	private static final String INFORMACION_INCOMPLETA = "Informacion incompleta";
	private static final String EXITO = "EXITO";
	private static final String IMPRIMIR = "IMPRIMIR";

	@Autowired
	private ProviderServiceRestTemplate providerRestTemplate;
	

	GenerarHojaConsig generarHoja= new GenerarHojaConsig();
	
	Gson gson = new Gson();
	
	@Autowired
	private ModelMapper modelMapper;

	@Override
	public Response<?> buscarArtConsig(DatosRequest request, Authentication authentication) throws IOException, ParseException {
	String datosJson = String.valueOf(request.getDatos().get("datos"));
	FiltrosHojaConsigRequest filtros = gson.fromJson(datosJson, FiltrosHojaConsigRequest.class);
   	if(filtros.getFecInicio()!=null) {
   		generarHoja.setFecInicio(formatFecha(filtros.getFecInicio()));
   		generarHoja.setFecFin(formatFecha(filtros.getFecFin()));
   	}
   	Response<?> response = MensajeResponseUtil.mensajeConsultaResponse(providerRestTemplate.consumirServicio(generarHoja.buscarArtConsig(request, filtros, fecFormat).getDatos(), urlConsulta,
			authentication), EXITO); 
       if(response.getDatos().toString().contains("id")) {
    	   logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"CONSULTA ARTICULOS CONSIGNADOS OK", CONSULTA);
    	   return response;
      
       }else {
       	response.setError(true);
       	response.setMensaje("45");
       	response.setDatos(null);
    	logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"NO HAY INFORMACION RELACIONADA A TU BUSQUEDA", CONSULTA);
       	return response;
       
       } 
  
}

	@Override
	public Response<?> generarHojaConsig(DatosRequest request, Authentication authentication) 
			throws IOException, ParseException {
		Response<?> response = new Response<>();
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
		GenerarHojaConsigRequest hojaRequest =  gson.fromJson(datosJson, GenerarHojaConsigRequest.class);	
		UsuarioDto usuario = gson.fromJson((String) authentication.getPrincipal(), UsuarioDto.class);
		//registrarActividad=new RegistrarActividad(actividadesRequest);
		generarHoja.setIdUsuario(usuario.getIdUsuario());
			try {
				response = providerRestTemplate.consumirServicio(generarHoja.generarHojaConsig(hojaRequest).getDatos(), urlCrearMultiple, authentication);
				logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"HOJA DE CONSIGNACION GENERADA CORRECTAMENTE", ALTA);	
				return response;
			}catch (Exception e) {
				String consulta = generarHoja.generarHojaConsig(hojaRequest).getDatos().get("query").toString();
				String encoded = new String(DatatypeConverter.parseBase64Binary(consulta));
				log.error("Error al ejecutar la query" +encoded);
				logUtil.crearArchivoLog(Level.SEVERE.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"ERROR AL EJECUTAR LA QUERY", ALTA);
				throw new IOException("5", e.getCause()) ;
			}
	}

	
	@Override
	public Response<?> generarReporteHojaConsig(DatosRequest request, Authentication authentication)
			throws IOException {
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
		ReporteDto reporte= gson.fromJson(datosJson, ReporteDto.class);
		Map<String, Object> envioDatos = new GenerarHojaConsig().reporteHojaAfiliacion(reporte.getIdHojaConsig());
		Response<?> response = providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
				authentication);
		logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"SE GENERO CORRECRAMENTE EL REPORTE HOJA DE COSIGNACION", IMPRIMIR);
		return response;
	}
	
	
    public String formatFecha(String fecha) throws ParseException {
		Date dateF = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
		DateFormat fecForma = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "MX"));
		return fecForma.format(dateF);       
	}

	}


