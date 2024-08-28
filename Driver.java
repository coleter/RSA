import java.io.File;

public class Driver {

	public static void main(String[] args) {
		File toEncrypt = new File("doi.txt");

		// To use a key stored in files
		// File n = new File("nValue.txt");
		// File e = new File("eValue.txt");
		// File d = new File("dValue.txt");
		// EncryptionManager em = new EncryptionManager(n, e, d);

		// To generate a new key
		EncryptionManager em = new EncryptionManager();

		// Encryption and decryption
		em.encrypt(toEncrypt);
		em.decrypt(new File(toEncrypt.getName().substring(0, toEncrypt.getName().lastIndexOf('.')) + "_encrypted.txt"));
	}

}
