import javax.sound.sampled.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor{

    private static Socket socket;
    public static ServerSocket ss= null;

    public Servidor() throws IOException {
        ss = new ServerSocket(5001);
    }


    public static void main(String[] args) {

        try {
            new Servidor();
            System.out.println("Esperando peticiones");

            while (true) {
                socket = ss.accept();
                socket.setSoLinger(true, 10);
                System.out.println("peticion recibida");
                new Thread(new HiloPetion(socket)).start();
            }
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public static class HiloPetion implements Runnable{

        protected  AudioFileFormat.Type aFF_T = AudioFileFormat.Type.WAVE;
        protected  AudioFormat aF = new AudioFormat(8000.0F, 16, 1, true, false);
        protected  TargetDataLine tD;
        private  File f;
        private DataOutputStream out;
        private Socket sc;
        public boolean grabar = true;
        public boolean listo = false;
        public boolean empezar = true;
        public  int contGrabados = 0;


        public HiloPetion(Socket sc) throws LineUnavailableException {
            this.sc = sc;
            DataLine.Info dLI = new DataLine.Info(TargetDataLine.class,aF);
            tD = (TargetDataLine)AudioSystem.getLine(dLI);

            System.out.println("Datos del Cliente");
            System.out.println("IP "+sc.getRemoteSocketAddress());
            System.out.println("Puerto "+sc.getPort());
        }

        @Override
        public void run() {
            try{
                new Recibiendo(sc).start();
                new Enviando(sc).start();
                for(int i=0;grabar;i++) {

                    f = new File("Grabacion"+i+".wav");
                    System.out.println("Entro bucle");

                    new Grabando().start();
                    Thread.sleep(5000);

                    tD.close();

                    System.out.println("Archivo "+f.getName() +" grabado");
                }

            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        }

        public class Grabando extends Thread{



            @Override
            public void run() {
                try {

                    System.out.println("Grabando");
                    tD.open(aF);
                    tD.start();
                    AudioSystem.write(new AudioInputStream(tD), aFF_T, f);

                }catch (Exception e){}
            }
        }

        public class Recibiendo extends Thread{

            private DataInputStream in;
            private boolean loop = true;
            public Recibiendo(Socket sc) throws IOException {
                 in = new DataInputStream(sc.getInputStream());
            }

            @Override
            public void run() {
                try{
                    while(loop) {
                        String msg = in.readUTF();
                        System.out.println("MENSAJE DEL CLIENTE " + msg);
                        if (msg.equals("FIN")) {
                            loop = false;
                            grabar = false;
                        }
                    }
                }catch (Exception e){}
            }
        }

        public class Enviando extends Thread{

            ObjectOutputStream out;
            Socket sc;
            boolean enviadoUltimo=false;
            boolean loop = true;
            private int contador = 0;


            public Enviando(Socket sc) throws IOException {
                this.sc = sc;
                out = new ObjectOutputStream(sc.getOutputStream());
            }

            @Override
            public void run() {
                try{

                    while(loop) {

                        Thread.sleep(6000);
                        String nombre = "Grabacion"+contador+".wav";
                        FileInputStream fis = new FileInputStream(nombre);

// Se instancia y rellena un mensaje de envio de fichero
                        Mensaje mensaje = new Mensaje();
                        mensaje.nombreFichero = nombre;

// Se leen los primeros bytes del fichero en un campo del mensaje
                        int leidos = fis.read(mensaje.contenidoFichero);

// Bucle mientras se vayan leyendo datos del fichero
                        while (leidos > -1) {
                            // Se rellena el número de bytes leidos
                            mensaje.bytesValidos = leidos;

                            // Si no se han leido el máximo de bytes, es porque el fichero
                            // se ha acabado y este es el último mensaje
                            if (leidos < Mensaje.LONGITUD_MAXIMA) {
                                // Se marca que este es el último mensaje
                                mensaje.ultimoMensaje = true;
                                enviadoUltimo = true;
                            } else
                                mensaje.ultimoMensaje = false;

                            // Se envía por el socket
                            out.writeObject(mensaje);

                            // Si es el último mensaje, salimos del bucle.
                            if (mensaje.ultimoMensaje)
                                break;

                            // Se crea un nuevo mensaje
                            mensaje = new Mensaje();
                            mensaje.nombreFichero = nombre;

                            // y se leen sus bytes.
                            leidos = fis.read(mensaje.contenidoFichero);
                        }

// En caso de que el fichero tenga justo un múltiplo de bytes de MensajeTomaFichero.LONGITUD_MAXIMA,
// no se habrá enviado el mensaje marcado como último. Lo hacemos ahora.
                        if (enviadoUltimo == false) {
                            mensaje.ultimoMensaje = true;
                            mensaje.bytesValidos = 0;
                            out.writeObject(mensaje);
                        }
// Se cierra el ObjectOutputStream

                        System.out.println("Termino de enviar ");
                        contador++;
                    }
                }catch (Exception e ){}
            }
        }
    }

}
