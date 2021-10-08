import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Table {
    private ConcurrentHashMap<Integer, InetAddress> clientesTCP; 
    private ConcurrentHashMap<Integer, InetAddress> fastFileServer;
    private ConcurrentHashMap<Integer, Integer> fastFileServerPort;
    private ConcurrentHashMap<Integer, byte[]> conteudo;
    private boolean ocupedffs;
    private ReentrantLock lock;

    /**
     * Construtor vazio
     */
    public Table(){
        this.clientesTCP = new ConcurrentHashMap<>();
        this.fastFileServer = new ConcurrentHashMap<>();
        this.fastFileServerPort = new ConcurrentHashMap<>();
        this.conteudo = new ConcurrentHashMap<>();
    }

    /**
     * Método que limpa o conteudo armazeado.
     */
    public void conteudoReset(){
        this.conteudo = new ConcurrentHashMap<>();
    }

    /**
     * Método responsável por devolver o número de FastFileServers.
     * @return Inteiro.
     */
    public int getFFSSize(){
        return this.fastFileServer.size();
    }

    /**
     * Método responsável por devolver o Address de um cliente.
     * @param client Inteiro identificador do cliente.
     * @return Address.
     */
    public InetAddress getTagged(int client){
        return this.clientesTCP.get(client);
    }
    
    /**
     * Método responsável por inserir um novo cliente.
     * @param id Id do Cliente.
     * @param t Address do Cliente.
     */
    public void addClient(int id, InetAddress t){
        this.clientesTCP.put(id,t);
    }

    /**
     * Método responsável por remover um Cliente.
     * @param id Inteiro identificador do cliente. 
     */
    public void removeClient(int id){
        this.clientesTCP.remove(id);
    }
    
    /**
     * Método responsável por devolver o adress de um FastFileServer.
     * @param id Inteiro identificador.
     * @return Address.
     */
    public InetAddress getInet(int id){
       return this.fastFileServer.get(id);
    }
    
    /**
     * Método responsável por adicionar um FastFileServer.
     * @param id Inteiro identificador do FFS.
     * @param i Address do FFS.
     */
    public void addFFS(int id , InetAddress i){
        this.fastFileServer.put(id,i);
    }

    /**
     * Método responsável por remover um FastFileServer.
     * @param id Inteiro identificador do FFS.
     */
    public void removeFFS(Integer id){
        this.fastFileServer.remove(id);
    }

    /**
     * Método responsável por devolver o estado de ocupação dos FFS.
     * @return Boolean.
     */
    public Boolean getOcuped(){
        return this.ocupedffs;
    }

    /**
     * Método responsável por alterar o estado de ocupação dos FFS.
     * @param b Boolean.
     */
    public void setOcuped(boolean b){
        ocupedffs=b;
    }

    /**
     * Método responsável por adicionar conteudo de um intervalo.
     * @param id Identificador do intervalo de bytes.
     * @param b Array de bytes.
     */
    public void addConteudo(int id, byte[] b)  {
        this.conteudo.put(id,  b);
    }

    /**
     * Método responsável por devolver o Address do primeiro FFS.
     * @return Address.
     */
    public InetAddress getFirstFFs() {
        Object[] array =  this.fastFileServer.values().toArray();
        System.out.println(array[0]);
        return (InetAddress) array[0];
    }

    /**
     * Método responsável por devolver a Port do primeiro FFS.
     * @return Inteiro da Port.
     */
    public Integer getFirstFFsPort() {
        Object[] array =  this.fastFileServerPort.values().toArray();
        System.out.println(array[0]);
        return (Integer) array[0];
    }

    /**
     * Método responsável por devolver o Map do conteudo armazenado.
     * @return Map.
     */
    public ConcurrentHashMap<Integer, byte[]> getConteudo(){
        return this.conteudo;
    }

    /**
     * Método responsável por devolver o Map dos Address dos FFS.
     * @return Map.
     */
    public ConcurrentHashMap<Integer, InetAddress> getFFS(){
        return this.fastFileServer;
    }

    /**
     * Método responsável por devolver o Map das Ports dos FFS.
     * @return
     */
    public ConcurrentHashMap<Integer, Integer> getFFSPort(){
        return this.fastFileServerPort;
    }

    /**
     * Método responsável por verificar se um Address está presente no map dos FFS.
     * @param address Address.
     * @return Boolean.
     */
    public Boolean containsAddress(InetAddress address){
        return this.fastFileServer.contains(address);
    }

    /**
     * Método responsável por inserir uma Port nova no Map de Ports.
     * @param id Inteiro identificador do FFS.
     * @param t Port do FFS.
     */
    public void addPort(int id, int t){
        this.fastFileServerPort.put(id,t);
    }

    /**
     * Método responsável por remover uma Port do Map de Ports.
     * @param port Identificador da Port do FFS.
     */
    public void removePort(Integer port){
        this.fastFileServerPort.remove(port);
    }
    
}

