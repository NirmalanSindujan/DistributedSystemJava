import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Neighbour{
	private String ip;
	private int port;
	private String username;
	private List<Neighbour> nodes = new ArrayList<Neighbour>();
	String[] filenames ;


	public Neighbour(String ip, int port, String username){
		this.ip = ip;
		this.port = port;
		this.username = username;
	}	

	public String getIp(){
		return this.ip;
	}

	public String getUsername(){
		return this.username;
	}

	public int getPort(){
		return this.port;
	}

	public List<Neighbour> getNodes() {
		System.out.println(nodes);
		return nodes;
	}

	public String[] getFilenames() {
		return filenames;
	}

	public void setNodes(Neighbour node) {
		System.out.println(nodes);
		this.nodes.add(node);
	}

	public void setFilenames(String[] filenames) {
		System.out.println("SET FILES FILENAME :" + Arrays.toString(filenames));
		this.filenames = filenames;
	}
}
