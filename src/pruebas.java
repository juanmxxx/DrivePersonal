import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
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
        listarFicherosRemotos();
    }

    private static void listarFicherosRemotos() throws IOException {

        List<String> ficheros = new ArrayList<>();
        conectar();
        String[] nombres = clienteFTP.listNames();

        if (nombres != null) {
            ficheros.addAll(Arrays.asList(nombres));
        }

        for (String fichero : ficheros) {
            System.out.println(fichero);
        }
        desconectar();
    }

    private static void listarFicherosLocal() throws IOException {
        File directorio = new File("clientServer");
        File[] ficheros = directorio.listFiles();
        for (File fichero : ficheros) {
            System.out.println(fichero.getName());
        }
    }
}
