package com.imss.sivimss.hoja.consignacion.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.imss.sivimss.hoja.consignacion.beans.GenerarHojaConsig;
import com.imss.sivimss.hoja.consignacion.exception.BadRequestException;
import com.imss.sivimss.hoja.consignacion.model.request.FiltrosHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.GenerarHojaConsigRequest;
import com.imss.sivimss.hoja.consignacion.model.request.ReporteDto;
import com.imss.sivimss.hoja.consignacion.model.request.UsuarioDto;
import com.imss.sivimss.hoja.consignacion.model.response.ArticulosConsigResponse;
import com.imss.sivimss.hoja.consignacion.model.response.HojaConsigResponse;
import com.imss.sivimss.hoja.consignacion.service.GenerarHojaConsigService;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.ConvertirGenerico;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.LogUtil;
import com.imss.sivimss.hoja.consignacion.util.MensajeResponseUtil;
import com.imss.sivimss.hoja.consignacion.util.ProviderServiceRestTemplate;
import com.imss.sivimss.hoja.consignacion.util.Response;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
	
	@Value("${plantilla.anexo-hoja-consig}")
	private String anexoHojaConsig;
	
	@Value("${generales.reporte-hoja-consig}")
	private String reporteHojaConsig;
	
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
	  	Response<?> response = new Response<>();
	String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
	FiltrosHojaConsigRequest filtros = gson.fromJson(datosJson, FiltrosHojaConsigRequest.class);
	List<HojaConsigResponse> hojaResponse;
	List<ArticulosConsigResponse> artResponse;
	HojaConsigResponse datosResponse;
   	if(filtros.getFecInicio()!=null) {
   		generarHoja.setFecInicio(formatFecha(filtros.getFecInicio())+ " 00:00:00");
   		generarHoja.setFecFin(formatFecha(filtros.getFecFin())+" 23:59:59");
   	}
   	Response<?> responseDatos = MensajeResponseUtil.mensajeConsultaResponse(providerRestTemplate.consumirServicio(generarHoja.buscarArtConsig(request, filtros, fecFormat).getDatos(), urlConsulta,authentication), EXITO); 
       if(responseDatos.getDatos().toString().contains("id")) {
    	   hojaResponse =  Arrays.asList(modelMapper.map(providerRestTemplate.consumirServicio(generarHoja.datosHojaConsig(request, filtros).getDatos(), urlConsulta,authentication).getDatos(), HojaConsigResponse[].class));
    	//   artResponse =  Arrays.asList(modelMapper.map(providerRestTemplate.consumirServicio(generarHoja.buscarArtConsig(request, filtros, fecFormat).getDatos(), urlConsulta,authentication).getDatos(), ArticulosConsigResponse[].class));
    	  artResponse = Arrays.asList(modelMapper.map(responseDatos.getDatos(), ArticulosConsigResponse[].class));
    	  datosResponse = hojaResponse.get(0);
    	  datosResponse.setArtResponse(artResponse);
    	   logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"CONSULTA ARTICULOS CONSIGNADOS OK", CONSULTA);
           response.setMensaje("Exito");
	      response.setDatos(ConvertirGenerico.convertInstanceOfObject(datosResponse));
       }else {
       	response.setMensaje("45");
       	response.setDatos(null);
    	logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"NO HAY INFORMACION RELACIONADA A TU BUSQUEDA", CONSULTA);
       } 
       response.setCodigo(200);
       response.setError(false);
       return response;
   	}
  

	@Override
   	public Response<?> buscarHojaConsig(DatosRequest request, Authentication authentication)
   			throws IOException, ParseException {
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
   		FiltrosHojaConsigRequest filtros = gson.fromJson(datosJson, FiltrosHojaConsigRequest.class);
   	 Integer pagina = Integer.valueOf(Integer.parseInt(request.getDatos().get("pagina").toString()));
     Integer tamanio = Integer.valueOf(Integer.parseInt(request.getDatos().get("tamanio").toString()));
     filtros.setTamanio(tamanio.toString());
     filtros.setPagina(pagina.toString());
   	   	if(filtros.getFecInicio()!=null) {
   	   		generarHoja.setFecInicio(formatFecha(filtros.getFecInicio()));
   	   		generarHoja.setFecFin(formatFecha(filtros.getFecFin()));
   	   	}
   	   	Response<?> response = MensajeResponseUtil.mensajeConsultaResponse(providerRestTemplate.consumirServicio(generarHoja.buscarHojaConsig(request, filtros, fecFormat).getDatos(), urlPaginado,
   				authentication), EXITO); 
   	       if(response.getDatos().toString().contains("id")) {
   	    	   logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"BUSCAR HOJA CONSIG OK", CONSULTA);
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
		generarHoja=new GenerarHojaConsig(hojaRequest);
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
		if(reporte.getIdHojaConsig()==null) {
			throw new BadRequestException(HttpStatus.BAD_REQUEST, INFORMACION_INCOMPLETA);
		}
		Map<String, Object> envioDatos = new GenerarHojaConsig().reporteHojaConsig(reporte.getIdHojaConsig(), anexoHojaConsig);
		Response<?> response = providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
				authentication);
		logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"SE GENERO CORRECTAMENTE LA HOJA DE COSIGNACION", IMPRIMIR);
		return response;
	}
	
	@Override
	public Response<?> generarReporteConsulta(DatosRequest request, Authentication authentication) throws IOException, ParseException {
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
		ReporteDto reporte= gson.fromJson(datosJson, ReporteDto.class);
		if(reporte.getFecInicio()!=null) {
			reporte.setFecInicio(formatFecha(reporte.getFecInicio()));
   	   		reporte.setFecFin(formatFecha(reporte.getFecFin()));
		}
		Map<String, Object> envioDatos = new GenerarHojaConsig().reporteConsultaHojaConsig(reporte, reporteHojaConsig);
		Response<?> response = providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
				authentication);
		logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"SE GENERO CORRECTAMENTE EL REPORTE HOJA DE COSIGNACION", IMPRIMIR);
		return response;
	}
	
	
	@Override
	public Response<?> buscarCatalogo(DatosRequest request, Authentication authentication) throws IOException {
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
		Response<?> response;
		FiltrosHojaConsigRequest filtros = gson.fromJson(datosJson, FiltrosHojaConsigRequest.class);
		if(filtros.getIdCatalogo()==1) {
		      response = MensajeResponseUtil.mensajeConsultaResponse(providerRestTemplate.consumirServicio(generarHoja.catalogoProveedores(request, filtros).getDatos(), urlConsulta,
					authentication), EXITO);
		    	   logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"CONSULTA ARTICULOS CONSIGNADOS OK", CONSULTA);
		}else {
			throw new BadRequestException(HttpStatus.BAD_REQUEST, INFORMACION_INCOMPLETA);
		}
	   	
	    	   return response;
	}
	
	 public String formatFecha(String fecha) throws ParseException {
			Date dateF = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			DateFormat fecForma = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "MX"));
			return fecForma.format(dateF);       
		}
	}


