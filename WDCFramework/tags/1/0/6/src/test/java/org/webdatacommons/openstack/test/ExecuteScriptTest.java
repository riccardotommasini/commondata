package org.webdatacommons.openstack.test;

import static org.jclouds.scriptbuilder.domain.Statements.exec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.options.RunScriptOptions;
import org.jclouds.compute.predicates.NodePredicates;
import org.jclouds.compute.reference.ComputeServiceConstants;
import org.jclouds.scriptbuilder.ScriptBuilder;
import org.jclouds.scriptbuilder.domain.OsFamily;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.jclouds.Constants;
import org.jclouds.openstack.keystone.v2_0.config.CredentialTypes;
import org.jclouds.openstack.keystone.v2_0.config.KeystoneProperties;
import org.junit.Test;
import org.webdatacommons.framework.processor.ProcessingNode;
import org.webdatacommons.structureddata.util.Statistics;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Module;


public class ExecuteScriptTest extends ProcessingNode{
	
	ComputeService client;
	
	String script = new ScriptBuilder()
            .addStatement(exec("sudo apt-get install -y python-software-properties debconf-utils"))
            .addStatement(exec("sudo add-apt-repository -y ppa:webupd8team/java"))
            .addStatement(exec("sudo apt-get update"))
            .addStatement(exec("echo \"oracle-java8-installer shared/accepted-oracle-license-v1-1 select true\" | sudo debconf-set-selections"))
            .addStatement(exec("sudo apt-get install -y oracle-java8-installer htop"))
            .addStatement(exec("java -Xmx1g -jar /home/ubuntu/"+getOrCry("jarName")+" > /tmp/start.log 2> /tmp/start_errors.log"))
            .render(OsFamily.UNIX);

	@Test
	public void dataStatTest() throws Exception {
		
		Iterable<Module> modules = ImmutableSet.<Module> of(new SshjSshClientModule());
		
		String provider = getOrCry("provider");
		 String identity = getOrCry("identity"); // tenantName:userName
		 String credential = getOrCry("credential");
		 
		 Properties overrides = new Properties();
		    overrides.setProperty(Constants.PROPERTY_CONNECTION_TIMEOUT, "0");
		    overrides.setProperty(Constants.PROPERTY_SO_TIMEOUT, "0");
	
		ComputeServiceContext context = ContextBuilder.newBuilder(provider)
              .endpoint(getOrCry("endpoint"))
              .credentials(identity, credential)
              .modules(modules).overrides(overrides)
              .buildApi(ComputeServiceContext.class);
             
		             
		
		Set<String> workerIDs = new HashSet<String>();
		workerIDs.add(getOrCry("zone")+"/"+"b9b2fde2-9035-4dc2-aeb0-7af3e78dfa3d");
		workerIDs.add(getOrCry("zone")+"/"+"07bd7c15-8ba2-4b8d-bd88-58731eed9f3f");
		
		client = context.getComputeService();
		
		
		
		RunScriptOptions options = RunScriptOptions.Builder
	            .blockOnComplete(true)
	            .overrideLoginUser("ubuntu")
	            .overrideLoginPrivateKey(getKeyFromFile())
	            .wrapInInitScript(false)
	            .runAsRoot(false);
	            

		 client.runScriptOnNodesMatching(NodePredicates.withIds(Iterables.toArray(workerIDs, String.class)), script, options);
		 
	}
	
	private String getKeyFromFile() throws Exception {
		 
		BufferedReader br = new BufferedReader(new FileReader(getOrCry("pemFilePath")));
		    try {
		        StringBuilder sb = new StringBuilder();
		        String line = br.readLine();

		        while (line != null) {
		            sb.append(line);
		            sb.append("\n");
		            line = br.readLine();
		        }
		        return sb.toString();
		    } finally {
		        br.close();
		    }
	}
}