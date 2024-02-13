import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class GestorFTP extends Thread{
    private FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private String filePath;

    public GestorFTP(String path) {
        clienteFTP = new FTPClient();
        this.filePath = path;
    }
    private void conectar() throws SocketException, IOException {
        clienteFTP.connect(SERVIDOR, PUERTO);
        int respuesta = clienteFTP.getReplyCode();

        if (!FTPReply.isPositiveCompletion(respuesta) ) {
            clienteFTP.disconnect();
            throw new IOException("Error al conectar con el servidor FTP");
        }

        boolean credencialesOK=clienteFTP.login(USUARIO,PASSWORD);

        if (!credencialesOK) {
            throw new IOException("Error al conectar con el servidor FTP. Credenciales incorrectas.");
        }

        clienteFTP.setFileType(FTP.BINARY_FILE_TYPE);
    }

    private void desconectar () throws IOException {
        clienteFTP.disconnect();
    }
    private boolean subirFichero (String path) throws IOException {
        File ficheroLocal = new File(path);
        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado;
    }

    public void run() {
        try {
            conectar();
            subirFichero(filePath);
            desconectar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
