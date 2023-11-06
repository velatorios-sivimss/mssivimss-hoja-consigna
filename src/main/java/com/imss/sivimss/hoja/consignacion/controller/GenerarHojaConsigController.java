package com.imss.sivimss.hoja.consignacion.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.imss.sivimss.hoja.consignacion.service.GenerarHojaConsigService;
import com.imss.sivimss.hoja.consignacion.util.DatosRequest;
import com.imss.sivimss.hoja.consignacion.util.ProviderServiceRestTemplate;
import com.imss.sivimss.hoja.consignacion.util.Response;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/hoja-consignacion")
public class GenerarHojaConsigController {
	
	@Autowired
	private ProviderServiceRestTemplate providerRestTemplate;
	
	@Autowired
	private GenerarHojaConsigService generarHojaService;
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/buscar-articulos")
	public CompletableFuture<Object> buscarArticulosConsignados(@RequestBody DatosRequest request,Authentication authentication) throws IOException, ParseException{
		Response<Object> response = generarHojaService.buscarArtConsig(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/buscar-hoja-consig")
	public CompletableFuture<Object> buscarHojaConsignacion(@RequestBody DatosRequest request,Authentication authentication) throws IOException, ParseException{
		Response<Object> response = generarHojaService.buscarHojaConsig(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/generar")
	public CompletableFuture<Object> generarHojaConsignacion(@RequestBody DatosRequest request,Authentication authentication) throws IOException, ParseException{
		Response<Object> response = generarHojaService.generarHojaConsig(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/generar-reporte")
	public CompletableFuture<Object> generarReporteHojaConsignacion(@RequestBody DatosRequest request,Authentication authentication) throws IOException{
		Response<Object> response = generarHojaService.generarReporteHojaConsig(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/reporte")
	public CompletableFuture<Object> generarReporteConsultaPrincipal(@RequestBody DatosRequest request,Authentication authentication) throws IOException, ParseException{
		Response<Object> response = generarHojaService.generarReporteConsulta(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/catalogo")
	public CompletableFuture<Object> buscarCatalogo(@RequestBody DatosRequest request,Authentication authentication) throws IOException{
		Response<Object> response = generarHojaService.buscarCatalogo(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/ver-detalle")
	public CompletableFuture<Object> detalleHojaConsignacion(@RequestBody DatosRequest request,Authentication authentication) throws IOException{
		Response<Object> response = generarHojaService.detalleHojaConsig(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	
	@CircuitBreaker(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@Retry(name = "msflujo", fallbackMethod = "fallbackGenerico")
	@TimeLimiter(name = "msflujo")
	@PostMapping("/adjuntar-factura")
	public CompletableFuture<Object> adjuntarFacturaHojaConsig(@RequestBody DatosRequest request,Authentication authentication) throws IOException{
		Response<Object> response = generarHojaService.adjuntarFactura(request,authentication);
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}	
	/**
	 * fallbacks generico
	 * 
	 * @return respuestas
	 */
	private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
			CallNotPermittedException e) {
		Response<?> response = providerRestTemplate.respuestaProvider(e.getMessage());
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}

	private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
			RuntimeException e) {
		Response<?> response = providerRestTemplate.respuestaProvider(e.getMessage());
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}

	private CompletableFuture<?> fallbackGenerico(@RequestBody DatosRequest request, Authentication authentication,
			NumberFormatException e) {
		Response<?> response = providerRestTemplate.respuestaProvider(e.getMessage());
		return CompletableFuture
				.supplyAsync(() -> new ResponseEntity<>(response, HttpStatus.valueOf(response.getCodigo())));
	}


}
