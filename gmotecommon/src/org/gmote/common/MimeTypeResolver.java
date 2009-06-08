/**
 * Copyright 2009 Marc Stogaitis and Mimi Sun
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gmote.common;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Converts a file extension to a mime type (content type). This is based on the
 * list provided at: http://www.w3schools.com/media/media_mimeref.asp
 * 
 * Note: We should probably move to something a little more robust such as
 * javax.activation.MimetypesFileTypeMap in the future. We were worried about
 * proper packaging on mac/linux/windows so we decided to go the simple way for
 * now but this should be investigated.
 * 
 * @author Marc Stogaitis
 */
public class MimeTypeResolver {
  private static final Map<String, String> mimeTypeMap = new HashMap<String, String>();
  public static final String UNKNOWN_MIME_TYPE = "unknown/unknown";
  static {
    fillMap();
  }
  
  public static String findMimeType(String fileName) {
    int ind = fileName.lastIndexOf('.');
    String ct = null;
    if (ind > 0) {
      ct = mimeTypeMap.get((fileName.substring(ind + 1).toLowerCase()));
    }
    if (ct == null) {
      ct = UNKNOWN_MIME_TYPE;
    }
    return ct;
  }
  
  
  public static String findMimeTypeSlow(File file) {
    String contentType = null;
    try {
      URL u = file.toURI().toURL();
      URLConnection uc = null;
      uc = u.openConnection();
      contentType = uc.getContentType();
    } catch (MalformedURLException e) {
      System.out.println(e.getMessage());
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    if (contentType == null) {
      contentType = UNKNOWN_MIME_TYPE;
    }
    return contentType;
  }

  private static void fillMap() {

    mimeTypeMap.put("23", "text/h323");
    mimeTypeMap.put("acx", "application/internet-property-stream");
    mimeTypeMap.put("ai", "application/postscript");
    mimeTypeMap.put("aif", "audio/x-aiff");
    mimeTypeMap.put("aifc", "audio/x-aiff");
    mimeTypeMap.put("aiff", "audio/x-aiff");
    mimeTypeMap.put("asf", "video/x-ms-asf");
    mimeTypeMap.put("asr", "video/x-ms-asf");
    mimeTypeMap.put("asx", "video/x-ms-asf");
    mimeTypeMap.put("au", "audio/basic");
    mimeTypeMap.put("avi", "video/x-msvideo");
    mimeTypeMap.put("axs", "application/olescript");
    mimeTypeMap.put("bas", "text/plain");
    mimeTypeMap.put("bcpio", "application/x-bcpio");
    mimeTypeMap.put("bin", "application/octet-stream");
    mimeTypeMap.put("bmp", "image/bmp");
    mimeTypeMap.put("c", "text/plain");
    mimeTypeMap.put("cat", "application/vnd.ms-pkiseccat");
    mimeTypeMap.put("cdf", "application/x-cdf");
    mimeTypeMap.put("cer", "application/x-x509-ca-cert");
    mimeTypeMap.put("class", "application/octet-stream");
    mimeTypeMap.put("clp", "application/x-msclip");
    mimeTypeMap.put("cmx", "image/x-cmx");
    mimeTypeMap.put("cod", "image/cis-cod");
    mimeTypeMap.put("cpio", "application/x-cpio");
    mimeTypeMap.put("crd", "application/x-mscardfile");
    mimeTypeMap.put("crl", "application/pkix-crl");
    mimeTypeMap.put("crt", "application/x-x509-ca-cert");
    mimeTypeMap.put("csh", "application/x-csh");
    mimeTypeMap.put("css", "text/css");
    mimeTypeMap.put("dcr", "application/x-director");
    mimeTypeMap.put("der", "application/x-x509-ca-cert");
    mimeTypeMap.put("dir", "application/x-director");
    mimeTypeMap.put("dll", "application/x-msdownload");
    mimeTypeMap.put("dms", "application/octet-stream");
    mimeTypeMap.put("doc", "application/msword");
    mimeTypeMap.put("dot", "application/msword");
    mimeTypeMap.put("dvi", "application/x-dvi");
    mimeTypeMap.put("dxr", "application/x-director");
    mimeTypeMap.put("eps", "application/postscript");
    mimeTypeMap.put("etx", "text/x-setext");
    mimeTypeMap.put("evy", "application/envoy");
    mimeTypeMap.put("exe", "application/octet-stream");
    mimeTypeMap.put("fif", "application/fractals");
    mimeTypeMap.put("flr", "x-world/x-vrml");
    mimeTypeMap.put("gif", "image/gif");
    mimeTypeMap.put("gtar", "application/x-gtar");
    mimeTypeMap.put("gz", "application/x-gzip");
    mimeTypeMap.put("h", "text/plain");
    mimeTypeMap.put("hdf", "application/x-hdf");
    mimeTypeMap.put("hlp", "application/winhlp");
    mimeTypeMap.put("hqx", "application/mac-binhex40");
    mimeTypeMap.put("hta", "application/hta");
    mimeTypeMap.put("htc", "text/x-component");
    mimeTypeMap.put("htm", "text/html");
    mimeTypeMap.put("html", "text/html");
    mimeTypeMap.put("htt", "text/webviewhtml");
    mimeTypeMap.put("ico", "image/x-icon");
    mimeTypeMap.put("ief", "image/ief");
    mimeTypeMap.put("iii", "application/x-iphone");
    mimeTypeMap.put("ins", "application/x-internet-signup");
    mimeTypeMap.put("isp", "application/x-internet-signup");
    mimeTypeMap.put("jfif", "image/pipeg");
    mimeTypeMap.put("jpe", "image/jpeg");
    mimeTypeMap.put("jpeg", "image/jpeg");
    mimeTypeMap.put("jpg", "image/jpeg");
    mimeTypeMap.put("js", "application/x-javascript");
    mimeTypeMap.put("latex", "application/x-latex");
    mimeTypeMap.put("lha", "application/octet-stream");
    mimeTypeMap.put("lsf", "video/x-la-asf");
    mimeTypeMap.put("lsx", "video/x-la-asf");
    mimeTypeMap.put("lzh", "application/octet-stream");
    mimeTypeMap.put("m13", "application/x-msmediaview");
    mimeTypeMap.put("m14", "application/x-msmediaview");
    mimeTypeMap.put("m3u", "audio/x-mpegurl");
    mimeTypeMap.put("man", "application/x-troff-man");
    mimeTypeMap.put("mdb", "application/x-msaccess");
    mimeTypeMap.put("me", "application/x-troff-me");
    mimeTypeMap.put("mht", "message/rfc822");
    mimeTypeMap.put("mhtml", "message/rfc822");
    mimeTypeMap.put("mid", "audio/mid");
    mimeTypeMap.put("mny", "application/x-msmoney");
    mimeTypeMap.put("mov", "video/quicktime");
    mimeTypeMap.put("movie", "video/x-sgi-movie");
    mimeTypeMap.put("mp2", "video/mpeg");
    mimeTypeMap.put("mp3", "audio/mpeg");
    mimeTypeMap.put("mp4", "video/mp4");
    mimeTypeMap.put("mpa", "video/mpeg");
    mimeTypeMap.put("mpe", "video/mpeg");
    mimeTypeMap.put("mpeg", "video/mpeg");
    mimeTypeMap.put("mpg", "video/mpeg");
    mimeTypeMap.put("mpp", "application/vnd.ms-project");
    mimeTypeMap.put("mpv2", "video/mpeg");
    mimeTypeMap.put("ms", "application/x-troff-ms");
    mimeTypeMap.put("mvb", "application/x-msmediaview");
    mimeTypeMap.put("nws", "message/rfc822");
    mimeTypeMap.put("oda", "application/oda");
    mimeTypeMap.put("ogg", "application/ogg");
    mimeTypeMap.put("p10", "application/pkcs10");
    mimeTypeMap.put("p12", "application/x-pkcs12");
    mimeTypeMap.put("p7b", "application/x-pkcs7-certificates");
    mimeTypeMap.put("p7c", "application/x-pkcs7-mime");
    mimeTypeMap.put("p7m", "application/x-pkcs7-mime");
    mimeTypeMap.put("p7r", "application/x-pkcs7-certreqresp");
    mimeTypeMap.put("p7s", "application/x-pkcs7-signature");
    mimeTypeMap.put("pbm", "image/x-portable-bitmap");
    mimeTypeMap.put("pdf", "application/pdf");
    mimeTypeMap.put("pfx", "application/x-pkcs12");
    mimeTypeMap.put("pgm", "image/x-portable-graymap");
    mimeTypeMap.put("pko", "application/ynd.ms-pkipko");
    mimeTypeMap.put("pma", "application/x-perfmon");
    mimeTypeMap.put("pmc", "application/x-perfmon");
    mimeTypeMap.put("pml", "application/x-perfmon");
    mimeTypeMap.put("pmr", "application/x-perfmon");
    mimeTypeMap.put("pmw", "application/x-perfmon");
    mimeTypeMap.put("pnm", "image/x-portable-anymap");
    mimeTypeMap.put("pot,", "application/vnd.ms-powerpoint");
    mimeTypeMap.put("ppm", "image/x-portable-pixmap");
    mimeTypeMap.put("pps", "application/vnd.ms-powerpoint");
    mimeTypeMap.put("ppt", "application/vnd.ms-powerpoint");
    mimeTypeMap.put("prf", "application/pics-rules");
    mimeTypeMap.put("ps", "application/postscript");
    mimeTypeMap.put("pub", "application/x-mspublisher");
    mimeTypeMap.put("qt", "video/quicktime");
    mimeTypeMap.put("ra", "audio/x-pn-realaudio");
    mimeTypeMap.put("ram", "audio/x-pn-realaudio");
    mimeTypeMap.put("ras", "image/x-cmu-raster");
    mimeTypeMap.put("rgb", "image/x-rgb");
    mimeTypeMap.put("rmi", "audio/mid");
    mimeTypeMap.put("roff", "application/x-troff");
    mimeTypeMap.put("rtf", "application/rtf");
    mimeTypeMap.put("rtx", "text/richtext");
    mimeTypeMap.put("scd", "application/x-msschedule");
    mimeTypeMap.put("sct", "text/scriptlet");
    mimeTypeMap.put("setpay", "application/set-payment-initiation");
    mimeTypeMap.put("setreg", "application/set-registration-initiation");
    mimeTypeMap.put("sh", "application/x-sh");
    mimeTypeMap.put("shar", "application/x-shar");
    mimeTypeMap.put("sit", "application/x-stuffit");
    mimeTypeMap.put("snd", "audio/basic");
    mimeTypeMap.put("spc", "application/x-pkcs7-certificates");
    mimeTypeMap.put("spl", "application/futuresplash");
    mimeTypeMap.put("src", "application/x-wais-source");
    mimeTypeMap.put("sst", "application/vnd.ms-pkicertstore");
    mimeTypeMap.put("stl", "application/vnd.ms-pkistl");
    mimeTypeMap.put("stm", "text/html");
    mimeTypeMap.put("svg", "image/svg+xml");
    mimeTypeMap.put("sv4cpio", "application/x-sv4cpio");
    mimeTypeMap.put("sv4crc", "application/x-sv4crc");
    mimeTypeMap.put("swf", "application/x-shockwave-flash");
    mimeTypeMap.put("t", "application/x-troff");
    mimeTypeMap.put("tar", "application/x-tar");
    mimeTypeMap.put("tcl", "application/x-tcl");
    mimeTypeMap.put("tex", "application/x-tex");
    mimeTypeMap.put("texi", "application/x-texinfo");
    mimeTypeMap.put("texinfo", "application/x-texinfo");
    mimeTypeMap.put("tgz", "application/x-compressed");
    mimeTypeMap.put("tif", "image/tiff");
    mimeTypeMap.put("tiff", "image/tiff");
    mimeTypeMap.put("tr", "application/x-troff");
    mimeTypeMap.put("trm", "application/x-msterminal");
    mimeTypeMap.put("tsv", "text/tab-separated-values");
    mimeTypeMap.put("txt", "text/plain");
    mimeTypeMap.put("uls", "text/iuls");
    mimeTypeMap.put("ustar", "application/x-ustar");
    mimeTypeMap.put("vcf", "text/x-vcard");
    mimeTypeMap.put("vrml", "x-world/x-vrml");
    mimeTypeMap.put("wav", "audio/x-wav");
    mimeTypeMap.put("wcm", "application/vnd.ms-works");
    mimeTypeMap.put("wdb", "application/vnd.ms-works");
    mimeTypeMap.put("wks", "application/vnd.ms-works");
    mimeTypeMap.put("wmf", "application/x-msmetafile");
    mimeTypeMap.put("wps", "application/vnd.ms-works");
    mimeTypeMap.put("wri", "application/x-mswrite");
    mimeTypeMap.put("wrl", "x-world/x-vrml");
    mimeTypeMap.put("wrz", "x-world/x-vrml");
    mimeTypeMap.put("xaf", "x-world/x-vrml");
    mimeTypeMap.put("xbm", "image/x-xbitmap");
    mimeTypeMap.put("xla", "application/vnd.ms-excel");
    mimeTypeMap.put("xlc", "application/vnd.ms-excel");
    mimeTypeMap.put("xlm", "application/vnd.ms-excel");
    mimeTypeMap.put("xls", "application/vnd.ms-excel");
    mimeTypeMap.put("xlt", "application/vnd.ms-excel");
    mimeTypeMap.put("xlw", "application/vnd.ms-excel");
    mimeTypeMap.put("xof", "x-world/x-vrml");
    mimeTypeMap.put("xpm", "image/x-xpixmap");
    mimeTypeMap.put("xwd", "image/x-xwindowdump");
    mimeTypeMap.put("z", "application/x-compress");
    mimeTypeMap.put("zip", "application/zip");
    mimeTypeMap.put("3gp", "video/3gp");
    mimeTypeMap.put("3gpp", "video/3gpp");
    mimeTypeMap.put("3gpp2", "video/3gpp2");
  }
}
