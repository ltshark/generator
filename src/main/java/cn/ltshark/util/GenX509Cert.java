package cn.ltshark.util;

import cn.ltshark.entity.User;
import sun.misc.BASE64Encoder;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * CA的根证书签署生成用户的证书
 *
 * @author ltshark
 */
public class GenX509Cert {
    private static final String ROOT_PFX = GlobalConfig.ROOT_PATH + "key/YAICDC01.pfx";
    private static final String USER_TEMP_CER = GlobalConfig.ROOT_PATH + "key/ss.cer";
    private static String UPNOID = "1.3.6.1.4.1.311.20.2.3";
    private static final String ROOT_KEY_PASSWORD = "1234";

    private static final String USER_KEY_STORE_PATH = GlobalConfig.ROOT_PATH + "key/user/";
    //用户模板证书
    private static X509Certificate userTempCertificate;

    //根证书秘钥
    private static PrivateKey rootPrivKey;
    //根证书
    private static X509Certificate rootCert;

    static {
        try {
            //获取用户模板证书
            userTempCertificate = getUserTempCertificate();

            //获取根证书
            KeyStore ks = KeyStore.getInstance("pkcs12");
            FileInputStream ksfis = new FileInputStream(ROOT_PFX);
            ks.load(ksfis, ROOT_KEY_PASSWORD.toCharArray());
            ksfis.close();
            rootPrivKey = (PrivateKey) ks.getKey("yaic-YAICDC01-CA", ROOT_KEY_PASSWORD.toCharArray());
            rootCert = (X509Certificate) ks.getCertificate("yaic-YAICDC01-CA");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createCert(User user) throws Exception {

        //生成客户密钥对
        KeyPair clientKeypair = genNewUserKeypair();

        byte certbytes[] = userTempCertificate.getEncoded();
        X509CertImpl x509certimpl = new X509CertImpl(certbytes);
        X509CertInfo x509certinfo = (X509CertInfo) x509certimpl.get("x509.info");
        x509certinfo.set("key", new CertificateX509Key(clientKeypair.getPublic()));
        X500Name subject = new X500Name("CN=" + user.getFullName() + ", OU=Network, OU=YAIC, DC=yaic, DC=com, DC=cn");
        x509certinfo.set("subject.dname", subject);
        Date bdate = new Date();
        Date edate = new Date();
        edate.setTime(bdate.getTime() + 3650 * 24L * 60L * 60L * 1000L);
        CertificateValidity certificatevalidity = new CertificateValidity(bdate, edate);
        x509certinfo.set("validity", certificatevalidity);

        CertificateExtensions exts = (CertificateExtensions) x509certinfo.get(X509CertInfo.EXTENSIONS);
        GeneralNames generalNames = new GeneralNames();
        ObjectIdentifier upnoid = new ObjectIdentifier(UPNOID);
        generalNames.add(new GeneralName(new OtherName(upnoid, getUTF8String(user.getFullName() + "@yaic.com.cn"))));
        SubjectAlternativeNameExtension subjectAlternativeNameExtension = new SubjectAlternativeNameExtension(false, generalNames);
        //更改用户备用名
        exts.set("SubjectAlternativeName", subjectAlternativeNameExtension);
        //更改私钥指纹信息
        exts.set("SubjectKeyIdentifier", new SubjectKeyIdentifierExtension(new KeyIdentifier(clientKeypair.getPublic()).getIdentifier()));
        x509certinfo.set(X509CertInfo.EXTENSIONS, exts);
        x509certinfo.set("serialNumber", new CertificateSerialNumber(user.getId().hashCode()));

        X509CertImpl x509certimpl1 = new X509CertImpl(x509certinfo);
        x509certimpl1.sign(rootPrivKey, "SHA1withRSA");
        BASE64Encoder base64 = new BASE64Encoder();
        FileOutputStream fos = new FileOutputStream(new File(USER_KEY_STORE_PATH, user.getFullName() + ".crt"));
        base64.encodeBuffer(x509certimpl1.getEncoded(), fos);

        Certificate[] certChain = {x509certimpl1};
        File pfxPath = new File(USER_KEY_STORE_PATH, user.getFullName() + ".pfx");
        savePfx(user.getFullName(), clientKeypair.getPrivate(), ROOT_KEY_PASSWORD, certChain, pfxPath);

//        FileInputStream in = new FileInputStream(pfxPath);
//        KeyStore inputKeyStore = KeyStore.getInstance("pkcs12");
//        inputKeyStore.load(in, ROOT_KEY_PASSWORD.toCharArray());
//        Certificate cert = inputKeyStore.getCertificate("22222222");
//        System.out.print(cert.getPublicKey());
//        PrivateKey privk = (PrivateKey) inputKeyStore.getKey("22222222", ROOT_KEY_PASSWORD.toCharArray());
//        FileOutputStream privKfos = new FileOutputStream(new File(USER_KEY_STORE_PATH, user.getFullName() + ".pvk"));
//        privKfos.write(privk.getEncoded());
//        System.out.print(privk);
//        in.close();
//
//        //验证根证书
//        x509certimpl1.verify(rootCert.getPublicKey(), null);
    }

    private static byte[] getUTF8String(String userData) {
//        String userData = "22222222@yaic.com.cn";
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
     * @param pfxPath
     * @throws Exception
     */
    public static void savePfx(String alias, PrivateKey privKey, String pwd,
                               Certificate[] certChain, File pfxPath) throws Exception {

        KeyStore outputKeyStore = KeyStore.getInstance("pkcs12");
//        System.out.println("KeyStore类型：" + outputKeyStore.getType());

        outputKeyStore.load(null, pwd.toCharArray());
        outputKeyStore.setKeyEntry(alias, privKey, pwd.toCharArray(), certChain);
        FileOutputStream out = new FileOutputStream(pfxPath);
        // 将此 keystore 存储到给定输出流，并用给定密码保护其完整性。
        outputKeyStore.store(out, pwd.toCharArray());
        out.close();
    }

    public void saveJks(String alias, PrivateKey privKey, String pwd,
                        Certificate[] certChain, String filepath) throws Exception {

        KeyStore outputKeyStore = KeyStore.getInstance("jks");
        System.out.println(outputKeyStore.getType());
        outputKeyStore.load(null, pwd.toCharArray());
        outputKeyStore.setKeyEntry(alias, privKey, pwd.toCharArray(), certChain);
        FileOutputStream out = new FileOutputStream(filepath);
        outputKeyStore.store(out, pwd.toCharArray());
        out.close();
    }

    public static void signUserPfxCert(User user) throws Exception {

        if (userTempCertificate == null || rootCert == null || rootPrivKey == null)
            throw new ExceptionInInitializerError("初始化根证书错误");
        createCert(user);
    }

    private static X509Certificate getUserTempCertificate() throws Exception {
        CertificateFactory certificatefactory = CertificateFactory.getInstance("X.509");
        FileInputStream bais = new FileInputStream(USER_TEMP_CER);
        return (X509Certificate) certificatefactory.generateCertificate(bais);
    }

    public static KeyPair genNewUserKeypair() throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "SUN");
        kpg.initialize(2048, sr);
        KeyPair kp = kpg.generateKeyPair();
        return kp;
    }

    public static void main(String[] args) {

        try {
            User user = new User();
            user.setFullName("22222222");
//            user.setId(22222222L);
            GenX509Cert.signUserPfxCert(user);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
