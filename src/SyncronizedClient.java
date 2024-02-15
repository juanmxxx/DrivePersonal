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
    //private static FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private static String remotePath =  "C:/Users/juanm/OneDrive - UNIVERSIDAD DE GRANADA/DAM/2º/Programacion servicios y procesos/tema4/clientServer";
    private static String filePath;

    public SyncronizedClient(String path) {
        filePath = path;
    }

/*
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

 */
/*
    private static void desconectar() throws IOException {
        clienteFTP.disconnect();
    }

 */


    private boolean subirFichero(FTPClient clienteFTP, String path) throws IOException, InterruptedException {
        File ficheroLocal = new File(path);

        System.out.println(ficheroLocal.getName());
        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado; // Devuelve true si se ha subido correctamente
    }

    private List<String> ficherosRemotos(FTPClient clienteFTP) throws IOException {

        List<String> ficheros = new ArrayList<>();
        clienteFTP.changeWorkingDirectory(remotePath);

        //conectar();
        String[] nombres = clienteFTP.listNames();
        File ficheroComparacion = new File(nombres[0]);
        String rutaAb= ficheroComparacion.getAbsolutePath();
        String rutaCan= ficheroComparacion.getCanonicalPath();
        System.out.println("ab  "+rutaAb);
        System.out.println("aC  "+rutaCan);
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

    private void sincronizar(){
        try {
            FTPClient clienteFTP= new FTPClient();
            clienteFTP.connect(SERVIDOR,PUERTO);
            clienteFTP.login(USUARIO,PASSWORD);

            List<String> ficherosLocales = ficherosLocal();
            List<String> ficherosRemotos = ficherosRemotos(clienteFTP);

            for(String archivoRemoto: ficherosRemotos){
                if(!ficherosLocales.contains(archivoRemoto)){
                    borrarArchivo(clienteFTP, archivoRemoto);
                }
            }

            for (String ficheroLocal : ficherosLocales) {
                if (!ficherosRemotos.contains(ficheroLocal)) {
                    subirCarpeta(clienteFTP, new File(filePath), ficheroLocal);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        try {
            System.out.println("Comenzando a sincronizar");
            while (true) {
                sincronizar();
                Thread.sleep(5000);
                System.out.println("Sincronizando....");
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private void subirCarpeta(FTPClient clienteFTP, File carpetaLocal, String rutaRemota) throws IOException {
        // Crear el directorio remoto
        boolean directorioCreado = clienteFTP.makeDirectory(rutaRemota);
        if (!directorioCreado) {
            System.out.println("No se pudo crear el directorio remoto: " + rutaRemota);
            return;
        }

        // Obtener los archivos y subdirectorios de la carpeta local
        File[] archivos = carpetaLocal.listFiles();
        if (archivos == null) {
            return;
        }

        // Recorrer los archivos y subdirectorios
        for (File archivo : archivos) {
            String rutaRemotaArchivo = rutaRemota + "/" + archivo.getName();
            if (archivo.isDirectory()) {
                // Si es un subdirectorio, llamar a este método recursivamente
                subirCarpeta(clienteFTP, archivo, rutaRemotaArchivo);
            } else {
                // Si es un archivo, subirlo al servidor FTP
                try (InputStream is = new FileInputStream(archivo)) {
                    boolean archivoSubido = clienteFTP.storeFile(rutaRemotaArchivo, is);
                    if (!archivoSubido) {
                        System.out.println("No se pudo subir el archivo: " + archivo.getName());
                    }
                }
            }
        }
    }

    private static void borrarArchivo(FTPClient clienteFTP, String archivo) throws IOException {
        File ficheroCompracion= new File(archivo);
        String rutaAb= ficheroCompracion.getAbsolutePath();
        String rutaCan= ficheroCompracion.getCanonicalPath();

        System.out.println("ab  "+rutaAb);
        System.out.println("aC  "+rutaCan);
        boolean archivoBorrado = clienteFTP.deleteFile(archivo);


        if (!archivoBorrado) {
            throw new IOException("No se pudo borrar el archivo: " + archivo);
        }
    }


}
