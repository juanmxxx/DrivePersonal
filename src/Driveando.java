import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Driveando extends Thread{
    private static FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
    private static final String remotePath = "C:/Users/juanm/OneDrive - UNIVERSIDAD DE GRANADA/DAM/2º/Programacion servicios y procesos/tema4/clientServer/";
    private static final String optionalPath = "C:/Users/juanm/";
    private static String filePath;

    public Driveando(String path) {
        clienteFTP = new FTPClient();
        filePath = path;
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
        InputStream is = new FileInputStream(ficheroLocal);
        boolean enviado = clienteFTP.storeFile(ficheroLocal.getName(), is);
        is.close();
        return enviado; // Devuelve true si se ha subido correctamente
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

    private void sincronizar(){
        try {
            conectar();
            List<String> ficherosLocales = ficherosLocal();
            List<String> ficherosRemotos = ficherosRemotos();

            for(String archivoRemoto: ficherosRemotos){
                if(!ficherosLocales.contains(archivoRemoto)){
                    eliminarDirectorioYContenido(archivoRemoto);
                }
            }

            for (String ficheroLocal : ficherosLocales) {
                if (!ficherosRemotos.contains(ficheroLocal)) {
                    subirCarpeta(new File(filePath), ficheroLocal);
                }
            }

            desconectar();
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


    private void updateFiles() throws IOException {
        List<String> ficherosLocales = ficherosLocal();
        List<String> ficherosRemotos = ficherosRemotos();

        for (String ficheroLocal : ficherosLocales) {
            if (!ficherosRemotos.contains(ficheroLocal)) {
                subirCarpeta(new File(filePath), ficheroLocal);
            }
        }

    }

    private void subirCarpeta(File carpetaLocal, String rutaRemota) throws IOException {
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
                subirCarpeta(archivo, rutaRemotaArchivo);
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

    // Este método se encarga de eliminar un directorio y su contenido.
    private static void eliminarDirectorioYContenido(String directorio) throws IOException {
        // Primero, obtenemos una lista de todos los archivos y subdirectorios en el directorio que queremos eliminar.
        FTPFile[] files = clienteFTP.listFiles(directorio);

        // Luego, recorremos cada archivo y subdirectorio.
        for (FTPFile file : files) {
            // Construimos la ruta completa del archivo o subdirectorio.
            String path = directorio + "/" + file.getName();
            // Si es un subdirectorio, llamamos a este método recursivamente.
            if (file.isDirectory()) {
                eliminarDirectorioYContenido(path);
            } else {
                // Si es un archivo, lo eliminamos.
                clienteFTP.deleteFile(path);
            }
        }

        // Finalmente, intentamos eliminar el directorio. Si no podemos eliminarlo, imprimimos un mensaje de error.
        boolean directoryRemoved = clienteFTP.removeDirectory(directorio);
        if (!directoryRemoved) {
            System.out.println("No se pudo eliminar el directorio: " + directorio);
        }
    }


}
