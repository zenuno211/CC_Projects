import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
 

public class FastFileServer {
    private DatagramSocket socket;
    private Pacote packet;
    private int bufferSize;
    private InetAddress address;
    private int port;
    private String path;


    /**
     * Construtor de 'FastFileServer'.
     * @param address Address do Gateway.
     * @param p Port do FFS.
     * @throws SocketException
     * @throws IOException
     */
    public FastFileServer(String address, String p) throws SocketException, IOException {
        this.packet = new Pacote();
        this.bufferSize = 1000;
        this.address = InetAddress.getByName(address);
        this.port = Integer.parseInt(p);
        this.socket = new DatagramSocket(Integer.parseInt(p));
    }
    
    /**
     * Main do 'FastFileServer'.
     * @param args args[0] irá conter o Address do Gateway e args[1] irá conter a Port do FFS a criar.
     */
    public static void main(String[] args){
 
        try {
            FastFileServer server = new FastFileServer(args[0],args[1]);
            //server.loadQuotesFromFile("Hello.txt");
            server.service();
        } catch (SocketException ex) {
            System.out.println("Socket error: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }
 
    /**
     * Método responsável pelas ações do 'FastFileServer'.
     * @throws IOException
     */
    private void service() throws IOException {
        Boolean confirmacao = false;
        DatagramPacket request = null;

        System.out.println("Estabelencendo conexão...");
        this.packet.setTag(0);
        System.out.println(this.address);
        request = new DatagramPacket(this.packet.getData(),this.packet.getSizeData(),this.address,8888);
        socket.send(request);

        
        
        DatagramPacket conexStablish = new DatagramPacket(this.packet.getData(), this.packet.getSizeData());
        socket.setSoTimeout(2000);
        while (!confirmacao) {
            try {
                socket.receive(conexStablish);
                confirmacao = true;
                System.out.println("Confirmação de conexão");
                socket.setSoTimeout(0);
            } catch (SocketTimeoutException ste) {
                System.out.println("Sem Resposta...");
                socket.send(request);
            }
        }    
        //Depois ver confirmação de tag
        
        String ficheiro = new String();
        while (true) {    
            
            
            System.out.println("A escuta");
            DatagramPacket serverResponseFile = new DatagramPacket(this.packet.getData(), this.packet.getSizeData());
            socket.receive(serverResponseFile);
            
          

            InetAddress clientAddress = request.getAddress();
            int clientPort = request.getPort();
            
            this.packet = new Pacote(serverResponseFile.getData());
            ficheiro = new String (this.packet.getFicheiro());
            ficheiro = ficheiro.replaceAll("[^\\a-zA-Z0-9:.]+","");

            if(this.packet.getTag()==2){

                
                File file = new File(ficheiro);
                if(!file.canRead()){
                    System.out.println("erro ao ler");
                }
                long tamanho = file.length();
               
                this.packet.setTamanho(tamanho);
            }
            
            else if(this.packet.getTag()==3){
                
                File file = new File(ficheiro);
                int intervalo = this.packet.getIntervalo();
                InputStream in = new FileInputStream(file);
                byte[] array = new byte[bufferSize];
                System.out.println("Intervalo" + intervalo);
                long tamanho = file.length();
                if(tamanho<intervalo+bufferSize){
                    in.skip(intervalo);
                    in.read(array, 0, (int)tamanho-intervalo);
                    this.packet.setConteudo(array, (int)tamanho-intervalo);
                    this.packet.setTamanhoConteudo((int)tamanho-intervalo);   
                    in.close();
                }
                else{
                    in.skip(intervalo);
                    in.read(array, 0, bufferSize);
                    this.packet.setConteudo(array, bufferSize);
                    this.packet.setTamanhoConteudo(bufferSize);              
                    in.close();
                }
            }

            //Resposta de FFS Para Gateway 
            DatagramPacket ffsResponse = new DatagramPacket(this.packet.getData(), this.packet.getSizeData(),clientAddress,clientPort);
            socket.send(ffsResponse);
        }
    }
 
}
