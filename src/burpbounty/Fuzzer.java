/*
Copyright 2018 Eduardo Garcia Melia <wagiro@gmail.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package burpbounty;

import burp.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

public class Fuzzer{

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;
    String profileName;
    String issuename;
    String issuedetail;
    String issuebackground;
    String remediationdetail;
    String remediationbackground;
    String charstourlencode;
    int scanner;
    int matchtype;
    String issueseverity;
    String issueconfidence;
    boolean excludeHTTP;
    boolean onlyHTTP;
    boolean notresponse;
    boolean iscontenttype;
    boolean isresponsecode;
    boolean negativect;
    boolean negativerc;
    String contenttype;
    String responsecode;
    boolean casesensitive;
    boolean urlencode;
    Integer maxredirect;
    Integer redirtype;
    int payloadposition;
    String timeout;
    String contentLength;
    List<String> payloads;
    List<String> payloadsEncoded;
    List<String> payloadsenc;
    List<String> greps;
    List<String> encoders;
    JsonArray data;
    Gson gson;
    ProfilesProperties profile_property;
    List<Headers> headers;
    CollaboratorData burpCollaboratorData;
    List<String> variationAttributes;
    List<Integer> insertionPointType;
    Boolean pathDiscovery;
    String filename;
    JsonArray allprofiles;
    JsonArray active_profiles;
    BurpBountyExtension bbe;
    public Tags tagui;
    int startIndex;
    private ThreadPoolExecutor executor;

    public Fuzzer(BurpBountyExtension bbe, IBurpExtenderCallbacks callbacks, CollaboratorData burpCollaboratorData, String filename, JsonArray allprofiles, Tags tagui,ThreadPoolExecutor threadpool) {

        this.callbacks = callbacks;
        helpers = callbacks.getHelpers();
        this.burpCollaboratorData = burpCollaboratorData;
        gson = new Gson();
        this.filename = filename;
        this.allprofiles = allprofiles;
        this.bbe = bbe;
        this.tagui = tagui;
        tagui.setBurpBountyExtension(bbe);
        this.startIndex = tagui.getRowCount();
        tagui.clear_find_payloads();
        this.executor = threadpool;
    }

    public List<IScanIssue> runAScan(IHttpRequestResponse baseRequestResponse, IScannerInsertionPoint insertionPoint, JsonArray activeprofiles, String bchost) {

        /*if (helpers.analyzeResponse(baseRequestResponse.getResponse()) == null | helpers.analyzeRequest(baseRequestResponse.getRequest()) == null) {
            return null;
        }*/

        List<IScanIssue> issues = new ArrayList<>();
        IHttpService httpService = baseRequestResponse.getHttpService();
        List<Integer> responseCodes = new ArrayList<>(Arrays.asList(300, 301, 303, 302, 307, 308));
        int limitredirect = 30;
        String ParamName = insertionPoint.getInsertionPointName();
        AtomicLong threadNum = new AtomicLong(0);
        for (int i = 0; i < activeprofiles.size(); i++) {
            Object idata = activeprofiles.get(i);
            profile_property = gson.fromJson(idata.toString(), ProfilesProperties.class);
            insertionPointType = profile_property.getInsertionPointType() != null ? profile_property.getInsertionPointType() : new ArrayList(Arrays.asList(0));
            Integer v= insertionPoint.getInsertionPointType()&0xff;
            if(!insertionPointType.contains(v))
            {
                continue;
            }

            payloads = profile_property.getPayloads();
            greps = profile_property.getGreps();
            issuename = profile_property.getIssueName();
            issueseverity = profile_property.getIssueSeverity();
            issueconfidence = profile_property.getIssueConfidence();
            issuedetail = profile_property.getIssueDetail();
            issuebackground = profile_property.getIssueBackground();
            remediationdetail = profile_property.getRemediationDetail();
            remediationbackground = profile_property.getRemediationBackground();
            matchtype = profile_property.getMatchType();
            notresponse = profile_property.getNotResponse();
            casesensitive = profile_property.getCaseSensitive();
            encoders = profile_property.getEncoder();
            urlencode = profile_property.getUrlEncode();
            charstourlencode = profile_property.getCharsToUrlEncode();
            iscontenttype = profile_property.getIsContentType();
            isresponsecode = profile_property.getIsResponseCode();
            contenttype = profile_property.getContentType();
            responsecode = profile_property.getResponseCode();
            excludeHTTP = profile_property.getExcludeHTTP();
            onlyHTTP = profile_property.getOnlyHTTP();
            negativect = profile_property.getNegativeCT();
            negativerc = profile_property.getNegativeRC();
            maxredirect = profile_property.getMaxRedir();
            redirtype = profile_property.getRedirection();
            payloadposition = profile_property.getPayloadPosition();
            timeout = profile_property.getTime();
            contentLength = profile_property.getContentLength();
            headers = profile_property.getHeader() != null ? profile_property.getHeader() : new ArrayList();
            variationAttributes = profile_property.getVariationAttributes() != null ? profile_property.getVariationAttributes() : new ArrayList();
            pathDiscovery = profile_property.getPathDiscover();
            profileName = profile_property.getName();
            IScanIssue matches = null;
            GrepMatch gm = new GrepMatch(callbacks);

            //If encoders exist...
            if (!encoders.isEmpty()) {
                switch (matchtype) {
                    case 1:
                        payloadsEncoded = processPayload(payloads, encoders);
                        payloads = new ArrayList(payloadsEncoded);
                        break;
                    case 2:
                        payloadsEncoded = processPayload(payloads, encoders);
                        payloads = new ArrayList(payloadsEncoded);
                        break;
                    case 3:
                        payloadsEncoded = processPayload(payloads, encoders);
                        greps = new ArrayList();
                        for (String p : payloads) {
                            greps.add("true,Or," + p);
                        }
                        payloads = payloadsEncoded;
                        break;
                    case 4:
                        greps = new ArrayList();
                        payloadsEncoded = processPayload(payloads, encoders);
                        for (String p : payloads) {
                            greps.add("true,Or," + p);
                        }
                        payloads = new ArrayList(payloadsEncoded);
                        break;
                    default:
                        payloadsEncoded = processPayload(payloads, encoders);
                        payloads = new ArrayList(payloadsEncoded);
                        break;
                }

            } else {
                if (matchtype == 3) {
                    for (String p : payloads) {
                        greps.add("true,Or," + p);
                    }
                }
            }

            for (String payload : payloads) {
                if(Config.get_run_status()==true)
                {
                    callbacks.printOutput("Stop Task!");
                    return issues;
                }
                if (urlencode) {
                    payload = encodeTheseURL(payload, charstourlencode);
                }

                if (payloadposition == 2) {
                    String value = insertionPoint.getBaseValue();
                    payload = value.concat(payload);
                }

                if (!headers.isEmpty()) {
                    for (int x = 0; x < headers.size(); x++) {
                        if (headers.get(x).type.equals("Payload")) {
                            if (headers.get(x).regex.equals("String")) {
                                payload = payload.replace(headers.get(x).match, headers.get(x).replace);
                            } else {
                                payload = payload.replaceAll(headers.get(x).match, headers.get(x).replace);
                            }
                        }
                    }
                }

                if (payload.contains(" ")) {//for avoid space in payload
                    payload = payload.replace(" ", "%20");
                }

                // do somethings
                byte[] request = new BuildUnencodeRequest(helpers).buildUnencodedRequest(insertionPoint, helpers.stringToBytes(payload), headers, bchost);

                try {
                    if (this.executor.getQueue().size()> 100) {
                        //String vb = String.format("%d",this.executor.getQueue().size());
                        //callbacks.printError(vb);
                        Thread.sleep(1500);
                    }
                    this.executor.submit(new FuzzerThread(callbacks,tagui,request,baseRequestResponse,ParamName, payload,profileName));
                }catch (Exception ex) {
                    callbacks.printError(ex.getMessage());
                    break;
                }
            }
        }

        return issues;
    }


    public List<IScanIssue> runResPScan(IHttpRequestResponse baseRequestResponse, JsonArray passiveresprofiles, String bchost) throws Exception {

        List<IScanIssue> issues = new ArrayList<>();

        for (int i = 0; i < passiveresprofiles.size(); i++) {
            Object idata = passiveresprofiles.get(i);
            profile_property = gson.fromJson(idata.toString(), ProfilesProperties.class);
            greps = profile_property.getGreps();
            issuename = profile_property.getIssueName();
            issueseverity = profile_property.getIssueSeverity();
            issueconfidence = profile_property.getIssueConfidence();
            issuedetail = profile_property.getIssueDetail();
            issuebackground = profile_property.getIssueBackground();
            remediationdetail = profile_property.getRemediationDetail();
            remediationbackground = profile_property.getRemediationBackground();
            matchtype = profile_property.getMatchType();
            notresponse = profile_property.getNotResponse();
            casesensitive = profile_property.getCaseSensitive();
            iscontenttype = profile_property.getIsContentType();
            isresponsecode = profile_property.getIsResponseCode();
            contenttype = profile_property.getContentType();
            responsecode = profile_property.getResponseCode();
            excludeHTTP = profile_property.getExcludeHTTP();
            onlyHTTP = profile_property.getOnlyHTTP();
            negativect = profile_property.getNegativeCT();
            negativerc = profile_property.getNegativeRC();
            scanner = profile_property.getScanner();
            IResponseInfo r;
            GrepMatch gm = new GrepMatch(callbacks);
            IScanIssue matches = null;
            int grep_index = 0;
            ArrayList<ArrayList<String>> greps_final = new ArrayList<>(greps.size());

            if (baseRequestResponse == null) {
                break;
            }

            try {
                r = helpers.analyzeResponse(baseRequestResponse.getResponse());
            } catch (NullPointerException e) {
                callbacks.printError(e.getMessage());
                break;
            }

            //multiarray
            for (int index = 0; index < greps.size(); index++) {
                greps_final.add(new ArrayList());
            }
            Integer responseCode = new Integer(r.getStatusCode());

            for (String grep : greps) {
                if ((isresponsecode && !isResponseCode(responsecode, negativerc, responseCode)) || (iscontenttype && !isContentType(contenttype, negativect, r))) {
                    break;
                }

                String[] tokens = grep.split(",", 3);

                if (tokens.length > 1) {
                    if (tokens[0].equals("true")) {
                        if (tokens[1].equals("Or")) {
                            if (!tokens[2].equals("")) {
                                greps_final.get(grep_index).add(tokens[2]);
                                grep_index = grep_index + 1;
                            }
                        }
                    }
                } else {
                    if (!tokens[0].equals("")) {
                        greps_final.get(grep_index).add(tokens[0]);
                        grep_index = grep_index + 1;
                    }
                }
            }

            for (int x = 0; x < grep_index; x++) {
                if (!greps_final.get(x).isEmpty()) {
                    matches = gm.getResponseMatches(baseRequestResponse, "", greps_final.get(x), issuename, issuedetail.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), issuebackground.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), remediationdetail.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), remediationbackground.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), "", matchtype,
                            issueseverity, issueconfidence, notresponse, casesensitive, false, excludeHTTP, onlyHTTP);
                }

                try {
                    if (matches != null) {
                        issues.add(matches);
                        callbacks.addScanIssue(matches);
                        break;
                    }
                } catch (Exception e) {
                    callbacks.printError(e.getMessage());
                    continue;
                }
            }
        }
        return issues;
    }

    public List<IScanIssue> runReqPScan(IHttpRequestResponse baseRequestResponse, JsonArray passivereqprofiles, String bchost) throws Exception {

        List<IScanIssue> issues = new ArrayList<>();
        Object[] matches = null;

        for (int i = 0; i < passivereqprofiles.size(); i++) {
            Object idata = passivereqprofiles.get(i);
            profile_property = gson.fromJson(idata.toString(), ProfilesProperties.class);
            greps = profile_property.getGreps();
            issuename = profile_property.getIssueName();
            issueseverity = profile_property.getIssueSeverity();
            issueconfidence = profile_property.getIssueConfidence();
            issuedetail = profile_property.getIssueDetail();
            issuebackground = profile_property.getIssueBackground();
            remediationdetail = profile_property.getRemediationDetail();
            remediationbackground = profile_property.getRemediationBackground();
            matchtype = profile_property.getMatchType();
            notresponse = profile_property.getNotResponse();
            casesensitive = profile_property.getCaseSensitive();
            scanner = profile_property.getScanner();

            int grep_index = 0;
            ArrayList<ArrayList<String>> greps_final = new ArrayList<>(greps.size());
            GrepMatch gm = new GrepMatch(callbacks);

            if (baseRequestResponse.getRequest() == null) {
                break;
            }

            //multiarray
            for (int index = 0; index < greps.size(); index++) {
                greps_final.add(new ArrayList());
            }

            for (String grep : greps) {

                String[] tokens = grep.split(",", 5);

                if (tokens.length > 1) {
                    if (tokens[0].equals("true")) {
                        if (tokens[1].equals("Or")) {
                            if (!tokens[4].equals("")) {
                                greps_final.get(grep_index).add(tokens[2] + "," + tokens[4]);
                                grep_index = grep_index + 1;
                            }
                        }
                    }
                } else {
                    if (!tokens[0].equals("")) {
                        greps_final.get(grep_index).add("All Request" + "," + tokens[0]);
                        grep_index = grep_index + 1;
                    }
                }
            }

            for (int x = 0; x < grep_index; x++) {
                if (!greps_final.get(x).isEmpty()) {
                    matches = gm.getRequestMatches(baseRequestResponse, greps_final.get(x), issuename, issuedetail.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), issuebackground.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), remediationdetail.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), remediationbackground.replace("<grep>", helpers.urlEncode(greps_final.get(x).toString())), matchtype,
                            issueseverity, issueconfidence, casesensitive, notresponse);

                    try {
                        if (matches[0] != null) {
                            IScanIssue vv = (IScanIssue) matches[0];
                            issues.add(vv);
                            break;
                        }
                    } catch (Exception e) {
                        callbacks.printError(e.getMessage());
                        continue;
                    }
                }
            }
        }
        return issues;
    }

    public Boolean isUrlExtension(String urlextension, boolean NegativeUrlExtension, IRequestInfo r) {

        if (urlextension.isEmpty()) {
            return false;
        }

        URL url = r.getUrl();
        List<String> extensions = Arrays.asList(urlextension.toLowerCase().split(","));
        String uri = url.getPath().toLowerCase();
        String ext;

        if (uri.contains(".")) {
            ext = uri.substring(uri.lastIndexOf(".") + 1);
        } else {
            return true;
        }

        if (extensions.contains(ext)) {
            if (!NegativeUrlExtension) {
                return true;
            } else {
                return false;
            }
        } else {
            if (NegativeUrlExtension) {
                return true;
            } else {
                return false;
            }
        }
    }

    public URL getRedirection(IHttpRequestResponse response, IHttpService httpService) {

        try {
            URL url = getLocation(httpService, response);

            if (url.toString().contains("burpcollaborator.net")) {
                return url;
            } else if (redirtype == 2) {
                if (url.getHost().contains(httpService.getHost())) {
                    return url;
                }
            } else if (redirtype == 3) {
                boolean isurl = callbacks.isInScope(url);
                if (isurl) {
                    return url;
                }
            } else if (redirtype == 4) {
                return url;
            } else {
                return null;
            }

            return null;
        } catch (NullPointerException | ArrayIndexOutOfBoundsException ex) {
            callbacks.printError(ex.getMessage());
            return null;
        }
    }

    public URL getLocation(IHttpService httpService, IHttpRequestResponse response) {
        String[] host = null;
        String Location = "";
        URL url;

        try {
            IResponseInfo response_info = helpers.analyzeResponse(response.getResponse());

            for (String header : response_info.getHeaders()) {
                if (header.toUpperCase().contains("LOCATION")) {
                    host = header.split("\\s+");
                    Location = host[1];
                }
            }

            if (Location.startsWith("http://") || Location.startsWith("https://")) {
                url = new URL(Location);
                return url;
            } else if (Location.startsWith("/")) {
                url = new URL(httpService.getProtocol() + "://" + httpService.getHost() + Location);
                return url;
            } else {
                url = new URL(httpService.getProtocol() + "://" + httpService.getHost() + "/" + Location);
                return url;
            }

        } catch (MalformedURLException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
            return null;
        }
    }

    public byte[] getMatchAndReplace(List<Headers> headers, byte[] checkRequest, String payload, String bchost) {
        String tempRequest = helpers.bytesToString(checkRequest);

        if (!headers.isEmpty()) {
            for (int x = 0; x < headers.size(); x++) {
                String replace = headers.get(x).replace;
                if (headers.get(x).type.equals("Request")) {
                    if (headers.get(x).regex.equals("String")) {
                        if (replace.contains("{PAYLOAD}")) {
                            replace = replace.replace("{PAYLOAD}", payload);
                        }
                        if (replace.contains("{BC}")) {
                            replace = replace.replace("{BC}", bchost);
                        }
                        if (headers.get(x).match.isEmpty()) {
                            tempRequest = tempRequest.replace("\r\n\r\n", "\r\n" + replace + "\r\n\r\n");
                        } else {
                            tempRequest = tempRequest.replace(headers.get(x).match, replace);
                        }
                    } else {
                        if (replace.contains("{PAYLOAD}")) {
                            replace = replace.replaceAll("\\{PAYLOAD\\}", payload);
                        }
                        if (replace.contains("{BC}")) {
                            replace = replace.replaceAll("\\{BC\\}", bchost);
                        }
                        if (headers.get(x).match.isEmpty()) {
                            tempRequest = tempRequest.replaceAll("\\r\\n\\r\\n", "\r\n" + replace + "\r\n\r\n");
                        } else {
                            tempRequest = tempRequest.replaceAll(headers.get(x).match, replace);
                        }
                    }

                }
            }
        }
        return helpers.stringToBytes(tempRequest);
    }

    public int getContentLength(IHttpRequestResponse response) {
        IResponseInfo response_info;
        try {
            response_info = helpers.analyzeResponse(response.getResponse());
        } catch (NullPointerException ex) {
            callbacks.printError("GenericScan line 1279: " + ex.getMessage());
            return 0;
        }

        int ContentLength = 0;

        for (String headers : response_info.getHeaders()) {
            if (headers.toUpperCase().contains("CONTENT-LENGTH:")) {
                ContentLength = Integer.parseInt(headers.split("\\s+")[1]);
            }
        }
        return ContentLength;
    }

    public boolean isResponseCode(String responsecodes, boolean negativerc, Integer responsecode) {

        if (responsecodes.isEmpty()) {
            return false;
        }

        List<String> items = Arrays.asList(responsecodes.split("\\s*,\\s*"));
        String code = Integer.toString(responsecode);

        if (items.contains(code)) {
            if (!negativerc) {
                return true;
            } else {
                return false;
            }
        } else {
            if (negativerc) {
                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isContentType(String contenttype, boolean negativect, IResponseInfo r) {
        List<String> HEADERS = r.getHeaders();

        if (contenttype.isEmpty()) {
            return false;
        }

        List<String> items = Arrays.asList(contenttype.toUpperCase().split("\\s*,\\s*"));

        for (String header : HEADERS) {
            if (header.toUpperCase().contains("CONTENT-TYPE")) {
                String content_type = header.substring(header.lastIndexOf(":") + 2).split(";")[0].toUpperCase();
                if (items.contains(content_type)) {
                    if (negativect) {
                        return false;
                    }
                } else {
                    if (!negativect) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public List processPayload(List<String> payloads, List<String> encoders) {
        List pay = new ArrayList();
        for (String payload : payloads) {

            for (String p : encoders) {
                switch (p) {
                    case "URL-encode key characters":
                        payload = encodeKeyURL(payload);
                        break;
                    case "URL-encode all characters":
                        payload = encodeURL(payload);
                        break;
                    case "URL-encode all characters (Unicode)":
                        payload = encodeUnicodeURL(payload);
                        break;
                    case "HTML-encode key characters":
                        payload = encodeKeyHTML(payload);
                        break;
                    case "HTML-encode all characters":
                        payload = encodeHTML(payload);
                        break;
                    case "Base64-encode":
                        payload = helpers.base64Encode(payload);
                    default:
                        break;
                }
            }
            pay.add(payload);
        }

        return pay;
    }

    public static String encodeURL(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            out.append("%" + Integer.toHexString((int) c));
        }
        return out.toString();
    }

    public static String encodeUnicodeURL(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            out.append("%u00" + Integer.toHexString((int) c));
        }
        return out.toString();
    }

    public static String encodeHTML(String s) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            out.append("&#x" + Integer.toHexString((int) c) + ";");
        }
        return out.toString();
    }

    public static String encodeKeyHTML(String s) {
        StringBuffer out = new StringBuffer();
        String key = "\\<\\(\\[\\\\\\^\\-\\=\\$\\!\\|\\]\\)\\?\\*\\+\\.\\>]\\&\\%\\:\\@ ";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (key.contains(s.substring(i, i + 1))) {
                out.append("&#x" + Integer.toHexString((int) c) + ";");
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String encodeKeyURL(String s) {
        StringBuffer out = new StringBuffer();
        String key = "\\<\\(\\[\\\\\\^\\-\\=\\$\\!\\|\\]\\)\\?\\*\\+\\.\\>]\\&\\%\\:\\@ ";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (key.contains(s.substring(i, i + 1))) {
                out.append("%" + Integer.toHexString((int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static String encodeTheseURL(String s, String characters) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (characters.indexOf(c) >= 0) {
                out.append("%" + Integer.toHexString((int) c));
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
