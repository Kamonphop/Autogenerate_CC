/**
 * A data class for callers. It consists only of setter methods.
 * <p>
 * It allows:
 * <ul>
 * <li>handling arraysort.</li>
 * </ul>
 */
class Caller
{
   ISort callbackref;
   Caller(ISort cbr)
   {
      callbackref = cbr;
   }
   void arraysort()
   {
      callbackref.sort();
   }
}
