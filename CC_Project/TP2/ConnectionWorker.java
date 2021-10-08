import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Map;

public class ConnectionWorker implements Runnable {
    Table table;
    DatagramSocket socket;
    InetAddress ffsAddress;
    Integer ffsPort;
    Integer id;
  
    /**
     * Construtor parametrizado de 'ConnectionWorker'.
     * @param t Table.
     * @param s Socket UDP.
     * @param ffsAddress Address do FFS.
     * @param ffsPort Port do FFS.
     * @param id Id do FFS na Table.
     */
    public ConnectionWorker(Table t, DatagramSocket s,InetAddress ffsAddress, Integer ffsPort,Integer id) {
        this.table = t;
        this.socket = s;
        this.ffsAddress = ffsAddress;
        this.ffsPort = ffsPort;
        this.id = id;
    }


    /**
     * Método responsável pela ação do ConnectionWorker
     */
    public void run(){
        try {
            Pacote p = new Pacote();
            p.setTag(0);
            System.out.println(ffsPort);
            DatagramPacket checkConnection = new DatagramPacket(p.getData(), p.getSizeData(), ffsAddress, ffsPort);
            socket.send(checkConnection);
            Boolean recebi = false;
            socket.setSoTimeout(100);
            int tentativas = 0;
            // verifica se o FFS responde, após duas tentativas sem responder remove-o da Table.
            while (!recebi && tentativas<2) {
                try {
                    socket.receive(checkConnection);
                    recebi = true;
                    System.out.println("ACK Existente");
                    socket.setSoTimeout(0);
                } catch(SocketTimeoutException ste) {
                    System.out.println("Sem Resposta400");
                    tentativas++;
                    if (tentativas <2){
                        socket.send(checkConnection);
                    }
                }
            }
            socket.setSoTimeout(0);
            if (tentativas >=2){
                this.table.removeFFS(id);
                this.table.removePort(id);
            }
        } catch (Exception e) {
            System.out.println("exceçao");
        }
        
            
    }
}