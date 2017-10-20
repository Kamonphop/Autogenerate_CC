import java.util.ArrayList;

public class Graph {
	ArrayList<Node> graph;
	public Graph(){
		graph = new ArrayList<Node>();
	}
	public void addNode(String startNode, String endNode){
		int start_index = find_node_index(startNode);
		int end_index = find_node_index(endNode);
		//add new node
		if(start_index == -1){
			Node new_node = new Node(startNode);
			new_node.addChild(endNode);
			graph.add(new_node);
		}
		//add end_node to the child node.
		else{
			Node temp = getNode(start_index);
			if(temp.find_childNode(endNode) == -1){
				temp.addChild(endNode);
			}
		}
		if(end_index == -1){
			Node new_node = new Node(endNode);
			graph.add(new_node);
		}
		//graph.add(a);
	}
	public int find_node_index(String line){
		for(int i = 0; i<graph.size(); i++){
			if(graph.get(i).linenum == line){
				return i;
			}
		}
		return -1;
	}
	public Node getNode(int index){
		return graph.get(index);
	}
	public Node getNodeByLine(String line){
		for(int i = 0; i<graph.size(); i++){
			if(graph.get(i).linenum == line){
				return graph.get(i);
			}
		}
		return null;
	}
	public void printGraph(){
		for(int i =0;i<graph.size();i++){
			System.out.print("Node: "+graph.get(i).linenum+" has "+graph.get(i).children.size()+" children: {");
			int size = graph.get(i).children.size();
			for(int j = 0;j<size;j++){
				if(j == size-1){
				System.out.print(graph.get(i).children.get(j));
				}
				else{
					System.out.print(graph.get(i).children.get(j)+",");
				}
			}
			System.out.println("}");
		}
	}
}
