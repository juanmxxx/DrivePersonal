import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Client {

    private static String carpetaCliente = "clientServer";
    // Método para crear una carpeta llamada "client" si no existe
    private static void crearCarpeta() throws InterruptedException {
        String folderPath = carpetaCliente;

        // Crear un objeto File que representa la carpeta
        File folder = new File(folderPath);

        // Verificar si la carpeta no existe
        if (!folder.exists()) {
            // Si la carpeta no existe, intenta crearla
            if (folder.mkdir()) {
                System.out.println("Creando carpeta: " + folderPath + "...");
                Thread.sleep(2000); // Esperar 2 segundos simulando un proceso
                System.out.println("Carpeta creada");
            }
        }
    }

    // Método principal
    public static void main(String[] args) throws InterruptedException {
        String nombreCarpeta;
        Scanner sc = new Scanner(System.in);

        // Llamar al método para crear la carpeta "client" si no existe
        crearCarpeta();

        // Solicitar al usuario que introduzca el nombre de la carpeta a comprimir
        System.out.print("Introduce el nombre de la carpeta que quieres comprimir: ");
        nombreCarpeta = sc.nextLine();
        nombreCarpeta += "_" + fechaHoraActual(); // Agregar fecha y hora actual al nombre de la carpeta
        String folderPath = carpetaCliente + "\\" + nombreCarpeta;

        // Crear un objeto File que representa la carpeta a comprimir
        File folder = new File(folderPath);

        // Verificar si la carpeta no existe
        if (!folder.exists()) {
            // Si la carpeta no existe, intenta crearla
            if (folder.mkdir()) {
                System.out.println("Carpeta creada");
            } else {
                System.out.println("Error al crear la carpeta");
            }
        }

        // Agregar "/" al nombre de la carpeta para formar una ruta de directorio válida
        nombreCarpeta = carpetaCliente + "/" + nombreCarpeta;
        // Comprimir la carpeta utilizando el método compressDirectory()
        comprimirDirectorio(nombreCarpeta);
        // Agregar extensión ".zip" al nombre de la carpeta
        nombreCarpeta += ".zip";

        // Subir el fichero comprimido al servidor FTP envolviendo el código en un hilo
        new GestorFTP(nombreCarpeta).start();

    }

    // Método para obtener la fecha y hora actual formateada como "dd.MM.yyyy_HH-mm-ss"
    public static String fechaHoraActual() {
        LocalDateTime fechaHoraActual = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm-ss");
        // Formatear la fecha y hora actual según el formato
        return fechaHoraActual.format(formatter);
    }

    // Método para comprimir una carpeta usando 7zip

    /**
     * Este metodo en lo que consiste es en comprimir una carpeta usando 7zip pero utilizando
     * un proceso para ello para asi llegar a poder aprovechar lo que es la programacion concurrente y que se puedan
     * ejecutar varios procesos a la misma vez para comprimir archivos
     * @param nombreCarpeta el directorio que se comprimira
     */
    public static void comprimirDirectorio(String nombreCarpeta) {
        try {
            // Mostrar el nombre de la carpeta que se va a comprimir
            System.out.println("Este es el nombre de la carpeta ahora mismo: " + nombreCarpeta);
            // Comando para comprimir la carpeta usando 7zip
            String command = "7z a -tzip " + nombreCarpeta + ".zip " + nombreCarpeta;

            // Crear el proceso
            Process process = new ProcessBuilder("cmd.exe", "/C", command).start();

            // Esperar a que el proceso termine
            process.waitFor();

            // Verificar el resultado del proceso
            int exitCode = process.exitValue();
            if (exitCode == 0) {
                System.out.println("Directorio comprimido exitosamente.");
            } else {
                System.out.println("Error al comprimir el directorio. Código de salida: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
