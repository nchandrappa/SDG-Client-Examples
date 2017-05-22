package io.pivotal.GemFire.Demo.controller;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.SelectResults;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {


	@javax.annotation.Resource(name="Customer")
	Region<Object, PdxInstance> customerRegion;

	@Autowired
	ClientCache cache;

	@RequestMapping("/")
	public String home() {
		return "Customer Search Service -- Available APIs: <br/>"
				+ "<br/>"
				+ "GET /load    	               - load customer info <br/>"
//				+ "GET /loadall    	               - load customer info in batches <br/>"
				+ "GET /query                      - query customer info <br/>";
	}

	@RequestMapping(method = RequestMethod.GET, path = "/load")
	@ResponseBody
	public String loadCache() throws Exception {

		BufferedReader reader = null;
		Resource resource = new ClassPathResource("data/Customer.json");
		try {
            	reader = new BufferedReader(new FileReader(resource.getFile()));
            	String line = null;
	            while ((line = reader.readLine()) != null) {
	            	System.out.println(line);
	            	PdxInstance jsonValue = JSONFormatter.fromJSON(line);
	            	customerRegion.put(jsonValue.getField("accountNumber"), jsonValue);
	            }
		} catch (IOException exception) {

		} finally {
			reader.close();
		}

		return "customer data successfully saved into cache";
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(method = RequestMethod.GET, path = "/query")
	@ResponseBody
	public String queryCache() {

		String queryString = "SELECT accountNumber, firstName, lastName FROM /Customer WHERE gender = 'M'";
		SelectResults<PdxInstance> results = (SelectResults <PdxInstance>)doQuery(queryString);
		return results.toString();

	}

	SelectResults<?> doQuery (String queryString) {

		// Get QueryService from Cache.
		QueryService qs = cache.getQueryService();

		// Create the Query Object.
		Query q = qs.newQuery(queryString);

		SelectResults<?> results = null;
		try {
			// Execute Query locally. Returns results set.
			results = (SelectResults<?>)q.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

}
