import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.net.Socket;

import java.util.Scanner;

public class Cliente {

        public static boolean recibiendo = true;
        private static AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
        private static File f = new File("Grabacion.wav");
        public static boolean sigue = true;

    public static void main(String[] args) throws IOException {



        try{
            Socket socket;
            byte[] mensaje_bytes = new byte[256];
            socket = new Socket("127.0.0.1",5001);

            new Recibiendo(socket).start();
            new Enviando(socket).start();

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Hola soy el cliente");
            while (sigue){

            }

            System.out.println("Enviar peticion de fin");
            Thread.sleep(5000);


            }catch (Exception e){
            System.err.println(e.getMessage());
        }
    }

    public static class Recibiendo extends Thread{

        DataInputStream in;
        private boolean loop = true;
        private int tam;
        private  BufferedInputStream buffin;
        private int contador = 0;

        public Recibiendo(Socket sc) throws IOException {

            in = new DataInputStream(sc.getInputStream());
            buffin = new BufferedInputStream( sc.getInputStream() );
        }

        @Override
        public void run() {
            try {
                while(loop) {
                    if(in.readUTF().equals("TAM")){
                        tam = in.readInt();
                        System.out.println("Recibido tamaño");

                    }

                    FileOutputStream fos = new FileOutputStream("Recibido"+contador+".wav");
                    BufferedOutputStream buffout = new BufferedOutputStream( fos );


                    // Creamos el array de bytes para leer los datos del archivo
                    byte[] buffer = new byte[ tam ];

                    // Obtenemos el archivo mediante la lectura de bytes enviados
                    for( int i = 0; i < buffer.length; i++ )
                    {
                        buffer[ i ] = ( byte )buffin.read( );
                    }
                    System.out.println("llego");
                    // Escribimos el archivo
                    buffout.write( buffer );
                    System.out.println("llego2");
                    // Cerramos flujos
                    buffout.flush();
                    System.out.println("llego3");
                    buffin.close();
                    System.out.println("llego4");
                    buffout.close();
                    System.out.println("llego5");
                    String res =in.readUTF();
                    System.out.println(res);
                    contador++;
                }
            }catch (Exception e){}
        }
    }

    public static class Enviando extends Thread{

        DataOutputStream out;
        private boolean loop = true;
        Scanner in = new Scanner(System.in);
        String input = "";
        public Enviando(Socket sc) throws IOException {
            out = new DataOutputStream(sc.getOutputStream());
        }

        @Override
        public void run() {
            try{
                while(loop){
                    input = in.nextLine();
                    switch (input){
                        case "e": out.writeUTF("Mensaje e");
                            break;
                        case "p": out.writeUTF("FIN");
                                  loop = false;
                                  sigue = false;
                            break;
                    }
                }
            }catch (Exception e){}
        }
    }
}
