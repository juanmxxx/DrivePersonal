package ejercicioA;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GestorFTP extends Thread {
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private String filePath;

    // Constructor que inicializa el cliente FTP y establece el path del fichero a subir
    public GestorFTP(String path) {
        clienteFTP = new FTPClient();
        this.filePath = path;
    }

    // Método privado para conectar al servidor FTP
    private void conectar() throws IOException {
        // Conectar al servidor FTP, puerto 21m, con un timeout de 10 segundos
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        // Verificar si la conexión fue exitosa
        if (!FTPReply.isPositiveCompletion(respuesta)) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        // Verificar las credenciales de acceso, si no son correctas, lanzar una excepción
        boolean credencialesOK = clienteFTP.login(USUARIO, PASSWORD);
        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }

        // Establecer el tipo de transferencia de archivos (binario)
        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    // Método privado para desconectar del servidor FTP
    private void desconectar() throws IOException {
        clienteFTP.disconnect();
    }

    // Método privado para subir un fichero al servidor FTP
    private boolean subirFichero(String path) throws IOException {
        File ficheroLocal = new File(path);
        System.out.println(ficheroLocal.getName());

        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado; // Devuelve true si se ha subido correctamente
    }

    // Sobrescribe el método run de la clase Thread para ejecutar la tarea de subir el fichero al servidor FTP
    public void run() {
        try {
            conectar(); // Conectar al servidor FTP
            System.out.println(filePath);

            if (subirFichero(filePath)) // Subir el fichero
                System.out.println("Fichero subido correctamente");

            desconectar(); // Desconectar del servidor FTP
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
