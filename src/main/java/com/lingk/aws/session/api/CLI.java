package com.lingk.aws.session.api;

import java.util.concurrent.Callable;

import org.apache.commons.exec.DefaultExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@SpringBootApplication
public class CLI implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger(CLI.class);

	public static void main(String[] args) {
		SpringApplication.run(CLI.class, args);
	}

	@Command(name = "lingk", mixinStandardHelpOptions = true, version = "checksum 4.0", description = "lingk-cli")
	public class Login implements Callable<Integer> {

		@Option(names = { "region" }, required = true, arity = "0..1", description = "EKS Region", interactive = true)
		String region;

		@Option(names = { "name" }, required = true, arity = "0..1", description = "EKS Cluster", interactive = true)
		String name;

		public Integer call() throws Exception {
			org.apache.commons.exec.CommandLine cmd = new org.apache.commons.exec.CommandLine("aws eks --region us-east-1 update-kubeconfig --name fission-eks");
			DefaultExecutor exec = new DefaultExecutor();
			return exec.execute(cmd);
		}

	}

	public void run(String... args) throws Exception {
		int exitCode = new CommandLine(new Login()).execute("region=us-east-1", "name=fission-eks");
		System.exit(exitCode);
	}

}
