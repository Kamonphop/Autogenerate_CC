/**
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ArithmeticInstruction;
import org.apache.bcel.generic.ArrayInstruction;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.ConversionInstruction;
import org.apache.bcel.generic.GotoInstruction;
import org.apache.bcel.generic.IfInstruction;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.ReturnInstruction;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.StackInstruction;

public class CFG {
	// Static Dotty file strings.
	protected static final String[] dottyFileHeader = new String[] {
			"{",
	};
	protected static final String[] dottyFileFooter = new String[] {
			"}"
	};
	protected static final String dottyEntryNode = "entry";
	protected static final String dottyExitNode = "exit";


	// Map associating line number with instruction.
	//SortedMap<Integer, InstructionHandle> statements = new TreeMap<Integer, InstructionHandle>();
	InstructionHandle[] instructionArray;
	/**
	 * Loads an instruction list and creates a new CFG.
	 * 
	 * @param instructions Instruction list from the method to create the CFG from.
	 */
	public CFG( InstructionList instructions ) {
		instructionArray = instructions.getInstructionHandles();	
	}

	/**
	 * Generates a Dotty file representing the CFG.
	 * @param graph 
	 * 
	 * @param out OutputStream to write the dotty file to.
	 */
	public void generateDotty( OutputStream _out, Method method, Graph graph, Code code, String javaFilepath, JavaClass cls ) {

		File file = new File(javaFilepath);
		FileReader fileReader;
		ArrayList<String> sourceCode = new ArrayList<String>();
		sourceCode.add("Ignore");
		try {
			fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				sourceCode.add(line.trim());
			}
			fileReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		PrintStream printStream = new PrintStream(_out);
		printStream.print(cls.getClassName() + "." + method.getName());
		for(String header: dottyFileHeader){
			printStream.print(header +"\n");
		}

		printStream.println("	entry ->   ");
		for(int i = 0; i<instructionArray.length;i++){
			Integer line = instructionArray[i].getPosition();
			Instruction instr = instructionArray[i].getInstruction();
			line = code.getLineNumberTable().getSourceLine(line);
			String codeLine = cleanInstruction(sourceCode.get(line));
			
			// Method Call
			if (instr instanceof InvokeInstruction) {

				ConstantPoolGen cpg = new ConstantPoolGen(cls.getConstantPool());
				InvokeInstruction invoke = (InvokeInstruction) instr;
				if(!invoke.getReferenceType(cpg).equals(cls.getClassName())) {
					printStream.println("	MC: "+codeLine+" calls "+invoke.getReferenceType(cpg)+"."+invoke.getName(cpg));
					graph.addNode(codeLine, invoke.getName(cpg));	
				}
				else {
					printStream.println("	MC: "+codeLine+" -> "+invoke.getName(cpg));
					graph.addNode(codeLine, invoke.getName(cpg));
				}
			}
			
			// Arthmetic
			if(instr instanceof ArithmeticInstruction) {
				ConstantPoolGen cpg = new ConstantPoolGen(cls.getConstantPool());
//				System.out.println(codeLine + " -> " + instr.getClass());
				printStream.println("	AI: "+codeLine+" [SubClass: "+instr.getName()+"	Type: "+((ArithmeticInstruction)instr).getType(cpg)+"]");
			}
			if(instr instanceof LocalVariableInstruction) {
				printStream.println("	LS: "+codeLine+" [SubClass: "+((LocalVariableInstruction) instr).getName()+"]");
			}
			// Control Flow
			if(instr instanceof BranchInstruction){
				BranchInstruction br = (BranchInstruction)instr;
				if(br instanceof GotoInstruction){
					InstructionHandle ihs = ((GotoInstruction)br).getTarget();
					int branchline = ihs.getPosition();
					branchline = code.getLineNumberTable().getSourceLine(branchline);
					String branchlineCode = cleanInstruction(sourceCode.get(branchline));
					printStream.println("	CF: "+codeLine+" -> "+branchlineCode+" [label = \""+br.getName()+"\"]");
					graph.addNode(codeLine, branchlineCode);
				}
				//if will have two branches the goto and itself
				else if(br instanceof IfInstruction){
					InstructionHandle nextbr = ((IfInstruction)br).getTarget();
					int branchline = nextbr.getPosition();
					int line2 = instructionArray[i+1].getPosition();
					branchline = code.getLineNumberTable().getSourceLine(branchline);
					String branchlineCode = cleanInstruction(sourceCode.get(branchline));
					line2 = code.getLineNumberTable().getSourceLine(line2);
					String line2Code = cleanInstruction(sourceCode.get(line2));
					printStream.println("	CF: "+codeLine+" -> "+branchlineCode+" [label = \""+br.getName()+"\"]");
					printStream.println("	CF: "+codeLine+" -> "+line2Code+" [label = \"!"+br.getName()+"\"]");
					graph.addNode(codeLine, branchlineCode);
					graph.addNode(codeLine, line2Code);
				}
				else if(br instanceof Select){
					for(InstructionHandle target: ((Select)br).getTargets()){
						int targetLine = target.getPosition();
						targetLine = code.getLineNumberTable().getSourceLine(targetLine);
						String targetLineCode = cleanInstruction(sourceCode.get(targetLine));
						printStream.println("	CF: "+codeLine+" -> "+targetLineCode+" [label = \""+br.getName()+"\"]");
						graph.addNode(codeLine, targetLineCode);
					}
					int nextLine = ((Select)br).getTarget().getPosition();
					nextLine = code.getLineNumberTable().getSourceLine(nextLine);
					String nextLineCode = cleanInstruction(sourceCode.get(nextLine));
					printStream.println("	CF: "+codeLine+" -> "+nextLineCode+" [label = \""+br.getName()+"\"]");
				}
			}
			else if(instr instanceof ReturnInstruction){
				printStream.println("	CF: "+codeLine+" -> exit [Return Type: "+((ReturnInstruction) instr).getType()+"]");
			}else{
				int line2 = instructionArray[i+1].getPosition();
				line2 = code.getLineNumberTable().getSourceLine(line2);
				String codeLine2 = cleanInstruction(sourceCode.get(line2));
				if(line != line2 ) {
					printStream.println("	CF: "+codeLine+" -> "+codeLine2);
					graph.addNode(codeLine, codeLine2);
				}
			}
		}
		for(String footer: dottyFileFooter){
			printStream.print(footer+"\n");
		}

	}

	/**
	 * Main method. Generate a Dotty file with the CFG representing a given class file.
	 * 
	 * @param args Expects two arguments: <input-class-file> <output-dotty-file>
	 */
	public static void main(String[] args) {

		PrintStream error = System.err;
		PrintStream debug = new PrintStream( new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				// TODO Auto-generated method stub

			}} );

		File classFolder = new File(args[0]);
		File javaFolder = new File(args[1]);
		String outputFolder = "";
		if(args.length>2){
			outputFolder = args[2];
		}


		ArrayList<String> inputClassFilenameList = new ArrayList<String>(); 
		ArrayList<String> inputJavaFilenameList = new ArrayList<String>();;
		
		File[] listOfFiles = classFolder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if (name.endsWith(".class")) {
				inputClassFilenameList.add(name);
			}
		}

		listOfFiles = javaFolder.listFiles();
		for (int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if (name.endsWith(".java") && inputClassFilenameList.contains(name.split(".java")[0]+".class")) {
				inputJavaFilenameList.add(name);
			}
		}		

		Collections.sort(inputClassFilenameList);
		Collections.sort(inputJavaFilenameList);

		for(int index = 0; index<inputClassFilenameList.size(); index++) {
			System.out.println("Parsing " + inputClassFilenameList.get(index) + "." );
			debug.println( "Parsing " + inputClassFilenameList.get(index)+ "." );
			JavaClass cls = null;
			try {
				cls = (new ClassParser( args[0]+File.separator+inputClassFilenameList.get(index) )).parse();
			} catch (IOException e) {
				e.printStackTrace( debug );
				error.println( "Error while parsing " + inputClassFilenameList.get(index) + "." );
				System.exit( 1 );
			}
			String outputDottyFilename = cls.getClassName();
			try {
				OutputStream output = new FileOutputStream( outputFolder + outputDottyFilename );
				for ( Method m : cls.getMethods() ) {
					debug.println( "   " + m.getName() );
					createCFG(error, debug, output, m, args[1]+File.separator+inputJavaFilenameList.get(index), cls);
				}
				output.close();
			} catch (IOException e) {
				e.printStackTrace( debug );
				error.println( "Error while writing to " + outputDottyFilename + "." );
				System.exit( 1 );
			}
		}
	}

	private static void createCFG(PrintStream error, PrintStream debug, OutputStream output, Method method, String javaFilepath, JavaClass cls) {
		Code code = method.getCode();
		if (code != null) // Non-abstract method 
		{
			// Create CFG.
			try {
				debug.println( "Creating CFG object for"+ method );
				CFG cfg = new CFG( new InstructionList( method.getCode().getCode()));

				Graph graph = new Graph();
				// Output Dotty file.
				debug.println( "Generating Dotty file." );

				cfg.generateDotty( output, method, graph, code, javaFilepath, cls);
			}catch (NullPointerException e) {
				// TODO: handle exception

			}
		}
	}
	
	private static String cleanInstruction(String codeLine) {
		codeLine = codeLine.replaceAll("[;{}]", "");         //Removes Special characters only
		codeLine = codeLine.trim();
		return codeLine;
	}
}