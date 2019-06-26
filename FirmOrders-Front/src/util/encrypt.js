import  CryptoJS from 'crypto-js'
import util from './util'
import JSEncrypt from './jsencrypt';

const serverPublicKey='MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCfXfMzgg4m5RRLg2vc' +
    'rYBFN4sBhE1VtW1sBkXxC5wtCRaOZv0kudk9CIQfU6c+eEaaZKUnygxHWdSqdwURCE0IKgLcol' +
    'XF+RHmu/rl977FfjRg9pAkBg5z05PfHDqWqkIsqX0iRaSP31BUZOgtwafbiBv2dBvRBMdq03ty4q8OQQIDAQAB';

const clientPrivateKey='MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAJ9d8zOCDiblFEuDa9ytgEU3iwGETVW1bWwGRfELnC0JFo5m/SS52T0IhB9Tp' +
    'z54RppkpSfKDEdZ1Kp3BREITQgqAtyiVcX5Eea7+uX3vsV+NGD2kCQGDnPTk98cOpaqQiypfSJFpI/fUFRk6C3Bp9uIG/Z0G9EEx2rTe3Lirw5BAgMBAAECgYB' +
    'tt2Ga4XvavTWWs1jL6cr4XD/gAHS5gBlgGIWIaMTRoIMd8Ltw0F5GDZngc1gdwvJgks+9L/X6HVczMJOVYVCeKwuBo6JcgK64lhh58wcOlMVcUSydIN4QXbUcWm' +
    'PnqTsZ1xErTQSdF6ybbNbP02Kf9VL0c/2SEnFx+hlQv4ZoPQJBAM/HxXGisWmA4WMU6WSw5zN78ms85v9nAUb16eHTxEA+Gopn/0kZVdZG7DOudcC9B3h8Q+pDlZ+aY3JL024' +
    '6K/cCQQDEWfD4W5xmJsr7Jf/G/i00iqMsEoaosBN8EbuyoTqGAauazqdRREv906y8Z43m+UcdyUyyvev+qya2ohDkz1mHAkBhJ5QXEm/KWU0KO1j2kBFLbYVox01r1wot2AbIZX' +
    'C6aU+XsBEaaRJN41PGxqigusKnf9Nx0rA/tL0TUIlIXUL5AkEAkbima8uhkPZtYmLbJtXwurPBUa4WHg8/Bq8qe/HIOzf2IZgI+PkU5LY51kGMQMT0EJkawPTOtlxTCOvSWd3A9' +
    'QJBALYU/fjwJsgA9z6/+yV60JX0TmqrS6HBP4MZ72ICDmgfrL7Fy/q+mmMWn952wxA05eqssl7mnXX0PHekdmrd9JU=';

/**
 * AES+RSA加密
 * @param context
 * @returns {string}
 * @constructor
 */
const Encrypt =(context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }

    if(typeof(context) == 'object'){
        context = JSON.stringify(context);
    }
    let word = CryptoJS.enc.Utf8.parse(context);

    const metadataKey = util.randomString(16);
    const metadataIv= util.randomString(16);

    //AES加密
    let aesEncrypted;
    const key = CryptoJS.enc.Utf8.parse(metadataKey);
    const iv = CryptoJS.enc.Utf8.parse(metadataIv);
    try {
        aesEncrypted = CryptoJS.AES.encrypt(word, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
    } catch (e) {
        console.error('AES Encryption failed')
    }

    //RSA加密
    let rsaEncrypted;
    try{
        let rsaEncrypt = new JSEncrypt();
        rsaEncrypt.setPublicKey(serverPublicKey);
        rsaEncrypted = rsaEncrypt.encryptLong(
            JSON.stringify({
                metadataKey: metadataKey,
                metadataIv: metadataIv,
                metadata: aesEncrypted.toString()
            }));
    }catch (e) {
        console.error('RSA Encryption failed')
    }
    return rsaEncrypted.toString();
}

/**
 * AES+RSA解密
 * @param context
 * @returns {any}
 * @constructor
 */
const Decrypt = (context) =>{
    if(!context || typeof(context) == 'undefined'){
        return;
    }

    //RSA解密
    let rsaDecryptedJson;
    try{
        let rsaDecrypt = new JSEncrypt();
        rsaDecrypt.setPrivateKey(clientPrivateKey);
        let rsaDecrypted = rsaDecrypt.decryptLong(context);
        rsaDecryptedJson = JSON.parse(rsaDecrypted);
    }catch (e) {
        console.error('RSA Decryption failed')
    }

    //AES解密
    const key = CryptoJS.enc.Utf8.parse(rsaDecryptedJson.metadataKey);
    const iv = CryptoJS.enc.Utf8.parse(rsaDecryptedJson.metadataIv);
    let decryptedStr;
    try{
        let decrypt = CryptoJS.AES.decrypt(rsaDecryptedJson.metadata, key, {
            iv: iv,
            mode: CryptoJS.mode.CBC,
            padding: CryptoJS.pad.Pkcs7
        });
        decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
    }catch (e) {
        console.error('AES Decryption failed')
    }
    return JSON.parse(decryptedStr);
}

export {
    Encrypt,
    Decrypt,
};

