import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;

import myfileio.MyFileIO;

// TODO: Auto-generated Javadoc
/**
 * The Class GenWeights.
 */
public class GenWeights {
	
	/** Constant representing the number of ASCII characters. */
	private final int NUM_ASCII = 128;
	/** Constant representing the start of the printable range of ASCII characters */
    private final int ASCII_PRINT_MIN = 32;    
	/** Constant representing the end of the printable range of ASCII characters */
    private final int ASCII_PRINT_MAX = 126;    
    /** The input and output File handles. */
    private File inf,outf;
    
    /** The array for collecting the weights */
    private int[] weights = new int[NUM_ASCII]; 
    
    /** Instance the the MyFileIO package to access methods for handling files */
    private MyFileIO fio;
    
    /** The ignore chr 13. */
    private boolean ignoreChr13 = false;
    
    /** Instance of the HuffCompAlerts - used as an intermediary between this 
     *  class and the GUI
     */
    private HuffCompAlerts hca;
    
    /** The Constant FILE_OK. */
    public static final int FILE_OK=0;
	
	/** The Constant EMPTY_NAME. */
	public static final int EMPTY_NAME=1;
	
	/** The Constant NOT_A_FILE. */
	public static final int NOT_A_FILE = 2;
	
	/** The Constant READ_EXIST_NOT. */
	public static final int FILE_DOES_NOT_EXIST=3;
	
	/** The Constant READ_ZERO_LENGTH. */
	public static final int READ_ZERO_LENGTH=4;
	
	/** The Constant NO_READ_ACCESS. */
	public static final int NO_READ_ACCESS=5;
	
	/** The Constant NO_WRITE_ACCESS. */
	public static final int NO_WRITE_ACCESS=6;
	
	/** The Constant WRITE_EXISTS. */
	public static final int WRITE_EXISTS=7;
	
	/** The Constant LIMIT. */
	private final static int LIMIT = -1;
	
	/** The Constant SHIFT. */
	private final static int SHIFT = 0;
    
	/**
	 * Instantiates a new GenWeights object and connects it to the supplied
	 * HuffCompAlerts object (hca).
	 *
	 * @param hca the hca
	 */
	public GenWeights(HuffCompAlerts hca) {
		this.hca = hca;
		fio = new MyFileIO();

	}

	/**
	 * Initializes all elements of the weights array to 0
	 */
	void initWeights() {
		for(int i = 0; i < weights.length; i++) {
			weights[i] = 0;
		}
	}

	/**
	 * Generate character-based frequency weights. You will write this method,
	 * using the MyFileIO fio instance to create the File object, check, open 
	 * and close the file.
	 * 
	 * NOTE: All source text files are located in the data/ directory. You do NOT need to 
	 *       prepend "data/" to the infName - the GUI handles this for you.
	 * 
	 * You should check the file for the following issues (using the getFileStatus method
	 * in MyFileIO) and take the appropriate action if they occur.
	 * 
	 * a) if the filename is empty, raise a WARNING alert with an appropriate message and
	 *    return.
	 * b) if the file does not exist or is empty, raise a WARNING alert with an appropriate 
	 *    message and return.
	 * c) if the file exists and is not empty, but is not readable, raise a WARNING alert with 
	 *    an appropriate message and return.
	 * 
	 * Each of these errors should be differentiated and reported to the user via an alert 
	 * 
	 * Assuming that the requirements of a, b and c have all been met successfully, 
	 * initialize the weights and read the file character by character, incrementing 
	 * weights[character] by one each time. Remember to account for the EOF character 
	 * (ordinal value 0) - this will never be read, but is present at the end of each 
	 * file, so you must increment weights[0] manually (or set to 1).
	 *  
	 * Refer to the HuffAlerts and HuffCompAlerts Classes to understand the alert types
	 * 
	 * Once the input file has been fully processed, you should print the weights to the console.
	 *
	 * @param infName - the name of the text file to read
	 */
	void generateWeights(String infName) {
		fio = new MyFileIO();
		inf = fio.getFileHandle(infName);
		int status = fio.checkFileStatus(inf, true);
		String input = "INPUT";
		

		if(status == EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.INPUT, input, "empty file name");
			return;
		} else if(status == FILE_DOES_NOT_EXIST || status == READ_ZERO_LENGTH) {
			hca.issueAlert(HuffAlerts.INPUT, input, "file does not exist");
			return;
		} else if(status == NO_READ_ACCESS) {
			hca.issueAlert(HuffAlerts.INPUT, input, "file exists but is not readable");
			return;
		}
		
