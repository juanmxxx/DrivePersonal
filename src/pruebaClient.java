import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.zip.*;

public class pruebaClient {
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";

    private static void crearCarpeta() throws InterruptedException {
        String folderPath = "client";

        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("Creando carpeta: " + folderPath + "...");
                Thread.sleep(2000);
                System.out.println("Carpeta creada");
            }
        }
    }



    public static void main(String[] args) throws InterruptedException {
        String nombreCarpeta;
        Scanner sc = new Scanner(System.in);
        crearCarpeta();

        System.out.print("Introduce el nombre de la carpeta que quieres comprimir: ");
        nombreCarpeta = sc.nextLine();
        nombreCarpeta += "_" + fechaHoraActual();
        String folderPath = "client\\" + nombreCarpeta;

        File folder = new File(folderPath);
        if (!folder.exists()) {
            if (folder.mkdir()) {
                System.out.println("Carpeta creada");
            } else {
                System.out.println("Error al crear la carpeta");
            }
        }

        String[] archivos = {"archivo1.txt", "archivo2.txt", "archivo3.txt"};
        for (String archivo : archivos) {
            File archivoFile = new File(folderPath, archivo);
            try {
                archivoFile.createNewFile();
                FileWriter writer = new FileWriter(archivoFile);
                writer.write("Contenido del archivo " + archivo);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /*
        String comandoCompact = "compact /C " + nombreCarpeta;

        try {
            // Crear el proceso
            Process proceso = new ProcessBuilder("cmd.exe", "/c", comandoCompact).start();

            // Esperar a que el proceso termine
            proceso.waitFor();

            // Verificar el resultado del proceso
            int exitCode = proceso.exitValue();
            if (exitCode == 0) {
                System.out.println("Archivo comprimido exitosamente.");
            } else {
                System.out.println("Error al comprimir el archivo. Código de salida: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

         */

        //Subir el fichero comprimido al servidor FTP
        new GestorFTP(folderPath).start();

    }

    public static String fechaHoraActual() {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss");
        // Formatear la fecha y hora actual según el formato
        return fechaHoraActual.format(formatter);
    }
}
