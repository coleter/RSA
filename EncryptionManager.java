import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class EncryptionManager {
    private Key key;

    /*
     * Default constructor, creates new Key
     */
    public EncryptionManager() {
        key = new Key();
    }

    /*
     * Constructor that takes in a Key object
     * 
     * @param k Key object
     */
    public EncryptionManager(Key k) {
        key = k;
    }

    /*
     * Constructor that takes in Key values stored in files
     * Includes n, e, and d so the key can be used for both encoding and decoding
     * 
     * @param n File containing n value
     * @param e File containing e value
     * @param d File containing d value
     */
    public EncryptionManager(File n, File e, File d) {
        key = new Key(n, e, d);
    }

    /*
     * Constructor that takes in Key values stored in files
     * Includes n and e so the key can only be used for encoding
     * 
     * @param n File containing n value
     * @param e File containing e value
     */
    public EncryptionManager(File n, File e) {
        key = new Key(n, e);
    }


    /*
     * Encodes a file using the key
     * 
     * @param f File to encode
     * 
     * @throws FileNotFoundException if the file is not found
     * @throws IOException if an IO error occurs
     * @throws SecurityException if there are insufficient permissions to access or modify files
     */
    public void encode(File f) {
		final int BLOCK_SIZE = 214;
		FileWriter fw = null;
		try {
			// Read the file and store it in a byte array
			byte[] bytes = Files.readAllBytes(f.toPath());

			// Calculate the number of blocks needed
			int numBlocks = (bytes.length / BLOCK_SIZE) + 1;
			BigInteger[] bigInts = new BigInteger[numBlocks];

			// Buffer to hold a single block of 214 bytes
			byte[] thisBlock = new byte[BLOCK_SIZE];

			// Encode each block except the last one
			for (int i = 0; i < numBlocks - 1; i++) {
				System.arraycopy(bytes, i * BLOCK_SIZE, thisBlock, 0, BLOCK_SIZE);
				// Convert the block to a BigInteger and store it
				bigInts[i] = new BigInteger(thisBlock);
			}

			// Encode the last block, which may be smaller than 214 bytes
			int remainingBytes = bytes.length % BLOCK_SIZE;
			byte[] lastBlock = new byte[remainingBytes];
			System.arraycopy(bytes, (numBlocks - 1) * BLOCK_SIZE, lastBlock, 0, remainingBytes);
			// Convert the block to a BigInteger and store it
			bigInts[numBlocks - 1] = new BigInteger(lastBlock);

			// Create a new file to store the ciphertext
			File cipher = new File("cipher.txt");
			if (cipher.exists() && !cipher.delete()) {
				throw new IOException("Failed to delete existing file: " + cipher.getName());
			}
			if (!cipher.createNewFile()) {
				throw new IOException("Failed to create new file: " + cipher.getName());
			}

			// Encrypt each block and write it to the file
			fw = new FileWriter(cipher);
			for (BigInteger bigInt : bigInts) {
				// Encrypt the block using the key (n, e)
				BigInteger encryptedBlock = key.encode(bigInt);
				// Write the encrypted block as a string to the file
				fw.write(encryptedBlock.toString() + "\n");
			}

			System.out.println("cipher.txt file created successfully");

		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + f.getName() + ". " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO error occurred during encode: " + e.getMessage());
		} catch (SecurityException e) {
			System.err.println("Security exception: insufficient permissions to access or modify files.");
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					System.err.println("Failed to close FileWriter: " + e.getMessage());
				}
			}
		}
	}

    /*
     * Encodes a file using another key
     * 
     * @param k Key to encode with
     * @param f File to decode
     */
    public void encode(Key k, File f) {
        EncryptionManager em = new EncryptionManager(k);
        em.encode(f);
    }

    /*
     * Encodes a file using a key stored in files
     * 
     * @param n File containing n value
     * @param e File containing e value
     * @param f File to decode
     */
    public void encode(File n, File e, File f) {
        Key k = new Key(n, e);
        encode(k, f);
    }

    /*
     * Decodes a file using the key
     * 
     * @param f File to decode
     */
    public void decode(File f) {
		FileWriter fw = null;
		if (!key.hasD()) {
			System.out.println("This key does not have a private exponent to decode with.");
			return;
		}
		try {
			// Prepare the output file for the decoded text
			File out = new File("decoded.txt");
			if (out.exists() && !out.delete()) {
				throw new IOException("Failed to delete existing file: " + out.getName());
			}
			if (!out.createNewFile()) {
				throw new IOException("Failed to create new file: " + out.getName());
			}
	
			// List to store the encrypted BigInteger blocks
			ArrayList<BigInteger> bs = new ArrayList<BigInteger>();
			// Read all lines from the cipher text file
			List<String> lines = Files.readAllLines(f.toPath());
	
			// Convert each line (representing an encrypted block) back to a BigInteger
			for (String line : lines) {
				bs.add(new BigInteger(line));
			}
	
			// Decrypt each BigInteger block and write the result to the output file
			fw = new FileWriter(out);
			for (BigInteger b : bs) {
				b = key.decode(b); // Decrypt the block using the private key (d, n)
				byte[] arr = b.toByteArray(); // Convert the decrypted BigInteger back to a byte array
	
				// Write non-null bytes to the output file
				for (byte by : arr) {
					if (by != 0) {  // Ignore null padding
						fw.write(by);
					}
				}
			}
			System.out.println("Decoded file created successfully");
	
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + f.getName() + ". " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO error occurred during decode: " + e.getMessage());
		} catch (SecurityException e) {
			System.err.println("Security exception: insufficient permissions to access or modify files.");
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					System.err.println("Failed to close FileWriter: " + e.getMessage());
				}
			}
		}
	}

    /*
     * Decodes a file using another key
     * 
     * @param k Key to decode with
     * @param f File to decode
     */
    public void decode(Key k, File f) {
        EncryptionManager em = new EncryptionManager(k);
        em.decode(f);
    }

    /*
     * Decodes a file using a key stored in files
     * 
     * @param n File containing n value
     * @param e File containing e value
     * @param d File containing d value
     * @param f File to decode
     */
    public void decode(File n, File e, File d, File f) {
        EncryptionManager em = new EncryptionManager(n, e, d);
        em.decode(f);
    }

}