//import java.util.Arrays;

public class SearchAlgo {
//	boolean explored[];
//	//List<Integer> searchpath;
//	
//	public SearchAlgo(int maxLine){
//		explored = new boolean[maxLine+1];
//		Arrays.fill(explored, false);
//		//searchpath = new ArrayList<Integer>();
//	}
//	public void reset(){
//		Arrays.fill(explored, false);
//	}
//	
//	public void DFS(Graph graph, String source, int destination,List<Integer> path){
//		int source_index = graph.find_node_index(source);
//		//System.out.println(source_index);
//		if(source_index == -1){
//			//explored[source] = false;
//			return;
//		}
//		//mark state as visited
//		//System.out.println("Exploring: "+source);
//		explored[source] = true;
//		//System.out.println(source);
//		path.add(source);
//		//normal goal test
//		if(source == destination && path.size() > 1){
//			//System.out.println(destination);
//			System.out.println("Goal Found");
//			for(int i = 0;i<path.size();i++){
//				if(i == path.size()-1){
//					System.out.print(path.get(i));
//				}
//				else{
//					System.out.print(path.get(i)+" -> ");
//				}
//			}
//			System.out.println();
//			return;
//		}
//		else{
//			ArrayList<Integer> children = graph.getNode(source_index).children;
//			for(int i = 0;i<children.size(); i++){
//				//if child state is not visited
//				if(explored[children.get(i)] == false){
//					DFS(graph,children.get(i),destination,path);
//				}
//				else{
//					if(children.get(i) == destination){
//						path.add(destination);
//						System.out.println("Goal Found");
//						for(int k = 0;k<path.size();k++){
//							if(k == path.size()-1){
//								System.out.print(path.get(k));
//							}
//							else{
//								System.out.print(path.get(k)+" -> ");
//							}
//						}
//						System.out.println();
//						return;
//					}
//				}
//			}
//		}
//		path.remove(path.size()-1);
//		//explored[source] = false;
//	}
//	
//	public void checkLoop(){
//		
//	}
}