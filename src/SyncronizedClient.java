import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyncronizedClient extends Thread{

    private static FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private static String filePath;

    public SyncronizedClient(String path) {
        clienteFTP = new FTPClient();
        this.filePath = path;
    }

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


    private boolean subirFichero(String path) throws IOException, InterruptedException {
        File ficheroLocal = new File(path);

        System.out.println(ficheroLocal.getName());
        Thread.sleep(2000);
        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado; // Devuelve true si se ha subido correctamente
    }

    public void run() {
        try {
            while (true) {
                conectar();
                //Por ahora solo actualiza y borra ficheros
                comprobarFicheros();
                //Tasa de actualizacion cada 15 segundos
                desconectar();
                Thread.sleep(15000);
                System.out.println("Sincronizando....");
            }


        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void updateFiles(){
        try {
            List<String> ficherosRemotos = ficherosRemotos();
            List<String> ficherosLocal = ficherosLocal();

            for (String ficheroLocal : ficherosLocal) {
                if (!ficherosRemotos.contains(ficheroLocal)) {
                    subirFichero(filePath + ficheroLocal);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void deleteFiles(){
        try {
            List<String> ficherosRemotos = ficherosRemotos();
            List<String> ficherosLocal = ficherosLocal();
            for (String ficheroRemoto : ficherosRemotos) {
                if (!ficherosLocal.contains(ficheroRemoto)) {
                    clienteFTP.deleteFile(filePath + ficheroRemoto);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void comprobarFicheros() {
        updateFiles();
        deleteFiles();
    }

    private List<String> ficherosRemotos() throws IOException {
        List<String> ficheros = new ArrayList<>();
        //conectar();
        String[] nombres = clienteFTP.listNames();
        if (nombres != null) {
            ficheros.addAll(Arrays.asList(nombres));
        }
        //desconectar();
        return ficheros;
    }

    private List<String> ficherosLocal() throws IOException {
        File directorio = new File(filePath);
        List <String> nombreFicheros = new ArrayList<>();
        File[] ficheros = directorio.listFiles();

        assert ficheros != null;
        for (File fichero : ficheros) {
            nombreFicheros.add(fichero.getName());
        }

        return nombreFicheros;
    }


}
