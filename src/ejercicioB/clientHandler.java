package ejercicioB;

import java.util.Scanner;

public class clientHandler {
    public static void main(String[] args) {

        System.out.println("BIENVENIDO A LA APLICACION DE CLIENTE JUANMICA DRIVE");
        new Driveando("local/").start();


        /*
        Scanner sc = new Scanner(System.in);
        String opcion;
        do{
            menu();

            System.out.print("Introduce una opcion: ");
            opcion = sc.nextLine();

            switch (opcion){
                case "1":
                    System.out.println("Subir fichero");
                    break;
                case "2":
                    System.out.println("Borrar fichero");
                    break;
                case "3":
                    System.out.println("Listar ficheros");
                    break;
                case "4":
                    System.out.println("Saliendo...");
                    break;
                default:
                    System.out.println("Opcion no valida");
            }
        }while (opcion.equals("1") || opcion.equals("2") || opcion.equals("3") || opcion.equals("4"));

         */
        

    }


    private static void menu(){
        System.out.println("1. Subir fichero");
        System.out.println("2. Borrar fichero");
        System.out.println("3. Listar ficheros");
        System.out.println("4. Salir");
    }


}
