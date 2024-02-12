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

public class Client {
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";

    private void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);

        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }

        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void desconectar() throws IOException {
        clienteFTP.disconnect();
    }

    private boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado;
    }

    private boolean descargarFichero(String ficheroRemoto, String pathLocal)
            throws IOException {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(pathLocal));
        boolean recibido = clienteFTP.retrieveFile(ficheroRemoto, os);
        os.close();
        return recibido;
    }

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

        try {
            FileOutputStream fos = new FileOutputStream("client\\" + nombreCarpeta + ".zip");
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            zipFolder(folder, folder.getName(), zipOut);

            zipOut.close();
            fos.close();

            System.out.println("Carpeta comprimida");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void zipFolder(File folder, String parentFolder, ZipOutputStream zos) {
        try {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) {
                    zipFolder(file, parentFolder, zos);
                    continue;
                }
                zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String fechaHoraActual() {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss");
        // Formatear la fecha y hora actual según el formato
        return fechaHoraActual.format(formatter);
    }
}
