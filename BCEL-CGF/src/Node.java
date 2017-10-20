import java.util.ArrayList;

public class Node {
	String linenum;
	ArrayList<String> children;
	Node parent;
	public Node(String line){
		this.linenum = line;
		children = new ArrayList<String>();
	}
	public Node(Node n){
		this.linenum = n.linenum;
		children = new ArrayList<String>();
		children.addAll(n.children);
	}
	
	public void addChild(String line){
		
		this.children.add(line);
	}
	public int find_childNode(String line){
		for(int i =0;i<children.size();i++){
			if(children.get(i) == line){
				return i;
			}
		}
		return -1;
	}
}
