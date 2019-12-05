package fr.edb.geocodeur;

import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Classe illustrant l'utilisation du HttpClient pour faire appel à un service
 * de Géocodage d'adresses
 * 
 * @author patrice 
 * 
 */
public class GeoCodeur {
	private final static boolean USE_PROXY=false;
	
	/**
	 * Appel du service de GeoCodage d'OpenStreetMap
	 * 
	 * @param adresse
	 *            - Adresse dont on veut les coordonnees
	 * @return flux JSON avec entre autres les coordonnees. Retourne [] si l'adresse n'a pas ete trouvee
	 */
	public String geoCode(String adresse) throws Exception {
		String res = null;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		try {
			if (USE_PROXY) {
				HttpHost proxy = new HttpHost("127.0.0.1", 8080, "http");
				httpclient.getParams().setParameter(
						ConnRoutePNames.DEFAULT_PROXY, proxy);
			}
			// URL du service à appeler avec paramètres associés
			HttpGet req = new HttpGet(
					"http://nominatim.openstreetmap.org/search?q="
							+ URLEncoder.encode(adresse, "UTF-8")
					+"&format=json&polygon=1&addressdetails=1");

			System.out.println("Envoi requete vers: " + req.getURI());
			// Exécution requ�te
			HttpResponse rsp = httpclient.execute(req);
			// Lecture r�ponse
			HttpEntity entity = rsp.getEntity();

			System.out.println("----------------------------------------");
			System.out.println(rsp.getStatusLine());

			if (entity != null) {
				res = EntityUtils.toString(entity);
			}
		} finally {
			// When HttpClient instance is no longer needed,
			// shut down the connection manager to ensure
			// immediate deallocation of all system resources
			httpclient.getConnectionManager().shutdown();
		}
		return res;

	}

	/**
	 * Methode VERY Quick & Dirty pour recuperer les coordonnees du flux de retour
	 * d'OpenSteetMap et calculer une URL pour GoogleMaps centree sur cette
	 * adresse
	 * @param s
	 *            - flux XML retourn� par Yahoo
	 * @return URL Google Maps
	 */
	public String calcGoogleMapsURL(String s) {
		String lat="";
		String lon="";
		int lat1 = s.indexOf("lat\":\"")+6;
		int lat2 = s.indexOf("\"",lat1);
		lat=s.substring(lat1,lat2);
		int lon1 = s.indexOf("lon\":\"")+6;
		int lon2 = s.indexOf("\"",lon1);
		lon=s.substring(lon1,lon2);
				
		//String url = "http://maps.google.fr/maps?hl=fr&sll=" + lat + "," + lon
		//		+ "&t=h&z=16";
		String url = "http://maps.google.fr?ll=" + lat + "," + lon;
		return url;
	}

	/**
	 * Point d'Entrée
	 */
	public static void main(String[] args)throws Exception {
		GeoCodeur gc = new GeoCodeur();
		// Adresse à GeoCoder
		String adresse = "3 rue de Brest, 35000 Rennes, France";
		String res = gc.geoCode(adresse);        
		System.out.println("coordonnées: " + res);
        if("[]".equals(res)){
				System.out.println("Adresse non trouvée!");
				return;
			}
			String url = gc.calcGoogleMapsURL(res);
			System.out.println("URL Google Maps: " + url);
			//lancement du navigateur
			String navigateur = "/usr/bin/firefox ";
		if(System.getProperty("os.name").startsWith("Windows")){
			navigateur = "C:\\Program Files\\Internet Explorer\\iexplore.exe ";
		}
		Process p=Runtime.getRuntime().exec(navigateur+url);
	}

}
