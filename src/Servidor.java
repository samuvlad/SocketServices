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

            DataOutputStream out;
            Socket sc;
            private File archivo;
            int tam;
            int i = 0;

            public Enviando(Socket sc) throws IOException {
                this.sc = sc;
                out = new DataOutputStream(sc.getOutputStream());
            }

            @Override
            public void run() {
                try{
                    while(true) {

                        System.out.println("---------------------------------termine vuelvo a enviar antes 6 seg");
                        Thread.sleep(6000);
                        System.out.println("despues1");
                        archivo = new File("Grabacion"+contGrabados+".wav");
                        System.out.println("despues2");
                        tam = (int) archivo.length();
                        System.out.println("tamaño "+tam);
                        out.writeInt(tam);
                        System.out.println("despues4");

                        FileInputStream fis = new FileInputStream(archivo.getName());
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());

                        byte[] buffer = new byte[tam];

                        bis.read(buffer);

                        for (int i = 0; i < buffer.length; i++) {
                            bos.write(buffer[i]);
                        }

                        System.out.println("------------------------------Archivo Enviado: " + archivo.getName());
                        fis.close();
                        bis.close();


                        contGrabados++;



                    }
                }catch (Exception e ){}
            }
        }
    }

}
