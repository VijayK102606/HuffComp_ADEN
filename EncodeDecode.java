/*
 * 
 */

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import myfileio.MyFileIO;

// TODO: Auto-generated Javadoc
/**
 * The Class EncodeDecode. 
 */
public class EncodeDecode {
	
	/** The encodeMap maps each ascii value to its huffman code */
	private String[] encodeMap;
	
	/** Instance of the huffman compression utilites for building the tree and encode man */
	private HuffmanCompressionUtilities huffUtil;
	
	/** Instance of GenWeights used to generate the frequency weights if no weights file is specified */
	private GenWeights gw;
	
	/** Instance of HuffCompAlerts for relaying information to the GUI or console */
	private HuffCompAlerts hca;
	
	/**  Provides facilities to robustly handle external file IO. */
	private MyFileIO fio;
	
	/** The bin util. */
	private BinaryIO binUtil;
	
	/**  The array for storing the frequency weights. */
	private int[] weights;	
	
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

	/**
	 * Instantiates a new EncodeDecode instance
	 *
	 * @param gw - instance of GenWeights
	 * @param hca - instance of HuffCompAlerts
	 */
	public EncodeDecode (GenWeights gw, HuffCompAlerts hca) {
		fio = new MyFileIO();
		this.gw = gw;
		this.hca = hca;
		huffUtil = new HuffmanCompressionUtilities();
		binUtil = new BinaryIO();
	}
	
	/**
	 * Encode. This function will do the following actions:
	 *         1) Error check the inputs
	 * 	       - Perform error checking on the file to encode, using MyFileIO fio.
	 *         - Generate the array of frequency weights - either read from a file in the output/ directory
	 *           or regenerate from the file to encode in the data/ directory
	 *         - Error check the output file...
	 *         Any errors will abort the conversion...
	 *         
	 *         2) set the weights in huffUtils
	 *         3) build the Huffman tree using huffUtils;
	 *         4) create the Huffman codes by traversing the trees.
	 *         5) call executeEncode to perform the conversion.
	 *
	 * @param fName 	the name of the input file to be encoded
	 * @param bfName 	the name of the binary (compressed) file to be created
	 * @param freqWts 	the name of the file to read for the frequency weights. If blank, or other error,
	 *                  generate the frequency weights from fName.
	 * @param optimize 	if true, ONLY add leaf nodes with non-zero weights to the priority queue
	 */
	void encode(String fName,String bfName, String freqWts, boolean optimize) {
		
		File input = fio.getFileHandle(fName);
		int inStatus = fio.checkFileStatus(input, true);
		String in = "INPUT";
		
		File output = fio.getFileHandle(bfName);
		int outStatus = fio.checkFileStatus(output, true);
		String out = "OUPUT";
		
		File freqWeights = fio.getFileHandle(freqWts);
		int wStatus = fio.checkFileStatus(freqWeights, true);
		String wts = "INPUT";
		
		if(inputError(inStatus, in)) {
			return;
		} else if(weightsError(wStatus, wts)) {
			weights = gw.readInputFileAndReturnWeights(fName);
		} else if(outputError(outStatus, out)) {
			return;
		} else if(!weightsError(wStatus, wts)) {
			weights = huffUtil.readFreqWeights(freqWeights);
		}
		
		huffUtil.setWeights(weights);
		huffUtil.buildHuffmanTree(optimize);
		huffUtil.createHuffmanCodes(huffUtil.getTreeRoot(), "", 0);
		executeEncode(input, output);
	}
	
	/**
	 * Execute encode. This function will write compressed binary file as part of part 3
	 * 
	 * This functions should:
	 * 1) get the encodeMap from HuffUtils 
	 * 2) initialize binStr to ""
	 * 3) open a BufferedReader for the text file and a BufferedOutputStream for the binary file
	 * 4) for each character in the textfile:
	 * 	  - append the huffman code to binStr;
	 *    - if binStr length >= 8, write the binStr to the binary file using binUtils.writeBinString();
	 *      binStr should be set to any returned string value.
	 * 5) when the input file is exhausted, write the EOF character, padding with 0's if needed 
	 * 6) close the the input and output files...
	 *
	 * @param inFile the File object that represents the file to be compressed
	 * @param binFile the File object that represents the compressed output file
	 */
	private void executeEncode(File inFile, File binFile) {
		encodeMap = huffUtil.getEncodeMap();
		String binStr = "";
		BufferedReader br = fio.openBufferedReader(inFile);
		BufferedOutputStream bs = fio.openBufferedOutputStream(binFile);
		int c;
		
		try {
			while ((c = br.read()) != -1) {
				binStr += encodeMap[c];	
				
				if(binStr.length() >= 8) {
					binStr = binUtil.writeBinString(bs, binStr);
				}	
			}
			binStr += encodeMap[0];
			
			while(binStr.length() % 8 != 0) {
				binStr += "0";
			}
			binUtil.writeBinString(bs, binStr);
			
			fio.closeFile(br);
			fio.closeStream(bs);
		} catch (Exception e) {
			System.out.println("read/write error");
		}
	}
	
