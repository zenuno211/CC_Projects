import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class FFSWorker implements Runnable {
    Table table;
    byte[] f;
    DatagramSocket socket;
    InetAddress ffsAddress;
    int ffsPort;
    int i;
    long tamanho;

    /**
     * Construtor do parametrizado do 'FFSWorker'.
     * @param t Table.
     * @param f Array de bytes que contem o path do ficheiro a pedir.
     * @param s Socket UDP.
     * @param ffsAddress Address do FFS.
     * @param ffsPort Port do FFS.
     * @param i Intervalo a pedir ao FFS.
     * @param tamanho Tamanho do ficheiro.
     */
    public FFSWorker(Table t, byte[] f, DatagramSocket s,InetAddress ffsAddress, int ffsPort, int i,long tamanho) {
        this.table = t;
        this.f = f;
        this.socket = s;
        this.ffsAddress = ffsAddress;
        this.ffsPort = ffsPort;
        this.i=i;
        this.tamanho = tamanho;
    }

    /**
     * Método responsável pela ação de um 'FFSWorker'
     */
    public void run(){

        Pacote p = new Pacote();
        p.setTag(3);
        p.setFicheiro(f, f.length);
        p.setIntervalo(i*1000);
        p.setTamanho(tamanho);
        try {
            
            Boolean recebi = false;
            int tentativas =0;
            
            
            if(tamanho>(i*1000)){
                DatagramPacket getConteudo = new DatagramPacket(p.getData(), p.getSizeData(), this.ffsAddress, this.ffsPort);
                socket.send(getConteudo);
                
            
                //recebe a resposta
                socket.setSoTimeout(200);
                while (!recebi && tentativas<5){
                    try {
                        socket.receive(getConteudo);
                        p = new Pacote(getConteudo.getData());
                        this.table.addConteudo(p.getIntervalo(), p.getConteudo());
                        recebi = true;
                        
                        socket.setSoTimeout(0);
                    } catch(SocketTimeoutException ste) {
                        System.out.println("Sem Resposta300");
                        if(tentativas<5){
                            socket.send(getConteudo);
                        }
                        tentativas++;
                    }
                }
                socket.setSoTimeout(0);
            }

        } catch (Exception e) {
            System.out.println("exceçao");
        }    
    }
}