package com.tencent.qcloud.cosxml.sample.common;

import java.net.URLEncoder;

/**
 * Created by bradyxiao on 2017/2/28.
 * author bradyxiao
 */
public class StringUtils {
    private static final char HEX_DIGITS[] =
            {'0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f'};
    public static String toHexString(byte[] data){
        StringBuilder result = new StringBuilder(data.length * 2);
        for(byte b : data){
            result.append(StringUtils.HEX_DIGITS[(b & 0xf0) >>> 4]);
            result.append(StringUtils.HEX_DIGITS[(b & 0x0f)]);
        }
        return result.toString();
    }

    /**
     * url编码，防止url中有些字符引起歧义
     * Url中只允许包含英文字母（a-zA-Z）、数字（0-9）、-_.~
     * 编码的部分：url中domain之后部分， 如http://domain/path?query中 path?query
     * 百分号编码:用%百分号加上两位的字符
     * @param path
     * @return
     */
    public static String encodedUrl(String path){
        String separate = "/";
        String enc = "utf-8";
        String space = "%20";
        if(path == null){
            return null;
        }
        String[] pathSegment = path.split(separate);
        StringBuilder encodedPath = new StringBuilder();
        try{
            for(String str : pathSegment){
                if(!isEmpty(str)){
                    encodedPath.append(URLEncoder.encode(str,enc).replace("+",space))
                    .append(separate);
                }
            }
            if(!path.endsWith(separate)){
                encodedPath.deleteCharAt(encodedPath.length() -1);
            }
            if(path.startsWith(separate)){
                return separate + encodedPath.toString();
            }else{
                return encodedPath.toString();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     *   scheme://authority/path?query#fragment
     * @param url
     * @return
     */

    public static String getScheme(String url){
        if(isEmpty(url)){
            return null;
        }
        String scheme = null;
        String separate = "://";
        int index = url.indexOf(separate);
        if(index != -1){
            scheme = url.substring(0,index);
        }
        return scheme;
    }

    public static String getHost(String url){
        if(isEmpty(url)){
            return null;
        }
        String host = null;
        int pos = url.indexOf("://");
        if(pos <= 0){
            return null;
        }
        int pos2 = url.indexOf('/',pos + 1);
        pos2 = pos2 >= 0 ? pos2 :url.length();
        host = url.substring(pos + 1, pos2);
        int pos3 = host.indexOf(':');
        if(pos3 >= 0)host = host.substring(0,pos3);
        return host;
    }

    public static long getPort(String url){
        if(isEmpty(url)){
            return -1;
        }
        String host = null;
        int pos = url.indexOf("://");
        if(pos <= 0){
            return -1;
        }
        int pos2 = url.indexOf('/',pos + 1);
        pos2 = pos2 >= 0 ? pos2 :url.length();
        host = url.substring(pos + 1, pos2);
        int pos3 = host.indexOf(':');
        long port = -1;
        if(pos3 >= 0){
         port = Long.parseLong(host.substring(pos3 + 1,host.length()));
        }
        return port;
    }

    /**
     *
     * @param url
     * @return String 格式： /xx
     */
    public static String getPath(String url){
        if(isEmpty(url)){
            return null;
        }
        String path = null;
        int pos = url.indexOf('/');
        int pos2 = url.indexOf('?');
        if(pos <= 0){
            return null;
        }
        if(pos >= 0){
            boolean isSlash = url.charAt(pos + 1) == '/'?true:false;
            if(isSlash){
                pos = url.indexOf('/',pos + 2);
            }
        }
        if(pos >= 0){
            int end = pos2 >= 0? pos2: url.length();
            path = url.substring(pos,end);
        }
        return path;
    }

    public static String getQuery(String url){
        if(isEmpty(url)){
            return null;
        }
        String query = null;
        int pos = url.indexOf('?');
        int pos2 = url.indexOf('#');
        if(pos <= 0){
            return null;
        }
        if(pos >= 0){
            int end = pos2 >= 0? pos2: url.length();
            query = url.substring(pos + 1,end);
        }
        return query;
    }

    public static String getFragment (String url){
        if(isEmpty(url)){
            return null;
        }
        String fragment  = null;
        int pos = url.indexOf('#');
        if(pos <= 0){
            return null;
        }
        if(pos >= 0){
            fragment = url.substring(pos + 1);
        }
        return fragment;
    }

    public static boolean isEmpty(String str){
        if(str == null || str.length() == 0){
            return true;
        }else{
            return false;
        }
    }

}
