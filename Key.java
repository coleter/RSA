import java.io.*;
import java.math.*;
import java.util.*;
import java.security.SecureRandom;

/**
 * The Key class is used to represent the cryptographic keys for RSA encryption
 * and decryption.
 * It manages the generation of public and private keys based on prime number
 * calculations.
 */
public class Key {
	// The modulus for both the public and private keys, product of two primes (p *
	// q)
	public BigInteger n;

	// The public exponent used for encryption, relatively prime to phi(n) or
	// (p-1)(q-1)
	public BigInteger e;

	// The private exponent used for decryption, the modular inverse of e mod phi(n)
	private BigInteger d;

	// Default constructor that initializes the Key with a default bit length, 2048
	public Key() {
		this(2048);
	}

	/**
	 * Constructor that initializes the Key with a specific bit length for the
	 * primes.
	 * It generates the public and private keys based on the bit length provided.
	 * 
	 * @param bitLength the bit length for the prime numbers p and q.
	 */
	public Key(int bitLength) {
		System.out.println("Searching for prime p");
		BigInteger p = findPrime(bitLength);
		System.out.println("Searching for prime q");
		BigInteger q = findPrime(bitLength);
		// Calculate the modulus n as the product of primes p and q.
		n = p.multiply(q);
		// Store modulus n in a file.
		saveBigIntToFile(n, 'n');
		// Calculate Euler's totient function phi(n) = (p-1)(q-1).
		BigInteger phi = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE));
		// Select a public exponent e that is relatively prime to phi(n).
		e = chooseE(phi, bitLength);
		// Save the public exponent e in a file.
		saveBigIntToFile(e, 'e');
		// Calculate the private exponent d as the modular inverse of e modulo phi(n).
		d = e.modInverse(phi);
		// Save the private key d to a file.
		saveBigIntToFile(d, 'd');
	}

	/**
	 * Constructor that initializes the Key with the public modulus n and exponent e
	 * from files.
	 * This constructor is used for operations that require only the public key,
	 * so it cannot decrypt
	 * 
	 * @param nSource the file containing the modulus n.
	 * @param eSource the file containing the public exponent e.
	 */
	public Key(File nSource, File eSource) {
		n = readBigIntFromFile(nSource);
		e = readBigIntFromFile(eSource);
		d = null; // No private key is needed for encryption operations.
		System.out.println("Key initialized with n and e values.");
	}

	/**
	 * Constructor that initializes the Key with the public modulus n, exponent e,
	 * and private exponent d from files.
	 * This constructor is suitable for all operations, including decryption.
	 * 
	 * @param nSource the file containing the modulus n.
	 * @param eSource the file containing the public exponent e.
	 * @param dSource the file containing the private exponent d.
	 */
	public Key(File nSource, File eSource, File dSource) {
		n = readBigIntFromFile(nSource);
		e = readBigIntFromFile(eSource);
		d = readBigIntFromFile(dSource);
		System.out.println("Key initialized with n, e, and d values.");
	}

	/**
	 * Reads a BigInteger value from the given file.
	 * 
	 * @param file the file containing the BigInteger value.
	 * @return the BigInteger value read from the file.
	 * @throws NumberFormatException if the BigInteger is improperly formatted.
	 */
	public static BigInteger readBigIntFromFile(File file) {
		Scanner scanner = null;
		BigInteger toReturn = null;
		try {
			scanner = new Scanner(file);
			if (scanner.hasNextBigInteger()) {
				toReturn = scanner.nextBigInteger();
			} else {
				scanner.close();
				throw new NumberFormatException("File " + file.getName() + " is not formatted correctly.");
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + file.getName());
		} finally {
			if (scanner != null) {
				scanner.close();
			}
		}
		return toReturn;
	}

	/**
	 * Generates a random prime number of the specified bit length using the
	 * Solovay-Strassen primality test.
	 * 
	 * @param bitLength the bit length of the prime number to generate.
	 * @return a probable prime number of the specified bit length.
	 */
	private BigInteger findPrime(int bitLength) {
		// Generate a random possible prime of the specified bit length.
		SecureRandom rand = new SecureRandom();
		BigInteger pp = new BigInteger(bitLength, rand);
		pp = pp.setBit(bitLength - 1); // Ensure the number is large enough
		pp = pp.setBit(0); // Ensure the number is odd
		// Check if the candidate is prime using the Solovay-Strassen primality test.
		int numWits = 0;
		double conf = Math.pow(10, -14); // Goal confidence level, 1 in 10^14 chance of being composite
		int numsTested = 1;
		while (confidence(bitLength, numWits) > conf) {
			BigInteger witness = new BigInteger(bitLength, rand);
			if (stillPrime(pp, witness)) {
				// If the candidate is still a probable prime, increment the witness count.
				numWits++;
			} else {
				// If the candidate is composite, reset the witness count and generate a new
				// candidate.
				numsTested++;
				numWits = 0;
				pp = new BigInteger(bitLength, rand);
				pp = pp.setBit(bitLength - 1);
				pp = pp.setBit(0);
			}
		}
		System.out.println("Prime found! Checked with " + numWits + " potential Euler witnesses, " + numsTested
				+ " numbers tested, confidence " + (1 - confidence(bitLength, numWits)));
		return pp;
	}

	/**
	 * Saves the BigInteger value to a file.
	 * 
	 * @param b the BigInteger value to save.
	 * @param c the character to append to the file name.
	 * 
	 * @throws IOException           if an I/O error occurs while saving the
	 *                               BigInteger to the file.
	 * @throws SecurityException     if the program does not have permission to
	 *                               access the file.
	 * @throws FileNotFoundException if the file is not found.
	 */
	public void saveBigIntToFile(BigInteger b, char c) {
		FileWriter fw = null;
		try {
			// Create a new file to store the BigInteger value
			File f = new File(c + "Value.txt");
			if (f.exists() && !f.delete()) {
				throw new IOException("Failed to delete existing file: " + f.getName());
			}
			if (!f.createNewFile()) {
				throw new IOException("Failed to create new file: " + f.getName());
			}
			fw = new FileWriter(f);
			fw.write(b.toString());
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		} catch (SecurityException e) {
			System.err.println("Security exception: insufficient permissions to access the file " + c + "Value.txt.");
		} catch (IOException e) {
			System.err.println("IO error occurred while saving BigInteger to file: " + e.getMessage());
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

	/**
	 * Calculates the probability that a number is composite based on the bit length
	 * and
	 * number of Euler witnesses tested.
	 * 
	 * @param bitLength the bit length of the prime number.
	 * @param numWits   the number of Euler witnesses tested.
	 * @return the confidence level of the primality test.
	 */
	public double confidence(int bitLength, int numWits) {
		double num = bitLength * Math.log(2) - 2;
		double denom = bitLength * Math.log(2) - 2 + Math.pow(2, numWits - 1);
		return num / denom;
	}

	/**
	 * Checks if the given number is still a probable prime after testing with the
	 * Euler witness
	 * by calculating the Euler criterion and comparing it to the Jacobi symbol.
	 * 
	 * @param p the probable prime number to test.
	 * @param w the Euler witness to test with the probable prime.
	 * @return true if the number is still a probable prime, false otherwise.
	 */
	public boolean stillPrime(BigInteger p, BigInteger w) {
		// Check if p is divisible by the witness. If it is, p is composite
		if (!p.gcd(w).equals(BigInteger.ONE)) {
			return false;
		}
		// Calculate the Euler criterion- w^((p-1)/2) mod p
		BigInteger euler = w.modPow(p.subtract(BigInteger.ONE).divide(BigInteger.TWO), p);
		// If the Euler criterion is not 1 or -1 (mod p), p is composite
		if (!(euler.equals(BigInteger.ONE))) {
			if (euler.equals(p.subtract(BigInteger.ONE))) {
				// If euler is -1 (mod p), set it to -1 for comparison
				euler = BigInteger.valueOf(-1);
			} else {
				return false;
			}
		}
		// If the Jacobi symbol is not equal to the Euler criterion, p is composite
		return jacobi(w, p).equals(euler);
	}

	/**
	 * Calculates the Jacobi symbol (w/p) for the given values using the Jacobi
	 * symbol algorithm.
	 * 
	 * @param w the value to calculate the Jacobi symbol for.
	 * @param p the modulus for the Jacobi symbol calculation.
	 * @return the Jacobi symbol for the given values.
	 */
	public BigInteger jacobi(BigInteger w, BigInteger p) {
		// Reduce w mod p
		w = w.mod(p);
		// Factor out all powers of 2 from w, and count the number of factors (i)
		int i = 0;
		while (w.mod(BigInteger.TWO).equals(BigInteger.ZERO)) {
			w = w.divide(BigInteger.TWO);
			i++;
		}
		// Determine the sign of the Jacobi symbol based on the factors of 2
		int factor = 1;
		if (i % 2 != 0) {
			if (p.mod(BigInteger.valueOf(8)).equals(BigInteger.valueOf(3))
					|| p.mod(BigInteger.valueOf(8)).equals(BigInteger.valueOf(5))) {
				factor = -1;
			}
		}
		// If w is 1, the Jacobi symbol is the current factor
		if (w.equals(BigInteger.ONE)) {
			return BigInteger.valueOf(factor);
		} else {
			// Otherwise, recursively calculate the Jacobi symbol ((p mod w)/w)
			// If both w and p are congruent to 3 mod 4, flip the sign of the Jacobi symbol
			if (w.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))
					&& p.mod(BigInteger.valueOf(4)).equals(BigInteger.valueOf(3))) {
				return jacobi(p, w).multiply(BigInteger.valueOf(-1 * factor));
			} else {
				return jacobi(p, w).multiply(BigInteger.valueOf(factor));
			}
		}
	}

	/**
	 * Chooses a public exponent e that is relatively prime to the given value phi.
	 * 
	 * @param phi       the value to choose the public exponent relative to.
	 * @param bitLength the bit length of the public exponent e.
	 * @return a public exponent e that is relatively prime to phi.
	 */
	public BigInteger chooseE(BigInteger phi, int bitLength) {
		// Generate a random public exponent e of the same bit length as phi
		SecureRandom rand = new SecureRandom();
		BigInteger e = new BigInteger(bitLength, rand);
		// If e is not relatively prime to phi, choose a new random e and try again
		if (!phi.gcd(e).equals(BigInteger.ONE)) {
			return chooseE(phi, bitLength);
		}
		return e;
	}

	/**
	 * Checks if the key has a private exponent d.
	 * 
	 * @return true if the key has a private exponent d, false otherwise.
	 */
	public boolean hasD() {
		return d != null;
	}

	/**
	 * Encrypts the given BigInteger using this key.
	 * 
	 * @param b the BigInteger to encrypt.
	 * @return an encrypted BigInteger.
	 */
	public BigInteger encrypt(BigInteger b) {
		return b.modPow(e, n);
	}

	/**
	 * Decrypt the given BigInteger using this key.
	 * 
	 * @param b the BigInteger to decrypt.
	 * @return a decrypted BigInteger.
	 */
	public BigInteger decrypt(BigInteger b) {
		if (d == null) {
			System.out.println("This key does not have a private exponent to decrypt with.");
			return null;
		}
		return b.modPow(d, n);
	}

}
