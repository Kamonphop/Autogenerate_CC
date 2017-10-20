import java.util.Scanner;

/**
 * A boundary class for clients. It communicates with the classes
 * {@link Scanner}, {@link Caller}, and unitdigitsort}.
 */
public class Client
{
   public static void main(String args[])
   {
      Scanner in = getScanner();
      int sizeOfArray = getSizeOfArray(in);
      int array[]=new int[sizeOfArray];
      
      getElementsOfArray(in, sizeOfArray, array);
      
     
      Caller caller = new Caller(new unitdigitsort(sizeOfArray ,array));
      caller.arraysort();
      
      in.close();
   }

	private static void getElementsOfArray(Scanner in, int sizeOfArray, int[] array) {
		System.out.println ("enter the elements of the array");
	    for(int i=0;i<sizeOfArray;i++)
	    {
	    	array[i]=in.nextInt();
	    }
	}
	
	private static int getSizeOfArray(Scanner in) {
		
		System.out.println ("Enter the no of elements of the array");
		int sizeOfArray = in.nextInt();
		return sizeOfArray;
	}

	private static Scanner getScanner() {
		return new Scanner(System.in);
	}
}
