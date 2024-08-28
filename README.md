# RSA Encryption and Decryption in Java

## Overview

This project implements RSA encryption and decryption in Java. The program allows you to generate RSA keys, encrypt plaintext files, and decrypt ciphertext files. The project demonstrates core concepts of cryptography, such as key generation, encryption, decryption, and handling of large integers.

## Features

Key Generation: Generates RSA public and private keys.
Encryption: Encrypts files using RSA public keys.
Decryption: Decrypts files using RSA private keys.

## Prerequisites

Java: Must have JDK 8 or higher installed.

## Project Structure

Driver.java: The main class that demonstrates encoding and decoding operations.

Key.java: Manages the generation, storage, and usage of RSA keys.

EncryptionManager.java: Handles the encryption and decryption of files.

## How to Use

1. Key Generation: 

    2048-bit keys are automatically generated when an EncryptionManager object is created.

    The public values N and E are required for encryption, while N, E, and the private value D is required for decryption. When a key is generated, all three values (N, E, and D) are saved in files called *nValue.txt*, *eValue.txt*, and *dValue.txt*. 

    Existing keys can be loaded via EncryptionManager constructors.
    ```
    // Assuming "nValue.txt" has been loaded into a File called nFile, and so on

    //For encryption only
    EncryptionManager em = new EncryptionManager(nFile, eFile); 
    
    //For encryption and decryption
    EncryptionManager em = new EncryptionManager(nFile, eFile, dFile);
    ```
    Key constructors can also be used to load existing keys.
    ```
    Key k = new Key(nFile, eFile, dFile);
    EncryptionManager em = new EncryptionManager(k);
    ```


2. Encrypt a File:

    To encrypt a file with EncryptionManager em:
    ```
    em.encode(new File("your_plaintext_file.txt"));
    ```
    The encrypted file will be named "your_plaintext_file_cipher.txt" 
3. Decrypt a File:

    To decrypt a file:
    ```
    em.decode(new File("your_plaintext_file_cipher.txt"));
    ```
    The decrypted file will be named "your_plaintext_file_decoded.txt"
    
4. Running the Program
    
    The Driver class demonstrates a simple encoding and decoding process:
    ```
    public class Driver {
        public static void main(String[] args) {
            EncryptionManager em = new EncryptionManager();
            em.encode(new File("doi.txt"));
            em.decode(new File("cipher.txt"));
        }
    }
    ```

5. Customize Key Size
    
    You can specify the bit length of the keys when creating a new Key object:
    ```
    Key key = new Key(3072); // Generates a key with 3072-bit length

    EncryptionManager em = new EncryptionManager(new Key(3072)); 
    ```

## File Format

**nValue.txt:** Contains the modulus n as a String.

**eValue.txt:** Contains the public exponent e as a String.

**dValue.txt:** Contains the private exponent d as a String.

**doi.txt:** The Declaration of Independence, as an example file to encrypt
