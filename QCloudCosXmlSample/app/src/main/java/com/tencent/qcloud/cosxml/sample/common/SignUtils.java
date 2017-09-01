package com.tencent.qcloud.cosxml.sample.common;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by bradyxiao on 2017/4/1.
 * author bradyxiao
 */
public class SignUtils {
    public String qSignAlgorithm = null;
    public String qSecretID = null;
    public String qSecretKey = null;
    public String qKeyTime = null;
    public String qSignTime = null;
    public String qHeaderList = null;
    public String qUrlParamList = null;
    public String headerList = null;
    public String urlParamList = null;
    public String qMethod = null;
    public String qUri = null;

    private static SignUtils instance;

    private SignUtils() {
        qSignAlgorithm = "sha1";
    }

    public static SignUtils getInstance() {
        if (instance == null) {
            instance = new SignUtils();
        }
        return instance;
    }

    public void setQSignAlgorithm(String qSignAlgorithm) {
        this.qSignAlgorithm = qSignAlgorithm.trim();
    }

    public void setSecretIdAndKey(String secretId, String secretKey) {
        this.qSecretID = secretId;
        this.qSecretKey = secretKey;
    }

    public void setQSignTime(long signTimeStart, long signTimeEnd) {
        qSignTime = signTimeStart + ";" + signTimeEnd;
    }

    public void setQKeyTime(long keyTimeStart, long keyTimeEnd) {
        qKeyTime = keyTimeStart + ";" + keyTimeEnd;
    }

    /**
     * @param headerMap key value是小写字母
     */
    public void setQHeaderList(Map<String, Object> headerMap) {
        if (headerMap != null) {
            //确保小写 key, value 经过uriEncode
            Map<String, Object> temp = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : headerMap.entrySet()) {
                temp.put(entry.getKey().toLowerCase().trim(), StringUtils.encodedUrl(((String) entry.getValue()).trim()));
            }

            //按key排序，方式为字典排序，即升序
            List<Map.Entry<String, Object>> list = new ArrayList<>(temp.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            StringBuilder keyStringBuilder = new StringBuilder();
            StringBuilder kvStringBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : list) {
                keyStringBuilder.append(entry.getKey()).append(";");
                kvStringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            keyStringBuilder.deleteCharAt(keyStringBuilder.length() - 1);
            kvStringBuilder.deleteCharAt(kvStringBuilder.length() - 1);
            this.qHeaderList = keyStringBuilder.toString();
            this.headerList = kvStringBuilder.toString();
        }
    }

