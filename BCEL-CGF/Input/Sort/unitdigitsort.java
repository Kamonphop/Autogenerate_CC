

/**
 * A {@link ISort} implementation for unitdigitsorts. This data class consists
 * only of setter methods.
 * <p>
 * It allows:
 * <ul>
 * <li>handling sort.</li>
 * </ul>
 */
public class unitdigitsort implements ISort 
{
   private int n;
   private int[] no = new int[15];
   public unitdigitsort(int an , int ano[])
   {
       n = an;
       no=ano;	
   }
   public void sort()
   {
      int temp;
      for(int i = 0 ; i < n ; i++)
      {
         for(int j = i ; j < n ; j++)
         {
              if((no[i]%10)>(no[j]%10))
              {
                  temp = no[j];
                  no[j] = no[i];
                  no[i] = temp;
              }
         }
      }
      for(int k = 0 ; k < n ; k++)
         System.out.println(no[k]);
   }
}
