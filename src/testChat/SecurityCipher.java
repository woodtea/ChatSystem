package testChat;

import java.io.*;
import java.security.*;
import java.security.interfaces.*;
import java.security.spec.*;
import java.math.BigInteger;
import javax.crypto.*;
import javax.crypto.interfaces.*;
import javax.crypto.spec.*;

//import org.passay.*;

import java.net.*;

/*
 * AESreturnType: 用于作为AES加密方法的返回值(需要同时返回
 * 加密之后的字符串与其IV向量字符串)
 * 
 */
class AESreturnType {
	private String cipherText = null;
	private String IV = null;
	AESreturnType(){}
	AESreturnType(String cipher, String IV){
		this.cipherText = cipher;
		this.IV = IV;
	}
	
	String get_cipher(){return cipherText;}
	String get_IV(){return IV;}
}

/*
 *	加密流程:
 *	客户端在登录之前先自主和服务器利用Diffie–Hellman算法交换密钥
 *	密钥即用于接下来的AES对称加密算法
 *
 *	在登录注册等操作之前,
 *
 *	客户端调用GenerateKey_Client(),参数为连接服务器的Socket产生的数据流
 *	服务器调用GenerateKey_Server(),参数为连接客户端的Socket产生的数据流
 *
 *
 *	这样两者调用完就会得到对应的密钥(都是byte[]数组).可以进行加密操作.
 *
 *	加密时调用AES_encrypt(),参数为之前生成的密钥(byte[]数组),需要加密的明文
 *	返回值包含两项,一项是加密的密文,一项是解密所需要的IV向量.
 *
 *	将这两项发送给解密方,解密方调用AES_decrypt,参数为之前生成的密钥,密文,
 *	以及送过来的IV向量(字符串).返回即解密出来的明文.
 *
 *	这就是加密与解密的过程
 *
 *	而此外此类还提供 SHA-256 单向HASH函数,用于服务器本地保存加密的密码
 *	调用 getSHA256StrJava(String),参数为待加密的明文,返回值即加密后的
 *	不可恢复的HASH值.
 * 
 */
public class SecurityCipher {
	
