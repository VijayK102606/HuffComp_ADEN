import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * The Class BinaryIO.
 */
public class BinaryIO {
	
	private static final int maxBitLen = 8;
	private static final int mult = 2;
	/**
	 * Instantiates a new binary IO.
	 */
	public BinaryIO() {
	}
	
	/**
	 * Converts a string of eight 1's and 0's to one byte. 
	 * The caller MUST guarantee that the string of 1's 
	 * and 0's is 8 bits - no more, no less.
	 *
	 * @param binStr the incoming binary string for the current character
	 * @return the int generated from the binary string
	 */
	int convStrToBin(String binStr) { 
		int num = 0;
		int factor  = 1;
		int numBinaryCharacters = 0;
		int index = binStr.length() - 1;
		
		for(; index >= 0; index--) {
			if (binStr.charAt(index) == '1') {
				num += factor;
				factor = factor * mult;
				numBinaryCharacters++;
			} else if(binStr.charAt(index) == '_') {
				continue;
			} else if(binStr.charAt(index) == '0') {
				factor = factor * mult;
				numBinaryCharacters++;
			} else {
				throw new NumberFormatException("wrong length");
			}
			if(numBinaryCharacters > maxBitLen) {
				throw new NumberFormatException("wrong length");
			}
		}
		return num;
	}
	
	/**
	 * Convert a byte value into a string of eight 1's and 0's (MSB to LSB).
	 *
	 * @param aByte the byte to convert
	 * @return the binary string of 1's and 0's that represents aByte
	 */
	String convBinToStr(int aByte) {
		String convertIntToBinary = "";
		int initVal = aByte;
		int index = 0;
		
		if (aByte < 0) {
			aByte += 1;
		}
		
		for(;aByte != 0; aByte = (byte) (aByte/mult)) {
			int remainder = aByte % mult;
			convertIntToBinary = Math.abs(remainder) + convertIntToBinary;
		}
		for(int charsLeft = maxBitLen - convertIntToBinary.length(), i = 0; i < charsLeft; i++) {
			convertIntToBinary = "0" + convertIntToBinary;
		}
		
		if (initVal < 0) {
			for (;index < convertIntToBinary.length(); index++) {
				String front = convertIntToBinary.substring(0, index);
				String tail = convertIntToBinary.substring(index + 1, convertIntToBinary.length());
				if (convertIntToBinary.charAt(index) != '0') {
					convertIntToBinary = front + "0" + tail;
				} else {
					convertIntToBinary = front + "1" + tail;
				}
			}
		}
		return convertIntToBinary;
	}
	
	/**
	 * WriteBinStr - this method attempts to convert a binary string 
	 *               to one or more bytes, and write them to the binary
	 *               file specified. Any remaining unwritten bits in the
	 *               binary string are returned to the caller.
	 * Algorithm:	While the binary string has *more* than 8 bits
	 *                 - convert the first 8 bits to a byte value
	 *                 - write the converted value to the file
	 *                 - remove the first 8 bits from the binary Str
	 *                 
	 *              If the binary string has 8 bits
	 *                 - convert the string to a byte value
	 *                 - write the converted value to the file
	 *                 - return "";
	 *              else 
	 *                 - return the binary string
	 *
	 * @param bos - the binary file to be created
	 * @param binStr - the binary string of 1's and 0's to be written to the file
	 * @return the string of any unwritten bits...
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	 String writeBinString(BufferedOutputStream bos, String binStr) throws IOException {
		 
		 while(binStr.length() > maxBitLen) {
			 int byteVal = convStrToBin(binStr.substring(0, maxBitLen));
			 bos.write(byteVal);
			 binStr = binStr.substring(maxBitLen);
		 }
		 
		 if(binStr.length() == maxBitLen) {
			 int byteVal = convStrToBin(binStr);
			 bos.write(byteVal);
			 return "";
		 } else {
			 return binStr;
		 }
	 }
	
}

