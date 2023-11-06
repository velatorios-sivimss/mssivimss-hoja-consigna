package com.imss.sivimss.hoja.consignacion.util;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LogUtil {
    @Value("${ruta-log}")
    private String rutaLog;

    @Value("${spring.application.name}")
    private String aplicacion;

    private String formatoFechaLog = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss").format(new Date());

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogUtil.class);

    public void crearArchivoLog(String tipoLog, String origen, String clasePath, String mensaje, String tiempoEjecucion)
            throws IOException {
        File archivo = new File(rutaLog + aplicacion + new SimpleDateFormat("ddMMyyyy").format(new Date()) + ".log");
        FileWriter escribirArchivo = null;
        try {
            escribirArchivo = new FileWriter(archivo, true);
            escribirArchivo.write("" + formatoFechaLog + " --- [" + tipoLog + "] " + origen + " " + clasePath + " : "
                    + mensaje + " ,  " + tiempoEjecucion);
            escribirArchivo.write("\r\n");
            escribirArchivo.close();
        } catch (Exception e) {
            log.error("No se puede escribir el log.");
            log.error(e.getMessage());
            if (escribirArchivo != null) {
                escribirArchivo.close();
            }
        }

    }

}
