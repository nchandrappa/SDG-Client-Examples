package io.pivotal.gemfire.demo;

import java.util.Properties;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.client.ClientRegionFactory;
import org.apache.geode.cache.client.ClientRegionShortcut;
import org.apache.geode.pdx.JSONFormatter;
import org.apache.geode.pdx.PdxInstance;
import org.apache.geode.pdx.ReflectionBasedAutoSerializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;


@Configuration
@SpringBootApplication
public class BasicClientApplication implements CommandLineRunner {


	public static void main(String[] args) {
		SpringApplication.run(BasicClientApplication.class, args);
	}

	Properties gemfireProperties() {
		Properties gemfireProperties = new Properties();
		gemfireProperties.setProperty("log-level", "config");
		return gemfireProperties;
	}

	@Override
	public void run(String... args) throws Exception {

		ClientCache gemfireCache = new ClientCacheFactory(gemfireProperties())
                .addPoolLocator("localhost", 10334)
                .setPdxSerializer(new ReflectionBasedAutoSerializer(".*"))
                .setPdxReadSerialized(false).create();

		ClientRegionFactory<String, PdxInstance> regionFactory
			= gemfireCache.createClientRegionFactory(ClientRegionShortcut.PROXY);

		Region<String, PdxInstance> customerRegion = regionFactory.create("Customer");

		String value = "{\"firstName\" : \"Allice\", \"lastName\" : \"Speed\"}";
		customerRegion.put("1", JSONFormatter.fromJSON(value));

		System.out.println("Retrieved Customer Data");
		System.out.println(customerRegion.get("1"));

	}
}
