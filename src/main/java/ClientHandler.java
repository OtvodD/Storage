import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }


    @Override
    public void run() {
        try (
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream())
        ) {
            System.out.printf("Client %s connected\n", socket.getInetAddress());
            while (true) {
                String command = in.readUTF();
                if ("upload".equals(command)) {
                    try {
                        File file = new File("server"  + File.separator + in.readUTF());
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);

                        long size = in.readLong();
                        System.out.println(size);
                        byte[] buffer = new byte[8 * 1024];


                        // Если я правильно понимаю, то в противном случае данная функция будет бесконечно перезаписывать файл,
                        // так как его размер с каждым разом будет в больше и больше

                        for (int i = 0; i < (size + (buffer.length - 1)) / (buffer.length); i++) {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        fos.close();
                        out.writeUTF("OK");
                    } catch (Exception e) {
                        out.writeUTF("FATAL ERROR");
                    }
                }

                if ("download".equals(command)) {

                    try {
                        File file = new File("server" + File.separator + in.readUTF());
                        if (!file.exists()){
                            throw new FileNotFoundException();
                        }

                        long fileLength = file.length();

                        FileInputStream fis = new FileInputStream(file);

                        int read = 0;

                        byte[] buffer = new byte[8 * 1024];

                        while ((read = fis.read(buffer)) != -1){
                            out.write(buffer, 0, read);
                        }

                        out.flush();
                        String status = in.readUTF();
                        System.out.println("sending status " + status);

                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }

                if ("exit".equals(command)) {
                    System.out.printf("Client %s disconnected correctly\n", socket.getInetAddress());
                    break;
                }

                System.out.println(command);
                out.writeUTF(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