		initWeights();
		readFiles();
		printWeights();
	}
	
	
	/**
	 * Read files.
	 */
	private void readFiles() {
		BufferedReader reader = fio.openBufferedReader(inf);
		int c = 0;
		
		try {
			while ((c = reader.read()) != LIMIT) {
				weights[c]++;
			}
			weights[SHIFT]++;
			fio.closeFile(reader);
		} catch(Exception e) {
			System.out.println("Read Error");
		}
	}
	
	/**
	 * Prints the weights to the console. Non-printing characters (0-31, 127) 
	 * are indicated with [ ], printing characters are displayed to help with debug
	 * 
	 */
	void printWeights() {
		for (int i = 0; i < weights.length; i++) {
			if ((i < ASCII_PRINT_MIN) || (i > ASCII_PRINT_MAX))  
				System.out.println("i:"+i+" [ ] = "+weights[i]);
			else 
				System.out.println("i:"+i+" ("+(char)i+") = "+weights[i]);		
		}
	}
	
	/**
	 * Write the character-based frequency data to the specified file, one index per line.
	 * Use the following format:
	 *   print the index and the frequency count separated by a comma.
	 *   ie, if weights[10]=421, this would write the following line:
	 *   10,421,
	 *   DO NOT PRINT THE ACTUAL CHARACTER - as this will cause problems when you
	 *   try to analyze the data with your favorite spreadsheet (YFSS)
	 *   
	 * Again, you will use the MyFileIO methods to create the File object, check, open and close
	 * the file. 
	 * 
	 * NOTE: all weights file will be written to the weights/ directory. outfName will already account 
	 * for this - you do NOT need to prepend "weights/" to outfName.
	 *   
	 * Assuming that there are no errors, raise an INFORMATION alert with an appropriate message
	 * to indicate to the user that the output file was created successfully. Make sure to refer to 
	 * the notes from class today for any more details. Again, the actual writing of the file might be
	 * best done in a separate helper method.
	 *   
	 * You must handle the following error conditions and take the appropriate actions,
	 * as in the generateWeights() method:
	 *   if outfName is blank, raise a OUTPUT alert with an appropriate message and return.
	 *   if the output file exists but is not writeable, raise an OUTPUT alert with an appropriate message
	 *   and return.
	 *   if the output file exists and is writeable, raise a CONFIRM alert with an appropriate
	 *   message to the user. if they cancel the operation, return; otherwise, continue.
	 *   otherwise, if there is no error (status == MyFileIO.FILE_OK), continue.
	 *   
	 * Refer to the HuffAlerts and HuffCompAlerts Classes to understand the alert types
	 * 
	 * @param outfName the name of the weights file (includes weights/ )
	 */
	 void saveWeightsToFile(String outfName) {
		 	fio = new MyFileIO();
			outf = fio.getFileHandle(outfName);
			int status = fio.checkFileStatus(outf, false);		
			String output = "OUTPUT";

			if(status == EMPTY_NAME || status == NOT_A_FILE) {
				hca.issueAlert(HuffAlerts.OUTPUT, output, "empty file name");
				System.out.println("empty name");
				return;
			}  else if(status == NO_WRITE_ACCESS) {
				hca.issueAlert(HuffAlerts.OUTPUT, output, "file exists but is not writeable");
				return;
			} else if(status == WRITE_EXISTS) {
				if(hca.issueAlert(HuffAlerts.CONFIRM, output, "Confirm overwriting file or cancel") == false) {
					return;
				}
			}
			writeFiles();	
			hca.issueAlert(HuffAlerts.DONE, "OUTPUT", "write was succesful");
	}
	 
	 /**
 	 * Write files.
 	 */
 	private void writeFiles() {
		 BufferedWriter writer = fio.openBufferedWriter(outf);
			try {
				for(int i = 0; i < weights.length; i++) {
					writer.write(i + "," + weights[i] + ",\n");
				}
				fio.closeFile(writer);
			} catch(Exception e) {
				System.out.println("caught exception");
			}
	}

	
	/**
	 * Read input file and return weights
	 * This file forces the creation of the weights for JUnit testing
	 * and during file encoding if the weights file does not exist.
	 * DO NOT CHANGE THIS METHOD
	 *
	 * @param infName the name of the text file to read
	 * @return the weights array
	 */
	int[] readInputFileAndReturnWeights(String infName) {
		System.out.println("Generating weights for: "+infName);
		generateWeights(infName);
		return weights;
	}	
}
