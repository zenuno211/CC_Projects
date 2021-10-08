import java.io.*;
import java.net.*;
import java.io.EOFException;
import java.io. IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.net.DatagramSocket;
import java.net.DatagramPacket;

/**
 * Classe 'TCPWorker'
 */
class TCPWorker implements Runnable{
    ServerSocket serverSocket;
    Table table;
    ReentrantLock lock;
    Condition c;

    /**
     * Construtor parametrizado de 'TCPWorker'.
     * @param port Port do Gateway TCP.
     * @param t Table.
     * @param lock ReentrantLock.
     * @param c Condition.
     * @throws IOException
     */
    public TCPWorker(int port,Table t,ReentrantLock lock,Condition c) throws IOException{
        this.serverSocket = new ServerSocket(port);
        this.table = t;
        this.lock = lock;
        this.c = c;
    }

    /**
     * Método responsável pela ação de um TCPWorker
     */
    public void run(){
        while (true) {
            int id=0;
            try{
                Socket socketTCP = serverSocket.accept();
                System.out.println("novo cliente " + id);
                Thread treader = new Thread(new ClientReader(id,socketTCP,table,lock,c));
                treader.start();
                id++;
            } catch (IOException  ignored){}
        }
    }
}


/**
 * Classe 'UDPWorker'
 */
class UDPWorker implements Runnable{
    DatagramSocket socket = new DatagramSocket(8888);
    ReentrantLock lock;
    Condition c;
    InetAddress address;
    int port;
    Table table;

    /**
     * Construtor parametrizado do 'UDPWorker'.
     * @param address Address do Gateway.
     * @param port Port do Gateway UDP.
     * @param table Table.
     * @param lock ReentrantLock.
     * @param c Condition.
     * @throws IOException
     */
    public UDPWorker(String address, int port,Table table,ReentrantLock lock,Condition c) throws IOException{
        this.address = InetAddress.getByName(address);
        this.port = port;
        this.table = table;
        this.lock = lock;
        this.c = c;
    }

    /**
     * Método responsável pela ação de um 'UDPWorker'
     */
    public void run(){
        try{

            byte[] buffer = new byte[1079];
            int idFFS=0;

            while(true){
                System.out.println("À escuta");
                DatagramPacket request = new DatagramPacket(buffer, 1029);
                socket.receive(request); //espera aqui
                //pacote recebido do FFS a iniciar sessão ou do CLientReader
                Pacote p = new Pacote(request.getData());
                InetAddress address = request.getAddress(); //endereço de qq cena que recebe por UDP (FFS ou clientReader)
                int port = request.getPort(); //Porta por onde recebe (FFS ou ClienteReader)
                
                if (p.getTag() == 0) {
                    //Conexão de um FFFS
                    System.out.println("Confirmação de conexão FFS");
                    //System.out.println("Endereço" + address);
                    //System.out.println("porta" + port);
                    DatagramPacket beginConexion = new DatagramPacket(p.getData(), p.getSizeData(), address, port);
                    socket.send(beginConexion);

                    //adiciona à table o novo ffs
                    if(!table.containsAddress(address)){ 
                    table.addFFS(idFFS, address);
                    table.addPort(idFFS,port);
                    idFFS++;
                    } 
                }
                if (p.getTag() == 1) {
                    //vai buscar o primeiro fastfileServer para perguntar se tem o ficheiro
                    for (Map.Entry<Integer, InetAddress> entry : this.table.getFFS().entrySet()){
                        Thread connectionWorker = new Thread(new ConnectionWorker(this.table,socket,entry.getValue(),this.table.getFFSPort().get(entry.getKey()),entry.getKey()));
                        connectionWorker.start();
                        connectionWorker.join();
                    }
                    if(table.getFFSSize()>0){
                    InetAddress ffsAddress = table.getFirstFFs();
                    Integer ffsPort = table.getFirstFFsPort();
                    p.setTag(2);
                    //Pergunta ao FFS se tem o ficheiro pretendido
                    Boolean recebi = false;
                    int tentativas =0;
                    DatagramPacket getTamanho = null;
                    getTamanho = new DatagramPacket(p.getData(), p.getSizeData(), ffsAddress, ffsPort);
                    socket.send(getTamanho);
                    //recebe a resposta
                    socket.setSoTimeout(200);
                    while (!recebi && tentativas<5) {
                        try {
                            socket.receive(getTamanho);
                            recebi = true;
                          
                            socket.setSoTimeout(0);
                        } catch (SocketTimeoutException ste) {
                            System.out.println("Sem Ack...");
                            if(tentativas<5){
                                socket.send(getTamanho);
                            }   
                            tentativas++;
                        }
                    }
                    socket.setSoTimeout(0);
                    p = new Pacote(getTamanho.getData());
                    //se o tamanho for maior que 0 pede ao FFS o ficheiro
                    if(p.getTamanho()!= 0){
                        long n = p.getTamanho() / (1000*table.getFFSSize()) + 1;
                        if(p.getTamanho()%(1000*table.getFFSSize()) == 0){
                            n--;
                        }
                        Thread [] list = new Thread [table.getFFSSize()*(int) n];
                        Thread [] listaInterna = new Thread [table.getFFSSize()];
                        int i = 0;
                        int it=0;
                        for(;n>0;n--){
                          
                            i=0;
                            for (Map.Entry<Integer, InetAddress> entry : this.table.getFFS().entrySet()){
                                Thread ffsworker = new Thread(new FFSWorker(this.table,p.getFicheiro(),socket,entry.getValue(), this.table.getFFSPort().get(entry.getKey()),it,p.getTamanho()));
                                ffsworker.start();
                                list[it] = ffsworker;
                                it++;
                                listaInterna[i] = ffsworker;
                                i++;
                            }
                            // Espera pelas threads todas criadas no ciclo for anterior
                            for(int j = 0;j < listaInterna.length ;j++){
                                try {
                                    listaInterna[j].join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }                                                 
                        }
                        // Espera por todas as threads criadas pelo n
                        for(int j = 0;j < list.length ;j++){
                            try {
                                list[j].join();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } 
                    }
                }
                //envia ao 'ClientReader' um pacote a informar que a tabela está preenchida e pronta a enviar de volta ao Cliente por TCP.
                p.setUltimo(true);
                DatagramPacket pedidoAFFS = new DatagramPacket(p.getData(), p.getSizeData(), address, port); // resposta ao ClienteReader
                socket.send(pedidoAFFS);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }   
    }
}

/**
 * Classe Gateway
 */
public class Gateway{ 
    /**
     * Método do Gateway
     * @param args 
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        Table table = new Table();
        ReentrantLock lock = new ReentrantLock();
        Condition c = lock.newCondition(); 
        Thread TCPWorker = new Thread(new TCPWorker(8080,table,lock,c));
        Thread UDPWorker = new Thread(new UDPWorker("localhost",8888,table,lock,c));
        TCPWorker.start();
        UDPWorker.start();
    }
}