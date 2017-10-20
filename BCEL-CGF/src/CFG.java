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
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InvokeInstruction;
import org.apache.bcel.generic.ReturnInstruction;

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
		printStream.print(method.getName());
		for(String header: dottyFileHeader){
			printStream.print(header +"\n");
		}

		printStream.println("	entry ->   ");
		for(int i = 0; i<instructionArray.length;i++){
			Integer line = instructionArray[i].getPosition();
			Instruction instr = instructionArray[i].getInstruction();
			line = code.getLineNumberTable().getSourceLine(line);
			String codeLine = sourceCode.get(line);

			if (instr instanceof InvokeInstruction) {

				ConstantPoolGen cpg = new ConstantPoolGen(cls.getConstantPool());
				InvokeInstruction invoke = (InvokeInstruction) instr;
				//				printStream.println("	"+line+" -> "+invoke.getName(cpg)+";");
				printStream.println("	MC: "+codeLine+" -> "+invoke.getName(cpg)+";");
				graph.addNode(codeLine, invoke.getName(cpg));
			}
			if(instr instanceof ReturnInstruction){
				printStream.println("	CF: "+codeLine+" -> exit;");
			}else{
				int line2 = instructionArray[i+1].getPosition();
				line2 = code.getLineNumberTable().getSourceLine(line2);
				String codeLine2 = sourceCode.get(line2);
				if(line != line2 ) {
					//						printStream.println("	"+line+" -> "+line2+";");
					printStream.println("	CF: "+codeLine+" -> "+codeLine2+";");
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

		// Check arguments.
		if ( args.length != 2 ) {
			error.println( "Wrong number of arguments." );
			error.println( "Usage: CFG <input-class-file> <output-dotty-file>" );
			System.exit( 1 );
		}

		//		List<String> inputClassFilenameList = Arrays.asList(args[0].split(File.pathSeparator));
		File folder = new File(args[0]);
		ArrayList<String> inputClassFilenameList = new ArrayList<String>(); 
		ArrayList<String> inputJavaFilenameList = new ArrayList<String>();;
		File[] listOfFiles = folder.listFiles();
		System.out.println(listOfFiles);
		for (int i = 0; i < listOfFiles.length; i++) {
			String name = listOfFiles[i].getName();
			if (name.endsWith(".class")) {
				inputClassFilenameList.add(args[0]+File.separator+name);
			} 
			else if (name.endsWith(".java")) {
				inputJavaFilenameList.add(args[0]+File.separator+name);
			}
		}


		for(int index = 0; index<inputClassFilenameList.size(); index++) {
			// Parse class file.
			String outputDottyFilename = args[1] ;
			System.out.println("Parsing " + inputClassFilenameList.get(index) + "." );
			debug.println( "Parsing " + inputClassFilenameList.get(index)+ "." );
			JavaClass cls = null;
			try {
				cls = (new ClassParser( inputClassFilenameList.get(index) )).parse();
			} catch (IOException e) {
				e.printStackTrace( debug );
				error.println( "Error while parsing " + inputClassFilenameList.get(index) + "." );
				System.exit( 1 );
			}
			outputDottyFilename += cls.getClassName();
			try {
				OutputStream output = new FileOutputStream( outputDottyFilename );
				for ( Method m : cls.getMethods() ) {
					debug.println( "   " + m.getName() );
					createCFG(error, debug, output, m, inputJavaFilenameList.get(index), cls);
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
}