	/*
	 * 密码强度检测相关,调用Passay库进行密码强度检测.
	 */
	/*
	private static LengthRule r1;
	private static CharacterCharacteristicsRule r2;
	private static WhitespaceRule r3;
	private static IllegalCharacterRule r4;
	private static PasswordValidator validator;
	
	static {
		char[] banChar={'\t','\n','\r'};
		r1 = new LengthRule(9,30);
		r2 = new CharacterCharacteristicsRule();
		r2.setNumberOfCharacteristics(3);
		r2.getRules().add(new CharacterRule(EnglishCharacterData.UpperCase,1));
		r2.getRules().add(new CharacterRule(EnglishCharacterData.LowerCase,1));
		r2.getRules().add(new CharacterRule(EnglishCharacterData.Digit,1));
		r2.getRules().add(new CharacterRule(EnglishCharacterData.Special,1));
		r3 = new WhitespaceRule();
		r4 = new IllegalCharacterRule(banChar);
		validator = new PasswordValidator(r1, r2, r3, r4);
	}
	*/
	/*
	 *	检验密码强度,如果强度满足要求则返回true,否则返回false
	 *	密码强度要求:
	 *		1)长度在9到30之间
	 *		2)以下四点至少要满足三点:
	 *			1)至少含有一个数字	
	 *			2)至少含有一个大写字母
	 *			3)至少含有一个小写字母
	 *			4)至少含有一个特殊字符
	 *		3)不包含空格,回车,换行符.
	 */
	/*
	public static boolean password_strength_check(String passwd){
		RuleResult result = validator.validate(new PasswordData(passwd));
		return result.isValid();
	}
	*/
	/*
	 * Diffie-Hellman Key Exchange
	 * 参考Oracle文档内加密相关算法实现
	 */
	public static byte[] GenerateKey_Client(DataInputStream din, DataOutputStream dout){
		int length = 0;
		byte[] bobPub = null;
		
		try {
			KeyPairGenerator aliceGen = KeyPairGenerator.getInstance("DH");
			aliceGen.initialize(2048);
			KeyPair alice = aliceGen.generateKeyPair();
			
			KeyAgreement aliceAgree = KeyAgreement.getInstance("DH");
			aliceAgree.init(alice.getPrivate());
			
			byte[] alicePubKey = alice.getPublic().getEncoded();
		
			dout.writeInt(alicePubKey.length);
			dout.write(alicePubKey);
			
			length = din.readInt();
			if (length > 0){
				bobPub = new byte[length];
				din.readFully(bobPub);
			}
			
			KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobPub);
			
			PublicKey bob = aliceKeyFac.generatePublic(x509KeySpec);
			aliceAgree.doPhase(bob, true);
			
			byte[] aliceSharedSecret = aliceAgree.generateSecret();
			return aliceSharedSecret;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] GenerateKey_Server(DataInputStream din, DataOutputStream dout){
		int length = 0;
		byte[] alicePub = null;
		
		try {
			length = din.readInt();
			if (length > 0){
				alicePub = new byte[length];
				din.readFully(alicePub, 0, alicePub.length);
			}
			
			KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePub);
			
			PublicKey alice = bobKeyFac.generatePublic(x509KeySpec);
			DHParameterSpec dhFromAlice = ((DHPublicKey)alice).getParams();
			
			KeyPairGenerator bobGen = KeyPairGenerator.getInstance("DH");
			bobGen.initialize(dhFromAlice);
			KeyPair bob = bobGen.generateKeyPair();
			
			KeyAgreement bobAgree = KeyAgreement.getInstance("DH");
			bobAgree.init(bob.getPrivate());
			
			byte[] bobPubKey = bob.getPublic().getEncoded(); 
			
			dout.writeInt(bobPubKey.length);
			dout.write(bobPubKey);
			
			bobAgree.doPhase(alice, true);
			byte[] bobSharedSecret = bobAgree.generateSecret();
			return bobSharedSecret;
		} catch (IOException e){
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * AES加密算法
	 * 输入为密钥与待加密的明文字符串,同时需要导入一个byte数组IV用来接收
	 * 解密参数向量,IV需要传输给解密者.
	 * 
	 * 返回值为加密字符串
	 * 同样参照Oracle文档上代码
	 * https://docs.oracle.com/javase/7/docs/technotes/guides/security/crypto/CryptoSpec.html#DH2Ex
	 */
	public static AESreturnType AES_encrypt(byte[] Secret, String plainText){
		SecretKeySpec key = new SecretKeySpec(Secret, 0, 16, "AES");
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] plain = plainText.getBytes();
			byte[] toSend = cipher.doFinal(plain);
			byte[] IV = cipher.getParameters().getEncoded();
			return new AESreturnType(byte2Hex(toSend),byte2Hex(IV));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * AES解密算法
	 * 输入为密钥与待解密的密文字符串,以及用来解密的解谜向量参数IV
	 * 返回值为解密字符串
	 * 同样参照Oracle文档上代码
	 */
	public static String AES_decrypt(byte[] Secret, String cipherText, String IV){
		SecretKeySpec key =new SecretKeySpec(Secret, 0, 16, "AES");
		try {
			AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
			aesParams.init(Hex2byte(IV));
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, aesParams);
			byte[] ciphertext = Hex2byte(cipherText);
			byte[] plain = cipher.doFinal(ciphertext);
			return new String(plain);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * SHA256,参考网上博客使用方法 
	 * 将字符串str加密,返回值为密文(单向HASH函数)
	 */
	public static String getSHA256Str(String str){
		MessageDigest messagedigest;
		String encodeStr="";
		try{
			messagedigest=MessageDigest.getInstance("SHA-256");
			messagedigest.update(str.getBytes("utf-8"));
			encodeStr=byte2Hex(messagedigest.digest());
		}catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return encodeStr;
	}
	

	private static byte[] Hex2byte(String hexStr){
		int length = hexStr.length();
		if (length<1)
			return null;
		length /= 2;
		byte[] result = new byte[length];
		for (int i = 0; i < length; ++i){
			int high = Integer.parseInt(hexStr.substring(i*2, i*2+1), 16);
			int low = Integer.parseInt(hexStr.substring(i*2+1, i*2+2), 16);
			result[i] = (byte)(high * 16 + low);
		}
		return result;
	}
	
	private static String byte2Hex(byte[] bytes){
		StringBuffer sb=new StringBuffer();
		String temp=null;
		for (int i=0;i<bytes.length;++i){
			temp=Integer.toHexString(bytes[i] & 0xFF);
			if (temp.length()==1){
				sb.append(0);
			}
			sb.append(temp.toUpperCase());
		}
		return sb.toString();
	}
	
	private static void byte2Hex(byte b, StringBuffer buf){
		char[] HexChars =  { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		int high = ((b & 0xf0) >> 4);
		int low = (b & 0x0f);
		buf.append(HexChars[high]);
		buf.append(HexChars[low]);
	}
	
	public static String toHexString(byte[] block){
		StringBuffer buf = new StringBuffer();
		int len = block.length;
		for (int i = 0;i < len; ++i){
			byte2Hex(block[i], buf);
			if (i < len-1)
				buf.append(":");
		}
		return buf.toString();
	}
	
	public static String get_send(byte[] alice, String text) {
		AESreturnType tmp = AES_encrypt(alice, text);
		String cipher = tmp.get_cipher();
		String IV = tmp.get_IV();
		return cipher + " " + IV;
	}
	
	public static String get_receive(byte[] bob, String text) {
		String tmp[] = text.split(" ");
		String ans = SecurityCipher.AES_decrypt(bob, tmp[0], tmp[1]);
		return ans;
	}
	
}
