package com.infa.taskterminator;

/**
 *
 * @author Mallikarjun Reddy
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.*;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Killer extends Thread {

    public static String token = null;
    public static String UiState = null;
    public static String ServiceState = null;
    public static String ConsoleState = null;
    public static String surl = null;
    public static String host = null;
    public static String id = null;
    public static String path = null;
    public static String json2 = null;
    public static String platform = null;
    public static String upgradeStatus = null;
    public static int waittime = 1;
    public static int maxWait = 1;
    public static int DST;
    public static String debug = null;
    public static String tokens[];

    public String getToken() throws JSONException {

        String line;
        StringBuffer jsonString = new StringBuffer();
        try {
            // http://stackoverflow.com/questions/15570656/how-to-send-request-payload-to-rest-api-in-java
            String urlr = host + "/ma/api/v2/user/login";
            URL url = new URL(urlr);
            String payload = json2;
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json;");
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                writer.write(payload);
            }
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getResponseCode() / 100 == 2
                    ? connection.getInputStream() : connection.getErrorStream()));
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        String s = jsonString.toString();
        JSONObject jsonObject = new JSONObject(s); // http://stackoverflow.com/questions/16574482/decoding-json-string-in-java
        String id = jsonObject.getString("icSessionId");
        if (id != null) {
        } else {
            JSONObject newJSON = jsonObject.getJSONObject("error");
            if (newJSON != null) {
                jsonObject = new JSONObject(newJSON.toString());
                System.out.println("ERROR: " + jsonObject.getString("message"));
                System.exit(0);
            }
        }
        surl = jsonObject.getString("serverUrl");
        return id;
    }

    public String getActivityMonitor(String t) throws Exception {
        String line;
        String status = null;
        StringBuffer jsonString = new StringBuffer();
        try {

            String urlr = surl + "/api/v2/activity/activityMonitor?details=true";
            URL url = new URL(urlr); // "https://app2.informaticacloud.com/saas/api/v2/agent/0001MT08000000000090"
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("icSessionId", token);
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getResponseCode() / 100 == 2
                    ? connection.getInputStream() : connection.getErrorStream()));
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            connection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
        String s = jsonString.toString();
        String s2 = "{ array:" + s + "}";
        JSONObject jsonObject = new JSONObject(s2);
        JSONArray entry = (JSONArray) jsonObject.get("array");// 2

        for (int i = 0; i < entry.length(); i++) {
            JSONObject jsonObject1 = (JSONObject) entry.get(i);// 3
            ZonedDateTime currentDate = ZonedDateTime.now(ZoneOffset.UTC);
            String input = jsonObject1.getString("startTime");
            try {
                ZonedDateTime formatter = ZonedDateTime.parse(input);
                long diffInMinutes = java.time.Duration.between(formatter, currentDate).toMinutes();
                if (DST == 1) {
                    if (diffInMinutes - 240 >= maxWait) // 4hrs*60mins
                    {
                        status = kill(jsonObject1.getString("taskId"), jsonObject1.getString("type"),
                                jsonObject1.getString("taskName"));
                    }
                }
                if (DST == 0) {
                    if (diffInMinutes - 300 >= maxWait) // 5hrs*60mins
                    {
                        status = kill(jsonObject1.getString("taskId"), jsonObject1.getString("type"),
                                jsonObject1.getString("taskName"));
                    }
                }
            } catch (DateTimeParseException exc) {
                System.out.printf("%s is not parsable!%n", input);
                throw exc;
            }
        }
        if (status == null) {
            status = "No Jobes were killed";
        }
        return status;
    }

    public String kill(String taskID, String taskType, String tname) throws Exception {
        String line;
        StringBuffer jsonString = new StringBuffer();
        try {
            // "https://app2.informaticacloud.com/saas/api/v2/agent/0001MT08000000000090"
            JSONObject obj = new JSONObject();
            obj.put("@type", "job");
            obj.put("taskId", taskID);
            obj.put("taskType", taskType);
            String payload = obj.toString();
            String urlr = surl + "/api/v2/job/stop";
            URL url = new URL(urlr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json;");
            connection.setRequestProperty("icSessionId", token);
            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                writer.write(payload);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getResponseCode() / 100 == 2
                    ? connection.getInputStream() : connection.getErrorStream()))) {
                while ((line = br.readLine()) != null) {
                    jsonString.append(line);
                }
            }
            connection.disconnect();
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e.getMessage());
        }
        String s = jsonString.toString();
        if("1".equals(debug)){
         System.out.println(s);
        }
        JSONObject jsonObject = new JSONObject(s);
        String status = jsonObject.getString("@type");
        if (status.equals("success")) {
            System.out.println(status + "lly" + " " + "killed " + tname);
        } else {
            System.out.println(status);
        }
        if (status.equals("error")) {
            System.out.println(jsonObject.getString("description"));
        }
        return status;
    }

    public void logout() throws JSONException {

        try {
            String urlr = surl + "/api/v2/user/logout";
            URL url = new URL(urlr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Content-Type", "application/json;");
            connection.setRequestProperty("icSessionId", token);
            int statusCode = connection.getResponseCode();
            if ("1".equals(debug)) {
                System.out.println("logged out (api): " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void readInput() {
        try {
            String uname = null;
            String pass = null;
            File fXmlFile = new File("info.xml");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            NodeList nList = doc.getElementsByTagName("info");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    uname = eElement.getElementsByTagName("uname").item(0).getTextContent();
                    pass = eElement.getElementsByTagName("pass").item(0).getTextContent();
                    host = eElement.getElementsByTagName("host").item(0).getTextContent();
                    String dst = eElement.getElementsByTagName("DST").item(0).getTextContent();
                    String wtime = eElement.getElementsByTagName("pollInterval").item(0).getTextContent();
                    String mWait = eElement.getElementsByTagName("maxWait").item(0).getTextContent();
                    waittime = Integer.parseInt(wtime);
                    maxWait = Integer.parseInt(mWait);
                    DST = Integer.parseInt(dst);
                    debug = eElement.getElementsByTagName("debug").item(0).getTextContent();
                }
                JSONObject obj = new JSONObject();
                // obj.put("@type","login");
                obj.put("username", uname);
                obj.put("password", pass);
                json2 = obj.toString();
            }
        } catch (IOException | NumberFormatException | ParserConfigurationException | JSONException | DOMException | SAXException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public static void main(String[] args) throws Exception {
        Killer us = new Killer();
        us.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Killer us = new Killer();
                us.readInput();
                token = us.getToken();
                if ("1".equals(debug)) {
                    System.out.println("icSessionId " + token);
                    System.out.println("ServerUlr " + surl);
                }
                System.out.println("Overall_Status : " + us.getActivityMonitor(token));
                us.logout();
                Thread.sleep(60000 * waittime);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
