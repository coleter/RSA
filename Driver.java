import java.io.File;

public class Driver {

	public static void main(String[] args) {
		EncryptionManager em = new EncryptionManager();
		em.encode(new File("doi.txt"));
		em.decode(new File("cipher.txt"));
	}

}
