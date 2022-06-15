import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    final static int TIME_WRITING_MESSAGE = 1000;

    public static void main(String[] args) {
        String host = "localhost";
        int port = 8080;

        new Thread(() -> client(host, port)).start();
        new Thread(() -> {
            try {
                server(host, port);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void client(String host, int port) {
        InetSocketAddress socketAddress = new InetSocketAddress(host, port);

        try (final SocketChannel socketChannel = SocketChannel.open(); Scanner scanner = new Scanner(System.in)) {

            socketChannel.connect(socketAddress);

            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

            String msg;
            while (true) {
                System.out.println("Введите сообщение для обработки на сервере:");
                msg = scanner.nextLine();

                if (msg.equals("end")) break;

                socketChannel.write(ByteBuffer.wrap(msg.getBytes(StandardCharsets.UTF_8)));
                Thread.sleep(TIME_WRITING_MESSAGE);

                int bytesCount = socketChannel.read(inputBuffer);
                System.out.println("Сообщение без пробелов: "
                        + new String(inputBuffer.array(), 0, bytesCount,
                        StandardCharsets.UTF_8));
                inputBuffer.clear();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void server(String host, int port) throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.bind(new InetSocketAddress(host, port));

        try (SocketChannel socketChannel = serverSocket.accept()) {

            final ByteBuffer inputBuffer = ByteBuffer.allocate(2 << 10);

            while (socketChannel.isConnected()) {
                int bytesCount = socketChannel.read(inputBuffer);

                if (bytesCount == -1) break;

                final String msg = new String(inputBuffer.array(), 0, bytesCount, StandardCharsets.UTF_8);
                inputBuffer.clear();

                socketChannel.write(ByteBuffer.wrap((msg.replace(" ", "")).getBytes(StandardCharsets.UTF_8)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

