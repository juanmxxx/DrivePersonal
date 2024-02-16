package ejercicioB;

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

public class Driveando extends Thread {
    private static FTPClient clienteFTP;
    private static final String SERVIDOR = "localhost";
    private static final int PUERTO = 21;
    private static final String USUARIO = "Miguel";
    private static final String PASSWORD = "1234";
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
        //Obtener la lista de archivos remotos
        List<String> ficheros = new ArrayList<>();
        String[] nombres = clienteFTP.listNames(); //Con el metodo listNames() directamente los proporciona

        if (nombres != null) {
            //Añadir los nombres de los archivos a la lista
            ficheros.addAll(Arrays.asList(nombres));
        }
        return ficheros;
    }

    private List<String> ficherosLocal() throws IOException {
        //Obtener la lista de archivos locales a partir del directorio especificado "clientServer/",
        // es decir, el directorio del cliente
        File directorio = new File(filePath);
        List<String> nombreFicheros = new ArrayList<>();
        // Obtener la lista de archivos y subdirectorios en el directorio
        File[] ficheros = directorio.listFiles();

        assert ficheros != null;
        // Añadir los nombres de los archivos a la lista
        for (File fichero : ficheros) {
            nombreFicheros.add(fichero.getName());
        }

        return nombreFicheros;
    }

    private void sincronizar() {
        try {
            conectar(); //Establecer conexion+
            // Obtener la lista de archivos remotos y locales primero
            List<String> ficherosLocales = ficherosLocal();
            List<String> ficherosRemotos = ficherosRemotos();

            // Recorrer la lista de archivos remotos
            for (String archivoRemoto : ficherosRemotos) {
                // Si un archivo remoto no está en la lista de archivos locales, eliminarlo
                if (!ficherosLocales.contains(archivoRemoto)) {
                    eliminarDirectorioYContenido(archivoRemoto);
                }

            }
            // Recorrer la lista de archivos locales
            for (String ficheroLocal : ficherosLocales) {
                // Si un archivo local no está en la lista de archivos remotos, subirlo
                if (!ficherosRemotos.contains(ficheroLocal)) {
                    subirCarpeta(new File(filePath), ficheroLocal);
                }
            }

            desconectar();
        } catch (IOException | InterruptedException e) {
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

    // Este método se encarga de subir un fichero al servidor FTP
    private void subirCarpeta(File carpetaLocal, String elemento) throws IOException, InterruptedException {
        // Comprobar si el archivo local es un directorio
        File archivoLocal = new File(filePath + elemento);

        // Si no es un directorio, subir el archivo
        if(archivoLocal.isFile()) {
            subirFichero(filePath + elemento);
        }else {
            // Crear el directorio remoto
            boolean directorioCreado = clienteFTP.makeDirectory(elemento);
            if (!directorioCreado) {
                System.out.println("No se pudo crear el directorio remoto: " + elemento);
                return;
            }

            // Obtener los archivos y subdirectorios de la carpeta local
            File[] archivos = new File(filePath + elemento).listFiles();
            if (archivos == null) {
                return;
            }

            // Recorrer los archivos y subdirectorios
            for (File archivo : archivos) {
                String rutaRemotaArchivo = elemento + "/" + archivo.getName();
                System.out.println(rutaRemotaArchivo);
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

    }

    // Este método se encarga de eliminar un directorio y su contenido o si es un archivo.
    private static void eliminarDirectorioYContenido(String directorio) throws IOException {
        // Obtenemos el elemento que queremos eliminar. Puede ser un archivo o un directorio. Y asi poder tratarlo adecuadamente
        FTPFile elemento = clienteFTP.mlistFile(directorio);

        // Si el elemento es un archivo, lo eliminamos directamente.
        if(elemento.isFile()){
            clienteFTP.deleteFile(directorio);
            // Si el elemento es un directorio, llamamos a este método recursivamente.
        }else {
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

            // Finalmente, intentaremos eliminar el directorio. Si no podemos eliminarlo, imprimimos un mensaje de error.
            boolean directoryRemoved = clienteFTP.removeDirectory(directorio);
            if (!directoryRemoved) {
                System.out.println("No se pudo eliminar el directorio: " + directorio);
            }
        }
    }

    private static boolean isUpdate(String archivo) throws IOException {
        File archivoLocal = new File(filePath + archivo);
        FTPFile archivoRemoto = clienteFTP.mlistFile(archivo);

        long modRemFile = archivoRemoto.getTimestamp().getTimeInMillis();
        long modLocFile = archivoLocal.lastModified();

        return modLocFile > modRemFile;
    }


}