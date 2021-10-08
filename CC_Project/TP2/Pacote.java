import java.nio.ByteBuffer;
import java.util.Arrays;

//Pacote: Tag -> IdCliente -> IdPacote ->ultimo -> tamanho do ficheiro -> Conteudo .Um campo que diga a ordem dos pacotes.

//Tag 0-> estabelecer conexao.
// Tag 1 -> tcp para udp.
// Tag 2 -> tamanho ficheiro. 
// Tag 3 -> ler ficheiro.

/**
 * Classe Pacote
 */
public class Pacote {
    private byte[] data;
    
    /**
     * Construtor vazio.
     */
    public Pacote(){
        this.data = new byte[1079]; 
    }

    /**
     * Construtor parametrizado.
     * @param b Array de bytes.
     */
    public Pacote(byte[] b){
        this.data = b;
    }

    /**
     * Método que devolve o array de bytes na sua totalidade.
     * @return Array de bytes.
     */
    public byte[] getData(){
        return this.data;
    }

    /**
     * Método que devolve o tamanho do array de bytes.
     * @return
     */
    public int getSizeData(){
        return this.data.length;
    }

    /**
     * Método responsável por inserir a Tag.
     * @param b Inteiro da Tag.
     */
    public void setTag(int b){
        byte[] x = ByteBuffer.allocate(4).putInt(b).array();
        int i=0;
        for(byte xx : x){
            this.data[i++]=xx;
        }
    }

    /**
     * Método responsável por devolver a Tag.
     * @return Inteiro da Tag.
     */
    public int getTag(){
        return ByteBuffer.wrap(this.data,0,4).getInt();
    }

    /**
     * Método responsável por inserir o TamanhoConteudo.
     * @param p Inteiro do TamanhoConteudo.
     */
    public void setTamanhoConteudo(int p){
        byte[] x = ByteBuffer.allocate(4).putInt(p).array();
        int i=4;
        for(byte xx : x){
            this.data[i++]=xx;
        }
    }

    /**
     * Método responsável por devolver o TamanhoConteudo.
     * @return Inteiro do TamanhoConteudo.
     */
    public int getTamanhoConteudo(){
        return ByteBuffer.wrap(this.data,4,4).getInt();
    }

    /**
     * Método responsável por inserir o Ultimo.
     * @param b Boolean do Ultimo.
     */
    public void setUltimo(boolean b){
        if(b) data[8] = 1; // se for o ultimo
        else data[8] = 0;  // se nao for o ultimo
    }

    /**
     * Método responsável por devolver o Ultimo.
     * @return Boolean do Ultimo.
     */
    public boolean isUltimo(){
        return data[8] == 1;
    }
    
    /**
     * Método responsável por inserir o Tamanho.
     * @param p Long do Tamanho.
     */
    public void setTamanho(long p){
        byte[] x = ByteBuffer.allocate(8).putLong(p).array();
        int i=9;
        for(byte xx : x){
            this.data[i++]=xx;
        }
    }

    /**
     * Método responsável por devolver o Tamanho.
     * @return Long do Tamanho.
     */
    public long getTamanho(){
        return ByteBuffer.wrap(this.data,9,8).getLong();
    }

    /**
     * Método responsável por inserir o Intervalo.
     * @param it Inteiro do Intervalo.
     */
    public void setIntervalo(int it){
        byte[] x = ByteBuffer.allocate(4).putInt(it).array();
        int i=17;
        for(byte xx : x){
            this.data[i++]=xx;
        }
    }

    /**
     * Método responsável por devolver o Intervalo.
     * @return Inteiro do Intervalo.
     */
    public int getIntervalo(){
        return ByteBuffer.wrap(this.data,17,4).getInt();
    }

    /**
     * Método responsável por inserir o ficheiro.
     * @param buffer Array de bytes com o path do ficheiro.
     * @param size Tamanho do array de bytes.
     */
    public void setFicheiro(byte[] buffer, int size){
        int i=21;
        for(int j = 0; j < size; j++){
            this.data[i++] = buffer[j];
        }
    }

    /**
     * Método responsável por devolver o path do ficheiro.
     * @return Array de bytes do path do ficheiro.
     */
    public byte[] getFicheiro(){
        return Arrays.copyOfRange(this.data, 21, 50);
    }

    /**
     * Método responsável por inserir o conteudo do ficheiro.
     * @param buffer Array de bytes com o conteudo do ficheiro.
     * @param size Tamanho do array de bytes.
     */
    public void setConteudo(byte[] buffer, int size){
        if(size == -1){
            return;
        }
        int i = 71;
        int j;
        for(j = 0; j < size; j++){
            this.data[i++] = buffer[j];
        }
    }

    /**
     * Método responsável por devolver o conteudo do pacote.
     * @return Array de bytes com o conteudo.
     */
    public byte[] getConteudo(){
        return Arrays.copyOfRange(this.data, 71, this.getTamanhoConteudo()+71);
        //return Arrays.copyOfRange(this.data, 71, data.length-8);
    }

}



