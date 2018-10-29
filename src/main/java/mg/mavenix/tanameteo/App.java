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

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

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
        InputStream icon = null;
        JSONParser parser = new JSONParser();

        try(InputStream in = new URL(url).openStream()) {

            try {
                Object obj = parser.parse(IOUtils.toString(in));
                JSONObject jsonObject = (JSONObject) obj;

                JSONObject forecast = (JSONObject) jsonObject.get("forecast");
                JSONObject simpleforecast = (JSONObject) forecast.get("simpleforecast");
                JSONArray forecastday = (JSONArray) simpleforecast.get("forecastday");

                Long period;

                Iterator<JSONObject> iter = forecastday.iterator();
                while (iter.hasNext()) {
                    forecast = iter.next();

                    period = (Long) forecast.get("period");
                    //on ne prend que celui de demain
                    if (period.intValue() != 2) {
                        continue;
                    }

                    StringBuilder text = new StringBuilder();

                    //date
                    JSONObject date = (JSONObject) forecast.get("date");
                    text.append(date.get("day"));
                    text.append(".");
                    text.append(date.get("month"));
                    text.append(".");
                    text.append(date.get("year"));
                    text.append(": ");

                    //condition
                    text.append((String) forecast.get("conditions"));
                    text.append("; ");

                    //temp
                    JSONObject low = (JSONObject) forecast.get("low");
                    text.append((String) low.get("celsius"));

                    text.append("°C à ");

                    JSONObject high = (JSONObject) forecast.get("high");
                    text.append((String) high.get("celsius"));
                    text.append("°C;");

                    //PoP
                    text.append(" PoP: ");
                    text.append(forecast.get("pop"));
                    text.append("%;");

                    //Vents
                    text.append(" Vent: ~");
                    JSONObject wind = (JSONObject) forecast.get("avewind");
                    text.append(wind.get("kph"));
                    text.append("km/h;");

                    //Humidity
                    text.append(" Humidité: ~");
                    text.append(forecast.get("avehumidity"));
                    text.append("%");
                    
                    //hashtags
                    text.append(" #Antananarivo #Madagascar #Madagasikara #meteo");
                    
                    if(text.length() < 130) {
                        text.append(" #weather");
                    }

                    LOGGER.info(text.toString());

                    String icon_url = (String) forecast.get("icon_url");
                    icon = new URL(icon_url).openStream();
                    twitter.updateProfileImage(icon);              
                    Status status = twitter.updateStatus(text.toString());
                    break;
                }

            } catch (TwitterException | ParseException ex) {
                LOGGER.log(Level.SEVERE, ex.getMessage());
            } finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(icon);
            }

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage());
        }

    }
}
