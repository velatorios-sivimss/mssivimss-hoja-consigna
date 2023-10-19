package com.imss.sivimss.hoja.consignacion.service;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.security.core.Authentication;

import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.Response;

public interface GenerarHojaConsigService {

	Response<Object> buscarArtConsig(DatosRequest request, Authentication authentication) throws IOException, ParseException;

	Response<Object> generarHojaConsig(DatosRequest request, Authentication authentication) throws IOException, ParseException;

	Response<Object> generarReporteHojaConsig(DatosRequest request, Authentication authentication) throws IOException;

	Response<Object> buscarCatalogo(DatosRequest request, Authentication authentication) throws IOException;

	Response<Object> buscarHojaConsig(DatosRequest request, Authentication authentication) throws IOException, ParseException;

	Response<Object> generarReporteConsulta(DatosRequest request, Authentication authentication) throws IOException, ParseException;

	Response<Object> detalleHojaConsig(DatosRequest request, Authentication authentication) throws IOException;

	Response<Object> adjuntarFactura(DatosRequest request, Authentication authentication) throws IOException;

}