    /**
     * @param urlParamList key  value 是小写字母
     */
    public void setQUrlParamList(Map<String, Object> urlParamList) {
        if (urlParamList != null) {
            //key value 均必须urlEncode且小写，
            Map<String, Object> temp = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : urlParamList.entrySet()) {
                temp.put(StringUtils.encodedUrl(entry.getKey().trim()).toLowerCase(),
                        StringUtils.encodedUrl(((String) entry.getValue()).trim()).toLowerCase());
            }

            List<Map.Entry<String, Object>> list = new ArrayList<>(urlParamList.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Object>>() {
                @Override
                public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
                    return o1.getKey().compareTo(o2.getKey());
                }
            });
            StringBuilder keyStringBuilder = new StringBuilder();
            StringBuilder kvStringBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : list) {
                keyStringBuilder.append(entry.getKey()).append(";");
                kvStringBuilder.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            keyStringBuilder.deleteCharAt(keyStringBuilder.length() - 1);
            kvStringBuilder.deleteCharAt(kvStringBuilder.length() - 1);
            this.qUrlParamList = keyStringBuilder.toString();
            this.urlParamList = kvStringBuilder.toString();
        }
    }

    public void setqMethod(String qMethod) {
        this.qMethod = qMethod.trim().toLowerCase();
    }

    public void setqUri(String qUri) {
        this.qUri = qUri.trim();
    }

    /**
     * 解析参数
     *
     * @param url        scheme://host:port/path?query#fragment
     * @param httpMethod
     */
    public void setUrl(String url, String httpMethod) {
        try {
            if (httpMethod != null) {
                qMethod = httpMethod.trim().toLowerCase();
            } else {
                throw new IllegalArgumentException("http method is null");
            }
            if (url != null) {
                qUri = StringUtils.getPath(url);
                if (qUri == null) throw new IllegalArgumentException("qUrl is null");
                String queryStr = StringUtils.getQuery(url);
                if (queryStr == null) throw new IllegalArgumentException("qUrlParamList is null");
                //urlEncode编码
                queryStr = StringUtils.encodedUrl(queryStr);
                String[] query = queryStr.split("&");
                int len = query.length;
                List<String> queryList = new ArrayList<>(len);
                for (int i = 0; i < len; ++i) {
                    queryList.add(query[i]);
                }
                //按字典排序
                Collections.sort(queryList, new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        Comparator<Object> comparator = Collator.getInstance();
                        return comparator.compare(o1, o2);
                    }
                });
                StringBuilder keyStringBuilder = new StringBuilder();
                StringBuilder kvStringBuilder = new StringBuilder();
                for (int i = 1; i < len; ++i) {
                    int pos = queryList.get(i).indexOf("=");
                    if (pos >= 0) {
                        keyStringBuilder.append(queryList.get(i).substring(0, pos).toLowerCase());
                    }
                    kvStringBuilder.append(queryList.get(i).toLowerCase()).append("&");
                }
                this.qUrlParamList = keyStringBuilder.toString();
                this.urlParamList = kvStringBuilder.toString();
            } else {
                throw new IllegalArgumentException("url is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * SignKey：携带有效时间，并通过 SecretKey 进行 HMAC-SHA1 加密的密钥串。
     * FormatString：将请求经过一定规范格式化后的字串。
     * StringToSign：包含校验算法、请求有效时间和 Hash 校验后的 FormatString 的字符串。
     * Signature：加密后的签名，使用 SignKey 与 StringToSign 通过 HMAC-SHA1 加密的字符串，填入 q-signature。
     *
     * @return String sign
     */
    public String getSign() {
        StringBuilder sign = new StringBuilder();

        String signKey = getSignkey(qSecretKey, qKeyTime);
        String formatString = getFromatString(qMethod, qUri, urlParamList, headerList);
        String stringToToken = getStringToSign(qSignAlgorithm, qSignTime, formatString);
        String signature = getSignature(signKey, stringToToken);

        sign.append("q-sign-algorithm").append("=").append(qSignAlgorithm)
                .append("&").append("q-ak").append("=").append(qSecretID)
                .append("&").append("q-sign-time").append("=").append(qSignTime)
                .append("&").append("q-key-time").append("=").append(qKeyTime);
        if (qHeaderList == null) {
            sign.append("&").append("q-header-list").append("=");
        } else {
            sign.append("&").append("q-header-list").append("=").append(qHeaderList);
        }
        if (qUrlParamList == null) {
            sign.append("&").append("q-url-param-list").append("=");
        } else {
            sign.append("&").append("q-header-list").append("=").append(qUrlParamList);
        }
        sign.append("&").append("q-signature").append("=").append(signature);
        return sign.toString();
    }

    protected String getSignkey(String secretKey, String qKeyTime) {
        String signKey = null;
        try {
            if (secretKey == null) {
                throw new IllegalArgumentException("secretKey is null");
            }
            if (qKeyTime == null) {
                throw new IllegalArgumentException("qKeyTime is null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            byte[] byteKey = secretKey.getBytes("utf-8");
            SecretKey hmacKey = new SecretKeySpec(byteKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(hmacKey);
            signKey = StringUtils.toHexString(mac.doFinal(qKeyTime.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return signKey;
    }

    protected String getFromatString(String qMethod, String qUri, String qParamters, String qHeaders) {
        StringBuilder formatString = new StringBuilder();
        if (qMethod == null) {
            throw new IllegalArgumentException("qMehtod is null");
        }
        if (qUri == null) {
            throw new IllegalArgumentException("qUri is null");
        }
        formatString.append(qMethod).append("\n")
                .append(qUri).append("\n");

        if (qParamters != null) {
            formatString.append(qParamters).append("\n");
        } else {
            formatString.append("\n");
        }
        if (qHeaders != null) {
            formatString.append(qHeaders).append("\n");
        } else {
            formatString.append("\n");
        }
        return formatString.toString();
    }

    protected String getStringToSign(String qSignAlgorithm, String qSignTime, String formatString) {
        StringBuilder stringToSign = new StringBuilder();
        if (qSignAlgorithm == null) {
            qSignAlgorithm = "sha1";
        }
        if (qSignTime == null) {
            throw new IllegalArgumentException("qSignTime is null");
        }
        if (formatString == null) {
            throw new IllegalArgumentException("formatString is null");
        }
        stringToSign.append(qSignAlgorithm).append("\n")
                .append(qSignTime).append("\n")
                .append(SHA1Utils.getSHA1FromString(formatString)).append("\n");
        return stringToSign.toString();
    }

    protected String getSignature(String signKey, String stringToSign) {
        String signature = null;
        try {
            if (signKey == null) {
                throw new IllegalArgumentException("signKey is null");
            }
            if (stringToSign == null) {
                throw new IllegalArgumentException("stringToSign is null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            byte[] byteKey = signKey.getBytes("utf-8");
            SecretKey hmacKey = new SecretKeySpec(byteKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(hmacKey);
            signature = StringUtils.toHexString(mac.doFinal(stringToSign.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return signature;
    }

}
