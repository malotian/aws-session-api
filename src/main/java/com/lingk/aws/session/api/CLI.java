package com.lingk.aws.session.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CLI {

	public static void main(String[] args) {

		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("aws", "eks", "--region", "us-east-1", "update-kubeconfig", "--name", "fission-eks");

		try {

			Process process = processBuilder.start();

			// blocked :(
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println(line);
			}

			int exitCode = process.waitFor();
			System.out.println("\nExited with error code : " + exitCode);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

}
