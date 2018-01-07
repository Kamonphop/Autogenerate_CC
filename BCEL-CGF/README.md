#BCEL

Compile BCEL

Go into **BCEL-CGF/src/** folder and run 
```
 javac -cp bcel-5.2.jar *.java
```

Running BCEL

```
java -cp .:bcel-5.2.jar CFG ../Input/CoffeeMaker_Web/build/classes/edu/ncsu/csc326/coffeemaker/ ../Input/CoffeeMaker_Web/src/edu/ncsu/csc326/coffeemaker/ ../Output/CoffeeMaker_CFG/
```

Where 
1. **../Input/CoffeeMaker_Web/build/classes/edu/ncsu/csc326/coffeemaker/** is the path to the input class files. 
2. **../Input/CoffeeMaker_Web/src/edu/ncsu/csc326/coffeemaker/** is the path to corresponding Java files. This folder contains .java and .class files of CoffeeMaker
2. **../Output/CoffeeMaker_CFG/** is the folder where the output files are written


Understanding the output File

1. CF - Control Flow for instructions
2. LS - Load/Store Instruction
3. MC - Invoke Instruction
4. AI - Arithmetic Instruction

Example

The following method **addInventory** of class **edu.ncsu.csc326.coffeemaker.CoffeeMaker**

```
public synchronized void addInventory(String amtCoffee, String amtMilk, String amtSugar, String amtChocolate) throws InventoryException {
    inventory.addCoffee(amtCoffee);
    inventory.addMilk(amtMilk);
    inventory.addSugar(amtSugar);
    inventory.addChocolate(amtChocolate);
}
```

is converted to the following

```
edu.ncsu.csc326.coffeemaker.CoffeeMaker.addInventory{
	entry ->   
	LS: inventory.addCoffee(amtCoffee) [SubClass: aload_1]
	MC: inventory.addCoffee(amtCoffee) calls edu.ncsu.csc326.coffeemaker.Inventory.addCoffee
	CF: inventory.addCoffee(amtCoffee) -> inventory.addMilk(amtMilk)
	LS: inventory.addMilk(amtMilk) [SubClass: aload_2]
	MC: inventory.addMilk(amtMilk) calls edu.ncsu.csc326.coffeemaker.Inventory.addMilk
	CF: inventory.addMilk(amtMilk) -> inventory.addSugar(amtSugar)
	LS: inventory.addSugar(amtSugar) [SubClass: aload_3]
	MC: inventory.addSugar(amtSugar) calls edu.ncsu.csc326.coffeemaker.Inventory.addSugar
	CF: inventory.addSugar(amtSugar) -> inventory.addChocolate(amtChocolate)
	LS: inventory.addChocolate(amtChocolate) [SubClass: aload]
	MC: inventory.addChocolate(amtChocolate) calls edu.ncsu.csc326.coffeemaker.Inventory.addChocolate
	CF: inventory.addChocolate(amtChocolate) -> 
	CF:  -> exit [Return Type: void]
}
```

Control Flow

```
entry ->   
CF: inventory.addCoffee(amtCoffee) -> inventory.addMilk(amtMilk)
CF: inventory.addMilk(amtMilk) -> inventory.addSugar(amtSugar)
CF: inventory.addSugar(amtSugar) -> inventory.addChocolate(amtChocolate)
CF: inventory.addChocolate(amtChocolate) -> 
CF:  -> exit [Return Type: void]
```
Load/Store Instructions

```
LS: inventory.addCoffee(amtCoffee) [SubClass: aload_1]
LS: inventory.addMilk(amtMilk) [SubClass: aload_2]
LS: inventory.addSugar(amtSugar) [SubClass: aload_3]
LS: inventory.addChocolate(amtChocolate) [SubClass: aload]
```
Invoke Instructions/Machine Calls

```
MC: inventory.addCoffee(amtCoffee) calls edu.ncsu.csc326.coffeemaker.Inventory.addCoffee
MC: inventory.addMilk(amtMilk) calls edu.ncsu.csc326.coffeemaker.Inventory.addMilk
MC: inventory.addSugar(amtSugar) calls edu.ncsu.csc326.coffeemaker.Inventory.addSugar
MC: inventory.addChocolate(amtChocolate) calls edu.ncsu.csc326.coffeemaker.Inventory.addChocolate
```
Arithmetic Instruction

This method doesn't contain and arithmetic instruction. But if it did contain an instruction like

```change = amtPaid - getRecipes()[recipeToPurchase].getPrice();```

then the output will contain

```AI: change = amtPaid - getRecipes()[recipeToPurchase].getPrice() [SubClass: isub	Type: int]```

which indicates integer subtraction

