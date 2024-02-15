import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class pruebas {
    private static FTPClient clienteFTP = new FTPClient();
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private String filePath;


    private static void conectar() throws IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        // Verificar si la conexión fue exitosa
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        // Verificar las credenciales de acceso
        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);
        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }

        // Establecer el tipo de transferencia de archivos (binario)
        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private static void desconectar() throws IOException {
        clienteFTP.disconnect();
    }
    public static void main(String[] args) throws IOException {
        try {
            conectar();
            eliminarDirectorioYContenido("hola");
            desconectar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void eliminarDirectorioYContenido(String directorio) throws IOException {
        FTPFile[] files = clienteFTP.listFiles(directorio);

        for (FTPFile file : files) {
            String path = directorio + "/" + file.getName();
            if (file.isDirectory()) {
                eliminarDirectorioYContenido(path);
            } else {
                clienteFTP.deleteFile(path);
            }
        }

        boolean directoryRemoved = clienteFTP.removeDirectory(directorio);
        if (!directoryRemoved) {
            System.out.println("No se pudo eliminar el directorio: " + directorio);
        }
    }

}
