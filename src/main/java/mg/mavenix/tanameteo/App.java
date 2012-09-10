package mg.mavenix.tanameteo;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Author: Thierry Randrianiriana <randrianiriana@gmail.com>
 * Date: 2012
 * Description: Tweet Antananarivo/Madagascar weather to a twitter account @MeteoTana
 * License: http://www.gnu.org/licenses/gpl.html
 */
public class App {

    public static void main(String[] args) {

        String consumerKey = "xxxxxxxxxxxxxxxxxxxxxxx";
        String consumerSecret = "xxxxxxxxxxxxxxxxxxxxxxx";
        String accessToken = "xxxxxxxxxxxxxxxxxxxxxxx";
        String accessTokenSecret = "xxxxxxxxxxxxxxxxxxxxxxx";
        String wuKey = "xxxxxxxxxxxxxxxxxxxxxxx";
        
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret);

        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();

        String url = "http://api.wunderground.com/api/"+wuKey+"/geolookup/conditions/forecast/lang:FR/q/MG/Antananarivo.json";
        InputStream in = null;
        InputStream icon = null;
        JSONParser parser = new JSONParser();

        try {
            in = new URL(url).openStream();
            try {
                Object obj = parser.parse(IOUtils.toString(in));
                JSONObject jsonObject = (JSONObject) obj;

                JSONObject o1 = (JSONObject) jsonObject.get("forecast");
                JSONObject o2 = (JSONObject) o1.get("simpleforecast");
                JSONArray o3 = (JSONArray) o2.get("forecastday");

                Long period;

                Iterator<JSONObject> iter = o3.iterator();
                while (iter.hasNext()) {
                    o1 = iter.next();

                    period = (Long) o1.get("period");
                    //on ne prend que celui de demain
                    if (period.intValue() != 2) {
                        continue;
                    }

                    StringBuilder text = new StringBuilder();

                    //date
                    JSONObject date = (JSONObject) o1.get("date");                    
                    text.append(date.get("day"));
                    text.append(".");
                    text.append(date.get("month"));
                    text.append(".");
                    text.append(date.get("year"));
                    text.append(": ");

                    //condition
                    text.append((String) o1.get("conditions"));
                    text.append("; ");

                    //temp
                    JSONObject low = (JSONObject) o1.get("low");
                    text.append((String) low.get("celsius"));

                    text.append("°C à ");

                    JSONObject high = (JSONObject) o1.get("high");
                    text.append((String) high.get("celsius"));
                    text.append("°C;");

                    //PoP
                    text.append(" PoP: ");
                    text.append(o1.get("pop"));
                    text.append("%;");

                    //Vents
                    text.append(" Vent: ~");
                    JSONObject wind = (JSONObject) o1.get("avewind");
                    text.append(wind.get("kph"));
                    text.append("km/h;");

                    //Humidity
                    text.append(" Humidité: ~");
                    text.append(o1.get("avehumidity"));
                    text.append("%");
                    
                    //hashtags
                    text.append(" #Antananarivo #Madagascar #Madagasikara #meteo");
                    
                    if(text.length() < 130) {
                        text.append(" #weather");
                    }

                    System.out.println(text.toString());

                    String icon_url = (String) o1.get("icon_url");
                    icon = new URL(icon_url).openStream();
                    twitter.updateProfileImage(icon);              
                    Status status = twitter.updateStatus(text.toString());
                    break;
                }

            } catch (TwitterException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(icon);
            }


        } catch (IOException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}
