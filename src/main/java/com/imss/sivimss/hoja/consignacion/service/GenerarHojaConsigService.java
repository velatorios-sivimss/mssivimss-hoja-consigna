package com.imss.sivimss.hoja.consignacion.service;

import java.io.IOException;
import java.text.ParseException;

import org.springframework.security.core.Authentication;

import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.Response;

public interface GenerarHojaConsigService {

	Response<?> buscarArtConsig(DatosRequest request, Authentication authentication) throws IOException, ParseException;

}
