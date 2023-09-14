package com.imss.sivimss.hoja.consignacion.service.impl;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.imss.sivimss.hoja.consignacion.beans.ReporteServiciosVel;
import com.imss.sivimss.hoja.consignacion.model.request.ReporteServiciosVelDto;
import com.imss.sivimss.hoja.consignacion.service.ReporteServiciosVelService;
import com.imss.sivimss.hoja.consignacion.util.AppConstantes;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.LogUtil;
import com.imss.sivimss.hoja.consignacion.util.ProviderServiceRestTemplate;
import com.imss.sivimss.hoja.consignacion.util.Response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ReporteServiciosVelImpl implements ReporteServiciosVelService {
	
	@Autowired
	private LogUtil logUtil;

	@Value("${endpoints.rutas.dominio-consulta}")
	private String urlConsulta;
	@Value("${endpoints.ms-reportes}")
	private String urlReportes;
	@Value("${formato-fecha}")
	private String fecFormat;
	
	@Value("${modulo.reporte-serv-vel}")
	private String reporteServVel;
	
	private static final String CONSULTA = "consulta";
	private static final String INFORMACION_INCOMPLETA = "Informacion incompleta";
	private static final String EXITO = "EXITO";
	private static final String IMPRIMIR = "IMPRIMIR";
	
	@Autowired
	private ProviderServiceRestTemplate providerRestTemplate;
	
	ReporteServiciosVel reporteServicios = new ReporteServiciosVel();
	
	Gson gson = new Gson();
	
	@Override
	public Response<?> generarReporteServiciosVel(DatosRequest request, Authentication authentication)
			throws IOException, ParseException {
		String datosJson = String.valueOf(request.getDatos().get(AppConstantes.DATOS));
		ReporteServiciosVelDto reporte= gson.fromJson(datosJson, ReporteServiciosVelDto.class);
		if(reporte.getFecha_inicial()!=null) {
			reporte.setFecInicioConsulta(formatFecha(reporte.getFecha_inicial()));
   	   		reporte.setFecFinConsulta(formatFecha(reporte.getFecha_final()));
		}
		Map<String, Object> envioDatos = new ReporteServiciosVel().generarReporte(reporte, reporteServVel);
		Response<?> response = providerRestTemplate.consumirServicioReportes(envioDatos, urlReportes,
				authentication);
		logUtil.crearArchivoLog(Level.INFO.toString(), this.getClass().getSimpleName(),this.getClass().getPackage().toString(),"SE GENERO CORRECTAMENTE EL REPORTE SERVICIOS VELATORIOS", IMPRIMIR);
		return response;
	}

	
	 public String formatFecha(String fecha) throws ParseException {
			Date dateF = new SimpleDateFormat("dd/MM/yyyy").parse(fecha);
			DateFormat fecForma = new SimpleDateFormat("yyyy-MM-dd", new Locale("es", "MX"));
			return fecForma.format(dateF);       
		}
}
