package cn.ltshark.util;

import sun.misc.BASE64Encoder;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

//import sun.security.x509.X500Signer;

/**
 * 首先生成CA的根证书，然后有CA的根证书签署生成ScriptX的证书
 *
 * @author Administrator
 */
public class GenX509Cert {
    /**
     * 提供强加密随机数生成器 (RNG)*
     */
    private SecureRandom sr;
    private static String UPNOID = "1.3.6.1.4.1.311.20.2.3";

    public GenX509Cert() throws NoSuchAlgorithmException,
            NoSuchProviderException {
        // 返回实现指定随机数生成器 (RNG) 算法的 SecureRandom 对象。
        sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
    }

    public void createCert(X509Certificate rootCert, PrivateKey rootPrivKey, X509Certificate tempCert,
                           KeyPair clientKeypair) throws CertificateException, IOException,
            InvalidKeyException, NoSuchAlgorithmException,
            NoSuchProviderException, SignatureException {

        // X.509 v1 证书的抽象类。此类提供了一种访问 X.509 v1 证书所有属性的标准方式。
        byte certbytes[] = tempCert.getEncoded();

        // The X509CertImpl class represents an X.509 certificate.
        X509CertImpl x509certimpl = new X509CertImpl(certbytes);

        // The X509CertInfo class represents X.509 certificate information.
        X509CertInfo x509certinfo = (X509CertInfo) x509certimpl
                .get("x509.info");

        // This class defines the X509Key attribute for the Certificate.
        x509certinfo.set("key", new CertificateX509Key(clientKeypair.getPublic()));

        X500Name subject = new X500Name("CN=10000000, OU=Network, OU=YAIC, DC=yaic, DC=com, DC=cn");

        x509certinfo.set("subject.dname", subject);

        // 开始时间
        Date bdate = new Date();

        // 结束时间
        Date edate = new Date();

        // 天 小时 分 秒 毫秒
        edate.setTime(bdate.getTime() + 3650 * 24L * 60L * 60L * 1000L);

        // validity为有效时间长度 单位为秒,This class defines the interval for which the
        // certificate is valid.证书的有效时间
        CertificateValidity certificatevalidity = new CertificateValidity(
                bdate, edate);

        x509certinfo.set("validity", certificatevalidity);
        CertificateExtensions exts = (CertificateExtensions) x509certinfo.get(X509CertInfo.EXTENSIONS);

        GeneralNames generalNames = new GeneralNames();
//        "10000000@yaic.com.cn"
        ObjectIdentifier upnoid = new ObjectIdentifier(UPNOID);

        generalNames.add(new GeneralName(new OtherName(upnoid, getUTF8String("10000000@yaic.com.cn"))));
        SubjectAlternativeNameExtension subjectAlternativeNameExtension = new SubjectAlternativeNameExtension(false, generalNames);
        exts.set("SubjectAlternativeName", subjectAlternativeNameExtension);
        exts.set("SubjectKeyIdentifier", new SubjectKeyIdentifierExtension(new KeyIdentifier(clientKeypair.getPublic()).getIdentifier()));

        x509certinfo.set(X509CertInfo.EXTENSIONS, exts);
        x509certinfo.set("serialNumber", new CertificateSerialNumber(10000000));

        X509CertImpl x509certimpl1 = new X509CertImpl(x509certinfo);
        x509certimpl1.sign(rootPrivKey, "SHA1withRSA");
        BASE64Encoder base64 = new BASE64Encoder();
        FileOutputStream fos = new FileOutputStream(new File("d:\\key\\10000000.crt"));
        base64.encodeBuffer(x509certimpl1.getEncoded(), fos);
        try {
            Certificate[] certChain = {x509certimpl1};
            savePfx("10000000", clientKeypair.getPrivate(), "1234", certChain, "d:\\key\\10000000.pfx");
            FileInputStream in = new FileInputStream("d:\\key\\10000000.pfx");
            KeyStore inputKeyStore = KeyStore.getInstance("pkcs12");
            inputKeyStore.load(in, "1234".toCharArray());
            Certificate cert = inputKeyStore.getCertificate("10000000");
            System.out.print(cert.getPublicKey());
            PrivateKey privk = (PrivateKey) inputKeyStore.getKey("10000000", "1234".toCharArray());
            FileOutputStream privKfos = new FileOutputStream(new File("d:\\key\\10000000.pvk"));
            privKfos.write(privk.getEncoded());
            System.out.print(privk);
            // base64.encode(key.getEncoded(), privKfos);
            in.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // 生成文件
        x509certimpl1.verify(rootCert.getPublicKey(), null);

    }

    private byte[] getUTF8String(String userData) {
//        String userData = "10000000@yaic.com.cn";
        byte l = (byte) userData.length();// 数据总长17位
        byte f = 12;
        byte[] bs = new byte[userData.length() + 2];
        bs[0] = f;
        bs[1] = l;
        for (int i = 2; i < bs.length; i++) {
            bs[i] = (byte) userData.charAt(i - 2);
        }
        return bs;
    }

    /**
     * 保存此根证书信息KeyStore Personal Information Exchange
     *
     * @param alias
     * @param privKey
     * @param pwd
     * @param certChain
     * @param filepath
     * @throws Exception
     */
    public void savePfx(String alias, PrivateKey privKey, String pwd,
                        Certificate[] certChain, String filepath) throws Exception {
        // 此类表示密钥和证书的存储设施。
        // 返回指定类型的 keystore 对象。此方法从首选 Provider 开始遍历已注册安全提供者列表。返回一个封装 KeyStoreSpi
        // 实现的新 KeyStore 对象，该实现取自第一个支持指定类型的 Provider。
        KeyStore outputKeyStore = KeyStore.getInstance("pkcs12");

        System.out.println("KeyStore类型：" + outputKeyStore.getType());

        // 从给定输入流中加载此 KeyStore。可以给定一个密码来解锁 keystore（例如，驻留在硬件标记设备上的 keystore）或检验
        // keystore 数据的完整性。如果没有指定用于完整性检验的密码，则不会执行完整性检验。如果要创建空
        // keystore，或者不能从流中初始化 keystore，则传递 null 作为 stream 的参数。注意，如果此 keystore
        // 已经被加载，那么它将被重新初始化，并再次从给定输入流中加载。
        outputKeyStore.load(null, pwd.toCharArray());

        // 将给定密钥（已经被保护）分配给给定别名。如果受保护密钥的类型为
        // java.security.PrivateKey，则它必须附带证明相应公钥的证书链。如果底层 keystore 实现的类型为
        // jks，则必须根据 PKCS #8 标准中的定义将 key 编码为
        // EncryptedPrivateKeyInfo。如果给定别名已经存在，则与别名关联的 keystore
        // 信息将被给定密钥（还可能包括证书链）重写。
        outputKeyStore
                .setKeyEntry(alias, privKey, pwd.toCharArray(), certChain);

        // KeyStore.PrivateKeyEntry pke=new
        // KeyStore.PrivateKeyEntry(kp.getPrivate(),certChain);
        // KeyStore.PasswordProtection password=new
        // KeyStore.PasswordProtection("123456".toCharArray());
        // outputKeyStore.setEntry("scriptx", pke, password);

        FileOutputStream out = new FileOutputStream(filepath);

        // 将此 keystore 存储到给定输出流，并用给定密码保护其完整性。
        outputKeyStore.store(out, pwd.toCharArray());

        out.close();
    }

    public void saveJks(String alias, PrivateKey privKey, String pwd,
                        Certificate[] certChain, String filepath) throws Exception {

        KeyStore outputKeyStore = KeyStore.getInstance("jks");

        System.out.println(outputKeyStore.getType());

        outputKeyStore.load(null, pwd.toCharArray());

        outputKeyStore
                .setKeyEntry(alias, privKey, pwd.toCharArray(), certChain);

        // KeyStore.PrivateKeyEntry pke=new
        // KeyStore.PrivateKeyEntry(kp.getPrivate(),certChain);
        // KeyStore.PasswordProtection password=new
        // KeyStore.PasswordProtection("123456".toCharArray());
        // outputKeyStore.setEntry("scriptx", pke, password);

        FileOutputStream out = new FileOutputStream(filepath);

        outputKeyStore.store(out, pwd.toCharArray());

        out.close();
    }

    public void signCert() throws NoSuchAlgorithmException,
            CertificateException, IOException, UnrecoverableKeyException,
            InvalidKeyException, NoSuchProviderException, SignatureException {

        try {

            KeyStore ks = KeyStore.getInstance("pkcs12");

            FileInputStream ksfis = new FileInputStream("d:\\key\\YAICDC01.pfx");
//            FileInputStream ksfis = new FileInputStream("d:\\key\\10000000.pfx");

            char[] storePwd = "1234".toCharArray();

            char[] keyPwd = "1234".toCharArray();

            // 从给定输入流中加载此 KeyStore。
            ks.load(ksfis, storePwd);

            ksfis.close();

            // 返回与给定别名关联的密钥(私钥)，并用给定密码来恢复它。必须已经通过调用 setKeyEntry，或者以
            // PrivateKeyEntry
            // 或 SecretKeyEntry 为参数的 setEntry 关联密钥与别名。
            PrivateKey rootPrivKey = (PrivateKey) ks.getKey("yaic-YAICDC01-CA", keyPwd);

//            PrivateKey privK = loadPrivateKey(new File("d:\\key\\1_pri.p8"));
            // 返回与给定别名关联的证书。如果给定的别名标识通过调用 setCertificateEntry 创建的条目，或者通过调用以
            // TrustedCertificateEntry 为参数的 setEntry
            // 创建的条目，则返回包含在该条目中的可信证书。如果给定的别名标识通过调用 setKeyEntry 创建的条目，或者通过调用以
            // PrivateKeyEntry 为参数的 setEntry 创建的条目，则返回该条目中证书链的第一个元素。
            X509Certificate rootCert = (X509Certificate) ks.getCertificate("yaic-YAICDC01-CA");
//            X509Certificate rootCert = (X509Certificate) ks.getCertificate("10000000");

            CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
            FileInputStream bais = new FileInputStream("d:\\key\\ss.cer");
            X509Certificate tempCert = (X509Certificate) certificatefactory.generateCertificate(bais);

            createCert(rootCert, rootPrivKey, tempCert, genKey());

        } catch (KeyStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public KeyPair genKey() throws NoSuchAlgorithmException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");

        kpg.initialize(2048, sr);

        System.out.print(kpg.getAlgorithm());

        KeyPair kp = kpg.generateKeyPair();

        return kp;
    }

    public static void main(String[] args) {

        try {

//            PublicKey pk = Cert.getPublicKey();
//            BASE64Encoder bse = new BASE64Encoder();
//            System.out.println("pk:" + bse.encode(pk.getEncoded()));

            GenX509Cert gcert = new GenX509Cert();


            gcert.signCert();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
