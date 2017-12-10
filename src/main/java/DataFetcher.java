import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created by danielyoung on 2017-10-14.
 */
public class DataFetcher {


    private static final String OFFSET_NAME = "offset";
    private static Gson gson;
    private static int LIMIT = 1000;
    private static int totalCount = 200001;
    private static int offset = 0;
    private static int requests = 0;
    private static PrintStream fileStream;

    public static void main(String[] args) throws InterruptedException {

        gson = new Gson()   ;
        Properties props = new Properties();
        InputStream is = null;
        OutputStream output = null;

        try {
            is = new FileInputStream("/Users/danielyoung/Documents/projects/code/rave/Booli-data-fetcher/src/main/resources/config.properties");

            output = new FileOutputStream("/Users/danielyoung/Documents/projects/code/rave/Booli-data-fetcher/src/main/resources/config2.properties");
            // load a properties file
            fileStream = new PrintStream(new File("file.txt"));
            String random = "”f4508htyuk98fe4f”";
            long a = System.currentTimeMillis() / 1000L;
            props.load(is);
            props.setProperty("hash", DigestUtils.sha1Hex(props.getProperty("callerId") + System.currentTimeMillis() / 1000L + props.getProperty("privateKey") + random));
            props.setProperty("time", String.valueOf(a));
            props.setProperty("random", random);
            props.store(output, null);
            String url = getReadyMadeURL(props);
            if (offset == 0) {
                runquery(url, offset);
                offset = offset + 1000;
            }

            while (totalCount - (offset + 1000) > 0 ){

                offset = offset + 1000;
                System.out.print(offset);
                TimeUnit.MILLISECONDS.sleep(1000);
                runquery(url, offset);

            }
            props.setProperty(OFFSET_NAME, String.valueOf(offset));
            props.store(output, null);
           // System.out.print(url);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private static void runquery(String url, int offset) throws IOException {

        url = addParams(url);
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestProperty(OFFSET_NAME, String.valueOf(offset));
        con.setRequestProperty("limit", String.valueOf(LIMIT));


        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        Map<String, Object> jsonMap;
        Gson gson = new Gson();
        Type outputType = new TypeToken<Map<String, Object>>(){}.getType();

        JsonElement element = gson.fromJson (response.toString(), JsonElement.class);
        JsonObject jsonObj = element.getAsJsonObject();
        JsonArray arrayen = jsonObj.getAsJsonArray("sold");
        for (JsonElement obja : arrayen) {
        printObjectsToLog((JsonObject) obja);

        }
        jsonMap = gson.fromJson(response.toString(), outputType);
        if (jsonMap.get("totalCount") instanceof Double) {
            totalCount = ((Double) jsonMap.get("totalCount")).intValue();
        }

}


    private static String addParams(String url) {
        return url + "&limit="+ LIMIT + "&offset=" + offset;

    }

    private static void printObjectsToLog(JsonObject obj) throws IOException {


        fileStream.println(obj.toString());
        //Files.write(Paths.get("sold.txt"), obj.toString().getBytes(), StandardOpenOption.APPEND);


    }


    private static String getReadyMadeURL(Properties props) {
    String url = props.getProperty("sold-url") + "q=stockholm" + "&callerId=" + props.getProperty("callerId") + "&time=" + props.getProperty("time") + "&unique=" +
             props.getProperty("random") + "&hash=" + props.getProperty("hash");

    return url;
    }
}
