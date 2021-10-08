import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ClientReader implements Runnable {
    private int id;
    private Socket s;
    private Table table;
    ReentrantLock lock;
    Condition c;
    
    /**
     * Construtor parametrizado de 'ClientReader'.
     * @param id Id do 'ClientReader'.
     * @param t Socket TCP.
     * @param table Table.
     * @param lock ReentrantLock.
     * @param c Condition.
     */
    public ClientReader(int id, Socket t, Table table, ReentrantLock lock, Condition c){
        this.id = id;
        this.s = t;
        this.table = table;
        this.lock = lock;
        this.c = c;
    }

    /**
     * Método reponsável pela ação de um 'ClientReader'.
     */
    public void  run(){
        BufferedReader in = null; PrintWriter out = null; BufferedOutputStream dataOut = null;
		String fileRequested = null;
        try{
        
      
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));
		
		out = new PrintWriter(s.getOutputStream());
		
		dataOut = new BufferedOutputStream(s.getOutputStream());

       
        String input = in.readLine();
		
        String[] campos = input.split(" ");
        System.out.println(input);
        campos[1]=campos[1].replace("/","");
        System.out.println(campos[1]);

        if(campos[0].equals("GET")){
            this.table.addClient(id, InetAddress.getByName(s.getInetAddress().getHostAddress()));
            InetAddress address = InetAddress.getByName("localhost");
            DatagramSocket s = new DatagramSocket();
            DatagramPacket dp;
            Pacote packet = new Pacote();
            packet.setTag(1);
            packet.setFicheiro(campos[1].getBytes(), campos[1].getBytes().length);
            dp = new DatagramPacket(packet.getData(),packet.getSizeData(),address,8888);
            try{
                lock.lock();
                while(table.getOcuped()){
                    c.await();
                }
                table.setOcuped(true);
            }finally{
                lock.unlock();
            }
            s.send(dp);
            s.receive(dp);
            if(this.table.getConteudo().size() !=0){
                packet = new Pacote(dp.getData());
                if(packet.isUltimo()){
                
                
                SortedSet<Integer> keySet = new TreeSet<>(this.table.getConteudo().keySet());
                for(Integer key : keySet){
                    dataOut.write(this.table.getConteudo().get(key),0,this.table.getConteudo().get(key).length);
                }
                out.print(""); 
                out.flush(); 
                dataOut.flush();  
                }
                this.table.conteudoReset();
                this.table.removeClient(id);
            }
            else{
                String error = "401 - Unreacheable File";
                dataOut.write(error.getBytes(), 0, error.length());
			    dataOut.flush();  
            } 
        }
        else{
            String error = "400 - Bad Request";
            dataOut.write(error.getBytes(), 0, error.length() );
			dataOut.flush(); 
        }
        try{
            lock.lock();
            table.setOcuped(false);
            c.signalAll();
        } finally{
            lock.unlock();
        }
        s.close();
        in.close();
        out.close();
        dataOut.close();
				
    
        } catch (Exception ex) {
            System.out.println("Erro no worker do primario");
            ex.printStackTrace();
        }    
        
    }

}