	/**
	 * Input error.
	 *
	 * @param inStatus the in status
	 * @param in the in
	 * @param fName the f name
	 */
	public boolean inputError(int inStatus, String in) {
		if(inStatus == EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.INPUT, in, "empty file name");
			return true;
		} else if(inStatus == NOT_A_FILE) {
			hca.issueAlert(HuffAlerts.INPUT, in, "Not a file");
			return true;
		} else if(inStatus == FILE_DOES_NOT_EXIST) {
			hca.issueAlert(HuffAlerts.INPUT, in, "file does not exist");
			return true;
		} else if(inStatus == READ_ZERO_LENGTH) {
			hca.issueAlert(HuffAlerts.INPUT, in, "empty file");
			return true;
		}
			return false;
	}
	
	/**
	 * Output error.
	 *
	 * @param outStatus the out status
	 * @param out the out
	 * @param bfName the bf name
	 */
	public boolean outputError(int outStatus, String out) {
		if(outStatus == EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.OUTPUT, out, "empty file name");
			return true;
		} else if(outStatus == NOT_A_FILE) {
			hca.issueAlert(HuffAlerts.OUTPUT, out, "Not a file");
			return true;
		} else if(outStatus == FILE_OK) {
			if(hca.issueAlert(HuffAlerts.CONFIRM, out, "Confirm overwriting file or cancel") == false) {
				return true;
			}
		}
			return false;
	}
	
	/**
	 * Weights error.
	 *
	 * @param wStatus the w status
	 * @param wts the wts
	 * @param fName the f name
	 * @return true, if successful
	 */
	public boolean weightsError(int wStatus, String wts) {
		if(wStatus == EMPTY_NAME) {
			hca.issueAlert(HuffAlerts.INPUT, wts, "empty file name, will regenerate weights file");
			return true;
		} else if(wStatus == NOT_A_FILE) {
			hca.issueAlert(HuffAlerts.INPUT, wts, "Not a file, will regenerate weights file");
			return true;
		} else if(wStatus == FILE_DOES_NOT_EXIST) {
			hca.issueAlert(HuffAlerts.INPUT, wts, "file does not exist, will regenerate weights file");
			return true;
		} else if(wStatus == READ_ZERO_LENGTH) {
			hca.issueAlert(HuffAlerts.INPUT, wts, "empty file, will regenerate weights file");
			return true;
		}
			return false;	
	}
	
	// DO NOT CODE THIS METHOD UNTIL EXPLICITLY INSTRUCTED TO DO SO!!!
	/**
	 * Decode. This function will only be addressed in part 5. It will 
	 *         1) Error check the inputs
	 * 	       - Perform error checking on the file to decode
	 *         - Generate the array of frequency weights - this MUST be provided as a file
	 *         - Error check the output file...
	 *         Any errors will abort the conversion...
	 *         
	 *         2) set the weights in huffUtils
	 *         3) build the Huffman tree using huffUtils;
	 *         4) create the Huffman codes by traversing the trees.
	 *         5) executeDecode
	 *
	 * @param bfName 	the name of the binary file to read
	 * @param ofName 	the name of the text file to write...
	 * @param freqWts the freq wts
	 * @param optimize - exclude 0-weight nodes from the tree
	 */
	void decode(String bfName, String ofName, String freqWts, boolean optimize) {
		File input = fio.getFileHandle(bfName);
		int inStatus = fio.checkFileStatus(input, true);
		String in = "INPUT";
		
		File output = fio.getFileHandle(ofName);
		int outStatus = fio.checkFileStatus(output, true);
		String out = "OUPUT";
		
		File freqWeights = fio.getFileHandle(freqWts);
		int wStatus = fio.checkFileStatus(freqWeights, true);
		String wts = "INPUT";
		
		if(inputError(inStatus, in)) {
			return;
		} else if(weightsError(wStatus, wts)) {
			return;
		} else if(outputError(outStatus, out)) {
			return;
		}
		weights = huffUtil.readFreqWeights(freqWeights);
		huffUtil.setWeights(weights);
		huffUtil.buildHuffmanTree(optimize);
		huffUtil.createHuffmanCodes(huffUtil.getTreeRoot(), "", 0);
		
		try {
			executeDecode(input, output);
		} catch (IOException e) {
			System.out.println("decode error");
		}
		hca.issueAlert(HuffAlerts.DONE, in, "succesful decode");
	}
	
	// DO NOT CODE THIS METHOD UNTIL EXPLICITLY INSTRUCTED TO DO SO!!!
	/**
	 * Execute decode.  - This is part of PART 5...
	 * This function performs the decode of the binary(compressed) file.
	 * It will read each byte from the binary file and convert it to a string of 1's and 0's
	 * This will be appended to any leftover bits from prior conversions.
	 * Starting from the head of the string, decode occurs by traversing the Huffman Tree from the root
	 * until a Leaf node is reached. If a leaf node is reached, the character is written to the output
	 * file, and the corresponding # of bits is removed from the string. If the end of the bit string is reached
	 * without reaching a leaf node, the next byte is processed, and so on until the encoded EOF
	 * character is encountered. 
	 * After completely decoding the file, close the input file and
	 * flushed and close the output file.
	 *
	 * @param binFile the file object for the binary input file
	 * @param outFile the file object for the binary output file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void executeDecode(File binFile, File outFile) throws IOException {
		encodeMap = huffUtil.getEncodeMap();
		BufferedInputStream br = fio.openBufferedInputStream(binFile);
		BufferedOutputStream bs = fio.openBufferedOutputStream(outFile);
		String binStr = "";
		int c;
		
		try {
			while((c = br.read()) != -1) {
				binStr += binUtil.convBinToStr(c);
				int d;

				while((d = huffUtil.decodeString(binStr)) != -1) {
					if(d == 0) {
						fio.closeStream(br);
						fio.closeStream(bs);
						return;
					}
					bs.write(d);
					binStr = binStr.substring(encodeMap[d].length());
				}
			}
		} catch(Exception e) {
			System.out.println("decode error");
		}
			fio.closeStream(br);
			fio.closeStream(bs);
	}
}